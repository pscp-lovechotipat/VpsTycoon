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
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.List;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;

// Good Morning
public class GameApplication extends Application implements Navigator {
    private Stage primaryStage;
    private GameConfig gameConfig;
    private ScreenManager screenManager;
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private GameplayScreen gameplayScreen;
    private GameSaveManager saveManager;
    private GameManager gameManager;
    private AudioManager audioManager;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Debug: Starting GameApplication");
        // Get basic stage properties
        System.out.println("Stage Details:");
        System.out.println("Width: " + primaryStage.getWidth());
        System.out.println("Height: " + primaryStage.getHeight());
        System.out.println("X Position: " + primaryStage.getX());
        System.out.println("Y Position: " + primaryStage.getY());
        System.out.println("Is Showing: " + primaryStage.isShowing());
        System.out.println("Is Resizable: " + primaryStage.isResizable());

        // Get scene information
        if (primaryStage.getScene() != null) {
            System.out.println("\nScene Details:");
            System.out.println("Scene Width: " + primaryStage.getScene().getWidth());
            System.out.println("Scene Height: " + primaryStage.getScene().getHeight());
            System.out.println("Root Node: " + primaryStage.getScene().getRoot().getClass().getSimpleName());
        }

        // Get window properties
        System.out.println("\nWindow Properties:");
        System.out.println("Title: " + primaryStage.getTitle());
        System.out.println("Style: " + primaryStage.getStyle());

        // Load font test
        System.out.println("Font Loader: " + FontLoader.TITLE_FONT);

        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);
        this.gameManager = GameManager.getInstance();

        ResourceManager.getInstance().getAudioManager().playMusic("Buckshot_Roulette_OST.mp3");
        
        initializeGame();
        showCutscene();

        // Starting Window
        primaryStage.show();
    }

    private GameConfig createGameConfig() {
        return DefaultGameConfig.getInstance();
    }

    private void initializeGame() {
        // Load configuration
        gameConfig.load();
        
        // Initialize managers
        saveManager = new GameSaveManager();
        
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
        
        // 1. ลบและรีเซ็ตประวัติแชท
        try {
            ChatHistoryManager chatManager =
                ChatHistoryManager.getInstance();
            
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
        
        // 7. สร้างและแสดง gameplay screen
        gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, newState);
        gameplayScreen.show();
        
        // ไม่บันทึกเกมตอนเริ่มใหม่ - ให้ GameplayScreen จัดการ
        System.out.println("เริ่มเกมใหม่เรียบร้อย");
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
        CutsceneScreen cutsceneScreen = new CutsceneScreen(gameConfig, screenManager, this);
        screenManager.switchScreen(cutsceneScreen);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 