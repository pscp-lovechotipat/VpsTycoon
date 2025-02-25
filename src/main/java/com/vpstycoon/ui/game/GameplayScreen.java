package com.vpstycoon.ui.game;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.VPSObject;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import com.vpstycoon.ui.desktop.DesktopScreen;
import com.vpstycoon.manager.RequestManager;
import com.vpstycoon.manager.VPSManager;
import com.vpstycoon.chat.ChatSystem;
import com.vpstycoon.company.Company;

import java.util.ArrayList;
import java.util.List;

public class GameplayScreen extends GameScreen {
    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private List<GameObject> gameObjects;
    private StackPane gameArea;
    private VBox debugOverlay;
    private boolean showDebug = false;
    private Label fpsLabel;
    private Label mouseLabel;
    private Label moneyLabel;
    private Label zoomLabel;
    private long lastTime = System.nanoTime();
    private int frameCount = 0;
    private AnimationTimer debugTimer;
    private ChatSystem chatSystem;
    private RequestManager requestManager;
    private VPSManager vpsManager;
    private final Company company;

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        this.company = new Company();
        loadGame();
        this.chatSystem = new ChatSystem();
        this.requestManager = new RequestManager();
        this.vpsManager = new VPSManager();
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            GameState state = saveManager.loadGame();
            if (state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                this.gameObjects = state.getGameObjects();
            } else {
                initializeGameObjects();
            }
        } else {
            initializeGameObjects();
        }
    }

    private void initializeGameObjects() {
        gameObjects.clear();
        gameObjects.add(new VPSObject("server", "Server", 500, 500));
        gameObjects.add(new VPSObject("database", "Database", 600, 600));
        gameObjects.add(new VPSObject("network", "Network", 700, 700));
        saveGame(); // Save initial state
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        // Create modal container with semi-transparent background
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        // Create modal content
        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                """);

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
                -fx-font-size: 14px;
                -fx-wrap-text: true;
                -fx-text-alignment: center;
                """);
        messageLabel.setMaxWidth(250);

        // Buttons container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        // Confirm Button
        Button confirmButton = createModalButton("Yes");
        confirmButton.setOnAction(e -> {
            onConfirm.run();
            gameArea.getChildren().remove(modalContainer);
        });

        // Cancel Button
        Button cancelButton = createModalButton("No");
        cancelButton.setStyle("""
                -fx-background-color: #95A5A6;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 20;
                -fx-background-radius: 5;
                -fx-min-width: 120;
                """);
        cancelButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        // Add all elements to modal content
        modalContent.getChildren().addAll(
                titleLabel,
                new Separator(),
                messageLabel,
                buttonBox
        );

        // Add modal content to container
        modalContainer.getChildren().add(modalContent);

        // Add click handler to close modal when clicking outside
        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        // Add modal to game area
        gameArea.getChildren().add(modalContainer);
    }

    @Override
    protected Region createContent() {
        BorderPane root = new BorderPane();
        root.setStyle("""
            -fx-background-color: #000000;
            """);

        // สร้างพื้นที่เกม
        gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);

        // สร้าง background layer
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("""
            -fx-background-image: url("/images/rooms/room.png");
            -fx-background-color: transparent;
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            """);
        backgroundLayer.prefWidthProperty().bind(gameArea.widthProperty());
        backgroundLayer.prefHeightProperty().bind(gameArea.heightProperty());


        // Create game objects container
        Pane objectsContainer = new Pane();
        for (GameObject obj : gameObjects) {
            GameObjectView view = new GameObjectView(obj);
            view.setOnMouseClicked(e -> showObjectDetails(obj));
            objectsContainer.getChildren().add(view);
        }

        Pane monitorLayer = new Pane();
        monitorLayer.setStyle("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: cover;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-pref-width: 5000px;
            -fx-pref-height: 5000px;
            -fx-translate-x: 250px; 
            -fx-translate-y: 200px;
        """);
        monitorLayer.setOnMouseClicked((MouseEvent e) -> openSimulationDesktop());

        // WorldGroup
        Group worldGroup = new Group(backgroundLayer, objectsContainer, monitorLayer);
        gameArea.getChildren().add(worldGroup);
        root.setCenter(gameArea);

        // Create top menu bar
        HBox menuBar = new HBox(20);
        menuBar.setPadding(new Insets(20));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle("-fx-background-color: #2C3E50;");

        Button saveButton = createButton("Save Game");
        saveButton.setOnAction(e -> {
            showConfirmationDialog(
                    "Save Game",
                    "Do you want to save your current progress?",
                    this::saveGame
            );
        });

        Button menuButton = createButton("Main Menu");
        menuButton.setOnAction(e -> {
            showConfirmationDialog(
                    "Return to Main Menu",
                    "Do you want to save and return to the main menu?",
                    () -> {
                        saveGame();
                        navigator.showMainMenu();
                    }
            );
        });

        menuBar.getChildren().addAll(saveButton, menuButton);
        root.setTop(menuBar);

        // Add zoom functionality
        gameArea.setOnScroll(e -> {
            double zoomFactor = 1.05;
            if (e.getDeltaY() < 0) {
                zoomFactor = 1.0 / zoomFactor;
            }
            worldGroup.setScaleX(worldGroup.getScaleX() * zoomFactor);
            worldGroup.setScaleY(worldGroup.getScaleY() * zoomFactor);
            e.consume();
        });

        // สร้าง Debug Overlay
        createDebugOverlay();


        // แก้ไข AnimationTimer ให้เก็บไว้ในตัวแปร
        debugTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateDebugInfo(now);
            }
        };
        debugTimer.start();


        // แก้ไข key event handler
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                exitGame();  // แยกโค้ดออกเป็นเมธอดใหม่
            } else if (event.getCode() == KeyCode.F3) {
                showDebug = !showDebug;
                debugOverlay.setVisible(showDebug);
            }
        });

        // เพิ่ม mouse move event สำหรับติดตามตำแหน่งเมาส์
        root.setOnMouseMoved(event -> {
            if (showDebug) {
                updateMousePosition(event.getX(), event.getY());
            }
        });

        // Set layout
        StackPane gameContainer = new StackPane(gameArea, debugOverlay);
        root.setCenter(gameContainer);

        root.setTop(menuBar);  // ย้าย menuBar มาไว้ท้ายสุด

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.requestFocus();
            }
        });

        return root;
    }


    private void openSimulationDesktop() {
        DesktopScreen desktop = new DesktopScreen(
            company.getRating(),
            company.getMarketingPoints(),
            chatSystem,
            requestManager,
            vpsManager
        );
        
        // จัดการ layout และขนาด
        StackPane.setAlignment(desktop, Pos.CENTER);
        desktop.setMaxSize(gameArea.getWidth() * 0.8, gameArea.getHeight() * 0.8);
        
        // เพิ่มเข้าไปใน gameArea
        gameArea.getChildren().clear();
        gameArea.getChildren().add(desktop);
    }

    private void openVPSDesktop() {
        ImageView vpsDesktopView = new ImageView(new Image("/images/others/logo.png"));
        double scaleFactor = 0.175; // ลองปรับค่านี้เพื่อให้พอดีกับจอ
        vpsDesktopView.setFitWidth(gameArea.getWidth() * scaleFactor);
        vpsDesktopView.setFitHeight(gameArea.getHeight() * scaleFactor);
        vpsDesktopView.setOnMouseClicked(e -> returnToRoom());

        gameArea.getChildren().clear();
        gameArea.getChildren().add(vpsDesktopView);
    }

    private void returnToRoom() {
        // เคลียร์ Desktop จำลอง
        gameArea.getChildren().clear();

        // สร้าง content ของห้องใหม่ (หรือจะเก็บ state เดิมไว้ก็ได้)
        Region newRoom = createContent();
        gameArea.getChildren().add(newRoom);
    }

    private void showObjectDetails(GameObject obj) {
        // Create modal container
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        // Create modal content
        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(obj.getName());
        titleLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                """);

        // Status
        Label statusLabel = new Label("Status: " + obj.getStatus());
        statusLabel.setStyle("-fx-font-size: 16px;");

        // Level
        Label levelLabel = new Label("Level: " + obj.getLevel());
        levelLabel.setStyle("-fx-font-size: 16px;");

        // Upgrade Button
        Button upgradeButton = createModalButton("Upgrade");
        upgradeButton.setOnAction(e -> {
            obj.upgrade(saveManager.loadGame());
            levelLabel.setText("Level: " + obj.getLevel());
            statusLabel.setText("Status: " + obj.getStatus());
            saveGame();
        });

        // Close Button
        Button closeButton = createModalButton("Close");
        closeButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        // Add all elements to modal content
        modalContent.getChildren().addAll(
                titleLabel,
                new Separator(),
                statusLabel,
                levelLabel,
                upgradeButton,
                closeButton
        );

        // Add modal content to container
        modalContainer.getChildren().add(modalContent);

        // Add click handler to close modal when clicking outside
        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        // Add modal to game area
        gameArea.getChildren().add(modalContainer);
    }

    private Button createModalButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-background-color: #3498DB;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 20;
                -fx-background-radius: 5;
                -fx-min-width: 120;
                """);

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace("#3498DB", "#2980B9"))
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("#2980B9", "#3498DB"))
        );

        return button;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-background-color: #3498DB;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8px 15px;
                -fx-background-radius: 5;
                """);
        return button;
    }

    private void saveGame() {
        GameState state = new GameState(gameObjects);
        saveManager.saveGame(state);
    }

    private void createDebugOverlay() {
        debugOverlay = new VBox(5);
        debugOverlay.setAlignment(Pos.BOTTOM_LEFT);
        debugOverlay.setPadding(new Insets(10));
        debugOverlay.setMouseTransparent(true);
        debugOverlay.setVisible(false);

        fpsLabel = new Label("FPS: 0");
        mouseLabel = new Label("Mouse: 0, 0");
        moneyLabel = new Label("Money: 0");
        zoomLabel = new Label("Zoom: 1.0x");

        // สไตล์สำหรับ debug text
        String labelStyle = """
            -fx-font-family: monospace;
            -fx-font-size: 14px;
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, black, 1, 1, 0, 0);
            """;

        fpsLabel.setStyle(labelStyle);
        mouseLabel.setStyle(labelStyle);
        moneyLabel.setStyle(labelStyle);
        zoomLabel.setStyle(labelStyle);

        debugOverlay.getChildren().addAll(fpsLabel, mouseLabel, moneyLabel, zoomLabel);
    }

    private void updateDebugInfo(long now) {
        if (!showDebug) return;

        // อัพเดท FPS
        frameCount++;
        if (now - lastTime >= 1_000_000_000) {
            fpsLabel.setText(String.format("FPS: %d", frameCount));
            frameCount = 0;
            lastTime = now;
        }

        // อัพเดทข้อมูล money จาก GameState
        GameState currentState = saveManager.loadGame();
        moneyLabel.setText(String.format("Money: %d", currentState.getMoney()));

        // อัพเดท zoom level
        Group worldGroup = (Group) gameArea.getChildren().get(0);
        zoomLabel.setText(String.format("Zoom: %.2fx", worldGroup.getScaleX()));
    }

    private void updateMousePosition(double x, double y) {
        mouseLabel.setText(String.format("Mouse: %.0f, %.0f", x, y));
    }

    private void exitGame() {
        saveGame();  // บันทึกเกม
        debugTimer.stop();  // หยุด debug timer
        stopAllGameObjects();  // หยุดการทำงานของ game objects ทั้งหมด
        navigator.showPlayMenu();  // กลับไปหน้าเมนู
    }

    private void stopAllGameObjects() {
        if (gameObjects != null) {
            for (GameObject obj : gameObjects) {
                obj.stop();  // ต้องมีเมธอด stop() ใน GameObject
            }
            gameObjects.clear();
        }
    }
} 