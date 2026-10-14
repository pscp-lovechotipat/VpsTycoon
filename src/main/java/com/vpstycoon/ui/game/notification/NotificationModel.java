package com.vpstycoon.ui.game.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationModel {
    private List<Notification> notifications = new ArrayList<>();

    
    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    

    
    public static class Notification {

        
        public Notification(String title, String content) {
        }


    }
}

