package com.vpstycoon.event;

import java.util.Random;

public enum EventType {
    DATA_BREACH("Data Breach", "Install Firewall and upgrade security", 10000, 0.2),
    POWER_OUTAGE("Power Outage", "Install UPS or move to co-location", 5000, 0.15),
    NETWORK_CONGESTION("Network Congestion", "Upgrade bandwidth or add load balancer", 8000, 0.25),
    HARDWARE_FAILURE("Hardware Failure", "Replace hardware or use backup", 12000, 0.3),
    PRICE_COMPLAINT("Price Complaint", "Adjust pricing or add promotions", 3000, 0.1),
    DDOS_ATTACK("DDoS Attack", "Install DDoS protection", 15000, 0.35),
    COMPETITOR_PRESSURE("Competitor Pressure", "Upgrade services or increase marketing", 7000, 0.2),
    IP_SHORTAGE("IP Shortage", "Purchase additional IP blocks", 4000, 0.15),
    SYSTEM_ERROR("System Error", "Rollback system or update patches", 6000, 0.2),
    SPECIAL_EVENT("Special Event", "Positive event - no action needed", 0, 0.0); 

    private final String displayName;
    private final String solution;
    private final long baseCost;        
    private final double scaleFactor;   

    EventType(String displayName, String solution, long baseCost, double scaleFactor) {
        this.displayName = displayName;
        this.solution = solution;
        this.baseCost = baseCost;
        this.scaleFactor = scaleFactor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSolution() {
        return solution;
    }

    public long getBaseCost() {
        return baseCost;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    
    public long calculateCost(Random random) {
        if (baseCost == 0) return 0; 
        double variation = (random.nextDouble() * 2 - 1) * scaleFactor; 
        return Math.round(baseCost * (1 + variation));
    }
}
