package com.vpstycoon.ui.menu;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.resource.ResourceManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
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
        Button playButton = createMenuButton("Play");
        playButton.setOnAction(e -> navigator.showPlayMenu());

        // Settings Button
        Button settingsButton = createMenuButton("Settings");
        settingsButton.setOnAction(e -> navigator.showSettings());

        // Quit Button
        Button quitButton = createMenuButton("Quit");
        quitButton.setOnAction(e -> Platform.exit());

        root.getChildren().addAll(titleLabel, playButton, settingsButton, quitButton);
        
        return root;
    }

    private Button createMenuButton(String text) {
        Button button = new Button();
        button.setPrefWidth(BUTTON_WIDTH);
        button.setPrefHeight(BUTTON_HEIGHT);
        
        // โหลด GIF สำหรับปุ่ม
        String gifPath = "/images/buttons/" + text.toLowerCase() + ".gif";
        URL gifUrl = ResourceManager.getResource(gifPath);
        
        if (gifUrl == null) {
            System.err.println("GIF resource not found for button: " + text);
            return createFallbackButton(text);
        }

        // สร้าง ImageView สำหรับ GIF
        ImageView imageView = new ImageView(new Image(gifUrl.toExternalForm()));
        
        // ตั้งค่าขนาดของ ImageView
        imageView.setFitWidth(BUTTON_WIDTH);
        imageView.setFitHeight(BUTTON_HEIGHT);
        imageView.setPreserveRatio(true);
        
        // ตั้งค่า graphic
        button.setGraphic(imageView);
        
        // สไตล์พื้นฐานของปุ่ม
        button.setStyle("""
            -fx-background-color: transparent;
            -fx-background-radius: 0;
            -fx-border-color: transparent;
            -fx-border-width: 2;
            -fx-padding: 0;
            """);
        
        // เพิ่ม hover effect ด้วย stroke
        button.setOnMouseEntered(e -> 
            button.setEffect(neon())
        );
        
        button.setOnMouseExited(e -> 
            button.setEffect(null)
        );
        
        return button;
    }

    private Button createFallbackButton(String text) {
        // ใช้ปุ่มแบบเดิมเป็น fallback
        Button button = new Button(text);
        button.setPrefWidth(BUTTON_WIDTH);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setStyle("""
            -fx-background-color: #3498DB;
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-background-radius: 5;
            """);
        
        button.setOnMouseEntered(e -> 
            button.setStyle(button.getStyle() + "-fx-background-color: #2980B9;")
        );
        button.setOnMouseExited(e -> 
            button.setStyle(button.getStyle() + "-fx-background-color: #3498DB;")
        );
        
        return button;
    }

    private Effect neon() {
        Glow glow = new Glow(1);
        DropShadow neonShadow = new DropShadow(20, Color.rgb(145, 0, 255, 0.6));
        ColorAdjust colorAdjust = new ColorAdjust();

        neonShadow.setSpread(0.2);
        colorAdjust.setBrightness(0.15);
        colorAdjust.setSaturation(0.4);

        glow.setInput(colorAdjust);
        neonShadow.setInput(glow);
        return neonShadow;
    }
} 