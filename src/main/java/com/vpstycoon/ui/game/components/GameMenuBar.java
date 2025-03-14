package com.vpstycoon.ui.game.components;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.status.CircleStatusButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Creates the top menu bar with status indicators in a cyberpunk theme.
 */
public class GameMenuBar extends HBox {
    private final GameplayContentPane parent;
    private AudioManager audioManager;

    // Cyberpunk theme colors
    private static final Color CYBER_PURPLE_BRIGHT = Color.rgb(200, 50, 255);
    private static final Color CYBER_PURPLE_DARK = Color.rgb(120, 20, 180);
    private static final Color CYBER_PINK = Color.rgb(255, 50, 180);
    private static final Color CYBER_PINK_DARK = Color.rgb(180, 20, 120);
    private static final Color CYBER_BLUE = Color.rgb(50, 200, 255);
    private static final Color CYBER_BLUE_DARK = Color.rgb(20, 100, 180);
    private static final Color CYBER_GREEN = Color.rgb(0, 255, 170);
    private static final Color CYBER_GREEN_DARK = Color.rgb(0, 180, 120);

    public GameMenuBar(GameplayContentPane parent) {
        super(20);
        this.parent = parent;
        setPadding(new Insets(40));
        setAlignment(Pos.TOP_CENTER);
        setPrefHeight(50);
        setMaxHeight(50);

        this.audioManager = AudioManager.getInstance();
    
        
        // Make the menu bar fully transparent
        setStyle("-fx-background-color: transparent;");
        
        initializeStatusButtons();
    }
    
    private void initializeStatusButtons() {
        // 1. Deploy status - Pink theme
        VBox deployStatus = createStatusButton(
                "Deploy", 
                12, 
                CYBER_PINK,
                CYBER_PINK_DARK
        );

        // 2. Network status - Blue theme
        VBox networkStatus = createStatusButton(
                "Network", 
                8, 
                CYBER_BLUE,
                CYBER_BLUE_DARK
        );

        // 3. Security status - Purple theme (main cyberpunk color)
        VBox securityStatus = createStatusButton(
                "Security", 
                5, 
                CYBER_PURPLE_BRIGHT,
                CYBER_PURPLE_DARK
        );

        // 4. Marketing status - Green theme
        VBox marketingStatus = createStatusButton(
                "Marketing", 
                10, 
                CYBER_GREEN,
                CYBER_GREEN_DARK
        );

        // Add all status indicators to menu bar
        getChildren().addAll(deployStatus, networkStatus, securityStatus, marketingStatus);
    }
    
    private VBox createStatusButton(String labelText, int number, Color topColor, Color bottomColor) {
        // Create CircleStatusButton and wrap it in a VBox
        CircleStatusButton statusButton = new CircleStatusButton(labelText, number, topColor, bottomColor, parent);
        audioManager.playSoundEffect("hover2.wav");
        return statusButton.getContainer();
    }
} 