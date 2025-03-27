package com.vpstycoon.game.thread.task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Network Routing Task - Placeholder class
 * In the full implementation, player would need to route data through a network
 */
public class NetworkRoutingTask extends GameTask {

    public NetworkRoutingTask() {
        super(
                "Network Routing",
                "Route data packets through the optimal network path",
                "/images/task/network_task.png",
                6500, // reward
                20,  // penalty (0.2 * 100)
                3,    // difficulty
                45    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        VBox placeholderPane = new VBox(20);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setPadding(new Insets(30));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        Text placeholderText = new Text("NETWORK ROUTING SYSTEM");
        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        placeholderText.setFill(Color.web("#00ffff"));
        
        Text descText = new Text("Connect matching network nodes to establish data routes.");
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Main routing area
        Pane routingPane = new Pane();
        routingPane.setPrefSize(600, 400);
        routingPane.setStyle("-fx-background-color: #0a1520; -fx-border-color: #3a4a5a; -fx-border-width: 2px;");
        
        // Create the wire pairs - similar to Among Us wire task
        List<Color> wireColors = Arrays.asList(
            Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN
        );
        Collections.shuffle(wireColors, new Random());
        
        // Create network nodes
        List<NetworkNode> leftNodes = new ArrayList<>();
        List<NetworkNode> rightNodes = new ArrayList<>();
        Map<NetworkNode, NetworkNode> nodePairs = new HashMap<>();
        
        for (int i = 0; i < 4; i++) {
            Color wireColor = wireColors.get(i);
            
            // Left node
            NetworkNode leftNode = new NetworkNode(wireColor, 50, 80 + i * 80);
            leftNodes.add(leftNode);
            
            // Right node (shuffled order)
            NetworkNode rightNode = new NetworkNode(wireColor, 550, 80 + i * 80);
            rightNodes.add(rightNode);
            
            // Remember the pairing
            nodePairs.put(leftNode, rightNode);
            
            // Add to pane
            routingPane.getChildren().addAll(leftNode.getCircle(), rightNode.getCircle());
        }
        
        // Shuffle right nodes positions
        Collections.shuffle(rightNodes, new Random());
        for (int i = 0; i < rightNodes.size(); i++) {
            NetworkNode node = rightNodes.get(i);
            node.setY(80 + i * 80);
        }
        
        // Active wire being drawn
        final Line[] activeLine = new Line[1];
        final NetworkNode[] activeSource = new NetworkNode[1];
        
        // Click handlers for nodes
        for (NetworkNode node : leftNodes) {
            node.getCircle().setOnMouseClicked(event -> {
                // Clear any previous line being drawn
                if (activeLine[0] != null) {
                    routingPane.getChildren().remove(activeLine[0]);
                }
                
                // Start new line
                activeLine[0] = new Line(node.getX(), node.getY(), node.getX(), node.getY());
                activeLine[0].setStroke(node.getColor());
                activeLine[0].setStrokeWidth(4);
                routingPane.getChildren().add(activeLine[0]);
                
                // Remember source
                activeSource[0] = node;
                
                log("Started connection from node with color: " + colorToString(node.getColor()));
                
                event.consume();
            });
        }
        
        // Mouse move handler
        routingPane.setOnMouseMoved(event -> {
            if (activeLine[0] != null) {
                activeLine[0].setEndX(event.getX());
                activeLine[0].setEndY(event.getY());
            }
        });
        
        // Click handlers for right nodes
        for (NetworkNode node : rightNodes) {
            node.getCircle().setOnMouseClicked(event -> {
                if (activeLine[0] != null && activeSource[0] != null) {
                    // Check if correct connection
                    if (nodePairs.get(activeSource[0]) == node) {
                        // Correct connection
                        activeLine[0].setEndX(node.getX());
                        activeLine[0].setEndY(node.getY());
                        
                        // Mark as connected
                        activeSource[0].setConnected(true);
                        node.setConnected(true);
                        
                        log("Correct connection established for color: " + colorToString(node.getColor()));
                        
                        // Check if all connected
                        checkTaskCompletion(leftNodes);
                    } else {
                        // Wrong connection, remove line
                        routingPane.getChildren().remove(activeLine[0]);
                        log("Wrong connection attempt, removed line");
                    }
                    
                    // Reset active elements
                    activeLine[0] = null;
                    activeSource[0] = null;
                }
                
                event.consume();
            });
        }
        
        placeholderPane.getChildren().addAll(placeholderText, descText, routingPane);
        gamePane.getChildren().add(placeholderPane);
    }
    
    // Helper method to check if all wires are correctly connected
    private void checkTaskCompletion(List<NetworkNode> nodes) {
        boolean allConnected = true;
        int connectedCount = 0;
        
        for (NetworkNode node : nodes) {
            if (node.isConnected()) {
                connectedCount++;
            } else {
                allConnected = false;
            }
        }
        
        log("Current connections: " + connectedCount + "/4 completed");
        
        if (allConnected) {
            log("All network connections established correctly, completing task");
            completeTask();
        }
    }
    
    // Helper to convert color to string for logging
    private String colorToString(Color color) {
        if (color.equals(Color.RED)) return "RED";
        if (color.equals(Color.BLUE)) return "BLUE";
        if (color.equals(Color.YELLOW)) return "YELLOW";
        if (color.equals(Color.GREEN)) return "GREEN";
        return "UNKNOWN";
    }
    
    // Network node representation
    private class NetworkNode {
        private final Circle circle;
        private final Color color;
        private double x, y;
        private boolean connected = false;
        
        public NetworkNode(Color color, double x, double y) {
            this.color = color;
            this.x = x;
            this.y = y;
            
            circle = new Circle(x, y, 15);
            circle.setFill(color);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
        }
        
        public Circle getCircle() {
            return circle;
        }
        
        public Color getColor() {
            return color;
        }
        
        public double getX() {
            return x;
        }
        
        public double getY() {
            return y;
        }
        
        public void setY(double y) {
            this.y = y;
            circle.setCenterY(y);
        }
        
        public boolean isConnected() {
            return connected;
        }
        
        public void setConnected(boolean connected) {
            this.connected = connected;
            // Visual update
            if (connected) {
                circle.setStroke(Color.LIMEGREEN);
                circle.setStrokeWidth(3);
            }
        }
    }
} 