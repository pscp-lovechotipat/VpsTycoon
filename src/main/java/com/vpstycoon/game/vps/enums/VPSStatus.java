package com.vpstycoon.game.vps.enums;


public enum VPSStatus {
    RUNNING("Running"),
    STOPPED("Stopped"),
    SUSPENDED("Suspended"),
    MAINTENANCE("Maintenance"),
    ERROR("Error");
    
    private final String displayName;
    
    VPSStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 

