package com.vpstycoon.ui.game.notification;

public class NotificationController {
    private final NotificationModel model;
    private final NotificationView view;

    public NotificationController(NotificationModel model, NotificationView view) {
        this.model = model;
        this.view = view;
    }

    // Method push สำหรับเพิ่มการแจ้งเตือน
    public void push(String title, String content) {
        NotificationModel.Notification notification = new NotificationModel.Notification(title, content);
        model.addNotification(notification);
        view.addNotificationPane(title, content);
    }
}