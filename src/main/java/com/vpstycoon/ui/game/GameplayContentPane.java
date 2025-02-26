package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.desktop.DesktopScreen;
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

        // สร้าง StackPane หลัก (rootStack)
        // สำหรับซ้อน "ส่วนแสดงเกม" (mainPane / gameArea) และ "DebugOverlay"
        StackPane rootStack = new StackPane();

        // 1) สร้าง "mainPane" หรือ "gameArea" สำหรับเนื้อหาเกม
        //    (ในที่นี้ใช้ StackPane gameArea ตามโค้ดเดิม)
        this.gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);

        // เรียกเมธอด setupUI() เพื่อสร้าง background, objects, menu bar ฯลฯ
        setupUI();
        // ตอนนี้เนื้อหาเกมอยู่ใน this.gameArea ด้านใน

        // นำ gameArea วางเป็น child ล่างสุดใน rootStack
        rootStack.getChildren().add(gameArea);

        // 2) เพิ่ม debug overlay ลอยซ้อนด้านบน
        rootStack.getChildren().add(debugOverlayManager.getDebugOverlay());
        // เริ่มจับ FPS (AnimationTimer)
        debugOverlayManager.startTimer();

        // 3) ตั้ง rootStack เป็น Center ของ BorderPane (this)
        setCenter(rootStack);

        // 4) ตั้งค่า key events
        setupKeyEvents();

        // 5) หากต้องการติดตาม mouse move เพื่อ debug
        setOnMouseMoved(e -> {
            if (showDebug) {
                debugOverlayManager.updateMousePosition(e.getX(), e.getY());
                debugOverlayManager.updateGameInfo(new GameSaveManager(),rootStack);
            }
        });
    }

    /**
     * สร้าง UI หลัก คล้ายๆ createContent เดิม
     */
    private void setupUI() {
        // กำหนด style พื้นหลังของ root นี้
        setStyle("-fx-background-color: #000000;");

        // สร้าง backgroundLayer
        Pane backgroundLayer = createBackgroundLayer();

        // สร้าง objectsContainer
        Pane objectsContainer = createObjectsContainer();

        // สร้าง monitorLayer (ไว้คลิกเปิด Desktop Simulation)
        Pane monitorLayer = createMonitorLayer();

        Group worldGroup = new Group(backgroundLayer, objectsContainer, monitorLayer);
        gameArea.getChildren().add(worldGroup);

        // DebugOverlay จาก DebugOverlayManager
        gameArea.getChildren().add(debugOverlayManager.getDebugOverlay());
        debugOverlayManager.startTimer(); // เริ่มจับ FPS

        // สร้างเมนูบาร์ และใส่ด้านบน (top) ของ BorderPane
        HBox menuBar = createMenuBar();
        setTop(menuBar);

        // เพิ่มการซูมด้วย scroll
        setupZoom(worldGroup);

        // focusable
        setFocusTraversable(true);
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
        backgroundLayer.prefHeightProperty().bind(gameArea.heightProperty());
        return backgroundLayer;
    }

    private Pane createObjectsContainer() {
        Pane objectsContainer = new Pane();
        for (GameObject obj : gameObjects) {
            GameObjectView view = new GameObjectView(obj);
            view.setOnMouseClicked(e -> showObjectDetails(obj));
            objectsContainer.getChildren().add(view);
        }
        return objectsContainer;
    }

    private Pane createMonitorLayer() {
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
        return monitorLayer;
    }

    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.setPadding(new Insets(20));
        menuBar.setAlignment(Pos.CENTER_LEFT);
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
            worldGroup.setScaleX(worldGroup.getScaleX() * zoomFactor);
            worldGroup.setScaleY(worldGroup.getScaleY() * zoomFactor);
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
}
