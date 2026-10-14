package com.vpstycoon.ui.game.flow;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;

import java.util.List;

public class GameFlowManager {
    private final GameSaveManager saveManager;
    private final List<GameObject> gameObjects;

    public GameFlowManager(GameSaveManager saveManager, List<GameObject> gameObjects) {
        this.saveManager = saveManager;
        this.gameObjects = gameObjects;
    }


}



