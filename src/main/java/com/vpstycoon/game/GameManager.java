package com.vpstycoon.game;

import com.vpstycoon.game.resource.ResourceManager;

public class GameManager {
    private static GameManager instance;

    private GameManager() {
        // ไม่ต้องสร้าง GameState หรือ Company เอง เพราะใช้จาก ResourceManager
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void saveState() {
        ResourceManager.getInstance().saveGameState(ResourceManager.getInstance().getCurrentState());
    }

    public void loadState() {
        ResourceManager.getInstance().loadGameState();
        // ไม่ต้องตั้ง currentState อีก เพราะ ResourceManager จัดการแล้ว
    }

    public GameState getCurrentState() {
        return ResourceManager.getInstance().getCurrentState();
    }

    public void deleteSavedGame() {
        ResourceManager.getInstance().deleteSaveFile();
    }

    public boolean hasSavedGame() {
        return ResourceManager.getInstance().hasSaveFile();
    }
}