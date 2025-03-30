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


public class DataDecryptionTask extends GameTask {
    private final int codeLength;
    private int[] code;
    private int[] playerCode;
    private List<Button> digitButtons;
    private List<Label> codeLabels;
    private Label messageLabel;
    private int currentPosition = 0;
    private Timeline scanningEffect;

    
    public DataDecryptionTask() {
        super(
                "Data Decryption Protocol",
                "Decrypt the system by finding the correct numeric sequence",
                "/images/task/decryption_task.png",
                6000, 
                10,  
                2,    
                60    
        );
        this.codeLength = 4; 
    }
    
    
    public DataDecryptionTask(int codeLength) {
        super(
                "Data Decryption Protocol",
                "Decrypt the system by finding the correct numeric sequence",
                "/images/task/decryption_task.png",
                5000 + (codeLength * 500), 
                5 + (codeLength * 2),  
                Math.min(5, 1 + (codeLength / 2)), 
                Math.min(120, 30 + (codeLength * 10)) 
        );
        this.codeLength = Math.min(8, Math.max(3, codeLength)); 
    }

    @Override
    protected void initializeTaskSpecifics() {
        
        generateCode();
        
        
        BorderPane decryptionPane = new BorderPane();
        decryptionPane.setPadding(new Insets(20)); 
        decryptionPane.setMaxWidth(700);
        decryptionPane.setMaxHeight(300); 
        decryptionPane.getStyleClass().add("cyberpunk-decryption-panel");
        
        
        CyberpunkEffects.addHoloGridLines(decryptionPane, 15, 15);
        
        
        Text titleText = CyberpunkEffects.createTaskTitle("DATA DECRYPTION PROTOCOL v2.77");
        Text descText = CyberpunkEffects.createTaskDescription(
                "Find the hidden encryption code by testing number combinations.\n" + 
                "Each attempt will give you feedback on your progress.");
        
        
        HBox codeDisplay = createCodeDisplay();
        
        
        messageLabel = new Label("ENTER DECRYPTION SEQUENCE");
        messageLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14)); 
        messageLabel.setTextFill(Color.LIGHTCYAN);
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setRadius(10);
        glow.setSpread(0.3);
        messageLabel.setEffect(glow);
        
        
        CyberpunkEffects.pulseNode(messageLabel);
        
        
        VBox numberPad = createNumberPad();
        
        
        VBox hintsPanel = createHintsPanel();
        
        
        addScannerEffect(codeDisplay);
        
        
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET CODE", false);
        resetButton.setOnAction(e -> resetCode());
        
        
        VBox topSection = new VBox(10); 
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(10)); 
        topSection.getChildren().addAll(titleText, descText);
        
        VBox centerSection = new VBox(10); 
        centerSection.setAlignment(Pos.CENTER);
        centerSection.setPadding(new Insets(10)); 
        centerSection.getChildren().addAll(codeDisplay, messageLabel);
        
        
        HBox inputSection = new HBox(10);
        inputSection.setAlignment(Pos.CENTER_LEFT);
        
        
        VBox resetButtonBox = new VBox();
        resetButtonBox.setAlignment(Pos.CENTER);
        resetButtonBox.getChildren().add(resetButton);
        
        inputSection.getChildren().addAll(numberPad, resetButtonBox);
        
        HBox controlSection = new HBox(20); 
        controlSection.setAlignment(Pos.CENTER);
        controlSection.setPadding(new Insets(10)); 
        controlSection.getChildren().addAll(inputSection, hintsPanel);
        
        decryptionPane.setTop(topSection);
        decryptionPane.setCenter(centerSection);
        decryptionPane.setBottom(controlSection);
        
        
        BorderPane backgroundPane = new BorderPane();
        backgroundPane.setCenter(decryptionPane);
        CyberpunkEffects.addAnimatedBackground(backgroundPane);
        
        
        gamePane.getChildren().add(backgroundPane);
    }
    
    
    private HBox createCodeDisplay() {
        HBox codeDisplay = new HBox(10); 
        codeDisplay.setAlignment(Pos.CENTER);
        codeDisplay.setPadding(new Insets(10)); 
        codeDisplay.setStyle("-fx-background-color: rgba(5, 15, 25, 0.6); -fx-border-color: #00ffff; -fx-border-width: 1;");
        
        codeLabels = new ArrayList<>();
        playerCode = new int[codeLength];
        
        
        int digitSize = Math.min(50, 200 / codeLength);
        
        for (int i = 0; i < codeLength; i++) {
            
            StackPane digitSlot = new StackPane();
            digitSlot.setMinSize(digitSize, digitSize);
            
            
            Rectangle background = new Rectangle(digitSize, digitSize);
            background.setFill(Color.web("#061020"));
            
            
            InnerShadow innerGlow = new InnerShadow();
            innerGlow.setColor(Color.web("#00FFFF", 0.6));
            innerGlow.setRadius(5);
            innerGlow.setChoke(0.5);
            background.setEffect(innerGlow);
            
            
            Label digitLabel = new Label("_");
            digitLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 
                Math.min(24, 18 + (digitSize / 10)))); 
            digitLabel.setTextFill(Color.web("#00FFFF"));
            
            
            Glow textGlow = new Glow(0.8);
            digitLabel.setEffect(textGlow);
            
            
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
            
            
            playerCode[i] = -1;
        }
        
        return codeDisplay;
    }
    
    
    private VBox createNumberPad() {
        VBox numberPad = new VBox(5); 
        numberPad.setAlignment(Pos.CENTER);
        numberPad.setPadding(new Insets(10)); 
        numberPad.setStyle("-fx-background-color: rgba(10, 20, 40, 0.6); -fx-border-color: #00ccff; -fx-border-width: 1px;");
        
        
        HBox[] rows = new HBox[4];
        digitButtons = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            rows[i] = new HBox(5); 
            rows[i].setAlignment(Pos.CENTER);
        }
        
        
        for (int i = 1; i <= 9; i++) {
            Button digitButton = createDigitButton(i);
            digitButtons.add(digitButton);
            rows[(i-1) / 3].getChildren().add(digitButton);
        }
        
        
        Button zeroButton = createDigitButton(0);
        digitButtons.add(zeroButton);
        rows[3].getChildren().add(zeroButton);
        
        
        Button confirmButton = CyberpunkEffects.createCyberpunkButton("CONFIRM", true);
        confirmButton.setPrefSize(100, 50);
        confirmButton.setOnAction(e -> checkCode());
        rows[3].getChildren().add(confirmButton);
        
        
        numberPad.getChildren().addAll(rows);
        
        return numberPad;
    }
    
    
    private Button createDigitButton(int digit) {
        Button button = new Button(Integer.toString(digit));
        button.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 18));
        button.setPrefSize(50, 50);
        
        
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#000520")),
            new Stop(1, Color.web("#003050"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        
        
        button.setStyle(
            "-fx-background-color: " + gradient.toString().replace("0x", "#") + "; " +
            "-fx-text-fill: #00ffff; " +
            "-fx-border-color: #00ffff; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 5px; " +
            "-fx-cursor: hand;"
        );
        
        
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
        
        
        button.setOnAction(e -> enterDigit(digit));
        
        return button;
    }
    
    
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
    
    
    private void addScannerEffect(HBox codeDisplay) {
        Rectangle scanner = new Rectangle(0, 0, 5, 50);
        scanner.setFill(Color.web("#00FFFF", 0.7));
        
        Glow glow = new Glow(0.8);
        scanner.setEffect(glow);
        
        codeDisplay.getChildren().add(scanner);
        
        
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
    
    
    private void generateCode() {
        code = new int[codeLength];
        for (int i = 0; i < codeLength; i++) {
            code[i] = random.nextInt(10);
        }
        
        
        StringBuilder codeString = new StringBuilder();
        for (int digit : code) {
            codeString.append(digit);
        }
        log("Generated code: " + codeString.toString());
    }
    
    
    private void enterDigit(int digit) {
        if (currentPosition < codeLength) {
            playerCode[currentPosition] = digit;
            codeLabels.get(currentPosition).setText(Integer.toString(digit));
            codeLabels.get(currentPosition).setTextFill(Color.WHITE);
            currentPosition++;
            
            
            if (currentPosition == codeLength) {
                messageLabel.setText("PRESS CONFIRM TO VERIFY CODE");
                messageLabel.setTextFill(Color.YELLOW);
            }
        }
    }
    
    
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
    
    
    private void checkCode() {
        if (currentPosition != codeLength) {
            messageLabel.setText("INCOMPLETE CODE - FILL ALL DIGITS");
            messageLabel.setTextFill(Color.RED);
                return;
            }
            
        
        boolean correct = true;
        for (int i = 0; i < codeLength; i++) {
            if (playerCode[i] != code[i]) {
                correct = false;
                break;
            }
        }
        
        if (correct) {
            
            messageLabel.setText("CODE VERIFIED - ACCESS GRANTED");
            messageLabel.setTextFill(Color.LIGHTGREEN);
            
            
            for (Label label : codeLabels) {
                label.setTextFill(Color.LIGHTGREEN);
                CyberpunkEffects.styleCompletionEffect(label);
            }
            
            
            if (scanningEffect != null) {
                scanningEffect.stop();
            }
            
            
            completeTask();
        } else {
            
            int exactMatches = 0;
            int valueMatches = 0;
            
            
            int[] codeDigitCounts = new int[10];
            int[] playerDigitCounts = new int[10];
            
            for (int i = 0; i < codeLength; i++) {
                
                if (playerCode[i] == code[i]) {
                    exactMatches++;
                    codeLabels.get(i).setTextFill(Color.LIGHTGREEN);
                } else {
                    codeLabels.get(i).setTextFill(Color.RED);
                }
                
                
                codeDigitCounts[code[i]]++;
                playerDigitCounts[playerCode[i]]++;
            }
            
            
            for (int i = 0; i < 10; i++) {
                valueMatches += Math.min(codeDigitCounts[i], playerDigitCounts[i]);
            }
            
            
            int positionMismatches = valueMatches - exactMatches;
            
            
            messageLabel.setText(String.format("CORRECT: %d | MISPLACED: %d", exactMatches, positionMismatches));
            
            
            for (int i = 0; i < codeLength; i++) {
                if (playerCode[i] == code[i]) {
                    
                    codeLabels.get(i).setTextFill(Color.LIGHTGREEN);
                } else {
                    
                    boolean digitExists = false;
                    for (int j = 0; j < codeLength; j++) {
                        if (playerCode[i] == code[j] && playerCode[j] != code[j]) {
                            digitExists = true;
                            break;
                        }
                    }
                    
                    if (digitExists) {
                        
                        codeLabels.get(i).setTextFill(Color.YELLOW);
                    } else {
                        
                        codeLabels.get(i).setTextFill(Color.RED);
                    }
                }
            }
            
            
            
        }
    }
    
    
    private void cleanupResources() {
        
        if (scanningEffect != null) {
            scanningEffect.stop();
        }
        
        
        code = null;
        playerCode = null;
        digitButtons.clear();
        codeLabels.clear();
        messageLabel = null;
        currentPosition = 0;
    }

    
    @Override
    protected void cleanupTask() {
        super.cleanupTask();
        cleanupResources();
    }
} 
