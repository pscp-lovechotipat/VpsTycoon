package com.vpstycoon.ui.cutscene;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Demo application to showcase the enhanced cutscene background
 */
public class CutsceneDemo extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Create configuration - use singleton instance since constructor is private
        GameConfig gameConfig = DefaultGameConfig.getInstance();
        
        // Create a simple navigator that just closes the application when showMainMenu is called
        Navigator demoNavigator = new Navigator() {
            @Override
            public void showMainMenu() {
                System.out.println("Demo completed. Exiting application.");
                primaryStage.close();
            }
            
            @Override
            public void showSettings() {
                // Not needed for demo
            }
            
            @Override
            public void startNewGame() {
                // Not needed for demo
            }
            
            @Override
            public void continueGame() {
                // Not needed for demo
            }
            
            @Override
            public void showLoadGame() {
                // Not needed for demo
            }
            
            @Override
            public void showInGameSettings() {
                // Not needed for demo
            }
        };
        
        // Create a basic screen manager for the demo
        StackPane rootPane = new StackPane();
        
        // Create the cutscene screen with enhanced background
        CutsceneScreen cutsceneScreen = new CutsceneScreen(gameConfig, null, demoNavigator);
        rootPane.getChildren().add(cutsceneScreen);
        
        // Set up the scene
        Scene scene = new Scene(rootPane, 1200, 800);
        primaryStage.setTitle("VPS Tycoon - Cutscene Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 