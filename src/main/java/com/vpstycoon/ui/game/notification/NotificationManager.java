package com.vpstycoon.ui.game.notification;

import com.vpstycoon.audio.AudioManager;
import javafx.application.Platform;

public class NotificationManager {
    private final NotificationView notificationView;
    private final CenterNotificationView centerNotificationView;
    private final CenterNotificationController centerNotificationController;
    private final MouseNotificationView mouseNotificationView;
    private final MouseNotificationController mouseNotificationController;

    public NotificationManager(AudioManager audioManager) {
        notificationView = new NotificationView();
        notificationView.setAudioManager(audioManager);

        centerNotificationView = new CenterNotificationView();
        centerNotificationView.setAudioManager(audioManager);
        centerNotificationController = new CenterNotificationController(new CenterNotificationModel(), centerNotificationView);

        mouseNotificationView = new MouseNotificationView();
        mouseNotificationController = new MouseNotificationController(mouseNotificationView);
    }

    public void push(String title, String content) {
        Platform.runLater(() -> {
            try {
                notificationView.addNotificationPane(title, content);
            } catch (Exception e) {
                System.err.println("Error showing notification: " + e.getMessage());
            }
        });
    }

    public void pushMouse(String content) {
        mouseNotificationController.addNotification(content);
    }

    public void pushCenter(String title, String content, String image) {
        centerNotificationController.push(title, content, image);
    }

    public void pushCenterAutoClose(String title, String content, String image, long autoCloseMillis) {
        centerNotificationController.pushAutoClose(title, content, image, autoCloseMillis);
    }

    public NotificationView getNotificationView() {
        return notificationView;
    }

    public CenterNotificationView getCenterNotificationView() {
        return centerNotificationView;
    }

    public MouseNotificationView getMouseNotificationView() {
        return mouseNotificationView;
    }
}
