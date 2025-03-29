package com.vpstycoon.screen;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.view.base.GameScreen;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JavaFXScreenManager implements ScreenManager {
    private final GameConfig config;
    private final Stage stage;
    
    public JavaFXScreenManager(GameConfig config, Stage stage) {
        this.config = config;
        this.stage = stage;
    }
    
    @Override
    public void applySettings(Stage stage, Scene scene) {
        ScreenResolution resolution = config.getResolution();
        
        if (config.isFullscreen()) {
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            stage.setFullScreen(true);
        } else {
            stage.setFullScreen(false);
            
            // Set the exact stage size with no decorations
            stage.setWidth(resolution.getWidth());
            stage.setHeight(resolution.getHeight());
            
            // Remove any internal padding
            stage.sizeToScene();
            
            // Center on screen
            stage.centerOnScreen();
        }

        // Force scene size to match stage
        if (scene != null) {
            // Ensure scene fill is black to avoid white borders
            scene.setFill(javafx.scene.paint.Color.BLACK);
            
            // Preserve the current root
            javafx.scene.Parent currentRoot = scene.getRoot();
            
            // Force layout pass and scene recreation for resolution updates
            if (currentRoot instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region rootRegion = (javafx.scene.layout.Region) currentRoot;
                
                // Remove any margin or padding
                rootRegion.setPadding(javafx.geometry.Insets.EMPTY);
                rootRegion.setStyle("-fx-background-color: black; -fx-padding: 0; -fx-margin: 0;");
                
                // Update region dimensions to match new resolution exactly
                rootRegion.setPrefWidth(resolution.getWidth());
                rootRegion.setPrefHeight(resolution.getHeight());
                rootRegion.setMinWidth(resolution.getWidth());
                rootRegion.setMinHeight(resolution.getHeight());
                rootRegion.setMaxWidth(resolution.getWidth());
                rootRegion.setMaxHeight(resolution.getHeight());
                
                // Force layout recalculation
                rootRegion.requestLayout();
                rootRegion.layout();
            }
            
            // Create a temporary container to force JavaFX to recreate the scene graph
            javafx.scene.layout.StackPane tempContainer = new javafx.scene.layout.StackPane();
            tempContainer.setStyle("-fx-background-color: black;");
            scene.setRoot(tempContainer);
            
            // Set the original root back to trigger a complete re-render
            scene.setRoot(currentRoot);
        }
    }
    
    @Override
    public void setResolution(ScreenResolution resolution) {
        config.setResolution(resolution);
    }
    
    @Override
    public void setFullscreen(boolean fullscreen) {
        config.setFullscreen(fullscreen);
        if (fullscreen) {
            config.setResolution(ScreenResolution.getMaxSupportedResolution());
        }
    }
    
    @Override
    public void switchScreen(Node screen) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(new StackPane(screen));
            stage.setScene(scene);
        } else {
            StackPane root = (StackPane) scene.getRoot();
            
            if (!root.getChildren().isEmpty()) {
                Node currentScreen = root.getChildren().get(0);
                
                javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), currentScreen);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                screen.setOpacity(0.0);
                
                root.getChildren().add(screen);
                
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), screen);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                
                fadeOut.setOnFinished(event -> {
                    root.getChildren().remove(currentScreen);
                    
                    fadeIn.play();
                });
                
                fadeOut.play();
            } else {
                root.getChildren().add(screen);
                
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), screen);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
            
            root.requestLayout();
        }
    }
    
    @Override
    public void switchScreen(GameScreen screen) {
        // เรียกใช้ method switchScreen(Node) ที่มีอยู่แล้ว โดยส่ง root element ของ GameScreen
        switchScreen(screen.getRoot());
        
        // เรียก onShow เพื่อให้ GameScreen รู้ว่าถูกแสดงแล้ว
        screen.onShow();
    }
    
    @Override
    public void updateScreenResolution() {
        // อัปเดตความละเอียดหน้าจอโดยใช้การตั้งค่าปัจจุบัน
        applySettings(stage, stage.getScene());
        
        // ตรวจสอบว่ามีหน้าจอปัจจุบันหรือไม่
        if (stage.getScene() != null && stage.getScene().getRoot() != null) {
            StackPane root = (StackPane) stage.getScene().getRoot();
            if (!root.getChildren().isEmpty()) {
                // บังคับให้มีการ redraw
                root.requestLayout();
            }
        }
    }
}