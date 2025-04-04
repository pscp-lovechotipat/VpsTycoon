package com.vpstycoon.game.vps.enums;

public enum VPSProduct {
    BASIC_VPS("Basic Server (1U)", 1, 1, 20, 50, 5, VPSSize.SIZE_1U),          
    STANDARD_VPS("Standard Server (1U)", 2, 4, 50, 100, 10, VPSSize.SIZE_1U),  
    PREMIUM_VPS("Premium Server (2U)", 4, 8, 100, 200, 20, VPSSize.SIZE_2U),   
    ENTERPRISE_VPS("Enterprise Server (3U)", 8, 16, 200, 500, 50, VPSSize.SIZE_3U),
    BLADE_SERVER("Blade Server", 4, 8, 100, 250, 25, VPSSize.BLADE),
    TOWER_SERVER("Tower Server (4U)", 16, 32, 500, 1000, 100, VPSSize.TOWER),
    ADVANCED_CLUSTER("Advanced Cluster (4U)", 32, 64, 1000, 10000, 1000, VPSSize.SIZE_4U),
    SUPERCOMPUTER_NODE("Supercomputer Node (6U)", 64, 128, 2000, 30000, 3000, VPSSize.SIZE_6U),
    AI_TRAINING_RIG("AI Training Rig (8U)", 96, 256, 4000, 50000, 5000, VPSSize.SIZE_8U),
    QUANTUM_VPS("Quantum Server (10U)", 128, 512, 8000, 100000, 10000, VPSSize.SIZE_10U),
    HYBRID_CLOUD_SERVER("Hybrid Cloud Server (6U)", 80, 192, 3000, 60000, 6000, VPSSize.SIZE_6U),
    GLOBAL_DATA_CENTER("Global Data Center (12U)", 256, 1024, 20000, 200000, 20000, VPSSize.SIZE_12U);

    private final String name;
    private final int cpu;
    private final int ram; 
    private final int storage; 
    private final int price; 
    private final int keepUp; 
    private final VPSSize size;

    VPSProduct(String name, int cpu, int ram, int storage, int price, int keepUp, VPSSize size) {
        this.name = name;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.price = price;
        this.keepUp = keepUp;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getCpu() {
        return cpu;
    }

    public int getRam() {
        return ram;
    }

    public int getStorage() {
        return storage;
    }

    public int getPrice() {
        return price;
    }

    public int getKeepUp() {
        return keepUp;
    }

    public VPSSize getSize() {
        return size;
    }

    public String getDescription() {
        return cpu + " CPU, " + ram + "GB RAM, " + storage + "GB SSD";
    }

    
    public String getPriceDisplay() {
        return "$" + price;
    }

    
    public String getKeepUpDisplay() {
        return "$" + keepUp + "/month";
    }

    public boolean isUnlocked(int marketingLevel) {
        
        if (this == BASIC_VPS) return true;
        
        
        int requiredLevel = this.ordinal();
        return marketingLevel >= requiredLevel;
    }
}

