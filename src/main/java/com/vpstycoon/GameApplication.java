package com.vpstycoon;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.screen.JavaFXScreenManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.SceneController;
import com.vpstycoon.ui.cutscene.CutsceneScreen;
import com.vpstycoon.ui.game.GameplayScreen;
import com.vpstycoon.ui.menu.MainMenuScreen;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.ui.screen.GameScreen;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.application.Application;
import javafx.application.Platform;
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
import java.time.LocalDateTime;
import java.util.List;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;


public class GameApplication extends Application implements Navigator, ResourceManager.ResourceLoadingListener {
    private Stage primaryStage;
    private GameConfig gameConfig;
    private ScreenManager screenManager;
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private GameplayScreen gameplayScreen;
    private GameSaveManager saveManager;
    private GameManager gameManager;
    private AudioManager audioManager;
    private Stage loadingStage;
    private Label loadingDetailsLabel;
    private javafx.scene.control.ProgressIndicator progressIndicator;
    private VBox loadingDetailsPane;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Debug: Starting GameApplication");
        

        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);
        this.gameManager = GameManager.getInstance();


        initializeLoadingScreen();
        

        Thread initThread = new Thread(() -> {
            try {

                ResourceManager resourceManager = ResourceManager.getInstance();
                resourceManager.setResourceLoadingListener(GameApplication.this);
                

                initResourceLoadingListener();
                

                resourceManager.preloadAssets();
                


                boolean preloadComplete = resourceManager.waitForPreload(60000);
                
                if (!preloadComplete) {
                    System.err.println("Warning: Resource preloading timed out after 60 seconds");

                }
                

                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("เริ่มเตรียมเกม กรุณารอสักครู่...");
                    }
                });
                

                finishInitialization();
                
            } catch (Exception e) {
                System.err.println("Error during game initialization: " + e.getMessage());
                e.printStackTrace();
                

                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "Failed to initialize game: " + e.getMessage());
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

                    ResourceManager.getInstance().getAudioManager().playMusic("Buckshot_Roulette_OST.mp3");
                    

                    if (statusLabel != null) {
                        statusLabel.setText("โหลดเสร็จสมบูรณ์ กำลังเริ่มเข้าสู่เกม...");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
                    }


                    System.out.println("กำลังเตรียม CutsceneScreen...");
                    CutsceneScreen cutsceneScreen = new CutsceneScreen(gameConfig, screenManager, this);
                    

                    System.out.println("CutsceneScreen พร้อมแล้ว");
                    

                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        screenManager.prepareScreen(cutsceneScreen);
                        primaryStage.show();

                        if (loadingStage != null && loadingStage.isShowing()) {
                            System.out.println("กำลังซ่อนหน้า Loading หลังจากแสดง CutsceneScreen...");

                            javafx.scene.layout.StackPane root = (javafx.scene.layout.StackPane) loadingStage.getScene().getRoot();
                            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(1000), root);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(event -> {
                                loadingStage.close();
                                loadingStage = null;
                                System.out.println("ปิดหน้าจอโหลดเรียบร้อย");
                                System.out.println("โหลดทรัพยากรเสร็จสมบูรณ์");
                            });
                            fadeOut.play();
                        }
                    });
                    pause.play();
                    
                } catch (Exception e) {
                    System.err.println("Error in JavaFX thread: " + e.getMessage());
                    e.printStackTrace();
                    hideLoadingScreen();
                    showAlert("Error", "Failed to initialize game UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error during game initialization: " + e.getMessage());
            e.printStackTrace();
            

            Platform.runLater(() -> {
                hideLoadingScreen();
                showAlert("Error", "Failed to initialize game: " + e.getMessage());
                primaryStage.show();
            });
        }
    }


    private void hideLoadingScreenWithCallback(Runnable callback) {

        Platform.runLater(() -> {
            try {
                if (loadingStage != null && loadingStage.isShowing()) {
                    System.out.println("ปิดหน้าจอโหลด...");
                    

                    javafx.scene.layout.StackPane root = (javafx.scene.layout.StackPane) loadingStage.getScene().getRoot();
                    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(500), root);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        loadingStage.close();
                        loadingStage = null;
                        System.out.println("ปิดหน้าจอโหลดเรียบร้อย");
                        System.out.println("โหลดทรัพยากรเสร็จสมบูรณ์");
                        

                        if (callback != null) {
                            callback.run();
                        }
                    });
                    fadeOut.play();
                } else {

                    if (callback != null) {
                        callback.run();
                    }
                }
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการปิดหน้าจอโหลด: " + e.getMessage());
                e.printStackTrace();
                

                if (callback != null) {
                    callback.run();
                }
            }
        });
    }

    private GameConfig createGameConfig() {
        return DefaultGameConfig.getInstance();
    }

    private void initializeGame() {

        gameConfig.load();
        

        saveManager = new GameSaveManager();
        

        Platform.runLater(() -> {
            try {

                SceneController.initialize(primaryStage, gameConfig, screenManager);
                

                createScreens();
                

                primaryStage.setTitle("VPS Tycoon");
                primaryStage.setResizable(false);
                primaryStage.setOnCloseRequest(e -> shutdown());
                

                GameEventBus.getInstance().subscribe(
                    SettingsChangedEvent.class,
                    event -> Platform.runLater(() -> 
                        SceneController.getInstance().updateResolution()
                    )
                );
            } catch (Exception e) {
                System.err.println("Error initializing game UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void createScreens() {
        mainMenuScreen = new MainMenuScreen(gameConfig, screenManager, this);
        settingsScreen = new SettingsScreen(gameConfig, screenManager, this, this::showMainMenu);
    }

    @Override
    public void showMainMenu() {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController ก่อนไปเมนูหลัก");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        mainMenuScreen.show();
    }

    @Override
    public void showSettings() {
        settingsScreen.show();
    }

    public void showPlayMenu() {
        showMainMenu();
    }

    @Override
    public void startNewGame() {
        System.out.println("=========== STARTING NEW GAME ===========");
        

        showLoadingScreen("Starting New Game...");

        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController ก่อนเริ่มเกมใหม่");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        new Thread(() -> {
            try {

                GameManager.resetInstance();
                

                ResourceManager.getInstance().resetRackAndInventory();


                


                ResourceManager.getInstance().deleteSaveFile();
                System.out.println("ลบไฟล์เซฟเดิมเรียบร้อย");
                

                ResourceManager.getInstance().resetMessengerData();
                

                ResourceManager.getInstance().resetGameTime();
                

                GameState existingState = ResourceManager.getInstance().getCurrentState();
                if (existingState != null) {
                    existingState.clearState();
                    System.out.println("ล้างค่า GameState ปัจจุบันเรียบร้อย");
                }
                

                GameState newState = new GameState();
                

                Company newCompany = new Company();
                newCompany.setMoney(50_000);
                newCompany.setRating(1.0);
                newState.setCompany(newCompany);
                

                newState.setLocalDateTime(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
                

                ResourceManager.getInstance().setCurrentState(newState);
                System.out.println("อัพเดท GameState ใน ResourceManager แล้ว");
                

                com.vpstycoon.ui.game.components.RoomObjectsLayer.preloadImages();
                

                final GameState finalState = newState;
                Platform.runLater(() -> {


                    if (gameplayScreen != null) {
                        gameplayScreen.release();
                    }
                    
                    gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, finalState);
                    gameplayScreen.show();
                    

                    hideLoadingScreen();
                    

                    System.out.println("เริ่มเกมใหม่เรียบร้อย");
                });
            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "Failed to start new game: " + e.getMessage());
                });
            }
        }).start();
    }
    

    private void preloadCommonResources() {
        try {


            com.vpstycoon.ui.game.components.RoomObjectsLayer.preloadImages();
            
            System.out.println("Common resources preloaded through RoomObjectsLayer");
        } catch (Exception e) {
            System.err.println("Error preloading resources: " + e.getMessage());
        }
    }
    

    private void showLoadingScreen(String message) {
        Platform.runLater(() -> {
            try {
                if (loadingStage == null) {
                    loadingStage = new Stage();
                    loadingStage.initOwner(primaryStage);
                    loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    loadingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    
                    StackPane root = new StackPane();
                    root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 20;");
                    
                    VBox content = new VBox(15);
                    content.setAlignment(Pos.CENTER);
                    
                    Label loadingLabel = new Label(message);
                    loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
                    
                    javafx.scene.control.ProgressIndicator progress = new javafx.scene.control.ProgressIndicator();
                    progress.setMaxSize(50, 50);
                    
                    content.getChildren().addAll(loadingLabel, progress);
                    root.getChildren().add(content);
                    
                    Scene scene = new Scene(root, 300, 150);
                    scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    loadingStage.setScene(scene);
                    

                    loadingStage.setX(primaryStage.getX() + (primaryStage.getWidth() - 300) / 2);
                    loadingStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 150) / 2);
                }
                
                loadingStage.show();
            } catch (Exception e) {
                System.err.println("Error showing loading screen: " + e.getMessage());
            }
        });
    }
    

    private void hideLoadingScreen() {

        Platform.runLater(() -> {
            try {
                if (loadingStage != null && loadingStage.isShowing()) {
                    System.out.println("ปิดหน้าจอโหลด...");
                    loadingStage.close();
                    loadingStage = null;
                    System.out.println("ปิดหน้าจอโหลดเรียบร้อย");
                    System.out.println("โหลดทรัพยากรเสร็จสมบูรณ์");
                }
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการปิดหน้าจอโหลด: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void continueGame() {
        System.out.println("=========== CONTINUING GAME ===========");
        

        showLoadingScreen("Loading Saved Game...");
        

        new Thread(() -> {
            try {

                boolean hasSaveFile = ResourceManager.getInstance().hasSaveFile();
                System.out.println("hasSaveFile: " + hasSaveFile);
                
                if (!hasSaveFile) {
                    Platform.runLater(() -> {
                        hideLoadingScreen();
                        showAlert("Error", "No save file found.");
                        showMainMenu();
                    });
                    return;
                }
                

                GameManager gameManager = GameManager.getInstance();
                GameState savedState = ResourceManager.getInstance().loadGameState();
                
                if (savedState != null && savedState.getCompany() != null) {

                    System.out.println("กำลังโหลดข้อมูลทั้งหมดในระบบ...");
                    gameManager.loadState();
                    

                    ChatHistoryManager.resetInstance();

                    ChatHistoryManager chatManager = ChatHistoryManager.getInstance();
                    System.out.println("โหลดข้อมูลประวัติแชทเรียบร้อยแล้ว");
                    

                    savedState = ResourceManager.getInstance().loadGameState();
                    

                    if (savedState == null || savedState.getCompany() == null) {
                        Platform.runLater(() -> {
                            hideLoadingScreen();
                            showAlert("Error", "Failed to load saved game state.");
                            showMainMenu();
                        });
                        return;
                    }
                    

                    System.out.println("กำลังเริ่มต้นเกมที่บันทึกไว้...");
                    startGame(savedState);
                } else {
                    Platform.runLater(() -> {
                        hideLoadingScreen();
                        showAlert("Error", "Failed to load saved game state.");
                        showMainMenu();
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading game: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "Failed to load game: " + e.getMessage());
                    showMainMenu();
                });
            }
        }).start();
    }

    @Override
    public void showLoadGame() {
        try {
            if (!ResourceManager.getInstance().hasSaveFile()) {
                showAlert("No saved game found", "There is no saved game to continue.");
                return;
            }
            GameState savedState = ResourceManager.getInstance().loadGameState();
            gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, savedState);
            gameplayScreen.show();
        } catch (Exception e) {
            showAlert("Error", "Could not load saved game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void showInGameSettings() {



    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadGameState(GameState state) {
        gameManager.loadState();
    }

    private void startGame(GameState state) {
        if (gameplayScreen != null) {
            gameplayScreen.release();
        }
        gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, state);
        gameplayScreen.show();
        System.out.println("เริ่มเกมด้วย GameplayScreen ซึ่งจะเริ่มการเดินเวลาเกม");
    }

    private void shutdown() {
        try {

            System.out.println("กำลังบันทึกเกมก่อนปิดแอพพลิเคชัน...");
            

            if (gameplayScreen != null) {
                try {

                    Company company = ResourceManager.getInstance().getCompany();
                    if (company != null) {
                        List<GameObject> gameObjects = 
                            ResourceManager.getInstance().getCurrentState().getGameObjects();
                        
                        GameState state = new GameState(company, gameObjects);
                        state.setLocalDateTime(ResourceManager.getInstance().getGameTimeController().getGameTimeManager().getGameDateTime());
                        

                        ResourceManager.getInstance().saveGameState(state);
                        System.out.println("บันทึกเกมเรียบร้อยแล้ว");
                    }
                } catch (Exception saveEx) {
                    System.err.println("เกิดข้อผิดพลาดในการบันทึกเกม: " + saveEx.getMessage());
                    saveEx.printStackTrace();
                }
            }
            

            gameConfig.save();
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            System.exit(1);
        }
    }

    private void showCutscene() {


        System.out.println("showCutscene ถูกเรียก - ข้ามเพราะได้จัดการใน finishInitialization แล้ว");
    }


    private void updateLoadingProgress(String message) {
        if (loadingDetailsLabel != null) {
            Platform.runLater(() -> loadingDetailsLabel.setText(message));
        }
    }
    
    private void initializeLoadingScreen() {
        Platform.runLater(() -> {
            try {

                StackPane root = new StackPane();
                root.setStyle("-fx-background-color: #121212;");

                javafx.scene.layout.Region darkBackground = new javafx.scene.layout.Region();
                darkBackground.setStyle("""
                    -fx-background-color: black;
                    -fx-background-radius: 0;
                """);

                javafx.scene.layout.GridPane gridLines = new javafx.scene.layout.GridPane();
                gridLines.setHgap(20);
                gridLines.setVgap(20);
                gridLines.setStyle("""
                    -fx-background-color: rgba(0, 0, 0, 0);
                    -fx-grid-lines-visible: true;
                    -fx-border-color: rgba(58, 19, 97, 0.3);
                    -fx-border-width: 1;
                """);

                int cols = 20;
                int rows = 20;

                for (int i = 0; i < cols; i++) {
                    javafx.scene.layout.ColumnConstraints colConstraint = new javafx.scene.layout.ColumnConstraints();
                    colConstraint.setPercentWidth(100.0 / cols);
                    gridLines.getColumnConstraints().add(colConstraint);
                }

                for (int i = 0; i < rows; i++) {
                    javafx.scene.layout.RowConstraints rowConstraint = new javafx.scene.layout.RowConstraints();
                    rowConstraint.setPercentHeight(100.0 / rows);
                    gridLines.getRowConstraints().add(rowConstraint);
                }

                VBox content = new VBox(20);
                content.setAlignment(Pos.CENTER);
                content.setPadding(new Insets(50));
                
                Label titleLabel = new Label("VPS Tycoon");
                titleLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");
                
                // เพิ่ม glow effect ให้ title คล้ายกับใน cutscene
                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                glow.setColor(javafx.scene.paint.Color.web("#8A2BE2"));  // สีม่วง
                glow.setRadius(20);
                glow.setSpread(0.2);
                titleLabel.setEffect(glow);

                Label loadingLabel = new Label("กำลังโหลดทรัพยากร...");
                loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                

                loadingDetailsLabel = new Label("");
                loadingDetailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
                
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(100);
                scrollPane.setMaxHeight(100);
                
                loadingDetailsPane = new VBox(5);
                loadingDetailsPane.setStyle("-fx-background-color: transparent;");
                scrollPane.setContent(loadingDetailsPane);

                progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setProgress(-1);
                progressIndicator.setStyle("""
                    -fx-progress-color: #8A2BE2;
                """);
                progressIndicator.setPrefSize(50, 50);

                statusLabel = new Label("กำลังเริ่มเกม...");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ecc71;");

                content.getChildren().addAll(titleLabel, loadingLabel, progressIndicator, statusLabel);

                root.getChildren().addAll(darkBackground, gridLines, content);

                Scene scene = new Scene(root, 500, 300);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                
                if (loadingStage == null) {
                    loadingStage = new Stage();
                    loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    loadingStage.setResizable(false);
                }
                
                loadingStage.setScene(scene);
                loadingStage.show();

                javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                loadingStage.setX((screenBounds.getWidth() - 500) / 2);
                loadingStage.setY((screenBounds.getHeight() - 300) / 2);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                

                initResourceLoadingListener();

            } catch (Exception e) {
                System.err.println("Error showing loading screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void initResourceLoadingListener() {

        final String[] imagesToPreload = {
            "/images/rooms/room.gif",
            "/images/Moniter/MoniterF2.png",
            "/images/servers/server2.gif",
            "/images/Object/Keroro.png",
            "/images/Object/MusicboxOn.gif",
            "/images/Object/MusicboxOff.png",
            "/images/Object/Table.png",
        };

        final String[] soundFiles = {
            "hover.wav",
            "click.wav",
            "click_app.wav",
            "server.mp3",
            "keroro_sound.mp3"
        };
        

        com.vpstycoon.ui.game.components.RoomObjectsLayer.preloadImages();
        

        Thread preloadThread = new Thread(() -> {
            try {
                ResourceManager resourceManager = ResourceManager.getInstance();
                int totalPreloaded = 0;
                

                for (String imagePath : imagesToPreload) {
                    try {

                        final String currentPath = imagePath;
                        Platform.runLater(() -> {
                            loadingDetailsLabel.setText("กำลังโหลด: " + currentPath);
                            

                            Label fileLabel = new Label("- " + currentPath);
                            fileLabel.setStyle("-fx-text-fill: #AAAAAA;");
                            loadingDetailsPane.getChildren().add(0, fileLabel);
                        });
                        

                        new Image(imagePath, true);
                        totalPreloaded++;
                        Thread.sleep(100);
                    } catch (Exception e) {
                        System.err.println("ไม่สามารถโหลดรูปภาพ: " + imagePath);
                    }
                }
                

                AudioManager audioManager = resourceManager.getAudioManager();
                for (String soundFile : soundFiles) {
                    try {

                        final String currentSound = soundFile;
                        Platform.runLater(() -> {
                            loadingDetailsLabel.setText("กำลังโหลดเสียง: " + currentSound);
                            Label fileLabel = new Label("- เสียง: " + currentSound);
                            fileLabel.setStyle("-fx-text-fill: #AAAAAA;");
                            loadingDetailsPane.getChildren().add(0, fileLabel);
                        });
                        

                        audioManager.preloadSoundEffect(soundFile);
                        totalPreloaded++;
                        Thread.sleep(100);
                    } catch (Exception e) {
                        System.err.println("ไม่สามารถโหลดไฟล์เสียง: " + soundFile);
                    }
                }
                

                int totalResources = totalPreloaded;
                Thread progressMonitor = new Thread(() -> {
                    try {

                        Thread.sleep(500);
                        

                        while (true) {

                            Thread.sleep(100);
                            

                            if (resourceManager.isPreloadComplete()) {

                                Platform.runLater(() -> {
                                    updateProgressStatus("การโหลดเสร็จสมบูรณ์! กำลังเตรียมเริ่มเกม...");
                                });
                                

                                Thread.sleep(1000);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error in progress monitor: " + e.getMessage());
                    }
                });
                
                progressMonitor.setDaemon(true);
                progressMonitor.start();
                

                final int finalCount = totalResources;
                Platform.runLater(() -> {
                    statusLabel.setText("โหลดทรัพยากรแล้ว " + finalCount + " รายการ");
                });
                
            } catch (Exception e) {
                System.err.println("Error in preload thread: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        preloadThread.setDaemon(true);
        preloadThread.start();
    }

    @Override
    public void onResourceLoading(String resourcePath) {
        if (resourcePath != null && !resourcePath.trim().isEmpty()) {
            Platform.runLater(() -> {

                loadingDetailsLabel.setText("กำลังโหลด: " + getSimpleFileName(resourcePath));
                

                Label fileLabel = new Label("- " + resourcePath);
                fileLabel.setStyle("-fx-text-fill: #AAAAAA;");
                
                if (loadingDetailsPane != null) {
                    loadingDetailsPane.getChildren().add(0, fileLabel);
                    

                    if (loadingDetailsPane.getChildren().size() > 100) {
                        loadingDetailsPane.getChildren().remove(100, loadingDetailsPane.getChildren().size());
                    }
                }
            });
        }
    }

    @Override
    public void onResourceLoadingComplete(int totalLoaded) {
        Platform.runLater(() -> {
            loadingDetailsLabel.setText("โหลดทรัพยากรเสร็จสมบูรณ์");
            
            if (statusLabel != null) {
                statusLabel.setText("โหลดทรัพยากรครบถ้วน " + totalLoaded + " รายการ");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
            }
        });
    }


    private String getSimpleFileName(String path) {
        if (path == null) return "";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }


    private void updateProgressStatus(String message) {

        if (statusLabel != null) {
            final String statusMessage = message;
            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText(statusMessage);
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
