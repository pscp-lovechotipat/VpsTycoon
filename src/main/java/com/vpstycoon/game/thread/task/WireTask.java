package com.vpstycoon.game.thread.task;

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

import java.util.*;


public class WireTask extends GameTask {
    private static final String[] WIRE_COLORS = {
            "#ff0066", 
            "#00ffff", 
            "#ffff00", 
            "#00ff00"  
    };
    
    private final int wireCount;
    private final Map<Circle, Circle> wireConnections = new HashMap<>();
    private final List<Circle> leftWires = new ArrayList<>();
    private final List<Circle> rightWires = new ArrayList<>();
    
    private Circle[] leftConnectors;
    private Circle[] rightConnectors;
    private Line[] connectionLines;
    
    private Circle selectedWire = null;
    private Line currentLine = null;
    private final List<Line> connectedLines = new ArrayList<>();

    
    public WireTask() {
        super(
                "Neural Network Calibration",
                "Connect matching colored neural pathways to calibrate the system",
                "/images/task/wire_task.png",
                5000, 
                0,  
                2,    
                45    
        );
        this.wireCount = 4; 
    }
    
    
    public WireTask(int wireCount) {
        super(
                "Neural Network Calibration",
                "Connect matching colored neural pathways to calibrate the system",
                "/images/task/wire_task.png",
                5000, 
                0,  
                wireCount <= 3 ? 1 : (wireCount <= 5 ? 2 : 3), 
                45    
        );
        this.wireCount = Math.min(wireCount, WIRE_COLORS.length);
    }
    
    
    private String[] getRandomColors(int count) {
        String[] colors = Arrays.copyOf(WIRE_COLORS, Math.min(count, WIRE_COLORS.length));
        return colors;
    }
    
    
    private void shuffleArray(String[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            
            String temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
    
    
    private void startConnection(int wireIndex, Circle[] connectors, Line[] lines) {
        
        lines[wireIndex].setStroke(Color.web(WIRE_COLORS[wireIndex]));
        lines[wireIndex].setStartX(connectors[wireIndex].getLayoutX() - 40);
        lines[wireIndex].setStartY(connectors[wireIndex].getLayoutY());
        lines[wireIndex].setEndX(connectors[wireIndex].getLayoutX() + 15);
        lines[wireIndex].setEndY(connectors[wireIndex].getLayoutY());
    }
    
    
    private void updateConnection(javafx.scene.input.MouseEvent e, int wireIndex, Line[] lines) {
        
        javafx.geometry.Point2D point = lines[wireIndex].getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
        lines[wireIndex].setEndX(point.getX());
        lines[wireIndex].setEndY(point.getY());
    }
    
    
    private void finishConnection(javafx.scene.input.MouseEvent e, int wireIndex, Circle[] rightConnectors, Line[] lines, String[] rightColors) {
        
        javafx.geometry.Point2D point = lines[wireIndex].getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
        double mouseX = point.getX();
        double mouseY = point.getY();
        
        
        for (int i = 0; i < rightConnectors.length; i++) {
            Circle target = rightConnectors[i];
            
            
            javafx.geometry.Point2D targetPoint = lines[wireIndex].getParent().sceneToLocal(
                target.localToScene(target.getBoundsInLocal()).getCenterX(),
                target.localToScene(target.getBoundsInLocal()).getCenterY()
            );
            double targetX = targetPoint.getX();
            double targetY = targetPoint.getY();
            
            
            if (Math.abs(mouseX - targetX) < 30 && Math.abs(mouseY - targetY) < 30) {
                
                lines[wireIndex].setEndX(targetX);
                lines[wireIndex].setEndY(targetY);
                
                
                lines[wireIndex].setStroke(leftConnectors[wireIndex].getFill());
                
                
                log("Connected wire " + wireIndex + " to connector " + i);
                
                
                String leftColor = ((javafx.scene.paint.Color)leftConnectors[wireIndex].getFill()).toString();
                String rightColor = ((javafx.scene.paint.Color)rightConnectors[i].getFill()).toString();
                log("Left color: " + leftColor + ", Right color: " + rightColor + ", Match: " + leftColor.equals(rightColor));
                
                
                boolean allConnected = true;
                boolean allCorrectColors = true;
                
                
                int[] currentConnections = new int[lines.length];
                java.util.Arrays.fill(currentConnections, -1); 
                
                
                for (int j = 0; j < lines.length; j++) {
                    boolean foundConnection = false;
                    
                    for (int k = 0; k < rightConnectors.length; k++) {
                        
                        javafx.geometry.Point2D connPoint = lines[j].getParent().sceneToLocal(
                            rightConnectors[k].localToScene(rightConnectors[k].getBoundsInLocal()).getCenterX(),
                            rightConnectors[k].localToScene(rightConnectors[k].getBoundsInLocal()).getCenterY()
                        );
                        
                        
                        if (lines[j].getStroke() != Color.TRANSPARENT && 
                            Math.abs(lines[j].getEndX() - connPoint.getX()) < 10 && 
                            Math.abs(lines[j].getEndY() - connPoint.getY()) < 10) {
                            
                            currentConnections[j] = k;
                            foundConnection = true;
                            
                            
                            String leftWireColor = ((javafx.scene.paint.Color)leftConnectors[j].getFill()).toString();
                            String rightWireColor = ((javafx.scene.paint.Color)rightConnectors[k].getFill()).toString();
                            
                            if (!leftWireColor.equals(rightWireColor)) {
                                log("Color mismatch: wire " + j + " (color " + leftWireColor + 
                                    ") connected to " + k + " (color " + rightWireColor + ")");
                                allCorrectColors = false;
                            } else {
                                log("Color match: wire " + j + " connected correctly");
                            }
                            
                            break;
                        }
                    }
                    
                    if (!foundConnection && lines[j].getStroke() != Color.TRANSPARENT) {
                        log("Warning: Wire " + j + " appears connected but endpoint not found near any connector");
                        allConnected = false;
                    }
                }
                
                
                int connectedCount = 0;
                for (int j = 0; j < currentConnections.length; j++) {
                    if (currentConnections[j] != -1) {
                        connectedCount++;
                    }
                }
                
                log("Connected wires: " + connectedCount + "/" + wireCount + 
                    " (allConnected=" + allConnected + 
                    ", allCorrectColors=" + allCorrectColors + ")");
                
                
                if (connectedCount == wireCount && allCorrectColors) {
                    log("All " + wireCount + " wires connected correctly!");
                    completeTask();
                }
                
                return;
            }
        }
        
        
        lines[wireIndex].setStroke(Color.TRANSPARENT);
    }

    @Override
    protected void initializeTaskSpecifics() {
        
        VBox taskContent = new VBox(15);
        taskContent.setAlignment(Pos.CENTER);
        taskContent.setPadding(new Insets(20));
        taskContent.setMaxWidth(600);
        taskContent.setMaxHeight(500);
        taskContent.setStyle("-fx-background-color: rgba(42, 27, 61, 0.7); -fx-background-radius: 5px; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(120, 0, 255, 0.2), 10, 0, 0, 3);");
        
        
        
        
        VBox wiresContainer = CyberpunkEffects.createCyberSection("CONNECTION MATRIX");
        wiresContainer.setAlignment(Pos.CENTER);
        wiresContainer.setPadding(new Insets(20));
        
        
        String[] leftColors = getRandomColors(wireCount);
        String[] rightColors = leftColors.clone();
        shuffleArray(rightColors);
        
        
        leftConnectors = new Circle[wireCount];
        rightConnectors = new Circle[wireCount];
        connectionLines = new Line[wireCount];
        
        for (int i = 0; i < wireCount; i++) {
            connectionLines[i] = new Line();
            connectionLines[i].setStroke(Color.TRANSPARENT);
            connectionLines[i].setStrokeWidth(3);
        }
        
        
        Pane wirePane = new Pane();
        wirePane.setPrefSize(500, 300);
        for (Line line : connectionLines) {
            wirePane.getChildren().add(line);
        }
        
        
        VBox leftSide = new VBox(20);
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setPadding(new Insets(0, 20, 0, 0));
        
        Label inputLabel = new Label("INPUT NODES");
        inputLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        leftSide.getChildren().add(inputLabel);
        
        for (int i = 0; i < wireCount; i++) {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(leftColors[i]));
            circle.setStroke(Color.web("#00F6FF"));
            circle.setStrokeWidth(2);
            
            
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(leftColors[i]));
            glow.setRadius(10);
            glow.setSpread(0.3);
            circle.setEffect(glow);
            
            leftConnectors[i] = circle;
            
            final int wireIndex = i;
            
            
            circle.setOnMouseEntered(e -> {
                circle.setStroke(Color.web("#E4FBFF"));
                circle.setStrokeWidth(3);
            });
            circle.setOnMouseExited(e -> {
                circle.setStroke(Color.web("#00F6FF"));
                circle.setStrokeWidth(2);
            });
            
            circle.setOnMousePressed(e -> startConnection(wireIndex, leftConnectors, connectionLines));
            circle.setOnMouseDragged(e -> updateConnection(e, wireIndex, connectionLines));
            circle.setOnMouseReleased(e -> finishConnection(e, wireIndex, rightConnectors, connectionLines, rightColors));
            
            leftSide.getChildren().add(circle);
        }
        
        
        VBox rightSide = new VBox(20);
        rightSide.setAlignment(Pos.CENTER_RIGHT);
        rightSide.setPadding(new Insets(0, 0, 0, 20));
        
        Label outputLabel = new Label("OUTPUT NODES");
        outputLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        rightSide.getChildren().add(outputLabel);
        
        for (int i = 0; i < wireCount; i++) {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(rightColors[i]));
            circle.setStroke(Color.web("#00F6FF"));
            circle.setStrokeWidth(2);
            
            
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(rightColors[i]));
            glow.setRadius(10);
            glow.setSpread(0.3);
            circle.setEffect(glow);
            
