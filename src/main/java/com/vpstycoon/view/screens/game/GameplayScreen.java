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

import com.vpstycoon.audio.interfaces.IAudioManager;
import com.vpstycoon.model.company.interfaces.ICompany;

public class GameplayScreen extends GameScreen {
    
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final INavigator navigator;
    private final IAudioManager audioManager;
    private final ICompany company;
    
    
    public GameplayScreen(GameConfig config, ScreenManager screenManager, INavigator navigator) {
        this.config = config;
        this.screenManager = screenManager;
        this.navigator = navigator;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        this.company = ResourceManager.getInstance().getCompany();
        
        createUI();
    }
    
    
    private void createUI() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #2C3E50;");
        
        
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
        
        
        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: #2C3E50;");
        
        Label placeholderLabel = new Label("นี่คือพื้นที่เล่นเกมหลัก");
        placeholderLabel.setTextFill(Color.WHITE);
        
        contentPane.getChildren().add(placeholderLabel);
        rootPane.setCenter(contentPane);
        
        
        HBox bottomMenu = new HBox(10);
        bottomMenu.setPadding(new Insets(10));
        bottomMenu.setAlignment(Pos.CENTER);
        bottomMenu.setStyle("-fx-background-color: #34495E;");
        
        Button menuButton = new Button("เมนู");
        menuButton.setOnAction(e -> {
            audioManager.playSound("click.mp3");
            showGameMenu();
        });
        
        Button saveButton = new Button("บันทึก");
        saveButton.setOnAction(e -> {
            audioManager.playSound("click.mp3");
            saveGame();
        });
        
        Button exitButton = new Button("ออก");
        exitButton.setOnAction(e -> {
            audioManager.playSound("click.mp3");
            navigator.saveAndExitToMainMenu();
        });
        
        bottomMenu.getChildren().addAll(menuButton, saveButton, exitButton);
        rootPane.setBottom(bottomMenu);
        
        root = rootPane;
    }
    
    
    private void showGameMenu() {
        
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
        
        
        StackPane overlay = new StackPane(menuBox);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        
        ((BorderPane) root).setCenter(overlay);
        
        
        resumeButton.setOnAction(e -> ((BorderPane) root).setCenter(new StackPane(new Label("กลับสู่เกม"))));
        exitButton.setOnAction(e -> navigator.navigateToMainMenu());
    }
    
    
    private void saveGame() {
        
        System.out.println("บันทึกเกม...");
    }

    @Override
    public void onShow() {
        
        audioManager.playMusic("gameplay_music.mp3");
    }

    @Override
    public void onHide() {
        
    }

    @Override
    public void onResize(double width, double height) {
        
    }
} 
