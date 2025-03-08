package com.vpstycoon.ui.game.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Creates the top menu bar with status indicators.
 */
public class GameMenuBar extends HBox {

    public GameMenuBar() {
        super(20);
        setPadding(new Insets(40));
        setAlignment(Pos.TOP_CENTER);
        setPrefHeight(50);
        setMaxHeight(50);
        
        initializeStatusButtons();
    }
    
    private void initializeStatusButtons() {
        // 1. Deploy status
        VBox deployStatus = createStatusButton(
                "Deploy", 
                12, 
                Color.rgb(240, 50, 50),
                Color.rgb(180, 20, 20)
        );

        // 2. Network status
        VBox networkStatus = createStatusButton(
                "Network", 
                8, 
                Color.rgb(50, 150, 240),
                Color.rgb(20, 100, 200)
        );

        // 3. Security status
        VBox securityStatus = createStatusButton(
                "Security", 
                5, 
                Color.rgb(150, 50, 220),
                Color.rgb(100, 20, 180)
        );

        // 4. Marketing status
        VBox marketingStatus = createStatusButton(
                "Marketing", 
                10, 
                Color.rgb(50, 200, 100),
                Color.rgb(20, 150, 50)
        );

        // Add all status indicators to menu bar
        getChildren().addAll(deployStatus, networkStatus, securityStatus, marketingStatus);
    }
    
    private VBox createStatusButton(String labelText, int number, Color topColor, Color bottomColor) {
        // Create CircleStatusButton and wrap it in a VBox
        CircleStatusButton statusButton = new CircleStatusButton(labelText, number, topColor, bottomColor);
        return statusButton.getContainer();
    }
} 