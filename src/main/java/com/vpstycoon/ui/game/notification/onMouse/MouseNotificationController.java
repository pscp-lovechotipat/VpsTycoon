package com.vpstycoon.ui.game.notification.onMouse;

import java.awt.*;

public class MouseNotificationController {
    private final MouseNotificationView view;

    public MouseNotificationController(MouseNotificationView view) {
        this.view = view;
    }

    public void addNotification(String content) {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        double mouseX = mousePoint.getX();
        double mouseY = mousePoint.getY();

        content = "+ " + content;

        view.addNotificationPane(content, mouseX, mouseY);
    }
}
