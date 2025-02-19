package com.vpstycoon;

import javafx.stage.Screen;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ScreenResolution {
    HD(1280, 720),
    FULL_HD(1920, 1080),
    QHD(2560, 1440),
    UHD(3840, 2160);

    private final int width;
    private final int height;

    ScreenResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Get available resolutions based on screen bounds
    public static List<ScreenResolution> getAvailableResolutions() {
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        int maxWidth = (int) bounds.getWidth();
        int maxHeight = (int) bounds.getHeight();

        return Arrays.stream(ScreenResolution.values())
                .filter(res -> res.width <= maxWidth && res.height <= maxHeight)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
