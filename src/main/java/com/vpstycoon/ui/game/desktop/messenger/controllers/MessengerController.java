package com.vpstycoon.ui.game.desktop.messenger.controllers;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
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
import com.vpstycoon.game.GameState;
import com.vpstycoon.ui.game.rack.Rack;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Random;

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

        
        chatHistoryManager.setMessengerController(this);
        
        
        chatHistoryManager.updateCustomerRequestReferences();

        
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null) {
            int savedFreeVmCount = currentState.getFreeVmCount();
            if (savedFreeVmCount > 0) {
                
                company.setAvailableVMs(savedFreeVmCount);
                System.out.println("ข้อมูลเริ่มต้น: โหลด Free VM count จาก GameState: " + savedFreeVmCount);
            }
        }
        
        
        System.out.println("ข้อมูลเริ่มต้น: โหลด VPS จาก GameState...");
        loadVPSFromGameState();

        setupListeners();
        updateRequestList();
        
        
        this.rentalManager.setOnArchiveRequest(() -> archiveRequest(requestListView.getSelectedRequest()));
        this.rentalManager.setVMAssignment(vmAssignments);
        this.rentalManager.setOnUpdateDashboard(this::updateDashboard);
        
        loadSkillLevels();
        
        
        
        Platform.runLater(() -> {
            System.out.println("ข้อมูลเริ่มต้น: อัพเดต Dashboard ครั้งแรกหลังจากตั้งค่าทั้งหมดเรียบร้อยแล้ว");
            updateDashboard();
        });
    }

    private void setupListeners() {
        requestManager.getRequests().addListener((ListChangeListener<CustomerRequest>) change -> {
            Platform.runLater(() -> {
                System.out.println("มีการเปลี่ยนแปลงรายการคำขอ...");
                System.out.println("จำนวน free VM ก่อนอัพเดต: " + company.getAvailableVMs());
                
                
                updateRequestList();
                
                
                dashboardView.updateDashboard(
                    company.getRating(), 
                    requestManager.getRequests().size(), 
                    company.getAvailableVMs(), 
                    vpsManager.getVPSMap().size()
                );
                
                System.out.println("จำนวน free VM หลังอัพเดต: " + company.getAvailableVMs());
            });
        });

        requestListView.getRequestView().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            chatAreaView.updateChatHeader(newVal);
            
            
            boolean hasAvailableVMs = false;
            boolean customerAlreadyHasVM = false;
            
            if (newVal != null) {
                
                customerAlreadyHasVM = isRequestAssigned(newVal);
                
                if (!newVal.isActive() && !newVal.isExpired() && !customerAlreadyHasVM) {
                    for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                        hasAvailableVMs = vps.getVms().stream()
                                .anyMatch(vm -> "Running".equals(vm.getStatus()) && 
                                        !vmAssignments.containsKey(vm) && 
                                        !vm.isAssignedToCustomer());
                        if (hasAvailableVMs) break;
                    }
                }
            }
            
            chatAreaView.getAssignVMButton().setDisable(newVal == null || newVal.isActive() || 
                                                     newVal.isExpired() || !hasAvailableVMs || 
                                                     customerAlreadyHasVM);
            
            chatAreaView.getArchiveButton().setDisable(newVal == null || (!newVal.isActive() && !newVal.isExpired()));
            if (newVal != null) {
                updateChatWithRequestDetails(newVal);
                
                
                if (customerAlreadyHasVM && !newVal.isExpired()) {
                    VPSOptimization.VM assignedVM = null;
                    for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                        if (entry.getValue().equals(newVal)) {
                            assignedVM = entry.getKey();
                            break;
                        }
                    }
                    
                    
                    if (!newVal.isActive()) {
                        newVal.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                        System.out.println("ปรับสถานะลูกค้า " + newVal.getName() + " เป็น active เนื่องจากมี VM อยู่แล้ว");
                    }
                    
                    if (assignedVM != null) {
                        chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM " + assignedVM.getName() + " ถูกกำหนดไว้แล้ว");
                        
                        
                        if (!assignedVM.isAssignedToCustomer()) {
                            
                            assignedVM.assignToCustomer(
                                String.valueOf(newVal.getId()),
                                newVal.getName(),
                                ResourceManager.getInstance().getGameTimeManager().getGameTimeMs()
                            );
                            System.out.println("บันทึกข้อมูลลูกค้า " + newVal.getName() + " ลงใน VM " + assignedVM.getName());
                        }
                    } else {
                        chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM ถูกกำหนดไว้แล้ว");
                    }
                    
                    
                    chatAreaView.getAssignVMButton().setDisable(true);
                    chatAreaView.getArchiveButton().setDisable(false);
                    chatAreaView.updateChatHeader(newVal);
                }
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
                        Thread responseThread = new Thread(() -> {
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
                        });
                        responseThread.setDaemon(true);
                        responseThread.start();
                    } else {
                        Thread responseThread = new Thread(() -> {
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
                        });
                        responseThread.setDaemon(true);
                        responseThread.start();
                    }
                }
            }
        });

        
        chatAreaView.getAssignVMButton().setOnAction(e -> {
            CustomerRequest selected = requestListView.getSelectedRequest();
            if (selected != null && !selected.isActive()) {
                
                boolean hasAnyVMs = false;
                int totalServerCount = vpsManager.getVPSMap().size();
                
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    if (!vps.getVms().isEmpty() && vps.getVms().stream()
                            .anyMatch(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())) {
                        hasAnyVMs = true;
                        break;
                    }
                }
                
                
                if (!hasAnyVMs) {
                    if (company.getAvailableVMs() > 0) {
                        chatAreaView.addSystemMessage("ไม่สามารถหา VM จริงในระบบได้แม้ว่าระบบจะรายงานว่ามี " + 
                                                    company.getAvailableVMs() + " VM ที่ว่าง");
                    } else {
                        chatAreaView.addSystemMessage("ไม่มี VM ที่พร้อมใช้งาน โปรดสร้าง VM ใหม่ก่อน");
                    }
                    return;
                }
                
                
                List<VPSOptimization.VM> allAvailableVMs = new ArrayList<>();
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    allAvailableVMs.addAll(vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .collect(Collectors.toList()));
                }
                
                if (allAvailableVMs.isEmpty()) {
                    chatAreaView.addSystemMessage("ไม่มี VM ที่พร้อมใช้งาน โปรดสร้าง VM ใหม่ก่อน");
                    return;
                }
                
                
                VMSelectionDialog dialog = new VMSelectionDialog(allAvailableVMs, rootStack);
                dialog.setOnConfirm(() -> {
                    VPSOptimization.VM selectedVM = dialog.getSelectedVM();
                    if (selectedVM != null) {
                        
                        boolean customerAlreadyHasVM = false;
                        VPSOptimization.VM existingVM = null;
                        
                        for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                            if (entry.getValue().equals(selected)) {
                                customerAlreadyHasVM = true;
                                existingVM = entry.getKey();
                                break;
                            }
                        }
                        
                        if (customerAlreadyHasVM) {
                            
                            if (!selected.isActive() && !selected.isExpired()) {
                                selected.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                                System.out.println("ปรับสถานะลูกค้า " + selected.getName() + " เป็น active เนื่องจากมี VM อยู่แล้ว");
                            }
                            
                            
                            if (existingVM != null && !existingVM.isAssignedToCustomer()) {
                                existingVM.assignToCustomer(
                                    String.valueOf(selected.getId()),
                                    selected.getName(),
                                    ResourceManager.getInstance().getGameTimeManager().getGameTimeMs()
                                );
                                System.out.println("บันทึกข้อมูลลูกค้า " + selected.getName() + " ลงใน VM " + existingVM.getName());
                            }
                            
                            
                            if (existingVM != null) {
                                chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM " + existingVM.getName() + " ถูกกำหนดไว้แล้ว");
                            } else {
                                chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM ถูกกำหนดไว้แล้ว");
                            }
                            
                            
                            chatAreaView.getAssignVMButton().setDisable(true);
                            chatAreaView.getArchiveButton().setDisable(false);
                            chatAreaView.updateChatHeader(selected);
                            
                            
                            return;
                        }
                        
                        
                        
                        vmAssignments.put(selectedVM, selected); 
                        selected.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                        chatAreaView.addSystemMessage("VM selected and assigned to request.");
                        chatAreaView.getAssignVMButton().setDisable(true); 
                        
                        
                        int currentAvailableVMs = company.getAvailableVMs();
                        if (currentAvailableVMs > 0) {
                            currentAvailableVMs--;
                            company.setAvailableVMs(currentAvailableVMs);
                            ResourceManager.getInstance().getCurrentState().setFreeVmCount(currentAvailableVMs);
                            System.out.println("ลดจำนวน available VM เนื่องจากมีการ assign VM แล้ว: " + currentAvailableVMs);
                        }
                        
                        updateDashboard(); 
                        updateRequestList(); 

                        
                        vmProvisioningManager.startVMProvisioning(selected, selectedVM, () -> {
                            completeVMProvisioning(selected, selectedVM);
                        });
                    }
                });
                
                
            }
        });

        chatAreaView.getArchiveButton().setOnAction(e -> archiveRequest(requestListView.getSelectedRequest()));
    }

    private void updateRequestList() {
        
        if (requestManager == null) {
            System.out.println("⚠️ Error: requestManager is null");
            return;
        }
        
        
        ObservableList<CustomerRequest> requests = requestManager.getRequests();
        
        
        if (requests == null) {
            System.out.println("⚠️ Error: requests from requestManager is null");
            return;
        }
        
        
        System.out.println("📋 กำลังอัปเดตรายการคำขอ: พบ " + requests.size() + " รายการ");
        
        
        List<CustomerRequest> validRequests = new ArrayList<>();
        for (CustomerRequest request : requests) {
            if (request != null) {
                if (request.getTitle() == null || request.getRequiredVCPUs() <= 0) {
                    System.out.println("⚠️ พบข้อมูลคำขอที่ไม่สมบูรณ์: ข้าม");
                } else {
                    validRequests.add(request);
                }
            }
        }
        
        
        if (validRequests.size() != requests.size()) {
            System.out.println("ℹ️ กรองข้อมูลคำขอ: จำนวนที่สมบูรณ์ = " + validRequests.size() + "/" + requests.size());
        }
        
        
        requestListView.updateRequestList(validRequests);
    }

    private void updateDashboard() {
        
        releaseExpiredVMs();
        
        
        int availableVMs = 0;
        int totalServers = 0;
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        
        
        
        if (company.getAvailableVMs() >= 0) {
            availableVMs = company.getAvailableVMs();
            System.out.println("Dashboard: ใช้ค่า free VM ที่บันทึกใน Company: " + availableVMs);
            
            
            int vpsMgrServerCount = vpsManager.getVPSMap().size();
            if (vpsMgrServerCount > 0) {
                totalServers = vpsMgrServerCount;
                System.out.println("   จำนวนเซิร์ฟเวอร์จาก VPSManager: " + totalServers);
            }
        } else {
            
            
            int vpsMgrServerCount = vpsManager.getVPSMap().size();
            int vpsMgrVMCount = 0;
            
            if (vpsMgrServerCount > 0) {
                totalServers = vpsMgrServerCount;
                
                
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    vpsMgrVMCount += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                
                availableVMs = vpsMgrVMCount;
                System.out.println("Dashboard: ใช้ค่า free VM จากการนับ VM ที่ไม่มีลูกค้าใช้จริง: " + availableVMs);
                
                
                company.setAvailableVMs(availableVMs);
                if (currentState != null) {
                    currentState.setFreeVmCount(availableVMs);
                }
            } else {
                
                loadVPSFromGameState();
                
                
                vpsMgrServerCount = vpsManager.getVPSMap().size();
                if (vpsMgrServerCount > 0) {
                    totalServers = vpsMgrServerCount;
                    
                    
                    for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                        availableVMs += (int) vps.getVms().stream()
                                .filter(vm -> "Running".equals(vm.getStatus()) && 
                                        !vmAssignments.containsKey(vm) && 
                                        !vm.isAssignedToCustomer())
                                .count();
                    }
                    
                    System.out.println("   จำนวนเซิร์ฟเวอร์และ free VM หลังจากโหลด: " + totalServers + ", " + availableVMs);
                    
                    
                    company.setAvailableVMs(availableVMs);
                    if (currentState != null) {
                        currentState.setFreeVmCount(availableVMs);
                    }
                }
            }
            
            
            if (availableVMs == 0) {
                checkAndUpdateFromRack();
                
                
                int rackVMCount = 0;
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    rackVMCount += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                if (rackVMCount > 0) {
                    availableVMs = rackVMCount;
                    System.out.println("2. ใช้ค่า free VM หลังอัพเดตจาก Rack: " + availableVMs);
                    
                    
                    company.setAvailableVMs(availableVMs);
                    if (currentState != null) {
                        currentState.setFreeVmCount(availableVMs);
                    }
                } else if (currentState != null && currentState.getFreeVmCount() > 0) {
                    
                    availableVMs = currentState.getFreeVmCount();
                    company.setAvailableVMs(availableVMs);
                    System.out.println("3. ใช้ค่า free VM จาก GameState: " + availableVMs);
                }
            }
        }
        
        
        boolean hasAnyVMs = false;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            if (!vps.getVms().isEmpty()) {
                hasAnyVMs = true;
                break;
            }
        }
        
        if (!hasAnyVMs && availableVMs > 0 && totalServers > 0) {
            System.out.println("⚠️ พบความไม่สอดคล้อง: มีค่า availableVMs=" + availableVMs + 
                               " แต่ไม่มี VM objects จริงในระบบ");
            
            
            availableVMs = 0;
            company.setAvailableVMs(0);
            if (currentState != null) {
                currentState.setFreeVmCount(0);
            }
            System.out.println("ปรับค่า availableVMs เป็น 0 เนื่องจากไม่มี VM objects จริงในระบบ");
        }
        
        
        
        
        int assignedVMCount = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            assignedVMCount += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && vm.isAssignedToCustomer())
                    .count();
        }
        
        
        if (assignedVMCount > 0) {
            System.out.println("พบ VM ที่มีลูกค้าใช้งานแล้ว: " + assignedVMCount + " VM");
        }
        
        
        if (!vmAssignments.isEmpty()) {
            System.out.println("พบ VM ที่กำลังถูก assign ในระบบ: " + vmAssignments.size() + " VM");
        }
        
        
        validateVMConsistency();
        
        
        dashboardView.updateDashboard(company.getRating(), requestManager.getRequests().size(), company.getAvailableVMs(), totalServers);
        
        
        
        
        
        if (currentState != null) {
            currentState.setFreeVmCount(company.getAvailableVMs());
        }
        
        
        CustomerRequest selected = requestListView.getSelectedRequest();
        if (selected != null && !selected.isActive() && !selected.isExpired()) {
            chatAreaView.getAssignVMButton().setDisable(company.getAvailableVMs() <= 0);
        }
    }
    
    
    private boolean checkAndUpdateFromRack() {
        ResourceManager resourceManager = ResourceManager.getInstance();
        Rack rack = resourceManager.getRack();
        
        if (rack != null) {
            
            List<VPSOptimization> installedServers = rack.getInstalledVPS();
            if (installedServers != null && !installedServers.isEmpty()) {
                int rackServers = installedServers.size();
                int rackVMs = 0;
                
                System.out.println("ตรวจสอบเซิร์ฟเวอร์ในแร็ค: " + rackServers + " เครื่อง");
                
                
                for (VPSOptimization vps : installedServers) {
                    String vpsId = vps.getVpsId();
                    if (!vpsManager.getVPSMap().containsKey(vpsId)) {
                        vpsManager.addVPS(vpsId, vps);
                        System.out.println("   เพิ่มเซิร์ฟเวอร์ " + vpsId + " จากแร็คเข้าสู่ VPSManager");
                    }
                    
                    
                    
                    int maxVMsPerServer = vps.getVCPUs(); 
                    int existingVMs = vps.getVms().size();
                    
                    
                    if (existingVMs >= maxVMsPerServer) {
                        System.out.println("   เซิร์ฟเวอร์ " + vpsId + " มี VM เกิน capacity แล้ว (" + 
                                         existingVMs + "/" + maxVMsPerServer + ")");
                        continue;
                    }
                    
                    
                    int availableVMsInServer = (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) &&
                                    !vm.isAssignedToCustomer())
                            .count();
                    
                    
                    int remainingCapacity = maxVMsPerServer - existingVMs;
                    if (remainingCapacity > 0) {
                        
                        rackVMs += availableVMsInServer;
                        
                        System.out.println("   เซิร์ฟเวอร์ " + vpsId + ": มี VM ว่างพร้อมใช้ " + 
                                         availableVMsInServer + " VM (capacity เหลือ " + 
                                         remainingCapacity + " VM)");
                    }
                }
                
                System.out.println("   VM ในแร็คที่พร้อมใช้งาน (ไม่มีลูกค้าใช้งาน): " + rackVMs + " VM");
                return true;
            }
        }
        return false;
    }
    
    
    private void createVirtualMachines(int vmCount, int serverCount) {
        
        int remainingVMs = vmCount;
        
        
        if (serverCount > 0) {
            
            Map<String, Integer> serverCapacity = new HashMap<>();
            int totalAvailableCapacity = 0;
            
            
            for (Map.Entry<String, VPSOptimization> entry : vpsManager.getVPSMap().entrySet()) {
                String serverId = entry.getKey();
                VPSOptimization vps = entry.getValue();
                
                
                
                int maxVMsPerServer = vps.getVCPUs();
                
                
                int existingVMs = vps.getVms().size();
                
                
                int remainingCapacity = Math.max(0, maxVMsPerServer - existingVMs);
                
                
                serverCapacity.put(serverId, remainingCapacity);
                totalAvailableCapacity += remainingCapacity;
                
                System.out.println("เซิร์ฟเวอร์ " + serverId + " มี capacity เหลือ " + 
                                  remainingCapacity + " VM (max: " + maxVMsPerServer + 
                                  ", มีอยู่แล้ว: " + existingVMs + ")");
            }
            
            
            if (totalAvailableCapacity < vmCount) {
                System.out.println("⚠️ เตือน: จำนวน VM ที่ต้องการสร้าง (" + vmCount + 
                                 ") มากกว่า capacity รวมที่เหลืออยู่ (" + totalAvailableCapacity + 
                                 ") จะสร้างเท่าที่ capacity เหลือ");
                
                
                remainingVMs = totalAvailableCapacity;
                
                
                company.setAvailableVMs(totalAvailableCapacity);
                ResourceManager.getInstance().getCurrentState().setFreeVmCount(totalAvailableCapacity);
                System.out.println("ปรับค่า availableVMs เป็น " + totalAvailableCapacity + " ตาม capacity จริง");
            }
            
            
            for (Map.Entry<String, VPSOptimization> entry : vpsManager.getVPSMap().entrySet()) {
                String serverId = entry.getKey();
                VPSOptimization vps = entry.getValue();
                
                
                int availableCapacity = serverCapacity.get(serverId);
                
                
                int vmsToCreate = Math.min(remainingVMs, availableCapacity);
                
                for (int i = 0; i < vmsToCreate; i++) {
                    
                    String vmName = "vm-" + System.currentTimeMillis() + "-" + i;
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                        vmName,
                        1, 
                        1, 
                        10  
                    );
                    
                    
                    newVM.setIp(generateRandomIp());
                    newVM.setStatus("Running");
                    
                    
                    CustomerRequest assignedRequest = findAssignedCustomerFromGameState(vmName);
                    if (assignedRequest != null) {
                        newVM.assignToCustomer(
                            String.valueOf(assignedRequest.getId()), 
                            assignedRequest.getName(), 
                            assignedRequest.getLastPaymentTime()
                        );
                        System.out.println("VM " + vmName + " ถูกกำหนดให้ลูกค้า " + assignedRequest.getName() + " แล้ว");
                    }
                    
                    vps.addVM(newVM);
                    System.out.println("สร้าง VM ใหม่ใน server " + vps.getVpsId() + ": " + vmName);
                    remainingVMs--;
                }
                
                if (remainingVMs <= 0) break;
            }
            
            
            if (remainingVMs > 0) {
                System.out.println("⚠️ ไม่สามารถสร้าง VM ได้ครบตามต้องการ เนื่องจาก capacity ไม่พอ ยังเหลืออีก " + 
                                  remainingVMs + " VM ที่ต้องการสร้าง");
                
                
                int actualCreated = vmCount - remainingVMs;
                if (company.getAvailableVMs() > actualCreated) {
                    company.setAvailableVMs(actualCreated);
                    ResourceManager.getInstance().getCurrentState().setFreeVmCount(actualCreated);
                    System.out.println("ปรับค่า availableVMs เป็น " + actualCreated + " ตามที่สร้างได้จริง");
                }
            }
        } else {
            System.out.println("⚠️ ไม่สามารถสร้าง VM ได้เนื่องจากไม่มีเซิร์ฟเวอร์ในระบบ");
            
            company.setAvailableVMs(0);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(0);
        }
    }

    
    private void releaseExpiredVMs() {
        List<VPSOptimization.VM> vmsToRelease = new ArrayList<>();
        
        
        for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
            if (entry.getValue().isExpired()) {
                vmsToRelease.add(entry.getKey());
            }
        }
        
        
        for (VPSOptimization.VM vm : vmsToRelease) {
            CustomerRequest request = vmAssignments.get(vm);
            if (request != null) {
                
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                    "Contract expired and VM released. You can archive this request.", new HashMap<>()));
                
                
                CustomerRequest selectedRequest = requestListView.getSelectedRequest();
                if (selectedRequest != null && selectedRequest.equals(request)) {
                    chatAreaView.addSystemMessage("Contract expired and VM released. You can archive this request.");
                }
            }
            
            
            vm.releaseFromCustomer();
            
            
            vmAssignments.remove(vm);
            
            
            int currentAvailableVMs = company.getAvailableVMs();
            currentAvailableVMs++;
            company.setAvailableVMs(currentAvailableVMs);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(currentAvailableVMs);
            System.out.println("เพิ่มจำนวน available VM เนื่องจากคำขอหมดอายุ: " + currentAvailableVMs);
        }
        
        
        if (!vmsToRelease.isEmpty()) {
            updateRequestList();
            
            CustomerRequest selectedRequest = requestListView.getSelectedRequest();
            if (selectedRequest != null) {
                boolean shouldEnableArchive = selectedRequest.isActive() || selectedRequest.isExpired();
                chatAreaView.getArchiveButton().setDisable(!shouldEnableArchive);
            }
            
            
            validateVMConsistency();
        }
    }

    
    private void validateVMConsistency() {
        
        int countFromVMs = 0;
        int invalidVMs = 0;
        Map<String, List<VPSOptimization.VM>> vmsToRemove = new HashMap<>();
        
        for (Map.Entry<String, VPSOptimization> entry : vpsManager.getVPSMap().entrySet()) {
            String serverId = entry.getKey();
            VPSOptimization vps = entry.getValue();
            
            
            int maxVMsPerServer = vps.getVCPUs(); 
            int totalVMsInServer = vps.getVms().size();
            
            
            if (totalVMsInServer > maxVMsPerServer) {
                System.out.println("⚠️ ตรวจพบว่าเซิร์ฟเวอร์ " + serverId + " มี VM เกิน capacity: " + 
                                  totalVMsInServer + " VM (max: " + maxVMsPerServer + " VM)");
                
                
                List<VPSOptimization.VM> allVMs = new ArrayList<>(vps.getVms());
                List<VPSOptimization.VM> unusedVMs = new ArrayList<>();
                List<VPSOptimization.VM> assignedVMs = new ArrayList<>();
                
                for (VPSOptimization.VM vm : allVMs) {
                    if (!vm.isAssignedToCustomer() && !vmAssignments.containsKey(vm)) {
                        unusedVMs.add(vm);
                    } else {
                        assignedVMs.add(vm);
                    }
                }
                
                
                int excessVMs = totalVMsInServer - maxVMsPerServer;
                List<VPSOptimization.VM> vmList = new ArrayList<>();
                
                
                int toRemoveFromUnused = Math.min(excessVMs, unusedVMs.size());
                for (int i = 0; i < toRemoveFromUnused; i++) {
                    vmList.add(unusedVMs.get(i));
                }
                
                
                if (toRemoveFromUnused < excessVMs) {
                    int toRemoveFromAssigned = excessVMs - toRemoveFromUnused;
                    System.out.println("⚠️ ต้องลบ VM ที่มีลูกค้าใช้งานออก " + toRemoveFromAssigned + 
                                      " VM เนื่องจาก capacity ไม่พอ");
                    
                    
                    for (int i = 0; i < Math.min(toRemoveFromAssigned, assignedVMs.size()); i++) {
                        vmList.add(assignedVMs.get(i));
                    }
                }
                
                
                vmsToRemove.put(serverId, vmList);
                invalidVMs += vmList.size();
            }
            
            
            countFromVMs += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && 
                            !vmAssignments.containsKey(vm) && 
                            !vm.isAssignedToCustomer())
                    .count();
        }
        
        
        if (invalidVMs > 0) {
            System.out.println("⚠️ จะดำเนินการลบ VM ที่เกิน capacity จำนวน " + invalidVMs + " VM");
            
            for (Map.Entry<String, List<VPSOptimization.VM>> entry : vmsToRemove.entrySet()) {
                String serverId = entry.getKey();
                List<VPSOptimization.VM> vmList = entry.getValue();
                VPSOptimization vps = vpsManager.getVPSMap().get(serverId);
                
                for (VPSOptimization.VM vm : vmList) {
                    
                    CustomerRequest request = null;
                    for (Map.Entry<VPSOptimization.VM, CustomerRequest> vmEntry : vmAssignments.entrySet()) {
                        if (vmEntry.getKey().equals(vm)) {
                            request = vmEntry.getValue();
                            break;
                        }
                    }
                    
                    
                    if (request != null) {
                        System.out.println("⚠️ ลบ VM " + vm.getName() + " ที่กำลังถูกใช้งานโดยลูกค้า " + 
                                          request.getName() + " เนื่องจากเกิน capacity");
                        
                        
                        request.deactivate();
                        
                        
                        vmAssignments.remove(vm);
                    } else if (vm.isAssignedToCustomer()) {
                        System.out.println("⚠️ ลบ VM " + vm.getName() + " ที่กำลังถูกใช้งานโดยลูกค้า " + 
                                          vm.getCustomerName() + " เนื่องจากเกิน capacity");
                    } else {
                        System.out.println("ลบ VM " + vm.getName() + " ที่ไม่มีลูกค้าใช้งาน เนื่องจากเกิน capacity");
                    }
                    
                    
                    vps.removeVM(vm);
                }
            }
            
            
            countFromVMs = 0;
            for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                countFromVMs += (int) vps.getVms().stream()
                        .filter(vm -> "Running".equals(vm.getStatus()) && 
                                !vmAssignments.containsKey(vm) && 
                                !vm.isAssignedToCustomer())
                        .count();
            }
        }
        
        
        int storedAvailableVMs = company.getAvailableVMs();
        
        
        if (countFromVMs != storedAvailableVMs) {
            System.out.println("⚠️ ตรวจพบความไม่สอดคล้องของข้อมูล: จำนวน VM จริงที่ว่าง = " + countFromVMs + 
                              " แต่ค่า availableVMs = " + storedAvailableVMs);
            company.setAvailableVMs(countFromVMs);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(countFromVMs);
            System.out.println("✅ แก้ไขแล้ว: กำหนดค่า availableVMs เป็น " + countFromVMs);
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

                
                if (request.isExpired()) {
                    chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, 
                        "This contract has expired and is waiting to be archived.", new HashMap<>()));
                }
            }
            
            chatAreaView.loadChatHistory(request);
            
            
            if (request.isExpired() && !vmAssignments.containsValue(request)) {
                chatAreaView.addSystemMessage("This request can be archived now to free up space in the request list.");
            }
        } else {
            chatAreaView.clearMessages();
        }
    }

    private void completeVMProvisioning(CustomerRequest request, VPSOptimization.VM vm) {
        
        request.activate(gameTimeManager.getGameTimeMs());
        chatAreaView.getArchiveButton().setDisable(false);
        chatAreaView.getAssignVMButton().setDisable(true);
        chatAreaView.updateChatHeader(request);
        
        
        vm.assignToCustomer(
            String.valueOf(request.getId()), 
            request.getName(), 
            gameTimeManager.getGameTimeMs()
        );
        System.out.println("บันทึกข้อมูลลูกค้า " + request.getName() + " (ID: " + request.getId() + ") ลงใน VM: " + vm.getName());
        
        
        request.assignToVM(vm.getId());
        System.out.println("ตั้งค่า assignToVM " + vm.getId() + " ให้กับ request " + request.getName());
        
        
        double ratingChange = vmProvisioningManager.calculateRatingChange(
            request, 
            vm.getVcpu(), 
            Integer.parseInt(vm.getRam().replaceAll("[^0-9]", "")), 
            Integer.parseInt(vm.getDisk().replaceAll("[^0-9]", ""))
        );
        
        
        Company company = ResourceManager.getInstance().getCompany();
        company.setRating(company.getRating() + ratingChange);
        System.out.println("Rating change applied: " + String.format("%.2f", ratingChange) + 
                ". New rating: " + String.format("%.2f", company.getRating()));
        
        
        vmAssignments.put(vm, request);
        
        
        double paymentAmount = request.getPaymentAmount();
        
        
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        double securityBonus = skillPointsSystem.getSecurityPaymentBonus();
        if (securityBonus > 0) {
            double bonusAmount = paymentAmount * securityBonus;
            paymentAmount += bonusAmount;
            System.out.println("Security bonus applied: +" + String.format("%.2f", bonusAmount) + 
                    " (" + (securityBonus * 100) + "%)");
        }
        
        
        company.addMoney(paymentAmount);
        System.out.println("Received initial payment of " + String.format("%.2f", paymentAmount) + 
                " from " + request.getName() + " (" + request.getRentalPeriodType().getDisplayName() + ")");
        
        
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, 
            "VM provisioning completed successfully", new HashMap<>()));
        chatAreaView.addSystemMessage("VM provisioning completed successfully");
        chatAreaView.addSystemMessage("Received initial payment of " + String.format("%.2f", paymentAmount) + 
                " from " + request.getName());
        
        
        String ratingMessage = ratingChange >= 0 
            ? "Customer is satisfied with the VM specs. Rating increased by " + String.format("%.2f", ratingChange)
            : "Customer expected better VM specs. Rating decreased by " + String.format("%.2f", Math.abs(ratingChange));
        chatAreaView.addSystemMessage(ratingMessage);
        
        
        skillPointsManager.awardSkillPoints(request, 0.2);

        validateVMConsistency();
        
        
        updateRequestList();

        
        updateDashboard();
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
            } else if (selected.isExpired()) {
                
                
                System.out.println("ไม่พบ VM สำหรับ request ที่หมดอายุ: " + selected.getName() + " แต่จะเพิ่ม availableVMs เพื่อแก้ไขความไม่สอดคล้อง");
                
                
                int countFromVMs = 0;
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    countFromVMs += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                
                int storedAvailableVMs = company.getAvailableVMs();
                
                
                if (countFromVMs != storedAvailableVMs) {
                    company.setAvailableVMs(countFromVMs);
                    ResourceManager.getInstance().getCurrentState().setFreeVmCount(countFromVMs);
                    System.out.println("แก้ไขความไม่สอดคล้อง: จำนวน VM จริงที่ว่าง = " + countFromVMs + 
                                      " แต่ค่า availableVMs = " + storedAvailableVMs);
                }
            }
            
            requestManager.getRequests().remove(selected);
            chatAreaView.clearMessages();
            chatAreaView.getAssignVMButton().setDisable(false); 
            updateRequestList();
            updateDashboard();
        }
    }

    public void releaseVM(VPSOptimization.VM vm, boolean isArchiving) {
        
        CustomerRequest requestToRelease = null;
        for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
            if (entry.getKey().equals(vm)) {
                requestToRelease = entry.getValue();
                break;
            }
        }
        
        if (requestToRelease != null) {
            if (isArchiving) {
                
                chatHistoryManager.addMessage(requestToRelease, new ChatMessage(MessageType.SYSTEM,
                    "Request archived and VM released.", new HashMap<>()));
                chatAreaView.addSystemMessage("Request archived and VM released.");
                
                
                requestManager.getRequests().remove(requestToRelease);
            } else {
                
                requestToRelease.markAsExpired();
                chatHistoryManager.addMessage(requestToRelease, new ChatMessage(MessageType.SYSTEM,
                    "Contract expired and VM released.", new HashMap<>()));
                chatAreaView.addSystemMessage("Contract expired and VM released.");
            }
            
            
            vm.releaseFromCustomer();
            
            
            requestToRelease.unassignFromVM();
            System.out.println("ลบการเชื่อมโยง assignToVM ของ request " + requestToRelease.getName());
            
            
            vmAssignments.remove(vm);
            
            
            int currentAvailableVMs = company.getAvailableVMs();
            currentAvailableVMs++;
            company.setAvailableVMs(currentAvailableVMs);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(currentAvailableVMs);
            System.out.println("เพิ่มจำนวน available VM เนื่องจากมีการปล่อย VM: " + currentAvailableVMs);
            
            
            updateDashboard();
            updateRequestList();
            
            
            CustomerRequest selectedRequest = requestListView.getSelectedRequest();
            if (selectedRequest != null) {
                boolean shouldEnableArchive = selectedRequest.isActive() || selectedRequest.isExpired();
                chatAreaView.getArchiveButton().setDisable(!shouldEnableArchive);
            }
        }
    }

    private void loadSkillLevels() {
        try {
            
            SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
            int deployLevel = skillPointsSystem.getSkillLevel(SkillPointsSystem.SkillType.DEPLOY);
            vmProvisioningManager.setDeployLevel(deployLevel);
            System.out.println("Loaded deploy skill level: " + deployLevel);
        } catch (Exception e) {
            System.err.println("Error loading skill levels: " + e.getMessage());
            vmProvisioningManager.setDeployLevel(1);
        }
    }

    private void cleanup() {
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        
        
        if (rentalManager != null) {
            rentalManager.detachFromTimeManager();
        }
        
        
        vmAssignments.clear();
        provisioningProgressBars.clear();
        
        System.out.println("MessengerController cleanup completed");
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void close() {
        try {
            
            if (chatHistoryManager != null) {
                chatHistoryManager.saveChatHistory();
                System.out.println("Chat history saved on close");
            }
            
            
            cleanup();
            
            
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

    private String generateRandomIp() {
        
        Random random = new Random();
        return "10." + 
               random.nextInt(255) + "." + 
               random.nextInt(255) + "." + 
               (random.nextInt(254) + 1); 
    }

    
    private void loadVPSFromGameState() {
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null && currentState.getGameObjects() != null) {
            int vpsCount = 0;
            List<VPSOptimization> loadedVPSList = new ArrayList<>();
            
            
            for (Object obj : currentState.getGameObjects()) {
                if (obj instanceof VPSOptimization) {
                    VPSOptimization vps = (VPSOptimization) obj;
                    String vpsId = vps.getVpsId();
                    
                    
                    if (!vpsManager.getVPSMap().containsKey(vpsId)) {
                        vpsManager.addVPS(vpsId, vps);
                        loadedVPSList.add(vps);
                        vpsCount++;
                    } else {
                        
                        loadedVPSList.add(vpsManager.getVPSMap().get(vpsId));
                    }
                }
            }
            
            System.out.println("โหลด VPS จาก GameState จำนวน " + vpsCount + " เครื่อง");
            
            
            for (VPSOptimization vps : loadedVPSList) {
                for (VPSOptimization.VM vm : vps.getVms()) {
                    if (vm.isAssignedToCustomer()) {
                        String customerId = vm.getCustomerId();
                        
                        
                        for (CustomerRequest request : requestManager.getRequests()) {
                            if (String.valueOf(request.getId()).equals(customerId)) {
                                
                                boolean vmFound = false;
                                for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                                    if (entry.getKey().equals(vm)) {
                                        vmFound = true;
                                        break;
                                    }
                                }
                                
                                
                                if (!vmFound) {
                                    vmAssignments.put(vm, request);
                                    System.out.println("เพิ่ม VM " + vm.getName() + " ที่ถูกกำหนดให้ลูกค้า " + 
                                                     request.getName() + " ลงใน vmAssignments");
                                }
                                
                                
                                if (!request.isActive() && !request.isExpired()) {
                                    request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                                    System.out.println("ปรับสถานะลูกค้า " + request.getName() + 
                                                     " เป็น active เนื่องจากมี VM " + vm.getName() + " ถูกกำหนดไว้แล้ว");
                                }
                                
                                break;
                            }
                        }
                    }
                }
            }
            
            
            List<CustomerRequest> activeRequests = new ArrayList<>();
            for (CustomerRequest request : requestManager.getRequests()) {
                if (request.isActive() && !request.isExpired() && !isRequestAssigned(request)) {
                    activeRequests.add(request);
                    System.out.println("พบคำขอที่ active: " + request.getName() + " (ID: " + request.getId() + ")");
                }
            }
            
            
            if (!activeRequests.isEmpty()) {
                int assignedCount = 0;
                
                
                int availableVMs = 0;
                for (VPSOptimization vps : loadedVPSList) {
                    
                    availableVMs += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                System.out.println("จำนวน VM ว่างในระบบ: " + availableVMs + " VM");
                
                
                if (activeRequests.size() > availableVMs) {
                    System.out.println("⚠️ จำนวน VM ว่างไม่พอสำหรับคำขอที่ active (" + 
                                      activeRequests.size() + " คำขอ, " + availableVMs + " VM)");
                    
                    
                    activeRequests.sort(Comparator.comparingLong(CustomerRequest::getCreationTime));
                    
                    
                    if (availableVMs < activeRequests.size()) {
                        int toBeRemoved = activeRequests.size() - availableVMs;
                        for (int i = 0; i < toBeRemoved; i++) {
                            
                            CustomerRequest droppedRequest = activeRequests.remove(activeRequests.size() - 1);
                            System.out.println("ไม่สามารถจัดสรร VM ให้คำขอ: " + droppedRequest.getName() + 
                                            " เนื่องจาก VM ไม่พอ");
                            
                            
                            droppedRequest.deactivate();
                        }
                        
                        System.out.println("จัดการให้คำขอที่มีความสำคัญน้อยกว่า " + toBeRemoved + 
                                        " คำขอ ไม่ได้รับการจัดสรร VM");
                    }
                }
                
                
                for (CustomerRequest request : activeRequests) {
                    
                    VPSOptimization.VM availableVM = null;
                    
                    
                    for (VPSOptimization vps : loadedVPSList) {
                        for (VPSOptimization.VM vm : vps.getVms()) {
                            if ("Running".equals(vm.getStatus()) && 
                                    !vm.isAssignedToCustomer() && 
                                    !vmAssignments.containsKey(vm)) {
                                availableVM = vm;
                                break;
                            }
                        }
                        if (availableVM != null) break;
                    }
                    
                    if (availableVM != null) {
                        
                        vmAssignments.put(availableVM, request);
                        availableVM.assignToCustomer(
                            String.valueOf(request.getId()), 
                            request.getName(), 
                            request.getLastPaymentTime()
                        );
                        assignedCount++;
                        System.out.println("กำหนด VM " + availableVM.getName() + " ให้กับลูกค้า " + request.getName());
                    } else {
                        
                        System.out.println("⚠️ ไม่พบ VM ว่างที่จะกำหนดให้กับลูกค้า " + request.getName());
                        
                        
                        request.deactivate();
                        System.out.println("ปรับคำขอของลูกค้า " + request.getName() + " เป็นไม่ active");
                    }
                }
                
                System.out.println("กำหนด VM ให้กับคำขอที่ active แล้ว " + assignedCount + " VM");
                
                
                int remainingFreeVMs = 0;
                for (VPSOptimization vps : loadedVPSList) {
                    remainingFreeVMs += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                company.setAvailableVMs(remainingFreeVMs);
                ResourceManager.getInstance().getCurrentState().setFreeVmCount(remainingFreeVMs);
                System.out.println("อัพเดตค่า availableVMs เป็น " + remainingFreeVMs + " ตามจำนวน VM ว่างที่เหลือ");
            }
        }
    }

    
    private CustomerRequest findAssignedCustomerFromGameState(String vmName) {
        
        String[] parts = vmName.split("-");
        if (parts.length < 3) {
            return null; 
        }
        
        
        for (CustomerRequest request : requestManager.getRequests()) {
            
            if (request.isActive() && !request.isExpired() && !isRequestAssigned(request)) {
                return request;
            }
        }
        return null;
    }
    
    
    private boolean isRequestAssigned(CustomerRequest request) {
        if (request == null) return false;
        
        
        if (request.isAssignedToVM()) {
            
            if (!request.isActive() && !request.isExpired()) {
                request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                System.out.println("ปรับสถานะลูกค้า " + request.getName() + " เป็น active เนื่องจากมี assignedToVmId");
            }
            
            
            String vmId = request.getAssignedVmId();
            boolean vmFound = false;
            
            
            for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                for (VPSOptimization.VM vm : vps.getVms()) {
                    if (vm.getId() != null && vm.getId().equals(vmId)) {
                        
                        if (!vmAssignments.containsKey(vm) || !vmAssignments.get(vm).equals(request)) {
                            vmAssignments.put(vm, request);
                            System.out.println("เพิ่ม VM " + vm.getName() + " และ request " + request.getName() + " เข้า vmAssignments");
                        }
                        vmFound = true;
                        break;
                    }
                }
                if (vmFound) break;
            }
            
            return true;
        }
        
        
        
        if (vmAssignments.values().contains(request)) {
            
            if (!request.isActive() && !request.isExpired()) {
                request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                System.out.println("ปรับสถานะลูกค้า " + request.getName() + " เป็น active เนื่องจากพบใน vmAssignments");
            }
            
            
            for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                if (entry.getValue().equals(request)) {
                    VPSOptimization.VM vm = entry.getKey();
                    
                    if (!request.isAssignedToVM()) {
                        request.assignToVM(vm.getId());
                        System.out.println("อัปเดต assignedToVmId = " + vm.getId() + " ให้กับ request " + request.getName());
                    }
                    break;
                }
            }
            
            return true;
        }
        
        
        String requestId = String.valueOf(request.getId());
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            for (VPSOptimization.VM vm : vps.getVms()) {
                if (vm.isAssignedToCustomer() && 
                    requestId.equals(vm.getCustomerId())) {
                    
                    
                    
                    vmAssignments.put(vm, request);
                    System.out.println("พบ VM ที่ถูกกำหนดให้ลูกค้า " + request.getName() + 
                                      " แต่ไม่ได้ถูกบันทึกใน vmAssignments จึงบันทึกเพิ่มเติม");
                    
                    
                    if (!request.isAssignedToVM()) {
                        request.assignToVM(vm.getId());
                        System.out.println("อัปเดต assignedToVmId = " + vm.getId() + " ให้กับ request " + request.getName());
                    }
                    
                    
                    if (!request.isActive() && !request.isExpired()) {
                        request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                        System.out.println("ปรับสถานะลูกค้า " + request.getName() + " เป็น active เนื่องจากพบใน VM");
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
    }

    
    public CustomerRequest findMatchingCustomerRequest(CustomerRequest historyChatRequest) {
        if (historyChatRequest == null) return null;
        
        
        int requestId = historyChatRequest.getId();
        String requestName = historyChatRequest.getName();
        
        
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.getId() == requestId && request.getName().equals(requestName)) {
                System.out.println("พบ CustomerRequest ตรงกันด้วย ID และชื่อ: " + requestId + ", " + requestName);
                return request;
            }
        }
        
        
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.getName().equals(requestName)) {
                System.out.println("พบ CustomerRequest ตรงกันด้วยชื่อ: " + requestName);
                return request;
            }
        }
        
        
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.getCustomerType() == historyChatRequest.getCustomerType() &&
                request.getRequestType() == historyChatRequest.getRequestType() &&
                request.getRequiredVCPUs() == historyChatRequest.getRequiredVCPUs() &&
                request.getRequiredRamGB() == historyChatRequest.getRequiredRamGB() &&
                request.getRequiredDiskGB() == historyChatRequest.getRequiredDiskGB()) {
                
                System.out.println("พบ CustomerRequest ตรงกันด้วยคุณสมบัติ: " + request.getName());
                return request;
            }
        }
        
        System.out.println("ไม่พบ CustomerRequest ที่ตรงกับ: " + requestName);
        return null;
    }

    
    public RequestManager getRequestManager() {
        return requestManager;
    }
}
