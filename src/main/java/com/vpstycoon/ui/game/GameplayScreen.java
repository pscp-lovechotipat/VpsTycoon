package com.vpstycoon.ui.game;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.VPSObject;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * GameplayScreen หลังแยกโค้ด UI ออกไปเป็น GameplayContentPane
 */
public class GameplayScreen extends GameScreen {
    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private ArrayList<GameObject> gameObjects;
    private final Company company;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;

    // Manager แยก
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;

        // เซ็ตอัพตัวแปรต่าง ๆ
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = new Company();
        loadGame(); // โหลด / ถ้าไม่มี save ก็สร้างใหม่

        this.chatSystem = new ChatSystem();
        this.requestManager = new RequestManager();
        this.vpsManager = new VPSManager();

        // สร้าง Manager สำหรับ flow และ debug
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            GameState state = saveManager.loadGame();
            if (state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                this.gameObjects = (ArrayList<GameObject>) state.getGameObjects();
            } else {
                initializeGameObjects();
            }
        } else {
            initializeGameObjects();
        }
    }

    private void initializeGameObjects() {
        gameObjects.clear();
        gameObjects.add(new VPSObject("server", "Server", 500, 500));
        gameObjects.add(new VPSObject("database", "Database", 600, 600));
        gameObjects.add(new VPSObject("network", "Network", 700, 700));
        // เซฟ initial state
        GameState state = new GameState(gameObjects);
        saveManager.saveGame(state);
    }

    /**
     * สร้างเนื้อหาเกม โดย return เป็น Region
     * ในที่นี้ เราใช้ GameplayContentPane ที่แยกเป็นคลาส
     */
    @Override
    protected Region createContent() {
        return new GameplayContentPane(
                this.gameObjects,
                this.navigator,
                this.chatSystem,
                this.requestManager,
                this.vpsManager,
                this.gameFlowManager,
                this.debugOverlayManager
        );
    }
}
