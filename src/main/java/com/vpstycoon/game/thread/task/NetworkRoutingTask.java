package com.vpstycoon.game.thread.task;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;

import javafx.scene.Cursor;
import javafx.scene.Group;

/**
 * Network Routing Task
 * Player must establish optimal network routes by connecting nodes
 */
public class NetworkRoutingTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(NetworkRoutingTask.class.getName());
    private static final Random random = new Random();
    
    private static final int NUM_NODES = 7;
    private static final int NUM_REQUIRED_CONNECTIONS = 5;
    
    private final List<NetworkNode> nodes = new ArrayList<>();
    private final List<NetworkConnection> connections = new ArrayList<>();
    private final List<NetworkConnection> optimalConnections = new ArrayList<>();
    
    private Pane networkPane;
    private Label statusLabel;
    private Button routeButton;
    private NetworkNode sourceNode;
    private NetworkNode destinationNode;
    private boolean pathCompleted = false;
    private BorderPane taskPane;

    public NetworkRoutingTask() {
        super(
                "Network Optimization",
                "Establish optimal network routes between critical servers",
                "/images/task/network_task.png",
                6000, // reward
                20,  // penalty (0.2 * 100)
                3,   // difficulty
                45   // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        try {
            // Create main task container
            taskPane = new BorderPane();
            taskPane.setStyle("-fx-background-color: linear-gradient(to bottom, #0A0A2A, #1A1A4A); -fx-border-color: #00FFFF; -fx-border-width: 2px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            taskPane.setPrefSize(650, 500);
            taskPane.setPadding(new Insets(20));
            
            // Create title area
            VBox headerBox = new VBox(10);
            headerBox.setAlignment(Pos.CENTER);
            
            Text titleText = new Text("NETWORK PATH OPTIMIZATION");
            titleText.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));
            titleText.setFill(Color.web("#00FFFF"));
            
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#00FFFF"));
            glow.setRadius(10);
            glow.setSpread(0.2);
            titleText.setEffect(glow);
            
            Text descText = new Text("Establish optimal network routes between critical nodes");
            descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
            descText.setFill(Color.web("#CCECFF"));
            
            headerBox.getChildren().addAll(titleText, descText);
            taskPane.setTop(headerBox);
            
            // Create network visualization pane
            networkPane = new Pane();
            networkPane.setPrefSize(600, 180);
            networkPane.setMaxSize(600, 180);
            networkPane.setPadding(new Insets(20));
            networkPane.setStyle("-fx-background-color: rgba(10, 15, 30, 0.7); -fx-border-color: #303060; -fx-border-width: 1px;");
            
            addGridLines(networkPane, 30, 30);
            
            // Generate network
            generateNetwork();
            
            // Create bottom control area
            VBox controlBox = new VBox(15);
            controlBox.setAlignment(Pos.CENTER);
            controlBox.setPadding(new Insets(20));
            
            // Status label
            statusLabel = new Label("ESTABLISH OPTIMAL NETWORK PATH");
            statusLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 16));
            statusLabel.setTextFill(Color.web("#00FFFF"));
            
            // Control buttons
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            
            Button resetButton = createCustomButton("RESET CONNECTIONS", false);
            resetButton.setOnAction(e -> resetConnections());
            
            routeButton = createCustomButton("ESTABLISH ROUTE", true);
            routeButton.setDisable(true);
            routeButton.setOnAction(e -> establishRoute());
            
            buttonBox.getChildren().addAll(resetButton, routeButton);
            
            controlBox.getChildren().addAll(statusLabel, buttonBox);
            
            // Add components to main task pane
            taskPane.setCenter(networkPane);
            taskPane.setBottom(controlBox);
            BorderPane.setMargin(controlBox, new Insets(20, 0, 20, 0));
            
            // Add the task pane to the game pane
            gamePane.getChildren().add(taskPane);
            
            // Show path requirement with flashing visualization
            highlightRequiredPath();
            
            log("NetworkRoutingTask initialized successfully");
        } catch (Exception e) {
            log("Error initializing NetworkRoutingTask: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Add grid lines to pane for cyberpunk look
     */
    private void addGridLines(Pane pane, int hSpacing, int vSpacing) {
        // Horizontal lines
        for (int y = vSpacing; y < pane.getPrefHeight(); y += vSpacing) {
            Line line = new Line(0, y, pane.getPrefWidth(), y);
            line.setStroke(Color.web("#00FFFF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
        
        // Vertical lines
        for (int x = hSpacing; x < pane.getPrefWidth(); x += hSpacing) {
            Line line = new Line(x, 0, x, pane.getPrefHeight());
            line.setStroke(Color.web("#00FFFF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
    }
    
    /**
     * Create a custom button with cyberpunk styling
     */
    private Button createCustomButton(String text, boolean primary) {
        Button button = new Button(text);
        button.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 14));
        
        String baseColor, hoverColor, textColor;
        if (primary) {
            baseColor = "#00FFFF";
            hoverColor = "#80FFFF";
            textColor = "#000000";
        } else {
            baseColor = "#FF0066";
            hoverColor = "#FF4D94";
            textColor = "#FFFFFF";
        }
        
        String baseStyle = 
            "-fx-background-color: " + baseColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-border-color: " + hoverColor + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;";
        
        String hoverStyle = 
            "-fx-background-color: " + hoverColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-border-color: " + baseColor + ";" +
            "-fx-border-width: 2px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + baseColor + ", 10, 0.5, 0, 0);";
        
        button.setStyle(baseStyle);
        
        // Add hover effect
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        
        return button;
    }
    
    /**
     * Generate a network of nodes and potential connections
     */
    private void generateNetwork() {
        try {
            log("Generating network nodes and connections...");
            if (networkPane == null) {
                log("ERROR: networkPane is null");
                return;
            }
            
            nodes.clear();
            connections.clear();
            optimalConnections.clear();
            
            // Get actual dimensions of the networkPane
            double paneWidth = networkPane.getPrefWidth();
            double paneHeight = networkPane.getPrefHeight();
            
            // Ensure we have valid dimensions
            if (paneWidth <= 0) paneWidth = 600;
            if (paneHeight <= 0) paneHeight = 300;
            
            // Define margins to keep nodes away from edges
            double margin = 30;
            double usableWidth = paneWidth - (2 * margin);
            double usableHeight = paneHeight - (2 * margin);
            
            log("Network pane dimensions: " + paneWidth + "x" + paneHeight);
            
            // Create nodes with positions constrained to the network pane
            for (int i = 0; i < NUM_NODES; i++) {
                // Position nodes in a more organized way within boundaries
                double x = margin + (random.nextDouble() * usableWidth);
                double y = margin + (random.nextDouble() * usableHeight);
                
                // Ensure positions are within bounds
                x = Math.min(paneWidth - margin, Math.max(margin, x));
                y = Math.min(paneHeight - margin, Math.max(margin, y));
                
                NetworkNode node = new NetworkNode(i, x, y);
                nodes.add(node);
                networkPane.getChildren().add(node.getVisual());
            }
            
            log("Created " + nodes.size() + " network nodes within bounds");
            
            // Set source and destination nodes (first and last)
            sourceNode = nodes.get(0);
            sourceNode.setType(NodeType.SOURCE);
            
            destinationNode = nodes.get(NUM_NODES - 1);
            destinationNode.setType(NodeType.DESTINATION);
            
            log("Set source node: " + sourceNode.getId() + ", destination node: " + destinationNode.getId());
            
            // Calculate maximum allowed distance based on pane size
            double maxDistance = Math.min(paneWidth, paneHeight) * 0.8;
            
            // Create potential connections between nodes
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    // Only create connections with 70% probability
                    if (random.nextDouble() < 0.7) {
                        NetworkNode nodeA = nodes.get(i);
                        NetworkNode nodeB = nodes.get(j);
                        
                        // Calculate distance
                        double distance = calculateDistance(nodeA, nodeB);
                        
                        // Skip if too far away - scale based on pane size
                        if (distance > maxDistance) continue;
                        
                        // Create connection
                        NetworkConnection connection = new NetworkConnection(nodeA, nodeB, distance);
                        connections.add(connection);
                        
                        // Add connection visual to the network pane (below nodes)
                        networkPane.getChildren().add(0, connection.getVisual());
                        
                        // Ensure weight text is above the line but below nodes
                        networkPane.getChildren().add(Math.min(1, networkPane.getChildren().size()), connection.getWeightGroup());
                    }
                }
            }
            
            log("Created " + connections.size() + " network connections");
            
            // Make sure there is at least one path from source to destination
            ensurePathExists();
            
            // Generate optimal path (minimize total distance)
            findOptimalPath();
        } catch (Exception e) {
            log("Error in generateNetwork: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ensure there is at least one path from source to destination
     */
    private void ensurePathExists() {
        // Check if a path exists
        Set<NetworkNode> visited = new HashSet<>();
        Queue<NetworkNode> queue = new LinkedList<>();
        
        // Start BFS from source
        queue.add(sourceNode);
        visited.add(sourceNode);
        
        while (!queue.isEmpty()) {
            NetworkNode current = queue.poll();
            
            // If we reached destination, path exists
            if (current == destinationNode) {
                return; // Path found, no need to continue
            }
            
            // Check all connections from current node
            for (NetworkConnection conn : connections) {
                NetworkNode neighbor = null;
                
                if (conn.getNodeA() == current) {
                    neighbor = conn.getNodeB();
                } else if (conn.getNodeB() == current) {
                    neighbor = conn.getNodeA();
                } else {
                    continue; // Connection not connected to current node
                }
                
                // If neighbor not visited, add to queue
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        // If we get here, no path exists - create a direct connection
        log("No path exists from source to destination, creating direct connection");
        double distance = calculateDistance(sourceNode, destinationNode);
        NetworkConnection directConn = new NetworkConnection(sourceNode, destinationNode, distance);
        connections.add(directConn);
        
        // Add connection visual
        networkPane.getChildren().add(0, directConn.getVisual());
        networkPane.getChildren().add(Math.min(1, networkPane.getChildren().size()), directConn.getWeightGroup());
    }
    
    /**
     * Calculate distance between two nodes
     */
    private double calculateDistance(NetworkNode nodeA, NetworkNode nodeB) {
        double dx = nodeA.getX() - nodeB.getX();
        double dy = nodeA.getY() - nodeB.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Find optimal path using a simplified algorithm
     * In a real implementation, this would use Dijkstra's or A* algorithm
     */
    private void findOptimalPath() {
        // For the network optimization task, we need to find the path with minimal total distance
        Map<NetworkNode, Double> distances = new HashMap<>();
        Map<NetworkNode, NetworkConnection> previousConnections = new HashMap<>();
        PriorityQueue<NetworkNode> queue = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<NetworkNode> visited = new HashSet<>();
        
        // Initialize distances
        for (NetworkNode node : nodes) {
            distances.put(node, node == sourceNode ? 0 : Double.MAX_VALUE);
        }
        
        // Start from source node
        queue.add(sourceNode);
        
        while (!queue.isEmpty()) {
            NetworkNode current = queue.poll();
            
            // If we reached destination, we're done
            if (current == destinationNode) {
                break;
            }
            
            // Skip if already processed
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            // Check all connections from current node
            for (NetworkConnection conn : connections) {
                NetworkNode neighbor = null;
                
                if (conn.getNodeA() == current) {
                    neighbor = conn.getNodeB();
                } else if (conn.getNodeB() == current) {
                    neighbor = conn.getNodeA();
                } else {
                    continue; // Connection not connected to current node
                }
                
                // Skip already visited nodes
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                // Calculate new distance
                double newDistance = distances.get(current) + conn.getDistance();
                
                // If new distance is better, update
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousConnections.put(neighbor, conn);
                    
                    // Re-add to queue with new priority
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        // Reconstruct optimal path
        optimalConnections.clear();
        NetworkNode current = destinationNode;
        
        while (current != sourceNode) {
            NetworkConnection conn = previousConnections.get(current);
            if (conn == null) {
                // No path found
                LOGGER.warning("No path found to destination node");
                return;
            }
            
            optimalConnections.add(0, conn); // Add to front to preserve order
            
            // Move to previous node
            if (conn.getNodeA() == current) {
                current = conn.getNodeB();
            } else {
                current = conn.getNodeA();
            }
        }
        
        LOGGER.info("Optimal path found with " + optimalConnections.size() + 
                   " connections and total distance " + distances.get(destinationNode));
    }
    
    /**
     * Highlight required path between source and destination
     */
    private void highlightRequiredPath() {
        // Flash source and destination
        Timeline flashSource = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(sourceNode.getCircle().fillProperty(), Color.web("#00FF00", 0.7))),
            new KeyFrame(Duration.millis(500), new KeyValue(sourceNode.getCircle().fillProperty(), Color.web("#39FF14", 1.0))),
            new KeyFrame(Duration.millis(1000), new KeyValue(sourceNode.getCircle().fillProperty(), Color.web("#00FF00", 0.7)))
        );
        flashSource.setCycleCount(3);
        
        Timeline flashDest = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(destinationNode.getCircle().fillProperty(), Color.web("#FF00A0", 0.7))),
            new KeyFrame(Duration.millis(500), new KeyValue(destinationNode.getCircle().fillProperty(), Color.web("#FF00FF", 1.0))),
            new KeyFrame(Duration.millis(1000), new KeyValue(destinationNode.getCircle().fillProperty(), Color.web("#FF00A0", 0.7)))
        );
        flashDest.setCycleCount(3);
        
        // Create dashed line between source and destination
        double startX = sourceNode.getX();
        double startY = sourceNode.getY();
        double endX = destinationNode.getX();
        double endY = destinationNode.getY();
        
        Line pathLine = new Line(startX, startY, endX, endY);
        pathLine.setStroke(Color.web("#00FFFF", 0.5));
        pathLine.setStrokeWidth(2);
        pathLine.getStrokeDashArray().addAll(10.0, 10.0);
        pathLine.setOpacity(0);
        
        networkPane.getChildren().add(pathLine);
        
        // Fade in path line
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), pathLine);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Animate dash pattern
        Timeline dashAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                pathLine.getStrokeDashArray().addAll(10.0, 10.0);
                pathLine.setStrokeDashOffset(0);
            }),
            new KeyFrame(Duration.seconds(2), new KeyValue(pathLine.strokeDashOffsetProperty(), 40))
        );
        dashAnimation.setCycleCount(Timeline.INDEFINITE);
        
        // Play animations in sequence
        ParallelTransition animations = new ParallelTransition(flashSource, flashDest, fadeIn, dashAnimation);
        animations.play();
        
        // Run task instructions
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            statusLabel.setText("CONNECT NODES TO ESTABLISH OPTIMAL PATH");
            
            // Fade out the guide line after a delay
            PauseTransition fadeDelay = new PauseTransition(Duration.seconds(5));
            fadeDelay.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), pathLine);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.play();
            });
            fadeDelay.play();
        });
        delay.play();
    }
    
    /**
     * Reset all connections
     */
    private void resetConnections() {
        // Reset all connections to inactive
        for (NetworkConnection conn : connections) {
            conn.setActive(false);
        }
        
        // Update UI
        pathCompleted = false;
        routeButton.setDisable(true);
        statusLabel.setText("CONNECTIONS RESET");
        
        // Reset node status
        for (NetworkNode node : nodes) {
            if (node != sourceNode && node != destinationNode) {
                node.setType(NodeType.NORMAL);
            }
        }
    }
    
    /**
     * Establish route with current connections
     */
    private void establishRoute() {
        List<NetworkConnection> path = findActiveConnectionPath();
        
        if (path.isEmpty() || !checkPathCompletion()) {
            statusLabel.setText("Connection failed: No path to destination");
            return;
        }
        
        // Calculate total distance of user's path
        double userPathDistance = path.stream()
            .mapToDouble(NetworkConnection::getDistance)
            .sum();
        
        // Calculate optimal path distance if it's not already calculated
        if (optimalConnections.isEmpty()) {
            findOptimalPath();
        }
        
        // Calculate optimal path distance
        double optimalDistance = optimalConnections.stream()
            .mapToDouble(NetworkConnection::getDistance)
            .sum();
        
        // Check if path is optimal (within 20% of optimal distance)
        boolean isOptimal = userPathDistance <= optimalDistance * 1.2;
        
        if (isOptimal) {
            statusLabel.setText("Optimal network path established! Distance: " + String.format("%.0f", userPathDistance));
            
            // Add glow effect to taskPane
            DropShadow glow = new DropShadow();
            glow.setColor(Color.CYAN);
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(10);
            taskPane.setEffect(glow);
            
            // Flash the node with a success color - replacing CyberpunkEffects.styleCompletionEffect(taskPane)
            Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(taskPane.opacityProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(100),
                    new KeyValue(taskPane.opacityProperty(), 0.7)
                ),
                new KeyFrame(Duration.millis(200),
                    new KeyValue(taskPane.opacityProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(300),
                    new KeyValue(taskPane.opacityProperty(), 0.7)
                ),
                new KeyFrame(Duration.millis(400),
                    new KeyValue(taskPane.opacityProperty(), 1.0)
                )
            );
            
            flash.setCycleCount(3);
            flash.setOnFinished(e -> {
                // Start data animation when flashing completes
                animateDataTransfer(path);
            });
            flash.play();
        } else {
            // Suboptimal path - too many connections or too long distance
            statusLabel.setText("Suboptimal route - Distance: " + String.format("%.0f", userPathDistance) + 
                              " (Optimal: " + String.format("%.0f", optimalDistance) + ")");
            statusLabel.setTextFill(Color.ORANGE);
            
            // Show optimal connections
            highlightOptimalConnections();
        }
    }
    
    /**
     * Highlight optimal connections
     */
    private void highlightOptimalConnections() {
        // First, reset all connections to inactive
        resetConnections();
        
        // If optimalConnections is empty, try to find a path with minimal connections
        if (optimalConnections.isEmpty()) {
            findMinimalPath();
        }
        
        // If still empty, show error message
        if (optimalConnections.isEmpty()) {
            statusLabel.setText("Could not determine optimal path");
            return;
        }
        
        // Calculate total distance of optimal path
        double totalDistance = optimalConnections.stream()
            .mapToDouble(NetworkConnection::getDistance)
            .sum();
        
        // Then highlight optimal connections
        Timeline highlightTimeline = new Timeline();
        
        for (int i = 0; i < optimalConnections.size(); i++) {
            NetworkConnection conn = optimalConnections.get(i);
            final int index = i;
            
            KeyFrame kf = new KeyFrame(Duration.seconds(0.5 * i), e -> {
                conn.setActive(true);
                statusLabel.setText("Optimal path: Connection " + (index + 1) + 
                                   " (Total: " + String.format("%.0f", totalDistance) + ")");
            });
            
            highlightTimeline.getKeyFrames().add(kf);
        }
        
        // After showing optimal path, reset again
        KeyFrame resetFrame = new KeyFrame(Duration.seconds(0.5 * optimalConnections.size() + 2), e -> {
            resetConnections();
            statusLabel.setText("Try again with a more optimal route");
            statusLabel.setTextFill(Color.web("#00FFFF"));
        });
        
        highlightTimeline.getKeyFrames().add(resetFrame);
        highlightTimeline.play();
    }
    
    /**
     * Find minimal path from source to destination
     */
    private void findMinimalPath() {
        // Use Dijkstra's algorithm to find the shortest path
        Map<NetworkNode, Double> distances = new HashMap<>();
        Map<NetworkNode, NetworkConnection> previousConnections = new HashMap<>();
        PriorityQueue<NetworkNode> queue = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<NetworkNode> visited = new HashSet<>();
        
        // Initialize distances
        for (NetworkNode node : nodes) {
            distances.put(node, node == sourceNode ? 0 : Double.MAX_VALUE);
        }
        
        // Start from source node
        queue.add(sourceNode);
        
        while (!queue.isEmpty()) {
            NetworkNode current = queue.poll();
            
            // If we reached destination, we're done
            if (current == destinationNode) {
                break;
            }
            
            // Skip if already processed
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            // Check all connections from current node
            for (NetworkConnection conn : connections) {
                NetworkNode neighbor = null;
                
                if (conn.getNodeA() == current) {
                    neighbor = conn.getNodeB();
                } else if (conn.getNodeB() == current) {
                    neighbor = conn.getNodeA();
                } else {
                    continue; // Connection not connected to current node
                }
                
                // Skip already visited nodes
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                // Calculate new distance
                double newDistance = distances.get(current) + conn.getDistance();
                
                // If new distance is better, update
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousConnections.put(neighbor, conn);
                    
                    // Re-add to queue with new priority
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        // Reconstruct optimal path
        optimalConnections.clear();
        NetworkNode current = destinationNode;
        
        if (previousConnections.containsKey(destinationNode)) {
            while (current != sourceNode) {
                NetworkConnection conn = previousConnections.get(current);
                optimalConnections.add(0, conn); // Add to front to preserve order
                
                // Move to previous node
                if (conn.getNodeA() == current) {
                    current = conn.getNodeB();
                } else {
                    current = conn.getNodeA();
                }
            }
        }
    }
    
    /**
     * Animate data transfer along the established path
     */
    private void animateDataTransfer(List<NetworkConnection> path) {
        // Create data packet visual
        Circle dataPacket = new Circle(10);
        dataPacket.setFill(Color.web("#00FF00"));
        
        Bloom glow = new Bloom();
        glow.setThreshold(0.3);
        dataPacket.setEffect(glow);
        
        networkPane.getChildren().add(dataPacket);
        
        // Create animation sequence for data packet
        Timeline dataAnimation = new Timeline();
        double totalDelay = 0;
        
        for (NetworkConnection conn : path) {
            NetworkNode start = conn.getNodeA();
            NetworkNode end = conn.getNodeB();
            
            // Ensure direction is from source towards destination
            if (!isNodeInPath(start)) {
                NetworkNode temp = start;
                start = end;
                end = temp;
            }
            
            final double startX = start.getX();
            final double startY = start.getY();
            final double endX = end.getX();
            final double endY = end.getY();
            
            // Calculate duration based on distance
            double distance = calculateDistance(start, end);
            double duration = distance / 200; // Speed factor
            
            // Create keyframes for this segment
            KeyFrame startFrame = new KeyFrame(Duration.seconds(totalDelay),
                    new KeyValue(dataPacket.centerXProperty(), startX),
                    new KeyValue(dataPacket.centerYProperty(), startY));
            
            KeyFrame endFrame = new KeyFrame(Duration.seconds(totalDelay + duration),
                    new KeyValue(dataPacket.centerXProperty(), endX),
                    new KeyValue(dataPacket.centerYProperty(), endY));
            
            dataAnimation.getKeyFrames().addAll(startFrame, endFrame);
            
            totalDelay += duration;
        }
        
        // When animation completes, show success
        dataAnimation.setOnFinished(e -> {
            // Fade out data packet
            FadeTransition fade = new FadeTransition(Duration.seconds(1), dataPacket);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(event -> {
                PauseTransition delay = new PauseTransition(Duration.seconds(1));
                delay.setOnFinished(evt -> completeTask());
                delay.play();
            });
            fade.play();
        });
        
        dataAnimation.play();
    }
    
    /**
     * Find active connections in path order
     */
    private List<NetworkConnection> findActiveConnectionPath() {
        List<NetworkConnection> path = new ArrayList<>();
        List<NetworkNode> visited = new ArrayList<>();
        
        // Start with source node
        visited.add(sourceNode);
        NetworkNode current = sourceNode;
        
        // Find path until we reach destination or can't proceed
        while (current != destinationNode) {
            boolean found = false;
            
            // Find an active connection from current node
            for (NetworkConnection conn : connections) {
                if (!conn.isActive()) continue;
                
                NetworkNode other = null;
                if (conn.getNodeA() == current && !visited.contains(conn.getNodeB())) {
                    other = conn.getNodeB();
                } else if (conn.getNodeB() == current && !visited.contains(conn.getNodeA())) {
                    other = conn.getNodeA();
                }
                
                if (other != null) {
                    // Found next node in path
                    path.add(conn);
                    visited.add(other);
                    current = other;
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                // No path found
                break;
            }
        }
        
        return path;
    }
    
    /**
     * Check if node is in the currently established path
     */
    private boolean isNodeInPath(NetworkNode node) {
        // First, get all nodes in active connections
        List<NetworkNode> pathNodes = new ArrayList<>();
        pathNodes.add(sourceNode);
        
        boolean changed;
        do {
            changed = false;
            for (NetworkConnection conn : connections) {
                if (!conn.isActive()) continue;
                
                NetworkNode nodeA = conn.getNodeA();
                NetworkNode nodeB = conn.getNodeB();
                
                if (pathNodes.contains(nodeA) && !pathNodes.contains(nodeB)) {
                    pathNodes.add(nodeB);
                    changed = true;
                } else if (pathNodes.contains(nodeB) && !pathNodes.contains(nodeA)) {
                    pathNodes.add(nodeA);
                    changed = true;
                }
            }
        } while (changed);
        
        return pathNodes.contains(node);
    }
    
    /**
     * Check if current connections form a complete path
     */
    private boolean checkPathCompletion() {
        // Track visited nodes
        List<NetworkNode> visited = new ArrayList<>();
        visited.add(sourceNode);
        
        boolean changed;
        do {
            changed = false;
            for (NetworkConnection conn : connections) {
                if (!conn.isActive()) continue;
                
                NetworkNode nodeA = conn.getNodeA();
                NetworkNode nodeB = conn.getNodeB();
                
                if (visited.contains(nodeA) && !visited.contains(nodeB)) {
                    visited.add(nodeB);
                    changed = true;
                } else if (visited.contains(nodeB) && !visited.contains(nodeA)) {
                    visited.add(nodeA);
                    changed = true;
                }
            }
        } while (changed);
        
        return visited.contains(destinationNode);
    }
    
    /**
     * Node type enum
     */
    private enum NodeType {
        NORMAL,
        SOURCE,
        DESTINATION,
        ACTIVE
    }
    
    /**
     * Network node class
     */
    private class NetworkNode {
        private final int id;
        private final double x;
        private final double y;
        private NodeType type;
        private final StackPane visual;
        private final Circle circle;
        private final Label label;
        
        public NetworkNode(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.type = NodeType.NORMAL;
            
            // Create visual representation
            circle = new Circle(15);
            updateNodeAppearance();
            
            label = new Label(String.valueOf(id));
            label.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 12));
            label.setTextFill(Color.WHITE);
            
            visual = new StackPane(circle, label);
            visual.setLayoutX(x - 15);
            visual.setLayoutY(y - 15);
            
            // Make node interactive
            setupInteraction();
        }
        
        public int getId() {
            return id;
        }
        
        public double getX() {
            return x;
        }
        
        public double getY() {
            return y;
        }
        
        public Circle getCircle() {
            return circle;
        }
        
        public NodeType getType() {
            return type;
        }
        
        public StackPane getVisual() {
            return visual;
        }
        
        public void setType(NodeType type) {
            this.type = type;
            updateNodeAppearance();
        }
        
        private void updateNodeAppearance() {
            switch (type) {
                case NORMAL:
                    circle.setFill(Color.web("#3A4A5A"));
                    circle.setStroke(Color.web("#5A6A7A"));
                    break;
                case SOURCE:
                    circle.setFill(Color.web("#00FF00"));
                    circle.setStroke(Color.web("#39FF14"));
                    break;
                case DESTINATION:
                    circle.setFill(Color.web("#FF00A0"));
                    circle.setStroke(Color.web("#FF00FF"));
                    break;
                case ACTIVE:
                    circle.setFill(Color.web("#00FFFF"));
                    circle.setStroke(Color.web("#80FFFF"));
                    break;
            }
            
            circle.setStrokeWidth(2);
        }
        
        /**
         * Setup node interaction handlers
         */
        private void setupInteraction() {
            // Make draggable
            AtomicReference<Double> dragDeltaX = new AtomicReference<>((double) 0);
            AtomicReference<Double> dragDeltaY = new AtomicReference<>((double) 0);
            
            visual.setOnMouseEntered(e -> {
                if (type != NodeType.SOURCE && type != NodeType.DESTINATION) {
                    circle.setStroke(Color.YELLOW);
                    circle.setStrokeWidth(2);
                }
                visual.setCursor(Cursor.HAND);
            });
            
            visual.setOnMouseExited(e -> {
                if (type != NodeType.SOURCE && type != NodeType.DESTINATION) {
                    circle.setStroke(Color.WHITE);
                    circle.setStrokeWidth(1);
                }
                visual.setCursor(Cursor.DEFAULT);
            });
            
            visual.setOnMousePressed(e -> {
                if (e.isSecondaryButtonDown()) {
                    // Toggle node active state on right click
                    toggleNodeState();
                } else {
                    // Prepare for drag on left click
                    dragDeltaX.set(visual.getLayoutX() - e.getSceneX());
                    dragDeltaY.set(visual.getLayoutY() - e.getSceneY());
                    visual.setCursor(Cursor.MOVE);
                }
                
                e.consume();
            });
            
            visual.setOnMouseDragged(e -> {
                if (!e.isSecondaryButtonDown()) {
                    double newX = e.getSceneX() + dragDeltaX.get();
                    double newY = e.getSceneY() + dragDeltaY.get();
                    
                    // Keep within bounds
                    newX = Math.max(0, Math.min(networkPane.getWidth() - 30, newX));
                    newY = Math.max(0, Math.min(networkPane.getHeight() - 30, newY));
                    
                    visual.setLayoutX(newX);
                    visual.setLayoutY(newY);
                    
                    // Update connected lines
                    for (NetworkConnection conn : connections) {
                        if (conn.getNodeA() == this || conn.getNodeB() == this) {
                            conn.updatePosition();
                        }
                    }
                }
                
                e.consume();
            });
            
            visual.setOnMouseReleased(e -> {
                visual.setCursor(Cursor.HAND);
                
                if (!e.isSecondaryButtonDown()) {
                    // Check if we can now establish route
                    boolean canEstablish = checkPathCompletion();
                    routeButton.setDisable(!canEstablish);
                    
                    if (canEstablish && !pathCompleted) {
                        statusLabel.setText("Path complete - Click ESTABLISH ROUTE");
                        pathCompleted = true;
                    } else if (!canEstablish) {
                        statusLabel.setText("Continue connecting nodes");
                        pathCompleted = false;
                    }
                }
                
                e.consume();
            });
            
            visual.setOnMouseClicked(e -> {
                // Handle click to select or deselect node
                if (e.getClickCount() == 2) {
                    // Double click = select node for connection
                    selectNodeForConnection();
                } else if (e.isSecondaryButtonDown()) {
                    // Right click handled in the pressed event
                } else {
                    // Single click = select node
                    System.out.println("Node " + id + " clicked");
                }
                
                e.consume();
            });
        }
        
        /**
         * Select node for connection
         */
        public void selectNodeForConnection() {
            // Cannot select source or destination for manual connections
            if (type == NodeType.SOURCE || type == NodeType.DESTINATION) {
                return;
            }
            
            log("Node " + id + " selected for connection");
            
            // Change appearance to show selection
            if (type == NodeType.NORMAL) {
                // Mark as active (selected)
                setType(NodeType.ACTIVE);
                
                // Find another active node to connect with
                NetworkNode otherNode = null;
                for (NetworkNode node : nodes) {
                    if (node != this && node.getType() == NodeType.ACTIVE) {
                        otherNode = node;
                        break;
                    }
                }
                
                if (otherNode != null) {
                    // Create connection between the two active nodes
                    createOrToggleConnection(this, otherNode);
                    
                    // Reset node types
                    setType(NodeType.NORMAL);
                    otherNode.setType(NodeType.NORMAL);
                    
                    // Check if we have a complete path
                    boolean canEstablish = checkPathCompletion();
                    routeButton.setDisable(!canEstablish);
                    
                    if (canEstablish && !pathCompleted) {
                        statusLabel.setText("Path complete - Click ESTABLISH ROUTE");
                        pathCompleted = true;
                    }
                }
            } else if (type == NodeType.ACTIVE) {
                // Deselect
                setType(NodeType.NORMAL);
            }
        }
        
        /**
         * Toggle node active state
         */
        public void toggleNodeState() {
            // Cannot toggle source or destination
            if (type == NodeType.SOURCE || type == NodeType.DESTINATION) {
                return;
            }
            
            // Toggle between normal and active
            if (type == NodeType.NORMAL) {
                setType(NodeType.ACTIVE);
            } else {
                setType(NodeType.NORMAL);
            }
        }
    }
    
    /**
     * Network connection class
     */
    private class NetworkConnection {
        private final NetworkNode nodeA;
        private final NetworkNode nodeB;
        private final double distance;
        private boolean active;
        private final Line visual;
        private final Text weightText;
        private final Rectangle weightBackground;
        private final Group weightGroup;
        
        public NetworkConnection(NetworkNode nodeA, NetworkNode nodeB, double distance) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.distance = distance;
            this.active = false;
            
            // Create visual representation
            visual = new Line(nodeA.getX(), nodeA.getY(), nodeB.getX(), nodeB.getY());
            
            // Create weight text
            weightText = new Text(String.format("%.0f", distance));
            weightText.setFont(Font.font("Orbitron", FontWeight.BOLD, 10));
            
            // Create background for weight text
            weightBackground = new Rectangle();
            weightBackground.setArcWidth(6);
            weightBackground.setArcHeight(6);
            
            // Group text and background
            weightGroup = new Group(weightBackground, weightText);
            
            // Position the text at the middle of the line
            updateWeightPosition();
            
            updateVisualAppearance();
            
            // Make connection interactive
            setupInteraction();
        }
        
        public NetworkNode getNodeA() {
            return nodeA;
        }
        
        public NetworkNode getNodeB() {
            return nodeB;
        }
        
        public double getDistance() {
            return distance;
        }
        
        public Line getVisual() {
            return visual;
        }
        
        public Text getWeightText() {
            return weightText;
        }
        
        public Group getWeightGroup() {
            return weightGroup;
        }
        
        public boolean isActive() {
            return active;
        }
        
        public void setActive(boolean active) {
            this.active = active;
            updateVisualAppearance();
            
            // If activating, update connected nodes
            if (active) {
                if (nodeA != sourceNode && nodeA != destinationNode) {
                    nodeA.setType(NodeType.ACTIVE);
                }
                if (nodeB != sourceNode && nodeB != destinationNode) {
                    nodeB.setType(NodeType.ACTIVE);
                }
            } else {
                // If deactivating, check if nodes are still active in other connections
                if (nodeA != sourceNode && nodeA != destinationNode) {
                    boolean stillActive = false;
                    for (NetworkConnection conn : connections) {
                        if (conn != this && conn.isActive() && (conn.getNodeA() == nodeA || conn.getNodeB() == nodeA)) {
                            stillActive = true;
                            break;
                        }
                    }
                    if (!stillActive) {
                        nodeA.setType(NodeType.NORMAL);
                    }
                }
                
                if (nodeB != sourceNode && nodeB != destinationNode) {
                    boolean stillActive = false;
                    for (NetworkConnection conn : connections) {
                        if (conn != this && conn.isActive() && (conn.getNodeA() == nodeB || conn.getNodeB() == nodeB)) {
                            stillActive = true;
                            break;
                        }
                    }
                    if (!stillActive) {
                        nodeB.setType(NodeType.NORMAL);
                    }
                }
            }
            
            // Check if we have a complete path
            pathCompleted = checkPathCompletion();
            routeButton.setDisable(!pathCompleted);
            
            if (pathCompleted && !routeButton.isDisabled()) {
                statusLabel.setText("PATH ESTABLISHED - READY TO ROUTE");
                statusLabel.setTextFill(Color.web("#00FFFF"));
            }
        }
        
        private void updateVisualAppearance() {
            if (active) {
                visual.setStroke(Color.web("#00FFFF"));
                visual.setStrokeWidth(3);
                
                // Update weight text appearance for active connection
                weightText.setFill(Color.web("#00FFFF"));
                weightBackground.setFill(Color.web("#1A2A3A"));
                weightBackground.setStroke(Color.web("#00FFFF"));
                weightBackground.setStrokeWidth(1);
                
                // Add glow effect
                Bloom glow = new Bloom();
                glow.setThreshold(0.3);
                visual.setEffect(glow);
            } else {
                visual.setStroke(Color.web("#3A4A5A", 0.7));
                visual.setStrokeWidth(1.5);
                visual.setEffect(null);
                
                // Update weight text appearance for inactive connection
                weightText.setFill(Color.web("#5A6A7A", 0.9));
                weightBackground.setFill(Color.web("#222222", 0.7));
                weightBackground.setStroke(Color.web("#3A4A5A", 0.5));
                weightBackground.setStrokeWidth(0.5);
            }
        }
        
        private void setupInteraction() {
            // Hover effect
            visual.setOnMouseEntered(e -> {
                if (!active) {
                    visual.setStroke(Color.web("#5A6A7A"));
                    visual.setStrokeWidth(2);
                }
            });
            
            visual.setOnMouseExited(e -> {
                if (!active) {
                    visual.setStroke(Color.web("#3A4A5A", 0.7));
                    visual.setStrokeWidth(1.5);
                }
            });
            
            // Toggle connection on click
            visual.setOnMouseClicked(e -> {
                setActive(!active);
            });
        }
        
        /**
         * Update connection line position based on connected nodes
         */
        public void updatePosition() {
            visual.setStartX(nodeA.getX());
            visual.setStartY(nodeA.getY());
            visual.setEndX(nodeB.getX());
            visual.setEndY(nodeB.getY());
            
            // Update weight text position
            updateWeightPosition();
        }
        
        /**
         * Update weight text position to the middle of the line
         */
        private void updateWeightPosition() {
            // Position weight text at middle of the line with a small offset
            double midX = (nodeA.getX() + nodeB.getX()) / 2;
            double midY = (nodeA.getY() + nodeB.getY()) / 2;
            
            // Add a small offset to avoid text directly on the line
            double dx = nodeB.getX() - nodeA.getX();
            double dy = nodeB.getY() - nodeA.getY();
            double length = Math.sqrt(dx * dx + dy * dy);
            
            // Calculate perpendicular offset (8 pixels away from line)
            double offsetX = 0;
            double offsetY = 0;
            if (length > 0) {
                offsetX = -dy * 8 / length;  // Perpendicular to line
                offsetY = dx * 8 / length;
            }
            
            // Position the text
            weightText.setX(0);
            weightText.setY(4); // Small vertical adjustment for centering
            
            // Size and position the background rectangle
            double padding = 4;
            double width = weightText.getLayoutBounds().getWidth() + padding * 2;
            double height = weightText.getLayoutBounds().getHeight() + padding;
            
            weightBackground.setWidth(width);
            weightBackground.setHeight(height);
            weightBackground.setX(-padding);
            weightBackground.setY(-weightText.getLayoutBounds().getHeight() + padding / 2);
            
            // Position the group
            weightGroup.setTranslateX(midX + offsetX - width / 2);
            weightGroup.setTranslateY(midY + offsetY);
        }
    }

    /**
     * Create or toggle connection between two nodes
     */
    private void createOrToggleConnection(NetworkNode nodeA, NetworkNode nodeB) {
        // First check if connection already exists
        NetworkConnection existingConn = null;
        
        for (NetworkConnection conn : connections) {
            if ((conn.getNodeA() == nodeA && conn.getNodeB() == nodeB) ||
                (conn.getNodeA() == nodeB && conn.getNodeB() == nodeA)) {
                existingConn = conn;
                break;
            }
        }
        
        if (existingConn != null) {
            // Toggle existing connection
            log("Toggling connection between nodes " + nodeA.getId() + " and " + nodeB.getId());
            existingConn.setActive(!existingConn.isActive());
        } else {
            // Create new connection
            log("Creating new connection between nodes " + nodeA.getId() + " and " + nodeB.getId());
            double distance = calculateDistance(nodeA, nodeB);
            NetworkConnection newConn = new NetworkConnection(nodeA, nodeB, distance);
            newConn.setActive(true); // Activate by default
            connections.add(newConn);
            
            // Add connection visual to the network pane (below nodes)
            networkPane.getChildren().add(0, newConn.getVisual());
            
            // Add weight text with proper z-order (above line but below nodes)
            networkPane.getChildren().add(Math.min(1, networkPane.getChildren().size()), newConn.getWeightGroup());
        }
    }
} 