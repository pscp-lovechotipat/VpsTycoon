package com.vpstycoon.screen;

import javafx.scene.Scene;
import javafx.stage.Stage;
import com.vpstycoon.screen.ScreenResolution;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public interface ScreenManager {
    void setResolution(ScreenResolution resolution);
    void setFullscreen(boolean fullscreen);
    void applySettings(Stage stage, Scene scene);
    void switchScreen(Node screen);
} 