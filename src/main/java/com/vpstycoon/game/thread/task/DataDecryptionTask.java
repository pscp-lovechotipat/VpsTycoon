package com.vpstycoon.game.thread.task;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Decryption Task - Player must figure out what number each symbol represents
 * and enter the correct combination to decrypt the data.
 */
public class DataDecryptionTask extends GameTask {
    // Cyberpunk symbols for numbers 0-9
    private static final String[] SYMBOLS = {
            "⌬", "⎔", "⏣", "⏥", "⏢", "⌘", "⍟", "⎈", "⏧", "⏨"
    };
    
    private final int codeLength;
    private final Map<String, Integer> symbolMap = new HashMap<>();
    private final List<String> codeSymbols = new ArrayList<>();
    private final List<Integer> solution = new ArrayList<>();
    private final List<TextField> inputFields = new ArrayList<>();

    /**
     * Constructor for Data Decryption Task
     */
    public DataDecryptionTask() {
        super(
                "Data Decryption",
                "Crack the encryption by determining which symbol represents which number",
                "/images/task/decryption_task.png",
                8000, // reward
                30,  // penalty (converted from 0.3 to 30)
                3,    // difficulty
                90    // time limit in seconds
        );
        this.codeLength = 4; // Default code length
        generateSymbolMapping();
    }
    
    /**
     * Constructor with custom code length
     * 
     * @param codeLength Length of the code to decrypt
     */
    public DataDecryptionTask(int codeLength) {
        super(
                "Data Decryption",
                "Crack the encryption by determining which symbol represents which number",
                "/images/task/decryption_task.png",
                codeLength <= 3 ? 6000 : (codeLength <= 5 ? 8000 : 12000), // reward based on length
                30, // penalty (converted from 0.3 to 30)
                codeLength <= 3 ? 2 : (codeLength <= 5 ? 3 : 4), // difficulty based on code length
                90   // time limit in seconds
        );
        this.codeLength = Math.min(codeLength, 8); // Max 8 symbols for challenge
        generateSymbolMapping();
    }
    
    /**
     * Generate random symbol-to-number mapping and the code to decrypt
     */
    private void generateSymbolMapping() {
        // Create a mapping of symbols to numbers
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }
        
        // Shuffle the numbers
        java.util.Collections.shuffle(numbers);
        
        // Assign each symbol a number
        for (int i = 0; i < SYMBOLS.length; i++) {
            symbolMap.put(SYMBOLS[i], numbers.get(i));
        }
        
        // Generate a random code
        for (int i = 0; i < codeLength; i++) {
            int symbolIndex = random.nextInt(SYMBOLS.length);
            String symbol = SYMBOLS[symbolIndex];
            codeSymbols.add(symbol);
            solution.add(symbolMap.get(symbol));
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Create the main layout
        VBox decryptionPane = new VBox(20);
        decryptionPane.setAlignment(Pos.CENTER);
        decryptionPane.setPadding(new Insets(20));
        decryptionPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        // Create clues section
        Text cluesTitle = new Text("DECRYPTION CLUES");
        cluesTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        cluesTitle.setFill(Color.web("#00ffff"));
        
        // Create the clues grid
        GridPane cluesGrid = new GridPane();
        cluesGrid.setHgap(15);
        cluesGrid.setVgap(10);
        cluesGrid.setAlignment(Pos.CENTER);
        
        // Generate clues based on symbol mapping
        generateClues(cluesGrid);
        
        // Create the code to decrypt
        Text codeTitle = new Text("ENCRYPTED DATA SEQUENCE");
        codeTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        codeTitle.setFill(Color.web("#ff00ff"));
        
        HBox codeDisplay = new HBox(15);
        codeDisplay.setAlignment(Pos.CENTER);
        
        for (String symbol : codeSymbols) {
            Text symbolText = new Text(symbol);
            symbolText.setFont(Font.font("Orbitron", FontWeight.BOLD, 40));
            symbolText.setFill(Color.web("#ff00ff"));
            
            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#ff00ff"));
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(10);
            symbolText.setEffect(glow);
            
            codeDisplay.getChildren().add(symbolText);
        }
        
        // Create input section
        Text inputTitle = new Text("ENTER DECRYPTION CODE");
        inputTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        inputTitle.setFill(Color.web("#00ffff"));
        
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        
        // Create input fields for the code
        for (int i = 0; i < codeLength; i++) {
            TextField digitField = new TextField();
            digitField.setPrefWidth(50);
            digitField.setPrefHeight(50);
            digitField.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 24));
            digitField.setStyle(
                "-fx-background-color: #12122a; " +
                "-fx-text-fill: #00ffff; " +
                "-fx-border-color: #30305a; " +
                "-fx-border-width: 2px; " +
                "-fx-alignment: center;"
            );
            
