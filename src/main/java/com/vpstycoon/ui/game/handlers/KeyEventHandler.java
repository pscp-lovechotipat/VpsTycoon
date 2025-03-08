package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.ResumeScreen;
import javafx.scene.input.KeyCode;

public class KeyEventHandler {
    private final GameplayContentPane contentPane;
    private final DebugOverlayManager debugOverlayManager;
    private boolean resumeScreenShowing = false;
    private ResumeScreen resumeScreen; // เพิ่มตัวแปรสำหรับ ResumeScreen

    public KeyEventHandler(GameplayContentPane contentPane, DebugOverlayManager debugOverlayManager) {
        this.contentPane = contentPane;
        this.debugOverlayManager = debugOverlayManager;
        this.resumeScreen = new ResumeScreen(contentPane.getNavigator(), this::hideResumeScreen); // สร้าง instance
    }

    public void setup() {
        contentPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (!resumeScreenShowing) {
                    showResumeScreen(); // เรียกเมธอดใหม่ที่เราสร้าง
                    resumeScreenShowing = true;
                }
            } else if (event.getCode() == KeyCode.F3) {
                toggleDebug();
            }
        });
    }

    // เมธอดสำหรับแสดง ResumeScreen
    private void showResumeScreen() {
        resumeScreen.setPrefSize(contentPane.getGameArea().getWidth(), contentPane.getGameArea().getHeight());
        contentPane.getGameArea().getChildren().add(resumeScreen); // เพิ่ม ResumeScreen เข้าไปใน gameArea
    }

    // เมธอดสำหรับซ่อน ResumeScreen
    private void hideResumeScreen() {
        contentPane.getGameArea().getChildren().remove(resumeScreen); // ลบ ResumeScreen ออกจาก gameArea
        resumeScreenShowing = false;
    }

    private void toggleDebug() {
        boolean newShowDebug = !contentPane.isShowDebug();
        contentPane.setShowDebug(newShowDebug);
        debugOverlayManager.toggleDebug();
    }

    // รักษาเมธอดนี้ไว้สำหรับรีเซ็ตสถานะจากภายนอก (ถ้าจำเป็น)
    public void setResumeScreenShowing(boolean showing) {
        this.resumeScreenShowing = showing;
    }
}