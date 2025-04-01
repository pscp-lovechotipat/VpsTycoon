package com.vpstycoon.view;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.view.interfaces.ISceneController;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class SceneController implements ISceneController {
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

    
    @Override
    public void setContent(Parent content) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(content);
    }

    
    @Override
    public void updateResolution() {
        ScreenResolution resolution = config.getResolution();
        
        
        setRegionSize(rootContainer, resolution.getWidth(), resolution.getHeight());
        
        
        screenManager.applySettings(stage, mainScene);
        
        
        mainScene.setFill(Color.BLACK);
        
        
        rootContainer.requestLayout();
        
        
        if (!rootContainer.getChildren().isEmpty()) {
            for (javafx.scene.Node child : rootContainer.getChildren()) {
                if (child instanceof Region) {
                    Region region = (Region) child;
                    setRegionSize(region, resolution.getWidth(), resolution.getHeight());
                    region.requestLayout();
                }
            }
        }
    }
    
    
    private void setRegionSize(Region region, double width, double height) {
        region.setPrefWidth(width);
        region.setPrefHeight(height);
        region.setMinWidth(width);
        region.setMinHeight(height);
        region.setMaxWidth(width);
        region.setMaxHeight(height);
    }
    
    
    @Override
    public Stage getStage() {
        return stage;
    }
    
    
    @Override
    public Scene getScene() {
        return mainScene;
    }
} 

