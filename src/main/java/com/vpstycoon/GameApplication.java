package com.vpstycoon;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.screen.JavaFXScreenManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.SceneController;
import com.vpstycoon.ui.game.GameplayScreen;
import com.vpstycoon.ui.menu.MainMenuScreen;
import com.vpstycoon.ui.menu.PlayMenuScreen;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.ui.screen.GameScreen;
import com.vpstycoon.ui.settings.SettingScreenInGame;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApplication extends Application implements Navigator {
    private Stage primaryStage;
    private GameConfig gameConfig;
    private ScreenManager screenManager;
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private SettingScreenInGame inGameSettingScreen;
    private GameplayScreen gameplayScreen;
    private GameSaveManager saveManager;
    private GameManager gameManager;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Debug: Starting GameApplication");
        // Get basic stage properties
        System.out.println("Stage Details:");
        System.out.println("Width: " + primaryStage.getWidth());
        System.out.println("Height: " + primaryStage.getHeight());
        System.out.println("X Position: " + primaryStage.getX());
        System.out.println("Y Position: " + primaryStage.getY());
        System.out.println("Is Showing: " + primaryStage.isShowing());
        System.out.println("Is Resizable: " + primaryStage.isResizable());

        // Get scene information
        if (primaryStage.getScene() != null) {
            System.out.println("\nScene Details:");
            System.out.println("Scene Width: " + primaryStage.getScene().getWidth());
            System.out.println("Scene Height: " + primaryStage.getScene().getHeight());
            System.out.println("Root Node: " + primaryStage.getScene().getRoot().getClass().getSimpleName());
        }

        // Get window properties
        System.out.println("\nWindow Properties:");
        System.out.println("Title: " + primaryStage.getTitle());
        System.out.println("Style: " + primaryStage.getStyle());

        // Load font test
        System.out.println("Font Loader: " + FontLoader.TITLE_FONT);

        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);
        this.gameManager = GameManager.getInstance();
        
        initializeGame();
        showMainMenu();

        // Starting Window
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
        inGameSettingScreen = new SettingScreenInGame(gameConfig, screenManager, this);
    }

    @Override
    public void showMainMenu() {
        mainMenuScreen.show();
    }

    @Override
    public void showSettings() {
        settingsScreen.show();
    }

    public void showPlayMenu() {
        showMainMenu();
    }

    @Override
    public void startNewGame() {
        GameState newState = new GameState();
        ResourceManager.getInstance().saveGameState(newState);
        gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, newState);
        gameplayScreen.show();
    }

    @Override
    public void continueGame() {
        if (ResourceManager.getInstance().hasSaveFile()) {
            try {
                GameState savedState = ResourceManager.getInstance().loadGameState();
                gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, savedState);
                gameplayScreen.show();
            } catch (Exception e) {
                showAlert("Error", "Could not load saved game: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("No saved game", "There is no saved game to continue.");
        }
    }

    @Override
    public void showLoadGame() {
        try {
            if (!ResourceManager.getInstance().hasSaveFile()) {
                showAlert("No saved game found", "There is no saved game to continue.");
                return;
            }
            GameState savedState = ResourceManager.getInstance().loadGameState();
            gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, savedState);
            gameplayScreen.show();
        } catch (Exception e) {
            showAlert("Error", "Could not load saved game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void showInGameSettings() {
        inGameSettingScreen.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadGameState(GameState state) {
        gameManager.loadState();
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