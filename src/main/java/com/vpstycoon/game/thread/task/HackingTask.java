package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


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

    
    public HackingTask() {
        super(
                "Neural Grid Hack",
                "Navigate the security grid to find the access path",
                "/images/task/hacking_task.png",
                8000, 
                15,   
                4,    
                90    
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        
        grid = new int[GRID_SIZE][GRID_SIZE];
        revealed = new boolean[GRID_SIZE][GRID_SIZE];
        targetPath = new ArrayList<>();
        playerPath = new ArrayList<>();
        gridButtons = new Button[GRID_SIZE][GRID_SIZE];
        
        
        generateGrid();
        
        
        BorderPane hackingPane = new BorderPane();
        hackingPane.setPadding(new Insets(10));
        hackingPane.setPrefWidth(600);
        hackingPane.setPrefHeight(420);
        hackingPane.setMaxWidth(600);
        hackingPane.setMaxHeight(420);
        hackingPane.setMinWidth(600);
        hackingPane.setMinHeight(420);
        
        
        hackingPane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0A0A2A, #151540);" +
            "-fx-border-color: #00FFFF;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 5px;"
        );
        
        
        CyberpunkEffects.addHoloGridLines(hackingPane, 20, 20);
        
        
        
        Text descText = CyberpunkEffects.createTaskDescription(
            "Find and connect the hidden access path through the security grid.\n" +
            "Start at any node and create a " + PATH_LENGTH + "-node path."
        );
        
        
        GridPane gridPane = createGridDisplay();
        
        
        statusLabel = CyberpunkEffects.createGlowingLabel("SELECT STARTING NODE", "#00FFFF");
        
        
        addScanningEffect(gridPane);
        
        
        HBox controlButtons = createControlButtons();
        
        
        VBox statusPanel = createStatusPanel();
        
        
        VBox topSection = new VBox(10);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(0, 0, 20, 0));
        topSection.getChildren().addAll(descText);
        
        VBox centerSection = new VBox(10);
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setPadding(new Insets(20, 0, 20, 0));
        centerSection.getChildren().addAll(gridPane, statusLabel);
        
        HBox bottomSection = new HBox(20);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setPadding(new Insets(20, 0, 0, 0));
        bottomSection.getChildren().addAll(statusPanel, controlButtons);
        
        hackingPane.setTop(topSection);
        hackingPane.setCenter(centerSection);
        hackingPane.setBottom(bottomSection);
        
        
        gamePane.getChildren().add(hackingPane);
        
        
        CyberpunkEffects.addAnimatedBackground(hackingPane);
    }
    
    
    private GridPane createGridDisplay() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(6);
        gridPane.setVgap(6);
        gridPane.setPadding(new Insets(10));
        
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                
                Button nodeButton = new Button();
                nodeButton.setPrefSize(48, 48);
                nodeButton.setMinSize(48, 48);
                nodeButton.setMaxSize(48, 48);
                
                
                styleGridButton(nodeButton, row, col, false);
                
                
                final int r = row;
                final int c = col;
                nodeButton.setOnAction(e -> handleNodeSelection(r, c));
                
                gridButtons[row][col] = nodeButton;
                gridPane.add(nodeButton, col, row);
            }
        }
        
        return gridPane;
    }
    
    
    private HBox createControlButtons() {
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        
        
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET", false);
        resetButton.setOnAction(e -> resetPath());
        
        
        Button verifyButton = CyberpunkEffects.createCyberpunkButton("VERIFY", true);
        verifyButton.setOnAction(e -> verifyPath());
        
        
        Button revealButton = CyberpunkEffects.createCyberpunkButton("REVEAL", false);
        revealButton.setOnAction(e -> revealPath());
        
        controlBox.getChildren().addAll(resetButton, verifyButton, revealButton);
        return controlBox;
    }
    
    
    private VBox createStatusPanel() {
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(8));
        statusBox.setStyle("-fx-background-color: rgba(0, 10, 30, 0.6); -fx-border-color: #00FFFF; -fx-border-width: 1px;");
        
        Text statusTitle = new Text("HACK STATUS");
        statusTitle.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        statusTitle.setFill(Color.web("#00FFFF"));
        
        Text pathLengthText = new Text("PATH: 0/" + PATH_LENGTH);
        pathLengthText.setId("pathLength");
        pathLengthText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 12));
        pathLengthText.setFill(Color.LIGHTCYAN);
        
        Text securityLevelText = new Text("SECURITY: HIGH");
        securityLevelText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 12));
        securityLevelText.setFill(Color.LIGHTCYAN);
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setRadius(10);
        glow.setSpread(0.2);
        statusTitle.setEffect(glow);
        
        statusBox.getChildren().addAll(statusTitle, pathLengthText, securityLevelText);
        return statusBox;
    }
    
    
    private void addScanningEffect(GridPane gridPane) {
        
        Rectangle hScanner = new Rectangle(gridPane.getWidth(), 3);
        hScanner.setFill(Color.web("#00FFFF", 0.7));
        
        
        Rectangle vScanner = new Rectangle(3, gridPane.getHeight());
        vScanner.setFill(Color.web("#00FFFF", 0.7));
        
        Glow glow = new Glow(0.8);
        hScanner.setEffect(glow);
        vScanner.setEffect(glow);
        
        StackPane scannerPane = new StackPane();
        scannerPane.getChildren().addAll(hScanner, vScanner);
        scannerPane.setMouseTransparent(true);
        
        
        StackPane gridContainer = new StackPane();
        gridContainer.getChildren().addAll(gridPane, scannerPane);
        
        
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
    
    
    private void styleGridButton(Button button, int row, int col, boolean selected) {
        int value = grid[row][col];
        
        
        String baseStyle = 
            "-fx-background-color: #080820; " +
            "-fx-border-color: #00DDFF; " +
            "-fx-border-width: 1px; " +
            "-fx-background-radius: 5px; " +
            "-fx-border-radius: 5px;";
        
        
        String selectedStyle = 
            "-fx-background-color: linear-gradient(to bottom, #004060, #002040); " +
            "-fx-border-color: #00FFFF; " +
            "-fx-border-width: 2px; " +
            "-fx-background-radius: 5px; " +
            "-fx-border-radius: 5px; " +
            "-fx-effect: dropshadow(gaussian, #00FFFF, 10, 0.7, 0, 0);";
            
        
        if (selected) {
            button.setStyle(selectedStyle);
            
            
            Label valueLabel = new Label(Integer.toString(value));
            valueLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
            valueLabel.setTextFill(Color.LIGHTCYAN);
            button.setGraphic(valueLabel);
            
            
            revealed[row][col] = true;
        } else if (revealed[row][col]) {
            
            button.setStyle(baseStyle);
            
            Label valueLabel = new Label(Integer.toString(value));
            valueLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
            valueLabel.setTextFill(Color.GRAY);
            button.setGraphic(valueLabel);
        } else {
            
            button.setStyle(baseStyle);
            button.setGraphic(null);
        }
    }
    
    
    private void generateGrid() {
        
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = random.nextInt(9) + 1;
                revealed[i][j] = false;
            }
        }
        
        
        int startRow = random.nextInt(GRID_SIZE);
        int startCol = random.nextInt(GRID_SIZE);
        
        targetPath.add(new int[]{startRow, startCol});
        
        
        for (int i = 1; i < PATH_LENGTH; i++) {
            int[] lastNode = targetPath.get(i - 1);
            int lastRow = lastNode[0];
            int lastCol = lastNode[1];
            
            
            List<int[]> possibleNextNodes = new ArrayList<>();
            
            
            if (lastRow > 0) {
                int[] upNode = new int[]{lastRow - 1, lastCol};
                if (!containsNode(targetPath, upNode)) {
                    possibleNextNodes.add(upNode);
                }
            }
            
            
            if (lastCol < GRID_SIZE - 1) {
                int[] rightNode = new int[]{lastRow, lastCol + 1};
                if (!containsNode(targetPath, rightNode)) {
                    possibleNextNodes.add(rightNode);
                }
            }
            
            
            if (lastRow < GRID_SIZE - 1) {
                int[] downNode = new int[]{lastRow + 1, lastCol};
                if (!containsNode(targetPath, downNode)) {
                    possibleNextNodes.add(downNode);
                }
            }
            
            
            if (lastCol > 0) {
                int[] leftNode = new int[]{lastRow, lastCol - 1};
                if (!containsNode(targetPath, leftNode)) {
                    possibleNextNodes.add(leftNode);
                }
            }
            
            
            if (possibleNextNodes.isEmpty()) {
                break;
            }
            
            
            int[] nextNode = possibleNextNodes.get(random.nextInt(possibleNextNodes.size()));
            targetPath.add(nextNode);
        }
        
        
        log("Generated path of length " + targetPath.size() + ":");
        for (int[] node : targetPath) {
            log("  Node at [" + node[0] + "," + node[1] + "] = " + grid[node[0]][node[1]]);
        }
    }
    
    
    private void handleNodeSelection(int row, int col) {
        
        int[] newNode = new int[]{row, col};
        
        if (containsNode(playerPath, newNode)) {
            
            if (playerPath.get(playerPath.size() - 1)[0] == row && 
                playerPath.get(playerPath.size() - 1)[1] == col) {
                
                
                playerPath.remove(playerPath.size() - 1);
                styleGridButton(gridButtons[row][col], row, col, false);
                
                updatePathStatus();
            } else {
                
                statusLabel.setText("CAN'T REMOVE NODE FROM MIDDLE OF PATH");
                statusLabel.setTextFill(Color.RED);
            }
            return;
        }
        
        
        if (playerPath.isEmpty()) {
            playerPath.add(newNode);
            styleGridButton(gridButtons[row][col], row, col, true);
            statusLabel.setText("NODE ADDED - SELECT ADJACENT NODE");
            statusLabel.setTextFill(Color.LIGHTCYAN);
            updatePathStatus();
            return;
        }
        
        
        int[] lastNode = playerPath.get(playerPath.size() - 1);
        int rowDiff = Math.abs(row - lastNode[0]);
        int colDiff = Math.abs(col - lastNode[1]);
        
        if ((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)) {
            
            playerPath.add(newNode);
            styleGridButton(gridButtons[row][col], row, col, true);
            statusLabel.setText("NODE ADDED - PATH LENGTH: " + playerPath.size() + "/" + PATH_LENGTH);
            statusLabel.setTextFill(Color.LIGHTCYAN);
            
            
            if (playerPath.size() == PATH_LENGTH) {
                statusLabel.setText("PATH COMPLETE - VERIFY TO HACK");
                statusLabel.setTextFill(Color.GREEN);
            }
            
            updatePathStatus();
        } else {
            
            statusLabel.setText("INVALID NODE - MUST BE ADJACENT");
            statusLabel.setTextFill(Color.RED);
        }
    }
    
    
    private void updatePathStatus() {
        Text pathLengthText = (Text) gamePane.lookup("#pathLength");
        if (pathLengthText != null) {
            pathLengthText.setText("PATH LENGTH: " + playerPath.size() + "/" + PATH_LENGTH);
        }
    }
    
    
    private void resetPath() {
        
        for (int[] node : playerPath) {
            styleGridButton(gridButtons[node[0]][node[1]], node[0], node[1], false);
        }
        
        
        playerPath.clear();
        
        
        statusLabel.setText("PATH RESET - SELECT STARTING NODE");
        statusLabel.setTextFill(Color.LIGHTCYAN);
        updatePathStatus();
    }
    
    
    private void verifyPath() {
        if (playerPath.size() != PATH_LENGTH) {
            statusLabel.setText("PATH INCOMPLETE - NEED " + PATH_LENGTH + " NODES");
            statusLabel.setTextFill(Color.RED);
            return;
        }
        
        
        int matchingNodes = 0;
        for (int[] playerNode : playerPath) {
            if (containsNode(targetPath, playerNode)) {
                matchingNodes++;
            }
        }
        
        
        int scorePercent = (matchingNodes * 100) / PATH_LENGTH;
        
        
        if (scorePercent >= 50) {
            statusLabel.setText("HACK SUCCESSFUL - SECURITY BYPASSED");
            statusLabel.setTextFill(Color.GREEN);
            
            
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
            
            
            if (scanEffect != null) {
                scanEffect.stop();
            }
            
            
            completeTask();
        } else {
            statusLabel.setText("HACK FAILED - ONLY " + scorePercent + "% MATCH");
            statusLabel.setTextFill(Color.RED);
            
            
            for (int[] node : playerPath) {
                CyberpunkEffects.styleFailureEffect(gridButtons[node[0]][node[1]]);
            }
            
            
            if (random.nextBoolean()) {
                failTask();
            } else {
                
                Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> resetPath()));
                resetTimeline.play();
            }
        }
    }
    
    
    private void revealPath() {
        
        resetPath();
        
        
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
    
    
    private boolean containsNode(List<int[]> path, int[] node) {
        for (int[] existingNode : path) {
            if (existingNode[0] == node[0] && existingNode[1] == node[1]) {
                return true;
            }
        }
        return false;
    }
    
    
    private void cleanupResources() {
        
        if (scanEffect != null) {
            scanEffect.stop();
        }
    }

    
    @Override
    protected void cleanupTask() {
        super.cleanupTask();
        cleanupResources();
    }
} 
