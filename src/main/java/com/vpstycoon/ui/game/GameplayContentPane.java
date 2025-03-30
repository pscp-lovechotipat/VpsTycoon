package com.vpstycoon.ui.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.DefaultGameConfig;
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
import com.vpstycoon.screen.ScreenResolution;
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

    private RequestManager requestManager;
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

        
        setStyle("-fx-background-color: black; -fx-padding: 0; -fx-margin: 0;");
        
        
        rootStack = new StackPane();
        
        rootStack.setStyle("-fx-background-color: black; -fx-padding: 0; -fx-margin: 0;");
        
        
        ScreenResolution resolution = DefaultGameConfig.getInstance().getResolution();
        rootStack.setPrefSize(resolution.getWidth(), resolution.getHeight());
        rootStack.setMinSize(resolution.getWidth(), resolution.getHeight());
        rootStack.setMaxSize(resolution.getWidth(), resolution.getHeight());
        
        gameArea = new StackPane();
        gameArea.setStyle("-fx-background-color: black; -fx-padding: 0; -fx-margin: 0;");
        gameArea.setPrefSize(resolution.getWidth(), resolution.getHeight());
        gameArea.setMinSize(resolution.getWidth(), resolution.getHeight());
        gameArea.setMaxSize(resolution.getWidth(), resolution.getHeight());
        rootStack.getChildren().add(gameArea);

        
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

        
        LocalDateTime initialDate = null;
        if (ResourceManager.getInstance().getCurrentState() != null) {
            initialDate = ResourceManager.getInstance().getCurrentState().getLocalDateTime();
        }
        
        if (initialDate == null) {
            
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

        
        syncWithGameManager();

        setupUI();
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();
        setCenter(rootStack);
        setupDebugFeatures();

        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        
        
        if (gameTimeController != null) {
            System.out.println("GameplayContentPane: เริ่มการเดินเวลาเกม (startTime)");
            try {
                
                boolean isRunning = gameTimeController.getGameTimeManager().isRunning();
                System.out.println("GameplayContentPane: สถานะ gameTimeManager.isRunning=" + isRunning);
                gameTimeController.startTime();
                System.out.println("GameplayContentPane: เรียก startTime สำเร็จ");
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการเริ่มเวลาเกม: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("GameplayContentPane: gameTimeController เป็น null ไม่สามารถเริ่มเวลาเกมได้");
        }
        System.out.println("Game time started at startup");
        
        
        if (roomObjects != null && !roomObjects.getRun()) {
            audioManager.pauseMusic();
        } else {
            audioManager.resumeMusic();
        }
        
        
        ResourceManager.getInstance().initializeGameEvent(this);
        System.out.println("เริ่มการทำงานของ GameEvent แล้ว");
    }

    
    private void initializeBasicRack() {
        
        if (rack.getMaxRacks() == 0) {
            rack.addRack(10);  
            rack.upgrade();    
            System.out.println("เพิ่ม rack เริ่มต้นพร้อม 1 slot ปลดล็อคแล้ว");
        }
    }

    private synchronized void setupUI() {
        System.out.println("กำลังตั้งค่า UI ใหม่...");
        
        
        ScreenResolution resolution = DefaultGameConfig.getInstance().getResolution();
        
        
        gameArea.getChildren().clear();
        
        
        rootStack.setPrefSize(resolution.getWidth(), resolution.getHeight());
        rootStack.setMinSize(resolution.getWidth(), resolution.getHeight());
        rootStack.setMaxSize(resolution.getWidth(), resolution.getHeight());
        
        gameArea.setPrefSize(resolution.getWidth(), resolution.getHeight());
        gameArea.setMinSize(resolution.getWidth(), resolution.getHeight());
        gameArea.setMaxSize(resolution.getWidth(), resolution.getHeight());
        
        
        Image backgroundImage = com.vpstycoon.ui.game.components.RoomObjectsLayer.loadImage("/images/rooms/room.gif");
        
        
        Pane backgroundLayer = createBackgroundLayer(backgroundImage);
        
        
        
        
        
        this.rootStack.getChildren().clear();
        System.out.println("ล้าง rootStack เรียบร้อย");

        
        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo, this::openKeroro, this::openMusicBox, this::openMusicStop);
        System.out.println("สร้าง roomObjects ใหม่เรียบร้อย");
        
        
        worldGroup = new Group(backgroundLayer, roomObjects.getServerLayer(), roomObjects.getMonitorLayer(), 
                roomObjects.getKeroroLayer(), roomObjects.getMusicBoxLayer(), roomObjects.getMusicStopLayer());
        System.out.println("สร้าง worldGroup ใหม่เรียบร้อย");
        
        
        double centerX = (resolution.getWidth() - backgroundLayer.getPrefWidth()) / 2.0;
        double centerY = (resolution.getHeight() - backgroundLayer.getPrefHeight()) / 2.0;
        worldGroup.setLayoutX(centerX);
        worldGroup.setLayoutY(centerY);
        
        
        worldGroup.setScaleX(1.0);
        worldGroup.setScaleY(1.0);
        worldGroup.setTranslateX(0);
        worldGroup.setTranslateY(0);
        
        
        gameArea.getChildren().add(worldGroup);
        System.out.println("เพิ่ม worldGroup เข้า gameArea เรียบร้อย");

        
        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        
        
        rootStack.getChildren().add(gameArea);
        
        
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

        
        StackPane.setMargin(moneyUI, new Insets(40));

        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);
        StackPane.setAlignment(resourceManager.getNotificationView(), Pos.TOP_RIGHT);
        StackPane.setAlignment(moneyUI, Pos.TOP_LEFT);
        StackPane.setAlignment(dateView, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(inGameMarketMenuBar, Pos.TOP_CENTER);
        StackPane.setMargin(inGameMarketMenuBar, new Insets(50, 0, 0, 0));

        
        debugOverlayManager.startTimer();
        
        
        if (zoomPanHandler != null) {
            zoomPanHandler.cleanup(); 
        }
        
        zoomPanHandler = new ZoomPanHandler(worldGroup, gameArea, debugOverlayManager, showDebug);
        zoomPanHandler.setup();

        
        resourceManager.initializeGameEvent(this);

        
        setStyle("-fx-background-color: #000000;");
    }

    private Pane createBackgroundLayer(Image backgroundImage) {
        Pane backgroundLayer = new Pane();
        
        
        
        double fixedScaleFactor = 0.26;
        
        
        double fixedWidth = backgroundImage.getWidth() * fixedScaleFactor;
        double fixedHeight = backgroundImage.getHeight() * fixedScaleFactor;
        
        
        backgroundLayer.setPrefWidth(fixedWidth);
        backgroundLayer.setPrefHeight(fixedHeight);
        backgroundLayer.setMinWidth(fixedWidth);
        backgroundLayer.setMinHeight(fixedHeight);
        backgroundLayer.setMaxWidth(fixedWidth);
        backgroundLayer.setMaxHeight(fixedHeight);
        
        
        backgroundLayer.setStyle("""
        -fx-background-image: url("/images/rooms/room.gif");
        -fx-background-size: contain;
        -fx-background-repeat: no-repeat;
        -fx-background-position: center;
        """);
        
        System.out.println("Created background layer with fixed dimensions: " + 
                          fixedWidth + "x" + fixedHeight);
                          
        return backgroundLayer;
    }

    
    private Pane createBackgroundLayer() {
        
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
        
        boolean marketAlreadyOpen = false;
        for (javafx.scene.Node node : getGameArea().getChildren()) {
            if (node instanceof MarketWindow) {
                marketAlreadyOpen = true;
                break;
            }
        }
        
        if (marketAlreadyOpen) {
            
            return;
        }
        
        
        final boolean menuBarWasVisible = menuBar.isVisible();
        final boolean marketMenuBarWasVisible = inGameMarketMenuBar.isVisible();
        final boolean moneyUIWasVisible = moneyUI.isVisible();
        final boolean dateViewWasVisible = dateView.isVisible();

        MarketWindow marketWindow = new MarketWindow(
            () -> {
                getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                
                
                menuBar.setVisible(menuBarWasVisible);
                inGameMarketMenuBar.setVisible(marketMenuBarWasVisible);
                moneyUI.setVisible(moneyUIWasVisible);
                dateView.setVisible(dateViewWasVisible);
            },
            () -> {
                getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                
                
                menuBar.setVisible(menuBarWasVisible);
                inGameMarketMenuBar.setVisible(marketMenuBarWasVisible);
                moneyUI.setVisible(moneyUIWasVisible);
                dateView.setVisible(dateViewWasVisible);
            },
            vpsManager,
            this
        );
        
        
        marketWindow.setOpacity(0);
        getGameArea().getChildren().add(marketWindow);
        
        
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), marketWindow);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void openSimulationDesktop() {
        audioManager.playSound("click_app.wav");
        
        if (simulationDesktopUI == null) {
            System.err.println("ไม่สามารถเปิดหน้าเดสก์ท็อปได้: SimulationDesktopUI เป็น null");
            pushCenterNotification("ข้อผิดพลาด", "ไม่สามารถเปิดหน้าเดสก์ท็อปได้: โปรดลองอีกครั้งในภายหลัง", "/images/icon/error.png");
            return;
        }
        
        if (requestManager == null) {
            try {
                System.out.println("กำลังสร้าง RequestManager ใหม่...");
                this.requestManager = new com.vpstycoon.game.manager.RequestManager(this.company);
                System.out.println("สร้าง RequestManager ใหม่สำเร็จ");
            } catch (Exception e) {
                System.err.println("ไม่สามารถสร้าง RequestManager: " + e.getMessage());
                e.printStackTrace();
                pushCenterNotification("ข้อผิดพลาด", "ไม่สามารถเปิดหน้าเดสก์ท็อปได้: ไม่สามารถสร้าง RequestManager", "/images/icon/error.png");
                return;
            }
        }
        
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
            
            System.out.println("Music paused");
            pushNotification("Music Paused", "Game music has been paused. Click the music box to resume.");
            roomObjects.setRun(false);
        }
    }

    public void openMusicStop(){
        if (!roomObjects.getRun()) {
            roomObjects.getMusicBoxLayer().setVisible(true);
            roomObjects.getMusicStopLayer().setVisible(false);
            audioManager.resumeMusic();
            
            System.out.println("Music resumed");
            pushNotification("Music Resumed", "Game music has been resumed.");
            roomObjects.setRun(true);
        }
    }

    
    public void hideMenus() {
        menuBar.setVisible(false);
        inGameMarketMenuBar.setVisible(false);
        moneyUI.setVisible(false);
        dateView.setVisible(false);
    }

    
    public void returnToRoom() {
        
        try {
            if (rackManagementUI != null) {
                rackManagementUI.dispose();
                System.out.println("ยกเลิกการลงทะเบียน listeners ของ RackManagementUI เรียบร้อย");
            }
        } catch (Exception ex) {
            System.err.println("เกิดข้อผิดพลาดในการ dispose RackManagementUI: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        
        javafx.application.Platform.runLater(() -> {
            try {
                
                if (isTransitioning) {
                    System.out.println("กำลังเปลี่ยนแปลง UI อยู่แล้ว กรุณารอสักครู่...");
                    return;
                }
                isTransitioning = true;
                
                
                while (rootStack.getChildren().size() > 1) {
                    rootStack.getChildren().remove(rootStack.getChildren().size() - 1);
                }
                
                
                StackPane overlay = new StackPane();
                overlay.setStyle("-fx-background-color: black;");
                overlay.setOpacity(0);
                
                
                javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setMaxSize(50, 50);
                progressIndicator.setVisible(false);
                overlay.getChildren().add(progressIndicator);
                
                rootStack.getChildren().add(overlay);
                
                
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), overlay);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(0.7);
                fadeIn.setOnFinished(e -> {
                    progressIndicator.setVisible(true);
                    
                    
                    Thread setupThread = new Thread(() -> {
                        try {
                            
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    
                                    gameArea.getChildren().clear();
                                } catch (Exception ex) {
                                    System.err.println("เกิดข้อผิดพลาดในการล้าง gameArea: " + ex.getMessage());
                                }
                            });
                            
                            
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    
                                    if (worldGroup != null && roomObjects != null) {
                                        
                                        gameArea.getChildren().add(worldGroup);
                                    } else {
                                        
                                        setupUI();
                                    }
                                    
                                    
                                    rootStack.getChildren().removeIf(node -> 
                                        node instanceof javafx.scene.control.ProgressIndicator ||
                                        node instanceof StackPane && node != gameArea);
                                    
                                    
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
                                    
                                    
                                    menuBar.setVisible(true);
                                    inGameMarketMenuBar.setVisible(true); 
                                    moneyUI.setVisible(true);
                                    dateView.setVisible(true);
                                    
                                    
                                    StackPane.setAlignment(menuBar, Pos.TOP_CENTER);
                                    StackPane.setAlignment(moneyUI, Pos.TOP_LEFT);
                                    StackPane.setAlignment(resourceManager.getNotificationView(), Pos.TOP_RIGHT);
                                    StackPane.setAlignment(resourceManager.getCenterNotificationView(), Pos.CENTER);
                                    
                                    
                                    System.out.println("จำนวน elements ใน rootStack: " + rootStack.getChildren().size());
                                    
                                    
                                    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), overlay);
                                    fadeOut.setFromValue(0.7);
                                    fadeOut.setToValue(0);
                                    fadeOut.setOnFinished(event -> {
                                        try {
                                            rootStack.getChildren().remove(overlay);
                                            isTransitioning = false; 
                                        } catch (Exception ex) {
                                            System.err.println("เกิดข้อผิดพลาดในการลบ overlay: " + ex.getMessage());
                                            isTransitioning = false; 
                                        }
                                    });
                                    fadeOut.play();
                                } catch (Exception ex) {
                                    System.err.println("เกิดข้อผิดพลาดในการ setup UI: " + ex.getMessage());
                                    ex.printStackTrace();
                                    isTransitioning = false; 
                                }
                            });
                        } catch (Exception ex) {
                            System.err.println("เกิดข้อผิดพลาดใน setupThread: " + ex.getMessage());
                            ex.printStackTrace();
                            isTransitioning = false; 
                        }
                    });
                    
                    setupThread.setDaemon(true);
                    setupThread.start();
                });
                
                fadeIn.play();
            } catch (Exception ex) {
                System.err.println("เกิดข้อผิดพลาดใน returnToRoom: " + ex.getMessage());
                ex.printStackTrace();
                isTransitioning = false; 
                
                
                try {
                    
                    gameArea.getChildren().clear();
                    if (worldGroup != null && roomObjects != null) {
                        gameArea.getChildren().add(worldGroup);
                    } else {
                        setupUI();
                    }
                    
                    
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
                    
                    
                    menuBar.setVisible(true);
                    inGameMarketMenuBar.setVisible(true);
                    moneyUI.setVisible(true);
                    dateView.setVisible(true);
                    
                    
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

    
    private boolean checkHasParent(javafx.scene.Node node) {
        return node.getParent() != null;
    }

    
    public boolean installVPSFromInventory(String vpsId) {
        VPSOptimization vps = vpsInventory.getVPS(vpsId);
        if (vps == null) {
            return false;
        }
        
        // ตรวจสอบว่ามีพื้นที่ว่างพอในแร็คหรือไม่
        if (vps.getSlotsRequired() > rack.getAvailableSlotUnits()) {
            pushNotification("Installation Failed",
                    "Not enough slots available in the rack. You need " + vps.getSlotsRequired() +
                            " slots, but only " + rack.getAvailableSlotUnits() + " are available.");
            return false;
        }
        
        if (rack.installVPS(vps)) {
            
            vpsInventory.removeVPS(vpsId);
            
            
            vpsManager.addVPS(vps.getVpsId(), vps);
            
            
            GameManager gameManager = GameManager.getInstance();
            
            
            if (gameManager.getVpsInventory().getVPS(vpsId) != null) {
                gameManager.getVpsInventory().removeVPS(vpsId);
                System.out.println("ลบ VPS จาก GameManager inventory: " + vpsId);
            }
            
            
            gameManager.installServer(vpsId);
            System.out.println("เพิ่ม VPS เข้า installed servers ของ GameManager: " + vpsId);
            
            
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังติดตั้ง VPS เรียบร้อย");
            
            return true;
        }
        return false;
    }

    public boolean uninstallVPSToInventory(VPSOptimization vps) {
        
        String vpsId = vps.getVpsId();
        
        if (!rack.getInstalledVPS().contains(vps)) {
            return false;
        }
        if (rack.uninstallVPS(vps)) {
            
            vpsInventory.addVPS(vpsId, vps);
            
            
            GameManager gameManager = GameManager.getInstance();
            
            
            gameManager.uninstallServer(vps);
            System.out.println("ลบ VPS จาก installed servers ของ GameManager: " + vpsId);
            
            
            if (gameManager.getVpsInventory().getVPS(vpsId) == null) {
                gameManager.getVpsInventory().addVPS(vpsId, vps);
                System.out.println("เพิ่ม VPS เข้า GameManager inventory: " + vpsId);
            }
            
            
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังถอด VPS เรียบร้อย");
            
            return true;
        }
        return false;
    }

    
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
        return navigator;
    }

    
    public void updateResolution() {
        System.out.println("Updating resolution of game content...");
        
        
        ScreenResolution resolution = DefaultGameConfig.getInstance().getResolution();
        
        
        gameArea.getChildren().clear();
        
        
        if (roomObjects != null) {
            roomObjects = null;
        }
        
        
        worldGroup = null;
        
        
        setPrefSize(resolution.getWidth(), resolution.getHeight());
        setMinSize(resolution.getWidth(), resolution.getHeight());
        setMaxSize(resolution.getWidth(), resolution.getHeight());
        
        
        rootStack.setPrefSize(resolution.getWidth(), resolution.getHeight());
        rootStack.setMinSize(resolution.getWidth(), resolution.getHeight());
        rootStack.setMaxSize(resolution.getWidth(), resolution.getHeight());
        
        
        gameArea.setPrefSize(resolution.getWidth(), resolution.getHeight());
        gameArea.setMinSize(resolution.getWidth(), resolution.getHeight());
        gameArea.setMaxSize(resolution.getWidth(), resolution.getHeight());
        
        
        
        rootStack.getChildren().clear();
        rootStack.getChildren().add(gameArea);
        
        
        setupUI();
        
        
        requestLayout();
        layout();
        
        System.out.println("Resolution update complete - background size remains fixed.");
    }

    
    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public void setSkillPointsSystem(SkillPointsSystem skillPointsSystem) {
        this.skillPointsSystem = skillPointsSystem;
    }

    
    private void syncWithGameManager() {
        System.out.println("กำลังซิงค์ข้อมูลกับ GameManager...");
        
        GameManager gameManager = GameManager.getInstance();
        VPSInventory gameManagerInventory = gameManager.getVpsInventory();
        
        
        if (gameManagerInventory != null && !gameManagerInventory.isEmpty() && vpsInventory.isEmpty()) {
            System.out.println("พบข้อมูล VPS ใน GameManager inventory: " + gameManagerInventory.getSize() + " รายการ");
            
            
            for (String vpsId : gameManagerInventory.getAllVPSIds()) {
                VPSOptimization vps = gameManagerInventory.getVPS(vpsId);
                if (vps != null) {
                    vpsInventory.addVPS(vpsId, vps);
                    System.out.println("โหลด VPS จาก GameManager: " + vpsId);
                }
            }
            
            System.out.println("ซิงค์ข้อมูลเสร็จสิ้น, VPS ใน inventory: " + vpsInventory.getSize() + " รายการ");
        }
        
        else if (!vpsInventory.isEmpty() && (gameManagerInventory == null || gameManagerInventory.isEmpty())) {
            System.out.println("พบข้อมูล VPS ใน GameplayContentPane inventory: " + vpsInventory.getSize() + " รายการ");
            
            
            for (String vpsId : vpsInventory.getAllVPSIds()) {
                VPSOptimization vps = vpsInventory.getVPS(vpsId);
                if (vps != null) {
                    gameManagerInventory.addVPS(vpsId, vps);
                    System.out.println("เพิ่ม VPS เข้า GameManager: " + vpsId);
                }
            }
            
            
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังซิงค์ข้อมูลเรียบร้อย");
        }
        
        else if (!vpsInventory.isEmpty() && !gameManagerInventory.isEmpty()) {
            System.out.println("พบข้อมูล VPS ทั้งสองฝั่ง - กำลังรวมข้อมูล");
            System.out.println("- GameplayContentPane: " + vpsInventory.getSize() + " รายการ");
            System.out.println("- GameManager: " + gameManagerInventory.getSize() + " รายการ");
            
            
            for (String vpsId : gameManagerInventory.getAllVPSIds()) {
                if (!vpsInventory.getAllVPSIds().contains(vpsId)) {
                    VPSOptimization vps = gameManagerInventory.getVPS(vpsId);
                    if (vps != null) {
                        vpsInventory.addVPS(vpsId, vps);
                        System.out.println("เพิ่ม VPS จาก GameManager เข้า inventory: " + vpsId);
                    }
                }
            }
            
            
            for (String vpsId : vpsInventory.getAllVPSIds()) {
                if (!gameManagerInventory.getAllVPSIds().contains(vpsId)) {
                    VPSOptimization vps = vpsInventory.getVPS(vpsId);
                    if (vps != null) {
                        gameManagerInventory.addVPS(vpsId, vps);
                        System.out.println("เพิ่ม VPS จาก inventory เข้า GameManager: " + vpsId);
                    }
                }
            }
            
            
            gameManager.saveState();
            System.out.println("บันทึกเกมหลังซิงค์ข้อมูลเรียบร้อย");
        }
    }
}
