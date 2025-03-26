package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.thread.RequestGenerator;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSSize;

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
        
        ResourceManager.getInstance().saveGameState(currentState);
    }

    /**
     * Load a saved game state
     */
    public void loadState() {
        GameState savedState = ResourceManager.getInstance().loadGameState();
        
        if (savedState != null) {
            // Get company from saved state
            Company company = savedState.getCompany();
            
            // Initialize game with saved company
            initializeNewGame(company);
            
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
            
            System.out.println("โหลดข้อมูลเสร็จสิ้น:");
            System.out.println("- Installed VPS: " + installedServers.size() + " เครื่อง");
            System.out.println("- VPS ใน inventory: " + vpsInventory.getSize() + " เครื่อง");
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