package com.vpstycoon.ui.game.notification.onMouse;

import java.awt.*;

public class MouseNotificationController {
    private final MouseNotificationModel model;
    private final MouseNotificationView view;

    public MouseNotificationController(MouseNotificationModel model, MouseNotificationView view) {
        this.model = model;
        this.view = view;
    }

    // เพิ่ม notification ใหม่
    public void addNotification(String content) {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        double mouseX = mousePoint.getX();
        double mouseY = mousePoint.getY();

        content = "+ " + content;

        MouseNotificationModel.Notification notification = new MouseNotificationModel.Notification(content);
        model.addNotification(notification); // เพิ่มเข้า model
        view.addNotificationPane(content, mouseX, mouseY); // แสดงใน view
    }
}
