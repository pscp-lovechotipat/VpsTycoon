package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.thread.RequestGenerator;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSSize;
import com.vpstycoon.ui.game.rack.Rack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {
    private static GameManager instance;
    
    // Game components
    private RequestManager requestManager;
    private GameTimeManager timeManager;
    private RequestGenerator requestGenerator;
    private VPSInventory vpsInventory;
    private List<VPSOptimization> installedServers;
    
    // Game state
    private boolean gameRunning = false;

    private GameManager() {
        installedServers = new ArrayList<>();
        vpsInventory = new VPSInventory();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    /**
     * Initialize a new game
     * @param company The player's company
     */
    public void initializeNewGame(Company company) {
        // Create request manager with the company
        requestManager = ResourceManager.getInstance().getRequestManager();
        
        // Create time manager
        timeManager = ResourceManager.getInstance().getGameTimeManager();
        
        // Create request generator
        requestGenerator = new RequestGenerator(requestManager);
        
        // Create VPS inventory
        vpsInventory = new VPSInventory();
        
        // Create installed servers list
        installedServers = new ArrayList<>();
        
        // เริ่มต้นด้วยค่าว่าง ไม่สร้าง VPS ตัวอย่าง
        // ผู้เล่นต้องซื้อ server ด้วยตัวเอง
        
        // Save initial state
        saveState();
    }
    
    /**
     * Save the current game state
     */
    public void saveState() {
        GameState currentState = new GameState();
        
        // Save company
        if (requestManager != null) {
            currentState.setCompany(requestManager.getVmProvisioningManager().getCompany());
            
            // บันทึกข้อมูล pendingRequests และ completedRequests
            List<CustomerRequest> pendingRequests = new ArrayList<>(requestManager.getRequests());
            List<CustomerRequest> completedRequests = new ArrayList<>(requestManager.getCompletedRequests());
            
            currentState.setPendingRequests(pendingRequests);
            currentState.setCompletedRequests(completedRequests);
            
            System.out.println("บันทึกข้อมูล Requests: pending=" + pendingRequests.size() + 
                              ", completed=" + completedRequests.size());
            
            // บันทึกข้อมูลการ assign VM ให้กับ request
            Map<String, String> vmAssignments = new HashMap<>();
            
            // เก็บข้อมูลการ assign VM จาก request ทั้งหมด (ทั้ง pending และ completed)
            for (CustomerRequest request : pendingRequests) {
                if (request.isAssignedToVM()) {
                    vmAssignments.put(request.getAssignedVmId(), String.valueOf(request.getId()));
                    System.out.println("บันทึกการ assign VM " + request.getAssignedVmId() + 
                                      " ให้กับ request " + request.getName());
                }
            }
            
            for (CustomerRequest request : completedRequests) {
                if (request.isAssignedToVM()) {
                    vmAssignments.put(request.getAssignedVmId(), String.valueOf(request.getId()));
                    System.out.println("บันทึกการ assign VM " + request.getAssignedVmId() + 
                                      " ให้กับ request " + request.getName() + " (completed)");
                }
            }
            
            currentState.setVmAssignments(vmAssignments);
            System.out.println("บันทึกข้อมูลการ assign VM: " + vmAssignments.size() + " รายการ");
        }
        
        // Save date/time if time manager is running
        if (timeManager != null) {
            currentState.setLocalDateTime(timeManager.getGameDateTime());
        }
        
        // Add all installed servers to gameObjects
        for (VPSOptimization server : installedServers) {
            // Since VPSOptimization now extends GameObject, we can add it directly
            currentState.addGameObject(server);
        }
        
        // บันทึกข้อมูล VPS ที่อยู่ใน inventory
        if (vpsInventory != null && !vpsInventory.isEmpty()) {
            System.out.println("บันทึก VPS ใน inventory: " + vpsInventory.getSize() + " รายการ");
            
            // เพิ่ม VPS จาก inventory เข้าไปใน gameObjects
            for (VPSOptimization vps : vpsInventory.getAllVPS()) {
                // ตรวจสอบว่า VPS นี้ถูกบันทึกไปแล้วหรือไม่
                boolean isDuplicate = false;
                for (GameObject obj : currentState.getGameObjects()) {
                    if (obj instanceof VPSOptimization && 
                        ((VPSOptimization) obj).getVpsId() != null && 
                        ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                // ถ้ายังไม่ถูกบันทึก ให้เพิ่มเข้าไป
                if (!isDuplicate) {
                    vps.setInstalled(false); // ต้องแน่ใจว่า VPS ที่อยู่ใน inventory มีสถานะเป็น "ไม่ได้ติดตั้ง"
                    currentState.addGameObject(vps);
                    System.out.println("เพิ่ม VPS จาก inventory ไปยัง GameState: " + vps.getVpsId());
                }
            }
            
            // สร้างข้อมูลเพิ่มเติมเกี่ยวกับ VPSInventory
            Map<String, Object> inventoryData = new HashMap<>();
            List<String> vpsIds = vpsInventory.getAllVPSIds();
            inventoryData.put("vpsIds", vpsIds);
            
            // บันทึกข้อมูลละเอียดของแต่ละ VPS
            Map<String, Map<String, Object>> vpsDetails = new HashMap<>();
            for (String vpsId : vpsIds) {
                VPSOptimization vps = vpsInventory.getVPS(vpsId);
                Map<String, Object> details = new HashMap<>();
                details.put("vCPUs", vps.getVCPUs());
                details.put("ramInGB", vps.getRamInGB());
                details.put("diskInGB", vps.getDiskInGB());
                details.put("size", vps.getSize().toString());
                details.put("name", vps.getName());
                vpsDetails.put(vpsId, details);
            }
            inventoryData.put("vpsDetails", vpsDetails);
            
            // บันทึกข้อมูล VPSInventory ลงใน GameState
            currentState.setVpsInventoryData(inventoryData);
        }
        
        // Save rack information
        Rack rack = ResourceManager.getInstance().getRack();
        if (rack != null) {
            try {
                // เก็บข้อมูลการตั้งค่า Rack
                Map<String, Object> rackConfig = new HashMap<>();
                rackConfig.put("maxRacks", rack.getMaxRacks());
                rackConfig.put("currentRackIndex", rack.getCurrentRackIndex());
                rackConfig.put("maxSlotUnits", rack.getMaxSlotUnits());
                rackConfig.put("unlockedSlotUnits", rack.getUnlockedSlotUnits());
                rackConfig.put("occupiedSlotUnits", rack.getOccupiedSlotUnits());
                
                // เก็บ VPS ที่ถูกติดตั้งใน Rack ทั้งหมด (ทุก rack)
                List<String> installedVpsIds = new ArrayList<>();
                
                // ใช้ getAllInstalledVPS เพื่อรวบรวม VPS จากทุก rack
                for (VPSOptimization vps : rack.getAllInstalledVPS()) {
                    if (vps != null && vps.getVpsId() != null) {
                        installedVpsIds.add(vps.getVpsId());
                        System.out.println("บันทึก VPS ที่ติดตั้งใน Rack: " + vps.getVpsId());
                        
                        // ตรวจสอบว่า VPS นี้อยู่ใน gameObjects หรือไม่
                        boolean found = false;
                        for (GameObject obj : currentState.getGameObjects()) {
                            if (obj instanceof VPSOptimization && 
                                ((VPSOptimization) obj).getVpsId() != null && 
                                ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                                found = true;
                                // แน่ใจว่าสถานะการติดตั้งถูกต้อง
                                ((VPSOptimization) obj).setInstalled(true);
                                break;
                            }
                        }
                        // ถ้าไม่พบ VPS นี้ใน gameObjects ให้เพิ่มเข้าไป
                        if (!found) {
                            vps.setInstalled(true);
                            currentState.addGameObject(vps);
                            System.out.println("เพิ่ม VPS จาก Rack เข้า GameObjects: " + vps.getVpsId());
                        }
                    }
                }
                rackConfig.put("installedVpsIds", installedVpsIds);
                
                // บันทึกข้อมูลเพิ่มเติมของ Rack
                // บันทึกขนาดของแต่ละ rack (จำนวน slots)
                List<Integer> slotCounts = new ArrayList<>();
                for (int i = 0; i < rack.getMaxRacks(); i++) {
                    slotCounts.add(10); // ปกติคือ 10 slots ต่อ rack
                }
                rackConfig.put("slotCounts", slotCounts);
                
                // บันทึกข้อมูล rack unlockedSlotUnits สำหรับทุก rack ไม่ใช่แค่ rack ปัจจุบัน
                rackConfig.put("unlockedSlotUnitsList", rack.getUnlockedSlotUnitsList());
                
                // บันทึกข้อมูล Rack ลงใน GameState
                currentState.setRackConfiguration(rackConfig);
                System.out.println("บันทึกข้อมูล Rack สำเร็จ: " + installedVpsIds.size() + " VPS ที่ติดตั้ง");
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Rack: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ไม่พบข้อมูล Rack ที่จะบันทึก");
        }
        
        // Save free VM count from company
        if (requestManager != null && requestManager.getVmProvisioningManager() != null 
            && requestManager.getVmProvisioningManager().getCompany() != null) {
            int freeVMCount = requestManager.getVmProvisioningManager().getCompany().getAvailableVMs();
            currentState.setFreeVmCount(freeVMCount);
            System.out.println("บันทึกจำนวน Free VM: " + freeVMCount);
        }
        
        ResourceManager.getInstance().saveGameState(currentState);
        System.out.println("บันทึกข้อมูลเกมสำเร็จ");
    }

    /**
     * Load a saved game state
     */
    public void loadState() {
        // เพิ่ม flag เพื่อติดตามว่าโหลดเสร็จแล้วหรือยัง
        boolean loadCompleted = false;
        boolean rackLoadedSuccessfully = false;
        
        try {
            GameState savedState = ResourceManager.getInstance().loadGameState();
            
            if (savedState != null) {
                // Get company from saved state
                Company company = savedState.getCompany();
                
                // Initialize game with saved company
                initializeNewGame(company);
                
                // ===== โหลดข้อมูล Requests =====
                if (this.requestManager != null) {
                    // โหลดข้อมูล pendingRequests
                    if (savedState.getPendingRequests() != null && !savedState.getPendingRequests().isEmpty()) {
                        // ล้าง pendingRequests ปัจจุบันก่อน
                        this.requestManager.getRequests().clear();
                        
                        // เพิ่ม pendingRequests จาก savedState
                        this.requestManager.getRequests().addAll(savedState.getPendingRequests());
                        System.out.println("โหลด pendingRequests: " + savedState.getPendingRequests().size() + " รายการ");
                    }
                    
                    // โหลดข้อมูล completedRequests
                    if (savedState.getCompletedRequests() != null) {
                        // เพิ่ม completedRequests จาก savedState เข้าไปใน requestManager
                        this.requestManager.addCompletedRequests(savedState.getCompletedRequests());
                        System.out.println("โหลด completedRequests: " + savedState.getCompletedRequests().size() + " รายการ");
                    }
                    
                    // โหลดข้อมูล VM assignments และตั้งค่า assignedToVmId ใน CustomerRequest
                    if (savedState.getVmAssignments() != null && !savedState.getVmAssignments().isEmpty()) {
                        Map<String, String> vmAssignments = savedState.getVmAssignments();
                        System.out.println("โหลดข้อมูลการ assign VM: " + vmAssignments.size() + " รายการ");
                        
                        // สร้าง map จาก requestId ไปยัง CustomerRequest เพื่อใช้ในการค้นหา
                        Map<String, CustomerRequest> requestIdMap = new HashMap<>();
                        
                        // เพิ่ม pending requests
                        for (CustomerRequest request : this.requestManager.getRequests()) {
                            requestIdMap.put(String.valueOf(request.getId()), request);
                        }
                        
                        // เพิ่ม completed requests
                        for (CustomerRequest request : this.requestManager.getCompletedRequests()) {
                            requestIdMap.put(String.valueOf(request.getId()), request);
                        }
                        
                        // ตั้งค่า assignedToVmId ใน CustomerRequest
                        for (Map.Entry<String, String> entry : vmAssignments.entrySet()) {
                            String vmId = entry.getKey();
                            String requestId = entry.getValue();
                            
                            CustomerRequest request = requestIdMap.get(requestId);
                            if (request != null) {
                                request.assignToVM(vmId);
                                System.out.println("ตั้งค่า assignToVM " + vmId + " ให้กับ request " + request.getName());
                            } else {
                                System.out.println("ไม่พบ request id " + requestId + " สำหรับ VM " + vmId);
                            }
                        }
                    }
                } else {
                    System.err.println("ไม่สามารถโหลดข้อมูล Requests ได้: requestManager เป็น null");
                }
                
                // Set date/time from saved state
                if (savedState.getLocalDateTime() != null) {
                    timeManager = ResourceManager.getInstance().getGameTimeManager();
                }
                
                // ล้างข้อมูล servers และ inventory เดิม
                installedServers.clear();
                vpsInventory.clear();
                
                // โหลดข้อมูล VPS และ server จาก gameObjects
                if (savedState.getGameObjects() != null) {
                    System.out.println("โหลด GameObjects: " + savedState.getGameObjects().size() + " รายการ");
                    
                    for (GameObject obj : savedState.getGameObjects()) {
                        if (obj instanceof VPSOptimization) {
                            VPSOptimization server = (VPSOptimization) obj;
                            
                            // ตรวจสอบว่า VPS นี้ได้รับการติดตั้งแล้วหรือไม่
                            if (server.isInstalled()) {
                                // เพิ่มเข้า installed servers
                                installedServers.add(server);
                                timeManager.addVPSServer(server);
                                System.out.println("โหลด installed VPS: " + server.getVpsId());
                            } else {
                                // เพิ่มเข้า inventory
                                if (server.getVpsId() != null && !server.getVpsId().isEmpty()) {
                                    vpsInventory.addVPS(server.getVpsId(), server);
                                    System.out.println("โหลด VPS เข้า inventory: " + server.getVpsId());
                                } else {
                                    // ถ้าไม่มี ID ให้สร้าง ID ใหม่
                                    String newId = "server-" + System.currentTimeMillis() + "-" + vpsInventory.getSize();
                                    server.setVpsId(newId);
                                    vpsInventory.addVPS(newId, server);
                                    System.out.println("โหลด VPS ที่ไม่มี ID เข้า inventory, สร้าง ID ใหม่: " + newId);
                                }
                            }
                        }
                    }
                }
                
                // โหลดข้อมูล VPSInventory เพิ่มเติม (ถ้ามี)
                Map<String, Object> inventoryData = savedState.getVpsInventoryData();
                if (inventoryData != null && !inventoryData.isEmpty()) {
                    System.out.println("พบข้อมูล VPSInventory เพิ่มเติม");
                    
                    if (inventoryData.containsKey("vpsIds") && inventoryData.containsKey("vpsDetails")) {
                        List<String> vpsIds = (List<String>) inventoryData.get("vpsIds");
                        Map<String, Map<String, Object>> vpsDetails = (Map<String, Map<String, Object>>) inventoryData.get("vpsDetails");
                        
                        // ตรวจสอบแต่ละ VPS ID ว่ามีใน inventory แล้วหรือไม่
                        for (String vpsId : vpsIds) {
                            if (!vpsInventory.getAllVPSIds().contains(vpsId) && vpsDetails.containsKey(vpsId)) {
                                // ถ้ายังไม่มี ให้สร้าง VPS ใหม่จากข้อมูลใน vpsDetails
                                Map<String, Object> details = vpsDetails.get(vpsId);
                                
                                VPSOptimization newVPS = new VPSOptimization();
                                newVPS.setVpsId(vpsId);
                                
                                // ตั้งค่าตาม details
                                if (details.containsKey("vCPUs")) {
                                    newVPS.setVCPUs((Integer) details.get("vCPUs"));
                                }
                                
                                if (details.containsKey("ramInGB")) {
                                    newVPS.setRamInGB((Integer) details.get("ramInGB"));
                                }
                                
                                if (details.containsKey("diskInGB")) {
                                    newVPS.setDiskInGB((Integer) details.get("diskInGB"));
                                }
                                
                                if (details.containsKey("name") && details.get("name") != null) {
                                    newVPS.setName((String) details.get("name"));
                                } else {
                                    newVPS.setName("VPS-" + vpsId);
                                }
                                
                                if (details.containsKey("size") && details.get("size") != null) {
                                    try {
                                        newVPS.setSize(VPSSize.valueOf((String) details.get("size")));
                                    } catch (Exception e) {
                                        newVPS.setSize(VPSSize.SIZE_1U); // ค่าเริ่มต้น
                                    }
                                }
                                
                                // เพิ่มเข้า inventory
                                newVPS.setInstalled(false);
                                vpsInventory.addVPS(vpsId, newVPS);
                                System.out.println("สร้าง VPS ใหม่จากข้อมูลเพิ่มเติม: " + vpsId);
                            }
                        }
                    }
                }
                
                // โหลดข้อมูล Rack (ถ้ามี)
                Rack rack = null;
                Map<String, Object> rackConfig = savedState.getRackConfiguration();
                if (rackConfig != null && !rackConfig.isEmpty()) {
                    System.out.println("พบข้อมูล Rack Configuration");
                    
                    try {
                        // 1. สร้าง Rack ใหม่
                        rack = new Rack();
                        
                        // 2. สร้าง Rack ตามจำนวนที่บันทึกไว้
                        int maxRacks = rackConfig.containsKey("maxRacks") ? (Integer) rackConfig.get("maxRacks") : 0;
                        
                        // ดึงข้อมูลขนาดของแต่ละ rack (ถ้ามี)
                        List<Integer> slotCounts = null;
                        if (rackConfig.containsKey("slotCounts")) {
                            slotCounts = (List<Integer>) rackConfig.get("slotCounts");
                        }
                        
                        // สร้าง rack ในจำนวนที่บันทึกไว้
                        for (int i = 0; i < maxRacks; i++) {
                            int slotCount = 10; // ค่าเริ่มต้น
                            if (slotCounts != null && i < slotCounts.size()) {
                                slotCount = slotCounts.get(i);
                            }
                            rack.addRack(slotCount);
                            System.out.println("สร้าง Rack #" + (i+1) + " พร้อม " + slotCount + " slots");
                        }
                        
                        // 3. ถ้ามีข้อมูล unlockedSlotUnitsList ให้ตั้งค่าตามนั้น
                        if (rackConfig.containsKey("unlockedSlotUnitsList")) {
                            List<Integer> unlockedList = (List<Integer>) rackConfig.get("unlockedSlotUnitsList");
                            // ใช้เมธอด setUnlockedSlotUnitsList ที่เราเพิ่งเพิ่มใน Rack
                            rack.setUnlockedSlotUnitsList(unlockedList);
                            System.out.println("พบข้อมูล unlockedSlotUnitsList: " + unlockedList);
                        }
                        
                        // 4. ตั้งค่า rack index ปัจจุบันที่เลือก
                        if (rackConfig.containsKey("currentRackIndex")) {
                            int currentIndex = (Integer) rackConfig.get("currentRackIndex");
                            if (currentIndex >= 0 && currentIndex < maxRacks) {
                                rack.setRackIndex(currentIndex);
                                System.out.println("ตั้งค่า currentRackIndex = " + currentIndex);
                            }
                        }
                        
                        // 5. ติดตั้ง VPS ใน rack
                        if (rackConfig.containsKey("installedVpsIds")) {
                            List<String> installedVpsIds = (List<String>) rackConfig.get("installedVpsIds");
                            System.out.println("จำนวน VPS ที่ติดตั้งใน Rack: " + installedVpsIds.size());
                            
                            // ติดตั้ง VPS แต่ละตัวเข้า rack
                            for (String vpsId : installedVpsIds) {
                                // ค้นหา VPS นี้จาก gameObjects หรือ inventory
                                VPSOptimization vps = null;
                                
                                // ค้นหาจาก installedServers ก่อน
                                for (VPSOptimization server : installedServers) {
                                    if (server.getVpsId() != null && server.getVpsId().equals(vpsId)) {
                                        vps = server;
                                        break;
                                    }
                                }
                                
                                // ถ้าไม่พบ ให้ลองค้นหาจาก inventory
                                if (vps == null) {
                                    vps = vpsInventory.getVPS(vpsId);
                                    if (vps != null) {
                                        // เอาออกจาก inventory และย้ายไปที่ installed
                                        vpsInventory.removeVPS(vpsId);
                                        installedServers.add(vps);
                                    }
                                }
                                
                                if (vps != null) {
                                    // ติดตั้ง VPS เข้า rack
                                    vps.setInstalled(true);
                                    if (rack.installVPS(vps)) {
                                        System.out.println("ติดตั้ง VPS " + vpsId + " เข้า Rack สำเร็จ");
                                    } else {
                                        System.out.println("ติดตั้ง VPS " + vpsId + " เข้า Rack ไม่สำเร็จ");
                                    }
                                } else {
                                    System.out.println("ไม่พบ VPS " + vpsId + " สำหรับติดตั้งเข้า Rack");
                                }
                            }
                        }
                        
                        // บันทึก rack ใน ResourceManager
                        ResourceManager.getInstance().setRack(rack);
                        System.out.println("ตั้งค่า Rack ใน ResourceManager สำเร็จ");
                        rackLoadedSuccessfully = true;
                        
                    } catch (Exception e) {
                        System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูล Rack: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ไม่พบข้อมูล Rack Configuration");
                    // ถ้าไม่พบข้อมูล Rack ให้สร้าง Rack เริ่มต้น
                    rack = new Rack();
                    rack.addRack(10); // สร้าง rack พร้อม 10 slots
                    ResourceManager.getInstance().setRack(rack);
                    System.out.println("เพิ่ม rack เริ่มต้นพร้อม 1 slot ปลดล็อคแล้ว");
                    rackLoadedSuccessfully = true;
                }
                
                // โหลดข้อมูล Free VM Count
                if (savedState.getFreeVmCount() > 0) {
                    int freeVMCount = savedState.getFreeVmCount();
                    if (company != null) {
                        company.setAvailableVMs(freeVMCount);
                        System.out.println("ตั้งค่า Free VM Count: " + freeVMCount);
                    }
                }
                
                System.out.println("โหลดข้อมูลเสร็จสิ้น:");
                System.out.println("- Installed VPS: " + installedServers.size() + " เครื่อง");
                System.out.println("- VPS ใน Inventory: " + vpsInventory.getSize() + " เครื่อง");
                if (rack != null) {
                    System.out.println("- Rack: " + rack.getMaxRacks() + " ชั้น");
                    System.out.println("- VPS ที่ติดตั้งใน Rack: " + rack.getInstalledVPS().size() + " เครื่อง");
                }
                
                // แน่ใจว่าโหลดเสร็จแล้ว
                loadCompleted = true;
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดเกม: " + e.getMessage());
            e.printStackTrace();
        }
        
        // ตรวจสอบว่าข้อมูล rack ถูกโหลดหรือไม่
        if (!rackLoadedSuccessfully) {
            System.out.println("ไม่สามารถโหลดข้อมูล Rack ได้ ทำการสร้าง Rack ใหม่...");
            Rack rack = new Rack();
            rack.addRack(10); // สร้าง rack พร้อม 10 slots
            ResourceManager.getInstance().setRack(rack);
            System.out.println("สร้าง Rack ใหม่เสร็จเรียบร้อย");
        }
    }

    /**
     * Get the current game state
     * @return The current game state
     */
    public GameState getCurrentState() {
        return ResourceManager.getInstance().getCurrentState();
    }

    /**
     * Delete the saved game
     */
    public void deleteSavedGame() {
        ResourceManager.getInstance().deleteSaveFile();
    }

    /**
     * Check if there is a saved game
     * @return true if there is a saved game, false otherwise
     */
    public boolean hasSavedGame() {
        return ResourceManager.getInstance().hasSaveFile();
    }
    
    /**
     * Install a server from inventory
     * @param serverId The server ID in the inventory
     * @return true if successful, false otherwise
     */
    public boolean installServer(String serverId) {
        VPSOptimization server = vpsInventory.getVPS(serverId);
        
        if (server != null) {
            // Remove from inventory
            vpsInventory.removeVPS(serverId);
            
            // Mark as installed
            server.setInstalled(true);
            
            // Add to installed servers
            installedServers.add(server);
            
            // Add to time manager for overhead costs
            timeManager.addVPSServer(server);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Uninstall a server and return it to inventory
     * @param server The server to uninstall
     * @return true if successful, false otherwise
     */
    public boolean uninstallServer(VPSOptimization server) {
        if (installedServers.contains(server)) {
            // Remove from installed servers
            installedServers.remove(server);
            
            // Mark as not installed
            server.setInstalled(false);
            
            // Add to inventory
            vpsInventory.addVPS("server-" + System.currentTimeMillis(), server);
            
            // Remove from time manager
            timeManager.removeVPSServer(server);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Buy a new server and add it to inventory
     * @param vcpus Number of vCPUs
     * @param ramGB Amount of RAM in GB
     * @param diskGB Amount of disk space in GB
     * @param cost The cost of the server
     * @return true if successful, false otherwise
     */
    public boolean buyServer(int vcpus, int ramGB, int diskGB, long cost) {
        if (requestManager == null || requestManager.getVmProvisioningManager() == null) {
            return false;
        }
        
        Company company = requestManager.getVmProvisioningManager().getCompany();
        
        // Check if company has enough money
        if (company.getMoney() < cost) {
            return false;
        }
        
        // Deduct cost
        company.setMoney(company.getMoney() - cost);
        
        // Create new server
        VPSOptimization server = new VPSOptimization();
        server.setVCPUs(vcpus);
        server.setRamInGB(ramGB);
        server.setDiskInGB(diskGB);
        
        // Add to inventory
        vpsInventory.addVPS("server-" + System.currentTimeMillis(), server);
        
        return true;
    }
    
    // Getters
    
    public RequestManager getRequestManager() {
        return requestManager;
    }
    
    public GameTimeManager getTimeManager() {
        return timeManager;
    }
    
    public RequestGenerator getRequestGenerator() {
        return requestGenerator;
    }
    
    public VPSInventory getVpsInventory() {
        return vpsInventory;
    }
    
    public List<VPSOptimization> getInstalledServers() {
        return new ArrayList<>(installedServers);
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
}