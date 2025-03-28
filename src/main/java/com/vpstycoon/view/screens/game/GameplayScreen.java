package com.vpstycoon.view.screens.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.service.ResourceManager;
import com.vpstycoon.view.base.GameScreen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * หน้าจอหลักของเกม
 */
public class GameplayScreen extends GameScreen {
    
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final INavigator navigator;
    private final AudioManager audioManager;
    private final Company company;
    
    /**
     * สร้าง GameplayScreen
     */
    public GameplayScreen(GameConfig config, ScreenManager screenManager, INavigator navigator) {
        this.config = config;
        this.screenManager = screenManager;
        this.navigator = navigator;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        this.company = ResourceManager.getInstance().getCompany();
        
        createUI();
    }
    
    // สร้าง UI สำหรับหน้าจอ
    private void createUI() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #2C3E50;");
        
        // แถบสถานะด้านบน
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(10));
        statusBar.setStyle("-fx-background-color: #34495E;");
        
        Label companyLabel = new Label("บริษัท: " + company.getName());
        companyLabel.setTextFill(Color.WHITE);
        
        Label balanceLabel = new Label("เงิน: $" + company.getMoney());
        balanceLabel.setTextFill(Color.WHITE);
        
        Label dateLabel = new Label("วันที่: 1 ม.ค. 2023");
        dateLabel.setTextFill(Color.WHITE);
        
        statusBar.getChildren().addAll(companyLabel, balanceLabel, dateLabel);
        rootPane.setTop(statusBar);
        
        // เนื้อหาหลัก
        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #2C3E50;");
        
        Label placeholderLabel = new Label("นี่คือพื้นที่เล่นเกมหลัก");
        placeholderLabel.setTextFill(Color.WHITE);
        
        contentPane.getChildren().add(placeholderLabel);
        rootPane.setCenter(contentPane);
        
        // เมนูด้านล่าง
        HBox bottomMenu = new HBox(10);
        bottomMenu.setPadding(new Insets(10));
        bottomMenu.setAlignment(Pos.CENTER);
        bottomMenu.setStyle("-fx-background-color: #34495E;");
        
        Button menuButton = new Button("เมนู");
        menuButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.mp3");
            showGameMenu();
        });
        
        Button saveButton = new Button("บันทึก");
        saveButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.mp3");
            saveGame();
        });
        
        Button exitButton = new Button("ออก");
        exitButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.mp3");
            navigator.saveAndExitToMainMenu();
        });
        
        bottomMenu.getChildren().addAll(menuButton, saveButton, exitButton);
        rootPane.setBottom(bottomMenu);
        
        root = rootPane;
    }
    
    // แสดงเมนูเกม
    private void showGameMenu() {
        // สร้าง popup menu
        VBox menuBox = new VBox(10);
        menuBox.setStyle("-fx-background-color: rgba(52, 73, 94, 0.9); -fx-padding: 20;");
        menuBox.setMaxWidth(300);
        menuBox.setMaxHeight(400);
        
        Label titleLabel = new Label("เมนูเกม");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 24;");
        
        Button resumeButton = new Button("เล่นต่อ");
        Button saveButton = new Button("บันทึกเกม");
        Button loadButton = new Button("โหลดเกม");
        Button settingsButton = new Button("ตั้งค่า");
        Button exitButton = new Button("ออกสู่เมนูหลัก");
        
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getChildren().addAll(
                titleLabel, resumeButton, saveButton, loadButton, settingsButton, exitButton);
        
        // สร้าง overlay สำหรับเมนู
        StackPane overlay = new StackPane(menuBox);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        // แสดง overlay บน root
        ((BorderPane) root).setCenter(overlay);
        
        // เพิ่ม event handlers
        resumeButton.setOnAction(e -> ((BorderPane) root).setCenter(new StackPane(new Label("กลับสู่เกม"))));
        exitButton.setOnAction(e -> navigator.navigateToMainMenu());
    }
    
    // บันทึกเกม
    private void saveGame() {
        // จะถูก implement ต่อไป
        System.out.println("บันทึกเกม...");
    }

    @Override
    public void onShow() {
        // เล่นเพลงประกอบเกม
        audioManager.playMusic("gameplay_music.mp3");
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