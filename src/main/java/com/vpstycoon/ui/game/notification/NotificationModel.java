package com.vpstycoon.ui.game.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationModel {
    private List<Notification> notifications = new ArrayList<>();

    // เพิ่มการแจ้งเตือนเข้าไปใน list
    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    // ดึงรายการการแจ้งเตือนทั้งหมด
    public List<Notification> getNotifications() {
        return notifications;
    }

    // คลาสย่อยสำหรับเก็บข้อมูลการแจ้งเตือน
    public static class Notification {
        private String title;
        private String content;

        // Constructor ที่กำหนดหัวข้อและเนื้อหา
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