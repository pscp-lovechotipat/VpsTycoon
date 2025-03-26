package com.vpstycoon.ui.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.object.RackObject;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeController;
import com.vpstycoon.game.thread.RequestGenerator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.scene.layout.Region;

import java.util.ArrayList;

public class GameplayScreen extends GameScreen {
    private GameState state;
    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private ArrayList<GameObject> gameObjects;
    private final Company company;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;
    private GameTimeController gameTimeController;

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = ResourceManager.getInstance().getCompany(); // ใช้ company จาก ResourceManager
        this.chatSystem = new ChatSystem();
        this.requestManager = ResourceManager.getInstance().getRequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

        loadGame();
    }

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator, GameState gameState) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        
        // บันทึก state ที่ได้รับมา
        this.state = gameState;
        
        // ตรวจสอบว่า state มี gameObjects หรือไม่
        if (gameState.getGameObjects() != null) {
            this.gameObjects = new ArrayList<>(gameState.getGameObjects());
            
            // แสดง log จำนวน gameObjects เพื่อตรวจสอบ
            System.out.println("จำนวน GameObjects ที่ได้รับ: " + gameState.getGameObjects().size());
        } else {
            this.gameObjects = new ArrayList<>();
            System.out.println("ไม่มี GameObjects ใน GameState ที่ได้รับ");
        }
        
        // ใช้ company จาก GameState ไม่ใช่ ResourceManager
        if (gameState.getCompany() != null) {
            this.company = gameState.getCompany();
            // อัพเดต company ใน ResourceManager ด้วย
            ResourceManager.getInstance().setCompany(this.company);
            System.out.println("ตั้งค่า Company จาก GameState: Money = $" + this.company.getMoney());
        } else {
            this.company = ResourceManager.getInstance().getCompany();
            System.out.println("ใช้ค่า Company จาก ResourceManager: Money = $" + this.company.getMoney());
        }
        
        this.chatSystem = new ChatSystem();
        this.requestManager = ResourceManager.getInstance().getRequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

        // โหลดเกมโดยใช้ GameState ที่ได้รับมา
        loadGame(gameState);
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            state = saveManager.loadGame();
            if (state != null && state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                this.gameObjects = new ArrayList<>(state.getGameObjects());
                System.out.println("โหลดเกมสำเร็จ มี GameObjects จำนวน: " + state.getGameObjects().size());
            } else {
                System.out.println("Load failed or no objects found, initializing new game.");
                initializeGameObjects();
            }
        } else {
            System.out.println("No save file found, initializing new game.");
            state = new GameState();
            initializeGameObjects();
        }
    }

    private void loadGame(GameState gameState) {
        if (gameState != null) {
            this.state = gameState;
            
            if (gameState.getGameObjects() != null && !gameState.getGameObjects().isEmpty()) {
                this.gameObjects = new ArrayList<>(gameState.getGameObjects());
                System.out.println("โหลดเกมสำเร็จจาก GameState ที่ได้รับ มี GameObjects จำนวน: " + gameState.getGameObjects().size());
                
                // ตรวจสอบและปริ้นท์ข้อมูลเงินเพื่อเช็คว่าโหลดถูกต้อง
                if (gameState.getCompany() != null) {
                    System.out.println("โหลด Company พร้อมเงิน: $" + gameState.getCompany().getMoney());
                }
            } else {
                System.out.println("GameState ไม่มี GameObjects, เริ่มเกมใหม่");
                initializeGameObjects();
            }
            
            // ถ้านี่เป็นการเริ่มเกมใหม่ (New Game) และไม่ใช่การ Continue
            if (gameState.getCompany() != null && gameState.getCompany().getMoney() == 10000 && 
                gameState.getLocalDateTime() != null && 
                gameState.getLocalDateTime().getYear() == 2000 &&
                gameState.getLocalDateTime().getMonthValue() == 1 &&
                gameState.getLocalDateTime().getDayOfMonth() == 1) {
                
                System.out.println("เริ่มเกมใหม่ ไม่ต้องบันทึกทันที");
                // ไม่ต้องบันทึกทันที
            } else {
                // Save the loaded game state to ensure continuity
                System.out.println("บันทึกเกมหลังจากโหลด เพื่อให้แน่ใจว่าข้อมูลต่อเนื่อง");
                gameFlowManager.saveGame();
            }
        } else {
            System.out.println("Provided game state is null, initializing new game.");
            this.state = new GameState();
            initializeGameObjects();
        }
    }

    private synchronized void initializeGameObjects() {
        int screenWidth = config.getResolution().getWidth();
        int screenHeight = config.getResolution().getHeight();
        int centerX = screenWidth / 2;
        int topMargin = screenHeight / 12;
        int spacing = screenWidth / 10;

        gameObjects.clear();

        RackObject deploy = new RackObject("deploy", "Deploy", 0, 0);
        deploy.setGridPosition(centerX - spacing * 2, topMargin);
        gameObjects.add(deploy);

        RackObject network = new RackObject("network", "Network", 0, 0);
        network.setGridPosition(centerX - spacing, topMargin);
        gameObjects.add(network);

        RackObject security = new RackObject("security", "Security", 0, 0);
        security.setGridPosition(centerX, topMargin);
        gameObjects.add(security);

        RackObject marketing = new RackObject("marketing", "Marketing", 0, 0);
        marketing.setGridPosition(centerX + spacing, topMargin);
        gameObjects.add(marketing);

        state.setGameObjects(gameObjects);

        RequestGenerator requestGenerator = new RequestGenerator(requestManager);

        requestGenerator.start();
        gameTimeController = ResourceManager.getInstance().getGameTimeController();
        gameTimeController.startTime();
    }

    @Override
    protected Region createContent() {
        return new GameplayContentPane(
                this.gameObjects,
                this.navigator,
                this.chatSystem,
                this.requestManager,
                this.vpsManager,
                this.gameFlowManager,
                this.debugOverlayManager,
                this.company,
                ResourceManager.getInstance().getRack()
        );
    }
}