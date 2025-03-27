package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Password Cracking Task - Player must crack a password by guessing characters
 * and receiving feedback on each attempt
 */
public class PasswordCrackingTask extends GameTask {
    private static final String[] POSSIBLE_PASSWORDS = {
        "ACCESS", "SECURE", "SYSTEM", "CIPHER", "CRYPTO",
        "DAEMON", "PORTAL", "MATRIX", "NEURAL", "BINARY",
        "CODEX", "VECTOR", "PROXY", "KERNEL", "NEXUS"
    };
    
    private String targetPassword;
    private StringBuilder currentGuess;
    private int attemptsRemaining = 5;
    private Label messageLabel;
    private List<Label> passwordLabels;
    private Label attemptsLabel;
    private Timeline scanEffect;

    public PasswordCrackingTask() {
        super(
                "Password Override",
                "Bypass security by cracking the digital lock",
                "/images/task/password_task.png",
                7000, // reward
                25,  // penalty (0.25 * 100)
                3,    // difficulty
                60    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        try {
            // Choose a random password from the list
            targetPassword = POSSIBLE_PASSWORDS[random.nextInt(POSSIBLE_PASSWORDS.length)];
            currentGuess = new StringBuilder();
            for (int i = 0; i < targetPassword.length(); i++) {
                currentGuess.append("_");
            }
            
            log("Selected password: " + targetPassword);
            
            // Create the main panel with cyberpunk styling
            BorderPane hackPanel = new BorderPane();
            hackPanel.setPadding(new Insets(20));
            hackPanel.setMaxWidth(700);
            hackPanel.setMaxHeight(500);
            
            // Add cyberpunk styling
            hackPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0A0A2A, #151540);" +
                "-fx-border-color: #FF00FF;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 5px;"
            );
            
            // Add holographic grid lines manually
            addHoloGridLines(hackPanel, 20, 20);
            
            // Create title and description
            Text titleText = createStyledTitle("SECURITY OVERRIDE v3.0");
            Text descText = createStyledDescription(
                "Crack the security password by guessing characters.\n" +
                "Each correct guess reveals more of the password."
            );
            
            // Create the password display
            HBox passwordDisplay = createPasswordDisplay();
            
            // Add a scanning effect to the password display
            addScanningEffect(passwordDisplay);
            
            // Create feedback message
            messageLabel = new Label("ENTER CHARACTERS TO CRACK THE PASSWORD");
            messageLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
            messageLabel.setTextFill(Color.LIGHTCYAN);
            
            // Add glow effect to message
            Glow messageGlow = new Glow(0.5);
            messageLabel.setEffect(messageGlow);
            
            // Create attempts display
            attemptsLabel = new Label("ATTEMPTS REMAINING: " + attemptsRemaining);
            attemptsLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
            attemptsLabel.setTextFill(Color.web("#FF00FF"));
            
            DropShadow labelGlow = new DropShadow();
            labelGlow.setColor(Color.web("#FF00FF"));
            labelGlow.setRadius(10);
            labelGlow.setSpread(0.5);
            attemptsLabel.setEffect(labelGlow);
            
            // Input area for password guessing
            HBox inputArea = createInputArea();
            
            // Digital lock visualization
            StackPane lockVisualization = createLockVisualization();
            
            // Arrange all components
            VBox topSection = new VBox(15);
            topSection.setAlignment(Pos.CENTER);
            topSection.setPadding(new Insets(20));
            topSection.getChildren().addAll(titleText, descText);
            
            VBox centerSection = new VBox(20);
            centerSection.setAlignment(Pos.CENTER);
            centerSection.setPadding(new Insets(20));
            centerSection.getChildren().addAll(passwordDisplay, messageLabel, attemptsLabel);
            
            VBox bottomSection = new VBox(25);
            bottomSection.setAlignment(Pos.CENTER);
            bottomSection.setPadding(new Insets(20));
            bottomSection.getChildren().addAll(lockVisualization, inputArea);
            
            hackPanel.setTop(topSection);
            hackPanel.setCenter(centerSection);
            hackPanel.setBottom(bottomSection);
            
            // Add the task content to the game pane
            gamePane.getChildren().add(hackPanel);
            
            // Add background
            addSimpleBackground(gamePane);
            
            log("PasswordCrackingTask initialized successfully");
        } catch (Exception e) {
            log("Error initializing PasswordCrackingTask: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a styled title text
     */
    private Text createStyledTitle(String title) {
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#00FFFF"));
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00FFFF"));
        glow.setRadius(15);
        glow.setSpread(0.7);
        titleText.setEffect(glow);
        
        return titleText;
    }
    
    /**
     * Create a styled description text
     */
    private Text createStyledDescription(String description) {
        Text descText = new Text(description);
        descText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 16));
        descText.setFill(Color.web("#CCECFF"));
        
        // Add a subtle glow effect
        Glow glow = new Glow(0.3);
        descText.setEffect(glow);
        
        return descText;
    }
    
    /**
     * Create a styled button
     */
    private Button createStyledButton(String text, boolean primary) {
        Button button = new Button(text);
        button.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        
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
     * Add grid lines to create a holographic effect
     */
    private void addHoloGridLines(Pane pane, int hSpacing, int vSpacing) {
        double width = pane.getPrefWidth();
        double height = pane.getPrefHeight();
        
        // Add horizontal lines
        for (int y = 0; y < height; y += vSpacing) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.web("#FF00FF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
        
        // Add vertical lines
        for (int x = 0; x < width; x += hSpacing) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.web("#FF00FF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
    }
    
    /**
     * Add a simple animated background
     */
    private void addSimpleBackground(Pane pane) {
        Rectangle background = new Rectangle(pane.getWidth(), pane.getHeight());
        background.widthProperty().bind(pane.widthProperty());
        background.heightProperty().bind(pane.heightProperty());
        background.setFill(Color.web("#0A0A2A", 0.3));
        
        if (pane.getChildren().size() > 0) {
            pane.getChildren().add(0, background);
        } else {
            pane.getChildren().add(background);
        }
    }
    
    /**
     * Create the password display with cyberpunk styling
     */
    private HBox createPasswordDisplay() {
        HBox passwordDisplay = new HBox(15);
        passwordDisplay.setAlignment(Pos.CENTER);
        passwordDisplay.setPadding(new Insets(20));
        passwordDisplay.setStyle("-fx-background-color: rgba(0, 10, 30, 0.6); -fx-border-color: #FF00FF; -fx-border-width: 1px;");
        
        passwordLabels = new ArrayList<>();
        
        for (int i = 0; i < targetPassword.length(); i++) {
            Label charLabel = new Label("_");
            charLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
            charLabel.setTextFill(Color.web("#FF00FF"));
            charLabel.setMinWidth(40);
            charLabel.setAlignment(Pos.CENTER);
            
            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FF00FF"));
            glow.setRadius(10);
            glow.setSpread(0.4);
            charLabel.setEffect(glow);
            
            // Animate label
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 5)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.radiusProperty(), 15))
            );
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Timeline.INDEFINITE);
            pulse.play();
            
            passwordLabels.add(charLabel);
            passwordDisplay.getChildren().add(charLabel);
        }
        
