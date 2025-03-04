package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.ui.debug.DebugOverlayManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.scene.input.KeyCode;

/**
 * Handles keyboard events for the game.
 */
public class KeyEventHandler {
    private final GameplayContentPane contentPane;
    private final DebugOverlayManager debugOverlayManager;
    private boolean resumeScreenShowing = false;

    public KeyEventHandler(GameplayContentPane contentPane, DebugOverlayManager debugOverlayManager) {
        this.contentPane = contentPane;
        this.debugOverlayManager = debugOverlayManager;
    }

    public void setup() {
        contentPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // Only show resume screen if it's not already showing
                if (!resumeScreenShowing) {
                    contentPane.showResumeScreen();
                    resumeScreenShowing = true;
                }
            } else if (event.getCode() == KeyCode.F3) {
                toggleDebug();
            }
        });
    }
    
    private void toggleDebug() {
        boolean newShowDebug = !contentPane.isShowDebug();
        contentPane.setShowDebug(newShowDebug);
        debugOverlayManager.toggleDebug();
    }
    
    // Method to reset the state when resume screen is hidden
    public void setResumeScreenShowing(boolean showing) {
        this.resumeScreenShowing = showing;
    }
} 