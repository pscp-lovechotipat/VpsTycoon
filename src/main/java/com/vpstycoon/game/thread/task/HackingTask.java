package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Hacking Grid Task - player must find and connect the right nodes in a grid to hack a system
 */
public class HackingTask extends GameTask {
    private static final int GRID_SIZE = 5;
    private static final int PATH_LENGTH = 4;
    
    private int[][] grid;
    private boolean[][] revealed;
    private List<int[]> targetPath;
    private List<int[]> playerPath;
    private Button[][] gridButtons;
    private Label statusLabel;
    private Timeline scanEffect;

    /**
     * Constructor for Hacking Task
     */
    public HackingTask() {
        super(
                "Neural Grid Hack",
                "Navigate the security grid to find the access path",
                "/images/task/hacking_task.png",
                8000, // reward
                15,   // penalty
                4,    // difficulty
                90    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Initialize data structures
        grid = new int[GRID_SIZE][GRID_SIZE];
        revealed = new boolean[GRID_SIZE][GRID_SIZE];
        targetPath = new ArrayList<>();
        playerPath = new ArrayList<>();
        gridButtons = new Button[GRID_SIZE][GRID_SIZE];
        
        // Generate the hacking grid with a valid path
        generateGrid();
        
        // Create the main container with cyberpunk styling
        BorderPane hackingPane = new BorderPane();
        hackingPane.setPadding(new Insets(20));
        hackingPane.setMaxWidth(700);
        hackingPane.setMaxHeight(550);
        
        // Apply cyberpunk styling with neon border
        hackingPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0A0A2A, #151540);" +
            "-fx-border-color: #00FFFF;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 5px;"
        );
        
        // Add holographic grid lines
        CyberpunkEffects.addHoloGridLines(hackingPane, 20, 20);
        
        // Create cyberpunk-styled title and description
        Text titleText = CyberpunkEffects.createTaskTitle("NEURAL GRID HACK v2.0");
        Text descText = CyberpunkEffects.createTaskDescription(
            "Find and connect the hidden access path through the security grid.\n" +
            "Start at any node and create a " + PATH_LENGTH + "-node path."
        );
        
        // Create the grid display
        GridPane gridPane = createGridDisplay();
        
        // Status message with cyberpunk styling
        statusLabel = CyberpunkEffects.createGlowingLabel("SELECT STARTING NODE", "#00FFFF");
        
        // Add scanning effect animation
        addScanningEffect(gridPane);
        
        // Create control buttons
        HBox controlButtons = createControlButtons();
        
        // Create status panel
        VBox statusPanel = createStatusPanel();
        
        // Arrange all components
        VBox topSection = new VBox(15);
        topSection.setAlignment(Pos.CENTER);
        topSection.getChildren().addAll(titleText, descText);
        
        VBox centerSection = new VBox(20);
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setPadding(new Insets(20, 0, 20, 0));
        centerSection.getChildren().addAll(gridPane, statusLabel);
        
        HBox bottomSection = new HBox(30);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setPadding(new Insets(20, 0, 0, 0));
        bottomSection.getChildren().addAll(statusPanel, controlButtons);
        
        hackingPane.setTop(topSection);
        hackingPane.setCenter(centerSection);
        hackingPane.setBottom(bottomSection);
        
        // Add the task content to the game pane
        gamePane.getChildren().add(hackingPane);
        
        // Add animated background effects
        CyberpunkEffects.addAnimatedBackground(gamePane);
    }
    
    /**
     * Create the hacking grid display with cyberpunk styling
     */
    private GridPane createGridDisplay() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        
        // Create grid buttons
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // Create node button with cyberpunk styling
                Button nodeButton = new Button();
                nodeButton.setPrefSize(60, 60);
                nodeButton.setMinSize(60, 60);
                nodeButton.setMaxSize(60, 60);
                
                // Set initial style (hidden node)
                styleGridButton(nodeButton, row, col, false);
                
                // Handle node selection
                final int r = row;
                final int c = col;
                nodeButton.setOnAction(e -> handleNodeSelection(r, c));
                