            // Limit to one character and numbers only
            digitField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    digitField.setText(newValue.substring(0, 1));
                }
                if (!newValue.isEmpty() && !newValue.matches("\\d")) {
                    digitField.setText(oldValue);
                }
            });
            
            // Auto-focus next field after input
            int fieldIndex = i;
            digitField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() == 1 && fieldIndex < codeLength - 1) {
                    Platform.runLater(() -> inputFields.get(fieldIndex + 1).requestFocus());
                }
            });
            
            inputFields.add(digitField);
            inputBox.getChildren().add(digitField);
        }
        
        // Create submit button
        Button submitButton = new Button("DECRYPT");
        submitButton.getStyleClass().add("button");
        submitButton.setStyle(
            "-fx-background-color: #2a0a60; " +
            "-fx-text-fill: #00ffff; " +
            "-fx-border-color: #7700ff; " +
            "-fx-border-width: 2px; " +
            "-fx-font-family: 'Share Tech Mono', monospace; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        submitButton.setOnAction(e -> checkSolution());
        
        // Add all components to the main layout
        decryptionPane.getChildren().addAll(
            cluesTitle,
            cluesGrid, 
            codeTitle,
            codeDisplay,
            inputTitle,
            inputBox,
            submitButton
        );
        
        gamePane.getChildren().add(decryptionPane);
        
        // Focus the first input field
        Platform.runLater(() -> inputFields.get(0).requestFocus());
    }
    
    /**
     * Generate clues for the player to figure out the symbol mapping
     * 
     * @param cluesGrid The grid to add clues to
     */
    private void generateClues(GridPane cluesGrid) {
        // Create 5 mathematical clues
        for (int i = 0; i < 5; i++) {
            generateMathClue(cluesGrid, i);
        }
    }
    
    /**
     * Generate a mathematical clue for symbol decryption
     * 
     * @param grid The grid to add the clue to
     * @param row The row to add the clue at
     */
    private void generateMathClue(GridPane grid, int row) {
        // Pick random operation
        String[] operations = {"+", "-", "×", "÷"};
        String operation = operations[random.nextInt(operations.length)];
        
        // Pick random symbols to use in clue
        String symbol1 = SYMBOLS[random.nextInt(SYMBOLS.length)];
        String symbol2 = SYMBOLS[random.nextInt(SYMBOLS.length)];
        
        // If divide, make sure divisible with no remainder
        if (operation.equals("÷")) {
            int num1 = symbolMap.get(symbol1);
            int num2 = symbolMap.get(symbol2);
            
            if (num2 == 0 || num1 % num2 != 0) {
                // Try again with addition
                operation = "+";
            }
        }
        
        // Calculate result
        int num1 = symbolMap.get(symbol1);
        int num2 = symbolMap.get(symbol2);
        int result;
        
        switch (operation) {
            case "+":
                result = num1 + num2;
                break;
            case "-":
                // Ensure positive result
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                    symbol1 = SYMBOLS[getIndexOfSymbol(num1)];
                    symbol2 = SYMBOLS[getIndexOfSymbol(num2)];
                }
                result = num1 - num2;
                break;
            case "×":
                result = num1 * num2;
                break;
            case "÷":
                // Already checked divisibility
                result = num1 / num2;
                break;
            default:
                result = num1 + num2;
        }
        
        // Create clue text
        Text symbolText1 = new Text(symbol1);
        symbolText1.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        symbolText1.setFill(Color.web("#00ffff"));
        
        Text operationText = new Text(" " + operation + " ");
        operationText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 20));
        operationText.setFill(Color.LIGHTGRAY);
        
        Text symbolText2 = new Text(symbol2);
        symbolText2.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        symbolText2.setFill(Color.web("#00ffff"));
        
        Text equalsText = new Text(" = ");
        equalsText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 20));
        equalsText.setFill(Color.LIGHTGRAY);
        
        Text resultText = new Text(Integer.toString(result));
        resultText.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 24));
        resultText.setFill(Color.web("#ff00ff"));
        
        // Add to grid
        grid.add(symbolText1, 0, row);
        grid.add(operationText, 1, row);
        grid.add(symbolText2, 2, row);
        grid.add(equalsText, 3, row);
        grid.add(resultText, 4, row);
    }
    
    /**
     * Get the index of a symbol for the given number
     * 
     * @param number The number to find the symbol for
     * @return The index of the symbol
     */
    private int getIndexOfSymbol(int number) {
        for (int i = 0; i < SYMBOLS.length; i++) {
            if (symbolMap.get(SYMBOLS[i]) == number) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Check if the player's solution is correct
     */
    private void checkSolution() {
        List<Integer> playerSolution = new ArrayList<>();
        
        // Read the player's input
        for (TextField field : inputFields) {
            String input = field.getText();
            if (input.isEmpty()) {
                // Highlight empty fields
                field.setStyle(
                    "-fx-background-color: #12122a; " +
                    "-fx-text-fill: #00ffff; " +
                    "-fx-border-color: #ff0000; " +
                    "-fx-border-width: 2px; " +
                    "-fx-alignment: center;"
                );
                return;
            }
            
            playerSolution.add(Integer.parseInt(input));
        }
        
        // Check if solution is correct
        if (playerSolution.equals(solution)) {
            completeTask();
        } else {
            // Show error feedback - flash fields in red
            for (TextField field : inputFields) {
                field.setStyle(
                    "-fx-background-color: #3a0a1a; " +
                    "-fx-text-fill: #ff3366; " +
                    "-fx-border-color: #ff0000; " +
                    "-fx-border-width: 2px; " +
                    "-fx-alignment: center;"
                );
            }
            
            // Reset style after brief delay
            Thread resetThread = new Thread(() -> {
                try {
                    Thread.sleep(700);
                    Platform.runLater(() -> {
                        for (TextField field : inputFields) {
                            field.setStyle(
                                "-fx-background-color: #12122a; " +
                                "-fx-text-fill: #00ffff; " +
                                "-fx-border-color: #30305a; " +
                                "-fx-border-width: 2px; " +
                                "-fx-alignment: center;"
                            );
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
            resetThread.setDaemon(true);
            resetThread.start();
        }
    }
} 