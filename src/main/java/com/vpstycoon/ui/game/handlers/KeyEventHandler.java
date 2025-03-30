package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.ResumeScreen;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.scene.input.KeyCode;

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
        

        this.resumeScreen = new ResumeScreen(
            contentPane.getNavigator(), 
            this::hideResumeScreen,
            this::showSettingsScreen
        );
        

        GameConfig config = DefaultGameConfig.getInstance();
        

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
                    System.out.println("ESC กดซ้ำขณะที่ Pause Menu กำลังแสดงอยู่: กลับไปเล่นต่อ");
                    hideResumeScreen();
                }
            } else if (event.getCode() == KeyCode.F3) {
                toggleDebug();
            }
        });
    }

    private void pauseAllGameThreads(String caller) {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController (จาก " + caller + ")");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        if (ResourceManager.getInstance().getGameEvent() != null &&
            ResourceManager.getInstance().getGameEvent().isRunning()) {
            System.out.println("หยุด GameEvent (จาก " + caller + ")");
            ResourceManager.getInstance().getGameEvent().pauseEvent();
        }

        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("หยุด RequestGenerator (จาก " + caller + ")");
            ResourceManager.getInstance().getRequestGenerator().pauseGenerator();
        }
    }

    private void resumeAllGameThreads(String caller) {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("เริ่ม GameTimeController (จาก " + caller + ")");
            ResourceManager.getInstance().getGameTimeController().startTime();
        }

        if (ResourceManager.getInstance().getGameEvent() != null) {
            System.out.println("เริ่ม GameEvent (จาก " + caller + ")");
            ResourceManager.getInstance().getGameEvent().resumeEvent();
        }

        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("เริ่ม RequestGenerator (จาก " + caller + ")");
            ResourceManager.getInstance().getRequestGenerator().resumeGenerator();
        }
    }

    private void showResumeScreen() {
        pauseAllGameThreads("showResumeScreen");

        resumeScreen.setPrefSize(contentPane.getWidth(), contentPane.getHeight());
        

        contentPane.getRootStack().getChildren().add(resumeScreen);
        

        resumeScreen.toFront();
    }

    private void hideResumeScreen() {
        resumeAllGameThreads("hideResumeScreen");

        contentPane.getRootStack().getChildren().remove(resumeScreen);
        resumeScreenShowing = false;
    }


    public void showSettingsScreen() {
        pauseAllGameThreads("showSettingsScreen");

        settingsScreen.setPrefSize(contentPane.getWidth(), contentPane.getHeight());

        contentPane.getRootStack().getChildren().add(settingsScreen);

        settingsScreen.toFront();
        
        settingsScreenShowing = true;

        if (resumeScreenShowing) {
            contentPane.getRootStack().getChildren().remove(resumeScreen);
            resumeScreenShowing = false;
        }
    }

    private void hideSettingsScreen() {
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
