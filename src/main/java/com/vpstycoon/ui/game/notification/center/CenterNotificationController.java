package com.vpstycoon.ui.game.notification.center;

import javafx.scene.image.Image;

public class CenterNotificationController {
    private final CenterNotificationModel model;
    private final CenterNotificationView view;

    public CenterNotificationController(CenterNotificationModel model, CenterNotificationView view) {
        this.model = model;
        this.view = view;
        this.view.setModel(model); // เชื่อม model กับ view
    }

    public void push(String title, String content) {
        view.addNotificationPane(title, content);
    }

    public void push(String title, String content, String image) {
        view.addNotificationPane(title, content, image);
    }
}