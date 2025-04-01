package com.vpstycoon.ui.game.flow;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;

import java.util.List;

public class GameFlowManager {
    private final GameSaveManager saveManager;
    private final List<GameObject> gameObjects;

    public GameFlowManager(GameSaveManager saveManager, List<GameObject> gameObjects) {
        this.saveManager = saveManager;
        this.gameObjects = gameObjects;
    }

    public void saveGame() {
        
        Company company = ResourceManager.getInstance().getCompany(); 
        
        if (company != null) {
            System.out.println("กำลังบันทึกเกม... เงินปัจจุบัน: $" + company.getMoney());
            
            
            GameState state = new GameState(company, gameObjects);
            
            
            if (ResourceManager.getInstance().getGameTimeController() != null) {
                state.setLocalDateTime(ResourceManager.getInstance().getGameTimeController().getGameTimeManager().getGameDateTime());
            }
            
            
            ResourceManager.getInstance().saveGameState(state);
            
            System.out.println("บันทึกเกมสำเร็จ! จำนวน GameObject: " + (gameObjects != null ? gameObjects.size() : 0));
        } else {
            System.err.println("ไม่สามารถบันทึกเกมได้: ไม่พบข้อมูล Company");
        }
    }

    public void stopAllGameObjects() {
        if (gameObjects != null) {
            for (GameObject obj : gameObjects) {
                obj.stop();  
            }
            gameObjects.clear();
        }
    }
}



