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
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.game.handlers.KeyEventHandler;
import com.vpstycoon.ui.game.handlers.ZoomPanHandler;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.List;
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
    private int occupiedSlots = 1; // Example: Starting with 1 occupied slot
    private final List<Pane> slotPanes = new ArrayList<>();

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

        // Initialize main containers
        this.rootStack = new StackPane();
        rootStack.setPrefSize(800, 600);
        rootStack.setMinSize(800, 600);

        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);
        gameArea.setMinSize(800, 600);

        // Setup UI components
        setupUI();

        // Initialize handlers
        this.keyEventHandler = new KeyEventHandler(this, debugOverlayManager);
        keyEventHandler.setup();

        // Set center content
        setCenter(rootStack);

        // Debug setup
        setupDebugFeatures();
    }

    /**
     * Main UI setup method
     */
    private void setupUI() {
        // Create world layers
        Pane backgroundLayer = createBackgroundLayer();
        Pane objectsContainer = createObjectsContainer();

        // Create menu bar
        GameMenuBar menuBar = new GameMenuBar();
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        // Create room objects (monitor, server, table)
        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop, this::openRackInfo );

        // Create world group with all elements
        worldGroup = new Group(
            backgroundLayer,
            objectsContainer,
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
            -fx-background-image: url("/images/rooms/room.png");
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

            // Calculate position based on grid
            double snappedX = Math.round(obj.getX() / cellSize) * cellSize;
            double snappedY = Math.round(obj.getY() / cellSize) * cellSize;

            view.setTranslateX(snappedX);
            view.setTranslateY(snappedY);

            // Add click handler
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
    private void openRackInfo() {
        StackPane rackPane = new StackPane();
        rackPane.setPrefSize(300, 450); // Adjusted size
        rackPane.setStyle("-fx-background-color: rgba(254,240,210,1); -fx-padding: 20px; -fx-border-color: black;");

        // Rack Panel (Colored Box Instead of Image)
        VBox rackBox = new VBox(5);
        rackBox.setPrefSize(150, 400); // Matching server dimensions
        rackBox.setStyle("-fx-background-color: #2E3B4E; -fx-border-color: black; -fx-border-width: 2px;");
        rackBox.setAlignment(Pos.TOP_CENTER);

        // VBox to hold rack slots
        VBox rackSlots = new VBox(5);
        rackSlots.setAlignment(Pos.TOP_CENTER);
        rackSlots.setTranslateY(5);

        // Create slot indicators
        slotPanes.clear();
        for (int i = 0; i < MAX_SLOTS; i++) {
            Pane slot = createRackSlot(i);
            slotPanes.add(slot);
            rackSlots.getChildren().add(slot);
        }

        // Server Information Panel
        VBox infoPane = new VBox(10);
        infoPane.setAlignment(Pos.TOP_LEFT);
        Label titleLabel = new Label("Rack Info");
        Label serverCount = new Label("Server: " + occupiedSlots + "/" + MAX_SLOTS + " Units");
        Label networkUsage = new Label("Networks: 10Gbps");
        Label userCount = new Label("User Active: 10");

        Button upgradeButton = new Button("Upgrade");
        upgradeButton.setOnAction(e -> upgradeRackSlot(serverCount));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            gameArea.getChildren().remove(rackPane);
        });

        infoPane.getChildren().addAll(titleLabel, serverCount, networkUsage, userCount, upgradeButton, closeButton);
        infoPane.setTranslateX(180); // Adjust positioning

        // Add slots inside rackBox
        rackBox.getChildren().add(rackSlots);

        // Stack layers together
        rackPane.getChildren().addAll(rackBox, infoPane);

        // Ensure that rackPane is added to gameArea only if it's not already present
        if (!gameArea.getChildren().contains(rackPane)) {
            gameArea.getChildren().add(rackPane);
        }
    }


    /**
     * Creates a Rack Slot UI Pane
     */
    private Pane createRackSlot(int index) {
        Pane slot = new Pane();
        slot.setPrefSize(100, 25);

        Rectangle rect = new Rectangle(100, 25);
        rect.setFill(index < occupiedSlots ? Color.DARKBLUE : Color.LIGHTGRAY);
        rect.setStroke(Color.BLACK);

        slot.getChildren().add(rect);
        slot.setOnMouseClicked(e -> System.out.println("Slot " + (index + 1) + " clicked"));

        return slot;
    }

    /**
     * Upgrades the Rack by adding a new server slot
     */
    private void upgradeRackSlot(Label serverCount) {
        if (occupiedSlots < MAX_SLOTS) {
            occupiedSlots++;
            slotPanes.get(occupiedSlots - 1).getChildren().get(0).setStyle("-fx-fill: darkblue;");
            serverCount.setText("Server: " + occupiedSlots + "/" + MAX_SLOTS + " Units");
        } else {
            System.out.println("No available slots");
        }
    }

    /**
     * Opens the desktop simulation screen
     */
    private void openSimulationDesktop() {
        DesktopScreen desktop = new DesktopScreen(
                0.0,               // Example companyRating
                0,                 // Example marketingPoints
                chatSystem,
                requestManager,
                vpsManager
        );
        StackPane.setAlignment(desktop, Pos.CENTER);
        desktop.setMaxSize(gameArea.getWidth() * 0.8, gameArea.getHeight() * 0.8);

        // Replace game area content
        gameArea.getChildren().clear();
        gameArea.getChildren().add(desktop);

        rootStack.getChildren().remove(1);

        // Add exit button
        desktop.addExitButton(this::returnToRoom);
    }

    /**
     * Return to the room view
     */
    public void returnToRoom() {
        gameArea.getChildren().clear();
        setupUI();
    }

    /**
     * Show the resume screen
     */
    public void showResumeScreen() {
        ResumeScreen resumeScreen = new ResumeScreen(navigator, this::hideResumeScreen);
        resumeScreen.setPrefSize(gameArea.getWidth(), gameArea.getHeight());
        gameArea.getChildren().add(resumeScreen);
    }

    /**
     * Hide the resume screen
     */
    public void hideResumeScreen() {
        gameArea.getChildren().removeIf(node -> node instanceof ResumeScreen);
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public boolean isShowDebug() {
        return showDebug;
    }

    public Group getWorldGroup() {
        return worldGroup;
    }

    public void setWorldGroup(Group worldGroup) {
        this.worldGroup = worldGroup;
    }

    public StackPane getGameArea() {
        return gameArea;
    }
}
