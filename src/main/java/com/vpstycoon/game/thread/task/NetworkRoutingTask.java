package com.vpstycoon.game.thread.task;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;


public class NetworkRoutingTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(NetworkRoutingTask.class.getName());
    private static final Random random = new Random();
    
    private static final int NUM_NODES = 7;

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
                6000, 
                20,  
                3,   
                45   
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        try {
            
            taskPane = new BorderPane();
            taskPane.setStyle("-fx-background-color: linear-gradient(to bottom, #0A0A2A, #1A1A4A); -fx-border-color: #00FFFF; -fx-border-width: 2px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            taskPane.setPrefSize(650, 500);
            taskPane.setPadding(new Insets(20));
            
            
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
            
            
            networkPane = new Pane();
            networkPane.setPrefSize(600, 180);
            networkPane.setMaxSize(600, 180);
            networkPane.setPadding(new Insets(20));
            networkPane.setStyle("-fx-background-color: rgba(10, 15, 30, 0.7); -fx-border-color: #303060; -fx-border-width: 1px;");
            
            addGridLines(networkPane, 30, 30);
            
            
            generateNetwork();
            
            
            VBox controlBox = new VBox(15);
            controlBox.setAlignment(Pos.CENTER);
            controlBox.setPadding(new Insets(20));
            
            
            statusLabel = new Label("ESTABLISH OPTIMAL NETWORK PATH");
            statusLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 16));
            statusLabel.setTextFill(Color.web("#00FFFF"));
            
            
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);
            
            Button resetButton = createCustomButton("RESET CONNECTIONS", false);
            resetButton.setOnAction(e -> resetConnections());
            
            routeButton = createCustomButton("ESTABLISH ROUTE", true);
            routeButton.setDisable(true);
            routeButton.setOnAction(e -> establishRoute());
            
            buttonBox.getChildren().addAll(resetButton, routeButton);
            
            controlBox.getChildren().addAll(statusLabel, buttonBox);
            
            
            taskPane.setCenter(networkPane);
            taskPane.setBottom(controlBox);
            BorderPane.setMargin(controlBox, new Insets(20, 0, 20, 0));
            
            
            gamePane.getChildren().add(taskPane);
            
            
            highlightRequiredPath();
            
            log("NetworkRoutingTask initialized successfully");
        } catch (Exception e) {
            log("Error initializing NetworkRoutingTask: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private void addGridLines(Pane pane, int hSpacing, int vSpacing) {
        
        for (int y = vSpacing; y < pane.getPrefHeight(); y += vSpacing) {
            Line line = new Line(0, y, pane.getPrefWidth(), y);
            line.setStroke(Color.web("#00FFFF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
        
        
        for (int x = hSpacing; x < pane.getPrefWidth(); x += hSpacing) {
            Line line = new Line(x, 0, x, pane.getPrefHeight());
            line.setStroke(Color.web("#00FFFF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
    }
    
    
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
        
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        
        return button;
    }
    
    
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
            
            
            double paneWidth = networkPane.getPrefWidth();
            double paneHeight = networkPane.getPrefHeight();
            
            
            if (paneWidth <= 0) paneWidth = 600;
            if (paneHeight <= 0) paneHeight = 300;
            
            
            double margin = 30;
            double usableWidth = paneWidth - (2 * margin);
            double usableHeight = paneHeight - (2 * margin);
            
            log("Network pane dimensions: " + paneWidth + "x" + paneHeight);
            
            
            for (int i = 0; i < NUM_NODES; i++) {
                
                double x = margin + (random.nextDouble() * usableWidth);
                double y = margin + (random.nextDouble() * usableHeight);
                
                
                x = Math.min(paneWidth - margin, Math.max(margin, x));
                y = Math.min(paneHeight - margin, Math.max(margin, y));
                
                NetworkNode node = new NetworkNode(i, x, y);
                nodes.add(node);
                networkPane.getChildren().add(node.getVisual());
            }
            
            log("Created " + nodes.size() + " network nodes within bounds");
            
            
            sourceNode = nodes.get(0);
            sourceNode.setType(NodeType.SOURCE);
            
            destinationNode = nodes.get(NUM_NODES - 1);
            destinationNode.setType(NodeType.DESTINATION);
            
            log("Set source node: " + sourceNode.getId() + ", destination node: " + destinationNode.getId());
            
            
            double maxDistance = Math.min(paneWidth, paneHeight) * 0.8;
            
            
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    
                    if (random.nextDouble() < 0.7) {
                        NetworkNode nodeA = nodes.get(i);
                        NetworkNode nodeB = nodes.get(j);
                        
                        
                        double distance = calculateDistance(nodeA, nodeB);
                        
                        
                        if (distance > maxDistance) continue;
                        
                        
                        NetworkConnection connection = new NetworkConnection(nodeA, nodeB, distance);
                        connections.add(connection);
                        
                        
                        networkPane.getChildren().add(0, connection.getVisual());
                        
                        
                        networkPane.getChildren().add(Math.min(1, networkPane.getChildren().size()), connection.getWeightGroup());
                    }
                }
            }
            
            log("Created " + connections.size() + " network connections");
            
            
            ensurePathExists();
            
            
            findOptimalPath();
        } catch (Exception e) {
            log("Error in generateNetwork: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private void ensurePathExists() {
        
        Set<NetworkNode> visited = new HashSet<>();
        Queue<NetworkNode> queue = new LinkedList<>();
        
        
        queue.add(sourceNode);
        visited.add(sourceNode);
        
        while (!queue.isEmpty()) {
            NetworkNode current = queue.poll();
            
            
            if (current == destinationNode) {
                return; 
            }
            
            
            for (NetworkConnection conn : connections) {
                NetworkNode neighbor = null;
                
                if (conn.getNodeA() == current) {
                    neighbor = conn.getNodeB();
                } else if (conn.getNodeB() == current) {
                    neighbor = conn.getNodeA();
                } else {
                    continue; 
                }
                
                
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        
        log("No path exists from source to destination, creating direct connection");
        double distance = calculateDistance(sourceNode, destinationNode);
        NetworkConnection directConn = new NetworkConnection(sourceNode, destinationNode, distance);
        connections.add(directConn);
        
        
        networkPane.getChildren().add(0, directConn.getVisual());
        networkPane.getChildren().add(Math.min(1, networkPane.getChildren().size()), directConn.getWeightGroup());
    }
    
    
    private double calculateDistance(NetworkNode nodeA, NetworkNode nodeB) {
        double dx = nodeA.getX() - nodeB.getX();
        double dy = nodeA.getY() - nodeB.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    
    private void findOptimalPath() {
        
        Map<NetworkNode, Double> distances = new HashMap<>();
        Map<NetworkNode, NetworkConnection> previousConnections = new HashMap<>();
        PriorityQueue<NetworkNode> queue = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<NetworkNode> visited = new HashSet<>();
        
        
        for (NetworkNode node : nodes) {
            distances.put(node, node == sourceNode ? 0 : Double.MAX_VALUE);
        }
        
        
        queue.add(sourceNode);
        
        while (!queue.isEmpty()) {
            NetworkNode current = queue.poll();
            
            
            if (current == destinationNode) {
                break;
            }
            
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            
            for (NetworkConnection conn : connections) {
                NetworkNode neighbor = null;
                
                if (conn.getNodeA() == current) {
                    neighbor = conn.getNodeB();
                } else if (conn.getNodeB() == current) {
                    neighbor = conn.getNodeA();
                } else {
                    continue; 
                }
                
                
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                
                double newDistance = distances.get(current) + conn.getDistance();
                
                
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousConnections.put(neighbor, conn);
                    
                    
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        
        optimalConnections.clear();
        NetworkNode current = destinationNode;
        
        while (current != sourceNode) {
            NetworkConnection conn = previousConnections.get(current);
            if (conn == null) {
                
                LOGGER.warning("No path found to destination node");
                return;
            }
            
            optimalConnections.add(0, conn); 
            
            
            if (conn.getNodeA() == current) {
                current = conn.getNodeB();
            } else {
                current = conn.getNodeA();
            }
        }
        
        LOGGER.info("Optimal path found with " + optimalConnections.size() + 
                   " connections and total distance " + distances.get(destinationNode));
    }
    
    
    private void highlightRequiredPath() {
        
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
        
        
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), pathLine);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        
        Timeline dashAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                pathLine.getStrokeDashArray().addAll(10.0, 10.0);
                pathLine.setStrokeDashOffset(0);
            }),
            new KeyFrame(Duration.seconds(2), new KeyValue(pathLine.strokeDashOffsetProperty(), 40))
        );
        dashAnimation.setCycleCount(Timeline.INDEFINITE);
        
        
        ParallelTransition animations = new ParallelTransition(flashSource, flashDest, fadeIn, dashAnimation);
        animations.play();
        
        
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            statusLabel.setText("CONNECT NODES TO ESTABLISH OPTIMAL PATH");
            
            
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
    
    
    private void resetConnections() {
        
        for (NetworkConnection conn : connections) {
            conn.setActive(false);
        }
        
        
        pathCompleted = false;
        routeButton.setDisable(true);
        statusLabel.setText("CONNECTIONS RESET");
        
        
        for (NetworkNode node : nodes) {
            if (node != sourceNode && node != destinationNode) {
                node.setType(NodeType.NORMAL);
            }
        }
    }
    
    
    private void establishRoute() {
        List<NetworkConnection> path = findActiveConnectionPath();
        
        if (path.isEmpty() || !checkPathCompletion()) {
            statusLabel.setText("Connection failed: No path to destination");
            return;
        }
        
        
        double userPathDistance = path.stream()
            .mapToDouble(NetworkConnection::getDistance)
            .sum();
        
        
        if (optimalConnections.isEmpty()) {
            findOptimalPath();
        }
        
        
        double optimalDistance = optimalConnections.stream()
            .mapToDouble(NetworkConnection::getDistance)
            .sum();
        
        
        boolean isOptimal = userPathDistance <= optimalDistance * 1.2;
        
        if (isOptimal) {
            statusLabel.setText("Optimal network path established! Distance: " + String.format("%.0f", userPathDistance));
            
            
            DropShadow glow = new DropShadow();
            glow.setColor(Color.CYAN);
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(10);
            taskPane.setEffect(glow);
            
            
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
                
                animateDataTransfer(path);
            });
            flash.play();
        } else {
            
            statusLabel.setText("Suboptimal route - Distance: " + String.format("%.0f", userPathDistance) + 
                              " (Optimal: " + String.format("%.0f", optimalDistance) + ")");
            statusLabel.setTextFill(Color.ORANGE);
            
            
            highlightOptimalConnections();
        }
    }
    
    
    private void highlightOptimalConnections() {
        
        resetConnections();
        
        
        if (optimalConnections.isEmpty()) {
            findMinimalPath();
        }
        
        
        if (optimalConnections.isEmpty()) {
            statusLabel.setText("Could not determine optimal path");
            return;
        }
        
        
        double totalDistance = optimalConnections.stream()
            .mapToDouble(NetworkConnection::getDistance)
            .sum();
        
        
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
        
        
        KeyFrame resetFrame = new KeyFrame(Duration.seconds(0.5 * optimalConnections.size() + 2), e -> {
            resetConnections();
            statusLabel.setText("Try again with a more optimal route");
            statusLabel.setTextFill(Color.web("#00FFFF"));
        });
        
        highlightTimeline.getKeyFrames().add(resetFrame);
        highlightTimeline.play();
    }
    
    
    private void findMinimalPath() {
        
        Map<NetworkNode, Double> distances = new HashMap<>();
        Map<NetworkNode, NetworkConnection> previousConnections = new HashMap<>();
        PriorityQueue<NetworkNode> queue = new PriorityQueue<>(Comparator.comparing(distances::get));
        Set<NetworkNode> visited = new HashSet<>();
        
        
        for (NetworkNode node : nodes) {
            distances.put(node, node == sourceNode ? 0 : Double.MAX_VALUE);
        }
        
        
        queue.add(sourceNode);
        
        while (!queue.isEmpty()) {
            NetworkNode current = queue.poll();
            
            
            if (current == destinationNode) {
                break;
            }
            
            
            if (visited.contains(current)) {
                continue;
            }
            
            visited.add(current);
            
            
            for (NetworkConnection conn : connections) {
                NetworkNode neighbor = null;
                
                if (conn.getNodeA() == current) {
                    neighbor = conn.getNodeB();
                } else if (conn.getNodeB() == current) {
                    neighbor = conn.getNodeA();
                } else {
                    continue; 
                }
                
                
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                
                double newDistance = distances.get(current) + conn.getDistance();
                
                
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousConnections.put(neighbor, conn);
                    
                    
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        
        optimalConnections.clear();
        NetworkNode current = destinationNode;
        
        if (previousConnections.containsKey(destinationNode)) {
            while (current != sourceNode) {
                NetworkConnection conn = previousConnections.get(current);
                optimalConnections.add(0, conn); 
                
                
                if (conn.getNodeA() == current) {
                    current = conn.getNodeB();
                } else {
                    current = conn.getNodeA();
                }
            }
        }
    }
    
    
    private void animateDataTransfer(List<NetworkConnection> path) {
        
        Circle dataPacket = new Circle(10);
        dataPacket.setFill(Color.web("#00FF00"));
        
        Bloom glow = new Bloom();
        glow.setThreshold(0.3);
        dataPacket.setEffect(glow);
        
        networkPane.getChildren().add(dataPacket);
        
        
        Timeline dataAnimation = new Timeline();
        double totalDelay = 0;
        
        for (NetworkConnection conn : path) {
            NetworkNode start = conn.getNodeA();
            NetworkNode end = conn.getNodeB();
            
            
            if (!isNodeInPath(start)) {
                NetworkNode temp = start;
                start = end;
                end = temp;
            }
            
            final double startX = start.getX();
            final double startY = start.getY();
            final double endX = end.getX();
            final double endY = end.getY();
            
            
            double distance = calculateDistance(start, end);
            double duration = distance / 200; 
            
            
            KeyFrame startFrame = new KeyFrame(Duration.seconds(totalDelay),
                    new KeyValue(dataPacket.centerXProperty(), startX),
                    new KeyValue(dataPacket.centerYProperty(), startY));
            
            KeyFrame endFrame = new KeyFrame(Duration.seconds(totalDelay + duration),
                    new KeyValue(dataPacket.centerXProperty(), endX),
                    new KeyValue(dataPacket.centerYProperty(), endY));
            
            dataAnimation.getKeyFrames().addAll(startFrame, endFrame);
            
            totalDelay += duration;
        }
        
        
        dataAnimation.setOnFinished(e -> {
            
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
    
    
    private List<NetworkConnection> findActiveConnectionPath() {
        List<NetworkConnection> path = new ArrayList<>();
        List<NetworkNode> visited = new ArrayList<>();
        
        
        visited.add(sourceNode);
        NetworkNode current = sourceNode;
        
        
        while (current != destinationNode) {
            boolean found = false;
            
            
            for (NetworkConnection conn : connections) {
                if (!conn.isActive()) continue;
                
                NetworkNode other = null;
                if (conn.getNodeA() == current && !visited.contains(conn.getNodeB())) {
                    other = conn.getNodeB();
                } else if (conn.getNodeB() == current && !visited.contains(conn.getNodeA())) {
                    other = conn.getNodeA();
                }
                
                if (other != null) {
                    
                    path.add(conn);
                    visited.add(other);
                    current = other;
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                
                break;
            }
        }
        
        return path;
    }
    
    
    private boolean isNodeInPath(NetworkNode node) {
        
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
    
    
    private boolean checkPathCompletion() {
        
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
    
    
    private enum NodeType {
        NORMAL,
        SOURCE,
        DESTINATION,
        ACTIVE
    }
    
    
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
            
            
            circle = new Circle(15);
            updateNodeAppearance();
            
            label = new Label(String.valueOf(id));
            label.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 12));
            label.setTextFill(Color.WHITE);
            
            visual = new StackPane(circle, label);
            visual.setLayoutX(x - 15);
            visual.setLayoutY(y - 15);
            
            
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
                    
                    toggleNodeState();
                } else {
                    
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
                    
                    
                    newX = Math.max(0, Math.min(networkPane.getWidth() - 30, newX));
                    newY = Math.max(0, Math.min(networkPane.getHeight() - 30, newY));
                    
                    visual.setLayoutX(newX);
                    visual.setLayoutY(newY);
                    
                    
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
                
                if (e.getClickCount() == 2) {
                    
                    selectNodeForConnection();
                } else if (e.isSecondaryButtonDown()) {
                    
                } else {
                    
                    System.out.println("Node " + id + " clicked");
                }
                
                e.consume();
            });
        }
        
        
        public void selectNodeForConnection() {
            
            if (type == NodeType.SOURCE || type == NodeType.DESTINATION) {
                return;
            }
            
            log("Node " + id + " selected for connection");
            
            
            if (type == NodeType.NORMAL) {
                
                setType(NodeType.ACTIVE);
                
                
                NetworkNode otherNode = null;
                for (NetworkNode node : nodes) {
                    if (node != this && node.getType() == NodeType.ACTIVE) {
                        otherNode = node;
                        break;
                    }
                }
                
                if (otherNode != null) {
                    
                    createOrToggleConnection(this, otherNode);
                    
                    
                    setType(NodeType.NORMAL);
                    otherNode.setType(NodeType.NORMAL);
                    
                    
                    boolean canEstablish = checkPathCompletion();
                    routeButton.setDisable(!canEstablish);
                    
                    if (canEstablish && !pathCompleted) {
                        statusLabel.setText("Path complete - Click ESTABLISH ROUTE");
                        pathCompleted = true;
                    }
                }
            } else if (type == NodeType.ACTIVE) {
                
                setType(NodeType.NORMAL);
            }
        }
        
        
        public void toggleNodeState() {
            
            if (type == NodeType.SOURCE || type == NodeType.DESTINATION) {
                return;
            }
            
            
            if (type == NodeType.NORMAL) {
                setType(NodeType.ACTIVE);
            } else {
                setType(NodeType.NORMAL);
            }
        }
    }
    
    
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
            
            
            visual = new Line(nodeA.getX(), nodeA.getY(), nodeB.getX(), nodeB.getY());
            
            
            weightText = new Text(String.format("%.0f", distance));
            weightText.setFont(Font.font("Orbitron", FontWeight.BOLD, 10));
            
            
            weightBackground = new Rectangle();
            weightBackground.setArcWidth(6);
            weightBackground.setArcHeight(6);
            
            
            weightGroup = new Group(weightBackground, weightText);
            
            
            updateWeightPosition();
            
            updateVisualAppearance();
            
            
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
            
            
            if (active) {
                if (nodeA != sourceNode && nodeA != destinationNode) {
                    nodeA.setType(NodeType.ACTIVE);
                }
                if (nodeB != sourceNode && nodeB != destinationNode) {
                    nodeB.setType(NodeType.ACTIVE);
                }
            } else {
                
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
                
                
                weightText.setFill(Color.web("#00FFFF"));
                weightBackground.setFill(Color.web("#1A2A3A"));
                weightBackground.setStroke(Color.web("#00FFFF"));
                weightBackground.setStrokeWidth(1);
                
                
                Bloom glow = new Bloom();
                glow.setThreshold(0.3);
                visual.setEffect(glow);
            } else {
                visual.setStroke(Color.web("#3A4A5A", 0.7));
                visual.setStrokeWidth(1.5);
                visual.setEffect(null);
                
                
                weightText.setFill(Color.web("#5A6A7A", 0.9));
                weightBackground.setFill(Color.web("#222222", 0.7));
                weightBackground.setStroke(Color.web("#3A4A5A", 0.5));
                weightBackground.setStrokeWidth(0.5);
            }
        }
        
        private void setupInteraction() {
            
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
            
            
            visual.setOnMouseClicked(e -> {
                setActive(!active);
            });
        }
        
        
        public void updatePosition() {
            visual.setStartX(nodeA.getX());
            visual.setStartY(nodeA.getY());
            visual.setEndX(nodeB.getX());
            visual.setEndY(nodeB.getY());
            
            
            updateWeightPosition();
        }
        
        
        private void updateWeightPosition() {
            
            double midX = (nodeA.getX() + nodeB.getX()) / 2;
            double midY = (nodeA.getY() + nodeB.getY()) / 2;
            
            
            double dx = nodeB.getX() - nodeA.getX();
            double dy = nodeB.getY() - nodeA.getY();
            double length = Math.sqrt(dx * dx + dy * dy);
            
            
            double offsetX = 0;
            double offsetY = 0;
            if (length > 0) {
                offsetX = -dy * 8 / length;  
                offsetY = dx * 8 / length;
            }
            
            
            weightText.setX(0);
            weightText.setY(4); 
            
            
            double padding = 4;
            double width = weightText.getLayoutBounds().getWidth() + padding * 2;
            double height = weightText.getLayoutBounds().getHeight() + padding;
            
            weightBackground.setWidth(width);
            weightBackground.setHeight(height);
            weightBackground.setX(-padding);
            weightBackground.setY(-weightText.getLayoutBounds().getHeight() + padding / 2);
            
            
            weightGroup.setTranslateX(midX + offsetX - width / 2);
            weightGroup.setTranslateY(midY + offsetY);
        }
    }

    
    private void createOrToggleConnection(NetworkNode nodeA, NetworkNode nodeB) {
        
        NetworkConnection existingConn = null;
        
        for (NetworkConnection conn : connections) {
            if ((conn.getNodeA() == nodeA && conn.getNodeB() == nodeB) ||
                (conn.getNodeA() == nodeB && conn.getNodeB() == nodeA)) {
                existingConn = conn;
                break;
            }
        }
        
        if (existingConn != null) {
            
            log("Toggling connection between nodes " + nodeA.getId() + " and " + nodeB.getId());
            existingConn.setActive(!existingConn.isActive());
        } else {
            
            log("Creating new connection between nodes " + nodeA.getId() + " and " + nodeB.getId());
            double distance = calculateDistance(nodeA, nodeB);
            NetworkConnection newConn = new NetworkConnection(nodeA, nodeB, distance);
            newConn.setActive(true); 
            connections.add(newConn);
            
            
            networkPane.getChildren().add(0, newConn.getVisual());
            
            
            networkPane.getChildren().add(Math.min(1, networkPane.getChildren().size()), newConn.getWeightGroup());
        }
    }
} 

