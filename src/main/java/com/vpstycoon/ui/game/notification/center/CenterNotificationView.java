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
     * สร้าง Task notification พร้อมปุ่มเริ่มเกมและปุ่มยกเลิก
     * 
     * @param title หัวข้อการแจ้งเตือน
     * @param content เนื้อหาการแจ้งเตือน
     * @param image ภาพประกอบการแจ้งเตือน
     * @param onStartTask callback เมื่อกดปุ่มเริ่มเกม
     */
    public void createAndShowTaskNotification(String title, String content, String image, Runnable onStartTask) {
        createAndShowTaskNotification(title, content, image, onStartTask, onStartTask);
    }

    /**
     * สร้าง Task notification พร้อมปุ่มเริ่มเกมและปุ่มยกเลิกแยกกัน
     * 
     * @param title หัวข้อการแจ้งเตือน
     * @param content เนื้อหาการแจ้งเตือน
     * @param image ภาพประกอบการแจ้งเตือน
     * @param onStartTask callback เมื่อกดปุ่มเริ่มเกม
     * @param onAbortTask callback เมื่อกดปุ่มยกเลิกเกม
     */
    public void createAndShowTaskNotification(String title, String content, String image, Runnable onStartTask, Runnable onAbortTask) {
        try {
            // Queue notification if another one is active
            if (currentOverlay != null) {
                if (model != null) {
                    model.addNotification(new CenterNotificationModel.Notification(title, content, image));
                    System.out.println("Task notification queued: " + title);
                } else {
                    System.err.println("Cannot queue notification: model is null");
                }
                return;
            }
            
            // สร้าง overlay background
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            overlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            // Prepare image object
            Image imageObj = null;
            if (image != null) {
                try {
                    // Try getting from model first
                    if (model != null) {
                        imageObj = new CenterNotificationModel.Notification(title, content, image).getImage();
                    } 
                    
                    // If still null, try to load directly
                    if (imageObj == null) {
                        imageObj = new Image(image);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading notification image: " + e.getMessage());
                }
            }

            // สร้าง notification pane สำหรับ task
            VBox notificationPane = createTaskNotificationPane(title, content, imageObj, onStartTask, onAbortTask);

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
            
            System.out.println("Task notification displayed: " + title);
        } catch (Exception e) {
            System.err.println("Error creating task notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * สร้าง VBox สำหรับแสดง notification ของ task พร้อมปุ่มเริ่มเกม
     */
    private VBox createTaskNotificationPane(String title, String content, Image image, Runnable onStartTask, Runnable onAbortTask) {
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
        Button closeButton = new Button("Abort Task");
        closeButton.setStyle("""
            -fx-background-color: rgba(255, 0, 0, 0.3);
            -fx-text-fill: #ff5555;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
            -fx-padding: 3 8;
            -fx-border-color: #ff5555;
            -fx-border-width: 1px;
            -fx-border-radius: 3;
            -fx-background-radius: 3;
        """);
        closeButton.setOnAction(e -> {
            try {
                // Safety check for null parent
                StackPane parent = (StackPane) pane.getParent();
                if (parent != null) {
                    // Close the notification
                    fadeOutAndRemove(parent);
                } else {
                    System.err.println("Warning: Abort button clicked but parent pane is null");
                }
                
                // If we have an abort callback, execute it
                if (onAbortTask != null) {
                    onAbortTask.run();
                }
            } catch (Exception ex) {
                System.err.println("Error processing abort button click: " + ex.getMessage());
                ex.printStackTrace();
                
                // Try to run the abort callback even if removing the UI failed
                if (onAbortTask != null) {
                    try {
                        onAbortTask.run();
                    } catch (Exception ignored) {
                        // Ignore exceptions in callback as a last resort
                    }
                }
            }
        });
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
            try {
                // ปิด notification safely
                StackPane parent = (StackPane) pane.getParent();
                if (parent != null) {
                    fadeOutAndRemove(parent);
                } else {
                    System.err.println("Warning: START TASK clicked but parent pane is null");
                }
                
                // เรียก callback
                if (onStartTask != null) {
                    try {
                        onStartTask.run();
                    } catch (Exception callbackEx) {
                        System.err.println("Error in START TASK callback: " + callbackEx.getMessage());
                        callbackEx.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error processing START TASK button: " + ex.getMessage());
                ex.printStackTrace();
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
        try {
            // Skip if already showing an overlay or if the model is null
            if (currentOverlay != null || model == null) {
                return;
            }
            
            if (model.hasNotifications()) {
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
        } catch (Exception e) {
            System.err.println("Error showing next notification: " + e.getMessage());
            e.printStackTrace();
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
        Button closeButton = new Button("Close");
        closeButton.setStyle("""
            -fx-background-color: rgba(255, 0, 0, 0.3);
            -fx-text-fill: #ff5555;
            -fx-font-weight: bold;
            -fx-font-size: 12px;
            -fx-padding: 3 8;
            -fx-border-color: #ff5555;
            -fx-border-width: 1px;
            -fx-border-radius: 3;
            -fx-background-radius: 3;
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
        if (overlay == null) {
            System.err.println("Warning: Attempted to fade out a null overlay");
            return;
        }
        
        try {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), overlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    if (getChildren().contains(overlay)) {
                        getChildren().remove(overlay);
                    }
                    
                    // Reset the current overlay reference if it's this one
                    if (currentOverlay == overlay) {
                        currentOverlay = null;
                    }
                    
                    // Try to show the next notification
                    showNextNotification();
                } catch (Exception ex) {
                    System.err.println("Error in fadeOut completion: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            System.err.println("Error starting fadeOut animation: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: try direct removal
            try {
                if (getChildren().contains(overlay)) {
                    getChildren().remove(overlay);
                }
                
                if (currentOverlay == overlay) {
                    currentOverlay = null;
                }
                
                showNextNotification();
            } catch (Exception ex) {
                System.err.println("Error in fadeOut fallback removal: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}