package com.vpstycoon.ui.debug;

import com.vpstycoon.game.GameState;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DebugOverlayManager extends GameState {
    private final VBox debugOverlay;
    private final Label fpsLabel;
    private final Label mouseLabel;
    private final Label moneyLabel;
    private final Label zoomLabel;

    private boolean showDebug;
    private long lastTime;
    private int frameCount;
    private final AnimationTimer debugTimer;

    public DebugOverlayManager() {
        
        debugOverlay = new VBox(5);
        debugOverlay.setAlignment(Pos.TOP_LEFT);
        debugOverlay.setPadding(new Insets(30));
        debugOverlay.setMouseTransparent(true);
        debugOverlay.setVisible(false);

        
        fpsLabel = new Label("FPS: 0");
        mouseLabel = new Label("Mouse: 0, 0");
        moneyLabel = new Label("Money: 0");
        zoomLabel = new Label("Zoom: 1.05x");

        String labelStyle = """
            -fx-font-family: monospace;
            -fx-font-size: 14px;
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, black, 1, 1, 0, 0);
            """;
        fpsLabel.setStyle(labelStyle);
        mouseLabel.setStyle(labelStyle);
        moneyLabel.setStyle(labelStyle);
        zoomLabel.setStyle(labelStyle);

        debugOverlay.getChildren().addAll(fpsLabel, mouseLabel, moneyLabel, zoomLabel);

        
        this.showDebug = false;
        this.lastTime = System.nanoTime();
        this.frameCount = 0;

        
        debugTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateFPS(now);
            }
        };
    }

    
    public void startTimer() {
        debugTimer.start();
    }

    
    public void stopTimer() {
        debugTimer.stop();
    }

    
    private void updateFPS(long now) {
        if (!showDebug) return;
        frameCount++;
        if (now - lastTime >= 1_000_000_000) {
            fpsLabel.setText(String.format("FPS: %d", frameCount));
            frameCount = 0;
            lastTime = now;
        }
    }

    
    public void updateGameInfo( StackPane gameArea) {
        if (!showDebug) return;

        moneyLabel.setText(String.format("Money: %d", super.getCompany().getMoney()));

        
        if (!gameArea.getChildren().isEmpty() && gameArea.getChildren().getFirst() instanceof Group worldGroup) {
            zoomLabel.setText(String.format("Zoom: %.2fx", worldGroup.getScaleY()));
        }
    }

    
    public void updateMousePosition(double x, double y) {
        if (showDebug) {
            mouseLabel.setText(String.format("Mouse: %.0f, %.0f", x, y));
        }
    }

    
    public void toggleDebug() {
        showDebug = !showDebug;
        debugOverlay.setVisible(showDebug);
    }

    public VBox getDebugOverlay() {
        return debugOverlay;
    }
}