        return passwordDisplay;
    }
    
    /**
     * Create input area for character guessing
     */
    private HBox createInputArea() {
        HBox inputArea = new HBox(15);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(20));
        
        // Text field for character input
        TextField charInput = new TextField();
        charInput.setPromptText("ENTER CHARACTER");
        charInput.setFont(Font.font("Monospaced", FontWeight.NORMAL, 16));
        charInput.setStyle(
            "-fx-background-color: rgba(0, 10, 30, 0.8);" +
            "-fx-text-fill: #00FFFF;" +
            "-fx-border-color: #00FFFF;" +
            "-fx-border-width: 1px;" +
            "-fx-max-width: 200px;"
        );
        
        // Limit to one character
        charInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                charInput.setText(newValue.substring(0, 1).toUpperCase());
            } else if (!newValue.isEmpty()) {
                charInput.setText(newValue.toUpperCase());
            }
        });
        
        // Guess button
        Button guessButton = createStyledButton("GUESS", true);
        guessButton.setOnAction(e -> {
            if (!charInput.getText().isEmpty()) {
                guessCharacter(charInput.getText().charAt(0));
                charInput.clear();
                charInput.requestFocus();
            }
        });
        
        // Reveal button (for troubleshooting)
        Button revealButton = createStyledButton("DECRYPT ALL", false);
        revealButton.setOnAction(e -> revealPassword());
        
        inputArea.getChildren().addAll(charInput, guessButton, revealButton);
        return inputArea;
    }
    
    /**
     * Create a cyberpunk lock visualization
     */
    private StackPane createLockVisualization() {
        StackPane lockPane = new StackPane();
        lockPane.setMinHeight(100);
        
        // Create the lock circle
        Rectangle lockBase = new Rectangle(200, 100);
        lockBase.setArcWidth(20);
        lockBase.setArcHeight(20);
        lockBase.setFill(Color.web("#151530"));
        lockBase.setStroke(Color.web("#FF00FF"));
        lockBase.setStrokeWidth(2);
        
        // Create digital circuitry pattern
        VBox circuitPattern = new VBox(5);
        circuitPattern.setMaxWidth(180);
        circuitPattern.setMaxHeight(80);
        
        for (int i = 0; i < 4; i++) {
            HBox row = new HBox(5);
            for (int j = 0; j < 8; j++) {
                Line circuit = new Line(0, 0, random.nextInt(15) + 5, 0);
                circuit.setStroke(Color.web("#FF00FF", 0.5 + random.nextDouble() * 0.5));
                circuit.setStrokeWidth(1);
                row.getChildren().add(circuit);
            }
            circuitPattern.getChildren().add(row);
        }
        
        // Add lock status text
        Text lockStatus = new Text("LOCKED");
        lockStatus.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 18));
        lockStatus.setFill(Color.web("#FF0000"));
        
        // Add glow effect to status
        Glow statusGlow = new Glow(0.8);
        lockStatus.setEffect(statusGlow);
        
        // Pulsing animation for status
        Timeline statusPulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(statusGlow.levelProperty(), 0.3)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(statusGlow.levelProperty(), 0.8))
        );
        statusPulse.setAutoReverse(true);
        statusPulse.setCycleCount(Timeline.INDEFINITE);
        statusPulse.play();
        
        lockPane.getChildren().addAll(lockBase, circuitPattern, lockStatus);
        return lockPane;
    }
    
    /**
     * Add a scanning effect to the password display
     */
    private void addScanningEffect(HBox passwordDisplay) {
        Rectangle scanner = new Rectangle(3, 60);
        scanner.setFill(Color.web("#00FFFF", 0.7));
        
        Glow glow = new Glow(0.8);
        scanner.setEffect(glow);
        
        passwordDisplay.getChildren().add(scanner);
        
        // Create scanning animation
        scanEffect = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(scanner.translateXProperty(), -scanner.getWidth() / 2)
            ),
            new KeyFrame(Duration.seconds(2), 
                new KeyValue(scanner.translateXProperty(), passwordDisplay.getWidth())
            )
        );
        
        scanEffect.setCycleCount(Timeline.INDEFINITE);
        scanEffect.setAutoReverse(false);
        scanEffect.play();
    }
    
    /**
     * Process a character guess
     */
    private void guessCharacter(char guess) {
        if (attemptsRemaining <= 0) {
            messageLabel.setText("NO ATTEMPTS REMAINING. ACCESS DENIED.");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Convert to uppercase for comparison
        char upperGuess = Character.toUpperCase(guess);
        
        // Check if the character is in the password
        boolean found = false;
        boolean allRevealed = true;
        
        for (int i = 0; i < targetPassword.length(); i++) {
            if (targetPassword.charAt(i) == upperGuess) {
                // Correct guess
                currentGuess.setCharAt(i, upperGuess);
                passwordLabels.get(i).setText(String.valueOf(upperGuess));
                found = true;
                
                // Add completion effect to the character
                styleCompletionEffect(passwordLabels.get(i));
                passwordLabels.get(i).setTextFill(Color.LIGHTGREEN);
            }
            
            // Check if all characters are revealed
            if (currentGuess.charAt(i) == '_') {
                allRevealed = false;
            }
        }
        
        // Update attempts
        if (!found) {
            attemptsRemaining--;
            attemptsLabel.setText("ATTEMPTS REMAINING: " + attemptsRemaining);
            
            // Red flash for wrong guess
            messageLabel.setText("CHARACTER NOT FOUND. TRY AGAIN.");
            messageLabel.setTextFill(Color.RED);
            
            // Shake animation for wrong guess
            Timeline shakeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(gamePane.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new KeyValue(gamePane.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(100), new KeyValue(gamePane.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(150), new KeyValue(gamePane.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new KeyValue(gamePane.translateXProperty(), 0))
            );
            shakeTimeline.play();
            
            // Check if out of attempts
            if (attemptsRemaining <= 0) {
                messageLabel.setText("ACCESS DENIED. SECURITY LOCKOUT INITIATED.");
                failTask();
            }
        } else {
            messageLabel.setText("CHARACTER FOUND! CONTINUE HACKING.");
            messageLabel.setTextFill(Color.LIGHTGREEN);
            
            // Check if all characters are revealed
            if (allRevealed) {
                passwordCompletedSuccessfully();
            }
        }
    }
    
    /**
     * Handle successful password completion
     */
    private void passwordCompletedSuccessfully() {
        messageLabel.setText("PASSWORD CRACKED! ACCESS GRANTED.");
        messageLabel.setTextFill(Color.LIGHTGREEN);
        
        // Stop scanning animation
        if (scanEffect != null) {
            scanEffect.stop();
        }
        
        // Success effects for all password characters
        for (Label label : passwordLabels) {
            label.setTextFill(Color.LIGHTGREEN);
            styleCompletionEffect(label);
        }
        
        // Complete the task
        completeTask();
    }
    
    /**
     * Reveal the full password (debug feature)
     */
    private void revealPassword() {
        for (int i = 0; i < targetPassword.length(); i++) {
            passwordLabels.get(i).setText(String.valueOf(targetPassword.charAt(i)));
            currentGuess.setCharAt(i, targetPassword.charAt(i));
        }
        passwordCompletedSuccessfully();
    }
    
    /**
     * Apply a completion effect to a node
     */
    private void styleCompletionEffect(Node node) {
        // Green glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(Color.LIGHTGREEN);
        glow.setRadius(15);
        glow.setSpread(0.7);
        node.setEffect(glow);
        
        // Pulsing animation
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 10)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(glow.radiusProperty(), 20)),
            new KeyFrame(Duration.seconds(1.0), new KeyValue(glow.radiusProperty(), 10))
        );
        pulse.setCycleCount(3);
        pulse.play();
    }
    
    /**
     * Clean up resources when task is done
     */
    private void cleanupResources() {
        // Stop any animations or timers
        if (scanEffect != null) {
            scanEffect.stop();
        }
        
        // Clear references
        targetPassword = null;
        currentGuess = null;
        messageLabel = null;
        passwordLabels.clear();
        attemptsLabel = null;
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