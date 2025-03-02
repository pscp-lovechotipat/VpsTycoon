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
        
        // Create room objects (monitor, server, table)
        roomObjects = new RoomObjectsLayer(this::openSimulationDesktop);
        
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

        // Create menu bar
        GameMenuBar menuBar = new GameMenuBar();
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        // Setup debug overlay
        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);

        // Add all elements to root
        if (!rootStack.getChildren().isEmpty()) {
            rootStack.getChildren().clear();
        }
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
