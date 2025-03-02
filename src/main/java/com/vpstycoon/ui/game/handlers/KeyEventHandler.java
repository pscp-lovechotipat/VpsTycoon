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

    public KeyEventHandler(GameplayContentPane contentPane, DebugOverlayManager debugOverlayManager) {
        this.contentPane = contentPane;
        this.debugOverlayManager = debugOverlayManager;
    }

    public void setup() {
        contentPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                contentPane.showResumeScreen();
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
} 