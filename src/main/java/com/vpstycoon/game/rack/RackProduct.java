package com.vpstycoon.game.rack;

public enum RackProduct {
    SMALL_RACK(10, 5000, 500),
    MEDIUM_RACK(11, 7500, 750),
    LARGE_RACK(12, 10000, 1000);

    private final int slots;
    private final int price;
    private final int maintenanceCost;

    RackProduct(int slots, int price, int maintenanceCost) {
        this.slots = slots;
        this.price = price;
        this.maintenanceCost = maintenanceCost;
    }

    public String getName() {
        return switch (this) {
            case SMALL_RACK -> "Small Rack";
            case MEDIUM_RACK -> "Medium Rack";
            case LARGE_RACK -> "Large Rack";
        };
    }

    public int getSlots() {
        return slots;
    }

    public int getPrice() {
        return price;
    }

    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    public String getDescription() {
        return "A rack with " + slots + " slots for VPS servers.";
    }

    public String getPriceDisplay() {
        return String.format("$%,d", price);
    }

    public String getKeepUpDisplay() {
        return String.format("$%,d", maintenanceCost);
    }
} 

