package com.vpstycoon.screen;

import com.vpstycoon.config.GameConfig;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JavaFXScreenManager implements ScreenManager {
    private final GameConfig config;
    private final Stage stage;
    
    public JavaFXScreenManager(GameConfig config, Stage stage) {
        this.config = config;
        this.stage = stage;
    }
    
    @Override
    public void applySettings(Stage stage, Scene scene) {
        if (config.isFullscreen()) {
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            stage.setFullScreen(true);
        } else {
            stage.setFullScreen(false);
            ScreenResolution resolution = config.getResolution();
            stage.setWidth(resolution.getWidth());
            stage.setHeight(resolution.getHeight());
            stage.centerOnScreen();
        }

        // Force scene size to match stage
        if (scene != null) {
            scene.setRoot(scene.getRoot()); // Trigger layout update
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

}