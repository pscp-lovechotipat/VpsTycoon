package com.vpstycoon.game.thread.task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.logging.Logger;

/**
 * Resource Optimization Task - Placeholder class
 * In the full implementation, player would need to allocate resources efficiently
 */
public class ResourceOptimizationTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(ResourceOptimizationTask.class.getName());

    public ResourceOptimizationTask() {
        super(
                "Resource Allocation",
                "Optimize system resources for maximum efficiency",
                "/images/task/resource_task.png",
                7000, // reward
                25,  // penalty (0.25 * 100)
                4,    // difficulty
                60    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Placeholder UI for now
        VBox placeholderPane = new VBox(20);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setPadding(new Insets(30));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        Text placeholderText = new Text("RESOURCE ALLOCATION SYSTEM");
        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        placeholderText.setFill(Color.web("#00ffff"));
        
        Text descText = new Text("Distribute computing resources to maximize system throughput.");
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Temporary easy complete button (for testing only)
        Button completeButton = new Button("OPTIMIZE RESOURCES");
        completeButton.getStyleClass().add("button");
        completeButton.setOnAction(e -> completeTask());
        
        placeholderPane.getChildren().addAll(placeholderText, descText, completeButton);
        gamePane.getChildren().add(placeholderPane);
    }
} 