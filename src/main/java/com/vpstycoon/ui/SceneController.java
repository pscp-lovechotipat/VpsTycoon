package com.vpstycoon.ui;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
        
        rootContainer.setStyle("-fx-background-color: black;");
        
        
        mainScene = new Scene(rootContainer);
        mainScene.setFill(Color.BLACK);
        
        
        stage.setScene(mainScene);
        
        
        updateResolution();
    }

    public void setContent(Parent content) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(content);
    }

    public void updateResolution() {
        ScreenResolution resolution = config.getResolution();
        
        
        rootContainer.setPrefWidth(resolution.getWidth());
        rootContainer.setPrefHeight(resolution.getHeight());
        rootContainer.setMinWidth(resolution.getWidth());
        rootContainer.setMinHeight(resolution.getHeight());
        rootContainer.setMaxWidth(resolution.getWidth());
        rootContainer.setMaxHeight(resolution.getHeight());
        
        
        screenManager.applySettings(stage, mainScene);
        
        
        mainScene.setFill(Color.BLACK);
        
        
        rootContainer.requestLayout();
        
        
        if (!rootContainer.getChildren().isEmpty()) {
            for (Node child : rootContainer.getChildren()) {
                if (child instanceof Region) {
                    Region region = (javafx.scene.layout.Region) child;
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

