package com.vpstycoon.game.vps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VPSInventory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
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
    
    
    public void clear() {
        inventory.clear();
    }
} 

