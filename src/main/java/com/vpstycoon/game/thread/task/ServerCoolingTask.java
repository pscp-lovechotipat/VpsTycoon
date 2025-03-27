package com.vpstycoon.game.thread.task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import java.util.Random;

/**
 * Server Cooling Task - Placeholder class
 * In the full implementation, player would need to manage server temperatures
 */
public class ServerCoolingTask extends GameTask {

    private static final Random random = new Random();

    public ServerCoolingTask() {
        super(
                "Server Cooling",
                "Maintain optimal server temperature by managing cooling systems",
                "/images/task/cooling_task.png",
                6000, // reward
                20,  // penalty (0.2 * 100)
                2,    // difficulty
                45    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        VBox placeholderPane = new VBox(20);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setPadding(new Insets(30));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        Text placeholderText = new Text("COOLING SYSTEM MANAGEMENT");
        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        placeholderText.setFill(Color.web("#00ffff"));
        
        Text descText = new Text("Balance server temperatures by adjusting cooling systems.");
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Server rack display
        javafx.scene.layout.HBox serverRack = new javafx.scene.layout.HBox(15);
        serverRack.setAlignment(Pos.CENTER);
        serverRack.setPadding(new Insets(20));
        serverRack.setStyle("-fx-background-color: #0a1520; -fx-border-color: #3a4a5a; -fx-border-width: 2px;");
        
        log("Creating server rack with 4 servers");
        
        // Create 4 server nodes with sliders
        for (int i = 0; i < 4; i++) {
            final int serverIndex = i;
            VBox serverNode = new VBox(10);
            serverNode.setAlignment(Pos.CENTER);
            serverNode.setPadding(new Insets(10));
            serverNode.setPrefWidth(120);
            
            // Server label
            Text serverLabel = new Text("SERVER " + (i + 1));
            serverLabel.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 14));
            serverLabel.setFill(Color.LIGHTCYAN);
            
            // Temperature display
            javafx.scene.shape.Rectangle tempIndicator = new javafx.scene.shape.Rectangle(80, 30);
            tempIndicator.setFill(Color.RED);
            
            // Random initial temperature between 60-90°C
            int initialTemp = random.nextInt(31) + 60;
            log("Server " + (i + 1) + " initial temperature: " + initialTemp + "°C");
            
            Text tempText = new Text(initialTemp + "°C");
            tempText.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 16));
            tempText.setFill(Color.WHITE);
            
            // Cooling slider
            javafx.scene.control.Slider coolingSlider = new javafx.scene.control.Slider(0, 100, 50);
            coolingSlider.setShowTickMarks(true);
            coolingSlider.setShowTickLabels(true);
            coolingSlider.setMajorTickUnit(25);
            coolingSlider.setPrefWidth(100);
            
            // Update temperature based on cooling level
            coolingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                // Calculate new temperature based on cooling (higher cooling = lower temp)
                int newTemp = (int)(90 - (newVal.doubleValue() * 0.4));
                tempText.setText(newTemp + "°C");
                
                log("Server " + (serverIndex + 1) + " temperature changed to " + newTemp + "°C (cooling: " + newVal.intValue() + "%)");
                
                // Update color based on temperature
                if (newTemp < 60) {
                    tempIndicator.setFill(Color.BLUE); // Too cold
                } else if (newTemp < 70) {
                    tempIndicator.setFill(Color.GREEN); // Optimal
                } else if (newTemp < 80) {
                    tempIndicator.setFill(Color.YELLOW); // Warning
                } else {
                    tempIndicator.setFill(Color.RED); // Danger
                }
                
                // Check if all servers are in optimal range (60-70°C)
                checkTaskCompletion();
            });
            
            // Add components to server node
            StackPane tempDisplay = new StackPane(tempIndicator, tempText);
            serverNode.getChildren().addAll(serverLabel, tempDisplay, coolingSlider);
            
            // Add server to rack
            serverRack.getChildren().add(serverNode);
        }
        
        placeholderPane.getChildren().addAll(placeholderText, descText, serverRack);
        gamePane.getChildren().add(placeholderPane);
    }
    
    private void checkTaskCompletion() {
        // Check if all server temperatures are in optimal range
        boolean allOptimal = true;
        int optimalCount = 0;
        
        for (javafx.scene.Node node : ((javafx.scene.layout.HBox)((VBox)gamePane.getChildren().get(0)).getChildren().get(2)).getChildren()) {
            VBox serverNode = (VBox) node;
            StackPane tempDisplay = (StackPane) serverNode.getChildren().get(1);
            Text tempText = (Text) tempDisplay.getChildren().get(1);
            
            // Extract temperature value
            String tempStr = tempText.getText();
            int temp = Integer.parseInt(tempStr.substring(0, tempStr.length() - 2));
            
            // Check if in optimal range
            if (temp >= 60 && temp <= 70) {
                optimalCount++;
            } else {
                allOptimal = false;
            }
        }
        
        log("Current server status: " + optimalCount + "/4 servers in optimal range");
        
        // Complete task if all servers are in optimal range
        if (allOptimal) {
            log("All servers in optimal temperature range, completing task");
            completeTask();
        }
    }
} 