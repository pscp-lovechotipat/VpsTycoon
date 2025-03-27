package com.vpstycoon.ui.game.notification.center;

import com.vpstycoon.FontLoader;
import com.vpstycoon.audio.AudioManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class CenterNotificationView extends StackPane {
    private AudioManager audioManager;
    private CenterNotificationModel model;
    private StackPane currentOverlay;

    public CenterNotificationView() {
        setAlignment(Pos.CENTER);
        setPickOnBounds(false);
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public void setModel(CenterNotificationModel model) {
        this.model = model;
        showNextNotification(); // เริ่มแสดง notification แรก
    }

    public void addNotificationPane(String title, String content) {
        createAndShowNotification(title, content, null);
    }

    public void addNotificationPane(String title, String content, String image) {
        createAndShowNotification(title, content, image);
    }

    /**
     * เพิ่ม notification ที่จะปิดอัตโนมัติหลังจากเวลาที่กำหนด
     * 
     * @param title หัวข้อ
     * @param content เนื้อหา
     * @param image รูปภาพ
     * @param autoCloseMillis เวลาในหน่วย millisecond ที่จะปิดอัตโนมัติ
     */
    public void addNotificationPaneAutoClose(String title, String content, String image, long autoCloseMillis) {
        model.addNotification(new CenterNotificationModel.Notification(title, content, image));
        if (currentOverlay == null) {
            showNextNotificationAutoClose(autoCloseMillis);
        }
    }

    /**
     * สร้าง Task notification พร้อมปุ่มเริ่มเกม
     * 
     * @param title หัวข้อการแจ้งเตือน
     * @param content เนื้อหาการแจ้งเตือน
     * @param image ภาพประกอบการแจ้งเตือน
     * @param onStartTask callback เมื่อกดปุ่มเริ่มเกม
     */
    public void createAndShowTaskNotification(String title, String content, String image, Runnable onStartTask) {
        if (currentOverlay != null) {
            // ถ้ามี notification แสดงอยู่แล้ว รอให้มันปิดก่อน แล้วค่อยแสดงอันใหม่
            model.addNotification(new CenterNotificationModel.Notification(title, content, image));
            return;
        }
        
        // สร้าง overlay background
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // สร้าง notification pane สำหรับ task
        VBox notificationPane = createTaskNotificationPane(title, content, 
                                 (image != null) ? new CenterNotificationModel.Notification(title, content, image).getImage() : null, 
                                 onStartTask);

        // เพิ่ม notificationPane ลงใน overlay
        overlay.getChildren().add(notificationPane);
        StackPane.setAlignment(notificationPane, Pos.CENTER);

        // ป้องกันการคลิกใน notificationPane ส่งผลให้ปิด
        notificationPane.setOnMouseClicked(e -> e.consume());

        // เพิ่ม overlay ลงใน view
        getChildren().add(overlay);
        currentOverlay = overlay;

        // เล่น animation fade in
        playFadeInAnimation(notificationPane);
        
        // เล่นเสียงแจ้งเตือน
        if (audioManager != null) {
            audioManager.playSoundEffect("notification.mp3");
        }
    }

    /**
     * สร้าง VBox สำหรับแสดง notification ของ task พร้อมปุ่มเริ่มเกม
     */
    private VBox createTaskNotificationPane(String title, String content, Image image, Runnable onStartTask) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));
        pane.setAlignment(Pos.CENTER);
        pane.setMaxWidth(500);
        pane.setMaxHeight(pane.getHeight());

        // Cyberpunk style
        pane.setStyle("""
            -fx-background-color: rgba(40, 10, 60, 0.9);
            -fx-border-color: #00ffff;
            -fx-border-width: 2px;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(gaussian, rgba(0, 255, 255, 0.7), 15, 0, 0, 0);
        """);

        // Close button ด้านมุมบน
        Button closeButton = new Button("X");
        closeButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #ff5555;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-padding: 0 5 0 5;
        """);
        closeButton.setOnAction(e -> fadeOutAndRemove((StackPane) pane.getParent()));
        StackPane closePane = new StackPane(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(FontLoader.TITLE_FONT);
        titleLabel.setTextFill(Color.rgb(0, 255, 255));
        titleLabel.setAlignment(Pos.CENTER);

        // Content
        Label contentLabel = new Label(content);
        contentLabel.setFont(FontLoader.LABEL_FONT);
        contentLabel.setTextFill(Color.WHITE);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(460);
        contentLabel.setAlignment(Pos.CENTER);

        // ปุ่มเริ่มเกม
        Button startTaskButton = new Button("START TASK");
        startTaskButton.setStyle("""
            -fx-background-color: #00ffff;
            -fx-text-fill: #000000;
            -fx-font-weight: bold;
            -fx-font-size: 16px;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            -fx-background-radius: 5;
        """);
        startTaskButton.setOnAction(e -> {
            // ปิด notification
            fadeOutAndRemove((StackPane) pane.getParent());
            // เรียก callback
            if (onStartTask != null) {
                onStartTask.run();
            }
        });
        
        // Effect เมื่อ hover
        startTaskButton.setOnMouseEntered(e -> 
            startTaskButton.setStyle("""
                -fx-background-color: #ffffff;
                -fx-text-fill: #000000;
                -fx-font-weight: bold;
                -fx-font-size: 16px;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-background-radius: 5;
                -fx-effect: dropshadow(gaussian, #00ffff, 10, 0.5, 0, 0);
            """)
        );
        
        startTaskButton.setOnMouseExited(e -> 
            startTaskButton.setStyle("""
                -fx-background-color: #00ffff;
                -fx-text-fill: #000000;
                -fx-font-weight: bold;
                -fx-font-size: 16px;
                -fx-padding: 10 20;
                -fx-cursor: hand;
                -fx-background-radius: 5;
            """)
        );

        // Add elements
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);
            pane.getChildren().addAll(closePane, titleLabel, contentLabel, imageView, startTaskButton);
        } else {
            pane.getChildren().addAll(closePane, titleLabel, contentLabel, startTaskButton);
        }

        // Glow effect
        Glow glow = new Glow(0.4);
        pane.setEffect(glow);

        return pane;
    }

    private void createAndShowNotification(String title, String content, String image) {
        model.addNotification(new CenterNotificationModel.Notification(title, content, image));
        if (currentOverlay == null) {
            showNextNotification();
        }
    }

    private void showNextNotification() {
        if (model.hasNotifications() && currentOverlay == null) {
            CenterNotificationModel.Notification next = model.getNextNotification();
            if (next != null) {
                // สร้าง overlay background
                StackPane overlay = new StackPane();
                overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
                overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // สร้าง notification pane
                VBox notificationPane = createNotificationPane(next.getTitle(), next.getContent(), next.getImage());

                // เพิ่ม notificationPane ลงใน overlay
                overlay.getChildren().add(notificationPane);
                StackPane.setAlignment(notificationPane, Pos.CENTER);

                // ตั้งค่าให้คลิก overlay เพื่อปิด
                overlay.setOnMouseClicked(e -> {
                    if (!notificationPane.getBoundsInParent().contains(e.getX(), e.getY())) {
                        fadeOutAndRemove(overlay);
                    }
                });

                // ป้องกันการคลิกใน notificationPane ส่งผลให้ปิด
                notificationPane.setOnMouseClicked(e -> e.consume());

                // เพิ่ม overlay ลงใน view
                getChildren().add(overlay);
                currentOverlay = overlay;

                // เล่น animation fade in
                playFadeInAnimation(notificationPane);
            }
        }
    }

    /**
     * แสดง notification ถัดไปที่จะปิดอัตโนมัติหลังจากเวลาที่กำหนด
     * 
     * @param autoCloseMillis เวลาในหน่วย millisecond ที่จะปิดอัตโนมัติ
     */
    private void showNextNotificationAutoClose(long autoCloseMillis) {
        if (model.hasNotifications() && currentOverlay == null) {
            CenterNotificationModel.Notification next = model.getNextNotification();
            if (next != null) {
                // สร้าง overlay background
                StackPane overlay = new StackPane();
                overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
                overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // สร้าง notification pane
                VBox notificationPane = createNotificationPane(next.getTitle(), next.getContent(), next.getImage());

                // เพิ่ม notificationPane ลงใน overlay
                overlay.getChildren().add(notificationPane);
                StackPane.setAlignment(notificationPane, Pos.CENTER);

                // ตั้งค่าให้คลิก overlay เพื่อปิด
                overlay.setOnMouseClicked(e -> {
                    if (!notificationPane.getBoundsInParent().contains(e.getX(), e.getY())) {
                        fadeOutAndRemove(overlay);
                    }
                });

                // ป้องกันการคลิกใน notificationPane ส่งผลให้ปิด
                notificationPane.setOnMouseClicked(e -> e.consume());

                // เพิ่ม overlay ลงใน view
                getChildren().add(overlay);
                currentOverlay = overlay;

                // เล่น animation fade in
                playFadeInAnimation(notificationPane);
                
                // ตั้งเวลาให้ notification หายไปอัตโนมัติ
                Thread autoCloseThread = new Thread(() -> {
                    try {
                        Thread.sleep(autoCloseMillis);
                        if (overlay == currentOverlay) { // ตรวจสอบว่ายังเป็น overlay ปัจจุบันอยู่ไหม
                            Platform.runLater(() -> fadeOutAndRemove(overlay));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                autoCloseThread.setDaemon(true);
                autoCloseThread.start();
            }
        }
    }

    private VBox createNotificationPane(String title, String content, Image image) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));
        pane.setAlignment(Pos.CENTER);
        pane.setMaxWidth(500);
        pane.setMaxHeight(pane.getHeight());

        // Cyberpunk style
        pane.setStyle("""
            -fx-background-color: rgba(40, 10, 60, 0.9);
            -fx-border-color: #6a00ff;
            -fx-border-width: 2px;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-effect: dropshadow(gaussian, rgba(106, 0, 255, 0.7), 15, 0, 0, 0);
        """);

        // Close button
        Button closeButton = new Button("X");
        closeButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #ff5555;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-padding: 0 5 0 5;
        """);
        closeButton.setOnAction(e -> fadeOutAndRemove((StackPane) pane.getParent()));
        StackPane closePane = new StackPane(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(FontLoader.TITLE_FONT);
        titleLabel.setTextFill(Color.rgb(0, 255, 255));
        titleLabel.setAlignment(Pos.CENTER);

        // Content
        Label contentLabel = new Label(content);
        contentLabel.setFont(FontLoader.LABEL_FONT);
        contentLabel.setTextFill(Color.WHITE);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(460);
        contentLabel.setAlignment(Pos.CENTER);

        // Add elements
        if (image != null) { // Check for null image
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);
            pane.getChildren().addAll(closePane, titleLabel, contentLabel, imageView);
        } else {
            pane.getChildren().addAll(closePane, titleLabel, contentLabel);
        }

        // Glow effect
        Glow glow = new Glow(0.4);
        pane.setEffect(glow);

        return pane;
    }

    private void playFadeInAnimation(VBox notificationPane) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), notificationPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void fadeOutAndRemove(StackPane overlay) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), overlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            getChildren().remove(overlay);
            currentOverlay = null;
            showNextNotification(); // แสดง notification ถัดไปเมื่ออันปัจจุบันปิด
        });
        fadeOut.play();
    }
}