package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
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
                    showResumeScreen();
                    resumeScreenShowing = true;
                }
            } else if (event.getCode() == KeyCode.F3) {
                toggleDebug();
            }
        });
    }

    // Methods for ResumeScreen
    private void showResumeScreen() {
        resumeScreen.setPrefSize(contentPane.getWidth(), contentPane.getHeight());
        
        // Add to rootStack instead of gameArea to ensure it appears on top of all other UI elements
        contentPane.getRootStack().getChildren().add(resumeScreen);
        
        // Ensure it's at the top of the Z-order
        resumeScreen.toFront();
    }

    private void hideResumeScreen() {
        contentPane.getRootStack().getChildren().remove(resumeScreen);
        resumeScreenShowing = false;
    }
    
    // Methods for SettingsScreen
    public void showSettingsScreen() {
        settingsScreen.setPrefSize(contentPane.getWidth(), contentPane.getHeight());
        
        // Add to rootStack instead of gameArea to ensure it appears on top of all other UI elements
        contentPane.getRootStack().getChildren().add(settingsScreen);
        
        // Ensure it's at the top of the Z-order
        settingsScreen.toFront();
        
        settingsScreenShowing = true;
        
        // Hide resume screen if it's showing
        if (resumeScreenShowing) {
            hideResumeScreen();
        }
    }

    private void hideSettingsScreen() {
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