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
     * รีเซ็ต instance ของ GameManager
     * เรียกใช้เมื่อต้องการเริ่มเกมใหม่
     */
    public static void resetInstance() {
        try {
            if (instance != null && instance.requestGenerator != null) {
                instance.requestGenerator.resetGenerator();
                System.out.println("Reset existing RequestGenerator before resetting GameManager");
            }
            
            if (instance != null && instance.timeManager != null) {
                // หยุด TimeManager ถ้าจำเป็น
                System.out.println("Resetting GameManager instance");
            }
            
            // สร้าง instance ใหม่
            instance = new GameManager();
            
            // อัพเดท instance ใน ResourceManager ด้วย
            ResourceManager.getInstance().setGameManager(instance);
            
            System.out.println("GameManager was reset successfully");
        } catch (Exception e) {
            System.err.println("Error resetting GameManager: " + e.getMessage());
            e.printStackTrace();
            // สร้างใหม่ในกรณีเกิดข้อผิดพลาด
            instance = new GameManager();
            
            // อัพเดท instance ใน ResourceManager ด้วย แม้จะเกิดข้อผิดพลาด
            ResourceManager.getInstance().setGameManager(instance);
        }
    }
    
    /**
     * Initialize a new game
     * @param company The player's company
     */
    public void initializeNewGame(Company company) {
        // หยุด RequestGenerator เก่าก่อน (ถ้ามี)
        if (requestGenerator != null) {
            try {
                requestGenerator.stopGenerator();
                System.out.println("Stopped existing RequestGenerator");
            } catch (Exception e) {
                System.err.println("Error stopping RequestGenerator: " + e.getMessage());
            }
        }

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
                    if (savedState.getCompletedRequests() != null && !savedState.getCompletedRequests().isEmpty()) {
                        // เพิ่ม completedRequests จาก savedState
                        this.requestManager.addCompletedRequests(savedState.getCompletedRequests());
                        System.out.println("โหลด completedRequests: " + savedState.getCompletedRequests().size() + " รายการ");
                    }
                }
                
                // ===== โหลดข้อมูล Rack =====
                try {
                    // ตรวจสอบว่าสามารถโหลดข้อมูล rack ได้หรือไม่
                    rackLoadedSuccessfully = ResourceManager.getInstance().loadRackDataFromGameState(savedState);
                    
                    // ถ้าโหลด rack สำเร็จให้แสดงข้อความแจ้งเตือน
                    if (rackLoadedSuccessfully) {
                        System.out.println("โหลดข้อมูล rack สำเร็จ (" + ResourceManager.getInstance().getRack().getInstalledVPS().size() + " VPS)");
                    } else {
                        System.err.println("ไม่สามารถโหลดข้อมูล rack ได้");
                    }
                } catch (Exception e) {
                    System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูล rack: " + e.getMessage());
                    e.printStackTrace();
                }
                
                // Start the RequestGenerator
                if (requestGenerator != null) {
                    // ตรวจสอบว่า Thread กำลังทำงานอยู่หรือไม่
                    if (!requestGenerator.isAlive()) {
                        requestGenerator.start();
                        System.out.println("Started RequestGenerator after loading state");
                    } else {
                        // ถ้า Thread กำลังทำงานอยู่แล้ว ให้ตรวจสอบว่ากำลัง paused อยู่หรือไม่
                        if (requestGenerator.isPaused()) {
                            requestGenerator.resumeGenerator();
                            System.out.println("Resumed existing RequestGenerator after loading state");
                        } else {
                            System.out.println("RequestGenerator is already running");
                        }
                    }
                }
                
                loadCompleted = true;
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดเกม: " + e.getMessage());
            e.printStackTrace();
            loadCompleted = false;
        }
        
        // แสดงผลการโหลดเกม
        if (loadCompleted) {
            System.out.println("โหลดเกมสำเร็จ");
            gameRunning = true;
        } else {
            System.err.println("โหลดเกมไม่สำเร็จ");
            gameRunning = false;
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
        
        // Deduct cost using the new spendMoney method
        if (!company.spendMoney(cost)) {
            return false;
        }
        
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