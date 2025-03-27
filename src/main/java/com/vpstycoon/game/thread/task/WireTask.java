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
    
    private Circle[] leftConnectors;
    private Circle[] rightConnectors;
    private Line[] connectionLines;
    
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
        lines[wireIndex].setStartX(connectors[wireIndex].getLayoutX() - 40);
        lines[wireIndex].setStartY(connectors[wireIndex].getLayoutY());
        lines[wireIndex].setEndX(connectors[wireIndex].getLayoutX() + 15);
        lines[wireIndex].setEndY(connectors[wireIndex].getLayoutY());
    }
    
    /**
     * Update a wire connection as it's being dragged
     * 
     * @param e Mouse event
     * @param wireIndex Index of the wire
     * @param lines Array of connection lines
     */
    private void updateConnection(javafx.scene.input.MouseEvent e, int wireIndex, Line[] lines) {
        // ใช้ scene coordinates เพื่อให้ตำแหน่งตรงกับเมาส์จริงๆ
        javafx.geometry.Point2D point = lines[wireIndex].getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
        lines[wireIndex].setEndX(point.getX());
        lines[wireIndex].setEndY(point.getY());
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
        // แปลง scene coordinates เป็น local coordinates ของ parent
        javafx.geometry.Point2D point = lines[wireIndex].getParent().sceneToLocal(e.getSceneX(), e.getSceneY());
        double mouseX = point.getX();
        double mouseY = point.getY();
        
        // Check if the line ends near a right connector
        for (int i = 0; i < rightConnectors.length; i++) {
            Circle target = rightConnectors[i];
            
            // แปลง target coordinates ให้อยู่ใน coordinate space เดียวกันกับ mouse
            javafx.geometry.Point2D targetPoint = lines[wireIndex].getParent().sceneToLocal(
                target.localToScene(target.getBoundsInLocal()).getCenterX(),
                target.localToScene(target.getBoundsInLocal()).getCenterY()
            );
            double targetX = targetPoint.getX();
            double targetY = targetPoint.getY();
            
            // เพิ่มระยะการตรวจจับให้มากขึ้น
            if (Math.abs(mouseX - targetX) < 30 && Math.abs(mouseY - targetY) < 30) {
                // Snap the line to the target
                lines[wireIndex].setEndX(targetX);
                lines[wireIndex].setEndY(targetY);
                
                // เปลี่ยนสีของเส้นให้ตรงกับข้อมูลจริง (เส้นที่ต่อจาก left connector)
                lines[wireIndex].setStroke(leftConnectors[wireIndex].getFill());
                
                // Debug: แสดงข้อมูลการเชื่อมต่อ
                log("Connected wire " + wireIndex + " to connector " + i);
                
                // ทดสอบว่าสีตรงกันหรือไม่
                String leftColor = ((javafx.scene.paint.Color)leftConnectors[wireIndex].getFill()).toString();
                String rightColor = ((javafx.scene.paint.Color)rightConnectors[i].getFill()).toString();
                log("Left color: " + leftColor + ", Right color: " + rightColor + ", Match: " + leftColor.equals(rightColor));
                
                // Check if this completes all connections
                boolean allConnected = true;
                boolean allCorrectColors = true;
                
                // สร้าง mapping ของการเชื่อมต่อปัจจุบัน
                int[] currentConnections = new int[lines.length];
                java.util.Arrays.fill(currentConnections, -1); // -1 = ยังไม่เชื่อมต่อ
                
                // ตรวจสอบการเชื่อมต่อทั้งหมด
                for (int j = 0; j < lines.length; j++) {
                    boolean foundConnection = false;
                    
                    for (int k = 0; k < rightConnectors.length; k++) {
                        // ควรใช้ parent ของ line ที่เรากำลังตรวจสอบ ไม่ใช่ของ line ปัจจุบัน
                        javafx.geometry.Point2D connPoint = lines[j].getParent().sceneToLocal(
                            rightConnectors[k].localToScene(rightConnectors[k].getBoundsInLocal()).getCenterX(),
                            rightConnectors[k].localToScene(rightConnectors[k].getBoundsInLocal()).getCenterY()
                        );
                        
                        // ตรวจสอบว่าเส้นเชื่อมต่อกับข้อต่อใด
                        if (lines[j].getStroke() != Color.TRANSPARENT && 
                            Math.abs(lines[j].getEndX() - connPoint.getX()) < 10 && 
                            Math.abs(lines[j].getEndY() - connPoint.getY()) < 10) {
                            
                            currentConnections[j] = k;
                            foundConnection = true;
                            
                            // เช็คว่าสีตรงกันหรือไม่ - โดยดูจากสีของ connector ไม่ใช่ตำแหน่ง
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
                
                // จำนวนสายที่เชื่อมต่อ (เฉพาะที่มีการเชื่อมต่อแล้ว)
                int connectedCount = 0;
                for (int j = 0; j < currentConnections.length; j++) {
                    if (currentConnections[j] != -1) {
                        connectedCount++;
                    }
                }
                
                log("Connected wires: " + connectedCount + "/" + wireCount + 
                    " (allConnected=" + allConnected + 
                    ", allCorrectColors=" + allCorrectColors + ")");
                
                // If all wires are connected correctly, complete the task
                if (connectedCount == wireCount && allCorrectColors) {
                    log("All " + wireCount + " wires connected correctly!");
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
        // Create the task UI using CyberpunkEffects
        VBox taskContent = new VBox(15);
        taskContent.setAlignment(Pos.CENTER);
        taskContent.setPadding(new Insets(20));
        taskContent.setMaxWidth(600);
        taskContent.setMaxHeight(500);
        taskContent.setStyle("-fx-background-color: rgba(42, 27, 61, 0.7); -fx-background-radius: 5px; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(120, 0, 255, 0.2), 10, 0, 0, 3);");
        
        // Task title already in the header, no need to add again
        
        // Create a container for the wires with cyberpunk styling
        VBox wiresContainer = CyberpunkEffects.createCyberSection("CONNECTION MATRIX");
        wiresContainer.setAlignment(Pos.CENTER);
        wiresContainer.setPadding(new Insets(20));
        
        // Create wires with random colors and positions
        String[] leftColors = getRandomColors(wireCount);
        String[] rightColors = leftColors.clone();
        shuffleArray(rightColors);
        
        // Store the connections
        leftConnectors = new Circle[wireCount];
        rightConnectors = new Circle[wireCount];
        connectionLines = new Line[wireCount];
        
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
        
        // Create left side connectors with cyberpunk styling
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
            
            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(leftColors[i]));
            glow.setRadius(10);
            glow.setSpread(0.3);
            circle.setEffect(glow);
            
            leftConnectors[i] = circle;
            
            final int wireIndex = i;
            
            // Add hover effect
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
        
        // Create right side connectors with cyberpunk styling
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
            
            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(rightColors[i]));
            glow.setRadius(10);
            glow.setSpread(0.3);
            circle.setEffect(glow);
            
            rightConnectors[i] = circle;
            
            // Add hover effect
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
        
        // Assemble the wire layout
        HBox wiresLayout = new HBox();
        wiresLayout.setAlignment(Pos.CENTER);
        wiresLayout.getChildren().addAll(leftSide, wirePane, rightSide);
        wiresContainer.getChildren().add(wiresLayout);
        
        // Add instructions
        Label instructionsLabel = new Label("Connect each input node to its matching output node");
        instructionsLabel.setStyle("-fx-text-fill: #E4FBFF; -fx-font-size: 14px; -fx-font-family: 'Monospace';");
        
        // Add a reset button with cyberpunk styling
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
        
        // Add the task components to the content
        taskContent.getChildren().addAll(wiresContainer, controlsBox);
        
        // Add to the game pane
        taskContent.setScaleX(0.8);
        taskContent.setScaleY(0.8);
        gamePane.getChildren().add(taskContent);
    }
} 