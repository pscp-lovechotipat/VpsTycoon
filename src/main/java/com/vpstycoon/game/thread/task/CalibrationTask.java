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
 * System Calibration Task - Placeholder class
 * In the full implementation, player would need to calibrate system parameters
 */
public class CalibrationTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(CalibrationTask.class.getName());

    public CalibrationTask() {
        super(
                "System Calibration",
                "Fine-tune system parameters for optimal performance",
                "/images/task/calibration_task.png",
                5500, // reward
                15,  // penalty (0.15 * 100)
                2,    // difficulty
                40    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Placeholder UI for now
        VBox placeholderPane = new VBox(20);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setPadding(new Insets(30));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        Text placeholderText = new Text("SYSTEM CALIBRATION");
        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        placeholderText.setFill(Color.web("#00ffff"));
        
        Text descText = new Text("Adjust system parameters to achieve peak performance levels.");
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Temporary easy complete button (for testing only)
        Button completeButton = new Button("CALIBRATE SYSTEM");
        completeButton.getStyleClass().add("button");
        completeButton.setOnAction(e -> completeTask());
        
        placeholderPane.getChildren().addAll(placeholderText, descText, completeButton);
        gamePane.getChildren().add(placeholderPane);
    }
} 