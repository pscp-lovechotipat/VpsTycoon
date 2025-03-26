package com.vpstycoon.ui.game.desktop.messenger.controllers;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.desktop.MessengerWindow;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import com.vpstycoon.ui.game.desktop.messenger.models.*;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.views.DashboardView;
import com.vpstycoon.ui.game.desktop.messenger.views.RequestListView;
import com.vpstycoon.ui.game.desktop.messenger.views.VMSelectionDialog;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MessengerController {
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Company company;
    private final ChatHistoryManager chatHistoryManager;
    private final MessengerWindow messengerWindow;
    private final RequestListView requestListView;
    private final ChatAreaView chatAreaView;
    private final DashboardView dashboardView;
    private final StackPane rootStack;
    private final GameTimeManager gameTimeManager;
    private Runnable onClose;
    private final Map<VPSOptimization.VM, CustomerRequest> vmAssignments = new HashMap<>();
    private final Map<CustomerRequest, ProgressBar> provisioningProgressBars = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final VMProvisioningManager vmProvisioningManager;
    private final RentalManager rentalManager;
    private final SkillPointsManager skillPointsManager;

    public MessengerController(RequestManager requestManager, VPSManager vpsManager, Company company,
                               ChatHistoryManager chatHistoryManager, StackPane rootStack,
                               GameTimeManager gameTimeManager, Runnable onClose) {
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.chatHistoryManager = chatHistoryManager;
        this.rootStack = rootStack;
        this.gameTimeManager = gameTimeManager;
        this.onClose = () -> {
            chatHistoryManager.saveChatHistory();
            cleanup();
            onClose.run();
        };

        this.messengerWindow = new MessengerWindow(chatHistoryManager);
        this.requestListView = messengerWindow.getRequestListView();
        this.chatAreaView = messengerWindow.getChatAreaView();
        this.dashboardView = messengerWindow.getDashboardView();

        this.vmProvisioningManager = new VMProvisioningManager(chatHistoryManager, chatAreaView, provisioningProgressBars);
        this.rentalManager = new RentalManager(chatHistoryManager, chatAreaView, company, gameTimeManager);
        this.skillPointsManager = new SkillPointsManager(chatHistoryManager, chatAreaView, ResourceManager.getInstance().getSkillPointsSystem());

        setupListeners();
        updateRequestList();
        updateDashboard();
        loadSkillLevels();

        this.rentalManager.setOnArchiveRequest(() -> archiveRequest(requestListView.getSelectedRequest()));
        this.rentalManager.setVMAssignment(vmAssignments);
        this.rentalManager.setOnUpdateDashboard(this::updateDashboard);
    }

    private void setupListeners() {
        requestManager.getRequests().addListener((ListChangeListener<CustomerRequest>) change -> {
            Platform.runLater(() -> {
                updateRequestList();
                updateDashboard();
            });
        });

        requestListView.getRequestView().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            chatAreaView.updateChatHeader(newVal);
            
            // Only enable the assignVM button if there are running VMs available
            boolean hasAvailableVMs = false;
            if (newVal != null && !newVal.isActive() && !newVal.isExpired()) {
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    hasAvailableVMs = vps.getVms().stream()
                            .anyMatch(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm));
                    if (hasAvailableVMs) break;
                }
            }
            
            chatAreaView.getAssignVMButton().setDisable(newVal == null || newVal.isActive() || newVal.isExpired() || !hasAvailableVMs);
            chatAreaView.getArchiveButton().setDisable(newVal == null || (!newVal.isActive() && !newVal.isExpired()));
            if (newVal != null) {
                updateChatWithRequestDetails(newVal);
            }
        });

        chatAreaView.getSendButton().setOnAction(e -> {
            String message = chatAreaView.getMessageInput().getText();
            if (!message.isEmpty()) {
                CustomerRequest selected = requestListView.getSelectedRequest();
                if (selected != null) {
                    chatHistoryManager.addMessage(selected, new ChatMessage(MessageType.USER, message, new HashMap<>()));
                    chatAreaView.addUserMessage(message);
                    chatAreaView.getMessageInput().clear();

                    if (!selected.isActive()) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    chatHistoryManager.addMessage(selected, new ChatMessage(MessageType.CUSTOMER,
                                            "Thanks for your response! Can you help me with my VM request?", new HashMap<>()));
                                    chatAreaView.addCustomerMessage(selected, "Thanks for your response! Can you help me with my VM request?");
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    } else {
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    chatHistoryManager.addMessage(selected, new ChatMessage(MessageType.CUSTOMER,
                                            "Thank you for checking in! The VM is working great.", new HashMap<>()));
                                    chatAreaView.addCustomerMessage(selected, "Thank you for checking in! The VM is working great.");
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                }
            }
        });

        // ปรับปรุงการจัดการปุ่ม Assign VM
        chatAreaView.getAssignVMButton().setOnAction(e -> {
            CustomerRequest selected = requestListView.getSelectedRequest();
            if (selected != null && !selected.isActive()) {
                List<VPSOptimization.VM> allAvailableVMs = new ArrayList<>();
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    allAvailableVMs.addAll(vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                            .collect(Collectors.toList()));
                }
                if (allAvailableVMs.isEmpty()) {
                    chatAreaView.addSystemMessage("No available VMs to assign. Please create new VMs first.");
                    return;
                }
                VMSelectionDialog dialog = new VMSelectionDialog(allAvailableVMs, rootStack);
                dialog.setOnConfirm(() -> {
                    VPSOptimization.VM selectedVM = dialog.getSelectedVM();
                    if (selectedVM != null) {
                        // Assign VM ทันทีเมื่อกด CONFIRM
                        vmAssignments.put(selectedVM, selected); // ล็อก VM
                        selected.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                        chatAreaView.addSystemMessage("VM selected and assigned to request.");
                        chatAreaView.getAssignVMButton().setDisable(true); // ปิดปุ่ม Assign VM ทันที
                        updateDashboard(); // อัพเดต UI เพื่อลดจำนวน VM ทันที
                        updateRequestList(); // อัพเดตสถานะคำขอ

                        // เริ่ม provisioning หลังจาก assign
                        vmProvisioningManager.startVMProvisioning(selected, selectedVM, () -> {
                            completeVMProvisioning(selected, selectedVM);
                        });
                    }
                });
                
                // Dialog is automatically shown when created, no need to call show()
            }
        });

        chatAreaView.getArchiveButton().setOnAction(e -> archiveRequest(requestListView.getSelectedRequest()));
    }

    private void updateRequestList() {
        requestListView.updateRequestList(requestManager.getRequests());
    }

    private void updateDashboard() {
        // First, check for any expired requests and release their VMs
        releaseExpiredVMs();
        
        // Update dashboard with available VM count
        int availableVMs = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            availableVMs += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .count();
        }
        dashboardView.updateDashboard(company.getRating(), requestManager.getRequests().size(), availableVMs, vpsManager.getVPSMap().size());
        
        // Update the AssignVMButton status for selected request
        CustomerRequest selected = requestListView.getSelectedRequest();
        if (selected != null && !selected.isActive() && !selected.isExpired()) {
            chatAreaView.getAssignVMButton().setDisable(availableVMs <= 0);
        }
    }

    /**
     * Checks for expired requests and releases their VMs
     */
    private void releaseExpiredVMs() {
        List<VPSOptimization.VM> vmsToRelease = new ArrayList<>();
        
        // Find all VMs with expired requests
        for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
            if (entry.getValue().isExpired()) {
                vmsToRelease.add(entry.getKey());
            }
        }
        
        // Release all expired VMs but don't remove the requests
        for (VPSOptimization.VM vm : vmsToRelease) {
            CustomerRequest request = vmAssignments.get(vm);
            if (request != null) {
                // เพิ่มข้อความในแชทเพื่อแจ้งว่า VM ถูกปล่อยคืนแล้ว
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                    "Contract expired and VM released. You can archive this request.", new HashMap<>()));
                
                // ถ้า request นี้ถูกเลือกอยู่ตอนนี้ ให้แสดงข้อความในแชท
                CustomerRequest selectedRequest = requestListView.getSelectedRequest();
                if (selectedRequest != null && selectedRequest.equals(request)) {
                    chatAreaView.addSystemMessage("Contract expired and VM released. You can archive this request.");
                }
            }
            
            // ปล่อย VM ออกจาก assignments แต่ยังคงเก็บ request ไว้ในสถานะ expired
            vmAssignments.remove(vm);
        }
        
        // อัพเดต UI ถ้ามีการเปลี่ยนแปลง
        if (!vmsToRelease.isEmpty()) {
            updateRequestList();
            // ตรวจสอบว่าปุ่ม Archive ควรเปิดหรือปิดสำหรับ request ที่เลือกในขณะนี้
            CustomerRequest selectedRequest = requestListView.getSelectedRequest();
            if (selectedRequest != null) {
                boolean shouldEnableArchive = selectedRequest.isActive() || selectedRequest.isExpired();
                chatAreaView.getArchiveButton().setDisable(!shouldEnableArchive);
            }
        }
    }

    private void updateChatWithRequestDetails(CustomerRequest request) {
        if (request != null) {
            List<ChatMessage> chatHistory = chatHistoryManager.getChatHistory(request);
            if (chatHistory == null || chatHistory.isEmpty()) {
                String requestMessage = "Hello! I need a VM with the following specs:\n" +
                        "• " + request.getRequiredVCPUs() + " vCPUs\n" +
                        "• " + request.getRequiredRam() + " RAM\n" +
                        "• " + request.getRequiredDisk() + " Disk\n\n" +
                        "Can you help me set this up?";
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER, requestMessage, new HashMap<>()));

                // เพิ่มข้อความเกี่ยวกับสถานะของ request
                if (request.isExpired()) {
                    chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, 
                        "This contract has expired and is waiting to be archived.", new HashMap<>()));
                }
            }
            
            chatAreaView.loadChatHistory(request);
            
            // แสดงข้อความสถานะเพิ่มเติมสำหรับ request ที่หมดอายุแล้ว
            if (request.isExpired() && !vmAssignments.containsValue(request)) {
                chatAreaView.addSystemMessage("This request can be archived now to free up space in the request list.");
            }
        } else {
            chatAreaView.clearMessages();
        }
    }

    private void completeVMProvisioning(CustomerRequest request, VPSOptimization.VM vm) {
        rentalManager.setupRentalPeriod(request, vm);
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "VM provisioning completed successfully", new HashMap<>()));
        chatAreaView.addSystemMessage("VM provisioning completed successfully");
        skillPointsManager.awardSkillPoints(request, 0.2);
        updateRequestList();
        updateDashboard(); // อัพเดต UI อีกครั้งหลัง provisioning เสร็จ
    }

    private void archiveRequest(CustomerRequest selected) {
        if (selected != null && (selected.isActive() || selected.isExpired())) {
            VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                    .filter(entry -> entry.getValue() == selected)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (assignedVM != null) {
                releaseVM(assignedVM, true);
            }
            requestManager.getRequests().remove(selected);
            chatAreaView.clearMessages();
            chatAreaView.getAssignVMButton().setDisable(false); // Enable ปุ่ม Assign VM ใหม่หลัง archive
            updateRequestList();
            updateDashboard();
        }
    }

    public void releaseVM(VPSOptimization.VM vm, boolean isArchiving) {
        CustomerRequest request = vmAssignments.get(vm);
        if (request != null) {
            if (isArchiving) {
                requestManager.getRequests().remove(request);
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Request archived and VM released", new HashMap<>()));
                chatAreaView.addSystemMessage("Request archived and VM released");
            } else {
                request.markAsExpired();
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Contract expired and VM released", new HashMap<>()));
                chatAreaView.addSystemMessage("Contract expired and VM released");
            }
        }
        vmAssignments.remove(vm); // ปล่อย VM ออกจากการล็อก
        updateDashboard(); // อัพเดต UI เพื่อแสดงจำนวน VM ที่ว่างเพิ่มขึ้น
        
        // อัปเดตปุ่ม Archive หลังจากปล่อย VM
        CustomerRequest selectedRequest = requestListView.getSelectedRequest();
        if (selectedRequest != null) {
            boolean shouldEnableArchive = selectedRequest.isActive() || selectedRequest.isExpired();
            chatAreaView.getArchiveButton().setDisable(!shouldEnableArchive);
        }
    }

    private void loadSkillLevels() {
        try {
            java.lang.reflect.Field skillLevelsField = com.vpstycoon.ui.game.status.CircleStatusButton.class
                    .getDeclaredField("skillLevels");
            skillLevelsField.setAccessible(true);
            HashMap<String, Integer> skillLevels = (HashMap<String, Integer>) skillLevelsField.get(null);
            int deployLevel = skillLevels.getOrDefault("Deploy", 1);
            vmProvisioningManager.setDeployLevel(deployLevel);
            System.out.println("Loaded skill levels: " + skillLevels);
        } catch (Exception e) {
            System.err.println("Error loading skill levels: " + e.getMessage());
            vmProvisioningManager.setDeployLevel(1);
        }
    }

    private void cleanup() {
        // ปิด scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        // ยกเลิกการเชื่อมต่อของ RentalManager กับ GameTimeManager
        if (rentalManager != null) {
            rentalManager.detachFromTimeManager();
        }
        
        // ล้าง collections ต่างๆ
        vmAssignments.clear();
        provisioningProgressBars.clear();
        
        System.out.println("MessengerController cleanup completed");
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void close() {
        try {
            // บันทึกประวัติแชทก่อนปิด
            if (chatHistoryManager != null) {
                chatHistoryManager.saveChatHistory();
                System.out.println("Chat history saved on close");
            }
            
            // ทำความสะอาดทรัพยากร
            cleanup();
            
            // เรียกฟังก์ชันเมื่อปิด
            if (onClose != null) {
                onClose.run();
            }
        } catch (Exception e) {
            System.err.println("Error during MessengerController close: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MessengerWindow getMessengerWindow() {
        return messengerWindow;
    }
}