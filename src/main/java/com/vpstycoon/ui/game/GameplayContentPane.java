package com.vpstycoon.ui.game;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.SkillPointsSystem;
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
import com.vpstycoon.ui.game.market.MarketUI;
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

public class GameplayContentPane extends BorderPane {
    private final StackPane rootStack;

    private final NotificationModel notificationModel;
    private final NotificationView notificationView;
    private final NotificationController notificationController;

    private final CenterNotificationView centerNotificationView;
    private final CenterNotificationController centerNotificationController;

    private final MouseNotificationModel mouseNotificationModel;
    private final MouseNotificationView mouseNotificationView;
    private final MouseNotificationController mouseNotificationController;

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
    private final MarketUI marketUI;
    private final SimulationDesktopUI simulationDesktopUI;

    private SkillPointsSystem skillPointsSystem;

    private final VPSInventoryUI vpsInventoryUI;

    private AudioManager audioManager;

    private final VPSInventory vpsInventory = new VPSInventory();

    private final GameTimeController gameTimeController;

    // Constructor
    public GameplayContentPane(
            List<GameObject> gameObjects, Navigator navigator, ChatSystem chatSystem,
            RequestManager requestManager, VPSManager vpsManager, GameFlowManager gameFlowManager,
            DebugOverlayManager debugOverlayManager, Company company, Rack rack,
            GameTimeController gameTimeController) {
        this.gameObjects = gameObjects;
        this.navigator = navigator;
        this.chatSystem = chatSystem;

        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.rack = rack;
        this.gameFlowManager = gameFlowManager;
        this.debugOverlayManager = debugOverlayManager;
        this.company = company;

        this.gameTimeController = gameTimeController;

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
        this.notificationController = new NotificationController(
                notificationModel,
                notificationView
        );

        this.mouseNotificationModel = new MouseNotificationModel();
        this.mouseNotificationView = new MouseNotificationView();
        this.mouseNotificationController = new MouseNotificationController(
                mouseNotificationModel,
                mouseNotificationView
        );

        this.centerNotificationView = new CenterNotificationView();
        this.centerNotificationController = new CenterNotificationController(
                new CenterNotificationModel(),
                centerNotificationView
        );

        this.moneyModel = new MoneyModel(ResourceManager.getInstance().getCompany().getMoney(),
                ResourceManager.getInstance().getCompany().getRating());
        this.moneyUI = new MoneyUI(this, moneyModel);
        this.moneyController = new MoneyController(moneyModel, moneyUI);

        this.dateModel = new DateModel(ResourceManager.getInstance().getCurrentState().getLocalDateTime());
        this.dateView = new DateView(this, dateModel);
        this.dateController = new DateController(dateModel, dateView);

        this.rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);

        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);

        this.menuBar = new GameMenuBar(this);

        this.inGameMarketMenuBar = new InGameMarketMenuBar(this, vpsManager);

        initializeSampleVPS();

        setupUI();
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();
        setCenter(rootStack);
        setupDebugFeatures();

        this.audioManager = AudioManager.getInstance();
    }

    // === Initialization Methods ===
    private void initializeSampleVPS() {
        VPSOptimization vps1 = new VPSOptimization();
        vps1.setVCPUs(2);
        vps1.setRamInGB(4);
        vps1.setDiskInGB(50);
        vps1.setSize(VPSSize.SIZE_1U);
        vps1.setInstalled(true);
        rack.installVPS(vps1);

        VPSOptimization vps2 = new VPSOptimization();
        vps2.setVCPUs(4);
        vps2.setRamInGB(8);
        vps2.setDiskInGB(100);
        vps2.setSize(VPSSize.SIZE_2U);
        vps2.setInstalled(true);
        rack.installVPS(vps2);

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

        vpsManager.createVPS("103.216.158.235-BasicVPS");
        vpsManager.getVPSMap().put("103.216.158.235-BasicVPS", invVps1);
        vpsManager.createVPS("103.216.158.236-EnterpriseVPS");
        vpsManager.getVPSMap().put("103.216.158.236-EnterpriseVPS", invVps2);

        VPSOptimization.VM vm1 = new VPSOptimization.VM("192.168.1.1", "Web Server", 1, "1 GB", "20 GB", "Running");
        VPSOptimization.VM vm2 = new VPSOptimization.VM("192.168.1.2", "Database", 1, "2 GB", "30 GB", "Running");
        vps1.addVM(vm1);
        vps1.addVM(vm2);
    }

    private synchronized void setupUI() {
        Pane backgroundLayer = createBackgroundLayer();

        this.rootStack.getChildren().clear();

        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo, this::openKeroro);
        worldGroup = new Group(backgroundLayer, roomObjects.getServerLayer(), roomObjects.getMonitorLayer(), roomObjects.getKeroroLayer());
        gameArea.getChildren().add(worldGroup);

        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        rootStack.getChildren().addAll(gameArea,
                moneyUI, menuBar,dateView,
                inGameMarketMenuBar, mouseNotificationView, notificationView, centerNotificationView,
                debugOverlay
        );

        menuBar.setVisible(true);
        menuBar.setPickOnBounds(false);

        inGameMarketMenuBar.setVisible(true);
        inGameMarketMenuBar.setPickOnBounds(false);

        notificationView.setMaxWidth(400);
        notificationView.setPickOnBounds(false);

        centerNotificationView.setPickOnBounds(false);
        StackPane.setAlignment(centerNotificationView, Pos.CENTER);

        moneyUI.setPickOnBounds(false);
        moneyUI.setVisible(true);

        dateView.setVisible(true);

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
        Image backgroundImage = new Image("/images/rooms/room2_NoKeroro(70)2.png");

        double scaleFactor = 0.26;
        backgroundLayer.setPrefWidth(backgroundImage.getWidth() * scaleFactor);
        backgroundLayer.setPrefHeight(backgroundImage.getHeight() * scaleFactor);

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

    // === Notification Methods ===
    public void pushNotification(String title, String content) {
        notificationModel.addNotification(new NotificationModel.Notification(title, content));
        notificationView.addNotificationPane(title, content);
    }

    public void pushMouseNotification(String content) {
        mouseNotificationController.addNotification(content);
    }

    public void pushCenterNotification(String title, String content) {
        centerNotificationController.push(title, content);
    }

    public void pushCenterNotification(String title, String content, Image image) {
        centerNotificationController.push(title, content, image);
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
        marketUI.openMarket();
    }

    public void openSimulationDesktop() {
        audioManager.playSoundEffect("click_app.wav");
        simulationDesktopUI.openSimulationDesktop();
    }

    public void openVPSInventory() {
        vpsInventoryUI.openInventory();
    }

    public void openSkillPointsWindow() {
        SkillPointsWindow skillPointsWindow = new SkillPointsWindow(getSkillPointsSystem(), () -> {
            getGameArea().getChildren().remove(getGameArea().getChildren().size() - 1);
        });

        getGameArea().getChildren().add(skillPointsWindow);
    }

    public void openKeroro() {
        System.out.println("Open Keroro");
        audioManager.playSoundEffect("keroro_sound.mp3");
        pushMouseNotification("Keroro");
    }

    // === Navigation Methods ===
    public void returnToRoom() {
        gameArea.getChildren().clear();
        setupUI();

        menuBar.setVisible(true);
        inGameMarketMenuBar.setVisible(true);
    }

    // === VPS Management Methods ===
    public boolean installVPSFromInventory(String vpsId) {
        VPSOptimization vps = vpsInventory.getVPS(vpsId);
        if (vps == null) {
            return false;
        }
        if (rack.installVPS(vps)) {
            vpsInventory.removeVPS(vpsId);
            return true;
        }
        return false;
    }

    public boolean uninstallVPSToInventory(VPSOptimization vps) {
        String vpsId = vpsManager.getVPSMap().keySet().stream()
                .filter(id -> vpsManager.getVPS(id) == vps)
                .findFirst()
                .orElse(null);
        if (vpsId == null || !rack.getInstalledVPS().contains(vps)) {
            return false;
        }
        if (rack.uninstallVPS(vps)) {
            vpsInventory.addVPS(vpsId, vps);
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

    public SkillPointsSystem getSkillPointsSystem() {
        if (skillPointsSystem == null) {
            skillPointsSystem = new SkillPointsSystem();
        }
        return skillPointsSystem;
    }

    // === Setter Methods ===
    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public void setSkillPointsSystem(SkillPointsSystem skillPointsSystem) {
        this.skillPointsSystem = skillPointsSystem;
    }
}