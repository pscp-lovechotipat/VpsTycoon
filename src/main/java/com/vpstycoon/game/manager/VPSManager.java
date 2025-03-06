package com.vpstycoon.game.manager;

import com.vpstycoon.game.vps.VPSOptimization;

import java.util.HashMap;
import java.util.Map;

public class VPSManager {
    private final Map<String, VPSOptimization> vpsMap = new HashMap<>();

    public void createVPS(String id) {
        // Placeholder for creation logic (e.g., validation)
        System.out.println("VPS created with ID: " + id);
    }

    public VPSOptimization getVPS(String id) {
        return vpsMap.get(id);
    }

    // Added method to resolve the errors
    public Map<String, VPSOptimization> getVPSMap() {
        return vpsMap; // Direct access; consider returning Collections.unmodifiableMap(vpsMap) if immutability is needed
    }
}