            rightConnectors[i] = circle;
            
            
            circle.setOnMouseEntered(e -> {
                circle.setStroke(Color.web("#E4FBFF"));
                circle.setStrokeWidth(3);
            });
            circle.setOnMouseExited(e -> {
                circle.setStroke(Color.web("#00F6FF"));
                circle.setStrokeWidth(2);
            });
            
            rightSide.getChildren().add(circle);
        }
        
        
        HBox wiresLayout = new HBox();
        wiresLayout.setAlignment(Pos.CENTER);
        wiresLayout.getChildren().addAll(leftSide, wirePane, rightSide);
        wiresContainer.getChildren().add(wiresLayout);
        
        
        Label instructionsLabel = new Label("Connect each input node to its matching output node");
        instructionsLabel.setStyle("-fx-text-fill: #E4FBFF; -fx-font-size: 14px; -fx-font-family: 'Monospace';");
        
        
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET CONNECTIONS", false);
        resetButton.setOnAction(e -> {
            for (Line line : connectionLines) {
                line.setStroke(Color.TRANSPARENT);
            }
        });
        
        VBox controlsBox = new VBox(15);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(15, 0, 0, 0));
        controlsBox.getChildren().addAll(instructionsLabel, resetButton);
        
        
        taskContent.getChildren().addAll(wiresContainer, controlsBox);
        
        
        taskContent.setScaleX(0.8);
        taskContent.setScaleY(0.8);
        gamePane.getChildren().add(taskContent);
    }
} 
