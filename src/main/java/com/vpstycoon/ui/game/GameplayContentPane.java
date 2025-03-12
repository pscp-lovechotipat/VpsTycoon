package com.vpstycoon.ui.game;

import com.vpstycoon.FontLoader;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSSize;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.components.GameMenuBar;
import com.vpstycoon.ui.game.components.InGameMarketMenuBar;
import com.vpstycoon.ui.game.components.RoomObjectsLayer;
import com.vpstycoon.ui.game.desktop.SimulationDesktopUI;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.game.handlers.KeyEventHandler;
import com.vpstycoon.ui.game.handlers.ZoomPanHandler;
import com.vpstycoon.ui.game.market.MarketUI;
import com.vpstycoon.ui.game.notification.NotificationController;
import com.vpstycoon.ui.game.notification.NotificationModel;
import com.vpstycoon.ui.game.notification.NotificationView;
import com.vpstycoon.ui.game.rack.RackManagementUI;
import com.vpstycoon.ui.game.vps.*;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

import com.vpstycoon.game.SkillPointsSystem;
import com.vpstycoon.ui.game.desktop.SkillPointsWindow;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.vps.enums.VPSStatus;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.ui.game.inventory.VPSInventoryUI;

public class GameplayContentPane extends BorderPane {
    private final StackPane rootStack;

    private final NotificationModel notificationModel;
    private final NotificationView notificationView;
    private final NotificationController notificationController;

    private Group worldGroup;
    private final StackPane gameArea;
    private final GameMenuBar menuBar;

    private final InGameMarketMenuBar inGameMarketMenuBar;
    private final List<GameObject> gameObjects;

    private final Navigator navigator;
    private final ChatSystem chatSystem;

    private final RequestManager requestManager;
    private final VPSManager vpsManager;

    private final GameFlowManager gameFlowManager;

    private final DebugOverlayManager debugOverlayManager;
    private final Company company;
    private RoomObjectsLayer roomObjects;

    private int occupiedSlots = 0;
    private int totalSlots = 0; // Total number of slots in the rack

    private final List<VPSOptimization> vpsList = new ArrayList<>();
    private final VPSInventory vpsInventory = new VPSInventory(); // Inventory of uninstalled VPS servers

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

    private SkillPointsSystem skillPointsSystem;

    private final VPSInventoryUI vpsInventoryUI;

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
        this.vpsInventoryUI = new VPSInventoryUI(this);

        this.notificationModel = new NotificationModel();
        this.notificationView = new NotificationView();
        this.notificationController = new NotificationController(notificationModel, notificationView);

