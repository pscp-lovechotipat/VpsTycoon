package com.vpstycoon.utils;

import com.vpstycoon.ScreenResolution;
import javafx.stage.Stage;

public class StageUtils {
    public static void setFixedSize(Stage stage, ScreenResolution resolution) {
        stage.setWidth(resolution.getWidth());
        stage.setHeight(resolution.getHeight());
        stage.setResizable(false);
    }
} 