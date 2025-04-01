package com.vpstycoon.game.vps.enums;


public enum VPSSize {
    SIZE_1U("1U", 1, 1.0),
    SIZE_2U("2U", 2, 1.5),
    SIZE_3U("3U", 3, 2.0),
    SIZE_4U("4U", 4, 2.5),
    BLADE("Blade Server", 1, 1.2),
    TOWER("Tower Server", 4, 3.0),
    SIZE_6U("6U", 6, 3.5),
    SIZE_8U("8U", 8, 4.0),
    SIZE_10U("10U", 10, 4.5),
    SIZE_12U("12U", 12, 5.0);

    private final String displayName;
    private final int slotsRequired;
    private final double performanceMultiplier;

    VPSSize(String displayName, int slotsRequired, double performanceMultiplier) {
        this.displayName = displayName;
        this.slotsRequired = slotsRequired;
        this.performanceMultiplier = performanceMultiplier;
    }

    VPSSize(String displayName, int slotsRequired) {
        this(displayName, slotsRequired, 1.0);
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlotsRequired() {
        return slotsRequired;
    }

    public double getPerformanceMultiplier() {
        return performanceMultiplier;
    }
}

