package com.vpstycoon.ui.game.notification.onMouse;

import com.vpstycoon.audio.AudioManager;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MouseNotificationView extends Pane {
    private AudioManager audioManager;
    public MouseNotificationView() {
        setMouseTransparent(true);
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public void addNotificationPane(String content, double mouseX, double mouseY) {
        // สร้าง notification pane
        Pane notificationPane = createNotificationPane(content);
        notificationPane.setPrefWidth(200); // กำหนดความกว้างคงที่ เช่น 200 พิกเซล
        double sceneX = mouseX - getScene().getWindow().getX();
        double sceneY = mouseY - getScene().getWindow().getY();

        // กำหนดตำแหน่งเริ่มต้น โดยให้อยู่กึ่งกลางแนวนอนของเมาส์ และเลื่อนลงด้านล่าง
        notificationPane.setLayoutX(sceneX - 50);
        notificationPane.setLayoutY(sceneY - 100);

        // เพิ่ม notification pane เข้าไปใน view
        getChildren().add(notificationPane);

        // แอนิเมชันเลื่อนขึ้นมา
        TranslateTransition appear = new TranslateTransition(Duration.seconds(0.5), notificationPane);
        appear.setFromY(50); // เริ่มจากตำแหน่งที่เลื่อนลง 50 พิกเซล
        appear.setToY(0);    // เลื่อนขึ้นมาถึงตำแหน่งจริง
        appear.setOnFinished(event -> {
            // รอ 3 วินาทีหลังจากเลื่อนขึ้นมา
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                // แอนิเมชันเลื่อนขึ้นและหายไป
                TranslateTransition disappear = new TranslateTransition(Duration.seconds(0.5), notificationPane);
                disappear.setFromY(0);   // เริ่มจากตำแหน่งปัจจุบัน
                disappear.setToY(-50);   // เลื่อนขึ้นไป 50 พิกเซล
                disappear.setOnFinished(ev -> getChildren().remove(notificationPane)); // ลบออกจาก view
                disappear.play();
            });
            pause.play();
        });
        appear.play();
    }

    // สร้าง pane สำหรับ notification โดยใช้ VBox
    private Pane createNotificationPane(String content) {
        VBox pane = new VBox();

        pane.setStyle("-fx-padding: 10;");

        Label label = new Label(content);
        label.setTextFill(Color.web("#FFD700"));

        pane.getChildren().add(label);


        return pane;
    }
}
