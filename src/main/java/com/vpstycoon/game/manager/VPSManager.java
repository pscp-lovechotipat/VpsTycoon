package com.vpstycoon.game.manager;

import com.vpstycoon.game.vps.VPSOptimization;

import java.util.HashMap;
import java.util.Map;

public class VPSManager {
    private final Map<String, VPSOptimization> vpsMap = new HashMap<>();

    
    
    public void addVPS(String id, VPSOptimization vps) {
        vps.setVpsId(id);  
        vpsMap.put(id, vps);
    }

    public VPSOptimization getVPS(String id) {
        return vpsMap.get(id);
    }

    
    public Map<String, VPSOptimization> getVPSMap() {
        return vpsMap; 
    }

    
}

