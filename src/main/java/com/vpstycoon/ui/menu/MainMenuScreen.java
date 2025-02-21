package com.vpstycoon.ui.menu;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.resource.ResourceManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;

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

        root.getChildren().addAll(titleLabel, playButton, settingsButton, quitButton);
        
        return root;
    }
} 