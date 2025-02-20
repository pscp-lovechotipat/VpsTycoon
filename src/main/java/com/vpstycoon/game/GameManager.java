package com.vpstycoon.game;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameManager {
    private GameState currentState;
    private static GameManager instance;
    private static final String SAVE_FILE = "savegame.dat";

    private GameManager() {
        currentState = new GameState();
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
            // ใช้ ObjectOutputStream เพื่อเขียนข้อมูลในรูปแบบที่ถูกต้อง
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(SAVE_FILE)))) {
                currentState.setLastSaveTime(System.currentTimeMillis());
                oos.writeObject(currentState);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameState loadSavedState() throws IOException, ClassNotFoundException {
        // ตรวจสอบว่าไฟล์มีอยู่หรือไม่
        if (!Files.exists(Paths.get(SAVE_FILE))) {
            throw new FileNotFoundException("No saved game found");
        }

        // ใช้ ObjectInputStream เพื่ออ่านข้อมูล
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(SAVE_FILE)))) {
            GameState savedState = (GameState) ois.readObject();
            this.currentState = savedState;
            return savedState;
        }
    }

    public boolean hasSavedGame() {
        return Files.exists(Paths.get(SAVE_FILE));
    }

    public void deleteSavedGame() {
        try {
            Files.deleteIfExists(Paths.get(SAVE_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 