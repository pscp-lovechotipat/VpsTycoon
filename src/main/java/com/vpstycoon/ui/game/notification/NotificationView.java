package com.vpstycoon.ui.game.notification;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class NotificationView extends VBox {
    public NotificationView() {
        setSpacing(10); // ระยะห่างระหว่างการแจ้งเตือน
        setPadding(new Insets(50)); // ขอบรอบนอก
    }

    // เพิ่มการแจ้งเตือนใหม่เข้าไปใน View
    public void addNotificationPane(String title, String content) {
        Pane notificationPane = createNotificationPane(title, content);
        getChildren().add(notificationPane);

        // สร้าง TranslateTransition สำหรับเลื่อนไปทางขวา
        TranslateTransition translateOutTransition = new TranslateTransition(Duration.seconds(1), notificationPane);
        translateOutTransition.setFromX(0); // เริ่มจากตำแหน่งปัจจุบัน
        translateOutTransition.setToX(300); // เลื่อนไปทางขวา 300 หน่วย

        TranslateTransition translateInTransition = new TranslateTransition(Duration.seconds(1), notificationPane);
        translateInTransition.setFromX(300);
        translateInTransition.setToX(0);

        // สร้าง FadeTransition สำหรับลด opacity
        FadeTransition fadeOutTransition = new FadeTransition(Duration.seconds(1), notificationPane);
        fadeOutTransition.setFromValue(1.0); // เริ่มจากไม่โปร่งใส
        fadeOutTransition.setToValue(0.0); // จางหายไปจนโปร่งใส

        FadeTransition fadeInTransition = new FadeTransition(Duration.seconds(1), notificationPane);
        fadeInTransition.setFromValue(0.0);
        fadeInTransition.setToValue(1.0);

        // รวมทั้งสอง animation เข้าด้วยกัน
        ParallelTransition parallelOutTransition = new ParallelTransition(translateOutTransition, fadeOutTransition);
        ParallelTransition parallelInTransition = new ParallelTransition(translateInTransition, fadeInTransition);


        // ตั้งเวลาให้หายไปหลังจาก 3 วินาที
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(3), // ระยะเวลา 3 วินาที
                event -> {
                    // เริ่ม animation
                    parallelOutTransition.play();
                }
        ));

        parallelInTransition.play();

        parallelInTransition.setOnFinished(event -> {
            timeline.setCycleCount(1); // รันแค่ครั้งเดียว
            timeline.play(); // เริ่มนับเวลา
        });

        // ลบการแจ้งเตือนเมื่อ animation เสร็จ
        parallelOutTransition.setOnFinished(event -> getChildren().remove(notificationPane));
    }

    // สร้างหน้าตาของการแจ้งเตือนแต่ละอัน
    private Pane createNotificationPane(String title, String content) {
        VBox pane = new VBox(5); // ระยะห่างระหว่างหัวข้อและเนื้อหา
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-radius: 5;");

        // หัวข้อ
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.BLACK);

        // เนื้อหา
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("System", 12));
        contentLabel.setTextFill(Color.BLACK);

        pane.getChildren().addAll(titleLabel, contentLabel);
        return pane;
    }
}