package com.vpstycoon.screen;

import com.vpstycoon.view.base.GameScreen;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface ScreenManager {
    void setResolution(ScreenResolution resolution);
    void setFullscreen(boolean fullscreen);
    void applySettings(Stage stage, Scene scene);
    void switchScreen(Node screen);
    void switchScreen(GameScreen screen);
    void prepareScreen(Node screen);
    void prepareScreen(GameScreen screen);
    void updateScreenResolution();
}

