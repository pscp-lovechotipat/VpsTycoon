package com.vpstycoon.screen;

import javafx.stage.Screen;

public enum ScreenResolution {
    HD(1280, 720, "1280x720 (HD)"),
    HD_PLUS(1600, 900, "1600x900 (HD+)"),
    FULL_HD(1920, 1080, "1920x1080 (Full HD)");

    private final int width;
    private final int height;
    private final String displayName;

    ScreenResolution(int width, int height, String displayName) {
        this.width = width;
        this.height = height;
        this.displayName = displayName;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() {
        return displayName;
    }

    public static ScreenResolution getMaxSupportedResolution() {
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        ScreenResolution[] resolutions = values();
        for (int i = resolutions.length - 1; i >= 0; i--) {
            if (resolutions[i].width <= bounds.getWidth() && 
                resolutions[i].height <= bounds.getHeight()) {
                return resolutions[i];
            }
        }
        return HD; // fallback to minimum resolution
    }
} 