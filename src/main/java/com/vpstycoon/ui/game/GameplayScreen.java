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
import com.vpstycoon.game.thread.GameTimeUpdater;
import com.vpstycoon.game.thread.RequestGenerator;
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
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = new Company();
        this.chatSystem = new ChatSystem();
        this.requestManager = new RequestManager(company);
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

        AudioManager.getInstance().playMusic("Pixel Paradise2.mp3");

        loadGame(); // Load existing game or initialize a new one
    }

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator, GameState gameState) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.state = gameState;
        this.gameObjects = new ArrayList<>(gameState.getGameObjects());
        this.company = gameState.getCompany() != null ? gameState.getCompany() : new Company();
        this.chatSystem = new ChatSystem();
        this.requestManager = new RequestManager(company);
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

        AudioManager.getInstance().playMusic("Pixel Paradise2.mp3");

        loadGame(gameState); // Set up game state from provided GameState
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            state = saveManager.loadGame();
            if (state != null && state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                this.gameObjects = new ArrayList<>(state.getGameObjects());
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
        if (gameState != null && gameState.getGameObjects() != null && !gameState.getGameObjects().isEmpty()) {
            this.state = gameState;
            this.gameObjects = new ArrayList<>(gameState.getGameObjects());
            System.out.println("Loaded game state: " + gameState.toString());
        } else {
            System.out.println("Provided game state is invalid, initializing new game.");
            this.state = new GameState();
            initializeGameObjects();
        }
    }

    private synchronized void initializeGameObjects() {
        int screenWidth = config.getResolution().getWidth();
        int screenHeight = config.getResolution().getHeight();
        int centerX = screenWidth / 2;
        int topMargin = screenHeight / 12; // ระยะห่างจากขอบบน
        int spacing = screenWidth / 10; // ระยะห่างระหว่างปุ่ม

        gameObjects.clear();

        // ปุ่ม Deploy
        RackObject deploy = new RackObject("deploy", "Deploy", 0, 0);
        deploy.setGridPosition(centerX - spacing * 2, topMargin);
        gameObjects.add(deploy);

        // ปุ่ม Network
        RackObject network = new RackObject("network", "Network", 0, 0);
        network.setGridPosition(centerX - spacing, topMargin);
        gameObjects.add(network);

        // ปุ่ม Security
        RackObject security = new RackObject("security", "Security", 0, 0);
        security.setGridPosition(centerX, topMargin);
        gameObjects.add(security);

        // ปุ่ม Marketing
        RackObject marketing = new RackObject("marketing", "Marketing", 0, 0);
        marketing.setGridPosition(centerX + spacing, topMargin);
        gameObjects.add(marketing);

        // อัปเดตสถานะเกม
        state.setGameObjects(gameObjects);

        // Start separate threads
        RequestGenerator requestGenerator = new RequestGenerator(requestManager);
        GameTimeUpdater gameTimeUpdater = new GameTimeUpdater();

        requestGenerator.start();
        gameTimeUpdater.start();
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
                this.company  // Added Company argument
        );
    }
}