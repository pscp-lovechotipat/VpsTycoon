package com.vpstycoon.config;

import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.screen.ScreenResolution;

public class DefaultGameConfig implements GameConfig {
    private static DefaultGameConfig instance;
    private ScreenResolution selectedResolution = ScreenResolution.RES_1280x720;
    private boolean isFullscreen = false;
    private double musicVolume = 0.3;
    private double sfxVolume = 0.5;
    private boolean vsyncEnabled = true;
    
    private DefaultGameConfig() {}
    
    public static DefaultGameConfig getInstance() {
        if (instance == null) {
            instance = new DefaultGameConfig();
        }
        return instance;
    }

    @Override
    public ScreenResolution getResolution() {
        return selectedResolution;
    }

    @Override
    public void setResolution(ScreenResolution resolution) {
        this.selectedResolution = resolution;
    }

    @Override
    public boolean isFullscreen() {
        return isFullscreen;
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        this.isFullscreen = fullscreen;
    }

    @Override
    public double getMusicVolume() {
        return musicVolume;
    }

    @Override
    public void setMusicVolume(double volume) {
        this.musicVolume = volume;
    }

    @Override
    public double getSfxVolume() {
        return sfxVolume;
    }

    @Override
    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;
    }

    @Override
    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    @Override
    public void setVsyncEnabled(boolean enabled) {
        this.vsyncEnabled = enabled;
    }

    @Override
    public void save() {
        try {
            ConfigSerializer.saveToFile(this);
        } catch (Exception e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    @Override
    public void load() {
        try {
            GameConfig loaded = ConfigSerializer.loadFromFile();
            if (loaded != null) {
                this.selectedResolution = loaded.getResolution();
                this.isFullscreen = loaded.isFullscreen();
                this.musicVolume = loaded.getMusicVolume();
                this.sfxVolume = loaded.getSfxVolume();
                this.vsyncEnabled = loaded.isVsyncEnabled();

                ResourceManager.getInstance().getAudioManager().setMusicVolume(this.musicVolume);
                ResourceManager.getInstance().getAudioManager().setSfxVolume(this.sfxVolume);
            }
        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    // ... implement other interface methods ...
} 