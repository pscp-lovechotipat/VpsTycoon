package com.vpstycoon.application;

import java.time.LocalDateTime;
import java.util.List;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.JavaFXScreenManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.service.ResourceManager;
import com.vpstycoon.service.interfaces.IResourceManager;
import com.vpstycoon.view.SceneController;
import com.vpstycoon.view.screens.cutscene.CutsceneScreen;
import com.vpstycoon.view.screens.game.GameplayScreen;
import com.vpstycoon.view.screens.menu.MainMenuScreen;
import com.vpstycoon.view.screens.settings.SettingsScreen;
import com.vpstycoon.view.base.GameScreen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


public class GameApplication extends Application implements INavigator, IResourceManager.ResourceLoadingListener {
    
    private Stage primaryStage;
    private GameConfig gameConfig;
    private ScreenManager screenManager;
    private GameScreen mainMenuScreen;
    private GameScreen settingsScreen;
    private GameScreen gameplayScreen;
    private GameScreen cutsceneScreen;
    private GameSaveManager saveManager;
    private GameManager gameManager;
    private AudioManager audioManager;
    private Stage loadingStage;
    private Label loadingDetailsLabel;
    private javafx.scene.control.ProgressIndicator progressIndicator;
    private VBox loadingDetailsPane;
    private Label statusLabel;
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("เริ่มต้น GameApplication");
        
        
        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);
        this.gameManager = GameManager.getInstance();
        
        
        initializeLoadingScreen();
        
        
        Thread initThread = new Thread(() -> {
            try {
                
                ResourceManager resourceManager = ResourceManager.getInstance();
                resourceManager.setResourceLoadingListener(this);
                
                
                resourceManager.preloadAssets();
                
                
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("เริ่มเตรียมเกม กรุณารอสักครู่...");
                    }
                });
                
                
                finishInitialization();
                
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดระหว่างการเริ่มต้นเกม: " + e.getMessage());
                e.printStackTrace();
                
                
                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "เริ่มต้นเกมล้มเหลว: " + e.getMessage());
                    primaryStage.show();
                });
            }
        });
        
        initThread.setDaemon(true);
        initThread.start();
    }
    
    
    private void finishInitialization() {
        try {
            
            initializeGame();
            
            
            Platform.runLater(() -> {
                try {
                    
                    audioManager = ResourceManager.getInstance().getAudioManager();
                    audioManager.playMusic("menu_music.mp3");
                    
                    
                    if (statusLabel != null) {
                        statusLabel.setText("โหลดเสร็จสมบูรณ์ กำลังเตรียมเริ่มเข้าสู่เกม...");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
                    }
                    
                    
                    System.out.println("กำลังแสดงหน้าแรก...");
                    
                    
                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        
                        hideLoadingScreen();
                        primaryStage.show();
                        
                        
                        navigateToCutscene();
                    });
                    pause.play();
                    
                } catch (Exception e) {
                    System.err.println("เกิดข้อผิดพลาดในเธรด JavaFX: " + e.getMessage());
                    e.printStackTrace();
                    hideLoadingScreen();
                    showAlert("Error", "เริ่มต้น UI ของเกมล้มเหลว: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดระหว่างการเริ่มต้นเกม: " + e.getMessage());
            e.printStackTrace();
            
            
            Platform.runLater(() -> {
                hideLoadingScreen();
                showAlert("Error", "เริ่มต้นเกมล้มเหลว: " + e.getMessage());
                primaryStage.show();
            });
        }
    }
    
    
    private void initializeLoadingScreen() {
        Platform.runLater(() -> {
            loadingStage = new Stage();
            loadingStage.setTitle("กำลังโหลด VPS Tycoon");
            
            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: #2C3E50;");
            
            VBox loadingBox = new VBox(20);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(30));
            
            Label titleLabel = new Label("วีพีเอส ไทคูน");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
            
            progressIndicator = new javafx.scene.control.ProgressIndicator();
            progressIndicator.setPrefSize(100, 100);
            
            statusLabel = new Label("กำลังโหลดทรัพยากร...");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
            
            loadingDetailsPane = new VBox(5);
            loadingDetailsPane.setMaxHeight(200);
            
            ScrollPane scrollPane = new ScrollPane(loadingDetailsPane);
            scrollPane.setMaxHeight(200);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            
            loadingDetailsLabel = new Label("");
            loadingDetailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #AAAAAA;");
            loadingDetailsPane.getChildren().add(loadingDetailsLabel);
            
            loadingBox.getChildren().addAll(titleLabel, progressIndicator, statusLabel, scrollPane);
            root.getChildren().add(loadingBox);
            
            Scene scene = new Scene(root, 600, 400);
            loadingStage.setScene(scene);
            loadingStage.show();
        });
    }
    
    
    private void hideLoadingScreen() {
        Platform.runLater(() -> {
            if (loadingStage != null && loadingStage.isShowing()) {
                loadingStage.close();
            }
        });
    }
    
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    private GameConfig createGameConfig() {
        return DefaultGameConfig.getInstance();
    }
    
    
    private void initializeGame() {
        try {
            
            SceneController.initialize(primaryStage, gameConfig, screenManager);
            
            
            createScreens();
            
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการเริ่มต้นเกม: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    
    private void createScreens() {
        try {
            
            if (mainMenuScreen == null) {
                
                
                System.out.println("สร้างหน้าจอต่างๆ...");
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างหน้าจอ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    
    
    
    
    @Override
    public void onResourceLoading(String resourcePath) {
        Platform.runLater(() -> {
            loadingDetailsLabel.setText("กำลังโหลด: " + resourcePath);
        });
    }
    
    @Override
    public void onResourceLoadingComplete(int totalLoaded) {
        Platform.runLater(() -> {
            loadingDetailsLabel.setText("โหลดเสร็จแล้ว " + totalLoaded + " รายการ");
            statusLabel.setText("โหลดทรัพยากรเสร็จแล้ว กำลังเริ่มต้นเกม...");
        });
    }
    
    
    
    
    
    @Override
    public void navigateToMainMenu() {
        if (mainMenuScreen != null) {
            screenManager.switchScreen(mainMenuScreen);
        }
    }

    @Override
    public void navigateToGame() {
        if (gameplayScreen != null) {
            screenManager.switchScreen(gameplayScreen);
        }
    }

    @Override
    public void navigateToSettings() {
        if (settingsScreen != null) {
            screenManager.switchScreen(settingsScreen);
        }
    }

    @Override
    public void navigateToCutscene() {
        if (cutsceneScreen != null) {
            screenManager.switchScreen(cutsceneScreen);
        } else {
            
            navigateToMainMenu();
        }
    }
    
    @Override
    public void saveAndExitToMainMenu() {
        
        
        
        navigateToMainMenu();
    }
} 
