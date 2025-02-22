package com.vpstycoon.game;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameManager {
    private static GameManager instance;

    private static final String SAVE_FILE = "savegame.dat";

    private GameState currentState;
    private final ObjectMapper objectMapper;

    private GameManager() {
        currentState = new GameState();
        objectMapper = new ObjectMapper();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void loadState(GameState state) {
        this.currentState = state;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void saveState() {
        try {
            File saveFile = new File(SAVE_FILE);
            currentState.setLastSaveTime(System.currentTimeMillis());

            objectMapper.writeValue(saveFile, currentState);
            System.out.println("Game saved successfully (JSON) to: " + SAVE_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * โหลด GameState จากไฟล์ JSON (savegame.dat)
     *
     * @throws IOException ถ้าไฟล์ไม่มี, ว่างเปล่า หรืออ่าน JSON ไม่ได้
     */
    public GameState loadSavedState() throws IOException {
        File saveFile = new File(SAVE_FILE);

        if (!saveFile.exists()) {
            throw new FileNotFoundException("No saved game found at " + SAVE_FILE);
        }
        if (saveFile.length() == 0) {
            throw new IOException("Save file is empty");
        }

        try {
            GameState loadedState = objectMapper.readValue(saveFile, GameState.class);
            this.currentState = loadedState;
            System.out.println("Game loaded successfully (JSON) from: " + SAVE_FILE);
            return loadedState;

        } catch (IOException e) {
            throw new IOException("Save file is corrupted or invalid JSON: " + e.getMessage(), e);
        }
    }

    public boolean hasSavedGame() {
        return Files.exists(Paths.get(SAVE_FILE));
    }

    public void deleteSavedGame() {
        try {
            Files.deleteIfExists(Paths.get(SAVE_FILE));
            System.out.println("Deleted save file: " + SAVE_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
