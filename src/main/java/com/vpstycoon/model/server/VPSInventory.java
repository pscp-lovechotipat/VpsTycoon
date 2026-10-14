package com.vpstycoon.model.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VPSInventory {
    private final Map<String, VPSOptimization> inventory;

    public VPSInventory() {
        this.inventory = new HashMap<>();
    }

    public void addVPS(String vpsId, VPSOptimization vps) {
        vps.setVpsId(vpsId);
        inventory.put(vpsId, vps);
    }

    public VPSOptimization removeVPS(String vpsId) {
        return inventory.remove(vpsId);
    }

    public VPSOptimization getVPS(String vpsId) {
        return inventory.get(vpsId);
    }

    public List<String> getAllVPSIds() {
        return new ArrayList<>(inventory.keySet());
    }

    public List<VPSOptimization> getAllVPS() {
        return new ArrayList<>(inventory.values());
    }

    public Map<String, VPSOptimization> getInventoryMap() {
        return inventory;
    }

    public int getSize() {
        return inventory.size();
    }

    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    public Map<String, Object> toSaveData() {
        Map<String, Object> inventoryData = new HashMap<>();
        List<String> vpsIds = getAllVPSIds();
        inventoryData.put("vpsIds", vpsIds);

        Map<String, Map<String, Object>> vpsDetails = new HashMap<>();
        for (String vpsId : vpsIds) {
            VPSOptimization vps = getVPS(vpsId);
            Map<String, Object> details = new HashMap<>();
            details.put("vCPUs", vps.getVCPUs());
            details.put("ramInGB", vps.getRamInGB());
            details.put("diskInGB", vps.getDiskInGB());
            details.put("size", vps.getSize().toString());
            details.put("name", vps.getName());
            vpsDetails.put(vpsId, details);
        }
        inventoryData.put("vpsDetails", vpsDetails);
        return inventoryData;
    }
}
