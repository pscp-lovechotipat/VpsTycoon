package com.vpstycoon.game.thread.task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Password Cracking Task - Placeholder class
 * In the full implementation, player would need to crack a digital lock
 */
public class PasswordCrackingTask extends GameTask {

    public PasswordCrackingTask() {
        super(
                "Password Override",
                "Bypass security by cracking the digital lock",
                "/images/task/password_task.png",
                7000, // reward
                25,  // penalty (0.25 * 100)
                3,    // difficulty
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
        
        Text placeholderText = new Text("SECURITY OVERRIDE SYSTEM");
        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        placeholderText.setFill(Color.web("#00ffff"));
        
        Text descText = new Text("Crack the security code to gain access to protected systems.");
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Temporary easy complete button (for testing only)
        Button completeButton = new Button("OVERRIDE SECURITY");
        completeButton.getStyleClass().add("button");
        completeButton.setOnAction(e -> completeTask());
        
        placeholderPane.getChildren().addAll(placeholderText, descText, completeButton);
        gamePane.getChildren().add(placeholderPane);
    }
}