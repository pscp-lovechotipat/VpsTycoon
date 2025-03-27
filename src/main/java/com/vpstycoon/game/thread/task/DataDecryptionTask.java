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
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Decryption Task - player must decrypt a sequence by solving a simple code.
 */
public class DataDecryptionTask extends GameTask {
    private final int codeLength;
    private int[] code;
    private int[] playerCode;
    private List<Button> digitButtons;
    private List<Label> codeLabels;
    private Label messageLabel;
    private int currentPosition = 0;
    private Timeline scanningEffect;

    /**
     * Constructor for Data Decryption Task
     */
    public DataDecryptionTask() {
        super(
                "Data Decryption Protocol",
                "Decrypt the system by finding the correct numeric sequence",
                "/images/task/decryption_task.png",
                6000, // reward
                10,  // penalty
                2,    // difficulty
                60    // time limit in seconds
        );
        this.codeLength = 4; // Default to 4 digits
    }
    
    /**
     * Constructor with custom code length
     * 
     * @param codeLength Number of digits in the code to decipher
     */
    public DataDecryptionTask(int codeLength) {
        super(
                "Data Decryption Protocol",
                "Decrypt the system by finding the correct numeric sequence",
                "/images/task/decryption_task.png",
                5000 + (codeLength * 500), // Reward scales with difficulty
                5 + (codeLength * 2),  // Penalty scales with difficulty
                Math.min(5, 1 + (codeLength / 2)), // Difficulty based on code length
                Math.min(120, 30 + (codeLength * 10)) // Time limit scales with length
        );
        this.codeLength = Math.min(8, Math.max(3, codeLength)); // Between 3 and 8 digits
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Generate a random code
        generateCode();
        
        // Create a cyberpunk-styled container for the task
        BorderPane decryptionPane = new BorderPane();
        decryptionPane.setPadding(new Insets(30));
        decryptionPane.setMaxWidth(700);
        decryptionPane.setMaxHeight(500);
        decryptionPane.getStyleClass().add("cyberpunk-decryption-panel");
        
        // Add holographic grid effect to decryption pane
        CyberpunkEffects.addHoloGridLines(decryptionPane, 15, 15);
        
        // Create title and description with neon glow
        Text titleText = CyberpunkEffects.createTaskTitle("DATA DECRYPTION PROTOCOL v2.77");
        Text descText = CyberpunkEffects.createTaskDescription(
                "Find the hidden encryption code by testing number combinations.\n" + 
                "Each attempt will give you feedback on your progress.");
        
        // Create a high-tech display for the code
        HBox codeDisplay = createCodeDisplay();
        
        // Feedback message display with cyberpunk styling
        messageLabel = new Label("ENTER DECRYPTION SEQUENCE");
        messageLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        messageLabel.setTextFill(Color.LIGHTCYAN);
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setRadius(10);
        glow.setSpread(0.3);
        messageLabel.setEffect(glow);
        
        // Apply pulse animation to message
        CyberpunkEffects.pulseNode(messageLabel);
        
        // Create cyberpunk-styled number pad
        VBox numberPad = createNumberPad();
        
        // Create hints panel with cyberpunk styling
        VBox hintsPanel = createHintsPanel();
        
        // Scanner effect for the code display
        addScannerEffect(codeDisplay);
        
        // Layout all components with cyberpunk flair
        VBox topSection = new VBox(15);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(20));
        topSection.getChildren().addAll(titleText, descText);
        
        VBox centerSection = new VBox(20);
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setPadding(new Insets(20));
        centerSection.getChildren().addAll(codeDisplay, messageLabel);
        
        HBox controlSection = new HBox(40);
        controlSection.setAlignment(Pos.CENTER);
        controlSection.setPadding(new Insets(20));
        controlSection.getChildren().addAll(numberPad, hintsPanel);
        
        decryptionPane.setTop(topSection);
        decryptionPane.setCenter(centerSection);
        decryptionPane.setBottom(controlSection);
        
        // Add reset button
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET CODE", false);
        resetButton.setOnAction(e -> resetCode());
        
