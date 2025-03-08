package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.components.GameMenuBar;
import com.vpstycoon.ui.game.components.GameObjectDetailsModal;
import com.vpstycoon.ui.game.components.RoomObjectsLayer;
import com.vpstycoon.ui.game.desktop.SimulationDesktopUI;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.game.handlers.KeyEventHandler;
import com.vpstycoon.ui.game.handlers.ZoomPanHandler;
import com.vpstycoon.ui.game.market.MarketUI;
import com.vpstycoon.ui.game.rack.RackManagementUI;
import com.vpstycoon.ui.game.vps.VPSCreationUI;
import com.vpstycoon.ui.game.vps.VPSInfoUI;
import com.vpstycoon.ui.game.vps.VMCreationUI;
import com.vpstycoon.ui.game.vps.VMEditUI;
import com.vpstycoon.ui.game.vps.VMInfoUI;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameplayContentPane extends BorderPane {
    private final StackPane rootStack;
    private Group worldGroup;
    private final StackPane gameArea;
    private final GameMenuBar menuBar;
    private final List<GameObject> gameObjects;
    private final Navigator navigator;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;
    private final Company company;
    private RoomObjectsLayer roomObjects;
    private int occupiedSlots = 2;
    private final List<VPSOptimization> vpsList = new ArrayList<>();
    private ZoomPanHandler zoomPanHandler;
    private KeyEventHandler keyEventHandler;
    private boolean showDebug = false;
    private final RackManagementUI rackManagementUI;
    private final VPSCreationUI vpsCreationUI;
    private final VPSInfoUI vpsInfoUI;
    private final VMCreationUI vmCreationUI;
    private final VMInfoUI vmInfoUI;
    private final VMEditUI vmEditUI;
    private final MarketUI marketUI;
    private final SimulationDesktopUI simulationDesktopUI;

    public GameplayContentPane(
            List<GameObject> gameObjects, Navigator navigator, ChatSystem chatSystem,
            RequestManager requestManager, VPSManager vpsManager, GameFlowManager gameFlowManager,
            DebugOverlayManager debugOverlayManager, Company company) {
        this.gameObjects = gameObjects;
        this.navigator = navigator;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.gameFlowManager = gameFlowManager;
        this.debugOverlayManager = debugOverlayManager;
        this.company = company;
        this.rackManagementUI = new RackManagementUI(this);
        this.vpsCreationUI = new VPSCreationUI(this);
        this.vpsInfoUI = new VPSInfoUI(this);
        this.vmCreationUI = new VMCreationUI(this);
        this.vmInfoUI = new VMInfoUI(this);
        this.vmEditUI = new VMEditUI(this);
        this.marketUI = new MarketUI(this);
        this.simulationDesktopUI = new SimulationDesktopUI(this);

        this.rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);
        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);
        this.menuBar = new GameMenuBar();

        // เพิ่มข้อมูล VPS ตัวอย่าง
        initializeSampleVPS();

        setupUI();
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();
        setCenter(rootStack);
        setupDebugFeatures();
    }

    private void initializeSampleVPS() {
        VPSOptimization vps1 = new VPSOptimization();
        vps1.setVCPUs(2);
        vps1.setRamInGB(4);
        vps1.setDiskInGB(50);
        vps1.addVM(new VPSOptimization.VM("192.168.1.10", "VM1-1", 1, "2 GB", "25 GB", "Running"));
        vps1.addVM(new VPSOptimization.VM("192.168.1.11", "VM1-2", 1, "2 GB", "25 GB", "Stopped"));
        vpsManager.createVPS("VPS1");
        vpsManager.getVPSMap().put("VPS1", vps1);
        vpsList.add(vps1);

        VPSOptimization vps2 = new VPSOptimization();
        vps2.setVCPUs(4);
        vps2.setRamInGB(8);
        vps2.setDiskInGB(100);
        vps2.addVM(new VPSOptimization.VM("192.168.1.20", "VM2-1", 2, "4 GB", "50 GB", "Running"));
        vpsManager.createVPS("VPS2");
        vpsManager.getVPSMap().put("VPS2", vps2);
        vpsList.add(vps2);
    }

    private synchronized void setupUI() {
        Pane backgroundLayer = createBackgroundLayer();
        this.rootStack.getChildren().clear();

        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo);
        worldGroup = new Group(backgroundLayer, roomObjects.getServerLayer(), roomObjects.getMonitorLayer());
        gameArea.getChildren().add(worldGroup);

        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        rootStack.getChildren().addAll(gameArea, menuBar, debugOverlay);

        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        debugOverlayManager.startTimer();
        zoomPanHandler = new ZoomPanHandler(worldGroup, gameArea, debugOverlayManager, showDebug);
        zoomPanHandler.setup();
        setStyle("-fx-background-color: #000000;");
    }

    private Pane createBackgroundLayer() {
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("""
            -fx-background-image: url("/images/rooms/room2(70).png");
            -fx-background-color: transparent;
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """);
        backgroundLayer.prefWidthProperty().bind(gameArea.widthProperty());
        backgroundLayer.prefHeightProperty().bind(rootStack.heightProperty());
        return backgroundLayer;
    }

    private void setupDebugFeatures() {
        setOnMouseMoved(e -> {
            if (showDebug) {
                debugOverlayManager.updateMousePosition(e.getX(), e.getY());
                debugOverlayManager.updateGameInfo(rootStack);
            }
        });
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
        marketUI.openMarket();
    }

    public void openSimulationDesktop() {
        simulationDesktopUI.openSimulationDesktop();
    }

    public void returnToRoom() {
        gameArea.getChildren().clear();
        setupUI();
    }

    // Getters and setters
    public StackPane getRootStack() {
        return rootStack;
    }

    public StackPane getGameArea() {
        return gameArea;
    }

    public GameMenuBar getMenuBar() { return this.menuBar; }

    public List<VPSOptimization> getVpsList() {
        return vpsList;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(int occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public VPSManager getVpsManager() {
        return vpsManager;
    }

    public Company getCompany() {
        return company;
    }

    public ChatSystem getChatSystem() {
        return chatSystem;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public boolean isShowDebug() {
        return showDebug;
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public Navigator getNavigator() {
        return  this.navigator;
    }
}