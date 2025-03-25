package com.vpstycoon.ui.game.notification.center;

import com.vpstycoon.game.resource.ResourceManager;
import javafx.scene.image.Image;

import javax.swing.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.Objects;
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
        private String image;

        public Notification(String title, String content) {
            this(title, content, null);
        }

        public Notification(String title, String content, String image) {
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
            String imagePath = image;
            ImageIcon imageUrl = new ImageIcon(Objects.requireNonNull(getClass().getResource(ResourceManager.getImagePath("/notification/default.png"))));
            return (imageUrl != null) ? new Image(String.valueOf(imageUrl)) : null;
        }
    }
}