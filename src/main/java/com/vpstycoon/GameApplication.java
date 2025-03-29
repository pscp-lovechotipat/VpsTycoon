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
                        statusLabel.setText("โหลดเสร็จสมบูรณ์ กำลังเริ่มเข้าสู่เกม...");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
                    }
                    
                    // 1. เตรียม CutsceneScreen ก่อนจะเปลี่ยนจากหน้า Loading
                    System.out.println("กำลังเตรียม CutsceneScreen...");
                    CutsceneScreen cutsceneScreen = new CutsceneScreen(gameConfig, screenManager, this);
                    
                    // 2. เพิ่มการเช็คว่า CutsceneScreen โหลดเสร็จ
                    System.out.println("CutsceneScreen พร้อมแล้ว");
                    
                    // 3. แสดง Loading ต่อไปอีกสักพักด้วย PauseTransition
                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        // 4. แสดง CutsceneScreen ในหน้าต่างหลัก และปิดหน้า loading หลังจาก fade in เสร็จ
                        // ตั้งค่าให้ primaryStage แสดง cutsceneScreen
                        screenManager.prepareScreen(cutsceneScreen);
                        primaryStage.show();
                        
                        // เมื่อ CutsceneScreen ถูกแสดงแล้ว จึงค่อย fade out หน้า loading
                        // ทำให้การเปลี่ยนหน้าจอดูราบรื่น
                        if (loadingStage != null && loadingStage.isShowing()) {
                            System.out.println("กำลังซ่อนหน้า Loading หลังจากแสดง CutsceneScreen...");
                            
                            // สร้าง fade out transition สำหรับหน้า loading
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
        // หยุด GameTimeController ก่อนไปเมนูหลัก (ถ้ามี)
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
        
        // Show a loading screen while game initializes
        showLoadingScreen("Starting New Game...");
        
        // ตรวจสอบและหยุด GameTimeController ก่อนเริ่มเกมใหม่ (ป้องกัน thread ซ้อนกัน)
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController ก่อนเริ่มเกมใหม่");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }
        
        // Move initialization operations to background thread
        new Thread(() -> {
            try {
                // รีเซ็ต GameManager ก่อน เพื่อให้เริ่มต้นใหม่ทั้งหมด
                GameManager.resetInstance();
                
                // รีเซ็ต Rack และ VPSInventory
                ResourceManager.getInstance().resetRackAndInventory();

                // หมายเหตุ: resetRackAndInventory() ได้เรียกใช้ resetMessengerData() แล้ว
                // จึงไม่จำเป็นต้องรีเซ็ตประวัติแชทซ้ำอีก
                
                // 2. ลบไฟล์เซฟเดิมก่อน
                ResourceManager.getInstance().deleteSaveFile();
                System.out.println("ลบไฟล์เซฟเดิมเรียบร้อย");
                
                // เรียกใช้ resetMessengerData() อีกครั้งเพื่อให้แน่ใจว่า RequestManager ถูกรีเซ็ตอย่างสมบูรณ์
                ResourceManager.getInstance().resetMessengerData();
                
                // เรียกใช้ resetGameTime() เพื่อรีเซ็ตเวลาเกมให้สมบูรณ์
                ResourceManager.getInstance().resetGameTime();
                
                // ล้างค่า GameState ปัจจุบันก่อนถ้ามีอยู่แล้ว
                GameState existingState = ResourceManager.getInstance().getCurrentState();
                if (existingState != null) {
                    existingState.clearState();
                    System.out.println("ล้างค่า GameState ปัจจุบันเรียบร้อย");
                }
                
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
        System.out.println("=========== CONTINUING GAME ===========");
        
        // Show a loading screen
        showLoadingScreen("Loading Saved Game...");
        
        // Load game in a background thread
        new Thread(() -> {
            try {
                // ตรวจสอบว่ามีไฟล์เซฟหรือไม่
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
                
                // 1. โหลด GameState จากไฟล์เซฟ
                GameManager gameManager = GameManager.getInstance();
                GameState savedState = ResourceManager.getInstance().loadGameState();
                
                if (savedState != null && savedState.getCompany() != null) {
                    // โหลดข้อมูลทั้งหมดในระบบก่อนสร้างหน้าเกม
                    System.out.println("กำลังโหลดข้อมูลทั้งหมดในระบบ...");
                    gameManager.loadState();
                    
                    // รีเซ็ต ChatHistoryManager instance เพื่อให้โหลดข้อมูลจาก save.dat ใหม่
                    ChatHistoryManager.resetInstance();
                    // โหลดข้อมูลประวัติแชท
                    ChatHistoryManager chatManager = ChatHistoryManager.getInstance();
                    System.out.println("โหลดข้อมูลประวัติแชทเรียบร้อยแล้ว");
                    
                    // 3. โหลดข้อมูลเกมจากไฟล์เซฟ อีกครั้งเพื่อนำไปสร้างหน้าเกม
                    savedState = ResourceManager.getInstance().loadGameState();
                    
                    // 4. ตรวจสอบว่าโหลดข้อมูลได้ถูกต้อง
                    if (savedState == null || savedState.getCompany() == null) {
                        Platform.runLater(() -> {
                            hideLoadingScreen();
                            showAlert("Error", "Failed to load saved game state.");
                            showMainMenu();
                        });
                        return;
                    }
                    
                    // เริ่มต้นเกม
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
        if (gameplayScreen != null) {
            gameplayScreen.release();
        }
        gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, state);
        gameplayScreen.show();
        System.out.println("เริ่มเกมด้วย GameplayScreen ซึ่งจะเริ่มการเดินเวลาเกม");
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
                
                // สร้าง effect พื้นหลังคล้ายกับ cutscene
                javafx.scene.layout.Region darkBackground = new javafx.scene.layout.Region();
                darkBackground.setStyle("""
                    -fx-background-color: black;
                    -fx-background-radius: 0;
                """);
                
                // สร้าง GridPane สำหรับเส้นกริด (คล้ายกับ CutsceneBackground)
                javafx.scene.layout.GridPane gridLines = new javafx.scene.layout.GridPane();
                gridLines.setHgap(20);
                gridLines.setVgap(20);
                gridLines.setStyle("""
                    -fx-background-color: rgba(0, 0, 0, 0);
                    -fx-grid-lines-visible: true;
                    -fx-border-color: rgba(58, 19, 97, 0.3);
                    -fx-border-width: 1;
                """);
                
                // สร้างจำนวนคอลัมน์และแถวให้พอดีกับขนาดหน้าจอ
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
                
                // Create loading details label with a scroll pane to show what's being loaded
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
                
                // Create a stylish progress indicator
                progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setProgress(-1); // ไม่ระบุความคืบหน้า (รอจนกว่าจะโหลดเสร็จ)
                progressIndicator.setStyle("""
                    -fx-progress-color: #8A2BE2;
                """);
                progressIndicator.setPrefSize(50, 50);
                
                // Create status label
                statusLabel = new Label("กำลังเริ่มเกม...");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ecc71;");
                
                // เพิ่ม component ทั้งหมดเข้าไปใน content
                content.getChildren().addAll(titleLabel, loadingLabel, progressIndicator, statusLabel);
                
                // เพิ่ม component ทั้งหมดเข้าไปใน root
                root.getChildren().addAll(darkBackground, gridLines, content);
                
                // สร้าง scene และตั้งค่า
                Scene scene = new Scene(root, 500, 300);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                
                if (loadingStage == null) {
                    loadingStage = new Stage();
                    loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    loadingStage.setResizable(false);
                }
                
                loadingStage.setScene(scene);
                loadingStage.show();
                
                // Center on screen
                javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                loadingStage.setX((screenBounds.getWidth() - 500) / 2);
                loadingStage.setY((screenBounds.getHeight() - 300) / 2);
                
                // Fade in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
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