package com.vpstycoon.ui.game.notification;

public class NotificationController {
    private final NotificationModel model;
    private NotificationView view;

    public NotificationController(NotificationModel model, NotificationView view) {
        this.model = model;
        this.view = view;
    }

    
    public void push(String title, String content) {
        NotificationModel.Notification notification = new NotificationModel.Notification(title, content);
        model.addNotification(notification);
        view.addNotificationPane(title, content);
    }

    public NotificationView getView() {
        return view;
    }

    public void setView(NotificationView notificationView) {
        this.view = notificationView;
    }
}

