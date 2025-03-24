package com.vpstycoon.ui.game.components;

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

    // Cyberpunk theme colors
    private static final Color CYBER_PURPLE_BRIGHT = Color.rgb(200, 50, 255);
    private static final Color CYBER_PURPLE_DARK = Color.rgb(120, 20, 180);
    private static final Color CYBER_PINK = Color.rgb(255, 50, 180);
    private static final Color CYBER_PINK_DARK = Color.rgb(180, 20, 120);
    private static final Color CYBER_BLUE = Color.rgb(50, 200, 255);
    private static final Color CYBER_BLUE_DARK = Color.rgb(20, 100, 180);
    private static final Color CYBER_GREEN = Color.rgb(0, 255, 170);
    private static final Color CYBER_GREEN_DARK = Color.rgb(0, 180, 120);
    private static final Color CYBER_YELLOW = Color.rgb(255, 200, 50);
    private static final Color CYBER_YELLOW_DARK = Color.rgb(180, 140, 20);
    private static final Color CYBER_LIGHT_GREEN = Color.rgb(100, 255, 100);
    private static final Color CYBER_LIGHT_GREEN_DARK = Color.rgb(60, 180, 60);

    public GameMenuBar(GameplayContentPane parent) {
        super(20);
        this.parent = parent;
        setPadding(new Insets(40));
        setAlignment(Pos.TOP_CENTER);
        setPrefHeight(50);
        setMaxHeight(50);

        setStyle("-fx-background-color: transparent;");

        initializeStatusButtons();
    }

    private void initializeStatusButtons() {
        // 1. Rack Slots - Yellow theme
        VBox rackSlotsStatus = createStatusButton(
                "Rack Slots",
                CYBER_YELLOW,
                CYBER_YELLOW_DARK
        );

        // 2. Network Speed - Blue theme
        VBox networkStatus = createStatusButton(
                "Network",
                CYBER_BLUE,
                CYBER_BLUE_DARK
        );

        // 3. Server Efficiency - Pink theme
        VBox deployStatus = createStatusButton(
                "Deploy",
                CYBER_PINK,
                CYBER_PINK_DARK
        );

        // 4. Marketing - Green theme
        VBox marketingStatus = createStatusButton(
                "Marketing",
                CYBER_GREEN,
                CYBER_GREEN_DARK
        );

        // 5. Security - Purple theme
        VBox securityStatus = createStatusButton(
                "Security",
                CYBER_PURPLE_BRIGHT,
                CYBER_PURPLE_DARK
        );

        // 6. Management - Light Green theme
        VBox managementStatus = createStatusButton(
                "Management",
                CYBER_LIGHT_GREEN,
                CYBER_LIGHT_GREEN_DARK
        );

        getChildren().addAll(rackSlotsStatus, networkStatus, deployStatus,
                marketingStatus, securityStatus, managementStatus);
    }

    private VBox createStatusButton(String labelText, Color topColor, Color bottomColor) {
        CircleStatusButton statusButton = new CircleStatusButton(labelText, topColor, bottomColor, parent);
        return statusButton.getContainer();
    }
} 