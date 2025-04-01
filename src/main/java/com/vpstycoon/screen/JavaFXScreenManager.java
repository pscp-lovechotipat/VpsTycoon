package com.vpstycoon.screen;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.view.base.GameScreen;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JavaFXScreenManager implements ScreenManager {
    private final GameConfig config;
    private final Stage stage;
    
    public JavaFXScreenManager(GameConfig config, Stage stage) {
        this.config = config;
        this.stage = stage;
    }
    
    @Override
    public void applySettings(Stage stage, Scene scene) {
        ScreenResolution resolution = config.getResolution();
        
        if (config.isFullscreen()) {
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            stage.setFullScreen(true);
        } else {
            stage.setFullScreen(false);
            

            stage.setWidth(resolution.getWidth());
            stage.setHeight(resolution.getHeight());
            

            stage.sizeToScene();
            

            stage.centerOnScreen();
        }


        if (scene != null) {

            scene.setFill(Color.BLACK);
            

            Parent currentRoot = scene.getRoot();
            

            if (currentRoot instanceof Region) {
                Region rootRegion = (Region) currentRoot;
                

                rootRegion.setPadding(javafx.geometry.Insets.EMPTY);
                rootRegion.setStyle("-fx-background-color: black; -fx-padding: 0; -fx-margin: 0;");
                

                rootRegion.setPrefWidth(resolution.getWidth());
                rootRegion.setPrefHeight(resolution.getHeight());
                rootRegion.setMinWidth(resolution.getWidth());
                rootRegion.setMinHeight(resolution.getHeight());
                rootRegion.setMaxWidth(resolution.getWidth());
                rootRegion.setMaxHeight(resolution.getHeight());
                

                rootRegion.requestLayout();
                rootRegion.layout();
            }
            

            StackPane tempContainer = new StackPane();
            tempContainer.setStyle("-fx-background-color: black;");
            scene.setRoot(tempContainer);
            

            scene.setRoot(currentRoot);
        }
    }
    
    @Override
    public void setResolution(ScreenResolution resolution) {
        config.setResolution(resolution);
    }
    
    @Override
    public void setFullscreen(boolean fullscreen) {
        config.setFullscreen(fullscreen);
        if (fullscreen) {
            config.setResolution(ScreenResolution.getMaxSupportedResolution());
        }
    }
    
    @Override
    public void switchScreen(Node screen) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(new StackPane(screen));
            stage.setScene(scene);
        } else {
            StackPane root = (StackPane) scene.getRoot();
            
            if (!root.getChildren().isEmpty()) {
                Node currentScreen = root.getChildren().get(0);
                
                FadeTransition fadeOut = new FadeTransition(
                    javafx.util.Duration.millis(300), currentScreen);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                
                screen.setOpacity(0.0);
                
                root.getChildren().add(screen);
                
                FadeTransition fadeIn = new FadeTransition(
                    javafx.util.Duration.millis(300), screen);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                
                fadeOut.setOnFinished(event -> {
                    root.getChildren().remove(currentScreen);
                    
                    fadeIn.play();
                });
                
                fadeOut.play();
            } else {
                root.getChildren().add(screen);
                
                FadeTransition fadeIn = new FadeTransition(
                    Duration.millis(300), screen);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
            
            root.requestLayout();
        }
    }
    
    @Override
    public void switchScreen(GameScreen screen) {

        switchScreen(screen.getRoot());
        

        screen.onShow();
    }
    
    @Override
    public void updateScreenResolution() {

        applySettings(stage, stage.getScene());
        

        if (stage.getScene() != null && stage.getScene().getRoot() != null) {
            StackPane root = (StackPane) stage.getScene().getRoot();
            if (!root.getChildren().isEmpty()) {

                root.requestLayout();
            }
        }
    }

    @Override
    public void prepareScreen(Node screen) {
        Scene scene = stage.getScene();

        
        if (scene == null) {
            
            StackPane root = new StackPane(screen);
            root.setStyle("-fx-background-color: black;");

            
            scene = new Scene(root, config.getResolution().getWidth(), config.getResolution().getHeight());
            scene.setFill(Color.BLACK);

            
            stage.setScene(scene);
        } else {
            
            if (!(scene.getRoot() instanceof StackPane)) {
                
                StackPane root = new StackPane(screen);
                root.setStyle("-fx-background-color: black;");

                
                scene.setRoot(root);
            } else {
                
                StackPane root = (StackPane) scene.getRoot();

                
                if (!root.getChildren().isEmpty()) {
                    screen.setOpacity(1.0); 
                    root.getChildren().add(screen);
                } else {
                    
                    root.getChildren().add(screen);
                }
            }
        }

        
        applySettings(stage, scene);
    }

    @Override
    public void prepareScreen(GameScreen screen) {
        
        prepareScreen(screen.getRoot());

        
        screen.onShow();
    }
}

