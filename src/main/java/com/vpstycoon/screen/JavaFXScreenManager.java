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
            ((StackPane) scene.getRoot()).getChildren().setAll(screen);
        }
    }
    
    // ... implement other methods ...
} 