package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.components.GameMenuBar;
import com.vpstycoon.ui.game.components.GameObjectDetailsModal;
import com.vpstycoon.ui.game.components.RoomObjectsLayer;
import com.vpstycoon.ui.game.desktop.DesktopScreen;
import com.vpstycoon.ui.game.desktop.MarketWindow;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.game.handlers.KeyEventHandler;
import com.vpstycoon.ui.game.handlers.ZoomPanHandler;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main gameplay area container, coordinates all UI elements and interactions.
 */
public class GameplayContentPane extends BorderPane {

    private final StackPane rootStack;
    private Group worldGroup;
    private final StackPane gameArea;

    // Game state objects
    private final List<GameObject> gameObjects;
    private final Navigator navigator;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;

    // UI components
    private RoomObjectsLayer roomObjects;

    // Rack State
    private static final int MAX_SLOTS = 10;
    private int occupiedSlots = 2; // Start with 2 slots
    private final List<Pane> slotPanes = new ArrayList<>();

    // VPS State
    private final List<VPS> vpsList = new ArrayList<>();

    // Handlers
    private ZoomPanHandler zoomPanHandler;
    private KeyEventHandler keyEventHandler;

    // State
    private boolean showDebug = false;

    public GameplayContentPane(
            List<GameObject> gameObjects,
            Navigator navigator,
            ChatSystem chatSystem,
            RequestManager requestManager,
            VPSManager vpsManager,
            GameFlowManager gameFlowManager,
            DebugOverlayManager debugOverlayManager
    ) {
        this.gameObjects = gameObjects;
        this.navigator = navigator;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.gameFlowManager = gameFlowManager;
        this.debugOverlayManager = debugOverlayManager;

        this.rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);

        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);

        // Default
        // Initialize with sample VPSs, each containing VMs
        List<VM> vms1 = new ArrayList<>();
        vms1.add(new VM("192.168.1.10", "VM1-1", 2, "4 GB", "50 GB", "Running"));
        vms1.add(new VM("192.168.1.11", "VM1-2", 1, "2 GB", "20 GB", "Stopped"));
        vpsList.add(new VPS("103.216.158.233", "VPS1", vms1));

        List<VM> vms2 = new ArrayList<>();
        vms2.add(new VM("192.168.1.20", "VM2-1", 4, "8 GB", "100 GB", "Running"));
        vpsList.add(new VPS("103.216.158.234", "VPS2", vms2));

        setupUI();
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();
        setCenter(rootStack);
        setupDebugFeatures();
    }

    private synchronized void setupUI() {
        // Create world layers
        Pane backgroundLayer = createBackgroundLayer();
        Pane objectsContainer = createObjectsContainer(); // ไม่ใช้แล้ว

        // Create menu bar
        GameMenuBar menuBar = new GameMenuBar();
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        // Create room objects (monitor, server, table)
        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo);

        // Create world group with all elements
        worldGroup = new Group(
                backgroundLayer,
//                objectsContainer, ไม่ใช้แล้ว
                roomObjects.getTableLayer(),
                roomObjects.getServerLayer(),
                roomObjects.getMonitorLayer()
        );

        // Add world to game area
        gameArea.getChildren().add(worldGroup);

        // Setup debug overlay
        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);

        // Add all elements to root
        rootStack.getChildren().clear();
        rootStack.getChildren().addAll(gameArea, menuBar, debugOverlay);

        // Start debug timer
        debugOverlayManager.startTimer();

        // Setup zoom and pan
        zoomPanHandler = new ZoomPanHandler(worldGroup, gameArea, debugOverlayManager, showDebug);
        zoomPanHandler.setup();

        // Set background color
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

    private Pane createObjectsContainer() {
        Pane objectsContainer = new Pane();
        double cellSize = 100;
        for (GameObject obj : gameObjects) {
            GameObjectView view = new GameObjectView(obj);
            double snappedX = Math.round(obj.getX() / cellSize) * cellSize;
            double snappedY = Math.round(obj.getY() / cellSize) * cellSize;
            view.setTranslateX(snappedX);
            view.setTranslateY(snappedY);
            view.setOnMouseClicked(e -> GameObjectDetailsModal.show(gameArea, obj, gameFlowManager));
            objectsContainer.getChildren().add(view);
        }
        return objectsContainer;
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
        BorderPane rackPane = new BorderPane();
        rackPane.setPrefSize(800, 600);
        rackPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Rack Management");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button closeButton = createModernButton("Close", "#F44336");
        closeButton.setOnAction(e -> returnToRoom()); // เปลี่ยนจาก clear ไปเป็น returnToRoom
        topBar.getChildren().addAll(titleLabel, closeButton);

        HBox contentBox = createCard();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(15));

        GridPane rackBox = new GridPane();
        rackBox.setPrefSize(150, 400);
        rackBox.setPadding(new Insets(5));
        rackBox.setStyle("-fx-background-color: #37474F; -fx-border-color: white; -fx-border-width: 2px; -fx-background-radius: 8px;");
        rackBox.setAlignment(Pos.TOP_CENTER);

        // แทนที่ VBox ด้วย GridPane
        GridPane rackSlots = new GridPane();
        rackSlots.setAlignment(Pos.CENTER);
        rackSlots.setPadding(new Insets(5));
        rackSlots.setHgap(5); // ระยะห่างแนวนอนระหว่างช่อง
        rackSlots.setVgap(5); // ระยะห่างแนวตั้งระหว่างช่อง

        // กำหนดจำนวนคอลัมน์ (เช่น 1 คอลัมน์)
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(100); // ใช้ความกว้างเต็ม
        rackSlots.getColumnConstraints().add(column);

        // เพิ่ม RowConstraints เพื่อให้แต่ละแถวมีขนาดเท่ากัน
        for (int i = 0; i < MAX_SLOTS; i++) {
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(400.0 / MAX_SLOTS); // แบ่งความสูงเท่าๆ กันตามจำนวน slot
            row.setVgrow(Priority.ALWAYS);
            rackSlots.getRowConstraints().add(row);
        }

        slotPanes.clear();
        for (int i = 0; i < MAX_SLOTS; i++) {
            VPS vps = (i < vpsList.size()) ? vpsList.get(i) : null;
            Pane slot = createRackSlot(i, vps, i < occupiedSlots);
            slotPanes.add(slot);
            // เพิ่ม slot ลงใน GridPane โดยระบุตำแหน่ง (คอลัมน์, แถว)
            rackSlots.add(slot, 0, i);
        }
        rackBox.getChildren().add(rackSlots);

        VBox infoPane = new VBox(10);
        infoPane.setAlignment(Pos.CENTER);
        Label infoTitle = new Label("Rack Status");
        infoTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label serverCount = new Label("VPS: " + vpsList.size() + "/" + occupiedSlots);
        serverCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        Label networkUsage = new Label("Network: 10 Gbps");
        networkUsage.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        Label userCount = new Label("Active Users: 10");
        userCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        Button upgradeButton = createModernButton("Upgrade Rack", "#4CAF50");
        upgradeButton.setOnAction(e -> {
            if (occupiedSlots < MAX_SLOTS) {
                occupiedSlots++;
                System.out.println("Rack upgraded to " + occupiedSlots + " slots");
                openRackInfo(); // Refresh the rack view
            } else {
                System.out.println("Max slots reached, cannot upgrade.");
            }
        });
        infoPane.getChildren().addAll(infoTitle, serverCount, networkUsage, userCount, upgradeButton);

        contentBox.getChildren().addAll(rackBox, infoPane);
        rackPane.setCenter(contentBox);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button openMarketButton = createModernButton("Open Market", "#FF9800");
        openMarketButton.setOnAction(e -> openMarket());
        buttonBox.getChildren().addAll(openMarketButton);
        rackPane.setBottom(buttonBox);

        rackPane.setTop(topBar);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(rackPane);
        rootStack.getChildren().remove(1); // Remove menubar
    }

    private void openCreateVPSPage() {
        if (vpsList.size() >= occupiedSlots) {
            System.out.println("Cannot create VPS: All slots are occupied.");
            return;
        }

        BorderPane createVPSPane = new BorderPane();
        createVPSPane.setPrefSize(800, 600);
        createVPSPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Create Virtual Private Server");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> openRackInfo());
        topBar.getChildren().addAll(backButton, titleLabel);

        HBox formBox = createCard();
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(15));

        VBox infoSection = createSection("Basic Information");
        HBox nameBox = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Enter VPS Name");
        nameBox.getChildren().addAll(new Label("Name:"), nameField);
        HBox ipBox = new HBox(10);
        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP (e.g., 103.216.158.233)");
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button resetButton = createModernButton("Reset", "#F44336");
        resetButton.setOnAction(e -> {
            nameField.clear();
            ipField.clear();
        });
        Button createButton = createModernButton("Create", "#2196F3");
        createButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || ipField.getText().isEmpty()) {
                System.out.println("Validation Error: All fields are required.");
            } else {
                VPS newVPS = new VPS(ipField.getText(), nameField.getText(), new ArrayList<>());
                vpsList.add(newVPS);
                System.out.println("Success: VPS created: " + ipField.getText());
                openRackInfo();
            }
        });
        buttonBox.getChildren().addAll(resetButton, createButton);

        createVPSPane.setTop(topBar);
        createVPSPane.setCenter(formBox);
        createVPSPane.setBottom(buttonBox);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(createVPSPane);
    }

    private void openVPSInfoPage(VPS vps) {
        BorderPane vpsInfoPane = new BorderPane();
        vpsInfoPane.setPrefSize(800, 600);
        vpsInfoPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("VPS Details: " + vps.getIp());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> openRackInfo());
        topBar.getChildren().addAll(backButton, titleLabel);

        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        HBox infoBox = createCard();
        infoBox.setPadding(new Insets(15));
        VBox vpsSection = createSection("VPS Information");
        Label vpsDetail = new Label("IP: " + vps.getIp() + "\nName: " + vps.getName() + "\nVMs: " + vps.getVms().size());
        vpsDetail.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 14px;");
        vpsSection.getChildren().add(vpsDetail);
        infoBox.getChildren().add(vpsSection);

        HBox vmListBox = createCard();
        vmListBox.setPadding(new Insets(15));
        Label vmLabel = new Label("Virtual Machines");
        vmLabel.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 20px; -fx-font-weight: bold;");
        List<HBox> vmRows = new ArrayList<>();
        for (VM vm : vps.getVms()) {
            HBox row = new HBox(10);
            Button vmButton = new Button(vm.getIp());
            vmButton.setPrefWidth(200);
            vmButton.setStyle("-fx-background-color: #455A64; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px;");
            vmButton.setOnMouseEntered(e -> vmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"));
            vmButton.setOnMouseExited(e -> vmButton.setStyle("-fx-background-color: #455A64; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px;"));
            vmButton.setOnAction(e -> openVMInfoPage(vm, vps));
            vmButton.setFocusTraversable(true);
            row.getChildren().add(vmButton);
            vmRows.add(row);
        }
        vmListBox.getChildren().addAll(vmLabel, new Separator());
        vmListBox.getChildren().addAll(vmRows);

        centerBox.getChildren().addAll(infoBox, vmListBox);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button createVMButton = createModernButton("Create VM", "#2196F3");
        createVMButton.setOnAction(e -> openCreateVMPage(vps));
        Button deleteVPSButton = createModernButton("Delete VPS", "#F44336");
        deleteVPSButton.setOnAction(e -> {
            vpsList.remove(vps);
            System.out.println("VPS deleted: " + vps.getIp());
            openRackInfo();
        });
        buttonBox.getChildren().addAll(createVMButton, deleteVPSButton);

        vpsInfoPane.setTop(topBar);
        vpsInfoPane.setCenter(centerBox);
        vpsInfoPane.setBottom(buttonBox);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(vpsInfoPane);
    }

    private void openCreateVMPage(VPS vps) {
        BorderPane createVMPane = new BorderPane();
        createVMPane.setPrefSize(800, 600);
        createVMPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Create VM for VPS: " + vps.getIp());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> openVPSInfoPage(vps));
        topBar.getChildren().addAll(backButton, titleLabel);

        HBox formBox = createCard();
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(15));

        VBox infoSection = createSection("Basic Information");
        HBox nameBox = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Enter VM Name");
        nameBox.getChildren().addAll(new Label("Name:"), nameField);
        HBox ipBox = new HBox(10);
        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP (e.g., 192.168.1.10)");
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        VBox perfSection = createSection("Performance Settings");
        HBox perfBox = new HBox(10);
        TextField vcpuField = new TextField();
        vcpuField.setPromptText("e.g., 2");
        TextField ramField = new TextField();
        ramField.setPromptText("e.g., 4 GB");
        TextField diskField = new TextField();
        diskField.setPromptText("e.g., 50 GB");
        perfBox.getChildren().addAll(
                new Label("vCPUs:"), vcpuField,
                new Label("RAM:"), ramField,
                new Label("Disk:"), diskField
        );
        perfSection.getChildren().add(perfBox);

        formBox.getChildren().addAll(infoSection, perfSection);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button resetButton = createModernButton("Reset", "#F44336");
        resetButton.setOnAction(e -> {
            nameField.clear();
            ipField.clear();
            vcpuField.clear();
            ramField.clear();
            diskField.clear();
        });
        Button createButton = createModernButton("Create", "#2196F3");
        createButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || ipField.getText().isEmpty() || vcpuField.getText().isEmpty() ||
                    ramField.getText().isEmpty() || diskField.getText().isEmpty()) {
                System.out.println("Validation Error: All fields are required.");
            } else {
                VM newVM = new VM(ipField.getText(), nameField.getText(),
                        Integer.parseInt(vcpuField.getText()), ramField.getText(), diskField.getText(), "Running");
                vps.getVms().add(newVM);
                System.out.println("Success: VM created: " + ipField.getText() + " in VPS: " + vps.getIp());
                openVPSInfoPage(vps);
            }
        });
        buttonBox.getChildren().addAll(resetButton, createButton);

        createVMPane.setTop(topBar);
        createVMPane.setCenter(formBox);
        createVMPane.setBottom(buttonBox);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(createVMPane);
    }

    private void openVMInfoPage(VM vm, VPS vps) {
        BorderPane vmInfoPane = new BorderPane();
        vmInfoPane.setPrefSize(800, 600);
        vmInfoPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("VM Details: " + vm.getIp());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> openVPSInfoPage(vps));
        topBar.getChildren().addAll(backButton, titleLabel);

        HBox infoBox = createCard();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(15));
        VBox vmSection = createSection("VM Information");
        Label vmDetail = new Label("IP: " + vm.getIp() + "\nName: " + vm.getName() +
                "\nStatus: " + vm.getStatus() + "\nvCPUs: " + vm.getvCPUs() +
                " | RAM: " + vm.getRam() + " | Disk: " + vm.getDisk());
        vmDetail.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 14px;");
        vmSection.getChildren().add(vmDetail);
        infoBox.getChildren().add(vmSection);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button editButton = createModernButton("Edit", "#2196F3");
        editButton.setOnAction(e -> openEditVMPage(vm, vps));
        Button deleteButton = createModernButton("Delete", "#F44336");
        deleteButton.setOnAction(e -> {
            vps.getVms().remove(vm);
            System.out.println("VM deleted: " + vm.getIp());
            openVPSInfoPage(vps);
        });
        buttonBox.getChildren().addAll(editButton, deleteButton);

        vmInfoPane.setTop(topBar);
        vmInfoPane.setCenter(infoBox);
        vmInfoPane.setBottom(buttonBox);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(vmInfoPane);
    }

    private void openEditVMPage(VM vm, VPS vps) {
        BorderPane editVMPane = new BorderPane();
        editVMPane.setPrefSize(800, 600);
        editVMPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Edit VM: " + vm.getIp());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> openVMInfoPage(vm, vps));
        topBar.getChildren().addAll(backButton, titleLabel);

        HBox formBox = createCard();
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(15));

        VBox infoSection = createSection("Basic Information");
        HBox nameBox = new HBox(10);
        TextField nameField = new TextField(vm.getName());
        nameField.setPromptText("Enter VM Name");
        nameBox.getChildren().addAll(new Label("Name:"), nameField);
        HBox ipBox = new HBox(10);
        TextField ipField = new TextField(vm.getIp());
        ipField.setPromptText("Enter IP (e.g., 192.168.1.10)");
        ipField.setDisable(true);
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        VBox perfSection = createSection("Performance Settings");
        HBox perfBox = new HBox(10);
        TextField vcpuField = new TextField(String.valueOf(vm.getvCPUs()));
        vcpuField.setPromptText("e.g., 2");
        TextField ramField = new TextField(vm.getRam());
        ramField.setPromptText("e.g., 4 GB");
        TextField diskField = new TextField(vm.getDisk());
        diskField.setPromptText("e.g., 50 GB");
        perfBox.getChildren().addAll(
                new Label("vCPUs:"), vcpuField,
                new Label("RAM:"), ramField,
                new Label("Disk:"), diskField
        );
        perfSection.getChildren().add(perfBox);

        VBox statusSection = createSection("Status");
        ChoiceBox<String> statusChoice = new ChoiceBox<>();
        statusChoice.getItems().addAll("Running", "Stopped", "Paused");
        statusChoice.setValue(vm.getStatus());
        statusSection.getChildren().addAll(new Label("Status:"), statusChoice);

        formBox.getChildren().addAll(infoSection, perfSection, statusSection);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button saveButton = createModernButton("Save", "#2196F3");
        saveButton.setOnAction(e -> {
            try {
                vm.setName(nameField.getText());
                vm.setvCPUs(Integer.parseInt(vcpuField.getText()));
                vm.setRam(ramField.getText());
                vm.setDisk(diskField.getText());
                vm.setStatus(statusChoice.getValue());
                System.out.println("VM updated: " + vm.getIp());
                openVMInfoPage(vm, vps);
            } catch (NumberFormatException ex) {
                System.out.println("Error: Invalid vCPUs value.");
            }
        });
        Button cancelButton = createModernButton("Cancel", "#F44336");
        cancelButton.setOnAction(e -> openVMInfoPage(vm, vps));
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        editVMPane.setTop(topBar);
        editVMPane.setCenter(formBox);
        editVMPane.setBottom(buttonBox);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(editVMPane);
    }

    public void openMarket() {
        MarketWindow marketWindow = new MarketWindow(
                () -> {
                    gameArea.getChildren().removeIf(node -> node instanceof MarketWindow);
                    openRackInfo();
                },
                vpsManager,
                this
        );
        gameArea.getChildren().add(marketWindow);
    }

    private synchronized void openSimulationDesktop() {
        DesktopScreen desktop = new DesktopScreen(
                0.0, // Example companyRating
                0,   // Example marketingPoints
                chatSystem,
                requestManager,
                vpsManager
        );
        StackPane.setAlignment(desktop, Pos.CENTER);
        desktop.setMaxSize(gameArea.getWidth() * 0.8, gameArea.getHeight() * 0.8);

        gameArea.getChildren().clear();
        gameArea.getChildren().add(desktop);
        rootStack.getChildren().remove(1); // Remove menubar

        desktop.addExitButton(this::returnToRoom); // เพิ่มปุ่ม Exit เพื่อกลับไปที่ห้อง
    }

    public void returnToRoom() {
        gameArea.getChildren().clear();
        setupUI(); // เรียก setupUI เพื่อกลับไปที่มุมมองห้องเริ่มต้น
    }

    private HBox createCard() {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: #37474F; -fx-padding: 15px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        card.setAlignment(Pos.CENTER);
        return card;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(10);
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        section.getChildren().add(label);
        return section;
    }

    private Button createModernButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        return button;
    }

    private Pane createRackSlot(int index, VPS vps, boolean isSlotAvailable) {
        Pane slot = new Pane();
        slot.setPrefSize(100, 25);
        Rectangle rect = new Rectangle(100, 25);
        rect.setFill(vps != null ? Color.web("#42A5F5") : (isSlotAvailable ? Color.LIGHTGRAY : Color.DARKGRAY));
        rect.setStroke(Color.WHITE);
        rect.setArcHeight(5);
        rect.setArcWidth(5);
        rect.setEffect(new DropShadow(5, Color.BLACK));
        rect.setOnMouseEntered(e -> {
            if (vps != null) rect.setFill(Color.web("#90CAF9"));
        });
        rect.setOnMouseExited(e -> {
            if (vps != null) rect.setFill(Color.web("#42A5F5"));
        });

        if (vps != null) {
            slot.setOnMouseClicked(e -> openVPSInfoPage(vps));
        } else if (isSlotAvailable) {
            slot.setOnMouseClicked(e -> openCreateVPSPage());
        } else {
            slot.setOnMouseClicked(e -> System.out.println("Slot " + (index + 1) + " clicked - Not available yet"));
        }

        slot.getChildren().add(rect);
        return slot;
    }

    public void showResumeScreen() {
        ResumeScreen resumeScreen = new ResumeScreen(navigator, this::hideResumeScreen);
        resumeScreen.setPrefSize(gameArea.getWidth(), gameArea.getHeight());
        gameArea.getChildren().add(resumeScreen);
    }

    public void hideResumeScreen() {
        gameArea.getChildren().removeIf(node -> node instanceof ResumeScreen);

        // Reset the state in KeyEventHandler
        if (keyEventHandler != null) {
            keyEventHandler.setResumeScreenShowing(false);
        }
    }

    public boolean isShowDebug() {
        return showDebug;
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public List<VPS> getVpsList() {
        return vpsList;
    }

    // VPS Class
    public static class VPS {
        private String ip;
        private String name;
        private List<VM> vms;

        public VPS(String ip, String name, List<VM> vms) {
            this.ip = ip;
            this.name = name;
            this.vms = vms;
        }

        public String getIp() { return ip; }
        public String getName() { return name; }
        public List<VM> getVms() { return vms; }
    }

    // VM Class
    public static class VM {
        private String ip;
        private String name;
        private int vCPUs;
        private String ram;
        private String disk;
        private String status;

        public VM(String ip, String name, int vCPUs, String ram, String disk, String status) {
            this.ip = ip;
            this.name = name;
            this.vCPUs = vCPUs;
            this.ram = ram;
            this.disk = disk;
            this.status = status;
        }

        public String getIp() { return ip; }
        public String getName() { return name; }
        public int getvCPUs() { return vCPUs; }
        public String getRam() { return ram; }
        public String getDisk() { return disk; }
        public String getStatus() { return status; }

        public void setName(String name) { this.name = name; }
        public void setvCPUs(int vCPUs) { this.vCPUs = vCPUs; }
        public void setRam(String ram) { this.ram = ram; }
        public void setDisk(String disk) { this.disk = disk; }
        public void setStatus(String status) { this.status = status; }
    }
}