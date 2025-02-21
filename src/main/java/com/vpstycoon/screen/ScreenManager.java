package com.vpstycoon.screen;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface ScreenManager {
    void setResolution(ScreenResolution resolution);
    void setFullscreen(boolean fullscreen);
    void applySettings(Stage stage, Scene scene);
    void switchScreen(Node screen);
} 