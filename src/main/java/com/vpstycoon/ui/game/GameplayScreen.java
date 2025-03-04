package com.vpstycoon.ui.game;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.*;
import com.vpstycoon.game.object.VPSObject;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
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
        this.requestManager = new RequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

        loadGame(); // Load existing game or initialize a new one
    }

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator, GameState gameState) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.state = gameState;
        this.gameObjects = new ArrayList<>(gameState.getGameObjects());
        this.company = new Company(); // Adjust if GameState provides company data
        this.chatSystem = new ChatSystem();
        this.requestManager = new RequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();

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
        gameObjects.clear();

        // Create circular button for Server
        VPSObject server = new VPSObject("server", "Server", 0, 0);
        server.setGridPosition(4, 8);
        gameObjects.add(server);

        // Create circular button for Database
        VPSObject database = new VPSObject("database", "Database", 0, 0);
        database.setGridPosition(8, 8);
        gameObjects.add(database);

        // Create circular button for Network
        VPSObject network = new VPSObject("network", "Network", 0, 0);
        network.setGridPosition(-4, 8);
        gameObjects.add(network);

        // Create circular button for Security
        VPSObject security = new VPSObject("security", "Security", 0, 0);
        security.setGridPosition(-8, 8);
        gameObjects.add(security);

        // Create circular button for Marketing
        VPSObject marketing = new VPSObject("marketing", "Marketing", 0, 0);
        marketing.setGridPosition(2, 8);
        gameObjects.add(marketing);

        // Update state with initialized game objects
        state.setGameObjects(gameObjects);

        // Start separate threads
        RequestGenerator requestGenerator = new RequestGenerator(requestManager);
        GameTimeUpdater gameTimeUpdater = new GameTimeUpdater(state);

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
                this.debugOverlayManager
        );
    }
}