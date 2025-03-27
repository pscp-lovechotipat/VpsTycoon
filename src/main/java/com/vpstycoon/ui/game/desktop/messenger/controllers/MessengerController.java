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

        // ตั้งค่า MessengerController ใน ChatHistoryManager เพื่อให้สามารถค้นหา CustomerRequest ได้
        chatHistoryManager.setMessengerController(this);
        
        // อัปเดต references ของ CustomerRequest ในประวัติแชท
        chatHistoryManager.updateCustomerRequestReferences();

        // Load saved VM data from the ResourceManager's GameState
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null) {
            int savedFreeVmCount = currentState.getFreeVmCount();
            if (savedFreeVmCount > 0) {
                // Set the company's available VMs based on the saved count
                company.setAvailableVMs(savedFreeVmCount);
                System.out.println("ข้อมูลเริ่มต้น: โหลด Free VM count จาก GameState: " + savedFreeVmCount);
            }
        }
        
        // โหลด VPS จาก GameState ก่อน
        System.out.println("ข้อมูลเริ่มต้น: โหลด VPS จาก GameState...");
        loadVPSFromGameState();

        setupListeners();
        updateRequestList();
        
        // ตั้งค่า RentalManager ก่อนอัพเดต UI
        this.rentalManager.setOnArchiveRequest(() -> archiveRequest(requestListView.getSelectedRequest()));
        this.rentalManager.setVMAssignment(vmAssignments);
        this.rentalManager.setOnUpdateDashboard(this::updateDashboard);
        
        loadSkillLevels();
        
        // อัพเดต Dashboard ที่หลังที่สุด (หลังจากตั้งค่าทุกอย่างเรียบร้อยแล้ว)
        // เพื่อให้ข้อมูล rack และอื่นๆ ถูกโหลดมาพร้อมใช้งานก่อน
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
                
                // อัพเดตเฉพาะรายการคำขอ ไม่ต้องคำนวณค่า free VM ใหม่
                updateRequestList();
                
                // อัพเดต Dashboard โดยไม่ต้องคำนวณค่า free VM ใหม่
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
            
            // Only enable the assignVM button if there are running VMs available and the customer doesn't already have a VM
            boolean hasAvailableVMs = false;
            boolean customerAlreadyHasVM = false;
            
            if (newVal != null) {
                // ตรวจสอบว่าลูกค้ารายนี้มี VM ถูกกำหนดไว้แล้วหรือไม่โดยใช้ isRequestAssigned
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
                
                // แสดงข้อความถ้าลูกค้ามี VM อยู่แล้ว
                if (customerAlreadyHasVM && !newVal.isExpired()) {
                    VPSOptimization.VM assignedVM = null;
                    for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                        if (entry.getValue().equals(newVal)) {
                            assignedVM = entry.getKey();
                            break;
                        }
                    }
                    
                    // ปรับสถานะลูกค้าเป็น active ถ้าไม่ได้เป็น active แล้ว
                    if (!newVal.isActive()) {
                        newVal.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                        System.out.println("ปรับสถานะลูกค้า " + newVal.getName() + " เป็น active เนื่องจากมี VM อยู่แล้ว");
                    }
                    
                    if (assignedVM != null) {
                        chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM " + assignedVM.getName() + " ถูกกำหนดไว้แล้ว");
                        
                        // ตรวจสอบว่า VM มีการบันทึกข้อมูลลูกค้าหรือไม่
                        if (!assignedVM.isAssignedToCustomer()) {
                            // บันทึกข้อมูลลูกค้าลงใน VM
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
                    
                    // อัพเดตปุ่ม UI
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
                // ตรวจสอบว่ามี VM ในระบบหรือไม่
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
                
                // ไม่สร้าง VM ใหม่จากปุ่ม Assign VM อีกต่อไป
                if (!hasAnyVMs) {
                    if (company.getAvailableVMs() > 0) {
                        chatAreaView.addSystemMessage("ไม่สามารถหา VM จริงในระบบได้แม้ว่าระบบจะรายงานว่ามี " + 
                                                    company.getAvailableVMs() + " VM ที่ว่าง");
                    } else {
                        chatAreaView.addSystemMessage("ไม่มี VM ที่พร้อมใช้งาน โปรดสร้าง VM ใหม่ก่อน");
                    }
                    return;
                }
                
                // รวบรวม VM ที่มีสถานะ Running และยังไม่ถูกใช้งาน
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
                
                // สร้าง Dialog สำหรับเลือก VM
                VMSelectionDialog dialog = new VMSelectionDialog(allAvailableVMs, rootStack);
                dialog.setOnConfirm(() -> {
                    VPSOptimization.VM selectedVM = dialog.getSelectedVM();
                    if (selectedVM != null) {
                        // ตรวจสอบว่าลูกค้ารายนี้มี VM ถูกกำหนดไว้แล้วหรือไม่
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
                            // ปรับสถานะลูกค้าเป็น active ถ้าไม่ได้เป็น active แล้ว
                            if (!selected.isActive() && !selected.isExpired()) {
                                selected.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                                System.out.println("ปรับสถานะลูกค้า " + selected.getName() + " เป็น active เนื่องจากมี VM อยู่แล้ว");
                            }
                            
                            // ตรวจสอบว่า VM มีการบันทึกข้อมูลลูกค้าหรือไม่
                            if (existingVM != null && !existingVM.isAssignedToCustomer()) {
                                existingVM.assignToCustomer(
                                    String.valueOf(selected.getId()),
                                    selected.getName(),
                                    ResourceManager.getInstance().getGameTimeManager().getGameTimeMs()
                                );
                                System.out.println("บันทึกข้อมูลลูกค้า " + selected.getName() + " ลงใน VM " + existingVM.getName());
                            }
                            
                            // แสดงข้อความให้ผู้ใช้ทราบ
                            if (existingVM != null) {
                                chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM " + existingVM.getName() + " ถูกกำหนดไว้แล้ว");
                            } else {
                                chatAreaView.addSystemMessage("ลูกค้ารายนี้มี VM ถูกกำหนดไว้แล้ว");
                            }
                            
                            // อัพเดต UI
                            chatAreaView.getAssignVMButton().setDisable(true);
                            chatAreaView.getArchiveButton().setDisable(false);
                            chatAreaView.updateChatHeader(selected);
                            
                            // ไม่ต้องดำเนินการต่อ
                            return;
                        }
                        
                        // ดำเนินการต่อเฉพาะเมื่อลูกค้ายังไม่มี VM
                        // Assign VM ทันทีเมื่อกด CONFIRM
                        vmAssignments.put(selectedVM, selected); // ล็อก VM
                        selected.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                        chatAreaView.addSystemMessage("VM selected and assigned to request.");
                        chatAreaView.getAssignVMButton().setDisable(true); // ปิดปุ่ม Assign VM ทันที
                        
                        // ลดจำนวน available VM ใน Company และ GameState ทันที
                        int currentAvailableVMs = company.getAvailableVMs();
                        if (currentAvailableVMs > 0) {
                            currentAvailableVMs--;
                            company.setAvailableVMs(currentAvailableVMs);
                            ResourceManager.getInstance().getCurrentState().setFreeVmCount(currentAvailableVMs);
                            System.out.println("ลดจำนวน available VM เนื่องจากมีการ assign VM แล้ว: " + currentAvailableVMs);
                        }
                        
                        updateDashboard(); // อัพเดต UI เพื่อแสดงจำนวน VM ที่ถูกต้อง
                        updateRequestList(); // อัพเดตสถานะคำขอ

                        // กลับไปใช้ provisioning animation แบบเดิม
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
        // ตรวจสอบว่า requestManager ไม่เป็น null
        if (requestManager == null) {
            System.out.println("⚠️ Error: requestManager is null");
            return;
        }
        
        // ดึงข้อมูลคำขอทั้งหมด
        ObservableList<CustomerRequest> requests = requestManager.getRequests();
        
        // ตรวจสอบความสมบูรณ์ของข้อมูล
        if (requests == null) {
            System.out.println("⚠️ Error: requests from requestManager is null");
            return;
        }
        
        // ตรวจสอบจำนวนคำขอทั้งหมด
        System.out.println("📋 กำลังอัปเดตรายการคำขอ: พบ " + requests.size() + " รายการ");
        
        // ถ้ามีข้อมูลที่ไม่สมบูรณ์ในรายการ ให้กรองออก
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
        
        // แสดงผลข้อมูลหลังการกรอง
        if (validRequests.size() != requests.size()) {
            System.out.println("ℹ️ กรองข้อมูลคำขอ: จำนวนที่สมบูรณ์ = " + validRequests.size() + "/" + requests.size());
        }
        
        // อัปเดตรายการในวิว
        requestListView.updateRequestList(validRequests);
    }

    private void updateDashboard() {
        // First, check for any expired requests and release their VMs
        releaseExpiredVMs();
        
        // Update dashboard with available VM count and total servers
        int availableVMs = 0;
        int totalServers = 0;
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        
        // ใช้ค่า free VM ที่ถูกบันทึกไว้ใน Company เป็นหลัก
        // เนื่องจากค่านี้จะถูกลดลงทันทีเมื่อมีการ assign VM และเพิ่มขึ้นเมื่อมีการปล่อย VM
        if (company.getAvailableVMs() >= 0) {
            availableVMs = company.getAvailableVMs();
            System.out.println("Dashboard: ใช้ค่า free VM ที่บันทึกใน Company: " + availableVMs);
            
            // ขั้นตอนที่ 1: ตรวจสอบจำนวน servers จาก VPSManager
            int vpsMgrServerCount = vpsManager.getVPSMap().size();
            if (vpsMgrServerCount > 0) {
                totalServers = vpsMgrServerCount;
                System.out.println("   จำนวนเซิร์ฟเวอร์จาก VPSManager: " + totalServers);
            }
        } else {
            // กรณีที่ยังไม่เคยบันทึกค่า free VM ไว้ ให้คำนวณจาก VPSManager
            // ขั้นตอนที่ 1: ตรวจสอบจำนวน servers และ VMs ว่างจาก VPSManager
            int vpsMgrServerCount = vpsManager.getVPSMap().size();
            int vpsMgrVMCount = 0;
            
            if (vpsMgrServerCount > 0) {
                totalServers = vpsMgrServerCount;
                
                // นับเฉพาะ VM ที่ Running และไม่มีลูกค้าใช้งานเท่านั้น
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    vpsMgrVMCount += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                // ใช้ค่าที่นับได้จริง
                availableVMs = vpsMgrVMCount;
                System.out.println("Dashboard: ใช้ค่า free VM จากการนับ VM ที่ไม่มีลูกค้าใช้จริง: " + availableVMs);
                
                // บันทึกค่านี้ลงใน Company และ GameState
                company.setAvailableVMs(availableVMs);
                if (currentState != null) {
                    currentState.setFreeVmCount(availableVMs);
                }
            } else {
                // ถ้าไม่มีเซิร์ฟเวอร์ใน VPSManager ให้ลองโหลดจาก GameState
                loadVPSFromGameState();
                
                // ตรวจสอบอีกครั้งหลังจากโหลด
                vpsMgrServerCount = vpsManager.getVPSMap().size();
                if (vpsMgrServerCount > 0) {
                    totalServers = vpsMgrServerCount;
                    
                    // นับ VM อีกครั้งหลังโหลด
                    for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                        availableVMs += (int) vps.getVms().stream()
                                .filter(vm -> "Running".equals(vm.getStatus()) && 
                                        !vmAssignments.containsKey(vm) && 
                                        !vm.isAssignedToCustomer())
                                .count();
                    }
                    
                    System.out.println("   จำนวนเซิร์ฟเวอร์และ free VM หลังจากโหลด: " + totalServers + ", " + availableVMs);
                    
                    // บันทึกค่านี้ลงใน Company และ GameState
                    company.setAvailableVMs(availableVMs);
                    if (currentState != null) {
                        currentState.setFreeVmCount(availableVMs);
                    }
                }
            }
            
            // ขั้นตอนที่ 2: ดึงข้อมูลจาก Rack ถ้ายังไม่มี VM ในระบบ
            if (availableVMs == 0) {
                checkAndUpdateFromRack();
                
                // นับ VM อีกครั้งหลังอัพเดตจาก Rack
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
                    
                    // บันทึกค่านี้ลงใน Company และ GameState
                    company.setAvailableVMs(availableVMs);
                    if (currentState != null) {
                        currentState.setFreeVmCount(availableVMs);
                    }
                } else if (currentState != null && currentState.getFreeVmCount() > 0) {
                    // ใช้ค่าจาก GameState เป็นทางเลือกสุดท้าย
                    availableVMs = currentState.getFreeVmCount();
                    company.setAvailableVMs(availableVMs);
                    System.out.println("3. ใช้ค่า free VM จาก GameState: " + availableVMs);
                }
            }
        }
        
        // ขั้นตอนที่ 3: ถ้าไม่มี VM objects จริงในระบบแต่มีค่า availableVMs > 0 ให้สร้าง VM objects
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
            
            // แทนที่จะสร้าง VM objects ให้ปรับค่า availableVMs เป็น 0 แทน
            availableVMs = 0;
            company.setAvailableVMs(0);
            if (currentState != null) {
                currentState.setFreeVmCount(0);
            }
            System.out.println("ปรับค่า availableVMs เป็น 0 เนื่องจากไม่มี VM objects จริงในระบบ");
        }
        
        // ตรวจสอบและแสดงข้อมูลสถิติ
        
        // ตรวจสอบ VM ที่มีลูกค้าใช้งานแล้ว (จาก VM objects)
        int assignedVMCount = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            assignedVMCount += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && vm.isAssignedToCustomer())
                    .count();
        }
        
        // ถ้ามี VM ที่ถูก assign แล้ว ให้แสดงข้อมูล
        if (assignedVMCount > 0) {
            System.out.println("พบ VM ที่มีลูกค้าใช้งานแล้ว: " + assignedVMCount + " VM");
        }
        
        // ตรวจสอบ VM ที่อยู่ใน vmAssignments
        if (!vmAssignments.isEmpty()) {
            System.out.println("พบ VM ที่กำลังถูก assign ในระบบ: " + vmAssignments.size() + " VM");
        }
        
        // เรียกใช้ validateVMConsistency เพื่อตรวจสอบและแก้ไขความไม่สอดคล้องของข้อมูล
        validateVMConsistency();
        
        // อัพเดต Dashboard ด้วยข้อมูลล่าสุด
        dashboardView.updateDashboard(company.getRating(), requestManager.getRequests().size(), company.getAvailableVMs(), totalServers);
        
        // บันทึกค่าลงใน Company เพื่อให้สามารถเรียกใช้ค่านี้ได้จากที่อื่น
        // ไม่ต้องตั้งค่าอีกครั้งเนื่องจากเราได้เรียก validateVMConsistency แล้ว
        
        // บันทึกค่าลงใน GameState ผ่าน ResourceManager ด้วย
        if (currentState != null) {
            currentState.setFreeVmCount(company.getAvailableVMs());
        }
        
        // Update the AssignVMButton status for selected request
        CustomerRequest selected = requestListView.getSelectedRequest();
        if (selected != null && !selected.isActive() && !selected.isExpired()) {
            chatAreaView.getAssignVMButton().setDisable(company.getAvailableVMs() <= 0);
        }
    }
    
    /**
     * ตรวจสอบและปรับปรุงข้อมูลจาก Rack
     * @return true ถ้ามีข้อมูลจาก Rack
     */
    private boolean checkAndUpdateFromRack() {
        ResourceManager resourceManager = ResourceManager.getInstance();
        Rack rack = resourceManager.getRack();
        
        if (rack != null) {
            // นับเซิร์ฟเวอร์ที่ติดตั้งในแร็ค
            List<VPSOptimization> installedServers = rack.getInstalledVPS();
            if (installedServers != null && !installedServers.isEmpty()) {
                int rackServers = installedServers.size();
                int rackVMs = 0;
                
                System.out.println("ตรวจสอบเซิร์ฟเวอร์ในแร็ค: " + rackServers + " เครื่อง");
                
                // ตรวจสอบว่าเซิร์ฟเวอร์เหล่านี้มีอยู่ในระบบ VPSManager หรือไม่ ถ้าไม่มีให้เพิ่ม
                for (VPSOptimization vps : installedServers) {
                    String vpsId = vps.getVpsId();
                    if (!vpsManager.getVPSMap().containsKey(vpsId)) {
                        vpsManager.addVPS(vpsId, vps);
                        System.out.println("   เพิ่มเซิร์ฟเวอร์ " + vpsId + " จากแร็คเข้าสู่ VPSManager");
                    }
                    
                    // นับเฉพาะ VM ที่มีสถานะ Running และยังไม่ได้ถูกใช้งาน และไม่มีลูกค้าใช้งานอยู่
                    // และตรวจสอบไม่ให้เกิน capacity ของ server
                    int maxVMsPerServer = vps.getVCPUs(); // ใช้ vCPU เป็นตัวกำหนด capacity
                    int existingVMs = vps.getVms().size();
                    
                    // ถ้ามี VM เกิน capacity แล้ว ไม่ให้นับเพิ่ม
                    if (existingVMs >= maxVMsPerServer) {
                        System.out.println("   เซิร์ฟเวอร์ " + vpsId + " มี VM เกิน capacity แล้ว (" + 
                                         existingVMs + "/" + maxVMsPerServer + ")");
                        continue;
                    }
                    
                    // นับ VM ที่ใช้ได้ แต่ไม่เกิน capacity
                    int availableVMsInServer = (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) &&
                                    !vm.isAssignedToCustomer())
                            .count();
                    
                    // ไม่นับเกิน capacity ที่เหลือ
                    int remainingCapacity = maxVMsPerServer - existingVMs;
                    if (remainingCapacity > 0) {
                        // เพิ่มจำนวน VM ที่สามารถใช้ได้ โดยไม่เกิน capacity ที่เหลือ
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
    
    /**
     * สร้าง VM objects ในระบบตามจำนวนที่ต้องการ
     * @param vmCount จำนวน VM ที่ต้องการสร้าง
     * @param serverCount จำนวนเซิร์ฟเวอร์ที่มี
     */
    private void createVirtualMachines(int vmCount, int serverCount) {
        // กระจาย VM ให้กับเซิร์ฟเวอร์ที่มีอยู่
        int remainingVMs = vmCount;
        
        // ถ้ามีเซิร์ฟเวอร์ คำนวณว่าแต่ละเซิร์ฟเวอร์ควรมี VM กี่ตัว แต่ไม่เกิน capacity
        if (serverCount > 0) {
            // สร้าง map เพื่อเก็บข้อมูลว่าแต่ละ server มี capacity เหลือเท่าไร
            Map<String, Integer> serverCapacity = new HashMap<>();
            int totalAvailableCapacity = 0;
            
            // คำนวณ capacity ของแต่ละ server
            for (Map.Entry<String, VPSOptimization> entry : vpsManager.getVPSMap().entrySet()) {
                String serverId = entry.getKey();
                VPSOptimization vps = entry.getValue();
                
                // คำนวณความจุสูงสุดตาม spec (vCPUs)
                // สมมติว่า VM แต่ละตัวใช้ 1 vCPU ต่อ VM เป็นอย่างน้อย
                int maxVMsPerServer = vps.getVCPUs();
                
                // ตรวจสอบจำนวน VM ที่มีอยู่แล้วในเซิร์ฟเวอร์นี้
                int existingVMs = vps.getVms().size();
                
                // คำนวณ capacity ที่เหลือ
                int remainingCapacity = Math.max(0, maxVMsPerServer - existingVMs);
                
                // เก็บค่าลงใน map
                serverCapacity.put(serverId, remainingCapacity);
                totalAvailableCapacity += remainingCapacity;
                
                System.out.println("เซิร์ฟเวอร์ " + serverId + " มี capacity เหลือ " + 
                                  remainingCapacity + " VM (max: " + maxVMsPerServer + 
                                  ", มีอยู่แล้ว: " + existingVMs + ")");
            }
            
            // ถ้า capacity รวมไม่พอสำหรับจำนวน VM ที่ต้องการสร้าง
            if (totalAvailableCapacity < vmCount) {
                System.out.println("⚠️ เตือน: จำนวน VM ที่ต้องการสร้าง (" + vmCount + 
                                 ") มากกว่า capacity รวมที่เหลืออยู่ (" + totalAvailableCapacity + 
                                 ") จะสร้างเท่าที่ capacity เหลือ");
                
                // ปรับลดจำนวน VM ที่จะสร้างให้ไม่เกิน capacity
                remainingVMs = totalAvailableCapacity;
                
                // อัพเดตค่า availableVMs ใน company และ GameState ให้สอดคล้องกับ capacity จริง
                company.setAvailableVMs(totalAvailableCapacity);
                ResourceManager.getInstance().getCurrentState().setFreeVmCount(totalAvailableCapacity);
                System.out.println("ปรับค่า availableVMs เป็น " + totalAvailableCapacity + " ตาม capacity จริง");
            }
            
            // สร้าง VM ตามจำนวนที่ capacity เหลือในแต่ละเซิร์ฟเวอร์
            for (Map.Entry<String, VPSOptimization> entry : vpsManager.getVPSMap().entrySet()) {
                String serverId = entry.getKey();
                VPSOptimization vps = entry.getValue();
                
                // ดึงค่า capacity ที่เหลือของเซิร์ฟเวอร์นี้
                int availableCapacity = serverCapacity.get(serverId);
                
                // สร้าง VM เท่าที่ capacity เหลือ แต่ไม่เกินจำนวนที่ต้องการ
                int vmsToCreate = Math.min(remainingVMs, availableCapacity);
                
                for (int i = 0; i < vmsToCreate; i++) {
                    // สร้าง VM ตามวิธีที่ถูกต้อง
                    String vmName = "vm-" + System.currentTimeMillis() + "-" + i;
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                        vmName,
                        1, // ใช้ 1 vCPU ต่อ VM 
                        1, // 1 GB RAM
                        10  // 10 GB disk
                    );
                    
                    // กำหนด IP address แยกหลังจากสร้าง VM
                    newVM.setIp(generateRandomIp());
                    newVM.setStatus("Running");
                    
                    // ตรวจสอบว่ามีการบันทึกข้อมูลลูกค้าใน GameState หรือไม่
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
            
            // ถ้าสร้าง VM ได้น้อยกว่าที่ต้องการ (เนื่องจาก capacity ไม่พอ)
            if (remainingVMs > 0) {
                System.out.println("⚠️ ไม่สามารถสร้าง VM ได้ครบตามต้องการ เนื่องจาก capacity ไม่พอ ยังเหลืออีก " + 
                                  remainingVMs + " VM ที่ต้องการสร้าง");
                
                // อัพเดตค่า availableVMs ให้สอดคล้องกับความเป็นจริง
                int actualCreated = vmCount - remainingVMs;
                if (company.getAvailableVMs() > actualCreated) {
                    company.setAvailableVMs(actualCreated);
                    ResourceManager.getInstance().getCurrentState().setFreeVmCount(actualCreated);
                    System.out.println("ปรับค่า availableVMs เป็น " + actualCreated + " ตามที่สร้างได้จริง");
                }
            }
        } else {
            System.out.println("⚠️ ไม่สามารถสร้าง VM ได้เนื่องจากไม่มีเซิร์ฟเวอร์ในระบบ");
            // ไม่มีเซิร์ฟเวอร์ ไม่ควรมี VM อยู่
            company.setAvailableVMs(0);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(0);
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
            
            // ลบข้อมูลลูกค้าออกจาก VM
            vm.releaseFromCustomer();
            
            // ปล่อย VM ออกจาก assignments แต่ยังคงเก็บ request ไว้ในสถานะ expired
            vmAssignments.remove(vm);
            
            // เพิ่มจำนวน available VM ทันที
            int currentAvailableVMs = company.getAvailableVMs();
            currentAvailableVMs++;
            company.setAvailableVMs(currentAvailableVMs);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(currentAvailableVMs);
            System.out.println("เพิ่มจำนวน available VM เนื่องจากคำขอหมดอายุ: " + currentAvailableVMs);
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
            
            // ตรวจสอบความสอดคล้องของข้อมูลหลังจากการปล่อย VM
            validateVMConsistency();
        }
    }

    /**
     * ตรวจสอบความสอดคล้องของข้อมูลระหว่างจำนวน VM จริงและค่าใน company
     */
    private void validateVMConsistency() {
        // ตรวจสอบจำนวน VM จริงในระบบ
        int countFromVMs = 0;
        int invalidVMs = 0;
        Map<String, List<VPSOptimization.VM>> vmsToRemove = new HashMap<>();
        
        for (Map.Entry<String, VPSOptimization> entry : vpsManager.getVPSMap().entrySet()) {
            String serverId = entry.getKey();
            VPSOptimization vps = entry.getValue();
            
            // ตรวจสอบว่า server แต่ละตัวไม่มี VM เกิน capacity
            int maxVMsPerServer = vps.getVCPUs(); // ใช้ vCPU เป็นตัวกำหนด capacity
            int totalVMsInServer = vps.getVms().size();
            
            // ตรวจสอบและเก็บรายการ VM ที่ต้องลบ (กรณีเกิน capacity)
            if (totalVMsInServer > maxVMsPerServer) {
                System.out.println("⚠️ ตรวจพบว่าเซิร์ฟเวอร์ " + serverId + " มี VM เกิน capacity: " + 
                                  totalVMsInServer + " VM (max: " + maxVMsPerServer + " VM)");
                
                // จัดเรียง VM ตามสถานะ - ลบที่ไม่ได้ใช้งานก่อน และเก็บรักษาที่มีลูกค้าใช้งานอยู่
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
                
                // คำนวณจำนวน VM ที่ต้องลบ
                int excessVMs = totalVMsInServer - maxVMsPerServer;
                List<VPSOptimization.VM> vmList = new ArrayList<>();
                
                // ลบจาก VM ที่ไม่ได้ใช้งานก่อน
                int toRemoveFromUnused = Math.min(excessVMs, unusedVMs.size());
                for (int i = 0; i < toRemoveFromUnused; i++) {
                    vmList.add(unusedVMs.get(i));
                }
                
                // ถ้ายังไม่พอ ต้องลบ VM ที่มีลูกค้าใช้งานด้วย (ถ้าจำเป็น)
                if (toRemoveFromUnused < excessVMs) {
                    int toRemoveFromAssigned = excessVMs - toRemoveFromUnused;
                    System.out.println("⚠️ ต้องลบ VM ที่มีลูกค้าใช้งานออก " + toRemoveFromAssigned + 
                                      " VM เนื่องจาก capacity ไม่พอ");
                    
                    // ในกรณีนี้ ควรแจ้งเตือนผู้ใช้งานว่ามีปัญหา
                    for (int i = 0; i < Math.min(toRemoveFromAssigned, assignedVMs.size()); i++) {
                        vmList.add(assignedVMs.get(i));
                    }
                }
                
                // เก็บรายการ VM ที่ต้องลบ
                vmsToRemove.put(serverId, vmList);
                invalidVMs += vmList.size();
            }
            
            // นับเฉพาะ VM ที่มีสถานะ Running และไม่ได้ถูกใช้งาน
            countFromVMs += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && 
                            !vmAssignments.containsKey(vm) && 
                            !vm.isAssignedToCustomer())
                    .count();
        }
        
        // ลบ VM ที่เกิน capacity
        if (invalidVMs > 0) {
            System.out.println("⚠️ จะดำเนินการลบ VM ที่เกิน capacity จำนวน " + invalidVMs + " VM");
            
            for (Map.Entry<String, List<VPSOptimization.VM>> entry : vmsToRemove.entrySet()) {
                String serverId = entry.getKey();
                List<VPSOptimization.VM> vmList = entry.getValue();
                VPSOptimization vps = vpsManager.getVPSMap().get(serverId);
                
                for (VPSOptimization.VM vm : vmList) {
                    // ตรวจสอบว่า VM นี้มีลูกค้าใช้งานอยู่หรือไม่
                    CustomerRequest request = null;
                    for (Map.Entry<VPSOptimization.VM, CustomerRequest> vmEntry : vmAssignments.entrySet()) {
                        if (vmEntry.getKey().equals(vm)) {
                            request = vmEntry.getValue();
                            break;
                        }
                    }
                    
                    // ถ้า VM นี้มีลูกค้าใช้งานอยู่ ให้แจ้งเตือนและปรับสถานะ
                    if (request != null) {
                        System.out.println("⚠️ ลบ VM " + vm.getName() + " ที่กำลังถูกใช้งานโดยลูกค้า " + 
                                          request.getName() + " เนื่องจากเกิน capacity");
                        
                        // ปรับสถานะของลูกค้า
                        request.deactivate();
                        
                        // ลบการเชื่อมโยง
                        vmAssignments.remove(vm);
                    } else if (vm.isAssignedToCustomer()) {
                        System.out.println("⚠️ ลบ VM " + vm.getName() + " ที่กำลังถูกใช้งานโดยลูกค้า " + 
                                          vm.getCustomerName() + " เนื่องจากเกิน capacity");
                    } else {
                        System.out.println("ลบ VM " + vm.getName() + " ที่ไม่มีลูกค้าใช้งาน เนื่องจากเกิน capacity");
                    }
                    
                    // ลบ VM ออกจาก server
                    vps.removeVM(vm);
                }
            }
            
            // คำนวณจำนวน VM ที่ว่างอีกครั้งหลังจากลบ
            countFromVMs = 0;
            for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                countFromVMs += (int) vps.getVms().stream()
                        .filter(vm -> "Running".equals(vm.getStatus()) && 
                                !vmAssignments.containsKey(vm) && 
                                !vm.isAssignedToCustomer())
                        .count();
            }
        }
        
        // ตรวจสอบค่าที่เก็บไว้
        int storedAvailableVMs = company.getAvailableVMs();
        
        // แก้ไขความไม่สอดคล้องถ้าจำเป็น
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
        // Mark request as active
        request.activate(gameTimeManager.getGameTimeMs());
        chatAreaView.getArchiveButton().setDisable(false);
        chatAreaView.getAssignVMButton().setDisable(true);
        chatAreaView.updateChatHeader(request);
        
        // บันทึกข้อมูลลูกค้าลงใน VM
        vm.assignToCustomer(
            String.valueOf(request.getId()), 
            request.getName(), 
            gameTimeManager.getGameTimeMs()
        );
        System.out.println("บันทึกข้อมูลลูกค้า " + request.getName() + " (ID: " + request.getId() + ") ลงใน VM: " + vm.getName());
        
        // เพิ่ม: อัปเดต assignToVM ใน CustomerRequest
        request.assignToVM(vm.getId());
        System.out.println("ตั้งค่า assignToVM " + vm.getId() + " ให้กับ request " + request.getName());
        
        // กำหนด VM ให้กับ request ใน vmAssignments
        vmAssignments.put(vm, request);
        
        // เพิ่มข้อความในแชท
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, 
            "VM provisioning completed successfully", new HashMap<>()));
        chatAreaView.addSystemMessage("VM provisioning completed successfully");
        
        // ให้รางวัล skill points
        skillPointsManager.awardSkillPoints(request, 0.2);

        validateVMConsistency();
        
        // อัพเดตรายการคำขอ
        updateRequestList();

        // อัพเดต dashboard เพื่อแสดงสถานะล่าสุด
        updateDashboard();
    }

    private void archiveRequest(CustomerRequest selected) {
        if (selected != null && (selected.isActive() || selected.isExpired())) {
            // ตรวจสอบวิธีแรกโดยการค้นหาใน vmAssignments
            VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                    .filter(entry -> entry.getValue() == selected)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            
            if (assignedVM != null) {
                // กรณีปกติ ยังพบ VM ใน vmAssignments
                releaseVM(assignedVM, true);
            } else if (selected.isExpired()) {
                // กรณีไม่พบ VM ใน vmAssignments แต่ request หมดอายุแล้ว
                // ให้ตรวจสอบความไม่สอดคล้องของข้อมูลและแก้ไข
                System.out.println("ไม่พบ VM สำหรับ request ที่หมดอายุ: " + selected.getName() + " แต่จะเพิ่ม availableVMs เพื่อแก้ไขความไม่สอดคล้อง");
                
                // ตรวจสอบจำนวน VM จริงในระบบ
                int countFromVMs = 0;
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    countFromVMs += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                // ตรวจสอบค่าที่เก็บไว้
                int storedAvailableVMs = company.getAvailableVMs();
                
                // แก้ไขความไม่สอดคล้องถ้าจำเป็น
                if (countFromVMs != storedAvailableVMs) {
                    company.setAvailableVMs(countFromVMs);
                    ResourceManager.getInstance().getCurrentState().setFreeVmCount(countFromVMs);
                    System.out.println("แก้ไขความไม่สอดคล้อง: จำนวน VM จริงที่ว่าง = " + countFromVMs + 
                                      " แต่ค่า availableVMs = " + storedAvailableVMs);
                }
            }
            
            requestManager.getRequests().remove(selected);
            chatAreaView.clearMessages();
            chatAreaView.getAssignVMButton().setDisable(false); // Enable ปุ่ม Assign VM ใหม่หลัง archive
            updateRequestList();
            updateDashboard();
        }
    }

    public void releaseVM(VPSOptimization.VM vm, boolean isArchiving) {
        // หา request ที่ใช้ VM นี้
        CustomerRequest requestToRelease = null;
        for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
            if (entry.getKey().equals(vm)) {
                requestToRelease = entry.getValue();
                break;
            }
        }
        
        if (requestToRelease != null) {
            if (isArchiving) {
                // เพิ่มข้อความในแชทเพื่อแจ้งว่า VM ถูกปล่อยคืนแล้ว
                chatHistoryManager.addMessage(requestToRelease, new ChatMessage(MessageType.SYSTEM,
                    "Request archived and VM released.", new HashMap<>()));
                chatAreaView.addSystemMessage("Request archived and VM released.");
                
                // ลบออกจาก pendingRequests
                requestManager.getRequests().remove(requestToRelease);
            } else {
                // กรณีหมดอายุ
                requestToRelease.markAsExpired();
                chatHistoryManager.addMessage(requestToRelease, new ChatMessage(MessageType.SYSTEM,
                    "Contract expired and VM released.", new HashMap<>()));
                chatAreaView.addSystemMessage("Contract expired and VM released.");
            }
            
            // ลบข้อมูลลูกค้าออกจาก VM
            vm.releaseFromCustomer();
            
            // ลบการเชื่อมโยงใน customerRequest
            requestToRelease.unassignFromVM();
            System.out.println("ลบการเชื่อมโยง assignToVM ของ request " + requestToRelease.getName());
            
            // ปล่อย VM ออกจาก assignments
            vmAssignments.remove(vm);
            
            // เพิ่มจำนวน available VM ทันที
            int currentAvailableVMs = company.getAvailableVMs();
            currentAvailableVMs++;
            company.setAvailableVMs(currentAvailableVMs);
            ResourceManager.getInstance().getCurrentState().setFreeVmCount(currentAvailableVMs);
            System.out.println("เพิ่มจำนวน available VM เนื่องจากมีการปล่อย VM: " + currentAvailableVMs);
            
            // ปรับปรุง UI
            updateDashboard();
            updateRequestList();
            
            // อัปเดตปุ่ม Archive หลังจากปล่อย VM
            CustomerRequest selectedRequest = requestListView.getSelectedRequest();
            if (selectedRequest != null) {
                boolean shouldEnableArchive = selectedRequest.isActive() || selectedRequest.isExpired();
                chatAreaView.getArchiveButton().setDisable(!shouldEnableArchive);
            }
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

    private String generateRandomIp() {
        // สร้าง IP แบบสุ่มในรูปแบบ 10.x.y.z สำหรับ private network
        Random random = new Random();
        return "10." + 
               random.nextInt(255) + "." + 
               random.nextInt(255) + "." + 
               (random.nextInt(254) + 1); // หลีกเลี่ยงค่า 0
    }

    /**
     * โหลดข้อมูล VPS จาก GameState โดยตรง และตรวจสอบข้อมูลลูกค้าที่ใช้งาน VM อยู่
     */
    private void loadVPSFromGameState() {
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null && currentState.getGameObjects() != null) {
            int vpsCount = 0;
            List<VPSOptimization> loadedVPSList = new ArrayList<>();
            
            // โหลด VPS จาก GameState
            for (Object obj : currentState.getGameObjects()) {
                if (obj instanceof VPSOptimization) {
                    VPSOptimization vps = (VPSOptimization) obj;
                    String vpsId = vps.getVpsId();
                    
                    // ถ้ายังไม่มีใน VPSManager ให้เพิ่มเข้าไป
                    if (!vpsManager.getVPSMap().containsKey(vpsId)) {
                        vpsManager.addVPS(vpsId, vps);
                        loadedVPSList.add(vps);
                        vpsCount++;
                    } else {
                        // ถ้ามีแล้วในระบบ ให้เก็บไว้เพื่อตรวจสอบ VM
                        loadedVPSList.add(vpsManager.getVPSMap().get(vpsId));
                    }
                }
            }
            
            System.out.println("โหลด VPS จาก GameState จำนวน " + vpsCount + " เครื่อง");
            
            // ตรวจสอบ VM ทั้งหมดที่มีลูกค้ากำหนดไว้แล้ว
            for (VPSOptimization vps : loadedVPSList) {
                for (VPSOptimization.VM vm : vps.getVms()) {
                    if (vm.isAssignedToCustomer()) {
                        String customerId = vm.getCustomerId();
                        
                        // ค้นหาลูกค้าจาก ID
                        for (CustomerRequest request : requestManager.getRequests()) {
                            if (String.valueOf(request.getId()).equals(customerId)) {
                                // ตรวจสอบว่า VM นี้ถูกบันทึกใน vmAssignments หรือไม่
                                boolean vmFound = false;
                                for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                                    if (entry.getKey().equals(vm)) {
                                        vmFound = true;
                                        break;
                                    }
                                }
                                
                                // ถ้ายังไม่ได้บันทึกใน vmAssignments ให้เพิ่มเข้าไป
                                if (!vmFound) {
                                    vmAssignments.put(vm, request);
                                    System.out.println("เพิ่ม VM " + vm.getName() + " ที่ถูกกำหนดให้ลูกค้า " + 
                                                     request.getName() + " ลงใน vmAssignments");
                                }
                                
                                // ปรับสถานะลูกค้าเป็น active ถ้าไม่ได้เป็น active อยู่แล้ว และไม่ได้หมดอายุ
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
            
            // ตรวจสอบคำขอที่ active อยู่และกำหนดให้กับ VM ที่เหมาะสม
            List<CustomerRequest> activeRequests = new ArrayList<>();
            for (CustomerRequest request : requestManager.getRequests()) {
                if (request.isActive() && !request.isExpired() && !isRequestAssigned(request)) {
                    activeRequests.add(request);
                    System.out.println("พบคำขอที่ active: " + request.getName() + " (ID: " + request.getId() + ")");
                }
            }
            
            // ถ้ามีคำขอที่ active อยู่ ให้กำหนด VM ให้แต่ละคำขอ (แต่ไม่สร้าง VM เพิ่ม)
            if (!activeRequests.isEmpty()) {
                int assignedCount = 0;
                
                // ตรวจสอบจำนวน VM ทั้งหมดที่มีในระบบ
                int availableVMs = 0;
                for (VPSOptimization vps : loadedVPSList) {
                    // นับ VM ที่ว่างอยู่
                    availableVMs += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer())
                            .count();
                }
                
                System.out.println("จำนวน VM ว่างในระบบ: " + availableVMs + " VM");
                
                // ตรวจสอบว่ามี VM ว่างพอสำหรับคำขอทั้งหมดหรือไม่
                if (activeRequests.size() > availableVMs) {
                    System.out.println("⚠️ จำนวน VM ว่างไม่พอสำหรับคำขอที่ active (" + 
                                      activeRequests.size() + " คำขอ, " + availableVMs + " VM)");
                    
                    // จัดเรียงคำขอตามเวลา (เก่าสุดอยู่หน้าสุด)
                    activeRequests.sort(Comparator.comparingLong(CustomerRequest::getCreationTime));
                    
                    // ตัดคำขอที่ใหม่สุดออก
                    if (availableVMs < activeRequests.size()) {
                        int toBeRemoved = activeRequests.size() - availableVMs;
                        for (int i = 0; i < toBeRemoved; i++) {
                            // เอาจากท้ายสุด (ใหม่สุด)
                            CustomerRequest droppedRequest = activeRequests.remove(activeRequests.size() - 1);
                            System.out.println("ไม่สามารถจัดสรร VM ให้คำขอ: " + droppedRequest.getName() + 
                                            " เนื่องจาก VM ไม่พอ");
                            
                            // ปรับสถานะของคำขอให้ไม่ active
                            droppedRequest.deactivate();
                        }
                        
                        System.out.println("จัดการให้คำขอที่มีความสำคัญน้อยกว่า " + toBeRemoved + 
                                        " คำขอ ไม่ได้รับการจัดสรร VM");
                    }
                }
                
                // เชื่อมต่อ VM กับคำขอที่ active (เฉพาะที่มี VM ว่างพอ)
                for (CustomerRequest request : activeRequests) {
                    // หา VM ที่ว่างอยู่
                    VPSOptimization.VM availableVM = null;
                    
                    // ตรวจสอบ VM ว่างในแต่ละ VPS
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
                        // กำหนด VM ให้กับคำขอ
                        vmAssignments.put(availableVM, request);
                        availableVM.assignToCustomer(
                            String.valueOf(request.getId()), 
                            request.getName(), 
                            request.getLastPaymentTime()
                        );
                        assignedCount++;
                        System.out.println("กำหนด VM " + availableVM.getName() + " ให้กับลูกค้า " + request.getName());
                    } else {
                        // ไม่พบ VM ว่าง
                        System.out.println("⚠️ ไม่พบ VM ว่างที่จะกำหนดให้กับลูกค้า " + request.getName());
                        
                        // ปรับสถานะของคำขอให้ไม่ active
                        request.deactivate();
                        System.out.println("ปรับคำขอของลูกค้า " + request.getName() + " เป็นไม่ active");
                    }
                }
                
                System.out.println("กำหนด VM ให้กับคำขอที่ active แล้ว " + assignedCount + " VM");
                
                // อัพเดตค่า availableVMs ใน company และ GameState
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

    /**
     * ค้นหาลูกค้าที่เคยใช้ VM นี้ใน GameState
     * @param vmName ชื่อของ VM
     * @return คำขอของลูกค้า หรือ null ถ้าไม่พบ
     */
    private CustomerRequest findAssignedCustomerFromGameState(String vmName) {
        // แยกชื่อของ VM ออกมา (format: vm-timestamp-index)
        String[] parts = vmName.split("-");
        if (parts.length < 3) {
            return null; // VM name ไม่ตรงตามรูปแบบ
        }
        
        // ค้นหาคำขอที่ active ที่ยังไม่ได้รับการกำหนด VM
        for (CustomerRequest request : requestManager.getRequests()) {
            // ตรวจสอบว่าคำขอ active แต่ยังไม่หมดอายุ และยังไม่ได้รับการกำหนด VM
            if (request.isActive() && !request.isExpired() && !isRequestAssigned(request)) {
                return request;
            }
        }
        return null;
    }
    
    /**
     * ตรวจสอบว่าคำขอถูกกำหนด VM แล้วหรือไม่
     * @param request คำขอที่ต้องการตรวจสอบ
     * @return true ถ้าคำขอถูกกำหนด VM แล้ว
     */
    private boolean isRequestAssigned(CustomerRequest request) {
        if (request == null) return false;
        
        // ตรวจสอบจาก assignedToVmId ก่อน (วิธีใหม่)
        if (request.isAssignedToVM()) {
            // ปรับสถานะลูกค้าเป็น active ถ้าไม่ได้เป็น active อยู่แล้ว
            if (!request.isActive() && !request.isExpired()) {
                request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                System.out.println("ปรับสถานะลูกค้า " + request.getName() + " เป็น active เนื่องจากมี assignedToVmId");
            }
            
            // ตรวจสอบให้แน่ใจว่า VM ที่ถูก assign มีอยู่จริง
            String vmId = request.getAssignedVmId();
            boolean vmFound = false;
            
            // ค้นหา VM ที่มี ID ตรงกับ assignedToVmId
            for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                for (VPSOptimization.VM vm : vps.getVms()) {
                    if (vm.getId() != null && vm.getId().equals(vmId)) {
                        // พบ VM ที่ถูก assign จึงเพิ่มเข้า vmAssignments ด้วย (ถ้ายังไม่มี)
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
        
        // ถ้าไม่มี assignedToVmId ให้ตรวจสอบแบบเดิม
        // ตรวจสอบใน vmAssignments
        if (vmAssignments.values().contains(request)) {
            // ปรับสถานะลูกค้าเป็น active ถ้าไม่ได้เป็น active อยู่แล้ว
            if (!request.isActive() && !request.isExpired()) {
                request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                System.out.println("ปรับสถานะลูกค้า " + request.getName() + " เป็น active เนื่องจากพบใน vmAssignments");
            }
            
            // ค้นหา VM ที่ assign ให้กับ request นี้
            for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                if (entry.getValue().equals(request)) {
                    VPSOptimization.VM vm = entry.getKey();
                    // อัปเดต assignedToVmId (ถ้ายังไม่มี)
                    if (!request.isAssignedToVM()) {
                        request.assignToVM(vm.getId());
                        System.out.println("อัปเดต assignedToVmId = " + vm.getId() + " ให้กับ request " + request.getName());
                    }
                    break;
                }
            }
            
            return true;
        }
        
        // ตรวจสอบใน VM ทั้งหมดว่ามีการบันทึกข้อมูลลูกค้าที่ตรงกับคำขอนี้หรือไม่
        String requestId = String.valueOf(request.getId());
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            for (VPSOptimization.VM vm : vps.getVms()) {
                if (vm.isAssignedToCustomer() && 
                    requestId.equals(vm.getCustomerId())) {
                    // พบว่า VM นี้ถูกกำหนดให้ลูกค้ารายนี้แล้ว
                    // แต่ไม่ได้ถูกบันทึกใน vmAssignments อาจเป็นเพราะโหลดจาก save
                    // ให้บันทึกเข้า vmAssignments ด้วย
                    vmAssignments.put(vm, request);
                    System.out.println("พบ VM ที่ถูกกำหนดให้ลูกค้า " + request.getName() + 
                                      " แต่ไม่ได้ถูกบันทึกใน vmAssignments จึงบันทึกเพิ่มเติม");
                    
                    // อัปเดต assignedToVmId (ถ้ายังไม่มี)
                    if (!request.isAssignedToVM()) {
                        request.assignToVM(vm.getId());
                        System.out.println("อัปเดต assignedToVmId = " + vm.getId() + " ให้กับ request " + request.getName());
                    }
                    
                    // ปรับสถานะลูกค้าเป็น active ถ้าไม่ได้เป็น active อยู่แล้ว และไม่ได้หมดอายุแล้ว
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

    /**
     * ค้นหา CustomerRequest ในปัจจุบันที่ตรงกับ CustomerRequest ที่เก็บในประวัติแชท
     * ใช้เพื่อแก้ไขปัญหา reference ไม่ตรงกันหลังการโหลดเกม
     * 
     * @param historyChatRequest CustomerRequest ที่เก็บในประวัติแชท
     * @return CustomerRequest ที่ตรงกันในปัจจุบัน หรือ null ถ้าไม่พบ
     */
    public CustomerRequest findMatchingCustomerRequest(CustomerRequest historyChatRequest) {
        if (historyChatRequest == null) return null;
        
        // 1. ลองค้นหาด้วย ID
        int requestId = historyChatRequest.getId();
        String requestName = historyChatRequest.getName();
        
        // ค้นหาด้วยทั้ง ID และชื่อ
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.getId() == requestId && request.getName().equals(requestName)) {
                System.out.println("พบ CustomerRequest ตรงกันด้วย ID และชื่อ: " + requestId + ", " + requestName);
                return request;
            }
        }
        
        // 2. ค้นหาด้วยชื่ออย่างเดียว
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.getName().equals(requestName)) {
                System.out.println("พบ CustomerRequest ตรงกันด้วยชื่อ: " + requestName);
                return request;
            }
        }
        
        // 3. ค้นหาด้วยคุณสมบัติอื่นๆ ที่สำคัญ
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

    /**
     * เพิ่มเมธอด getter สำหรับ requestManager
     * @return RequestManager ที่ใช้ในปัจจุบัน
     */
    public RequestManager getRequestManager() {
        return requestManager;
    }
}