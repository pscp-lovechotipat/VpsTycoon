package com.vpstycoon.ui.game.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationModel {
    private List<Notification> notifications = new ArrayList<>();

    
    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    
    public List<Notification> getNotifications() {
        return notifications;
    }

    
    public static class Notification {
        private String title;
        private String content;

        
        public Notification(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
