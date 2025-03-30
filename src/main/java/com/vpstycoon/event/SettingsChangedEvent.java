package com.vpstycoon.event;

import com.vpstycoon.config.GameConfig;

public class SettingsChangedEvent {
    private final GameConfig newConfig;

    public SettingsChangedEvent(GameConfig newConfig) {
        this.newConfig = newConfig;
    }

    public GameConfig getNewConfig() {
        return newConfig;
    }
} 
