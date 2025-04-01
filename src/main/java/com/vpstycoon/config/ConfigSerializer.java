package com.vpstycoon.config;

import com.vpstycoon.screen.ScreenResolution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigSerializer {
    private static final String CONFIG_FILE = "game_config.properties";

    public static void saveToFile(GameConfig config) {
        Properties properties = new Properties();
        properties.setProperty("resolution", config.getResolution().toString());
        properties.setProperty("fullscreen", String.valueOf(config.isFullscreen()));
        properties.setProperty("musicVolume", String.valueOf(config.getMusicVolume()));
        properties.setProperty("sfxVolume", String.valueOf(config.getSfxVolume()));
        properties.setProperty("vsyncEnabled", String.valueOf(config.isVsyncEnabled()));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            properties.store(out, "Game Configuration");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + e.getMessage(), e);
        }
    }

    public static GameConfig loadFromFile() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return DefaultGameConfig.getInstance();
        }

        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(configFile)) {
            properties.load(in);
            
            DefaultGameConfig config = DefaultGameConfig.getInstance();
            
            String resolutionStr = properties.getProperty("resolution");
            if (resolutionStr != null) {
                try {
                    config.setResolution(ScreenResolution.valueOf(resolutionStr));
                } catch (IllegalArgumentException e) {
                    
                }
            }
            
            String fullscreenStr = properties.getProperty("fullscreen");
            if (fullscreenStr != null) {
                config.setFullscreen(Boolean.parseBoolean(fullscreenStr));
            }
            
            String musicVolumeStr = properties.getProperty("musicVolume");
            if (musicVolumeStr != null) {
                try {
                    config.setMusicVolume(Double.parseDouble(musicVolumeStr));
                } catch (NumberFormatException e) {
                    
                }
            }
            
            String sfxVolumeStr = properties.getProperty("sfxVolume");
            if (sfxVolumeStr != null) {
                try {
                    config.setSfxVolume(Double.parseDouble(sfxVolumeStr));
                } catch (NumberFormatException e) {
                    
                }
            }
            
            String vsyncEnabledStr = properties.getProperty("vsyncEnabled");
            if (vsyncEnabledStr != null) {
                config.setVsyncEnabled(Boolean.parseBoolean(vsyncEnabledStr));
            }
            
            return config;
        } catch (IOException e) {
            return DefaultGameConfig.getInstance();
        }
    }
} 

