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
        // Set the background color of the root container to black
        rootContainer.setStyle("-fx-background-color: black;");
        
        // Create scene with black background to avoid white borders
        mainScene = new Scene(rootContainer);
        mainScene.setFill(javafx.scene.paint.Color.BLACK);
        
        // Ensure the stage has the exact size as the scene
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
        
        // Ensure scene size matches stage exactly
        mainScene.setFill(javafx.scene.paint.Color.BLACK);
        
        // Force a redraw of the content
        rootContainer.requestLayout();
        
        // If we have any content, ensure it's properly sized for the new resolution
        if (!rootContainer.getChildren().isEmpty()) {
            for (javafx.scene.Node child : rootContainer.getChildren()) {
                if (child instanceof javafx.scene.layout.Region) {
                    javafx.scene.layout.Region region = (javafx.scene.layout.Region) child;
                    region.setPrefWidth(resolution.getWidth());
                    region.setPrefHeight(resolution.getHeight());
                    region.setMinWidth(resolution.getWidth());
                    region.setMinHeight(resolution.getHeight());
                    region.setMaxWidth(resolution.getWidth());
                    region.setMaxHeight(resolution.getHeight());
                    region.requestLayout();
                }
            }
        }
    }
} 