        BorderPane.setMargin(resetButton, new Insets(20, 0, 0, 0));
        BorderPane.setAlignment(resetButton, Pos.CENTER);
        
        // Add animated background for additional cyberpunk effect
        BorderPane backgroundPane = new BorderPane();
        backgroundPane.setCenter(decryptionPane);
        backgroundPane.setBottom(resetButton);
        CyberpunkEffects.addAnimatedBackground(backgroundPane);
        
        // Add the task content to the game pane
        gamePane.getChildren().add(backgroundPane);
    }
    
    /**
     * Create the code display with cyberpunk styling
     */
    private HBox createCodeDisplay() {
        HBox codeDisplay = new HBox(15);
        codeDisplay.setAlignment(Pos.CENTER);
        codeDisplay.setPadding(new Insets(20));
        codeDisplay.setStyle("-fx-background-color: rgba(5, 15, 25, 0.6); -fx-border-color: #00ffff; -fx-border-width: 1;");
        
        codeLabels = new ArrayList<>();
        playerCode = new int[codeLength];
        
        for (int i = 0; i < codeLength; i++) {
            // Create each digit slot with cyberpunk styling
            StackPane digitSlot = new StackPane();
            digitSlot.setMinSize(50, 50);
            
            // Create background rectangle with neon border
            Rectangle background = new Rectangle(50, 50);
            background.setFill(Color.web("#061020"));
            
            // Add inner stroke effect
            InnerShadow innerGlow = new InnerShadow();
            innerGlow.setColor(Color.web("#00FFFF", 0.6));
            innerGlow.setRadius(5);
            innerGlow.setChoke(0.5);
            background.setEffect(innerGlow);
            
            // Create digit label
            Label digitLabel = new Label("_");
            digitLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 24));
            digitLabel.setTextFill(Color.web("#00FFFF"));
            
            // Animate the label with a glow effect
            Glow textGlow = new Glow(0.8);
            digitLabel.setEffect(textGlow);
            
            // Animate the glow effect
            Timeline pulseGlow = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(textGlow.levelProperty(), 0.3)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(textGlow.levelProperty(), 0.8))
            );
            pulseGlow.setAutoReverse(true);
            pulseGlow.setCycleCount(Timeline.INDEFINITE);
            pulseGlow.play();
            
            digitSlot.getChildren().addAll(background, digitLabel);
            codeDisplay.getChildren().add(digitSlot);
            codeLabels.add(digitLabel);
            
            // Initialize player code to -1 (empty)
            playerCode[i] = -1;
        }
        
        return codeDisplay;
    }
    
    /**
     * Create number pad with cyberpunk styling
     */
    private VBox createNumberPad() {
        VBox numberPad = new VBox(10);
        numberPad.setAlignment(Pos.CENTER);
        numberPad.setPadding(new Insets(20));
        numberPad.setStyle("-fx-background-color: rgba(10, 20, 40, 0.6); -fx-border-color: #00ccff; -fx-border-width: 1px;");
        
        // Create digit buttons in a grid
        HBox[] rows = new HBox[4];
        digitButtons = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            rows[i] = new HBox(10);
            rows[i].setAlignment(Pos.CENTER);
        }
        
        // Numbers 1-9
        for (int i = 1; i <= 9; i++) {
            Button digitButton = createDigitButton(i);
            digitButtons.add(digitButton);
            rows[(i-1) / 3].getChildren().add(digitButton);
        }
        
        // Number 0
        Button zeroButton = createDigitButton(0);
        digitButtons.add(zeroButton);
        rows[3].getChildren().add(zeroButton);
        
        // Confirm button
        Button confirmButton = CyberpunkEffects.createCyberpunkButton("CONFIRM", true);
        confirmButton.setPrefSize(100, 50);
        confirmButton.setOnAction(e -> checkCode());
        rows[3].getChildren().add(confirmButton);
        
        // Add all rows to the number pad
        numberPad.getChildren().addAll(rows);
        
        return numberPad;
    }
    
    /**
     * Create a digit button with cyberpunk styling
     */
    private Button createDigitButton(int digit) {
        Button button = new Button(Integer.toString(digit));
        button.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 18));
        button.setPrefSize(50, 50);
        
        // Create gradient for the button
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#000520")),
            new Stop(1, Color.web("#003050"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        
        // Style the button with cyberpunk look
        button.setStyle(
            "-fx-background-color: " + gradient.toString().replace("0x", "#") + "; " +
            "-fx-text-fill: #00ffff; " +
            "-fx-border-color: #00ffff; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 5px; " +
            "-fx-cursor: hand;"
        );
        
        // Add hover effect
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: " + gradient.toString().replace("0x", "#") + "; " +
            "-fx-text-fill: #ffffff; " +
            "-fx-border-color: #80ffff; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 5px; " +
            "-fx-effect: dropshadow(gaussian, #00ffff, 10, 0.5, 0, 0);" +
            "-fx-cursor: hand;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + gradient.toString().replace("0x", "#") + "; " +
            "-fx-text-fill: #00ffff; " +
            "-fx-border-color: #00ffff; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 5px; " +
            "-fx-cursor: hand;"
        ));
        
        // Add click action
        button.setOnAction(e -> enterDigit(digit));
        
        return button;
    }
    
    /**
     * Create hints panel with cyberpunk styling
     */
    private VBox createHintsPanel() {
        VBox hintsPanel = new VBox(10);
        hintsPanel.setAlignment(Pos.CENTER);
        hintsPanel.setPadding(new Insets(10));
        hintsPanel.setStyle("-fx-background-color: rgba(0, 20, 40, 0.6); -fx-border-color: #ff00ff; -fx-border-width: 1px;");
        
        Text hintsTitle = new Text("DECRYPTION HINTS");
        hintsTitle.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        hintsTitle.setFill(Color.web("#ff00ff"));
        
        Text hint1 = new Text("GREEN = Correct digit & position");
        hint1.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 12));
        hint1.setFill(Color.LIGHTGREEN);
        
        Text hint2 = new Text("YELLOW = Correct digit, wrong position");
        hint2.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 12));
        hint2.setFill(Color.YELLOW);
        
        Text hint3 = new Text("RED = Incorrect digit");
        hint3.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 12));
        hint3.setFill(Color.SALMON);
        
        hintsPanel.getChildren().addAll(hintsTitle, hint1, hint2, hint3);
        
        return hintsPanel;
    }
    
    /**
     * Add scanning effect across the code display
     */
    private void addScannerEffect(HBox codeDisplay) {
        Rectangle scanner = new Rectangle(0, 0, 5, 50);
        scanner.setFill(Color.web("#00FFFF", 0.7));
        
        Glow glow = new Glow(0.8);
        scanner.setEffect(glow);
        
        codeDisplay.getChildren().add(scanner);
        
        // Create scanning animation
        scanningEffect = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(scanner.translateXProperty(), -10)
            ),
            new KeyFrame(Duration.seconds(1.5), 
                new KeyValue(scanner.translateXProperty(), codeDisplay.getWidth() + 10)
            )
        );
        
        scanningEffect.setCycleCount(Timeline.INDEFINITE);
        scanningEffect.setAutoReverse(false);
        scanningEffect.play();
    }
    
    /**
     * Generate a random code of specified length
     */
    private void generateCode() {
        code = new int[codeLength];
        for (int i = 0; i < codeLength; i++) {
            code[i] = random.nextInt(10);
        }
        
        // Debug output of code
        StringBuilder codeString = new StringBuilder();
        for (int digit : code) {
            codeString.append(digit);
        }
        log("Generated code: " + codeString.toString());
    }
    
    /**
     * Enter a digit at the current position
     */
    private void enterDigit(int digit) {
        if (currentPosition < codeLength) {
            playerCode[currentPosition] = digit;
            codeLabels.get(currentPosition).setText(Integer.toString(digit));
            codeLabels.get(currentPosition).setTextFill(Color.WHITE);
            currentPosition++;
            
            // Check if all digits are entered
            if (currentPosition == codeLength) {
                messageLabel.setText("PRESS CONFIRM TO VERIFY CODE");
                messageLabel.setTextFill(Color.YELLOW);
            }
        }
    }
    
    /**
     * Reset the entered code
     */
    private void resetCode() {
        currentPosition = 0;
        for (int i = 0; i < codeLength; i++) {
            playerCode[i] = -1;
            codeLabels.get(i).setText("_");
            codeLabels.get(i).setTextFill(Color.web("#00FFFF"));
        }
        messageLabel.setText("ENTER DECRYPTION SEQUENCE");
        messageLabel.setTextFill(Color.LIGHTCYAN);
    }
    
    /**
     * Check if the entered code matches the target code
     */
    private void checkCode() {
        if (currentPosition != codeLength) {
            messageLabel.setText("INCOMPLETE CODE - FILL ALL DIGITS");
            messageLabel.setTextFill(Color.RED);
                return;
            }
            
        // Check if code is correct
        boolean correct = true;
        for (int i = 0; i < codeLength; i++) {
            if (playerCode[i] != code[i]) {
                correct = false;
                break;
            }
        }
        
        if (correct) {
            // Success! Code is correct
            messageLabel.setText("CODE VERIFIED - ACCESS GRANTED");
            messageLabel.setTextFill(Color.LIGHTGREEN);
            
            // Apply success effects
            for (Label label : codeLabels) {
                label.setTextFill(Color.LIGHTGREEN);
                CyberpunkEffects.styleCompletionEffect(label);
            }
            
            // Stop scanning animation
            if (scanningEffect != null) {
                scanningEffect.stop();
            }
            
            // Complete the task
            completeTask();
        } else {
            // Check how many digits are correct and in the right position
            int exactMatches = 0;
            int valueMatches = 0;
            
            // Count digits that are used in the code (for value matches)
            int[] codeDigitCounts = new int[10];
            int[] playerDigitCounts = new int[10];
            
            for (int i = 0; i < codeLength; i++) {
                // Count exact matches
                if (playerCode[i] == code[i]) {
                    exactMatches++;
                    codeLabels.get(i).setTextFill(Color.LIGHTGREEN);
                } else {
                    codeLabels.get(i).setTextFill(Color.RED);
                }
                
                // Count digit occurrences
                codeDigitCounts[code[i]]++;
                playerDigitCounts[playerCode[i]]++;
            }
            
            // Count value matches (correct digit, wrong position)
            for (int i = 0; i < 10; i++) {
                valueMatches += Math.min(codeDigitCounts[i], playerDigitCounts[i]);
            }
            
            // Subtract exact matches from value matches to get only position mismatches
            int positionMismatches = valueMatches - exactMatches;
            
            // Update UI with feedback
            messageLabel.setText(String.format("CORRECT: %d | MISPLACED: %d", exactMatches, positionMismatches));
            
            // Apply color coding to digits based on matches
            for (int i = 0; i < codeLength; i++) {
                if (playerCode[i] == code[i]) {
                    // Exact match - green
                    codeLabels.get(i).setTextFill(Color.LIGHTGREEN);
                } else {
                    // Check if digit exists in code
                    boolean digitExists = false;
                    for (int j = 0; j < codeLength; j++) {
                        if (playerCode[i] == code[j] && playerCode[j] != code[j]) {
                            digitExists = true;
                            break;
                        }
                    }
                    
                    if (digitExists) {
                        // Digit exists but wrong position - yellow
                        codeLabels.get(i).setTextFill(Color.YELLOW);
                    } else {
                        // Digit doesn't exist in code - red
                        codeLabels.get(i).setTextFill(Color.RED);
                    }
                }
            }
            
            // Check if player has reached the attempt limit
            // In this implementation we don't have attempt limits
        }
    }
    
    /**
     * Clean up resources when task is done
     */
    private void cleanupResources() {
        // Stop any animations or timers
        if (scanningEffect != null) {
            scanningEffect.stop();
        }
        
        // Clear references
        code = null;
        playerCode = null;
        digitButtons.clear();
        codeLabels.clear();
        messageLabel = null;
        currentPosition = 0;
    }

    /**
     * ทำความสะอาด resources ทั้งหมดและเตรียมสำหรับ task ถัดไป
     */
    @Override
    protected void cleanupTask() {
        super.cleanupTask();
        cleanupResources();
    }
} 