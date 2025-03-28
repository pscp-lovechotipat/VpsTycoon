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
import javafx.util.Duration;

// Good Morning
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
        
        // Set up basic application properties
        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);
        this.gameManager = GameManager.getInstance();

        // Show loading screen while resources load
        initializeLoadingScreen();
        
        // Initialize the game in a separate thread
        Thread initThread = new Thread(() -> {
            try {
                // Set up the resource loading listener first
                ResourceManager resourceManager = ResourceManager.getInstance();
                resourceManager.setResourceLoadingListener(GameApplication.this);
                
                // Start our custom resource preloader which will show loading progress
                initResourceLoadingListener();
                
                // Then start the ResourceManager's built-in preloading
                resourceManager.preloadAssets();
                
                // Wait for ResourceManager preloading to complete with a timeout
                // This ensures we don't hang indefinitely
                boolean preloadComplete = resourceManager.waitForPreload(60000);
                
                if (!preloadComplete) {
                    System.err.println("Warning: Resource preloading timed out after 60 seconds");
                    // Continue anyway to avoid hanging
                }
                
                // Update status before initialization
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("เริ่มเตรียมเกม กรุณารอสักครู่...");
                    }
                });
                
                // Finalize initialization
                finishInitialization();
                
            } catch (Exception e) {
                System.err.println("Error during game initialization: " + e.getMessage());
                e.printStackTrace();
                
                // Show error on JavaFX thread
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

    /**
     * Finalize game initialization after resources are loaded
     */
    private void finishInitialization() {
        try {
            // Initialize the game (non-UI operations can happen off JavaFX thread)
            initializeGame();
            
            // All UI operations must be on the JavaFX thread
            Platform.runLater(() -> {
                try {
                    // Play music after resources are loaded
                    ResourceManager.getInstance().getAudioManager().playMusic("Buckshot_Roulette_OST.mp3");
                    
                    // Update loading screen to show we're ready
                    if (statusLabel != null) {
                        statusLabel.setText("โหลดเสร็จสมบูรณ์ กำลังเตรียมเริ่มเข้าสู่เกม...");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
                    }
                    
                    // 1. เตรียม CutsceneScreen ก่อนจะซ่อนหน้า Loading
                    System.out.println("กำลังเตรียม CutsceneScreen...");
                    CutsceneScreen cutsceneScreen = new CutsceneScreen(gameConfig, screenManager, this);
                    
                    // 2. เพิ่มการเช็คว่า CutsceneScreen โหลดเสร็จ (อาจใช้ตรงนี้ได้เลย เพราะเป็น synchronous)
                    System.out.println("CutsceneScreen พร้อมแล้ว");
                    
                    // 3. แสดง Loading ต่อไปอีกสักพักด้วย PauseTransition
                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        // 4. สร้าง callback ที่จะเปลี่ยนไปหน้า Cutscene หลังจาก Loading ถูกซ่อนเรียบร้อยแล้ว
                        Runnable afterLoadingHidden = () -> {
                            // 5. ตั้งค่า callback ให้ CutsceneScreen 
                            System.out.println("กำลังแสดง CutsceneScreen...");
                            
                            // 6. ใช้ ScreenManager เพื่อเปลี่ยนหน้าจอ (มี fadeout/fadein ในตัว)
                            screenManager.switchScreen(cutsceneScreen);
                            
                            // 7. แสดง primary stage
                            primaryStage.show();
                        };
                        
                        // ซ่อนหน้า Loading พร้อมกับส่ง callback
                        System.out.println("กำลังซ่อนหน้า Loading...");
                        hideLoadingScreenWithCallback(afterLoadingHidden);
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
            
            // Show error on JavaFX thread
            Platform.runLater(() -> {
                hideLoadingScreen();
                showAlert("Error", "Failed to initialize game: " + e.getMessage());
                primaryStage.show();
            });
        }
    }

    /**
     * ซ่อนหน้า Loading และเรียก callback เมื่อเสร็จสิ้น
     */
    private void hideLoadingScreenWithCallback(Runnable callback) {
        // Always use Platform.runLater when updating UI elements
        Platform.runLater(() -> {
            try {
                if (loadingStage != null && loadingStage.isShowing()) {
                    System.out.println("ปิดหน้าจอโหลด...");
                    
                    // สร้าง fade out transition สำหรับหน้า loading
                    javafx.scene.layout.StackPane root = (javafx.scene.layout.StackPane) loadingStage.getScene().getRoot();
                    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(500), root);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        loadingStage.close();
                        loadingStage = null;
                        System.out.println("ปิดหน้าจอโหลดเรียบร้อย");
                        System.out.println("โหลดทรัพยากรเสร็จสมบูรณ์");
                        
                        // เรียก callback หลังจากปิดหน้าโหลดเรียบร้อยแล้ว
                        if (callback != null) {
                            callback.run();
                        }
                    });
                    fadeOut.play();
                } else {
                    // ถ้าไม่มีหน้า loading แสดงอยู่ ให้เรียก callback ทันที
                    if (callback != null) {
                        callback.run();
                    }
                }
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการปิดหน้าจอโหลด: " + e.getMessage());
                e.printStackTrace();
                
                // ถึงแม้จะมีข้อผิดพลาด ก็ให้เรียก callback
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
        // Load configuration (non-UI operation)
        gameConfig.load();
        
        // Initialize managers (non-UI operation)
        saveManager = new GameSaveManager();
        
        // All UI operations must be on the JavaFX thread
        Platform.runLater(() -> {
            try {
                // Initialize scene controller
                SceneController.initialize(primaryStage, gameConfig, screenManager);
                
                // Create screens
                createScreens();
                
                // Setup primary stage
                primaryStage.setTitle("VPS Tycoon");
                primaryStage.setResizable(false);
                primaryStage.setOnCloseRequest(e -> shutdown());
                
                // Subscribe to settings changes
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
        
        // Show a loading screen while game initializes
        showLoadingScreen("Starting New Game...");
        
        // Move initialization operations to background thread
        new Thread(() -> {
            try {
                // 1. ลบและรีเซ็ตประวัติแชท
                try {
                    ChatHistoryManager chatManager = ResourceManager.getInstance().getChatHistory();
                    
                    // เคลียร์ประวัติแชททั้งหมด
                    ChatHistoryManager.resetInstance();
                    System.out.println("ล้างประวัติแชทเรียบร้อย");
                } catch (Exception e) {
                    System.err.println("เกิดข้อผิดพลาดในการลบประวัติแชท: " + e.getMessage());
                }
                
                // 2. ลบไฟล์เซฟเดิมก่อน
                ResourceManager.getInstance().deleteSaveFile();
                System.out.println("ลบไฟล์เซฟเดิมเรียบร้อย");
                
                // 3. สร้าง GameState ใหม่ (ไม่อ่านจากไฟล์)
                GameState newState = new GameState();
                
                // 4. สร้าง Company ใหม่และตั้งค่าเริ่มต้น
                Company newCompany = new Company();
                newCompany.setMoney(50_000);
                newCompany.setRating(1.0);
                newState.setCompany(newCompany);
                
                // 5. รีเซ็ตเวลากลับไปที่เริ่มเกม
                newState.setLocalDateTime(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
                
                // 6. อัพเดท currentState ใน ResourceManager
                ResourceManager.getInstance().setCurrentState(newState);
                System.out.println("อัพเดท GameState ใน ResourceManager แล้ว");
                
                // Force preload images once - this is done in a static initializer of RoomObjectsLayer
                com.vpstycoon.ui.game.components.RoomObjectsLayer.preloadImages();
                
                // Create and show gameplay screen on the JavaFX thread
                final GameState finalState = newState;
                Platform.runLater(() -> {
                    // 7. สร้างและแสดง gameplay screen
                    // Release previous screen if it exists to free resources
                    if (gameplayScreen != null) {
                        gameplayScreen.release();
                    }
                    
                    gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, finalState);
                    gameplayScreen.show();
                    
                    // Close loading screen
                    hideLoadingScreen();
                    
                    // ไม่บันทึกเกมตอนเริ่มใหม่ - ให้ GameplayScreen จัดการ
                    System.out.println("เริ่มเกมใหม่เรียบร้อย");
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Show error on JavaFX thread
                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "Failed to start new game: " + e.getMessage());
                });
            }
        }).start();
    }
    
    // Preload common resources to avoid stutter
    private void preloadCommonResources() {
        try {
            // Use the static preloading method from RoomObjectsLayer
            // which already implements a caching mechanism
            com.vpstycoon.ui.game.components.RoomObjectsLayer.preloadImages();
            
            System.out.println("Common resources preloaded through RoomObjectsLayer");
        } catch (Exception e) {
            System.err.println("Error preloading resources: " + e.getMessage());
        }
    }
    
    // Loading screen management
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
                    
                    // Center on primary stage
                    loadingStage.setX(primaryStage.getX() + (primaryStage.getWidth() - 300) / 2);
                    loadingStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 150) / 2);
                }
                
                loadingStage.show();
            } catch (Exception e) {
                System.err.println("Error showing loading screen: " + e.getMessage());
            }
        });
    }
    
    /**
     * Hide the loading screen when all resources are loaded
     */
    private void hideLoadingScreen() {
        // Always use Platform.runLater when updating UI elements
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
        System.out.println("=========== CONTINUE GAME ===========");
        
        // 1. รีเซ็ต ChatHistoryManager ก่อน
        try {
            ChatHistoryManager.resetInstance();
            System.out.println("รีเซ็ต ChatHistoryManager เรียบร้อย");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ต ChatHistoryManager: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 2. ตรวจสอบไฟล์เซฟ
        if (ResourceManager.getInstance().hasSaveFile()) {
            try {
                System.out.println("พบไฟล์เซฟ กำลังโหลด...");
                
                // 2.1 เตรียม GameManager ให้พร้อมโหลดข้อมูล
                if (gameManager == null) {
                    gameManager = GameManager.getInstance();
                }
                
                // 2.2 โหลดข้อมูลเกมจากไฟล์เซฟ
                GameState savedState = ResourceManager.getInstance().loadGameState();
                
                // 2.3 ทำการโหลดข้อมูลทั้งหมดในฝั่ง GameManager ก่อน
                if (savedState != null && savedState.getCompany() != null) {
                    // โหลดข้อมูลทั้งหมดในระบบก่อนสร้างหน้าเกม
                    System.out.println("กำลังโหลดข้อมูลทั้งหมดในระบบ...");
                    gameManager.loadState();
                    
                    // 3. โหลดข้อมูลเกมจากไฟล์เซฟ อีกครั้งเพื่อนำไปสร้างหน้าเกม
                    savedState = ResourceManager.getInstance().loadGameState();
                    
                    // 4. ตรวจสอบว่าโหลดข้อมูลได้ถูกต้อง
                    if (savedState != null && savedState.getCompany() != null) {
                        System.out.println("โหลดเกมสำเร็จ ข้อมูลบริษัท:");
                        System.out.println("- เงิน: $" + savedState.getCompany().getMoney());
                        System.out.println("- Rating: " + savedState.getCompany().getRating());
                        System.out.println("- Free VM: " + savedState.getFreeVmCount());
                        
                        // ตรวจสอบข้อมูล rack
                        if (savedState.getRackConfiguration() != null) {
                            System.out.println("- พบข้อมูล Rack Configuration");
                        } else {
                            System.out.println("- ไม่พบข้อมูล Rack Configuration");
                        }
                        
                        // แสดงข้อมูล VPS Inventory ถ้ามี
                        if (savedState.getVpsInventoryData() != null) {
                            System.out.println("- พบข้อมูล VPS Inventory");
                        } else {
                            System.out.println("- ไม่พบข้อมูล VPS Inventory");
                        }
                        
                        // 5. สร้างและแสดงหน้าเกมด้วยข้อมูลที่โหลดมา
                        gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, savedState);
                        gameplayScreen.show();
                        
                        // 6. ตรวจสอบว่า TimeThread เริ่มทำงานหรือไม่
                        ResourceManager resourceManager = ResourceManager.getInstance();
                        if (resourceManager.getGameTimeController() != null) {
                            // เริ่ม TimeThread และ RequestGenerator อีกครั้ง
                            System.out.println("เริ่มระบบเวลาเกมอีกครั้ง...");
                            resourceManager.getGameTimeController().startTime();
                            
                            // เริ่ม RequestGenerator ใหม่จาก GameManager (ถ้ามี)
                            if (gameManager != null && gameManager.getRequestGenerator() != null) {
                                System.out.println("เริ่ม RequestGenerator อีกครั้ง...");
                                if (!gameManager.getRequestGenerator().isAlive()) {
                                    gameManager.getRequestGenerator().start();
                                }
                            }
                        }
                    } else {
                        showAlert("Error", "ไม่สามารถโหลดเกมได้: ข้อมูลเสียหาย");
                        System.err.println("โหลดเกมล้มเหลว: ข้อมูลว่างหรือไม่ถูกต้อง");
                    }
                } else {
                    showAlert("Error", "ไม่สามารถโหลดเกมได้: ข้อมูลเสียหาย");
                    System.err.println("โหลดเกมล้มเหลว: ข้อมูลว่างหรือไม่ถูกต้อง");
                }
            } catch (Exception e) {
                showAlert("Error", "ไม่สามารถโหลดเกมได้: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("No saved game", "ไม่พบข้อมูลเกมที่บันทึกไว้");
            System.err.println("ไม่พบไฟล์เซฟเกม");
        }
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
        // This method is kept for compatibility with the Navigator interface,
        // but its functionality is now handled by the GameplayScreen directly
        // No implementation needed as the settings popup is shown from the game screen
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
        screenManager.switchScreen(new GameScreen(this, gameConfig, state));
    }

    private void shutdown() {
        try {
            // บันทึกเกมก่อนปิดแอพ
            System.out.println("กำลังบันทึกเกมก่อนปิดแอพพลิเคชัน...");
            
            // ตรวจสอบว่าอยู่ในเกมหรือไม่ (gameplayScreen ไม่เป็น null)
            if (gameplayScreen != null) {
                try {
                    // สร้าง GameState จากข้อมูลปัจจุบัน
                    Company company = ResourceManager.getInstance().getCompany();
                    if (company != null) {
                        List<GameObject> gameObjects = 
                            ResourceManager.getInstance().getCurrentState().getGameObjects();
                        
                        GameState state = new GameState(company, gameObjects);
                        state.setLocalDateTime(ResourceManager.getInstance().getGameTimeController().getGameTimeManager().getGameDateTime());
                        
                        // บันทึกเกม
                        ResourceManager.getInstance().saveGameState(state);
                        System.out.println("บันทึกเกมเรียบร้อยแล้ว");
                    }
                } catch (Exception saveEx) {
                    System.err.println("เกิดข้อผิดพลาดในการบันทึกเกม: " + saveEx.getMessage());
                    saveEx.printStackTrace();
                }
            }
            
            // บันทึกการตั้งค่า
            gameConfig.save();
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            System.exit(1);
        }
    }

    private void showCutscene() {
        // ไม่ต้องสร้าง CutsceneScreen ใหม่และเปลี่ยนหน้าจอที่นี่
        // เพราะได้ดำเนินการในส่วนของ finishInitialization แล้ว
        System.out.println("showCutscene ถูกเรียก - ข้ามเพราะได้จัดการใน finishInitialization แล้ว");
    }

    /**
     * Update loading progress text on the loading screen
     * @param message The current loading details to display
     */
    private void updateLoadingProgress(String message) {
        if (loadingDetailsLabel != null) {
            Platform.runLater(() -> loadingDetailsLabel.setText(message));
        }
    }
    
    private void initializeLoadingScreen() {
        Platform.runLater(() -> {
            try {
                // Create a more detailed loading screen
                StackPane root = new StackPane();
                root.setStyle("-fx-background-color: #121212;");
                
                VBox content = new VBox(20);
                content.setAlignment(Pos.CENTER);
                content.setPadding(new Insets(50));
                
                Label titleLabel = new Label("VPS Tycoon");
                titleLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");
                
                Label loadingLabel = new Label("กำลังโหลดทรัพยากร...");
                loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                
                // Create loading details label with a scroll pane to show what's being loaded
                loadingDetailsLabel = new Label("");
                loadingDetailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7FDBFF;");
                
                // Create a scroll pane for loading details to show multiple files
                VBox detailsBox = new VBox(5);
                detailsBox.setPrefHeight(150);
                detailsBox.setStyle("-fx-background-color: #1E1E1E; -fx-padding: 10;");
                
                ScrollPane detailsScrollPane = new ScrollPane(detailsBox);
                detailsScrollPane.setPrefWidth(600);
                detailsScrollPane.setPrefHeight(150);
                detailsScrollPane.setFitToWidth(true);
                detailsScrollPane.setStyle("-fx-background: #1E1E1E; -fx-border-color: #444;");
                
                progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setMaxSize(60, 60);
                
                Label statusLabel = new Label("โปรดรอสักครู่...");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #AAAAAA;");
                
                content.getChildren().addAll(titleLabel, loadingLabel, progressIndicator, 
                                            loadingDetailsLabel, detailsScrollPane, statusLabel);
                root.getChildren().add(content);
                
                // Store references for updating
                this.loadingDetailsPane = detailsBox;
                this.statusLabel = statusLabel;
                
                // Create scene with the configured resolution
                int width = gameConfig.getResolution().getWidth();
                int height = gameConfig.getResolution().getHeight();
                Scene scene = new Scene(root, width, height);
                
                primaryStage.setScene(scene);
                primaryStage.setTitle("VPS Tycoon - Loading");
                primaryStage.show();
                
                // Initialize the resource loading listener
                initResourceLoadingListener();
            } catch (Exception e) {
                System.err.println("Error showing loading screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void initResourceLoadingListener() {
        // Prepare a list of important resources to manually preload
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
        
        // Force preload all images required for the game
        com.vpstycoon.ui.game.components.RoomObjectsLayer.preloadImages();
        
        // Start a daemon thread to preload additional resources
        Thread preloadThread = new Thread(() -> {
            try {
                ResourceManager resourceManager = ResourceManager.getInstance();
                int totalPreloaded = 0;
                
                // Preload all the images
                for (String imagePath : imagesToPreload) {
                    try {
                        // Update UI safely on JavaFX thread
                        final String currentPath = imagePath;
                        Platform.runLater(() -> {
                            loadingDetailsLabel.setText("กำลังโหลด: " + currentPath);
                            
                            // Add to the details list
                            Label fileLabel = new Label("- " + currentPath);
                            fileLabel.setStyle("-fx-text-fill: #AAAAAA;");
                            loadingDetailsPane.getChildren().add(0, fileLabel);
                        });
                        
                        // Actually load the image into cache - this can be done off the JavaFX thread
                        new Image(imagePath, true);
                        totalPreloaded++;
                        Thread.sleep(100); // Give UI time to update
                    } catch (Exception e) {
                        System.err.println("ไม่สามารถโหลดรูปภาพ: " + imagePath);
                    }
                }
                
                // Preload all sound files
                AudioManager audioManager = resourceManager.getAudioManager();
                for (String soundFile : soundFiles) {
                    try {
                        // Update UI safely on JavaFX thread
                        final String currentSound = soundFile;
                        Platform.runLater(() -> {
                            loadingDetailsLabel.setText("กำลังโหลดเสียง: " + currentSound);
                            Label fileLabel = new Label("- เสียง: " + currentSound);
                            fileLabel.setStyle("-fx-text-fill: #AAAAAA;");
                            loadingDetailsPane.getChildren().add(0, fileLabel);
                        });
                        
                        // Preload the sound - this can be done off the JavaFX thread
                        audioManager.preloadSoundEffect(soundFile);
                        totalPreloaded++;
                        Thread.sleep(100); // Give UI time to update
                    } catch (Exception e) {
                        System.err.println("ไม่สามารถโหลดไฟล์เสียง: " + soundFile);
                    }
                }
                
                // Start a separate thread to monitor ResourceManager loading
                int totalResources = totalPreloaded;
                Thread progressMonitor = new Thread(() -> {
                    try {
                        // Wait a little bit to make sure ResourceManager preloading has started
                        Thread.sleep(500);
                        
                        // Update progress based on ResourceManager's status
                        while (true) {
                            // Sleep briefly to avoid consuming too many resources
                            Thread.sleep(100);
                            
                            // Check if ResourceManager preloading is complete
                            if (resourceManager.isPreloadComplete()) {
                                // Update UI safely on JavaFX thread
                                Platform.runLater(() -> {
                                    updateProgressStatus("การโหลดเสร็จสมบูรณ์! กำลังเตรียมเริ่มเกม...");
                                });
                                
                                // Give a brief pause to show completion message
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
                
                // Update final count
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
                // Update the main loading label
                loadingDetailsLabel.setText("กำลังโหลด: " + getSimpleFileName(resourcePath));
                
                // Add to the detailed list in the scroll pane
                Label fileLabel = new Label("- " + resourcePath);
                fileLabel.setStyle("-fx-text-fill: #AAAAAA;");
                
                if (loadingDetailsPane != null) {
                    loadingDetailsPane.getChildren().add(0, fileLabel);
                    
                    // Keep the list at a reasonable size
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

    // Helper method to extract a simplified filename from a path
    private String getSimpleFileName(String path) {
        if (path == null) return "";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    /**
     * Update the progress status text on the loading screen
     * @param message The status message to display
     */
    private void updateProgressStatus(String message) {
        // Always use Platform.runLater to update UI elements
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