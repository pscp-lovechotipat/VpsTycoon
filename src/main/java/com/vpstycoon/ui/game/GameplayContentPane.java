package com.vpstycoon.ui.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeController;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSSize;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.components.GameMenuBar;
import com.vpstycoon.ui.game.components.InGameMarketMenuBar;
import com.vpstycoon.ui.game.components.RoomObjectsLayer;
import com.vpstycoon.ui.game.desktop.SimulationDesktopUI;
import com.vpstycoon.ui.game.desktop.SkillPointsWindow;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.game.handlers.KeyEventHandler;
import com.vpstycoon.ui.game.handlers.ZoomPanHandler;
import com.vpstycoon.ui.game.inventory.VPSInventoryUI;
import com.vpstycoon.ui.game.desktop.MarketWindow;
import com.vpstycoon.ui.game.notification.NotificationController;
import com.vpstycoon.ui.game.notification.NotificationModel;
import com.vpstycoon.ui.game.notification.NotificationView;
import com.vpstycoon.ui.game.notification.center.CenterNotificationController;
import com.vpstycoon.ui.game.notification.center.CenterNotificationModel;
import com.vpstycoon.ui.game.notification.center.CenterNotificationView;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationController;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationModel;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationView;
import com.vpstycoon.ui.game.rack.Rack;
import com.vpstycoon.ui.game.rack.RackManagementUI;
import com.vpstycoon.ui.game.status.date.DateController;
import com.vpstycoon.ui.game.status.date.DateModel;
import com.vpstycoon.ui.game.status.date.DateView;
import com.vpstycoon.ui.game.status.money.MoneyController;
import com.vpstycoon.ui.game.status.money.MoneyModel;
import com.vpstycoon.ui.game.status.money.MoneyUI;
import com.vpstycoon.ui.game.vps.*;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class GameplayContentPane extends BorderPane {
    private final StackPane rootStack;

    private final DateView dateView;
    private final DateController dateController;
    private final DateModel dateModel;

    private final MoneyModel moneyModel;
    private final MoneyUI moneyUI;
    private final MoneyController moneyController;

    private Group worldGroup;
    private final StackPane gameArea;
    private final GameMenuBar menuBar;

    private final InGameMarketMenuBar inGameMarketMenuBar;
    private final List<GameObject> gameObjects;

    private final Navigator navigator;
    private final ChatSystem chatSystem;

    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Rack rack;

    private final GameFlowManager gameFlowManager;

    private final DebugOverlayManager debugOverlayManager;
    private final Company company;
    private RoomObjectsLayer roomObjects;

    private ZoomPanHandler zoomPanHandler;
    private KeyEventHandler keyEventHandler;

    private boolean showDebug = false;

    private final RackManagementUI rackManagementUI;
    private final VPSCreationUI vpsCreationUI;
    private final VPSInfoUI vpsInfoUI;
    private final VMCreationUI vmCreationUI;
    private final VMInfoUI vmInfoUI;
    private final VMEditUI vmEditUI;
    private final SimulationDesktopUI simulationDesktopUI;

    private SkillPointsSystem skillPointsSystem;

    private final VPSInventoryUI vpsInventoryUI;

    private AudioManager audioManager;

    private final VPSInventory vpsInventory = new VPSInventory();

    private final GameTimeController gameTimeController;

    private final ResourceManager resourceManager = ResourceManager.getInstance();

    private boolean isTransitioning = false;

    // Constructor
    public GameplayContentPane(
            List<GameObject> gameObjects, Navigator navigator, ChatSystem chatSystem,
            RequestManager requestManager, VPSManager vpsManager, GameFlowManager gameFlowManager,
            DebugOverlayManager debugOverlayManager, Company company, Rack rack) {
        this.gameObjects = gameObjects;
        this.navigator = navigator;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.gameFlowManager = gameFlowManager;
        this.debugOverlayManager = debugOverlayManager;
        this.company = company;
        this.rack = rack;

        // Initialize UI Components
        rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);
        
        gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);
        rootStack.getChildren().add(gameArea);

        // เตรียม rack เริ่มต้น (ถ้ายังไม่มี)
        initializeBasicRack();

        this.gameTimeController = ResourceManager.getInstance().getGameTimeController();

        this.rackManagementUI = new RackManagementUI(this);
        this.vpsCreationUI = new VPSCreationUI(this);
        this.vpsInfoUI = new VPSInfoUI(this);
        this.vmCreationUI = new VMCreationUI(this);
        this.vmInfoUI = new VMInfoUI(this);
        this.vmEditUI = new VMEditUI(this);
        this.simulationDesktopUI = new SimulationDesktopUI(this);
        this.vpsInventoryUI = new VPSInventoryUI(this);

        this.moneyModel = new MoneyModel(ResourceManager.getInstance().getCompany().getMoney(),
                ResourceManager.getInstance().getCompany().getRating());
        this.moneyUI = new MoneyUI(this, moneyModel);
        this.moneyController = new MoneyController(moneyModel, moneyUI);

        // ตรวจสอบค่า LocalDateTime ก่อนสร้าง DateModel
        LocalDateTime initialDate = null;
        if (ResourceManager.getInstance().getCurrentState() != null) {
            initialDate = ResourceManager.getInstance().getCurrentState().getLocalDateTime();
        }
        
        if (initialDate == null) {
            // ถ้าเป็น null ให้ใช้ค่าเริ่มต้น
            initialDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            System.out.println("วันที่เริ่มต้นเป็น null กำหนดเป็น: " + initialDate);
        } else {
            System.out.println("ใช้วันที่จาก ResourceManager: " + initialDate);
        }
        
        this.dateModel = new DateModel(initialDate, this);
        this.dateView = new DateView(this, dateModel);
        this.dateController = new DateController(dateModel, dateView);

        this.menuBar = new GameMenuBar(this);

        this.inGameMarketMenuBar = new InGameMarketMenuBar(this, vpsManager);

        // ซิงค์ข้อมูลกับ GameManager
        syncWithGameManager();

        setupUI();
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();
        setCenter(rootStack);
        setupDebugFeatures();

        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        // Make sure game time state matches music state at startup
        if (roomObjects != null && !roomObjects.getRun()) {
            // If music is off at startup, also stop the game time
            gameTimeController.stopTime();
            System.out.println("Game time stopped at startup (music off)");
        } else {
            // If music is on at startup, ensure game time is running
            gameTimeController.startTime();
            System.out.println("Game time started at startup (music on)");
        }
    }

    /**
     * เตรียม rack เริ่มต้นสำหรับผู้เล่นใหม่
     */
    private void initializeBasicRack() {
        // สร้าง rack เริ่มต้นเท่านั้น ถ้ายังไม่มี
        if (rack.getMaxRacks() == 0) {
            rack.addRack(10);  // Add a rack with 10 slots
            rack.upgrade();    // Unlock the first slot
            System.out.println("เพิ่ม rack เริ่มต้นพร้อม 1 slot ปลดล็อคแล้ว");
        }
    }

    private synchronized void setupUI() {
        System.out.println("กำลังตั้งค่า UI ใหม่...");
        
        // Use cached image to avoid reloading
        Image backgroundImage = com.vpstycoon.ui.game.components.RoomObjectsLayer.loadImage("/images/rooms/room.gif");
        
        Pane backgroundLayer = createBackgroundLayer(backgroundImage);

        this.rootStack.getChildren().clear();
        System.out.println("ล้าง rootStack เรียบร้อย");

        // Create room objects with optimized image loading
        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo, this::openKeroro, this::openMusicBox, this::openMusicStop);
        System.out.println("สร้าง roomObjects ใหม่เรียบร้อย");
        
        // Create the world group with all components
        worldGroup = new Group(backgroundLayer, roomObjects.getServerLayer(), roomObjects.getMonitorLayer(), 
                roomObjects.getKeroroLayer(), roomObjects.getMusicBoxLayer(), roomObjects.getMusicStopLayer());
        System.out.println("สร้าง worldGroup ใหม่เรียบร้อย");
        
        // Add world group to game area
        gameArea.getChildren().add(worldGroup);
        System.out.println("เพิ่ม worldGroup เข้า gameArea เรียบร้อย");

        // Get debug overlay
        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        
        // Add all UI components to root stack
        rootStack.getChildren().clear();
        rootStack.getChildren().add(gameArea);
        
        // เพิ่มแต่ละ UI element เข้าไปใน rootStack อย่างชัดเจน
        rootStack.getChildren().addAll(
                moneyUI, menuBar, dateView,
                inGameMarketMenuBar,
                resourceManager.getMouseNotificationView(),
                resourceManager.getNotificationView(),
                resourceManager.getCenterNotificationView(),
                debugOverlay
        );
        
        System.out.println("เพิ่ม UI elements เข้า rootStack เรียบร้อย (" + 
                           (rootStack.getChildren().size() - 1) + " elements)");

        // Configure visibility and properties
        menuBar.setVisible(true);
        menuBar.setPickOnBounds(false);

        inGameMarketMenuBar.setVisible(true);
        inGameMarketMenuBar.setPickOnBounds(false);

        resourceManager.getNotificationView().setMaxWidth(400);
        resourceManager.getNotificationView().setPickOnBounds(false);

        resourceManager.getCenterNotificationView().setPickOnBounds(false);
        StackPane.setAlignment(resourceManager.getCenterNotificationView(), Pos.CENTER);

        moneyUI.setPickOnBounds(false);
        moneyUI.setVisible(true);

        dateView.setVisible(true);

        StackPane.setMargin(this.moneyUI, new Insets(40));

        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);
        StackPane.setAlignment(resourceManager.getNotificationView(), Pos.TOP_RIGHT);
        StackPane.setAlignment(moneyUI, Pos.TOP_LEFT);

        // Start debug timer
        debugOverlayManager.startTimer();
        
        // Setup zoom and pan handler
        zoomPanHandler = new ZoomPanHandler(worldGroup, gameArea, debugOverlayManager, showDebug);
        zoomPanHandler.setup();

        // Initialize game events
        resourceManager.initializeGameEvent(this);

        // Set background color
        setStyle("-fx-background-color: #000000;");
    }

    private Pane createBackgroundLayer(Image backgroundImage) {
        Pane backgroundLayer = new Pane();

        double scaleFactor = 0.26;
        backgroundLayer.setPrefWidth(backgroundImage.getWidth() * scaleFactor);
        backgroundLayer.setPrefHeight(backgroundImage.getHeight() * scaleFactor);

        // Use CSS to set the background with the cached image path
        backgroundLayer.setStyle("""
        -fx-background-image: url("/images/rooms/room.gif");
        -fx-background-size: cover;
        -fx-background-repeat: no-repeat;
        -fx-background-position: center;
    """);
        return backgroundLayer;
    }

    // Overloaded method to support both preloaded and on-demand loading
    private Pane createBackgroundLayer() {
        // Use cached image instead of creating a new one
        Image backgroundImage = com.vpstycoon.ui.game.components.RoomObjectsLayer.loadImage("/images/rooms/room.gif");
        return createBackgroundLayer(backgroundImage);
    }

    private void setupDebugFeatures() {
        setOnMouseMoved(e -> {
            if (showDebug) {
                debugOverlayManager.updateMousePosition(e.getX(), e.getY());
                debugOverlayManager.updateGameInfo(rootStack);
            }
        });
    }

    // === Notification Methods ===
    public void pushNotification(String title, String content) {
        resourceManager.pushNotification(title, content);
    }

    public void pushMouseNotification(String content) {
        resourceManager.pushMouseNotification(content);
    }

    public void pushCenterNotification(String title, String content) {
        resourceManager.pushCenterNotification(title, content);
    }

    public void pushCenterNotification(String title, String content, String image) {
        resourceManager.pushCenterNotification(title, content, image);
    }

    // === UI Opening Methods ===
    public void openRackInfo() {
        rackManagementUI.openRackInfo();
    }

    public void openCreateVPSPage() {
        vpsCreationUI.openCreateVPSPage();
    }

    public void openVPSInfoPage(VPSOptimization vps) {
        vpsInfoUI.openVPSInfoPage(vps);
    }

    public void openCreateVMPage(VPSOptimization vps) {
        vmCreationUI.openCreateVMPage(vps);
    }

    public void openVMInfoPage(VPSOptimization.VM vm, VPSOptimization vps) {
        vmInfoUI.openVMInfoPage(vm, vps);
    }

    public void openEditVMPage(VPSOptimization.VM vm, VPSOptimization vps) {
        vmEditUI.openEditVMPage(vm, vps);
    }

    public void openMarket() {
        // Store current UI visibility state
        final boolean menuBarWasVisible = menuBar.isVisible();
        final boolean marketMenuBarWasVisible = inGameMarketMenuBar.isVisible();
        final boolean moneyUIWasVisible = moneyUI.isVisible();
        final boolean dateViewWasVisible = dateView.isVisible();

        MarketWindow marketWindow = new MarketWindow(
            () -> {
                getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                
                // Always restore previous visibility state, not just set to true
                menuBar.setVisible(menuBarWasVisible);
                inGameMarketMenuBar.setVisible(marketMenuBarWasVisible);
                moneyUI.setVisible(moneyUIWasVisible);
                dateView.setVisible(dateViewWasVisible);
            },
            () -> {
                getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                
                // Always restore previous visibility state
                menuBar.setVisible(menuBarWasVisible);
                inGameMarketMenuBar.setVisible(marketMenuBarWasVisible);
                moneyUI.setVisible(moneyUIWasVisible);
                dateView.setVisible(dateViewWasVisible);
            },
            vpsManager,
            this
        );
        getGameArea().getChildren().add(marketWindow);
    }

    public void openSimulationDesktop() {
        audioManager.playSoundEffect("click_app.wav");
        simulationDesktopUI.openSimulationDesktop();
    }

    public void openVPSInventory() {
        vpsInventoryUI.openInventory();
    }

    public void openSkillPointsWindow() {
        SkillPointsWindow skillPointsWindow = new SkillPointsWindow(ResourceManager.getInstance().getSkillPointsSystem(), () -> {
            getGameArea().getChildren().remove(getGameArea().getChildren().size() - 1);
        });

        getGameArea().getChildren().add(skillPointsWindow);
    }

    public void openKeroro() {
        System.out.println("Open Keroro");
        audioManager.playSoundEffect("keroro_sound.mp3");
        pushMouseNotification("Keroro");
    }

    public void openMusicBox() {
        if (roomObjects.getRun()) {
            roomObjects.getMusicBoxLayer().setVisible(false);
            roomObjects.getMusicStopLayer().setVisible(true);
            audioManager.pauseMusic();
            // Remove game time pausing functionality
            System.out.println("Music paused");
            pushNotification("Music Paused", "Music is now paused. Click the music box to resume.");
            roomObjects.setRun(false);
        }
    }

    public void openMusicStop(){
        if (!roomObjects.getRun()) {
            roomObjects.getMusicBoxLayer().setVisible(true);
            roomObjects.getMusicStopLayer().setVisible(false);
            audioManager.resumeMusic();
            // Remove game time resuming functionality
            System.out.println("Music resumed");
            pushNotification("Music Resumed", "Music is now playing.");
            roomObjects.setRun(true);
        }
    }

    /**
     * Hide all menu elements (used when opening other windows)
     */
    public void hideMenus() {
        menuBar.setVisible(false);
        inGameMarketMenuBar.setVisible(false);
        moneyUI.setVisible(false);
        dateView.setVisible(false);
    }

    // === Navigation Methods ===
    public void returnToRoom() {
        // ทำให้การอ้างถึง rackManagementUI ปลอดภัยขึ้นโดยตรวจสอบก่อนเรียกเมธอด
        try {
            if (rackManagementUI != null) {
                rackManagementUI.dispose();
                System.out.println("ยกเลิกการลงทะเบียน listeners ของ RackManagementUI เรียบร้อย");
            }
        } catch (Exception ex) {
            System.err.println("เกิดข้อผิดพลาดในการ dispose RackManagementUI: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        // Use Platform.runLater to move UI operations off the main thread
        javafx.application.Platform.runLater(() -> {
            try {
                // ป้องกันการเรียกซ้ำโดยการใช้ flag
                if (isTransitioning) {
                    System.out.println("กำลังเปลี่ยนแปลง UI อยู่แล้ว กรุณารอสักครู่...");
                    return;
                }
                isTransitioning = true;
                
                // ทำความสะอาด rootStack - เก็บเฉพาะ child แรก
                while (rootStack.getChildren().size() > 1) {
                    rootStack.getChildren().remove(rootStack.getChildren().size() - 1);
                }
                
                // Create fade transition for smooth visual effect
                StackPane overlay = new StackPane();
                overlay.setStyle("-fx-background-color: black;");
                overlay.setOpacity(0);
                
                // Add progress indicator
                javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setMaxSize(50, 50);
                progressIndicator.setVisible(false);
                overlay.getChildren().add(progressIndicator);
                
                rootStack.getChildren().add(overlay);
                
                // Create fade in transition
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlay);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(0.7);
                fadeIn.setOnFinished(e -> {
                    progressIndicator.setVisible(true);
                    
                    // Execute UI operations in background thread
                    Thread setupThread = new Thread(() -> {
                        try {
                            // Clear current content
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    // บันทึกข้อมูลสถานะปัจจุบันก่อนล้าง
                                    gameArea.getChildren().clear();
                                } catch (Exception ex) {
                                    System.err.println("เกิดข้อผิดพลาดในการล้าง gameArea: " + ex.getMessage());
                                }
                            });
                            
                            // Setup UI on JavaFX thread (avoid recreating all UI components)
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    // If world group and room objects already exist, reuse them
                                    if (worldGroup != null && roomObjects != null) {
                                        // Just add existing components back to game area
                                        gameArea.getChildren().add(worldGroup);
                                    } else {
                                        // Only setup UI if components don't already exist
                                        setupUI();
                                    }
                                    
                                    // ถ้า UI elements ไม่อยู่ใน rootStack ให้เพิ่มกลับเข้าไปใหม่
                                    rootStack.getChildren().removeIf(node -> 
                                        node instanceof javafx.scene.control.ProgressIndicator ||
                                        node instanceof StackPane && node != gameArea);
                                    
                                    // เพิ่ม UI elements กลับเข้า rootStack ทุกครั้ง
                                    if (!rootStack.getChildren().contains(menuBar)) {
                                        rootStack.getChildren().add(menuBar);
                                        System.out.println("เพิ่ม menuBar เข้า rootStack");
                                    }
                                    
                                    if (!rootStack.getChildren().contains(moneyUI)) {
                                        rootStack.getChildren().add(moneyUI);
                                        System.out.println("เพิ่ม moneyUI เข้า rootStack");
                                    }
                                    
                                    if (!rootStack.getChildren().contains(dateView)) {
                                        rootStack.getChildren().add(dateView);
                                        System.out.println("เพิ่ม dateView เข้า rootStack");
                                    }
                                    
                                    if (!rootStack.getChildren().contains(inGameMarketMenuBar)) {
                                        rootStack.getChildren().add(inGameMarketMenuBar);
                                        System.out.println("เพิ่ม inGameMarketMenuBar เข้า rootStack");
                                    }
                                    
                                    if (!rootStack.getChildren().contains(resourceManager.getNotificationView())) {
                                        rootStack.getChildren().add(resourceManager.getNotificationView());
                                    }
                                    
                                    if (!rootStack.getChildren().contains(resourceManager.getCenterNotificationView())) {
                                        rootStack.getChildren().add(resourceManager.getCenterNotificationView());
                                    }
                                    
                                    if (!rootStack.getChildren().contains(resourceManager.getMouseNotificationView())) {
                                        rootStack.getChildren().add(resourceManager.getMouseNotificationView());
                                    }
                                    
                                    // แสดงผล UI elements โดยตรง
                                    menuBar.setVisible(true);
                                    inGameMarketMenuBar.setVisible(true); 
                                    moneyUI.setVisible(true);
                                    dateView.setVisible(true);
                                    
                                    // ตั้งค่าตำแหน่งของ UI elements
                                    StackPane.setAlignment(menuBar, Pos.TOP_CENTER);
                                    StackPane.setAlignment(moneyUI, Pos.TOP_LEFT);
                                    StackPane.setAlignment(resourceManager.getNotificationView(), Pos.TOP_RIGHT);
                                    StackPane.setAlignment(resourceManager.getCenterNotificationView(), Pos.CENTER);
                                    
                                    // บันทึกจำนวน UI ใน rootStack
                                    System.out.println("จำนวน elements ใน rootStack: " + rootStack.getChildren().size());
                                    
                                    // Create fade out transition
                                    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), overlay);
                                    fadeOut.setFromValue(0.7);
                                    fadeOut.setToValue(0);
                                    fadeOut.setOnFinished(event -> {
                                        try {
                                            rootStack.getChildren().remove(overlay);
                                            isTransitioning = false; // คืนค่า flag
                                        } catch (Exception ex) {
                                            System.err.println("เกิดข้อผิดพลาดในการลบ overlay: " + ex.getMessage());
                                            isTransitioning = false; // คืนค่า flag แม้จะมีข้อผิดพลาด
                                        }
                                    });
                                    fadeOut.play();
                                } catch (Exception ex) {
                                    System.err.println("เกิดข้อผิดพลาดในการ setup UI: " + ex.getMessage());
                                    ex.printStackTrace();
                                    isTransitioning = false; // คืนค่า flag เมื่อเกิดข้อผิดพลาด
                                }
                            });
                        } catch (Exception ex) {
                            System.err.println("เกิดข้อผิดพลาดใน setupThread: " + ex.getMessage());
                            ex.printStackTrace();
                            isTransitioning = false; // คืนค่า flag เมื่อเกิดข้อผิดพลาด
                        }
                    });
                    
                    setupThread.setDaemon(true);
                    setupThread.start();
                });
                
                fadeIn.play();
            } catch (Exception ex) {
                System.err.println("เกิดข้อผิดพลาดใน returnToRoom: " + ex.getMessage());
                ex.printStackTrace();
                isTransitioning = false; // คืนค่า flag เมื่อเกิดข้อผิดพลาด
                
                // ในกรณีที่เกิดข้อผิดพลาด ให้พยายามกู้คืนสถานะ UI
                try {
                    // ล้าง gameArea และเพิ่ม worldGroup กลับเข้าไป
                    gameArea.getChildren().clear();
                    if (worldGroup != null && roomObjects != null) {
                        gameArea.getChildren().add(worldGroup);
                    } else {
                        setupUI();
                    }
                    
                    // เพิ่ม UI elements เข้า rootStack ในกรณีฉุกเฉิน
                    if (!rootStack.getChildren().contains(menuBar)) {
                        rootStack.getChildren().add(menuBar);
                    }
                    
                    if (!rootStack.getChildren().contains(moneyUI)) {
                        rootStack.getChildren().add(moneyUI);
                    }
                    
                    if (!rootStack.getChildren().contains(dateView)) {
                        rootStack.getChildren().add(dateView);
                    }
                    
                    if (!rootStack.getChildren().contains(inGameMarketMenuBar)) {
                        rootStack.getChildren().add(inGameMarketMenuBar);
                    }
                    
                    // กำหนดให้แสดงผล UI ทุกอย่าง
                    menuBar.setVisible(true);
                    inGameMarketMenuBar.setVisible(true);
                    moneyUI.setVisible(true);
                    dateView.setVisible(true);
                    
                    // กำหนดตำแหน่งของ UI elements
                    StackPane.setAlignment(menuBar, Pos.TOP_CENTER);
                    StackPane.setAlignment(moneyUI, Pos.TOP_LEFT);
                    
                    System.out.println("กู้คืนสถานะ UI สำเร็จ (แผนฉุกเฉิน)");
                    System.out.println("  - menuBar: " + (menuBar != null ? "ok" : "null"));
                    System.out.println("  - inGameMarketMenuBar: " + (inGameMarketMenuBar != null ? "ok" : "null"));
                    System.out.println("  - moneyUI: " + (moneyUI != null ? "ok" : "null"));
                    System.out.println("  - dateView: " + (dateView != null ? "ok" : "null"));
                    System.out.println("  - จำนวน elements ใน rootStack: " + rootStack.getChildren().size());
                } catch (Exception recoverEx) {
                    System.err.println("ไม่สามารถกู้คืน UI ได้: " + recoverEx.getMessage());
                    recoverEx.printStackTrace();
                }
            }
        });
    }

    /**
     * ตรวจสอบว่า UI element นี้มี parent แล้วหรือยัง
     * @param node node ที่ต้องการตรวจสอบ
     * @return true ถ้ามี parent แล้ว
     */
    private boolean checkHasParent(javafx.scene.Node node) {
        return node.getParent() != null;
    }

    // === VPS Management Methods ===
    public boolean installVPSFromInventory(String vpsId) {
        VPSOptimization vps = vpsInventory.getVPS(vpsId);
        if (vps == null) {
            return false;
        }
        if (rack.installVPS(vps)) {
            // ลบ VPS จาก GameplayContentPane inventory
            vpsInventory.removeVPS(vpsId);
            
            // เพิ่ม VPS เข้า VPSManager
            vpsManager.addVPS(vps.getVpsId(), vps);
            
            // ซิงค์กับ GameManager
            GameManager gameManager = GameManager.getInstance();
            
            // ลบ VPS จาก inventory ของ GameManager
            if (gameManager.getVpsInventory().getVPS(vpsId) != null) {
                gameManager.getVpsInventory().removeVPS(vpsId);
                System.out.println("ลบ VPS จาก GameManager inventory: " + vpsId);
            }
            
            // เพิ่ม VPS เข้า installed servers ของ GameManager
            gameManager.installServer(vpsId);
            System.out.println("เพิ่ม VPS เข้า installed servers ของ GameManager: " + vpsId);
            
            // บันทึกเกมทันที
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังติดตั้ง VPS เรียบร้อย");
            
            return true;
        }
        return false;
    }

    public boolean uninstallVPSToInventory(VPSOptimization vps) {
        // Use the VPS ID directly from the VPS object
        String vpsId = vps.getVpsId();
        
        if (!rack.getInstalledVPS().contains(vps)) {
            return false;
        }
        if (rack.uninstallVPS(vps)) {
            // เพิ่ม VPS เข้า GameplayContentPane inventory
            vpsInventory.addVPS(vpsId, vps);
            
            // ซิงค์กับ GameManager
            GameManager gameManager = GameManager.getInstance();
            
            // ลบ VPS จาก installed servers ของ GameManager
            gameManager.uninstallServer(vps);
            System.out.println("ลบ VPS จาก installed servers ของ GameManager: " + vpsId);
            
            // เพิ่ม VPS เข้า inventory ของ GameManager (แม้ว่า uninstallServer จะทำให้แล้ว แต่เราทำอีกครั้งเพื่อความมั่นใจ)
            if (gameManager.getVpsInventory().getVPS(vpsId) == null) {
                gameManager.getVpsInventory().addVPS(vpsId, vps);
                System.out.println("เพิ่ม VPS เข้า GameManager inventory: " + vpsId);
            }
            
            // บันทึกเกมทันที
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังถอด VPS เรียบร้อย");
            
            return true;
        }
        return false;
    }

    // === Dialog Methods ===
    public void showVMSelectionDialog() {
        List<VPSOptimization.VM> allAvailableVMs = new ArrayList<>();
        for (VPSOptimization vps : vpsManager.getVPSList()) {
            allAvailableVMs.addAll(vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()))
                    .collect(java.util.stream.Collectors.toList()));
        }
        if (allAvailableVMs.isEmpty()) {
            pushNotification("Error", "No available VMs to assign.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Assign VM");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2c3e50;");

        Label titleLabel = new Label("Select a VM to Assign");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        ComboBox<VPSOptimization.VM> vmComboBox = new ComboBox<>();
        vmComboBox.setMaxWidth(Double.MAX_VALUE);
        vmComboBox.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        vmComboBox.getItems().addAll(allAvailableVMs);
        vmComboBox.setPromptText("Select a VM");

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> {
            VPSOptimization.VM selectedVM = vmComboBox.getValue();
            if (selectedVM != null) {
                pushNotification("Success", "Assigned VM: " + selectedVM.getName());
                dialog.close();
            } else {
                pushNotification("Error", "Please select a VM.");
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        Label selectVMLabel = new Label("Select VM:");
        selectVMLabel.setStyle("-fx-text-fill: white;");

        content.getChildren().addAll(titleLabel, selectVMLabel, vmComboBox, buttonBox);
        dialog.getDialogPane().setContent(content);

        dialog.setOnShown(event -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            Stage mainStage = (Stage) getScene().getWindow();
            stage.setX(mainStage.getX() + (mainStage.getWidth() / 2) - (stage.getWidth() / 2));
            stage.setY(mainStage.getY() + (mainStage.getHeight() / 2) - (stage.getHeight() / 2));
        });

        dialog.showAndWait();
    }

    // === Getter Methods ===
    public StackPane getRootStack() {
        return rootStack;
    }

    public StackPane getGameArea() {
        return gameArea;
    }

    public GameMenuBar getMenuBar() {
        return this.menuBar;
    }

    public MoneyUI getMoneyUI() {
        return moneyUI;
    }

    public DateView getDateView() {
        return dateView;
    }

    public List<VPSOptimization> getVpsList() {
        return rack.getInstalledVPS();
    }

    public InGameMarketMenuBar getInGameMarketMenuBar() {
        return inGameMarketMenuBar;
    }

    public VPSManager getVpsManager() {
        return vpsManager;
    }

    public Company getCompany() {
        return company;
    }

    public GameTimeController getGameTimeController() {
        return gameTimeController;
    }

    public ChatSystem getChatSystem() {
        return chatSystem;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public VPSInventory getVpsInventory() {
        return vpsInventory;
    }

    public Rack getRack() {
        return rack;
    }

    public int getOccupiedSlots() {
        return rack.getOccupiedSlotUnits();
    }

    public int getTotalSlots() {
        return rack.getMaxSlotUnits();
    }

    public int getAvailableSlot() {
        return rack.getAvailableSlotUnits();
    }

    public int getAllAvailableSlots() {
        return rack.getUnlockedSlotUnits();
    }

    public boolean isShowDebug() {
        return showDebug;
    }

    public Navigator getNavigator() {
        return this.navigator;
    }

    // === Setter Methods ===
    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public void setSkillPointsSystem(SkillPointsSystem skillPointsSystem) {
        this.skillPointsSystem = skillPointsSystem;
    }

    /**
     * ซิงค์ข้อมูล VPS Inventory กับ GameManager
     * เพื่อให้แน่ใจว่าข้อมูลระหว่าง GameplayContentPane และ GameManager ตรงกัน
     */
    private void syncWithGameManager() {
        System.out.println("กำลังซิงค์ข้อมูลกับ GameManager...");
        
        GameManager gameManager = GameManager.getInstance();
        VPSInventory gameManagerInventory = gameManager.getVpsInventory();
        
        // ถ้า inventory ของ GameManager มีข้อมูล แต่ของเราไม่มี ให้ใช้ข้อมูลจาก GameManager
        if (gameManagerInventory != null && !gameManagerInventory.isEmpty() && vpsInventory.isEmpty()) {
            System.out.println("พบข้อมูล VPS ใน GameManager inventory: " + gameManagerInventory.getSize() + " รายการ");
            
            // คัดลอกข้อมูลจาก GameManager มาใส่ใน inventory ของเรา
            for (String vpsId : gameManagerInventory.getAllVPSIds()) {
                VPSOptimization vps = gameManagerInventory.getVPS(vpsId);
                if (vps != null) {
                    vpsInventory.addVPS(vpsId, vps);
                    System.out.println("โหลด VPS จาก GameManager: " + vpsId);
                }
            }
            
            System.out.println("ซิงค์ข้อมูลเสร็จสิ้น, VPS ใน inventory: " + vpsInventory.getSize() + " รายการ");
        }
        // ถ้า inventory ของเรามีข้อมูล แต่ของ GameManager ไม่มี ให้ใช้ข้อมูลของเรา
        else if (!vpsInventory.isEmpty() && (gameManagerInventory == null || gameManagerInventory.isEmpty())) {
            System.out.println("พบข้อมูล VPS ใน GameplayContentPane inventory: " + vpsInventory.getSize() + " รายการ");
            
            // คัดลอกข้อมูลจาก inventory ของเราไปใส่ใน GameManager
            for (String vpsId : vpsInventory.getAllVPSIds()) {
                VPSOptimization vps = vpsInventory.getVPS(vpsId);
                if (vps != null) {
                    gameManagerInventory.addVPS(vpsId, vps);
                    System.out.println("เพิ่ม VPS เข้า GameManager: " + vpsId);
                }
            }
            
            // บันทึกเกมเพื่ออัปเดตข้อมูล
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังซิงค์ข้อมูลเรียบร้อย");
        }
        // ถ้าทั้งสองฝั่งมีข้อมูล ให้รวมข้อมูลเข้าด้วยกัน
        else if (!vpsInventory.isEmpty() && !gameManagerInventory.isEmpty()) {
            System.out.println("พบข้อมูล VPS ทั้งสองฝั่ง - กำลังรวมข้อมูล");
            System.out.println("- GameplayContentPane: " + vpsInventory.getSize() + " รายการ");
            System.out.println("- GameManager: " + gameManagerInventory.getSize() + " รายการ");
            
            // ตรวจสอบว่า VPS ใน GameManager มีอยู่ใน inventory ของเราหรือไม่
            for (String vpsId : gameManagerInventory.getAllVPSIds()) {
                if (!vpsInventory.getAllVPSIds().contains(vpsId)) {
                    VPSOptimization vps = gameManagerInventory.getVPS(vpsId);
                    if (vps != null) {
                        vpsInventory.addVPS(vpsId, vps);
                        System.out.println("เพิ่ม VPS จาก GameManager เข้า inventory: " + vpsId);
                    }
                }
            }
            
            // ตรวจสอบว่า VPS ใน inventory ของเรามีอยู่ใน GameManager หรือไม่
            for (String vpsId : vpsInventory.getAllVPSIds()) {
                if (!gameManagerInventory.getAllVPSIds().contains(vpsId)) {
                    VPSOptimization vps = vpsInventory.getVPS(vpsId);
                    if (vps != null) {
                        gameManagerInventory.addVPS(vpsId, vps);
                        System.out.println("เพิ่ม VPS จาก inventory เข้า GameManager: " + vpsId);
                    }
                }
            }
            
            // บันทึกเกมเพื่ออัปเดตข้อมูล
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังซิงค์ข้อมูลเรียบร้อย");
        }
    }
}