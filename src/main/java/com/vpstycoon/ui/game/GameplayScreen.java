package com.vpstycoon.ui.game;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.GameManager;
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
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.game.rack.Rack;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.application.Platform;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

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
    private GameplayContentPane contentPane; 

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = ResourceManager.getInstance().getCompany(); 
        this.chatSystem = new ChatSystem();
        this.requestManager = ResourceManager.getInstance().getRequestManager();
        this.vpsManager = new VPSManager();
        this.gameFlowManager = new GameFlowManager(saveManager, gameObjects);
        this.debugOverlayManager = new DebugOverlayManager();
        subscribeToSettingsChanges();
        loadGame();
    }

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator, GameState gameState) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.state = gameState;
        if (gameState.getGameObjects() != null) {
            this.gameObjects = new ArrayList<>(gameState.getGameObjects());
            System.out.println("จำนวน GameObjects ที่ได้รับ: " + gameState.getGameObjects().size());
        } else {
            this.gameObjects = new ArrayList<>();
            System.out.println("ไม่มี GameObjects ใน GameState ที่ได้รับ");
        }
        if (gameState.getCompany() != null) {
            this.company = gameState.getCompany();
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
        subscribeToSettingsChanges();
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
                if (gameState.getCompany() != null) {
                    System.out.println("โหลด Company พร้อมเงิน: $" + gameState.getCompany().getMoney());
                }
            } else {
                System.out.println("GameState ไม่มี GameObjects, เริ่มเกมใหม่");
                initializeGameObjects();
            }
            if (gameState.getCompany() != null && gameState.getCompany().getMoney() == 10000 && 
                gameState.getLocalDateTime() != null && 
                gameState.getLocalDateTime().getYear() == 2000 &&
                gameState.getLocalDateTime().getMonthValue() == 1 &&
                gameState.getLocalDateTime().getDayOfMonth() == 1) {
                System.out.println("เริ่มเกมใหม่ ไม่ต้องบันทึกทันที");
            } 
            gameTimeController = ResourceManager.getInstance().getGameTimeController();
            if (gameTimeController != null) {
                if (gameState.getLocalDateTime() != null) {
                    System.out.println("กำลังตั้งค่าเวลาเกมจาก GameState: " + gameState.getLocalDateTime() + 
                                      " (GameTimeMs: " + gameState.getGameTimeMs() + ")");
                    gameTimeController.resetTime(gameState.getLocalDateTime());
                }
                
                gameTimeController.startTime();
                System.out.println("เริ่มการเดินเวลาเกมหลังจากโหลด GameState");
            } else {
                System.out.println("ไม่พบ gameTimeController ใน ResourceManager");
            }
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

        try {
            System.out.println("================= เริ่มการสร้าง RequestGenerator ใน GameplayScreen =================");
            RequestGenerator existingGenerator = ResourceManager.getInstance().getRequestGenerator();
            if (existingGenerator != null) {
                if (existingGenerator.isAlive()) {
                    System.out.println("❌ พบ RequestGenerator เดิมกำลังทำงานอยู่ (Thread ID: " + existingGenerator.getId() + ")");
                    existingGenerator.stopGenerator();
                    System.out.println("✅ หยุด RequestGenerator เดิมแล้ว");
                } else {
                    System.out.println("❌ พบ RequestGenerator เดิมไม่ได้ทำงาน");
                }
            } else {
                System.out.println("❌ ไม่พบ RequestGenerator ใน ResourceManager");
            }
            
            RequestManager requestManager = ResourceManager.getInstance().getRequestManager();
            if (requestManager == null) {
                System.out.println("❌ ไม่พบ RequestManager - กำลังสร้างใหม่");
                requestManager = new RequestManager(ResourceManager.getInstance().getCompany());
                ResourceManager.getInstance().setRequestManager(requestManager);
            } else {
                System.out.println("✅ พบ RequestManager พร้อมใช้งาน");
            }
            
            RequestGenerator requestGenerator = new RequestGenerator(requestManager);
            ResourceManager.getInstance().setRequestGenerator(requestGenerator);
            requestGenerator.start();
            
            if (requestGenerator.isAlive()) {
                System.out.println("✅ RequestGenerator ใหม่เริ่มทำงานแล้ว (Thread ID: " + requestGenerator.getId() + ")");
            } else {
                System.out.println("❌ RequestGenerator ใหม่ไม่ได้ทำงาน");
            }
            
            System.out.println("RequestGenerator ทำงานเรียบร้อยแล้ว");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้าง RequestGenerator: " + e.getMessage());
            e.printStackTrace();
        }
        
        gameTimeController = ResourceManager.getInstance().getGameTimeController();
        gameTimeController.startTime();
    }

    @Override
    protected Region createContent() {
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
        
        Platform.runLater(() -> {
            try {
                new Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (gameFlowManager != null) {
                                    Rack rack = ResourceManager.getInstance().getRack();
                                    if (rack != null) {
                                        System.out.println("ข้อมูล rack:");
                                        System.out.println("- rack index ปัจจุบัน: " + rack.getCurrentRackIndex());
                                        System.out.println("- จำนวน slot ที่ปลดล็อค: " + rack.getUnlockedSlotUnits());
                                        
                                        List<VPSOptimization> allVPS = rack.getAllInstalledVPS();
                                        System.out.println("- จำนวน VPS ทั้งหมดที่ติดตั้งในทุก rack: " + allVPS.size());
                                        
                                        GameManager gameManager = GameManager.getInstance();
                                        System.out.println("- จำนวน VPS ที่ติดตั้งใน GameManager: " + gameManager.getInstalledServers().size());
                                        System.out.println("- จำนวน VPS ใน Inventory: " + gameManager.getVpsInventory().getSize());
                                        
                                        System.out.println("ยกเลิกการบันทึกเกมอัตโนมัติหลังโหลด เพื่อป้องกันข้อมูลไม่สมบูรณ์");
                                        System.out.println("กรุณาบันทึกเกมด้วยตนเองหลังจากเริ่มเล่นเกม");
                                        
                                        if (contentPane != null) {
                                            
                                        }
                                    } else {
                                        System.out.println("ไม่พบข้อมูล Rack ในระบบ - ยกเลิกการบันทึกอัตโนมัติ");
                                    }
                                }
                            });
                        }
                    }, 
                    500 
                );
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการตั้งเวลาบันทึกเกม: " + e.getMessage());
            }
        });
        
        return contentPane;
    }

    public void release() {
        try {
            System.out.println("Releasing GameplayScreen resources");
            
            if (gameTimeController != null) {
                try {
                    gameTimeController.stopTime();
                } catch (Exception e) {
                    System.err.println("Error stopping game time controller: " + e.getMessage());
                }
            }
            
            
            try {
                RequestGenerator generator = ResourceManager.getInstance().getRequestGenerator();
                if (generator != null && generator.isAlive()) {
                    System.out.println("หยุด RequestGenerator เมื่อออกจากหน้าเกม (Thread ID: " + generator.getId() + ")");
                    generator.pauseGenerator();
                }
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการหยุด RequestGenerator: " + e.getMessage());
            }
            
            if (gameObjects != null) {
                gameObjects.clear();
            }
            
            System.gc();
            
            System.out.println("GameplayScreen resources released");
        } catch (Exception e) {
            System.err.println("Error releasing GameplayScreen resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void subscribeToSettingsChanges() {
        GameEventBus.getInstance().subscribe(
            SettingsChangedEvent.class,
            event -> Platform.runLater(() -> {
                if (contentPane != null) {
                    System.out.println("Resolution changed, updating game screen layout");
                    contentPane.updateResolution();
                }
            })
        );
    }

    @Override
    public void show() {
        super.show();
        validateRequestGenerator();
        System.out.println("GameplayScreen - show() พร้อมใช้งาน RequestGenerator เรียบร้อยแล้ว");
    }
    
    private void validateRequestGenerator() {
        try {
            System.out.println("GameplayScreen - กำลังตรวจสอบ RequestGenerator เมื่อแสดงหน้าเกม");
            RequestGenerator generator = ResourceManager.getInstance().getRequestGenerator();
            
            if (generator == null) {
                System.out.println("GameplayScreen - ไม่พบ RequestGenerator - กำลังสร้างใหม่");
                RequestManager requestManager = ResourceManager.getInstance().getRequestManager();
                if (requestManager == null) {
                    System.out.println("GameplayScreen - ไม่พบ RequestManager - กำลังสร้างใหม่");
                    requestManager = new RequestManager(ResourceManager.getInstance().getCompany());
                    ResourceManager.getInstance().setRequestManager(requestManager);
                }
                
                generator = new RequestGenerator(requestManager);
                ResourceManager.getInstance().setRequestGenerator(generator);
                generator.start();
                System.out.println("GameplayScreen - สร้าง RequestGenerator ใหม่และเริ่มทำงานแล้ว (Thread ID: " + generator.getId() + ")");
            } else if (!generator.isAlive()) {
                System.out.println("GameplayScreen - พบ RequestGenerator แต่ไม่ทำงาน - กำลังรีสตาร์ท");
                RequestManager requestManager = ResourceManager.getInstance().getRequestManager();
                if (requestManager == null) {
                    System.out.println("GameplayScreen - ไม่พบ RequestManager - กำลังสร้างใหม่");
                    requestManager = new RequestManager(ResourceManager.getInstance().getCompany());
                    ResourceManager.getInstance().setRequestManager(requestManager);
                }
                
                generator = new RequestGenerator(requestManager);
                ResourceManager.getInstance().setRequestGenerator(generator);
                generator.start();
                System.out.println("GameplayScreen - รีสตาร์ท RequestGenerator แล้ว (Thread ID: " + generator.getId() + ")");
            } else if (generator.isPaused()) {
                System.out.println("GameplayScreen - RequestGenerator ถูกพักการทำงาน - กำลังเริ่มต่อ");
                generator.resumeGenerator();
                System.out.println("GameplayScreen - เริ่มการทำงาน RequestGenerator ต่อแล้ว (Thread ID: " + generator.getId() + ")");
            } else {
                System.out.println("GameplayScreen - RequestGenerator กำลังทำงานอยู่ (Thread ID: " + generator.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println("GameplayScreen - เกิดข้อผิดพลาดในการตรวจสอบ RequestGenerator: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

