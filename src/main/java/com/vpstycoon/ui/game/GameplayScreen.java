package com.vpstycoon.ui.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.object.RackObject;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeController;
import com.vpstycoon.game.thread.RequestGenerator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.application.Platform;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import com.vpstycoon.ui.game.rack.Rack;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;

public class GameplayScreen extends GameScreen {
    private GameState state;
    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private ArrayList<GameObject> gameObjects;
    private final Company company;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;
    private GameTimeController gameTimeController;
    private GameplayContentPane contentPane; // Store reference to content pane

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = ResourceManager.getInstance().getCompany(); // ใช้ company จาก ResourceManager
        this.chatSystem = new ChatSystem();
        this.requestManager = ResourceManager.getInstance().getRequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();
        
        // Register for settings change events
        subscribeToSettingsChanges();

        loadGame();
    }

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator, GameState gameState) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        
        // บันทึก state ที่ได้รับมา
        this.state = gameState;
        
        // ตรวจสอบว่า state มี gameObjects หรือไม่
        if (gameState.getGameObjects() != null) {
            this.gameObjects = new ArrayList<>(gameState.getGameObjects());
            
            // แสดง log จำนวน gameObjects เพื่อตรวจสอบ
            System.out.println("จำนวน GameObjects ที่ได้รับ: " + gameState.getGameObjects().size());
        } else {
            this.gameObjects = new ArrayList<>();
            System.out.println("ไม่มี GameObjects ใน GameState ที่ได้รับ");
        }
        
        // ใช้ company จาก GameState ไม่ใช่ ResourceManager
        if (gameState.getCompany() != null) {
            this.company = gameState.getCompany();
            // อัพเดต company ใน ResourceManager ด้วย
            ResourceManager.getInstance().setCompany(this.company);
            System.out.println("ตั้งค่า Company จาก GameState: Money = $" + this.company.getMoney());
        } else {
            this.company = ResourceManager.getInstance().getCompany();
            System.out.println("ใช้ค่า Company จาก ResourceManager: Money = $" + this.company.getMoney());
        }
        
        this.chatSystem = new ChatSystem();
        this.requestManager = ResourceManager.getInstance().getRequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();
        
        // Register for settings change events
        subscribeToSettingsChanges();

        // โหลดเกมโดยใช้ GameState ที่ได้รับมา
        loadGame(gameState);
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            state = saveManager.loadGame();
            if (state != null && state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                this.gameObjects = new ArrayList<>(state.getGameObjects());
                System.out.println("โหลดเกมสำเร็จ มี GameObjects จำนวน: " + state.getGameObjects().size());
            } else {
                System.out.println("Load failed or no objects found, initializing new game.");
                initializeGameObjects();
            }
        } else {
            System.out.println("No save file found, initializing new game.");
            state = new GameState();
            initializeGameObjects();
        }
    }

    private void loadGame(GameState gameState) {
        if (gameState != null) {
            this.state = gameState;
            
            if (gameState.getGameObjects() != null && !gameState.getGameObjects().isEmpty()) {
                this.gameObjects = new ArrayList<>(gameState.getGameObjects());
                System.out.println("โหลดเกมสำเร็จจาก GameState ที่ได้รับ มี GameObjects จำนวน: " + gameState.getGameObjects().size());
                
                // ตรวจสอบและปริ้นท์ข้อมูลเงินเพื่อเช็คว่าโหลดถูกต้อง
                if (gameState.getCompany() != null) {
                    System.out.println("โหลด Company พร้อมเงิน: $" + gameState.getCompany().getMoney());
                }
            } else {
                System.out.println("GameState ไม่มี GameObjects, เริ่มเกมใหม่");
                initializeGameObjects();
            }
            
            // ถ้านี่เป็นการเริ่มเกมใหม่ (New Game) และไม่ใช่การ Continue
            if (gameState.getCompany() != null && gameState.getCompany().getMoney() == 10000 && 
                gameState.getLocalDateTime() != null && 
                gameState.getLocalDateTime().getYear() == 2000 &&
                gameState.getLocalDateTime().getMonthValue() == 1 &&
                gameState.getLocalDateTime().getDayOfMonth() == 1) {
                
                System.out.println("เริ่มเกมใหม่ ไม่ต้องบันทึกทันที");
                // ไม่ต้องบันทึกทันที
            } 
            // ลบหรือคอมเม้นท์ส่วนนี้เพื่อไม่ให้บันทึกทันทีหลังโหลด
            // else {
            //    // Save the loaded game state to ensure continuity
            //    System.out.println("บันทึกเกมหลังจากโหลด เพื่อให้แน่ใจว่าข้อมูลต่อเนื่อง");
            //    gameFlowManager.saveGame();
            // }
        } else {
            System.out.println("Provided game state is null, initializing new game.");
            this.state = new GameState();
            initializeGameObjects();
        }
    }

    private synchronized void initializeGameObjects() {
        int screenWidth = config.getResolution().getWidth();
        int screenHeight = config.getResolution().getHeight();
        int centerX = screenWidth / 2;
        int topMargin = screenHeight / 12;
        int spacing = screenWidth / 10;

        gameObjects.clear();

        RackObject deploy = new RackObject("deploy", "Deploy", 0, 0);
        deploy.setGridPosition(centerX - spacing * 2, topMargin);
        gameObjects.add(deploy);

        RackObject network = new RackObject("network", "Network", 0, 0);
        network.setGridPosition(centerX - spacing, topMargin);
        gameObjects.add(network);

        RackObject security = new RackObject("security", "Security", 0, 0);
        security.setGridPosition(centerX, topMargin);
        gameObjects.add(security);

        RackObject marketing = new RackObject("marketing", "Marketing", 0, 0);
        marketing.setGridPosition(centerX + spacing, topMargin);
        gameObjects.add(marketing);

        state.setGameObjects(gameObjects);

        RequestGenerator requestGenerator = new RequestGenerator(requestManager);

        requestGenerator.start();
        gameTimeController = ResourceManager.getInstance().getGameTimeController();
        gameTimeController.startTime();
    }

    @Override
    protected Region createContent() {
        // สร้าง GameplayContentPane ซึ่งเป็นหน้าหลักของเกม
        contentPane = new GameplayContentPane(
                this.gameObjects,
                this.navigator,
                this.chatSystem,
                this.requestManager,
                this.vpsManager,
                this.gameFlowManager,
                this.debugOverlayManager,
                this.company,
                ResourceManager.getInstance().getRack()
        );
        
        // ตั้งเวลาบันทึกเกมหลังจากโหลดเสร็จสมบูรณ์ 5 วินาที
        // จะได้มั่นใจว่าทุกอย่างโหลดเรียบร้อยแล้ว
        Platform.runLater(() -> {
            try {
                // สร้าง timer task ที่จะทำงานหลังจาก delay 5 วินาที
                new Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            // ทำงานบน JavaFX thread
                            Platform.runLater(() -> {
                                // ยืนยันว่ายังอยู่ในหน้าเกม
                                if (gameFlowManager != null) {
                                    // ตรวจสอบว่าข้อมูล Rack โหลดสมบูรณ์แล้วหรือยัง
                                    Rack rack = ResourceManager.getInstance().getRack();
                                    if (rack != null) {
                                        System.out.println("กำลังตรวจสอบความพร้อมของข้อมูลก่อนบันทึกเกม...");
                                        System.out.println("- จำนวน rack: " + rack.getMaxRacks());
                                        System.out.println("- rack index ปัจจุบัน: " + rack.getCurrentRackIndex());
                                        System.out.println("- จำนวน slot ที่ปลดล็อค: " + rack.getUnlockedSlotUnits());
                                        
                                        // แสดงข้อมูล VPS ที่ติดตั้งในทุก rack
                                        List<VPSOptimization> allVPS = rack.getAllInstalledVPS();
                                        System.out.println("- จำนวน VPS ทั้งหมดที่ติดตั้งในทุก rack: " + allVPS.size());
                                        
                                        // ตรวจสอบข้อมูลใน GameManager ว่าโหลดสมบูรณ์หรือไม่
                                        GameManager gameManager = GameManager.getInstance();
                                        System.out.println("- จำนวน VPS ที่ติดตั้งใน GameManager: " + gameManager.getInstalledServers().size());
                                        System.out.println("- จำนวน VPS ใน Inventory: " + gameManager.getVpsInventory().getSize());
                                        
                                        // ยังไม่ต้องบันทึกทันที ให้ผู้เล่นบันทึกเองหลังจากเริ่มเล่นและสำรวจดูแล้ว
                                        // หรือถ้าจำเป็นต้องบันทึก ให้ตรวจสอบเงื่อนไขให้ชัดเจนว่าข้อมูลพร้อมบันทึกจริงๆ แล้ว
                                        System.out.println("ยกเลิกการบันทึกเกมอัตโนมัติหลังโหลด เพื่อป้องกันข้อมูลไม่สมบูรณ์");
                                        System.out.println("กรุณาบันทึกเกมด้วยตนเองหลังจากเริ่มเล่นเกม");
                                        
                                        // อัปเดต UI สำหรับการแสดงผล Rack หากจำเป็น
                                        if (contentPane != null) {
                                            // ไม่ต้องเรียกใช้ refreshRackDisplay เพราะไม่มีเมธอดนี้
                                        }
                                        
                                        // ปิดการบันทึกอัตโนมัติ
                                        // gameFlowManager.saveGame(); // ปิดการบันทึกอัตโนมัติหลังจากโหลด
                                    } else {
                                        System.out.println("ไม่พบข้อมูล Rack ในระบบ - ยกเลิกการบันทึกอัตโนมัติ");
                                    }
                                }
                            });
                        }
                    }, 
                    500 // delay 5 วินาที
                );
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการตั้งเวลาบันทึกเกม: " + e.getMessage());
            }
        });
        
        return contentPane;
    }

    /**
     * Properly release resources held by this screen when it's no longer needed.
     * This helps reduce memory usage and improve performance when transitioning between screens.
     */
    public void release() {
        try {
            System.out.println("Releasing GameplayScreen resources");
            
            // Stop any running timers or threads
            if (gameTimeController != null) {
                try {
                    gameTimeController.stopTime();
                } catch (Exception e) {
                    System.err.println("Error stopping game time controller: " + e.getMessage());
                }
            }
            
            // Clear game objects to free memory
            if (gameObjects != null) {
                gameObjects.clear();
            }
            
            // Explicitly trigger garbage collection to free memory
            // (only use this in specific memory-critical scenarios)
            System.gc();
            
            System.out.println("GameplayScreen resources released");
        } catch (Exception e) {
            System.err.println("Error releasing GameplayScreen resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to settings change events to update the UI when resolution changes
     */
    private void subscribeToSettingsChanges() {
        GameEventBus.getInstance().subscribe(
            SettingsChangedEvent.class,
            event -> Platform.runLater(() -> {
                // Update content pane with new resolution
                if (contentPane != null) {
                    System.out.println("Resolution changed, updating game screen layout");
                    contentPane.updateResolution();
                }
            })
        );
    }
}