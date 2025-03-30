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

        // ถ้ายังไม่มี Scene ให้สร้างใหม่
        if (scene == null) {
            // สร้าง StackPane และใส่ screen เข้าไป
            StackPane root = new StackPane(screen);
            root.setStyle("-fx-background-color: black;");

            // สร้าง Scene ใหม่พร้อม root ที่มี screen อยู่แล้ว
            scene = new Scene(root, config.getResolution().getWidth(), config.getResolution().getHeight());
            scene.setFill(Color.BLACK);

            // ตั้งค่า Scene ให้ stage
            stage.setScene(scene);
        } else {
            // ถ้ามี Scene อยู่แล้ว แต่ยังไม่มี root
            if (!(scene.getRoot() instanceof StackPane)) {
                // สร้าง StackPane ใหม่และใส่ screen เข้าไป
                StackPane root = new StackPane(screen);
                root.setStyle("-fx-background-color: black;");

                // ตั้งค่า root ใหม่ให้ Scene
                scene.setRoot(root);
            } else {
                // ถ้ามี root ที่เป็น StackPane อยู่แล้ว
                StackPane root = (StackPane) scene.getRoot();

                // ถ้ามี children อยู่แล้ว ให้เพิ่ม screen เป็น child ใหม่
                if (!root.getChildren().isEmpty()) {
                    screen.setOpacity(1.0); // ตั้งค่าความโปร่งใสให้เห็นชัดเจน
                    root.getChildren().add(screen);
                } else {
                    // ถ้ายังไม่มี children ให้เพิ่ม screen เป็น child แรก
                    root.getChildren().add(screen);
                }
            }
        }

        // ปรับขนาดและตำแหน่งของ stage ตามการตั้งค่า
        applySettings(stage, scene);
    }

    @Override
    public void prepareScreen(GameScreen screen) {
        // เรียกใช้ method prepareScreen(Node) โดยส่ง root element ของ GameScreen
        prepareScreen(screen.getRoot());

        // เรียก onShow เพื่อให้ GameScreen รู้ว่าถูกแสดงแล้ว
        screen.onShow();
    }
}
