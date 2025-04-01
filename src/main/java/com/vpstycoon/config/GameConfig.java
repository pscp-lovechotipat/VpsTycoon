package com.vpstycoon.config;

import com.vpstycoon.screen.ScreenResolution;

public interface GameConfig {
    ScreenResolution getResolution();
    void setResolution(ScreenResolution resolution);
    boolean isFullscreen();
    void setFullscreen(boolean fullscreen);
    double getMusicVolume();
    void setMusicVolume(double volume);
    double getSfxVolume();
    void setSfxVolume(double volume);
    boolean isVsyncEnabled();
    void setVsyncEnabled(boolean enabled);
    void save();
    void load();
} 

