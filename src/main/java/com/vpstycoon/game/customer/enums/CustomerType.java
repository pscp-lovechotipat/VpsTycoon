package com.vpstycoon.game.customer.enums;

public enum CustomerType {
    
    INDIVIDUAL(0, "Individual Customer"),
    SMALL_BUSINESS(100, "Small Business"),
    MEDIUM_BUSINESS(300, "Medium Business"),
    LARGE_BUSINESS(500, "Large Business"),
    ENTERPRISE(1000, "ENTERPRISE"),
    BUSINESS(2000, "ENTERPRISE");
    
    private final int requiredPoints;
    private final String displayName;
    
    CustomerType(int requiredPoints, String displayName) {
        this.requiredPoints = requiredPoints;
        this.displayName = displayName;
    }
    
    public int getRequiredPoints() {
        return requiredPoints;
    }

    public String getDisplayName() {
        return displayName;
    }
} 
