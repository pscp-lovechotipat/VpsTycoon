package com.vpstycoon.ui.game.handlers;

import com.vpstycoon.ui.debug.DebugOverlayManager;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;


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
        
        gameArea.setOnScroll(e -> {
            double zoomFactor = 1.05;
            if (e.getDeltaY() < 0) {
                zoomFactor = 1.0 / zoomFactor;
            }
            
            
            double newScale = worldGroup.getScaleX() * zoomFactor;
            
            
            double minScale = 0.5;
            double maxScale = 2.0;
            
            newScale = Math.max(minScale, Math.min(newScale, maxScale));
            
            worldGroup.setScaleX(newScale);
            worldGroup.setScaleY(newScale);
            
            updateDebugInfoIfNeeded();
            e.consume();
        });
        
        
        gameArea.setOnZoom(e -> {
            double zoomFactor = e.getZoomFactor();
            
            
            double newScale = worldGroup.getScaleX() * zoomFactor;
            
            
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
        
        worldGroup.setOnMousePressed(e -> {
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
            translateAnchorX = worldGroup.getTranslateX();
            translateAnchorY = worldGroup.getTranslateY();
            isPanning = true;
            
            gameArea.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            e.consume();
        });
        
        
        worldGroup.setOnMouseDragged(e -> {
            if (isPanning) {
                double deltaX = e.getSceneX() - mouseAnchorX;
                double deltaY = e.getSceneY() - mouseAnchorY;

                
                double newTranslateX = translateAnchorX + deltaX;
                double newTranslateY = translateAnchorY + deltaY;

                
                double worldWidth = worldGroup.getBoundsInLocal().getWidth();
                double worldHeight = worldGroup.getBoundsInLocal().getHeight();
                double viewWidth = gameArea.getWidth();
                double viewHeight = gameArea.getHeight();

                
                int delta = 500;

                double minX = Math.min(-delta, viewWidth - worldWidth); 
                double maxX = delta; 
                double minY = Math.min(-delta, viewHeight - worldHeight); 
                double maxY = delta; 

                
                newTranslateX = Math.max(minX, Math.min(maxX, newTranslateX));
                newTranslateY = Math.max(minY, Math.min(maxY, newTranslateY));

                
                worldGroup.setTranslateX(newTranslateX);
                worldGroup.setTranslateY(newTranslateY);

                updateDebugInfoIfNeeded();
                e.consume();
            }
        });
        
        
        worldGroup.setOnMouseReleased(e -> {
            if (isPanning) {
                isPanning = false;
                gameArea.setCursor(javafx.scene.Cursor.DEFAULT);
                e.consume();
            }
        });
        
        
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
        
        gameArea.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                worldGroup.setScaleX(1.0);
                worldGroup.setScaleY(1.0);
                worldGroup.setTranslateX(0);
                worldGroup.setTranslateY(0);
                e.consume();
            }
        });
        
        
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
    
    
    public void cleanup() {
        
        gameArea.setOnScroll(null);
        gameArea.setOnZoom(null);
        gameArea.setOnKeyPressed(null);
        
        
        worldGroup.setOnMousePressed(null);
        worldGroup.setOnMouseDragged(null);
        worldGroup.setOnMouseReleased(null);
        worldGroup.setOnMouseEntered(null);
        worldGroup.setOnMouseExited(null);
        
        System.out.println("ZoomPanHandler event handlers removed");
    }
} 
