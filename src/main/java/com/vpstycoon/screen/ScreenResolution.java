package com.vpstycoon.screen;

import javafx.stage.Screen;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ScreenResolution {
    RES_1280x720(1280, 720, "1280x720 (HD)"),
    RES_1366x768(1366, 768, "1366x768 (HD)"),
    RES_1600x900(1600, 900, "1600x900 (HD+)"),
    RES_1920x1080(1920, 1080, "1920x1080 (Full HD)"),
    RES_2048x1152(2048, 1152, "2048x1152 (2K)"),
    RES_2560x1440(2560, 1440, "2560x1440 (QHD)"),
    RES_3440x1440(3440, 1440, "3440x1440 (UWQHD)"),
    RES_3840x2160(3840, 2160, "3840x2160 (4K UHD)");

    private final int width;
    private final int height;
    private final String displayName;

    ScreenResolution(int width, int height, String displayName) {
        this.width = width;
        this.height = height;
        this.displayName = displayName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static ScreenResolution getMaxSupportedResolution() {
        Screen primaryScreen = Screen.getPrimary();
        double maxWidth = primaryScreen.getBounds().getWidth();
        double maxHeight = primaryScreen.getBounds().getHeight();

        ScreenResolution maxRes = RES_1280x720; // Default minimum
        for (ScreenResolution res : values()) {
            if (res.width <= maxWidth && res.height <= maxHeight 
                && (res.width > maxRes.width || res.height > maxRes.height)) {
                maxRes = res;
            }
        }
        return maxRes;
    }
    
    /**
     * ดึงรายการความละเอียดทั้งหมดที่รองรับ
     * @return รายการความละเอียดที่รองรับบนจอปัจจุบัน
     */
    public static List<ScreenResolution> getAvailableResolutions() {
        Screen primaryScreen = Screen.getPrimary();
        double maxWidth = primaryScreen.getBounds().getWidth();
        double maxHeight = primaryScreen.getBounds().getHeight();
        
        // กรองเอาเฉพาะความละเอียดที่จอปัจจุบันรองรับ
        return Arrays.stream(values())
                .filter(res -> res.width <= maxWidth && res.height <= maxHeight)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return displayName;
    }
} 