                gridButtons[row][col] = nodeButton;
                gridPane.add(nodeButton, col, row);
            }
        }
        
        return gridPane;
    }
    
    /**
     * Create control buttons
     */
    private HBox createControlButtons() {
        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER);
        
        // Reset button
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET PATH", false);
        resetButton.setOnAction(e -> resetPath());
        
        // Verify button
        Button verifyButton = CyberpunkEffects.createCyberpunkButton("VERIFY PATH", true);
        verifyButton.setOnAction(e -> verifyPath());
        
        // Cheat button (for testing)
        Button revealButton = CyberpunkEffects.createCyberpunkButton("REVEAL PATH", false);
        revealButton.setOnAction(e -> revealPath());
        
        controlBox.getChildren().addAll(resetButton, verifyButton, revealButton);
        return controlBox;
    }
    
    /**
     * Create status panel with cyberpunk styling
     */
    private VBox createStatusPanel() {
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(15));
        statusBox.setStyle("-fx-background-color: rgba(0, 10, 30, 0.6); -fx-border-color: #00FFFF; -fx-border-width: 1px;");
        
        Text statusTitle = new Text("HACK STATUS");
        statusTitle.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        statusTitle.setFill(Color.web("#00FFFF"));
        
        Text pathLengthText = new Text("PATH LENGTH: 0/" + PATH_LENGTH);
        pathLengthText.setId("pathLength");
        pathLengthText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        pathLengthText.setFill(Color.LIGHTCYAN);
        
        Text securityLevelText = new Text("SECURITY LEVEL: HIGH");
        securityLevelText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        securityLevelText.setFill(Color.LIGHTCYAN);
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setRadius(10);
        glow.setSpread(0.2);
        statusTitle.setEffect(glow);
        
        statusBox.getChildren().addAll(statusTitle, pathLengthText, securityLevelText);
        return statusBox;
    }
    
    /**
     * Add scanning effect to the grid
     */
    private void addScanningEffect(GridPane gridPane) {
        // Horizontal scanner
        Rectangle hScanner = new Rectangle(gridPane.getWidth(), 3);
        hScanner.setFill(Color.web("#00FFFF", 0.7));
        
        // Vertical scanner
        Rectangle vScanner = new Rectangle(3, gridPane.getHeight());
        vScanner.setFill(Color.web("#00FFFF", 0.7));
        
        Glow glow = new Glow(0.8);
        hScanner.setEffect(glow);
        vScanner.setEffect(glow);
        
        StackPane scannerPane = new StackPane();
        scannerPane.getChildren().addAll(hScanner, vScanner);
        scannerPane.setMouseTransparent(true);
        
        // Add scanner pane on top of grid
        StackPane gridContainer = new StackPane();
        gridContainer.getChildren().addAll(gridPane, scannerPane);
        
        // Create scanning animation
        scanEffect = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(hScanner.translateYProperty(), -gridPane.getHeight() / 2),
                new KeyValue(vScanner.translateXProperty(), -gridPane.getWidth() / 2)
            ),
            new KeyFrame(Duration.seconds(3), 
                new KeyValue(hScanner.translateYProperty(), gridPane.getHeight()),
                new KeyValue(vScanner.translateXProperty(), gridPane.getWidth())
            )
        );
        
        scanEffect.setCycleCount(Timeline.INDEFINITE);
        scanEffect.setAutoReverse(true);
        scanEffect.play();
    }
    
    /**
     * Style a grid button based on its state
     */
    private void styleGridButton(Button button, int row, int col, boolean selected) {
        int value = grid[row][col];
        
        // Base style for all nodes
        String baseStyle = 
            "-fx-background-color: #080820; " +
            "-fx-border-color: #00DDFF; " +
            "-fx-border-width: 1px; " +
            "-fx-background-radius: 5px; " +
            "-fx-border-radius: 5px;";
        
        // Style for selected nodes in the path
        String selectedStyle = 
            "-fx-background-color: linear-gradient(to bottom, #004060, #002040); " +
            "-fx-border-color: #00FFFF; " +
            "-fx-border-width: 2px; " +
            "-fx-background-radius: 5px; " +
            "-fx-border-radius: 5px; " +
            "-fx-effect: dropshadow(gaussian, #00FFFF, 10, 0.7, 0, 0);";
            
        // Apply appropriate style
        if (selected) {
            button.setStyle(selectedStyle);
            
            // Update button text based on grid value
            Label valueLabel = new Label(Integer.toString(value));
            valueLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 18));
            valueLabel.setTextFill(Color.LIGHTCYAN);
            button.setGraphic(valueLabel);
            
            // Update revealed state
            revealed[row][col] = true;
        } else if (revealed[row][col]) {
            // Node has been revealed but is not in the current path
            button.setStyle(baseStyle);
            
            Label valueLabel = new Label(Integer.toString(value));
            valueLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 18));
            valueLabel.setTextFill(Color.GRAY);
            button.setGraphic(valueLabel);
        } else {
            // Node is hidden
            button.setStyle(baseStyle);
            button.setGraphic(null);
        }
    }
    
    /**
     * Generate a random grid with a valid path
     */
    private void generateGrid() {
        // Fill the grid with random numbers 1-9
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = random.nextInt(9) + 1;
                revealed[i][j] = false;
            }
        }
        
        // Generate a random path
        int startRow = random.nextInt(GRID_SIZE);
        int startCol = random.nextInt(GRID_SIZE);
        
        targetPath.add(new int[]{startRow, startCol});
        
        // Add consecutive nodes to form a path
        for (int i = 1; i < PATH_LENGTH; i++) {
            int[] lastNode = targetPath.get(i - 1);
            int lastRow = lastNode[0];
            int lastCol = lastNode[1];
            
            // Find possible next nodes (adjacent, not diagonal, not already in path)
            List<int[]> possibleNextNodes = new ArrayList<>();
            
            // Check up
            if (lastRow > 0) {
                int[] upNode = new int[]{lastRow - 1, lastCol};
                if (!containsNode(targetPath, upNode)) {
                    possibleNextNodes.add(upNode);
                }
            }
            
            // Check right
            if (lastCol < GRID_SIZE - 1) {
                int[] rightNode = new int[]{lastRow, lastCol + 1};
                if (!containsNode(targetPath, rightNode)) {
                    possibleNextNodes.add(rightNode);
                }
            }
            
            // Check down
            if (lastRow < GRID_SIZE - 1) {
                int[] downNode = new int[]{lastRow + 1, lastCol};
                if (!containsNode(targetPath, downNode)) {
                    possibleNextNodes.add(downNode);
                }
            }
            
            // Check left
            if (lastCol > 0) {
                int[] leftNode = new int[]{lastRow, lastCol - 1};
                if (!containsNode(targetPath, leftNode)) {
                    possibleNextNodes.add(leftNode);
                }
            }
            
            // If no possible moves, break out
            if (possibleNextNodes.isEmpty()) {
                break;
            }
            
            // Choose a random next node
            int[] nextNode = possibleNextNodes.get(random.nextInt(possibleNextNodes.size()));
            targetPath.add(nextNode);
        }
        
        // Debug output for target path
        log("Generated path of length " + targetPath.size() + ":");
        for (int[] node : targetPath) {
            log("  Node at [" + node[0] + "," + node[1] + "] = " + grid[node[0]][node[1]]);
        }
    }
    
    /**
     * Handle node selection by the player
     */
    private void handleNodeSelection(int row, int col) {
        // Check if the node is already in the path
        int[] newNode = new int[]{row, col};
        
        if (containsNode(playerPath, newNode)) {
            // Node already selected - check if it's the last one
            if (playerPath.get(playerPath.size() - 1)[0] == row && 
                playerPath.get(playerPath.size() - 1)[1] == col) {
                
                // Remove the last node
                playerPath.remove(playerPath.size() - 1);
                styleGridButton(gridButtons[row][col], row, col, false);
                
                updatePathStatus();
            } else {
                // Node is in the middle of the path - can't remove it
                statusLabel.setText("CAN'T REMOVE NODE FROM MIDDLE OF PATH");
                statusLabel.setTextFill(Color.RED);
            }
            return;
        }
        
        // Check if this is the first node
        if (playerPath.isEmpty()) {
            playerPath.add(newNode);
            styleGridButton(gridButtons[row][col], row, col, true);
            statusLabel.setText("NODE ADDED - SELECT ADJACENT NODE");
            statusLabel.setTextFill(Color.LIGHTCYAN);
            updatePathStatus();
            return;
        }
        
        // Check if the new node is adjacent to the last one
        int[] lastNode = playerPath.get(playerPath.size() - 1);
        int rowDiff = Math.abs(row - lastNode[0]);
        int colDiff = Math.abs(col - lastNode[1]);
        
        if ((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)) {
            // Adjacent node - add to path
            playerPath.add(newNode);
            styleGridButton(gridButtons[row][col], row, col, true);
            statusLabel.setText("NODE ADDED - PATH LENGTH: " + playerPath.size() + "/" + PATH_LENGTH);
            statusLabel.setTextFill(Color.LIGHTCYAN);
            
            // Check if path is complete
            if (playerPath.size() == PATH_LENGTH) {
                statusLabel.setText("PATH COMPLETE - VERIFY TO HACK");
                statusLabel.setTextFill(Color.GREEN);
            }
            
            updatePathStatus();
        } else {
            // Not adjacent
            statusLabel.setText("INVALID NODE - MUST BE ADJACENT");
            statusLabel.setTextFill(Color.RED);
        }
    }
    
    /**
     * Update the path status display
     */
    private void updatePathStatus() {
        Text pathLengthText = (Text) gamePane.lookup("#pathLength");
        if (pathLengthText != null) {
            pathLengthText.setText("PATH LENGTH: " + playerPath.size() + "/" + PATH_LENGTH);
        }
    }
    
    /**
     * Reset the player's path
     */
    private void resetPath() {
        // Reset grid button styles
        for (int[] node : playerPath) {
            styleGridButton(gridButtons[node[0]][node[1]], node[0], node[1], false);
        }
        
        // Clear the path
        playerPath.clear();
        
        // Update status
        statusLabel.setText("PATH RESET - SELECT STARTING NODE");
        statusLabel.setTextFill(Color.LIGHTCYAN);
        updatePathStatus();
    }
    
    /**
     * Verify if the player's path matches the target path
     */
    private void verifyPath() {
        if (playerPath.size() != PATH_LENGTH) {
            statusLabel.setText("PATH INCOMPLETE - NEED " + PATH_LENGTH + " NODES");
            statusLabel.setTextFill(Color.RED);
            return;
        }
        
        // Calculate matching score based on nodes in common
        int matchingNodes = 0;
        for (int[] playerNode : playerPath) {
            if (containsNode(targetPath, playerNode)) {
                matchingNodes++;
            }
        }
        
        // Calculate the final score as a percentage of matching nodes
        int scorePercent = (matchingNodes * 100) / PATH_LENGTH;
        
        // Path is successful if it includes at least half of the target nodes
        if (scorePercent >= 50) {
            statusLabel.setText("HACK SUCCESSFUL - SECURITY BYPASSED");
            statusLabel.setTextFill(Color.GREEN);
            
            // Apply success effect to path nodes
            for (int[] node : playerPath) {
                int row = node[0];
                int col = node[1];
                Button button = gridButtons[row][col];
                button.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #003020, #001510); " +
                    "-fx-border-color: #00FF80; " +
                    "-fx-border-width: 2px; " +
                    "-fx-effect: dropshadow(gaussian, #00FF80, 15, 0.8, 0, 0);"
                );
                CyberpunkEffects.styleCompletionEffect(button);
            }
            
            // Stop the scanner effect
            if (scanEffect != null) {
                scanEffect.stop();
            }
            
            // Mark task as complete
            completeTask();
        } else {
            statusLabel.setText("HACK FAILED - ONLY " + scorePercent + "% MATCH");
            statusLabel.setTextFill(Color.RED);
            
            // Apply failure effect to path nodes
            for (int[] node : playerPath) {
                CyberpunkEffects.styleFailureEffect(gridButtons[node[0]][node[1]]);
            }
            
            // Reduce attempts or fail task
            if (random.nextBoolean()) {
                failTask();
            } else {
                // Give the player another chance
                Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> resetPath()));
                resetTimeline.play();
            }
        }
    }
    
    /**
     * Reveal the correct path (debug/cheat feature)
     */
    private void revealPath() {
        // First reset the current path
        resetPath();
        
        // Reveal the target path
        for (int[] node : targetPath) {
            int row = node[0];
            int col = node[1];
            playerPath.add(node);
            styleGridButton(gridButtons[row][col], row, col, true);
        }
        
        statusLabel.setText("TARGET PATH REVEALED - VERIFY TO HACK");
        statusLabel.setTextFill(Color.YELLOW);
        updatePathStatus();
    }
    
    /**
     * Check if a path contains a specific node
     */
    private boolean containsNode(List<int[]> path, int[] node) {
        for (int[] existingNode : path) {
            if (existingNode[0] == node[0] && existingNode[1] == node[1]) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clean up resources when task is done
     */
    private void cleanupResources() {
        // Stop any animations or timers
        if (scanEffect != null) {
            scanEffect.stop();
        }
    }

    /**
     * Clean up resources and prepare for the next task
     */
    @Override
    protected void cleanupTask() {
        super.cleanupTask();
        cleanupResources();
    }
} 