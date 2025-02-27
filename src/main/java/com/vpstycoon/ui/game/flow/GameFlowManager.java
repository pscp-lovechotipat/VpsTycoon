package com.vpstycoon.ui.game.flow;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;

import java.util.List;

public class GameFlowManager {
    private final GameSaveManager saveManager;
    private final List<GameObject> gameObjects;

    public GameFlowManager(GameSaveManager saveManager, List<GameObject> gameObjects) {
        this.saveManager = saveManager;
        this.gameObjects = gameObjects;
    }

    public void saveGame() {
        GameState state = new GameState(gameObjects);
        saveManager.saveGame(state);
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

