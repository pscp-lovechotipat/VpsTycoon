package com.vpstycoon.ui.game.notification.onMouse;

import java.awt.*;

public class MouseNotificationController {
    private final MouseNotificationModel model;
    private MouseNotificationView view;

    public MouseNotificationController(MouseNotificationModel model, MouseNotificationView view) {
        this.model = model;
        this.view = view;
    }

    
    public void addNotification(String content) {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        double mouseX = mousePoint.getX();
        double mouseY = mousePoint.getY();

        content = "+ " + content;

        MouseNotificationModel.Notification notification = new MouseNotificationModel.Notification(content);
        model.addNotification(notification); 
        view.addNotificationPane(content, mouseX, mouseY); 
    }

    public void setView(MouseNotificationView mouseNotificationView) {
        this.view = mouseNotificationView;
    }
}

