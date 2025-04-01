package com.vpstycoon.game.vps.enums;

public enum RackProduct {
    SMALL_RACK("Small Rack (10 slots)", 10, 5000, 500),
    MEDIUM_RACK("Medium Rack (11 slots)", 11, 7500, 750),
    LARGE_RACK("Large Rack (12 slots)", 12, 10000, 1000);

    private final String name;
    private final int slots;
    private final int price;
    private final int keepUp;

    RackProduct(String name, int slots, int price, int keepUp) {
        this.name = name;
        this.slots = slots;
        this.price = price;
        this.keepUp = keepUp;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public int getPrice() {
        return price;
    }

    public int getKeepUp() {
        return keepUp;
    }

    public String getDescription() {
        return slots + " slots, Monthly maintenance: $" + keepUp;
    }

    public String getPriceDisplay() {
        return "$" + price;
    }

    public String getKeepUpDisplay() {
        return "$" + keepUp + "/month";
    }
} 

