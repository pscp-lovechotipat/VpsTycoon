package com.vpstycoon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.config.DefaultGameConfig;

public class ConfigSerializer {
    private static final String CONFIG_FILE = "game_config.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void saveToFile(GameConfig config) {
        try {
            File configFile = new File(CONFIG_FILE);
            mapper.writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + e.getMessage(), e);
        }
    }

    public static GameConfig loadFromFile() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return DefaultGameConfig.getInstance();
        }
        try {
            return mapper.readValue(configFile, DefaultGameConfig.class);
        } catch (IOException e) {
            return DefaultGameConfig.getInstance();
        }
    }
} 