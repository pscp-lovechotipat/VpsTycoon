package com.vpstycoon.ui;

import com.vpstycoon.ScreenResolution;
import com.vpstycoon.utils.StageUtils;
import javafx.stage.Stage;

public class GameScreen {
    private Stage primaryStage;
    private ScreenResolution selectedResolution;

    public GameScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // init resolution
        this.selectedResolution = ScreenResolution.getAvailableResolutions().getFirst();
    }

    public void show() {
        primaryStage.setTitle("Game Screen");
        StageUtils.setFixedSize(primaryStage, selectedResolution);
    }
} 