package com.vpstycoon.ui.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.SkillPointsSystem;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
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
import com.vpstycoon.ui.game.market.MarketUI;
import com.vpstycoon.ui.game.notification.NotificationController;
import com.vpstycoon.ui.game.notification.NotificationModel;
import com.vpstycoon.ui.game.notification.NotificationView;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationController;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationModel;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationView;
import com.vpstycoon.ui.game.rack.RackManagementUI;
import com.vpstycoon.ui.game.status.money.MoneyController;
import com.vpstycoon.ui.game.status.money.MoneyModel;
import com.vpstycoon.ui.game.status.money.MoneyUI;
import com.vpstycoon.ui.game.vps.*;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class GameplayContentPane extends BorderPane {
    private final StackPane rootStack;

    private final NotificationModel notificationModel;
    private final NotificationView notificationView;
    private final NotificationController notificationController;

    private final MouseNotificationModel mouseNotificationModel;
    private final MouseNotificationView mouseNotificationView;
    private final MouseNotificationController mouseNotificationController;

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

    private AudioManager audioManager;

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

        this.mouseNotificationModel = new MouseNotificationModel();
        this.mouseNotificationView = new MouseNotificationView();
        this.mouseNotificationController = new MouseNotificationController(mouseNotificationModel, mouseNotificationView);

        this.moneyModel = new MoneyModel(ResourceManager.getInstance().getCompany().getMoney(),
                ResourceManager.getInstance().getCompany().getRating()); // ค่าเริ่มต้นของ money และ ratting
        this.moneyUI = new MoneyUI(this, moneyModel);
        this.moneyController = new MoneyController(moneyModel, moneyUI);

        this.rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);

        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);

        this.menuBar = new GameMenuBar(this);

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

        this.audioManager = AudioManager.getInstance();
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
        rootStack.getChildren().addAll(gameArea, moneyUI, menuBar, inGameMarketMenuBar, mouseNotificationView, notificationView, debugOverlay);

        // Explicitly set menu bars to visible in the main gameplay screen
        menuBar.setVisible(true);
        menuBar.setPickOnBounds(false);

        inGameMarketMenuBar.setVisible(true);
        inGameMarketMenuBar.setPickOnBounds(false);

        notificationView.setMaxWidth(400); // จำกัดความกว้างของการแจ้งเตือน
        notificationView.setPickOnBounds(false);

        moneyUI.setPickOnBounds(false);
        moneyUI.setVisible(true);

        StackPane.setMargin(this.moneyUI, new Insets(40));

        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);
        StackPane.setAlignment(notificationView, Pos.TOP_RIGHT);
        StackPane.setAlignment(moneyUI, Pos.TOP_LEFT);

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

    public void pushMouseNotification(String content) {
        mouseNotificationController.addNotification(content);
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
        audioManager.playSoundEffect("click_app.wav");
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

    public MoneyUI getMoneyUI() {
        return moneyUI;
    }

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
        audioManager.playSoundEffect("keroro_sound.mp3");
        pushMouseNotification("Keroro");
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

    /**
     * แสดง Dialog สำหรับเลือก VM
     */
    public void showVMSelectionDialog() {
        // ตรวจสอบว่าไม่มี VM ที่พร้อมใช้งานหรือไม่
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

        // สร้าง Dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL); // ทำให้เป็น Modal
        dialog.setTitle("Assign VM");

        // สร้างเนื้อหาของ Dialog
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
                // ทำอะไรสักอย่างเมื่อเลือก VM (เช่น assign VM)
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

        // ตั้งค่าตำแหน่ง Dialog ให้อยู่กลางจอ
        dialog.setOnShown(event -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            Stage mainStage = (Stage) getScene().getWindow();
            stage.setX(mainStage.getX() + (mainStage.getWidth() / 2) - (stage.getWidth() / 2));
            stage.setY(mainStage.getY() + (mainStage.getHeight() / 2) - (stage.getHeight() / 2));
        });

        // แสดง Dialog
        dialog.showAndWait();
    }
}