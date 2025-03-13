package com.vpstycoon.ui.game.notification.onMouse;

import com.vpstycoon.ui.game.notification.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class MouseNotificationModel {
    private final List<Notification> notifications = new ArrayList<>();

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public static class Notification {
        private final String content;

        public Notification(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
}
