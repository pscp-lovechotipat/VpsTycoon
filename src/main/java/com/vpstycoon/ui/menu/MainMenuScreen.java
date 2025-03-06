package com.vpstycoon.ui.menu;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainMenuScreen extends GameScreen {
    private static final double BUTTON_WIDTH = 160;
    private static final double BUTTON_HEIGHT = 40;
    private SettingsScreen settingsScreen;
    private final Navigator navigator;
    private final GameSaveManager saveManager;

    public MainMenuScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
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
                         """); // ดาร์คบลู

        // Enforce resolution
        enforceResolution(root);

        // Logo or Title
        Label titleLabel = new Label("VPS Tycoon");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Continue Button (แสดงเฉพาะเมื่อมีเซฟเกม)
        if (saveManager.saveExists()) {
            MenuButton continueButton = new MenuButton(MenuButtonType.CONTINUE);
            continueButton.setOnAction(e -> navigator.showLoadGame());
            root.getChildren().add(continueButton);
        }

        // New Game Button
        MenuButton newGameButton = new MenuButton(MenuButtonType.NEW_GAME);
        newGameButton.setOnAction(e -> navigator.startNewGame());

        // Settings Button
        MenuButton settingsButton = new MenuButton(MenuButtonType.SETTINGS);
        settingsButton.setOnAction(e -> navigator.showSettings());

        // Quit Button
        MenuButton quitButton = new MenuButton(MenuButtonType.QUIT);
        quitButton.setOnAction(e -> Platform.exit());

        // Delete Game Button
        MenuButton deleteButton = new MenuButton(MenuButtonType.DELETEGAME);
        deleteButton.setOnAction(e -> {
            GameSaveManager save = new GameSaveManager();
            save.deleteGame();
            // รีเฟรชหน้าเพื่อลบปุ่ม Continue
            screenManager.switchScreen(createContent());
        });

        root.getChildren().addAll(newGameButton, settingsButton, quitButton, deleteButton);
        
        return root;
    }
} 