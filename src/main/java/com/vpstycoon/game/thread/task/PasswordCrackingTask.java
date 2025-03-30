package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


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
                7000, 
                25,  
                3,    
                60    
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        try {
            
            targetPassword = POSSIBLE_PASSWORDS[random.nextInt(POSSIBLE_PASSWORDS.length)];
            currentGuess = new StringBuilder();
            for (int i = 0; i < targetPassword.length(); i++) {
                currentGuess.append("_");
            }
            
            log("Selected password: " + targetPassword);
            
            
            BorderPane hackPanel = new BorderPane();
            hackPanel.setPadding(new Insets(10));
            hackPanel.setPrefWidth(680);
            hackPanel.setPrefHeight(400);
            hackPanel.setMaxWidth(680);
            hackPanel.setMaxHeight(400);
            hackPanel.setMinWidth(680);
            hackPanel.setMinHeight(400);
            
            
            hackPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0A0A2A, #151540);" +
                "-fx-border-color: #FF00FF;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 5px;"
            );
            
            
            addHoloGridLines(hackPanel, 20, 20);
            
            
            Text titleText = createStyledTitle("SECURITY OVERRIDE v3.0");
            Text descText = createStyledDescription(
                "Crack the security password by guessing characters.\n" +
                "Each correct guess reveals more of the password."
            );
            
            
            HBox passwordDisplay = createPasswordDisplay();
            
            
            addScanningEffect(passwordDisplay);
            
            
            messageLabel = new Label("ENTER CHARACTERS TO CRACK THE PASSWORD");
            messageLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            messageLabel.setTextFill(Color.LIGHTCYAN);
            
            
            Glow messageGlow = new Glow(0.5);
            messageLabel.setEffect(messageGlow);
            
            
            attemptsLabel = new Label("ATTEMPTS REMAINING: " + attemptsRemaining);
            attemptsLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            attemptsLabel.setTextFill(Color.web("#FF00FF"));
            
            DropShadow labelGlow = new DropShadow();
            labelGlow.setColor(Color.web("#FF00FF"));
            labelGlow.setRadius(10);
            labelGlow.setSpread(0.5);
            attemptsLabel.setEffect(labelGlow);
            
            
            HBox inputArea = createInputArea();
            
            
            StackPane lockVisualization = createLockVisualization();
            
            
            VBox topSection = new VBox(8);
            topSection.setAlignment(Pos.CENTER);
            topSection.setPadding(new Insets(8));
            topSection.setMaxHeight(110);
            topSection.getChildren().addAll(titleText, descText);
            
            VBox centerSection = new VBox(10);
            centerSection.setAlignment(Pos.CENTER);
            centerSection.setPadding(new Insets(10));
            centerSection.setMaxHeight(160);
            centerSection.getChildren().addAll(passwordDisplay, messageLabel, attemptsLabel);
            
            VBox bottomSection = new VBox(15);
            bottomSection.setAlignment(Pos.CENTER);
            bottomSection.setPadding(new Insets(10));
            bottomSection.setMaxHeight(160);
            bottomSection.getChildren().addAll(lockVisualization, inputArea);
            
            
            BorderPane.setAlignment(topSection, Pos.TOP_CENTER);
            BorderPane.setAlignment(centerSection, Pos.CENTER);
            BorderPane.setAlignment(bottomSection, Pos.BOTTOM_CENTER);
            
            hackPanel.setTop(topSection);
            hackPanel.setCenter(centerSection);
            hackPanel.setBottom(bottomSection);
            
            
            gamePane.getChildren().clear();
            
            
            StackPane hackPanelContainer = new StackPane();
            hackPanelContainer.setAlignment(Pos.CENTER);
            hackPanelContainer.getChildren().add(hackPanel);
            
            
            hackPanelContainer.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            hackPanelContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            hackPanelContainer.setPrefSize(680, 400);
            
            
            gamePane.getChildren().add(hackPanelContainer);
            
            
            
            
            
            log("PasswordCrackingTask initialized successfully");
        } catch (Exception e) {
            log("Error initializing PasswordCrackingTask: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private Text createStyledTitle(String title) {
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#00FFFF"));
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00FFFF"));
        glow.setRadius(10);
        glow.setSpread(0.2);
        titleText.setEffect(glow);
        
        return titleText;
    }
    
    
    private Text createStyledDescription(String description) {
        Text descText = new Text(description);
        descText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 16));
        descText.setFill(Color.web("#CCECFF"));
        
        
        Glow glow = new Glow(0.3);
        descText.setEffect(glow);
        
        return descText;
    }
    
    
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
        
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        
        return button;
    }
    
    
    private void addHoloGridLines(Pane pane, int hSpacing, int vSpacing) {
        double width = pane.getPrefWidth();
        double height = pane.getPrefHeight();
        
        
        for (int y = 0; y < height; y += vSpacing) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.web("#FF00FF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
        
        
        for (int x = 0; x < width; x += hSpacing) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.web("#FF00FF", 0.2));
            line.setStrokeWidth(0.5);
            pane.getChildren().add(line);
        }
    }
    
    
    private HBox createPasswordDisplay() {
        HBox passwordDisplay = new HBox(10);
        passwordDisplay.setAlignment(Pos.CENTER);
        passwordDisplay.setPadding(new Insets(10));
        passwordDisplay.setStyle("-fx-background-color: rgba(0, 10, 30, 0.6); -fx-border-color: #FF00FF; -fx-border-width: 1px;");
        passwordDisplay.setMaxWidth(600);
        
        passwordLabels = new ArrayList<>();
        
        
        int charWidth = Math.max(30, Math.min(40, 200 / targetPassword.length()));
        int fontSize = Math.max(24, Math.min(36, 100 / targetPassword.length()));
        
        for (int i = 0; i < targetPassword.length(); i++) {
            Label charLabel = new Label("_");
            charLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, fontSize));
            charLabel.setTextFill(Color.web("#FF00FF"));
            charLabel.setMinWidth(charWidth);
            charLabel.setAlignment(Pos.CENTER);
            
            
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#FF00FF"));
            glow.setRadius(10);
            glow.setSpread(0.4);
            charLabel.setEffect(glow);
            
            
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
    
    
    private HBox createInputArea() {
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(10));
        inputArea.setMaxWidth(600);
        
        
        TextField charInput = new TextField();
        charInput.setPromptText("ENTER CHARACTER");
        charInput.setFont(Font.font("Monospaced", FontWeight.NORMAL, 14));
        charInput.setStyle(
            "-fx-background-color: rgba(0, 10, 30, 0.8);" +
            "-fx-text-fill: #00FFFF;" +
            "-fx-border-color: #00FFFF;" +
            "-fx-border-width: 1px;" +
            "-fx-max-width: 160px;" +
            "-fx-pref-width: 160px;"
        );
        
        
        charInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                charInput.setText(newValue.substring(0, 1).toUpperCase());
            } else if (!newValue.isEmpty()) {
                charInput.setText(newValue.toUpperCase());
            }
        });
        
        
        Button guessButton = createStyledButton("GUESS", true);
        guessButton.setOnAction(e -> {
            if (!charInput.getText().isEmpty()) {
                guessCharacter(charInput.getText().charAt(0));
                charInput.clear();
                charInput.requestFocus();
            }
        });
        
        
        Button revealButton = createStyledButton("DECRYPT ALL", false);
        revealButton.setOnAction(e -> revealPassword());
        
        inputArea.getChildren().addAll(charInput, guessButton, revealButton);
        return inputArea;
    }
    
    
    private StackPane createLockVisualization() {
        StackPane lockPane = new StackPane();
        lockPane.setMinHeight(80);
        lockPane.setMaxHeight(80);
        
        
        Rectangle lockBase = new Rectangle(180, 80);
        lockBase.setArcWidth(20);
        lockBase.setArcHeight(20);
        lockBase.setFill(Color.web("#151530"));
        lockBase.setStroke(Color.web("#FF00FF"));
        lockBase.setStrokeWidth(2);
        
        
        VBox circuitPattern = new VBox(3);
        circuitPattern.setMaxWidth(160);
        circuitPattern.setMaxHeight(60);
        
        for (int i = 0; i < 3; i++) {
            HBox row = new HBox(5);
            for (int j = 0; j < 6; j++) {
                Line circuit = new Line(0, 0, random.nextInt(10) + 5, 0);
                circuit.setStroke(Color.web("#FF00FF", 0.5 + random.nextDouble() * 0.5));
                circuit.setStrokeWidth(1);
                row.getChildren().add(circuit);
            }
            circuitPattern.getChildren().add(row);
        }
        
        
        Text lockStatus = new Text("LOCKED");
        lockStatus.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        lockStatus.setFill(Color.web("#FF0000"));
        
        
        Glow statusGlow = new Glow(0.8);
        lockStatus.setEffect(statusGlow);
        
        
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
    
    
    private void addScanningEffect(HBox passwordDisplay) {
        Rectangle scanner = new Rectangle(3, 60);
        scanner.setFill(Color.web("#00FFFF", 0.7));
        
        Glow glow = new Glow(0.8);
        scanner.setEffect(glow);
        
        passwordDisplay.getChildren().add(scanner);
        
        
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
    
    
    private void guessCharacter(char guess) {
        if (attemptsRemaining <= 0) {
            messageLabel.setText("NO ATTEMPTS REMAINING. ACCESS DENIED.");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        
        char upperGuess = Character.toUpperCase(guess);
        
        
        boolean found = false;
        boolean allRevealed = true;
        
        for (int i = 0; i < targetPassword.length(); i++) {
            if (targetPassword.charAt(i) == upperGuess) {
                
                currentGuess.setCharAt(i, upperGuess);
                passwordLabels.get(i).setText(String.valueOf(upperGuess));
                found = true;
                
                
                styleCompletionEffect(passwordLabels.get(i));
                passwordLabels.get(i).setTextFill(Color.LIGHTGREEN);
            }
            
            
            if (currentGuess.charAt(i) == '_') {
                allRevealed = false;
            }
        }
        
        
        if (!found) {
            attemptsRemaining--;
            attemptsLabel.setText("ATTEMPTS REMAINING: " + attemptsRemaining);
            
            
            messageLabel.setText("CHARACTER NOT FOUND. TRY AGAIN.");
            messageLabel.setTextFill(Color.RED);
            
            
            if (attemptsRemaining > 0) {
                
                DropShadow errorGlow = new DropShadow();
                errorGlow.setColor(Color.RED);
                errorGlow.setRadius(20);
                errorGlow.setSpread(0.5);
                
                Node targetNode = messageLabel;
                targetNode.setEffect(errorGlow);
                
                Timeline flashTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(errorGlow.radiusProperty(), 20)),
                    new KeyFrame(Duration.millis(300), new KeyValue(errorGlow.radiusProperty(), 5)),
                    new KeyFrame(Duration.millis(600), new KeyValue(errorGlow.radiusProperty(), 20))
                );
                flashTimeline.setCycleCount(2);
                flashTimeline.setOnFinished(e -> targetNode.setEffect(new Glow(0.5)));
                flashTimeline.play();
            }
            
            
            if (attemptsRemaining <= 0) {
                messageLabel.setText("ACCESS DENIED. SECURITY LOCKOUT INITIATED.");
                failTask();
            }
        } else {
            messageLabel.setText("CHARACTER FOUND! CONTINUE HACKING.");
            messageLabel.setTextFill(Color.LIGHTGREEN);
            
            
            if (allRevealed) {
                passwordCompletedSuccessfully();
            }
        }
    }
    
    
    private void passwordCompletedSuccessfully() {
        messageLabel.setText("PASSWORD CRACKED! ACCESS GRANTED.");
        messageLabel.setTextFill(Color.LIGHTGREEN);
        
        
        if (scanEffect != null) {
            scanEffect.stop();
        }
        
        
        for (Label label : passwordLabels) {
            label.setTextFill(Color.LIGHTGREEN);
            styleCompletionEffect(label);
        }
        
        
        completeTask();
    }
    
    
    private void revealPassword() {
        for (int i = 0; i < targetPassword.length(); i++) {
            passwordLabels.get(i).setText(String.valueOf(targetPassword.charAt(i)));
            currentGuess.setCharAt(i, targetPassword.charAt(i));
        }
        passwordCompletedSuccessfully();
    }
    
    
    private void styleCompletionEffect(Node node) {
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.LIGHTGREEN);
        glow.setRadius(15);
        glow.setSpread(0.7);
        node.setEffect(glow);
        
        
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 10)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(glow.radiusProperty(), 20)),
            new KeyFrame(Duration.seconds(1.0), new KeyValue(glow.radiusProperty(), 10))
        );
        pulse.setCycleCount(3);
        pulse.play();
    }
    
    
    private void cleanupResources() {
        
        if (scanEffect != null) {
            scanEffect.stop();
        }
        
        
        targetPassword = null;
        currentGuess = null;
        messageLabel = null;
        passwordLabels.clear();
        attemptsLabel = null;
    }

    
    @Override
    protected void cleanupTask() {
        super.cleanupTask();
        cleanupResources();
    }
}
