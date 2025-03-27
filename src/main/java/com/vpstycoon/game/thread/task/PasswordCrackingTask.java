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
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
        
        // Add holographic grid lines
        CyberpunkEffects.addHoloGridLines(hackPanel, 20, 20);
        
        // Create cyberpunk-styled title and description
        Text titleText = CyberpunkEffects.createTaskTitle("SECURITY OVERRIDE v3.0");
        Text descText = CyberpunkEffects.createTaskDescription(
            "Crack the security password by guessing characters.\n" +
            "Each correct guess reveals more of the password."
        );
        
        // Create the password display
        HBox passwordDisplay = createPasswordDisplay();
        
        // Add a scanning effect to the password display
        addScanningEffect(passwordDisplay);
        
        // Create feedback message with cyberpunk styling
        messageLabel = new Label("ENTER CHARACTERS TO CRACK THE PASSWORD");
        messageLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        messageLabel.setTextFill(Color.LIGHTCYAN);
        
        // Add glow effect to message
        Glow messageGlow = new Glow(0.5);
        messageLabel.setEffect(messageGlow);
        
        // Create attempts display
        attemptsLabel = CyberpunkEffects.createGlowingLabel("ATTEMPTS REMAINING: " + attemptsRemaining, "#FF00FF");
        
        // Input area for password guessing
        HBox inputArea = createInputArea();
        
        // Digital lock visualization
        StackPane lockVisualization = createLockVisualization();
        
        // Arrange all components
        VBox topSection = new VBox(15);
        topSection.setAlignment(Pos.CENTER);
        topSection.getChildren().addAll(titleText, descText);
        
        VBox centerSection = new VBox(20);
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setPadding(new Insets(30, 0, 30, 0));
        centerSection.getChildren().addAll(passwordDisplay, messageLabel, attemptsLabel);
        
        VBox bottomSection = new VBox(25);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.getChildren().addAll(lockVisualization, inputArea);
        
        hackPanel.setTop(topSection);
        hackPanel.setCenter(centerSection);
        hackPanel.setBottom(bottomSection);
        
        // Add the task content to the game pane
        gamePane.getChildren().add(hackPanel);
        
        // Add animated background glitch effects
        CyberpunkEffects.addAnimatedBackground(gamePane);
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
            charLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 36));
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
        charInput.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 16));
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
        Button guessButton = CyberpunkEffects.createCyberpunkButton("GUESS", true);
        guessButton.setOnAction(e -> {
            if (!charInput.getText().isEmpty()) {
                guessCharacter(charInput.getText().charAt(0));
                charInput.clear();
                charInput.requestFocus();
            }
        });
        
        // Reveal button (for troubleshooting)
        Button revealButton = CyberpunkEffects.createCyberpunkButton("DECRYPT ALL", false);
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
                CyberpunkEffects.styleCompletionEffect(passwordLabels.get(i));
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
            CyberpunkEffects.styleCompletionEffect(label);
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