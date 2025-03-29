package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.ResumeScreen;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.scene.input.KeyCode;
import com.vpstycoon.game.resource.ResourceManager;

public class KeyEventHandler {
    private final GameplayContentPane contentPane;
    private final DebugOverlayManager debugOverlayManager;
    private boolean resumeScreenShowing = false;
    private boolean settingsScreenShowing = false;
    private ResumeScreen resumeScreen;
    private SettingsScreen settingsScreen;

    public KeyEventHandler(GameplayContentPane contentPane, DebugOverlayManager debugOverlayManager) {
        this.contentPane = contentPane;
        this.debugOverlayManager = debugOverlayManager;
        
        // Create the resume screen with both callbacks
        this.resumeScreen = new ResumeScreen(
            contentPane.getNavigator(), 
            this::hideResumeScreen,
            this::showSettingsScreen
        );
        
        // Get config from DefaultGameConfig
        GameConfig config = DefaultGameConfig.getInstance();
        
        // Use the alternative constructor that doesn't require ScreenManager
        this.settingsScreen = new SettingsScreen(
            config,
            contentPane.getNavigator(),
            this::hideSettingsScreen
        );
    }

    public void setup() {
        contentPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (!resumeScreenShowing && !settingsScreenShowing) {
                    System.out.println("ESC กดแล้ว: กำลังแสดง Pause Menu และหยุดเกม");
                    showResumeScreen();
                    resumeScreenShowing = true;
                } else if (resumeScreenShowing) {
                    // กด ESC ขณะที่ Pause Menu กำลังแสดงอยู่ ให้กลับไปเล่นต่อ
                    System.out.println("ESC กดซ้ำขณะที่ Pause Menu กำลังแสดงอยู่: กลับไปเล่นต่อ");
                    hideResumeScreen();
                }
            } else if (event.getCode() == KeyCode.F3) {
                toggleDebug();
            }
        });
    }

    /**
     * หยุดการทำงานของทุก thread ในเกม
     */
    private void pauseAllGameThreads(String caller) {
        // หยุด GameTimeController
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController (จาก " + caller + ")");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }
        
        // หยุด GameEvent
        if (ResourceManager.getInstance().getGameEvent() != null &&
            ResourceManager.getInstance().getGameEvent().isRunning()) {
            System.out.println("หยุด GameEvent (จาก " + caller + ")");
            ResourceManager.getInstance().getGameEvent().pauseEvent();
        }
        
        // หยุด RequestGenerator
        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("หยุด RequestGenerator (จาก " + caller + ")");
            ResourceManager.getInstance().getRequestGenerator().pauseGenerator();
        }
    }
    
    /**
     * เริ่มการทำงานของทุก thread ในเกม
     */
    private void resumeAllGameThreads(String caller) {
        // เริ่ม GameTimeController
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("เริ่ม GameTimeController (จาก " + caller + ")");
            ResourceManager.getInstance().getGameTimeController().startTime();
        }
        
        // เริ่ม GameEvent
        if (ResourceManager.getInstance().getGameEvent() != null) {
            System.out.println("เริ่ม GameEvent (จาก " + caller + ")");
            ResourceManager.getInstance().getGameEvent().resumeEvent();
        }
        
        // เริ่ม RequestGenerator
        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("เริ่ม RequestGenerator (จาก " + caller + ")");
            ResourceManager.getInstance().getRequestGenerator().resumeGenerator();
        }
    }
    
    // Methods for ResumeScreen
    private void showResumeScreen() {
        // หยุดเวลาและทุก thread เมื่อแสดง Pause Menu
        pauseAllGameThreads("showResumeScreen");
        
        resumeScreen.setPrefSize(contentPane.getWidth(), contentPane.getHeight());
        
        // Add to rootStack instead of gameArea to ensure it appears on top of all other UI elements
        contentPane.getRootStack().getChildren().add(resumeScreen);
        
        // Ensure it's at the top of the Z-order
        resumeScreen.toFront();
    }

    private void hideResumeScreen() {
        // เริ่มเวลาและทุก thread เมื่อซ่อน Pause Menu
        resumeAllGameThreads("hideResumeScreen");
        
        contentPane.getRootStack().getChildren().remove(resumeScreen);
        resumeScreenShowing = false;
    }
    
    // Methods for SettingsScreen
    public void showSettingsScreen() {
        // ทำให้แน่ใจว่าเกมยังคงหยุดอยู่เมื่อเปิดหน้าตั้งค่า
        pauseAllGameThreads("showSettingsScreen");
        
        settingsScreen.setPrefSize(contentPane.getWidth(), contentPane.getHeight());
        
        // Add to rootStack instead of gameArea to ensure it appears on top of all other UI elements
        contentPane.getRootStack().getChildren().add(settingsScreen);
        
        // Ensure it's at the top of the Z-order
        settingsScreen.toFront();
        
        settingsScreenShowing = true;
        
        // Hide resume screen if it's showing
        if (resumeScreenShowing) {
            // ไม่ต้องเรียก hideResumeScreen() ทั้งหมดเพราะจะทำให้เริ่ม thread กลับมาทำงาน
            // แทนที่จะเรียกเมธอดทั้งหมด เราเพียงแค่เอา resumeScreen ออกจาก UI
            contentPane.getRootStack().getChildren().remove(resumeScreen);
            resumeScreenShowing = false;
        }
    }

    private void hideSettingsScreen() {
        // เริ่มเวลาและทุก thread เมื่อกลับจากหน้าตั้งค่า
        resumeAllGameThreads("hideSettingsScreen");
        
        contentPane.getRootStack().getChildren().remove(settingsScreen);
        settingsScreenShowing = false;
    }

    private void toggleDebug() {
        boolean newShowDebug = !contentPane.isShowDebug();
        contentPane.setShowDebug(newShowDebug);
        debugOverlayManager.toggleDebug();
    }

    public void setResumeScreenShowing(boolean showing) {
        this.resumeScreenShowing = showing;
    }
    
    public void setSettingsScreenShowing(boolean showing) {
        this.settingsScreenShowing = showing;
    }
    
    public boolean isSettingsScreenShowing() {
        return settingsScreenShowing;
    }
}