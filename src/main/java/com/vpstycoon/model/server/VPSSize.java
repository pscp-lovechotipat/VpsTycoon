package com.vpstycoon.model.server;

public enum VPSSize {
    SIZE_1U("1U", 1),
    SIZE_2U("2U", 2),
    SIZE_3U("3U", 3),
    SIZE_4U("4U", 4),
    BLADE("Blade Server", 1),
    TOWER("Tower Server", 4),
    SIZE_6U("6U", 6),
    SIZE_8U("8U", 8),
    SIZE_10U("10U", 10),
    SIZE_12U("12U", 12);

    private final String displayName;
    private final int slotsRequired;

    VPSSize(String displayName, int slotsRequired) {
        this.displayName = displayName;
        this.slotsRequired = slotsRequired;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlotsRequired() {
        return slotsRequired;
    }
}
