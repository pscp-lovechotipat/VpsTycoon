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
    private PlayMenuScreen playMenuScreen;
    private final Navigator navigator;

    public MainMenuScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
    }

    public void setSettingsScreen(SettingsScreen settingsScreen) {
        this.settingsScreen = settingsScreen;
    }

    public void setPlayMenuScreen(PlayMenuScreen playMenuScreen) {
        this.playMenuScreen = playMenuScreen;
    }

    @Override
    protected Region createContent() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2C3E50;"); // ดาร์คบลู

        // Enforce resolution
        enforceResolution(root);

        // Logo or Title
        Label titleLabel = new Label("VPS Tycoon");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Play Button
        MenuButton playButton = new MenuButton(MenuButtonType.PLAY);
        playButton.setOnAction(e -> navigator.showPlayMenu());

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
        });

        root.getChildren().addAll(titleLabel, playButton, settingsButton, quitButton, deleteButton);
        
        return root;
    }
} 