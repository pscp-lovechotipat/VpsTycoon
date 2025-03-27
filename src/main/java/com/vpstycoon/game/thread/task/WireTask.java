package com.vpstycoon.game.thread.task;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wire Connection Task - player must connect matching colored wires
 */
public class WireTask extends GameTask {
    private static final String[] WIRE_COLORS = {
            "#ff0066", // neon pink
            "#00ffff", // cyan
            "#ffff00", // yellow
            "#00ff00"  // green
    };
    
    private final int wireCount;
    private final Map<Circle, Circle> wireConnections = new HashMap<>();
    private final List<Circle> leftWires = new ArrayList<>();
    private final List<Circle> rightWires = new ArrayList<>();
    
    private Circle selectedWire = null;
    private Line currentLine = null;
    private final List<Line> connectedLines = new ArrayList<>();

    /**
     * Constructor for Wire Task
     */
    public WireTask() {
        super(
                "Neural Network Calibration",
                "Connect matching colored neural pathways to calibrate the system",
                "/images/task/wire_task.png",
                5000, // reward
                0,  // penalty
                2,    // difficulty
                45    // time limit in seconds
        );
        this.wireCount = 4; // Default to 4 wires
    }
    
    /**
     * Constructor with custom wire count
     * 
     * @param wireCount Number of wires to connect
     */
    public WireTask(int wireCount) {
        super(
                "Neural Network Calibration",
                "Connect matching colored neural pathways to calibrate the system",
                "/images/task/wire_task.png",
                5000, // reward
                0,  // penalty
                wireCount <= 3 ? 1 : (wireCount <= 5 ? 2 : 3), // difficulty based on wire count
                45    // time limit in seconds
        );
        this.wireCount = Math.min(wireCount, WIRE_COLORS.length);
    }
    
    /**
     * Get random colors for the wires
     * 
     * @param count Number of colors to get
     * @return Array of color hex codes
     */
    private String[] getRandomColors(int count) {
        String[] colors = Arrays.copyOf(WIRE_COLORS, Math.min(count, WIRE_COLORS.length));
        return colors;
    }
    
