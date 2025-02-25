package com.vpstycoon.manager;

import java.util.HashMap;
import java.util.Map;
import com.vpstycoon.vps.VPSOptimization;

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