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
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainMenuScreen extends GameScreen {
    private static final double BUTTON_WIDTH = 160;
    private static final double BUTTON_HEIGHT = 40;
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
        root.setStyle("""
                         -fx-background-color: #2C3E50;
                         -fx-background-image: url("/images/wallpaper/Wallpaper.png");
                         -fx-background-size: contain;
                         -fx-background-position: center;
                         """); 

        
        enforceResolution(root);

        
        Label titleLabel = new Label();
        titleLabel.setStyle("""
                            -fx-background-image: url("/images/logo/vps_tycoon_logo.png");
                            -fx-background-position: center;
                            -fx-background-size: contain;
                            -fx-background-repeat: no-repeat;
                            -fx-alignment: center;
                            -fx-pref-width: 300px;
                            -fx-pref-height: 200px;
                            """);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), titleLabel);
        scaleTransition.setFromX(1.0); 
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.1);   
        scaleTransition.setToY(1.1);
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE); 
        scaleTransition.setAutoReverse(true); 
        scaleTransition.play(); 

        
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

        root.getChildren().add(titleLabel);

        
        if (saveManager.saveExists()) {
            MenuButton continueButton = new MenuButton(MenuButtonType.CONTINUE);
            continueButton.setOnAction(e -> {
                navigator.showLoadGame();
                audioManager.playSoundEffect("click.wav");
            });
            root.getChildren().add(continueButton);
        }

        root.getChildren().addAll(newGameButton, settingsButton, quitButton, deleteButton);
        
        return root;
    }
} 
