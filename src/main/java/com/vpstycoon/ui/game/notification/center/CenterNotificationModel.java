package com.vpstycoon.ui.game.notification.center;

import javafx.scene.image.Image;
import java.util.LinkedList;
import java.util.Queue;

public class CenterNotificationModel {
    private Queue<Notification> notificationQueue = new LinkedList<>();

    public void addNotification(Notification notification) {
        notificationQueue.offer(notification);
    }

    public Notification getNextNotification() {
        return notificationQueue.poll();
    }

    public boolean hasNotifications() {
        return !notificationQueue.isEmpty();
    }

    public static class Notification {
        private String title;
        private String content;
        private Image image;

        public Notification(String title, String content) {
            this(title, content, null);
        }

        public Notification(String title, String content, Image image) {
            this.title = title;
            this.content = content;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public Image getImage() {
            return image;
        }
    }
}