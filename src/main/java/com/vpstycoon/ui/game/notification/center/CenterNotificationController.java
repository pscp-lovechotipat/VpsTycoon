package com.vpstycoon.ui.game.notification.center;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class CenterNotificationController {
    private final CenterNotificationModel model;
    private CenterNotificationView view;

    public CenterNotificationController(CenterNotificationModel model, CenterNotificationView view) {
        this.model = model;
        this.view = view;
        this.view.setModel(model); // เชื่อม model กับ view
    }

    public void push(String title, String content) {
        Platform.runLater(() -> view.addNotificationPane(title, content));
    }

    public void push(String title, String content, String image) {
        Platform.runLater(() -> view.addNotificationPane(title, content, image));
    }
    
    /**
     * แสดง notification ที่จะหายไปเองอัตโนมัติหลังจากเวลาที่กำหนด
     * 
     * @param title หัวข้อ notification
     * @param content เนื้อหา notification
     * @param image รูปภาพประกอบ
     * @param autoCloseMillis เวลาในหน่วย millisecond ที่จะปิด notification อัตโนมัติ
     */
    public void pushAutoClose(String title, String content, String image, long autoCloseMillis) {
        Platform.runLater(() -> view.addNotificationPaneAutoClose(title, content, image, autoCloseMillis));
    }

    public void setView(CenterNotificationView centerNotificationView) {
        this.view = centerNotificationView;
    }
}