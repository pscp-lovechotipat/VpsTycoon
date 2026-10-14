package com.vpstycoon.screen;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface ScreenManager {
    void applySettings(Stage stage, Scene scene);
    void switchScreen(Node screen);
    void prepareScreen(Node screen);
}
