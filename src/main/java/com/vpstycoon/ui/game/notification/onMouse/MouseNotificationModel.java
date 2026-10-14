package com.vpstycoon.ui.game.notification.onMouse;

import java.util.ArrayList;
import java.util.List;

public class MouseNotificationModel {
    private final List<Notification> notifications = new ArrayList<>();

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }


    public static class Notification {

        public Notification(String content) {
        }

    }
}


