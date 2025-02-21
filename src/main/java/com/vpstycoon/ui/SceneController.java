package com.vpstycoon.ui;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SceneController {
    private static SceneController instance;
    private final Stage stage;
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final StackPane rootContainer;
    private Scene mainScene;

    private SceneController(Stage stage, GameConfig config, ScreenManager screenManager) {
        this.stage = stage;
        this.config = config;
        this.screenManager = screenManager;
        this.rootContainer = new StackPane();
        
        initializeScene();
    }

    public static SceneController getInstance() {
        return instance;
    }

    public static void initialize(Stage stage, GameConfig config, ScreenManager screenManager) {
        if (instance == null) {
            instance = new SceneController(stage, config, screenManager);
        }
    }

    private void initializeScene() {
        mainScene = new Scene(rootContainer);
        stage.setScene(mainScene);
        
        // Apply initial resolution
        updateResolution();
    }

    public void setContent(Parent content) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(content);
    }

    public void updateResolution() {
        ScreenResolution resolution = config.getResolution();
        
        // Set container size
        rootContainer.setPrefWidth(resolution.getWidth());
        rootContainer.setPrefHeight(resolution.getHeight());
        rootContainer.setMinWidth(resolution.getWidth());
        rootContainer.setMinHeight(resolution.getHeight());
        rootContainer.setMaxWidth(resolution.getWidth());
        rootContainer.setMaxHeight(resolution.getHeight());
        
        // Apply settings to stage
        screenManager.applySettings(stage, mainScene);
    }
} 