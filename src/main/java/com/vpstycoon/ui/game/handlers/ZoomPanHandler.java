package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.ui.debug.DebugOverlayManager;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;

/**
 * Handles zoom and pan interactions for the game world.
 */
public class ZoomPanHandler {
    private final Group worldGroup;
    private final StackPane gameArea;
    private final DebugOverlayManager debugOverlayManager;
    private boolean showDebug;
    
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private boolean isPanning = false;

    public ZoomPanHandler(Group worldGroup, StackPane gameArea, 
                          DebugOverlayManager debugOverlayManager, boolean showDebug) {
        this.worldGroup = worldGroup;
        this.gameArea = gameArea;
        this.debugOverlayManager = debugOverlayManager;
        this.showDebug = showDebug;
    }

    public void setup() {
        setupZoomHandlers();
        setupPanHandlers();
        setupResetHandler();
    }
    
    private void setupZoomHandlers() {
        // Mouse wheel zoom
        gameArea.setOnScroll(e -> {
            double zoomFactor = 1.05;
            if (e.getDeltaY() < 0) {
                zoomFactor = 1.0 / zoomFactor;
            }
            
            // Calculate new scale
            double newScale = worldGroup.getScaleX() * zoomFactor;
            
            // Set min/max scale limits
            double minScale = 0.5;
            double maxScale = 2.0;
            
            newScale = Math.max(minScale, Math.min(newScale, maxScale));
            
            worldGroup.setScaleX(newScale);
            worldGroup.setScaleY(newScale);
            
            updateDebugInfoIfNeeded();
            e.consume();
        });
        
        // Touchpad pinch zoom
        gameArea.setOnZoom(e -> {
            double zoomFactor = e.getZoomFactor();
            
            // Calculate new scale using zoom factor from gesture
            double newScale = worldGroup.getScaleX() * zoomFactor;
            
            // Set min/max scale limits
            double minScale = 0.5;
            double maxScale = 2.0;
            
            newScale = Math.max(minScale, Math.min(newScale, maxScale));
            
            worldGroup.setScaleX(newScale);
            worldGroup.setScaleY(newScale);
            
            updateDebugInfoIfNeeded();
            e.consume();
        });
    }
    
    private void setupPanHandlers() {
        // Mouse pressed handler
        worldGroup.setOnMousePressed(e -> {
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
            translateAnchorX = worldGroup.getTranslateX();
            translateAnchorY = worldGroup.getTranslateY();
            isPanning = true;
            
            gameArea.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            e.consume();
        });
        
        // Mouse drag handler
        worldGroup.setOnMouseDragged(e -> {
            if (isPanning) {
                double deltaX = e.getSceneX() - mouseAnchorX;
                double deltaY = e.getSceneY() - mouseAnchorY;

                // คำนวณค่าใหม่
                double newTranslateX = translateAnchorX + deltaX;
                double newTranslateY = translateAnchorY + deltaY;

                // ดึงขนาดของ worldGroup และ gameArea
                double worldWidth = worldGroup.getBoundsInLocal().getWidth();
                double worldHeight = worldGroup.getBoundsInLocal().getHeight();
                double viewWidth = gameArea.getWidth();
                double viewHeight = gameArea.getHeight();

                // คำนวณขอบเขต
                int delta = 500;

                double minX = Math.min(-delta, viewWidth - worldWidth); // ค่าต่ำสุดของ translateX
                double maxX = delta; // ค่าสูงสุดของ translateX
                double minY = Math.min(-delta, viewHeight - worldHeight); // ค่าต่ำสุดของ translateY
                double maxY = delta; // ค่าสูงสุดของ translateY

                // จำกัดค่า translateX และ translateY
                newTranslateX = Math.max(minX, Math.min(maxX, newTranslateX));
                newTranslateY = Math.max(minY, Math.min(maxY, newTranslateY));

                // ตั้งค่าที่จำกัดแล้ว
                worldGroup.setTranslateX(newTranslateX);
                worldGroup.setTranslateY(newTranslateY);

                updateDebugInfoIfNeeded();
                e.consume();
            }
        });
        
        // Mouse released handler
        worldGroup.setOnMouseReleased(e -> {
            if (isPanning) {
                isPanning = false;
                gameArea.setCursor(javafx.scene.Cursor.DEFAULT);
                e.consume();
            }
        });
        
        // Mouse cursor handlers
        worldGroup.setOnMouseEntered(e -> {
            gameArea.setCursor(javafx.scene.Cursor.HAND);
        });
        
        worldGroup.setOnMouseExited(e -> {
            if (!isPanning) {
                gameArea.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
    }
    
    private void setupResetHandler() {
        // Reset zoom and position with spacebar
        gameArea.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                worldGroup.setScaleX(1.0);
                worldGroup.setScaleY(1.0);
                worldGroup.setTranslateX(0);
                worldGroup.setTranslateY(0);
                e.consume();
            }
        });
        
        // Enable key events
        gameArea.setFocusTraversable(true);
    }
    
    private void updateDebugInfoIfNeeded() {
        if (showDebug) {
            debugOverlayManager.updateGameInfo(gameArea);
        }
    }
    
    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }
} 