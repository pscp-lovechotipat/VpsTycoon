package com.vpstycoon.manager;

import com.vpstycoon.model.GameObject;
import com.vpstycoon.model.GameState;
import com.vpstycoon.model.customer.CustomerRequest;
import com.vpstycoon.model.server.Rack;
import com.vpstycoon.model.server.VPSInventory;
import com.vpstycoon.model.server.VPSOptimization;
import com.vpstycoon.thread.GameTimeManager;
import com.vpstycoon.thread.RequestGenerator;

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

            System.out.println("GameManager was reset successfully");
        } catch (Exception e) {
            System.err.println("Error resetting GameManager: " + e.getMessage());
            e.printStackTrace();

            instance = new GameManager();
        }
    }

    public void saveState() {
        GameState currentState = new GameState();

        if (requestManager != null) {
            currentState.setCompany(requestManager.getCompany());

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

            currentState.setVpsInventoryData(vpsInventory.toSaveData());
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

        if (requestManager != null && requestManager.getCompany() != null) {
            int freeVMCount = requestManager.getCompany().getAvailableVMs();
            currentState.setFreeVmCount(freeVMCount);
            System.out.println("บันทึกจำนวน Free VM: " + freeVMCount);
        }

        ResourceManager.getInstance().saveGameState(currentState);
        System.out.println("บันทึกข้อมูลเกมสำเร็จ");
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

    public VPSInventory getVpsInventory() {
        return vpsInventory;
    }
}
