package com.vpstycoon;

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
}
