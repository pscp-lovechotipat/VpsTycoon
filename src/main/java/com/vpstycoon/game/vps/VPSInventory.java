package com.vpstycoon.game.vps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the player's inventory of VPS servers that are not yet installed in racks.
 */
public class VPSInventory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map of VPS ID to VPSOptimization object
    private final Map<String, VPSOptimization> inventory;
    
    public VPSInventory() {
        this.inventory = new HashMap<>();
    }
    
    /**
     * Add a VPS to the inventory
     * @param vpsId The unique ID of the VPS
     * @param vps The VPS object
     */
    public void addVPS(String vpsId, VPSOptimization vps) {
        vps.setVpsId(vpsId);  // Set the ID in the VPS object itself
        inventory.put(vpsId, vps);
    }
    
    /**
     * Remove a VPS from the inventory
     * @param vpsId The unique ID of the VPS to remove
     * @return The removed VPS, or null if not found
     */
    public VPSOptimization removeVPS(String vpsId) {
        return inventory.remove(vpsId);
    }
    
    /**
     * Get a VPS from the inventory
     * @param vpsId The unique ID of the VPS
     * @return The VPS object, or null if not found
     */
    public VPSOptimization getVPS(String vpsId) {
        return inventory.get(vpsId);
    }
    
    /**
     * Get all VPS IDs in the inventory
     * @return List of VPS IDs
     */
    public List<String> getAllVPSIds() {
        return new ArrayList<>(inventory.keySet());
    }
    
    /**
     * Get all VPS objects in the inventory
     * @return List of VPS objects
     */
    public List<VPSOptimization> getAllVPS() {
        return new ArrayList<>(inventory.values());
    }
    
    /**
     * Get the inventory map
     * @return The inventory map
     */
    public Map<String, VPSOptimization> getInventoryMap() {
        return inventory;
    }
    
    /**
     * Get the number of VPS servers in the inventory
     * @return The inventory size
     */
    public int getSize() {
        return inventory.size();
    }
    
    /**
     * Check if the inventory is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return inventory.isEmpty();
    }
    
    /**
     * Clear the inventory
     */
    public void clear() {
        inventory.clear();
    }
} 