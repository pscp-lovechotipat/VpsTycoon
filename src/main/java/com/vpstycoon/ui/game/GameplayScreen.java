package com.vpstycoon.ui.game;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameLoop;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.object.VPSObject;
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

/**
 * GameplayScreen หลังแยกโค้ด UI ออกไปเป็น GameplayContentPane
 */
public class GameplayScreen extends GameScreen {
    private GameState state;

    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private ArrayList<GameObject> gameObjects;
    private final Company company;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;

    private GameLoop gameLoop;

    // Manager แยก
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.state = new GameState();

        // เซ็ตอัพตัวแปรต่าง ๆ
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = new Company();

        this.chatSystem = new ChatSystem();
        this.requestManager = new RequestManager();
        this.vpsManager = new VPSManager();

        // สร้าง Manager สำหรับ flow และ debug
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

        loadGame(); // โหลด / ถ้าไม่มี save ก็สร้างใหม่
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            state = saveManager.loadGame();
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

        VPSObject server = new VPSObject("server", "Server", 0, 0);
        server.setGridPosition(8, 8);  // (8,8) จะคูณด้วย CELL_SIZE (64) => (512,512)
        gameObjects.add(server);

        VPSObject database = new VPSObject("database", "Database", 0, 0);
        database.setGridPosition(10, 8);  // ตัวอย่างตำแหน่ง (10,8) => (640,512)
        gameObjects.add(database);

        VPSObject network = new VPSObject("network", "Network", 0, 0);
        network.setGridPosition(12, 8);  // ตัวอย่างตำแหน่ง (12,8) => (768,512)
        gameObjects.add(network);
        // เซฟ initial state
        saveManager.saveGame(state);
        initializeGameLoop(state);
    }

    private void initializeGameLoop(GameState state) {
        this.gameLoop = new GameLoop(state, this.requestManager, this.company);
        this.gameLoop.start();
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
