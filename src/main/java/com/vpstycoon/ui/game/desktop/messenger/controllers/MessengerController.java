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
            
            // Only enable the assignVM button if there are running VMs available
            boolean hasAvailableVMs = false;
            if (newVal != null && !newVal.isActive() && !newVal.isExpired()) {
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    hasAvailableVMs = vps.getVms().stream()
                            .anyMatch(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) && 
                                    !vm.isAssignedToCustomer());
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
                
                // ถ้าไม่มี VM จริงในระบบ แต่มีค่า availableVMs บอกว่ามี และมีเซิร์ฟเวอร์อยู่ในระบบ
                if (!hasAnyVMs && company.getAvailableVMs() > 0 && totalServerCount > 0) {
                    System.out.println("สร้าง VM objects ในขณะที่กดปุ่ม Assign VM");
                    
                    // สร้าง VM ตามจำนวนที่ควรมี
                    createVirtualMachines(company.getAvailableVMs(), totalServerCount);
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
                    if (company.getAvailableVMs() > 0) {
                        chatAreaView.addSystemMessage("แม้ว่าระบบจะรายงานว่ามี " + company.getAvailableVMs() + 
                            " VM ที่ว่าง แต่ไม่สามารถหา VM จริงในระบบได้ โปรดสร้าง VM ใหม่ก่อน");
                    } else {
                        chatAreaView.addSystemMessage("ไม่มี VM ที่พร้อมใช้งาน โปรดสร้าง VM ใหม่ก่อน");
                    }
                    return;
                }
                
                // สร้าง Dialog สำหรับเลือก VM
                VMSelectionDialog dialog = new VMSelectionDialog(allAvailableVMs, rootStack);
                dialog.setOnConfirm(() -> {
                    VPSOptimization.VM selectedVM = dialog.getSelectedVM();
                    if (selectedVM != null) {
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
            System.out.println("4. สร้าง VM objects เนื่องจากมีค่า availableVMs=" + availableVMs + 
                               " แต่ไม่มี VM objects จริงในระบบ");
            
            createVirtualMachines(availableVMs, totalServers);
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
        
        // ตรวจสอบความสอดคล้องของข้อมูล
        int countFromVMs = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            countFromVMs += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && 
                            !vmAssignments.containsKey(vm) && 
                            !vm.isAssignedToCustomer())
                    .count();
        }
        
        if (countFromVMs != availableVMs) {
            System.out.println("⚠️ ข้อมูลไม่สอดคล้องกัน: จำนวน VM จริงที่ว่าง = " + countFromVMs + 
                              " แต่ค่า availableVMs = " + availableVMs);
        }
        
        // อัพเดต Dashboard ด้วยข้อมูลล่าสุด
        dashboardView.updateDashboard(company.getRating(), requestManager.getRequests().size(), availableVMs, totalServers);
        
        // บันทึกค่าลงใน Company เพื่อให้สามารถเรียกใช้ค่านี้ได้จากที่อื่น
        company.setAvailableVMs(availableVMs);
        
        // บันทึกค่าลงใน GameState ผ่าน ResourceManager ด้วย
        if (currentState != null) {
            currentState.setFreeVmCount(availableVMs);
        }
        
        // Update the AssignVMButton status for selected request
        CustomerRequest selected = requestListView.getSelectedRequest();
        if (selected != null && !selected.isActive() && !selected.isExpired()) {
            chatAreaView.getAssignVMButton().setDisable(availableVMs <= 0);
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
                    rackVMs += (int) vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && 
                                    !vmAssignments.containsKey(vm) &&
                                    !vm.isAssignedToCustomer())
                            .count();
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
        
        // ถ้ามีเซิร์ฟเวอร์ คำนวณว่าแต่ละเซิร์ฟเวอร์ควรมี VM กี่ตัว
        if (serverCount > 0) {
            int vmsPerServer = (int) Math.ceil((double) vmCount / serverCount);
            
            for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                // ไม่สร้างเกินจำนวน VM ที่เหลือ
                int vmsToCreate = Math.min(remainingVMs, vmsPerServer);
                
                for (int i = 0; i < vmsToCreate; i++) {
                    // สร้าง VM ตามวิธีที่ถูกต้อง
                    String vmName = "vm-" + System.currentTimeMillis() + "-" + i;
                    String vmIp = generateRandomIp();
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                        vmIp,
                        vmName,
                        vps.getVCPUs(),
                        vps.getRamInGB() + " GB",
                        vps.getDiskInGB() + " GB",
                        "Running"
                    );
                    
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
        
        // อัพเดต dashboard เพื่อแสดงสถานะล่าสุด
        updateDashboard();
        
        // เพิ่มข้อความในแชท
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, 
            "VM provisioning completed successfully", new HashMap<>()));
        chatAreaView.addSystemMessage("VM provisioning completed successfully");
        
        // ให้รางวัล skill points
        skillPointsManager.awardSkillPoints(request, 0.2);
        
        // อัพเดตรายการคำขอ
        updateRequestList();
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
            
            // ปล่อย VM
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
            
            // ตรวจสอบคำขอที่ active อยู่และกำหนดให้กับ VM ที่เหมาะสม
            List<CustomerRequest> activeRequests = new ArrayList<>();
            for (CustomerRequest request : requestManager.getRequests()) {
                if (request.isActive() && !request.isExpired()) {
                    activeRequests.add(request);
                    System.out.println("พบคำขอที่ active: " + request.getName() + " (ID: " + request.getId() + ")");
                }
            }
            
            // ถ้ามีคำขอที่ active อยู่ ให้กำหนด VM ให้แต่ละคำขอ
            if (!activeRequests.isEmpty()) {
                int assignedCount = 0;
                
                // สร้าง VM เพิ่มถ้าจำเป็น (เฉพาะกรณีที่ VPS มี VM น้อยกว่าจำนวนคำขอ)
                int totalVMs = 0;
                for (VPSOptimization vps : loadedVPSList) {
                    totalVMs += vps.getVms().size();
                }
                
                if (totalVMs < activeRequests.size()) {
                    // สร้าง VM เพิ่มเติมสำหรับคำขอที่ active
                    int neededVMs = activeRequests.size() - totalVMs;
                    createVirtualMachines(neededVMs, loadedVPSList.size());
                    System.out.println("สร้าง VM เพิ่ม " + neededVMs + " VM สำหรับคำขอที่ active");
                }
                
                // เชื่อมต่อ VM กับคำขอที่ active
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
                    }
                }
                
                System.out.println("กำหนด VM ให้กับคำขอที่ active แล้ว " + assignedCount + " VM");
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
        return vmAssignments.values().contains(request);
    }
}