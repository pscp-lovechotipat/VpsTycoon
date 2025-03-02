package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameObject;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private boolean isPanning = false;

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
        Pane tableLayer = createTableLayer();
        Pane serverLayer = createServerLayer();

        Group worldGroup = new Group(backgroundLayer, objectsContainer , tableLayer, serverLayer , monitorLayer);

        gameArea.getChildren().add(worldGroup);

        HBox menuBar = this.createMenuBar();
        StackPane.setAlignment(menuBar, Pos.TOP_CENTER);

        VBox debugOverlay = debugOverlayManager.getDebugOverlay();
        StackPane.setAlignment(debugOverlay, Pos.BOTTOM_LEFT);

        // Overload
        if (!rootStack.getChildren().isEmpty()) {
            rootStack.getChildren().clear();
        }
        rootStack.getChildren().addAll(gameArea, menuBar, debugOverlay);

        debugOverlayManager.startTimer();

        setupZoomAndPan(worldGroup);

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
        monitorLayer.setPrefWidth(100);
        monitorLayer.setPrefHeight(100);
        monitorLayer.setStyle("""
            -fx-background-image: url('/images/Moniter/MoniterF2.png');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-scale-x: 2;
            -fx-scale-y: 2;
            -fx-translate-x: 750px;
            -fx-translate-y: 520px;
        """);
        monitorLayer.setOnMouseClicked((MouseEvent e) -> openSimulationDesktop());
        return monitorLayer;
    }
    private Pane createTableLayer() {
        Pane tableLayer = new Pane();
        tableLayer.setPrefWidth(400);
        tableLayer.setPrefHeight(400);
        tableLayer.setPrefHeight(400);
        tableLayer.setStyle("""
        -fx-background-image: url('/images/Table/Table.png');
        -fx-background-size: contain;
        -fx-background-repeat: no-repeat;
        -fx-background-position: center;
        -fx-translate-x: 400px;
        -fx-translate-y: 350px;
    """);
        return tableLayer;
    }

    private Pane createServerLayer() {
        Pane serverLayer = new Pane();
        serverLayer.setPrefWidth(400);
        serverLayer.setPrefHeight(400);
        serverLayer.setStyle("""
        -fx-background-image: url('/images/servers/server.png');
        -fx-background-size: contain;
        -fx-background-repeat: no-repeat;
        -fx-background-position: center;
        -fx-translate-x: 550px;
        -fx-translate-y: 350px;
    """);
        return serverLayer;
    }

    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.setPadding(new Insets(40));
        menuBar.setAlignment(Pos.CENTER);
        menuBar.setPrefHeight(50);
        menuBar.setMaxHeight(50);

        // 1. Deploy (แสดงสถานะ deploy VPS)
        VBox deployStatus = createCircleButtonStack(
                "Deploy", 
                12, 
                Color.rgb(240, 50, 50),   // สีแดงอ่อนด้านบน
                Color.rgb(180, 20, 20)   // สีแดงเข้มด้านล่าง
        );

        // 2. Network (แสดงสถานะเครือข่าย)
        VBox networkStatus = createCircleButtonStack(
                "Network", 
                8, 
                Color.rgb(50, 150, 240),   // สีฟ้าอ่อนด้านบน
                Color.rgb(20, 100, 200)   // สีน้ำเงินเข้มด้านล่าง
        );

        // 3. Security (แสดงสถานะความปลอดภัย)
        VBox securityStatus = createCircleButtonStack(
                "Security", 
                5, 
                Color.rgb(150, 50, 220),   // สีม่วงอ่อนด้านบน
                Color.rgb(100, 20, 180)   // สีม่วงเข้มด้านล่าง
        );

        // 4. Marketing (แสดงสถานะการตลาด)
        VBox marketingStatus = createCircleButtonStack(
                "Marketing", 
                10, 
                Color.rgb(50, 200, 100),   // สีเขียวอ่อนด้านบน
                Color.rgb(20, 150, 50)    // สีเขียวเข้มด้านล่าง
        );

        // เพิ่มทุกตัวแสดงสถานะลงใน menuBar โดยเรียงจากซ้ายไปขวา
        menuBar.getChildren().addAll(deployStatus, networkStatus, securityStatus, marketingStatus);

        return menuBar;
    }

    /**
     * สร้างตัวแสดงสถานะวงกลมพร้อมป้ายกำกับใต้วงกลม
     * @param labelText ข้อความบนป้ายกำกับ
     * @param number ตัวเลขที่จะแสดงในวงกลม
     * @param topColor สีด้านบนของ gradient
     * @param bottomColor สีด้านล่างของ gradient
     * @return VBox ที่ประกอบด้วยวงกลมและป้ายกำกับ
     */
    private VBox createCircleButtonStack(String labelText, int number, Color topColor, Color bottomColor) {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);

        // สร้างวงกลมขาวด้านนอก (เงา)
        Circle outerCircle = new Circle(38);
        outerCircle.setEffect(new DropShadow(10, Color.BLACK));
        Stop[] outerCircleStops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(220, 220, 220))
        };
        LinearGradient outerCircleGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, outerCircleStops);
        outerCircle.setFill(outerCircleGradient);

        // สร้างวงกลมสีด้านใน (gradient)
        Circle innerCircle = new Circle(30);
        DropShadow innerShadow = new DropShadow();
        innerShadow.setRadius(2);
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        innerShadow.setOffsetY(2);
        innerCircle.setEffect(innerShadow);
        
        // สร้าง gradient จากสีที่กำหนด
        Stop[] stops = new Stop[] {
                new Stop(0, topColor),
                new Stop(1, bottomColor)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        innerCircle.setFill(gradient);

        // เพิ่มตัวเลข
        Label numberLabel = new Label(String.valueOf(number));
        numberLabel.setTextFill(Color.WHITE);
        numberLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // ซ้อนวงกลมและตัวเลข
        StackPane circleStack = new StackPane();
        circleStack.getChildren().addAll(outerCircle, innerCircle, numberLabel);

        // สร้างป้ายกำกับด้านล่าง
        Label textLabel = new Label(labelText);
        textLabel.setPrefWidth(80);
        
        // สร้าง gradient สำหรับป้ายกำกับ
        Stop[] labelStops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(220, 220, 220))
        };
        LinearGradient labelGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, labelStops);
        
        // ตั้งค่าพื้นหลังและขอบ
        textLabel.setBackground(new Background(new BackgroundFill(
                labelGradient, new CornerRadii(4), Insets.EMPTY)));
        
        // เพิ่ม padding ให้กับป้ายกำกับ
        textLabel.setPadding(new Insets(6));
        
        // ตั้งค่าเงา
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setOffsetY(1);
        textLabel.setEffect(shadow);
        
        // ตั้งค่าข้อความ
        textLabel.setTextFill(Color.rgb(100, 100, 100));
        textLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        textLabel.setAlignment(Pos.CENTER);

        // เพิ่มทั้งหมดลงใน VBox
        container.getChildren().addAll(circleStack, textLabel);
        
        return container;
    }

    /**
     * เพิ่มการซูมด้วย Scroll
     */
    private void setupZoomAndPan(Group worldGroup) {
        // 1. ซูมด้วย Scroll (คงเดิม)
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

        // 1.1 เพิ่มการซูมด้วยการถ่างนิ้วบน touchpad
        gameArea.setOnZoom(e -> {
            // ใช้ค่า ZoomFactor จาก event โดยตรง
            double zoomFactor = e.getZoomFactor();
            
            // คำนวณค่า scale ใหม่ โดยใช้ค่า zoomFactor จาก gesture
            double newScale = worldGroup.getScaleX() * zoomFactor;
            
            // กำหนดค่า scale ต่ำสุดและสูงสุด
            double minScale = 0.5;
            double maxScale = 2.0;
            
            // ตรวจสอบและปรับค่า newScale ให้อยู่ในช่วงที่กำหนด
            newScale = Math.max(minScale, Math.min(newScale, maxScale));
            
            worldGroup.setScaleX(newScale);
            worldGroup.setScaleY(newScale);
            
            // อัพเดท Debug info ถ้ามี
            if (showDebug) {
                debugOverlayManager.updateGameInfo(rootStack);
            }
            
            // ไม่ลืม consume
            e.consume();
        });

        // 2. เพิ่มการจับเมาส์ดาวน์เพื่อเริ่มการลาก (pan)
        worldGroup.setOnMousePressed(e -> {
            // บันทึกตำแหน่งเริ่มต้นของเมาส์และ group
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
            translateAnchorX = worldGroup.getTranslateX();
            translateAnchorY = worldGroup.getTranslateY();
            isPanning = true;
            
            // เปลี่ยน cursor เป็นรูปมือกำ
            gameArea.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            
            e.consume();
        });

        // 3. เพิ่มการเลื่อน Group ตามการลากเมาส์
        worldGroup.setOnMouseDragged(e -> {
            if (isPanning) {
                // คำนวณระยะที่ต้องเลื่อน
                double deltaX = e.getSceneX() - mouseAnchorX;
                double deltaY = e.getSceneY() - mouseAnchorY;
                
                // เลื่อน Group
                worldGroup.setTranslateX(translateAnchorX + deltaX);
                worldGroup.setTranslateY(translateAnchorY + deltaY);
                
                // อัพเดท Debug info ถ้ามี
                if (showDebug) {
                    debugOverlayManager.updateGameInfo(rootStack);
                }
                
                e.consume();
            }
        });

        // 4. หยุดการลากเมื่อปล่อยเมาส์
        worldGroup.setOnMouseReleased(e -> {
            if (isPanning) {
                isPanning = false;
                // เปลี่ยน cursor กลับเป็นปกติ
                gameArea.setCursor(javafx.scene.Cursor.DEFAULT);
                e.consume();
            }
        });

        // 5. เมื่อเมาส์เข้าไปในพื้นที่ให้เปลี่ยน cursor เป็นรูปมือ
        worldGroup.setOnMouseEntered(e -> {
            gameArea.setCursor(javafx.scene.Cursor.HAND);
        });

        // 6. เมื่อเมาส์ออกจากพื้นที่ให้เปลี่ยน cursor กลับเป็นปกติ
        worldGroup.setOnMouseExited(e -> {
            if (!isPanning) {
                gameArea.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        // 7. ใช้ปุ่ม spacebar สำหรับรีเซ็ตการซูมและตำแหน่ง (optional)
        gameArea.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                // รีเซ็ตการซูมและตำแหน่ง
                worldGroup.setScaleX(1.0);
                worldGroup.setScaleY(1.0);
                worldGroup.setTranslateX(0);
                worldGroup.setTranslateY(0);
                e.consume();
            }
        });
        
        // ทำให้สามารถรับ key event
        gameArea.setFocusTraversable(true);
    }

    /**
     * ติดตามการกดปุ่มบนคีย์บอร์ด (ESC, F3)
     */
    private void setupKeyEvents() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                showResumeScreen();
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
     * ตัวอย่าง "Desktop" อื่น (ถ้ายังต้องการ)
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
     * เปิดหน้า Resume
     */
    private void showResumeScreen() {
        ResumeScreen resumeScreen = new ResumeScreen(navigator, this::hideResumeScreen);
        
        // ตั้งค่าขนาดเท่ากับหน้าจอเกม
        resumeScreen.setPrefSize(gameArea.getWidth(), gameArea.getHeight());
        
        // เพิ่มเข้าไปใน gameArea หรือ rootStack แล้วแต่โครงสร้าง
        gameArea.getChildren().add(resumeScreen);
    }

    /**
     * ซ่อนหน้า Resume และกลับไปเล่นเกม
     */
    private void hideResumeScreen() {
        // ลบหน้า Resume ออกจาก gameArea
        gameArea.getChildren().removeIf(node -> node instanceof ResumeScreen);
    }

    public Group getWorldGroup() {
        return worldGroup;
    }

    public void setWorldGroup(Group worldGroup) {
        this.worldGroup = worldGroup;
    }
}
