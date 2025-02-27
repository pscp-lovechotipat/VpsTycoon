package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameLoop;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.desktop.DesktopScreen;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.navigation.Navigator;
import com.vpstycoon.ui.utils.ButtonUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.List;

/**
 * คลาสนี้แทนโค้ดใน createContent() เดิม
 * สืบทอดจาก BorderPane หรือ Pane อื่นๆ ตามสะดวก
 */
public class GameplayContentPane extends BorderPane {

    private final StackPane rootStack;
    private Group worldGroup = new Group();
    private final StackPane gameArea;

    private final List<GameObject> gameObjects;
    private final Navigator navigator;
    private final ChatSystem chatSystem;

    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final GameFlowManager gameFlowManager;
    private final DebugOverlayManager debugOverlayManager;
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

        setupUI();

        debugOverlayManager.startTimer();

        setCenter(rootStack);

        setupKeyEvents();

        setOnMouseMoved(e -> {
            if (showDebug) {
                debugOverlayManager.updateMousePosition(e.getX(), e.getY());
                debugOverlayManager.updateGameInfo(rootStack);
            }
        });

        System.out.println("Children in rootStack: " + rootStack.getChildren().size());
        System.out.println("gameArea size: " + gameArea.getWidth() + " x " + gameArea.getHeight());

    }

    /**
     * สร้าง UI หลัก คล้ายๆ createContent เดิม
     */
    private void setupUI() {
        Pane backgroundLayer = createBackgroundLayer();
        Pane objectsContainer = createObjectsContainer();
        Pane monitorLayer = createMonitorLayer();

        Group worldGroup = new Group(backgroundLayer, objectsContainer, monitorLayer);

        gameArea.getChildren().add(worldGroup);

        HBox menuBar = createMenuBar();
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);

        // Overload
        if (!rootStack.getChildren().isEmpty()) {
            rootStack.getChildren().clear();
        }
        rootStack.getChildren().addAll(gameArea, menuBar, debugOverlay);

        debugOverlayManager.startTimer();

        setupZoom(worldGroup);

        setStyle("-fx-background-color: #000000;");
    }

    /**
     * คล้ายในโค้ดเดิม
     */
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

            // คำนวณตำแหน่งตามช่อง (gridX, gridY)
            double snappedX = Math.round(obj.getX() / cellSize) * cellSize;
            double snappedY = Math.round(obj.getY() / cellSize) * cellSize;

            view.setTranslateX(snappedX);
            view.setTranslateY(snappedY);

            view.setOnMouseClicked(e -> showObjectDetails(obj));

            objectsContainer.getChildren().add(view);
        }
        return objectsContainer;
    }

    private Pane createMonitorLayer() {
        // โหลดภาพ
        Image monitorImage = new Image("/images/Moniter/MoniterF2.png");

        // ดึงขนาดของภาพ
        double imageWidth = monitorImage.getWidth();
        double imageHeight = monitorImage.getHeight();

        // สร้าง Pane และตั้งค่าขนาดให้เท่ากับภาพ
        Pane monitorLayer = new Pane();
        monitorLayer.setPrefWidth(imageWidth);
        monitorLayer.setPrefHeight(imageHeight);
        monitorLayer.setStyle("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-translate-x: 550px;
            -fx-translate-y: 320px;
        """);
        monitorLayer.setOnMouseClicked((MouseEvent e) -> openSimulationDesktop());
        return monitorLayer;
    }

    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.setPadding(new Insets(20));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setPrefHeight(50);
        menuBar.setMaxHeight(50);

        menuBar.setStyle("-fx-background-color: #2C3E50;");

        // ใช้ UIUtils สร้างปุ่ม
        Button saveButton = ButtonUtils.createButton("Save Game");
        saveButton.setOnAction(e -> showConfirmationDialog(
                "Save Game",
                "Do you want to save your current progress?",
                gameFlowManager::saveGame
        ));

        Button menuButton = ButtonUtils.createButton("Main Menu");
        menuButton.setOnAction(e -> showConfirmationDialog(
                "Return to Main Menu",
                "Do you want to save and return to the main menu?",
                () -> {
                    gameFlowManager.saveGame();
                    navigator.showMainMenu();
                }
        ));

        menuBar.getChildren().addAll(saveButton, menuButton);
        return menuBar;
    }

    /**
     * เพิ่มการซูมด้วย Scroll
     */
    private void setupZoom(Group worldGroup) {
        gameArea.setOnScroll(e -> {
            double zoomFactor = 1.05;
            if (e.getDeltaY() < 0) {
                zoomFactor = 1.0 / zoomFactor;
            }
            // คำนวณค่า scale ใหม่
            double newScale = worldGroup.getScaleX() * zoomFactor;

            // กำหนดค่า scale ต่ำสุดและสูงสุด
            double minScale = 0.5;
            double maxScale = 2.0;

            // ตรวจสอบและปรับค่า newScale ให้อยู่ในช่วงที่กำหนด
            newScale = Math.max(minScale, Math.min(newScale, maxScale));

            worldGroup.setScaleX(newScale);
            worldGroup.setScaleY(newScale);

            // ไม่ลืม consume
            e.consume();
        });
    }

    /**
     * ติดตามการกดปุ่มบนคีย์บอร์ด (ESC, F3)
     */
    private void setupKeyEvents() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                exitGame();
            } else if (event.getCode() == KeyCode.F3) {
                showDebug = !showDebug;
                debugOverlayManager.toggleDebug();
            }
        });
    }

    /**
     * โชว์รายละเอียดของ GameObject
     */
    private void showObjectDetails(GameObject obj) {
        // สร้างหน้าต่าง Modal ใน gameArea
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(obj.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label statusLabel = new Label("Status: " + obj.getStatus());
        Label levelLabel = new Label("Level: " + obj.getLevel());

        // ปุ่ม Upgrade
        Button upgradeButton = ButtonUtils.createModalButton("Upgrade");
        upgradeButton.setOnAction(e -> {
            obj.upgrade(null); // ถ้าต้องการส่ง state ก็ปรับตามโค้ดเดิม
            levelLabel.setText("Level: " + obj.getLevel());
            statusLabel.setText("Status: " + obj.getStatus());
            gameFlowManager.saveGame(); // เซฟเกมหลังอัปเกรด
        });

        // ปุ่ม Close
        Button closeButton = ButtonUtils.createModalButton("Close");
        closeButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        modalContent.getChildren().addAll(titleLabel, new Separator(), statusLabel, levelLabel, upgradeButton, closeButton);

        modalContainer.getChildren().add(modalContent);

        // คลิกนอก modal เพื่อปิด
        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        gameArea.getChildren().add(modalContainer);
    }

    /**
     * เปิด Desktop Simulation (Monitor ถูกคลิก)
     */
    private void openSimulationDesktop() {
        DesktopScreen desktop = new DesktopScreen(
                0.0,               // ตัวอย่าง อาจส่ง companyRating
                0,                 // ตัวอย่าง อาจส่ง marketingPoints
                chatSystem,
                requestManager,
                vpsManager
        );
        StackPane.setAlignment(desktop, Pos.CENTER);
        desktop.setMaxSize(gameArea.getWidth() * 0.8, gameArea.getHeight() * 0.8);

        // แทนที่ทุกอย่างใน gameArea
        gameArea.getChildren().clear();
        gameArea.getChildren().add(desktop);

        // add exit button
        Button exitButton = ButtonUtils.createModalButton("Exit Desktop");
        exitButton.setOnAction(e -> returnToRoom());
        StackPane.setAlignment(exitButton, Pos.BOTTOM_RIGHT);
        gameArea.getChildren().add(exitButton);

    }

    /**
     * ตัวอย่าง “Desktop” อื่น (ถ้ายังต้องการ)
     */
    private void openVPSDesktop() {
        ImageView vpsDesktopView = new ImageView(new Image("/images/others/logo.png"));
        double scaleFactor = 0.175;
        vpsDesktopView.setFitWidth(gameArea.getWidth() * scaleFactor);
        vpsDesktopView.setFitHeight(gameArea.getHeight() * scaleFactor);
        vpsDesktopView.setOnMouseClicked(e -> returnToRoom());

        gameArea.getChildren().clear();
        gameArea.getChildren().add(vpsDesktopView);
    }

    private void returnToRoom() {
        gameArea.getChildren().clear();
        setupUI(); // สร้างใหม่ หรืออาจเก็บ state เดิม
    }

    /**
     * Show Dialog ยืนยันอะไรบางอย่าง
     */
    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setMaxWidth(250);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-wrap-text: true; -fx-text-alignment: center;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button confirmButton = ButtonUtils.createModalButton("Yes");
        confirmButton.setOnAction(e -> {
            onConfirm.run();
            gameArea.getChildren().remove(modalContainer);
        });

        Button cancelButton = ButtonUtils.createModalButton("No");
        cancelButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        modalContent.getChildren().addAll(titleLabel, new Separator(), messageLabel, buttonBox);
        modalContainer.getChildren().add(modalContent);

        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        gameArea.getChildren().add(modalContainer);
    }

    /**
     * ออกจากเกม (กด ESC)
     */
    private void exitGame() {
        // เซฟเกมก่อน
        gameFlowManager.saveGame();
        // หยุด timer debug
        debugOverlayManager.stopTimer();
        // หยุด game objects
        gameFlowManager.stopAllGameObjects();
        // กลับไปเมนู
        navigator.showPlayMenu();
    }

    public Group getWorldGroup() {
        return worldGroup;
    }

    public void setWorldGroup(Group worldGroup) {
        this.worldGroup = worldGroup;
    }
}
