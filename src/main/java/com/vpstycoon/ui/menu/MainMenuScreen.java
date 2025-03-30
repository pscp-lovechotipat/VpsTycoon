package com.vpstycoon.ui.menu;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuScreen extends GameScreen {
    private SettingsScreen settingsScreen;
    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private AudioManager audioManager;

    public MainMenuScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.audioManager = ResourceManager.getInstance().getAudioManager();
    }

    public void setSettingsScreen(SettingsScreen settingsScreen) {
        this.settingsScreen = settingsScreen;
    }

    @Override
    protected Region createContent() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        
        enforceResolution(root);
        
        Pane logo = new Pane();
        logo.setPrefWidth(400);
        logo.setPrefHeight(200);
        logo.setTranslateX(100);
        logo.setStyle("""
                -fx-background-image: url("/images/home/logo.gif");
                -fx-background-position: center;
                -fx-background-size: contain;
                -fx-background-repeat: no-repeat;
                -fx-alignment: center;
                """);
        logo.setScaleX(3);
        logo.setScaleY(3);
        
        MenuButton newGameButton = new MenuButton(MenuButtonType.NEW_GAME);
        newGameButton.setOnAction(e -> {
            navigator.startNewGame();
            audioManager.playSoundEffect("click.wav");
        });
        
        MenuButton settingsButton = new MenuButton(MenuButtonType.SETTINGS);
        settingsButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            navigator.showSettings();
        });
        
        MenuButton quitButton = new MenuButton(MenuButtonType.QUIT);
        quitButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            Platform.exit();
        });
        
        MenuButton deleteButton = new MenuButton(MenuButtonType.DELETEGAME);
        deleteButton.setOnAction(e -> {
            GameSaveManager save = new GameSaveManager();
            save.deleteGame();
            screenManager.switchScreen(createContent());
        });
        
        root.getChildren().add(logo);
        
        if (saveManager.saveExists()) {
            MenuButton continueButton = new MenuButton(MenuButtonType.CONTINUE);
            continueButton.setOnAction(e -> {
                navigator.showLoadGame();
                audioManager.playSoundEffect("click.wav");
            });
            root.getChildren().add(continueButton);
        }
        
        root.getChildren().addAll(newGameButton, settingsButton, quitButton, deleteButton);
        
        Pane itSleepImage = new Pane();
        itSleepImage.setPrefWidth(400);
        itSleepImage.setPrefHeight(400);
        itSleepImage.setScaleX(0.7);
        itSleepImage.setScaleY(0.7);
        
        itSleepImage.setStyle("""
            -fx-background-image: url('/images/home/itstudent.gif');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """);
        
        StackPane stackPane = new StackPane();
        stackPane.setStyle("""
            -fx-background-color: #2C3E50;
            -fx-background-image: url("/images/home/background.png");
            -fx-background-size: contain;
            -fx-background-position: center;
            """);
        
        stackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                itSleepImage.prefWidthProperty().bind(newScene.widthProperty().multiply(0.30));
                itSleepImage.prefHeightProperty().bind(newScene.heightProperty().multiply(0.30));
                
                double referenceWidth = 1280.0;
                double referenceHeight = 720.0;
                
                itSleepImage.translateXProperty().bind(
                    newScene.widthProperty().multiply(392 / referenceWidth)
                );
                
                itSleepImage.translateYProperty().bind(
                    newScene.heightProperty().multiply(114 / referenceHeight)
                );
            }
        });
        
        stackPane.getChildren().add(itSleepImage);
        StackPane.setAlignment(itSleepImage, Pos.TOP_LEFT);
        stackPane.getChildren().add(root);
        
        return stackPane;
    }
} 
