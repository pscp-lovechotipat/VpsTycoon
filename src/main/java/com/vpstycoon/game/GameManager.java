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
    
    
    private RequestManager requestManager;
    private GameTimeManager timeManager;
    private RequestGenerator requestGenerator;
    private VPSInventory vpsInventory;
    private List<VPSOptimization> installedServers;
    
    
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
    
    
    public static void resetInstance() {
        try {
            if (instance != null && instance.requestGenerator != null) {
                instance.requestGenerator.resetGenerator();
                System.out.println("Reset existing RequestGenerator before resetting GameManager");
            }
            
            if (instance != null && instance.timeManager != null) {
                
                System.out.println("Resetting GameManager instance");
            }
            
            
            instance = new GameManager();
            
            
            ResourceManager.getInstance().setGameManager(instance);
            
            System.out.println("GameManager was reset successfully");
        } catch (Exception e) {
            System.err.println("Error resetting GameManager: " + e.getMessage());
            e.printStackTrace();
            
            instance = new GameManager();
            
            
            ResourceManager.getInstance().setGameManager(instance);
        }
    }
    
    
    public void initializeNewGame(Company company) {
        
        if (requestGenerator != null) {
            try {
                requestGenerator.stopGenerator();
                System.out.println("Stopped existing RequestGenerator");
            } catch (Exception e) {
                System.err.println("Error stopping RequestGenerator: " + e.getMessage());
            }
        }

        
        requestManager = ResourceManager.getInstance().getRequestManager();
        
        
        timeManager = ResourceManager.getInstance().getGameTimeManager();
        
        
        requestGenerator = new RequestGenerator(requestManager);
        
        
        vpsInventory = new VPSInventory();
        
        
        installedServers = new ArrayList<>();
        
        
        
        
        
        saveState();
    }
    
    
    public void saveState() {
        GameState currentState = new GameState();
        
        
        if (requestManager != null) {
            currentState.setCompany(requestManager.getVmProvisioningManager().getCompany());
            
            
            List<CustomerRequest> pendingRequests = new ArrayList<>(requestManager.getRequests());
            List<CustomerRequest> completedRequests = new ArrayList<>(requestManager.getCompletedRequests());
            
            currentState.setPendingRequests(pendingRequests);
            currentState.setCompletedRequests(completedRequests);
            
            System.out.println("บันทึกข้อมูล Requests: pending=" + pendingRequests.size() + 
                              ", completed=" + completedRequests.size());
            
            
            Map<String, String> vmAssignments = new HashMap<>();
            
            
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
        
        
        if (timeManager != null) {
            currentState.setLocalDateTime(timeManager.getGameDateTime());
        }
        
        
        for (VPSOptimization server : installedServers) {
            
            currentState.addGameObject(server);
        }
        
        
        if (vpsInventory != null && !vpsInventory.isEmpty()) {
            System.out.println("บันทึก VPS ใน inventory: " + vpsInventory.getSize() + " รายการ");
            
            
            for (VPSOptimization vps : vpsInventory.getAllVPS()) {
                
                boolean isDuplicate = false;
                for (GameObject obj : currentState.getGameObjects()) {
                    if (obj instanceof VPSOptimization && 
                        ((VPSOptimization) obj).getVpsId() != null && 
                        ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                
                if (!isDuplicate) {
                    vps.setInstalled(false); 
                    currentState.addGameObject(vps);
                    System.out.println("เพิ่ม VPS จาก inventory ไปยัง GameState: " + vps.getVpsId());
                }
            }
            
            
            Map<String, Object> inventoryData = new HashMap<>();
            List<String> vpsIds = vpsInventory.getAllVPSIds();
            inventoryData.put("vpsIds", vpsIds);
            
            
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
            
            
            currentState.setVpsInventoryData(inventoryData);
        }
        
        
        Rack rack = ResourceManager.getInstance().getRack();
        if (rack != null) {
            try {
                
                Map<String, Object> rackConfig = new HashMap<>();
                rackConfig.put("maxRacks", rack.getMaxRacks());
                rackConfig.put("currentRackIndex", rack.getCurrentRackIndex());
                rackConfig.put("maxSlotUnits", rack.getMaxSlotUnits());
                rackConfig.put("unlockedSlotUnits", rack.getUnlockedSlotUnits());
                rackConfig.put("occupiedSlotUnits", rack.getOccupiedSlotUnits());
                
                
                List<String> installedVpsIds = new ArrayList<>();
                
                
                for (VPSOptimization vps : rack.getAllInstalledVPS()) {
                    if (vps != null && vps.getVpsId() != null) {
                        installedVpsIds.add(vps.getVpsId());
                        System.out.println("บันทึก VPS ที่ติดตั้งใน Rack: " + vps.getVpsId());
                        
                        
                        boolean found = false;
                        for (GameObject obj : currentState.getGameObjects()) {
                            if (obj instanceof VPSOptimization && 
                                ((VPSOptimization) obj).getVpsId() != null && 
                                ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                                found = true;
                                
                                ((VPSOptimization) obj).setInstalled(true);
                                break;
                            }
                        }
                        
                        if (!found) {
                            vps.setInstalled(true);
                            currentState.addGameObject(vps);
                            System.out.println("เพิ่ม VPS จาก Rack เข้า GameObjects: " + vps.getVpsId());
                        }
                    }
                }
                rackConfig.put("installedVpsIds", installedVpsIds);
                
                
                
                List<Integer> slotCounts = new ArrayList<>();
                for (int i = 0; i < rack.getMaxRacks(); i++) {
                    slotCounts.add(10); 
                }
                rackConfig.put("slotCounts", slotCounts);
                
                
                rackConfig.put("unlockedSlotUnitsList", rack.getUnlockedSlotUnitsList());
                
                
                currentState.setRackConfiguration(rackConfig);
                System.out.println("บันทึกข้อมูล Rack สำเร็จ: " + installedVpsIds.size() + " VPS ที่ติดตั้ง");
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Rack: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ไม่พบข้อมูล Rack ที่จะบันทึก");
        }
        
        
        if (requestManager != null && requestManager.getVmProvisioningManager() != null 
            && requestManager.getVmProvisioningManager().getCompany() != null) {
            int freeVMCount = requestManager.getVmProvisioningManager().getCompany().getAvailableVMs();
            currentState.setFreeVmCount(freeVMCount);
            System.out.println("บันทึกจำนวน Free VM: " + freeVMCount);
        }
        
        ResourceManager.getInstance().saveGameState(currentState);
        System.out.println("บันทึกข้อมูลเกมสำเร็จ");
    }

    
    public void loadState() {
        
        boolean loadCompleted = false;
        boolean rackLoadedSuccessfully = false;
        
        try {
            GameState savedState = ResourceManager.getInstance().loadGameState();
            
            if (savedState != null) {
                
                Company company = savedState.getCompany();
                
                
                initializeNewGame(company);
                
                
                if (this.requestManager != null) {
                    
                    if (savedState.getPendingRequests() != null && !savedState.getPendingRequests().isEmpty()) {
                        
                        this.requestManager.getRequests().clear();
                        
                        
                        this.requestManager.getRequests().addAll(savedState.getPendingRequests());
                        System.out.println("โหลด pendingRequests: " + savedState.getPendingRequests().size() + " รายการ");
                    }
                    
                    
                    if (savedState.getCompletedRequests() != null && !savedState.getCompletedRequests().isEmpty()) {
                        
                        this.requestManager.addCompletedRequests(savedState.getCompletedRequests());
                        System.out.println("โหลด completedRequests: " + savedState.getCompletedRequests().size() + " รายการ");
                    }
                }
                
                
                try {
                    
                    rackLoadedSuccessfully = ResourceManager.getInstance().loadRackDataFromGameState(savedState);
                    
                    
                    if (rackLoadedSuccessfully) {
                        System.out.println("โหลดข้อมูล rack สำเร็จ (" + ResourceManager.getInstance().getRack().getInstalledVPS().size() + " VPS)");
                    } else {
                        System.err.println("ไม่สามารถโหลดข้อมูล rack ได้");
                    }
                } catch (Exception e) {
                    System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูล rack: " + e.getMessage());
                    e.printStackTrace();
                }
                
                
                if (requestGenerator != null) {
                    
                    if (!requestGenerator.isAlive()) {
                        requestGenerator.start();
                        System.out.println("Started RequestGenerator after loading state");
                    } else {
                        
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
        
        
        if (loadCompleted) {
            System.out.println("โหลดเกมสำเร็จ");
            gameRunning = true;
        } else {
            System.err.println("โหลดเกมไม่สำเร็จ");
            gameRunning = false;
        }
    }

    
    public GameState getCurrentState() {
        return ResourceManager.getInstance().getCurrentState();
    }

    
    public void deleteSavedGame() {
        ResourceManager.getInstance().deleteSaveFile();
    }

    
    public boolean hasSavedGame() {
        return ResourceManager.getInstance().hasSaveFile();
    }
    
    
    public boolean installServer(String serverId) {
        VPSOptimization server = vpsInventory.getVPS(serverId);
        
        if (server != null) {
            
            vpsInventory.removeVPS(serverId);
            
            
            server.setInstalled(true);
            
            
            installedServers.add(server);
            
            
            timeManager.addVPSServer(server);
            
            return true;
        }
        
        return false;
    }
    
    
    public boolean uninstallServer(VPSOptimization server) {
        if (installedServers.contains(server)) {
            
            installedServers.remove(server);
            
            
            server.setInstalled(false);
            
            
            vpsInventory.addVPS("server-" + System.currentTimeMillis(), server);
            
            
            timeManager.removeVPSServer(server);
            
            return true;
        }
        
        return false;
    }
    
    
    public boolean buyServer(int vcpus, int ramGB, int diskGB, long cost) {
        if (requestManager == null || requestManager.getVmProvisioningManager() == null) {
            return false;
        }
        
        Company company = requestManager.getVmProvisioningManager().getCompany();
        
        
        if (company.getMoney() < cost) {
            return false;
        }
        
        
        if (!company.spendMoney(cost)) {
            return false;
        }
        
        
        VPSOptimization server = new VPSOptimization();
        server.setVCPUs(vcpus);
        server.setRamInGB(ramGB);
        server.setDiskInGB(diskGB);
        
        
        vpsInventory.addVPS("server-" + System.currentTimeMillis(), server);
        
        return true;
    }
    
    
    
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