        this.rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);

        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);

        this.menuBar = new GameMenuBar();

        this.inGameMarketMenuBar = new InGameMarketMenuBar(this, vpsManager);

        // เพิ่มข้อมูล VPS ตัวอย่าง
        initializeSampleVPS();

        setupUI();
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();
        setCenter(rootStack);
        setupDebugFeatures();

        // Initialize total slots
        this.totalSlots = 10; // Default total slots
    }

    private void initializeSampleVPS() {
        // Create sample VPS for the rack
        VPSOptimization vps1 = new VPSOptimization();
        vps1.setVCPUs(2);
        vps1.setRamInGB(4);
        vps1.setDiskInGB(50);
        vps1.setSize(VPSSize.SIZE_1U);
        vps1.setInstalled(true);
        vpsList.add(vps1);
        
        VPSOptimization vps2 = new VPSOptimization();
        vps2.setVCPUs(4);
        vps2.setRamInGB(8);
        vps2.setDiskInGB(100);
        vps2.setSize(VPSSize.SIZE_2U);
        vps2.setInstalled(true);
        vpsList.add(vps2);
        
        // Update occupied slots based on installed VPS sizes
        occupiedSlots = vps1.getSlotsRequired() + vps2.getSlotsRequired();
        
        // Create sample VPS for the inventory
        VPSOptimization invVps1 = new VPSOptimization();
        invVps1.setVCPUs(1);
        invVps1.setRamInGB(2);
        invVps1.setDiskInGB(20);
        invVps1.setSize(VPSSize.SIZE_1U);
        vpsInventory.addVPS("103.216.158.235-BasicVPS", invVps1);
        
        VPSOptimization invVps2 = new VPSOptimization();
        invVps2.setVCPUs(8);
        invVps2.setRamInGB(16);
        invVps2.setDiskInGB(200);
        invVps2.setSize(VPSSize.SIZE_3U);
        vpsInventory.addVPS("103.216.158.236-EnterpriseVPS", invVps2);
        
        // Add to VPS manager
        vpsManager.createVPS("103.216.158.235-BasicVPS");
        vpsManager.getVPSMap().put("103.216.158.235-BasicVPS", invVps1);
        vpsManager.createVPS("103.216.158.236-EnterpriseVPS");
        vpsManager.getVPSMap().put("103.216.158.236-EnterpriseVPS", invVps2);
        
        // Add sample VMs to the first VPS
        VPSOptimization.VM vm1 = new VPSOptimization.VM("192.168.1.1", "Web Server", 1, "1 GB", "20 GB", "Running");
        VPSOptimization.VM vm2 = new VPSOptimization.VM("192.168.1.2", "Database", 1, "2 GB", "30 GB", "Running");
        vps1.addVM(vm1);
        vps1.addVM(vm2);
        
        // Initialize total slots
        totalSlots = 10;
    }

    private synchronized void setupUI() {
        Pane backgroundLayer = createBackgroundLayer();

        this.rootStack.getChildren().clear();

        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo, this::openKeroro);
        worldGroup = new Group(backgroundLayer, roomObjects.getServerLayer(), roomObjects.getMonitorLayer(), roomObjects.getKeroroLayer());
        gameArea.getChildren().add(worldGroup);

        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        rootStack.getChildren().addAll(gameArea, menuBar, inGameMarketMenuBar, notificationView, debugOverlay);

        // Explicitly set menu bars to visible in the main gameplay screen
        menuBar.setVisible(true);
        menuBar.setPickOnBounds(false);

        inGameMarketMenuBar.setVisible(true);
        inGameMarketMenuBar.setPickOnBounds(false);

        StackPane.setAlignment(notificationView, Pos.TOP_RIGHT);
        notificationView.setMaxWidth(400); // จำกัดความกว้างของการแจ้งเตือน
        notificationView.setPickOnBounds(false);

        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        debugOverlayManager.startTimer();
        zoomPanHandler = new ZoomPanHandler(worldGroup, gameArea, debugOverlayManager, showDebug);
        zoomPanHandler.setup();
        setStyle("-fx-background-color: #000000;");
    }

    private Pane createBackgroundLayer() {
        Pane backgroundLayer = new Pane();
        Image backgroundImage = new Image("/images/rooms/room2_NoKeroro(70)2.png"); // เปลี่ยน path รูปตามที่คุณใช้

        double scaleFactor = 0.26;
        backgroundLayer.setPrefWidth(backgroundImage.getWidth() * scaleFactor);
        backgroundLayer.setPrefHeight(backgroundImage.getHeight() * scaleFactor);

        // ใช้ CSS ตั้ง background
        backgroundLayer.setStyle("""
        -fx-background-image: url("/images/rooms/room2_NoKeroro(70)2.png");
        -fx-background-size: cover;
        -fx-background-repeat: no-repeat;
        -fx-background-position: center;
    """);
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

    public void pushNotification(String title, String content) {
        notificationModel.addNotification(new NotificationModel.Notification(title, content));
        notificationView.addNotificationPane(title, content);
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
        
        // Make sure menu bars are visible in the main gameplay screen
        menuBar.setVisible(true);
        inGameMarketMenuBar.setVisible(true);
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

    public InGameMarketMenuBar getInGameMarketMenuBar() {
        return inGameMarketMenuBar;
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

    public void openKeroro() {
        System.out.println("Open Keroro");
        //รอตัวเองกลับมาทำ-บบ-
//        this.rootStack.getChildren().clear();
//        this.rootStack.getChildren().add(roomObjects.getKeroroLayer()); // Jiant keroro?
    }

    /**
     * Get the skill points system
     * @return The skill points system
     */
    public SkillPointsSystem getSkillPointsSystem() {
        if (skillPointsSystem == null) {
            skillPointsSystem = new SkillPointsSystem();
        }
        return skillPointsSystem;
    }
    
    /**
     * Set the skill points system
     * @param skillPointsSystem The skill points system to set
     */
    public void setSkillPointsSystem(SkillPointsSystem skillPointsSystem) {
        this.skillPointsSystem = skillPointsSystem;
    }
    
    /**
     * Open the skill points window
     */
    public void openSkillPointsWindow() {
        SkillPointsWindow skillPointsWindow = new SkillPointsWindow(getSkillPointsSystem(), () -> {
            // Close action
            getGameArea().getChildren().remove(getGameArea().getChildren().size() - 1);
        });
        
        getGameArea().getChildren().add(skillPointsWindow);
    }
    
    /**
     * Get the game state
     * @return The game state
     */
    public GameState getGameState() {
        // ใช้ GameState จาก GameManager
        return GameManager.getInstance().getCurrentState();
    }

    /**
     * Get the VPS inventory
     * @return The VPS inventory
     */
    public VPSInventory getVpsInventory() {
        return vpsInventory;
    }

    /**
     * Get the total number of slots in the rack
     * @return The total number of slots
     */
    public int getTotalSlots() {
        return totalSlots;
    }

    /**
     * Set the total number of slots in the rack
     * @param totalSlots The total number of slots
     */
    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    /**
     * Install a VPS from inventory into the rack
     * @param vpsId The ID of the VPS to install
     * @return true if installation was successful, false otherwise
     */
    public boolean installVPSFromInventory(String vpsId) {
        // Get the VPS from inventory
        VPSOptimization vps = vpsInventory.getVPS(vpsId);
        if (vps == null) {
            return false; // VPS not found in inventory
        }
        
        // Check if there are enough slots available
        int slotsRequired = vps.getSlotsRequired();
        if (occupiedSlots + slotsRequired > totalSlots) {
            return false; // Not enough slots available
        }
        
        // Remove from inventory and add to rack
        vpsInventory.removeVPS(vpsId);
        vpsList.add(vps);
        vps.setInstalled(true);
        
        // Update occupied slots
        occupiedSlots += slotsRequired;
        
        return true;
    }
    
    /**
     * Uninstall a VPS from the rack and add it to inventory
     * @param vps The VPS to uninstall
     * @return true if uninstallation was successful, false otherwise
     */
    public boolean uninstallVPSToInventory(VPSOptimization vps) {
        // Find the VPS ID
        String vpsId = vpsManager.getVPSMap().keySet().stream()
                .filter(id -> vpsManager.getVPS(id) == vps)
                .findFirst()
                .orElse(null);
        
        if (vpsId == null || !vpsList.contains(vps)) {
            return false; // VPS not found in rack
        }
        
        // Remove from rack and add to inventory
        vpsList.remove(vps);
        vpsInventory.addVPS(vpsId, vps);
        vps.setInstalled(false);
        
        // Update occupied slots
        occupiedSlots -= vps.getSlotsRequired();
        
        return true;
    }

    /**
     * Open the VPS inventory UI
     */
    public void openVPSInventory() {
        vpsInventoryUI.openInventory();
    }
}
