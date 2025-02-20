package com.vpstycoon;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import java.util.Optional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.JavaFXScreenManager;
import com.vpstycoon.ui.settings.SettingsScreen;
import com.vpstycoon.ui.menu.MainMenuScreen;
import com.vpstycoon.ui.menu.PlayMenuScreen;
import com.vpstycoon.ui.game.GameplayScreen;
import com.vpstycoon.ui.SceneController;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.ui.screen.GameScreen;

public class GameApplication extends Application implements Navigator {
    private Stage primaryStage;
    private GameConfig gameConfig;
    private ScreenManager screenManager;
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private PlayMenuScreen playMenuScreen;
    private GameplayScreen gameplayScreen;
    private GameSaveManager saveManager;
    private GameManager gameManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);
        this.gameManager = GameManager.getInstance();
        
        initializeGame();
        showMainMenu();
        primaryStage.show();
    }

    private GameConfig createGameConfig() {
        return DefaultGameConfig.getInstance();
    }

    private void initializeGame() {
        // Load configuration
        gameConfig.load();
        
        // Initialize managers
        saveManager = new GameSaveManager();
        
        // Initialize scene controller
        SceneController.initialize(primaryStage, gameConfig, screenManager);
        
        // Create screens
        createScreens();
        
        // Setup primary stage
        primaryStage.setTitle("VPS Tycoon");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> shutdown());
        
        // Subscribe to settings changes
        GameEventBus.getInstance().subscribe(
            SettingsChangedEvent.class,
            event -> Platform.runLater(() -> 
                SceneController.getInstance().updateResolution()
            )
        );
    }

    private void createScreens() {
        mainMenuScreen = new MainMenuScreen(gameConfig, screenManager, this);
        settingsScreen = new SettingsScreen(gameConfig, screenManager, this);
        playMenuScreen = new PlayMenuScreen(this);
    }

    @Override
    public void showMainMenu() {
        mainMenuScreen.show();
    }

    @Override
    public void showSettings() {
        settingsScreen.show();
    }

    @Override
    public void showPlayMenu() {
        if (playMenuScreen == null) {
            playMenuScreen = new PlayMenuScreen(this);
        }
        screenManager.switchScreen(playMenuScreen);
    }

    @Override
    public void startNewGame() {
        // Create gameplay screen with new game state
        gameplayScreen = new GameplayScreen(gameConfig, screenManager, this);
        
        // Create new save file
        GameState newState = new GameState();
        saveManager.saveGame(newState);
        
        // Show gameplay screen
        gameplayScreen.show();
    }

    @Override
    public void continueGame() {
        if (saveManager.saveExists()) {
            // Create gameplay screen and load existing state
            gameplayScreen = new GameplayScreen(gameConfig, screenManager, this);
            gameplayScreen.show();
        }
    }

    @Override
    public void showLoadGame() {
        try {
            if (!gameManager.hasSavedGame()) {
                showAlert("No saved game found", "There is no saved game to continue.");
                return;
            }

            GameState savedState = gameManager.loadSavedState();
            startGame(savedState);
            
        } catch (IOException | ClassNotFoundException e) {
            showAlert("Error", "Could not load saved game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadGameState(GameState state) {
        gameManager.loadState(state);
    }

    private void startGame(GameState state) {
        screenManager.switchScreen(new GameScreen(this, gameConfig, state));
    }

    private void shutdown() {
        try {
            gameConfig.save();
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 