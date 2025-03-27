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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

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
        // Create main task container
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(650, 500);
        taskPane.setPadding(new Insets(20));
        
        // Add animated background
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        // Create title area
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Text titleText = CyberpunkEffects.createTaskTitle("NETWORK PATH OPTIMIZATION");
        Text descText = CyberpunkEffects.createTaskDescription("Establish optimal network routes between critical nodes");
        headerBox.getChildren().addAll(titleText, descText);
        taskPane.setTop(headerBox);
        
        // Create network visualization pane
        networkPane = new Pane();
        networkPane.setPrefSize(600, 350);
        networkPane.setStyle("-fx-background-color: rgba(10, 15, 30, 0.7); -fx-border-color: #303060; -fx-border-width: 1px;");
        
        // Generate network
        generateNetwork();
        
        // Add grid lines to network pane
        CyberpunkEffects.addHoloGridLines(networkPane, 30, 30);
        
        // Create bottom control area
        VBox controlBox = new VBox(15);
        controlBox.setAlignment(Pos.CENTER);
        
        // Status label
        statusLabel = new Label("ESTABLISH OPTIMAL NETWORK PATH");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        CyberpunkEffects.pulseNode(statusLabel);
        
        // Control buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET CONNECTIONS", false);
        resetButton.setOnAction(e -> resetConnections());
        
        routeButton = CyberpunkEffects.createCyberpunkButton("ESTABLISH ROUTE", true);
        routeButton.setDisable(true);
        routeButton.setOnAction(e -> establishRoute());
        
        buttonBox.getChildren().addAll(resetButton, routeButton);
        
        controlBox.getChildren().addAll(statusLabel, buttonBox);
        
        // Add components to main task pane
        taskPane.setCenter(networkPane);
        taskPane.setBottom(controlBox);
        BorderPane.setMargin(controlBox, new Insets(10, 0, 10, 0));
        
        // Add scanner effect to the task pane
        CyberpunkEffects.addScanningEffect(taskPane);
        
        // Add the task pane to the game pane
        gamePane.getChildren().add(taskPane);
        
        // Show path requirement with flashing visualization
        highlightRequiredPath();
    }
    
    /**
     * Generate a network of nodes and potential connections
     */
    private void generateNetwork() {
        nodes.clear();
        connections.clear();
        optimalConnections.clear();
        
        // Create nodes
        for (int i = 0; i < NUM_NODES; i++) {
            double x = 50 + random.nextDouble() * 500;
            double y = 50 + random.nextDouble() * 250;
            
            NetworkNode node = new NetworkNode(i, x, y);
            nodes.add(node);
            networkPane.getChildren().add(node.getVisual());
        }
        
        // Set source and destination nodes (first and last)
        sourceNode = nodes.get(0);
        sourceNode.setType(NodeType.SOURCE);
        
        destinationNode = nodes.get(NUM_NODES - 1);
        destinationNode.setType(NodeType.DESTINATION);
        
        // Create potential connections between nodes
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                // Only create connections with 70% probability
                if (random.nextDouble() < 0.7) {
                    NetworkNode nodeA = nodes.get(i);
                    NetworkNode nodeB = nodes.get(j);
                    
                    // Calculate distance
                    double distance = calculateDistance(nodeA, nodeB);
                    
                    // Skip if too far away
                    if (distance > 250) continue;
                    
                    // Create connection
                    NetworkConnection connection = new NetworkConnection(nodeA, nodeB, distance);
                    connections.add(connection);
                    
                    // Add connection visual to the network pane (below nodes)
                    networkPane.getChildren().add(0, connection.getVisual());
                }
            }
        }
        
        // Generate optimal path (minimize total distance)
        findOptimalPath();
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
        // This is a simplified path-finding for demonstration
        // In a real implementation, use Dijkstra's or A* algorithm
        
        List<NetworkNode> currentPath = new ArrayList<>();
        currentPath.add(sourceNode);
        
        findPath(currentPath, destinationNode, new ArrayList<>());
        
        LOGGER.info("Optimal path found with " + optimalConnections.size() + " connections");
    }
    
    /**
     * Recursive helper for path finding
     */
    private void findPath(List<NetworkNode> currentPath, NetworkNode target, List<NetworkConnection> currentConnections) {
        NetworkNode current = currentPath.get(currentPath.size() - 1);
        
        // If we reached the target
        if (current == target) {
            // If this path is better than existing optimal path
            if (optimalConnections.isEmpty() || currentConnections.size() < optimalConnections.size()) {
                optimalConnections.clear();
                optimalConnections.addAll(currentConnections);
            }
            return;
        }
        
        // Try all connections from current node
        for (NetworkConnection conn : connections) {
            NetworkNode nextNode = null;
            
            if (conn.getNodeA() == current && !currentPath.contains(conn.getNodeB())) {
                nextNode = conn.getNodeB();
            } else if (conn.getNodeB() == current && !currentPath.contains(conn.getNodeA())) {
                nextNode = conn.getNodeA();
            }
            
            if (nextNode != null) {
                // Add to current path
                currentPath.add(nextNode);
                currentConnections.add(conn);
                
                // Recursive call
                findPath(currentPath, target, currentConnections);
                
                // Backtrack
                currentPath.remove(currentPath.size() - 1);
                currentConnections.remove(currentConnections.size() - 1);
            }
        }
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
        if (!pathCompleted) {
            statusLabel.setText("NO COMPLETE PATH DETECTED");
            return;
        }
        
        // Count active connections
        int activeCount = 0;
        for (NetworkConnection conn : connections) {
            if (conn.isActive()) {
                activeCount++;
            }
        }
        
        // Check if path is optimal
        boolean isOptimal = activeCount <= NUM_REQUIRED_CONNECTIONS;
        
        if (isOptimal) {
            // Success!
            statusLabel.setText("OPTIMAL NETWORK PATH ESTABLISHED");
            statusLabel.setTextFill(Color.web("#00FF00"));
            
            // Completion effect
            animateDataTransfer();
            
        } else {
            // Too many connections
            statusLabel.setText("SUBOPTIMAL ROUTE - TOO MANY CONNECTIONS");
            statusLabel.setTextFill(Color.web("#FFCC00"));
            
            // Show optimal connections briefly
            highlightOptimalConnections();
        }
    }
    
    /**
     * Highlight optimal connections
     */
    private void highlightOptimalConnections() {
        // First, reset all connections to inactive
        resetConnections();
        
        // Then highlight optimal connections
        Timeline highlightTimeline = new Timeline();
        
        for (int i = 0; i < optimalConnections.size(); i++) {
            NetworkConnection conn = optimalConnections.get(i);
            final int index = i;
            
            KeyFrame kf = new KeyFrame(Duration.seconds(0.5 * i), e -> {
                conn.setActive(true);
                statusLabel.setText("SHOWING OPTIMAL PATH: CONNECTION " + (index + 1));
            });
            
            highlightTimeline.getKeyFrames().add(kf);
        }
        
        // After showing optimal path, reset again
        KeyFrame resetFrame = new KeyFrame(Duration.seconds(0.5 * optimalConnections.size() + 2), e -> {
            resetConnections();
            statusLabel.setText("TRY AGAIN WITH FEWER CONNECTIONS");
            statusLabel.setTextFill(Color.web("#00FFFF"));
        });
        
        highlightTimeline.getKeyFrames().add(resetFrame);
        highlightTimeline.play();
    }
    
    /**
     * Animate data transfer along the established path
     */
    private void animateDataTransfer() {
        // Create data packet visual
        Circle dataPacket = new Circle(10);
        dataPacket.setFill(Color.web("#00FF00"));
        
        Bloom glow = new Bloom();
        glow.setThreshold(0.3);
        dataPacket.setEffect(glow);
        
        networkPane.getChildren().add(dataPacket);
        
        // Get all active connections in order
        List<NetworkConnection> activePath = findActiveConnectionPath();
        
        if (activePath.isEmpty()) {
            dataPacket.setVisible(false);
            return;
        }
        
        // Create animation sequence for data packet
        Timeline dataAnimation = new Timeline();
        double totalDelay = 0;
        
        for (NetworkConnection conn : activePath) {
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
            // Success effect
            CyberpunkEffects.styleCompletionEffect(taskPane);
            
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
        
        private void setupInteraction() {
            // Hover effect
            visual.setOnMouseEntered(e -> {
                if (type == NodeType.NORMAL) {
                    circle.setFill(Color.web("#4A5A6A"));
                    
                    DropShadow glow = new DropShadow();
                    glow.setColor(Color.web("#00FFFF", 0.7));
                    glow.setRadius(10);
                    visual.setEffect(glow);
                }
            });
            
            visual.setOnMouseExited(e -> {
                if (type == NodeType.NORMAL) {
                    circle.setFill(Color.web("#3A4A5A"));
                    visual.setEffect(null);
                }
            });
            
            // Selection effect (for debugging)
            visual.setOnMouseClicked(e -> {
                // For clicking nodes directly
            });
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
        
        public NetworkConnection(NetworkNode nodeA, NetworkNode nodeB, double distance) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.distance = distance;
            this.active = false;
            
            // Create visual representation
            visual = new Line(nodeA.getX(), nodeA.getY(), nodeB.getX(), nodeB.getY());
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
                
                // Add glow effect
                Bloom glow = new Bloom();
                glow.setThreshold(0.3);
                visual.setEffect(glow);
            } else {
                visual.setStroke(Color.web("#3A4A5A", 0.7));
                visual.setStrokeWidth(1.5);
                visual.setEffect(null);
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
    }
} 