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
        File saveFile = new File(SAVE_FILE);

        // ตรวจสอบไฟล์อย่างละเอียด
        if (!saveFile.exists()) {
            throw new FileNotFoundException("No saved game found at " + SAVE_FILE);
        }
        if (saveFile.length() == 0) {
            throw new IOException("Save file is empty");
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(SAVE_FILE)))) {
            Object obj = ois.readObject();
            if (!(obj instanceof GameState)) {
                throw new ClassNotFoundException("Invalid save data: not a GameState object");
            }

            GameState savedState = (GameState) obj;
            this.currentState = savedState;
            System.out.println("Game loaded successfully");
            return savedState;
        } catch (StreamCorruptedException e) {
            throw new IOException("Save file is corrupted: " + e.getMessage());
        } catch (InvalidClassException e) {
            throw new ClassNotFoundException("Save file contains incompatible class version: " + e.getMessage());
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