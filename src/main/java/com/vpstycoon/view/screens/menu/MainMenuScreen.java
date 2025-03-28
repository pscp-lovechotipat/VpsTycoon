package com.vpstycoon.view.screens.menu;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.service.ResourceManager;
import com.vpstycoon.view.base.GameScreen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * หน้าจอเมนูหลักของเกม
 */
public class MainMenuScreen extends GameScreen {
    
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final INavigator navigator;
    private final AudioManager audioManager;
    
    /**
     * สร้าง MainMenuScreen
     */
    public MainMenuScreen(GameConfig config, ScreenManager screenManager, INavigator navigator) {
        this.config = config;
        this.screenManager = screenManager;
        this.navigator = navigator;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        createUI();
    }
    
    // สร้าง UI สำหรับหน้าจอ
    private void createUI() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #1E1E1E;");
        
        // โลโก้
        Label titleLabel = new Label("วีพีเอส ไทคูน");
        titleLabel.setFont(new Font("System", 48));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        
        // ปุ่มเมนู
        Button playButton = createMenuButton("เริ่มเกมใหม่", event -> {
            audioManager.playSoundEffect("click.mp3");
            navigator.navigateToGame();
        });
        
        Button continueButton = createMenuButton("เล่นต่อ", event -> {
            audioManager.playSoundEffect("click.mp3");
            navigator.navigateToGame();
        });
        
        Button settingsButton = createMenuButton("ตั้งค่า", event -> {
            audioManager.playSoundEffect("click.mp3");
            navigator.navigateToSettings();
        });
        
        Button exitButton = createMenuButton("ออกจากเกม", event -> {
            audioManager.playSoundEffect("click.mp3");
            System.exit(0);
        });
        
        // เพิ่มปุ่มเข้าไปใน VBox
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getChildren().addAll(titleLabel, playButton, continueButton, settingsButton, exitButton);
        menuBox.setPadding(new Insets(50));
        
        rootPane.setCenter(menuBox);
        
        // ข้อความด้านล่าง
        Label versionLabel = new Label("เวอร์ชัน 1.0");
        versionLabel.setTextFill(Color.GRAY);
        BorderPane.setAlignment(versionLabel, Pos.CENTER);
        BorderPane.setMargin(versionLabel, new Insets(20));
        rootPane.setBottom(versionLabel);
        
        root = rootPane;
    }
    
    // สร้างปุ่มเมนู
    private Button createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setMinWidth(200);
        button.setPrefHeight(40);
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px;");
        button.setOnAction(handler);
        return button;
    }

    @Override
    public void onShow() {
        // เล่นเพลงประกอบเมนูหลัก
        audioManager.playMusic("menu_music.mp3");
    }

    @Override
    public void onHide() {
        // ทำความสะอาดทรัพยากร
    }

    @Override
    public void onResize(double width, double height) {
        // ปรับขนาดตามหน้าจอ
    }
} 