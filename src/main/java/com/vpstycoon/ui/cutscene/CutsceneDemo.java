package com.vpstycoon.ui.cutscene;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class CutsceneDemo extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        
        GameConfig gameConfig = DefaultGameConfig.getInstance();
        
        
        Navigator demoNavigator = new Navigator() {
            @Override
            public void showMainMenu() {
                System.out.println("Demo completed. Exiting application.");
                primaryStage.close();
            }
            
            @Override
            public void showSettings() {
                
            }
            
            @Override
            public void startNewGame() {
                
            }
            
            @Override
            public void continueGame() {
                
            }
            
            @Override
            public void showLoadGame() {
                
            }
            
            @Override
            public void showInGameSettings() {
                
            }
        };
        
        
        StackPane rootPane = new StackPane();
        
        
        CutsceneScreen cutsceneScreen = new CutsceneScreen(gameConfig, null, demoNavigator);
        rootPane.getChildren().add(cutsceneScreen);
        
        
        Scene scene = new Scene(rootPane, 1200, 800);
        primaryStage.setTitle("VPS Tycoon - Cutscene Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 
