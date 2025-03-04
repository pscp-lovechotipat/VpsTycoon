package com.vpstycoon.ui.game.flow;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class GameFlowManager {
    private final GameSaveManager saveManager;
    private final List<GameObject> gameObjects;

    public GameFlowManager(GameSaveManager saveManager, List<GameObject> gameObjects) {
        this.saveManager = saveManager;
        this.gameObjects = gameObjects;
    }

    public void saveGame() {
        Company company = ResourceManager.getInstance().getCompany(); // ดึง Company จาก ResourceManager
        GameState state = new GameState(company, gameObjects); // สร้าง GameState ใหม่
        ResourceManager.getInstance().saveGameState(state); // บันทึกด้วย ResourceManager
    }

    public void stopAllGameObjects() {
        if (gameObjects != null) {
            for (GameObject obj : gameObjects) {
                obj.stop();  // ต้องมีเมธอด stop() ใน GameObject
            }
            gameObjects.clear();
        }
    }
}

