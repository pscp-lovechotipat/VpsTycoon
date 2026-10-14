package com.vpstycoon.model.server;

public enum VPSStatus {
    RUNNING("Running");

    private final String displayName;

    VPSStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
