package com.vpstycoon.manager;

import com.vpstycoon.vps.VPSOptimization;

import java.util.HashMap;
import java.util.Map;

public class VPSManager {
    private Map<String, VPSOptimization> activeVPS;
    
    public VPSManager() {
        this.activeVPS = new HashMap<>();
    }
    
    public void createVPS(String id) {
        activeVPS.put(id, new VPSOptimization());
    }
    
    public VPSOptimization getVPS(String id) {
        return activeVPS.get(id);
    }
} 