    /**
     * Shuffle an array in place
     * 
     * @param array Array to shuffle
     */
    private void shuffleArray(String[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Simple swap
            String temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
    
    /**
     * Start a wire connection from a connector
     * 
     * @param wireIndex Index of the wire
     * @param connectors Array of connectors
     * @param lines Array of connection lines
     */
    private void startConnection(int wireIndex, Circle[] connectors, Line[] lines) {
        // Clear any existing line at this index
        lines[wireIndex].setStroke(Color.web(WIRE_COLORS[wireIndex]));
        lines[wireIndex].setStartX(connectors[wireIndex].getLayoutX() + 15);
        lines[wireIndex].setStartY(connectors[wireIndex].getLayoutY() + 15);
        lines[wireIndex].setEndX(connectors[wireIndex].getLayoutX() + 15);
        lines[wireIndex].setEndY(connectors[wireIndex].getLayoutY() + 15);
    }
    
    /**
     * Update a wire connection as it's being dragged
     * 
     * @param e Mouse event
     * @param wireIndex Index of the wire
     * @param lines Array of connection lines
     */
    private void updateConnection(javafx.scene.input.MouseEvent e, int wireIndex, Line[] lines) {
        lines[wireIndex].setEndX(e.getX());
        lines[wireIndex].setEndY(e.getY());
    }
    
    /**
     * Finish a wire connection to a connector
     * 
     * @param e Mouse event
     * @param wireIndex Index of the wire
     * @param rightConnectors Right side connectors
     * @param lines Array of connection lines
     * @param rightColors Colors of right connectors
     */
    private void finishConnection(javafx.scene.input.MouseEvent e, int wireIndex, Circle[] rightConnectors, Line[] lines, String[] rightColors) {
        // Check if the line ends near a right connector
        for (int i = 0; i < rightConnectors.length; i++) {
            Circle target = rightConnectors[i];
            double targetX = target.getLayoutX() + 15;
            double targetY = target.getLayoutY() + 15;
            
            // Check if mouse is near the target
            if (Math.abs(e.getX() - targetX) < 20 && Math.abs(e.getY() - targetY) < 20) {
                // Snap the line to the target
                lines[wireIndex].setEndX(targetX);
                lines[wireIndex].setEndY(targetY);
                
                // Check if this completes all connections
                boolean allConnected = true;
                for (int j = 0; j < lines.length; j++) {
                    // A line is connected if its end coordinates match a right connector
                    boolean lineConnected = false;
                    for (int k = 0; k < rightConnectors.length; k++) {
                        double connectorX = rightConnectors[k].getLayoutX() + 15;
                        double connectorY = rightConnectors[k].getLayoutY() + 15;
                        
                        if (Math.abs(lines[j].getEndX() - connectorX) < 5 && Math.abs(lines[j].getEndY() - connectorY) < 5) {
                            lineConnected = true;
                            
                            // Check color match (on array indices for simplicity)
                            if (j != k) {
                                // Colors don't match - game not complete yet
                                return;
                            }
                            
                            break;
                        }
                    }
                    
                    if (!lineConnected) {
                        allConnected = false;
                    }
                }
                
                // If all wires are connected correctly, complete the task
                if (allConnected) {
                    completeTask();
                }
                
                return;
            }
        }
        
        // If not connecting to a target, reset the line
        lines[wireIndex].setStroke(Color.TRANSPARENT);
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Create the task UI
        VBox taskContent = new VBox(10);
        taskContent.setAlignment(Pos.CENTER);
        taskContent.setPadding(new Insets(20));
        taskContent.setMaxWidth(600);
        taskContent.setMaxHeight(500);
        taskContent.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 10;");
        
        // Task title
        Label titleLabel = new Label(getTaskName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #cba6f7;");
        
        // Task description
        Label descriptionLabel = new Label(getTaskDescription());
        descriptionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cdd6f4;");
        descriptionLabel.setWrapText(true);
        
        // Timer label
        timerLabel = new Label("Time remaining: " + getTimeLimit() + "s");
        timerLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f38ba8;");
        
        // Create a container for the wires
        VBox wiresContainer = new VBox(15);
        wiresContainer.setAlignment(Pos.CENTER);
        wiresContainer.setPadding(new Insets(20));
        wiresContainer.setStyle("-fx-background-color: #313244; -fx-background-radius: 8;");
        
        // Create wires with random colors and positions
        String[] leftColors = getRandomColors(wireCount);
        String[] rightColors = leftColors.clone();
        shuffleArray(rightColors);
        
        // Store the connections
        Circle[] leftConnectors = new Circle[wireCount];
        Circle[] rightConnectors = new Circle[wireCount];
        Line[] connectionLines = new Line[wireCount];
        
        for (int i = 0; i < wireCount; i++) {
            connectionLines[i] = new Line();
            connectionLines[i].setStroke(Color.TRANSPARENT);
            connectionLines[i].setStrokeWidth(3);
        }
        
        // Add connection lines first (so they appear behind connectors)
        Pane wirePane = new Pane();
        wirePane.setPrefSize(500, 300);
        for (Line line : connectionLines) {
            wirePane.getChildren().add(line);
        }
        
        // Create left side connectors
        VBox leftSide = new VBox(20);
        leftSide.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < wireCount; i++) {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(leftColors[i]));
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
            leftConnectors[i] = circle;
            
            final int wireIndex = i;
            circle.setOnMousePressed(e -> startConnection(wireIndex, leftConnectors, connectionLines));
            circle.setOnMouseDragged(e -> updateConnection(e, wireIndex, connectionLines));
            circle.setOnMouseReleased(e -> finishConnection(e, wireIndex, rightConnectors, connectionLines, rightColors));
            
            leftSide.getChildren().add(circle);
        }
        
        // Create right side connectors
        VBox rightSide = new VBox(20);
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        for (int i = 0; i < wireCount; i++) {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(rightColors[i]));
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
            rightConnectors[i] = circle;
            rightSide.getChildren().add(circle);
        }
        
        // Assemble the wire layout
        HBox wiresLayout = new HBox();
        wiresLayout.setAlignment(Pos.CENTER);
        wiresLayout.getChildren().addAll(leftSide, wirePane, rightSide);
        wiresContainer.getChildren().add(wiresLayout);
        
        // Add everything to the task content
        taskContent.getChildren().addAll(
            titleLabel,
            descriptionLabel,
            timerLabel,
            wiresContainer
        );
        
        // Add a reset button
        Button resetButton = new Button("RESET CONNECTIONS");
        resetButton.setStyle("-fx-background-color: #45475a; -fx-text-fill: white; -fx-padding: 8 16;");
        resetButton.setOnAction(e -> {
            for (Line line : connectionLines) {
                line.setStroke(Color.TRANSPARENT);
            }
        });
        
        taskContent.getChildren().add(resetButton);
        
        // Add to the task container
        taskContainer.getChildren().add(taskContent);
        
        // Add the task content to the task pane
        taskPane.setCenter(taskContent);
    }
} 