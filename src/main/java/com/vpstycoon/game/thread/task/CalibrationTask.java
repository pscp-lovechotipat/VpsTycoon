package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * System Calibration Task
 * The player must calibrate circuit components by clicking them in the correct sequence
 */
public class CalibrationTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(CalibrationTask.class.getName());
    private static final Random random = new Random();
    private static final int GRID_SIZE = 4;
    private static final int SEQUENCE_LENGTH = 5;
    
    private final List<Integer> sequence = new ArrayList<>();
    private final List<StackPane> components = new ArrayList<>();
    
    private int currentStep = 0;
    private boolean showingSequence = true;
    private Label statusLabel;
    private BorderPane taskPane;

    public CalibrationTask() {
        super(
                "System Calibration",
                "Fine-tune system parameters for optimal performance",
                "/images/task/calibration_task.png",
                5500, // reward
                15,   // penalty (0.15 * 100)
                2,    // difficulty
                40    // time limit in seconds
        );
        
        // Generate random sequence
        generateSequence();
    }

    private void generateSequence() {
        sequence.clear();
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            sequence.add(random.nextInt(GRID_SIZE * GRID_SIZE));
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Create main task container
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(500, 400);
        taskPane.setPadding(new Insets(20));
        
        // Add animated background
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        // Create title area
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        // Text titleText = CyberpunkEffects.createTaskTitle("SYSTEM CALIBRATION");
        Text descText = CyberpunkEffects.createTaskDescription("Calibrate components by repeating the sequence");
        headerBox.getChildren().addAll(descText);
        taskPane.setTop(headerBox);
        
        // Create circuit board with components
        GridPane circuitBoard = createCircuitBoard();
        taskPane.setCenter(circuitBoard);
        
        // Status label
        statusLabel = new Label("MEMORIZE SEQUENCE");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        statusLabel.setAlignment(Pos.CENTER);
        
        // Reset button
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET SEQUENCE", false);
        resetButton.setOnAction(e -> {
            if (!showingSequence) {
                currentStep = 0;
                statusLabel.setText("SEQUENCE RESET");
                statusLabel.setTextFill(Color.web("#FFA500"));
                
                // Brief delay then show sequence again
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> showSequence());
                pause.play();
            }
        });
        
        // Bottom panel with status and button
        VBox bottomPanel = new VBox(15);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.getChildren().addAll(statusLabel, resetButton);
        taskPane.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(20, 0, 10, 0));
        
        // Add the circuit board to the game pane
        gamePane.getChildren().add(taskPane);
        
        // Start showing the sequence after a brief delay
        PauseTransition startDelay = new PauseTransition(Duration.seconds(1));
        startDelay.setOnFinished(e -> showSequence());
        startDelay.play();
    }
    
    /**
     * Create the circuit board with components
     */
    private GridPane createCircuitBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        
        // Add a background for the circuit board
        Rectangle background = new Rectangle(300, 300);
        background.setFill(Color.web("#0A0A2A"));
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setStroke(Color.web("#303060"));
        background.setStrokeWidth(2);
        StackPane boardPane = new StackPane(background);
        
        // Add circuit traces (lines between components)
        Pane tracesPane = new Pane();
        for (int i = 0; i < 10; i++) {
            Line trace = new Line(
                    random.nextDouble() * 280, 
                    random.nextDouble() * 280,
                    random.nextDouble() * 280, 
                    random.nextDouble() * 280
            );
            trace.setStroke(Color.web("#0080FF", 0.3));
            trace.setStrokeWidth(1);
            tracesPane.getChildren().add(trace);
        }
        boardPane.getChildren().add(tracesPane);
        
        // Create components grid
        GridPane componentsGrid = new GridPane();
        componentsGrid.setAlignment(Pos.CENTER);
        componentsGrid.setHgap(45);
        componentsGrid.setVgap(45);
        
        // Create circuit components
        components.clear();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                final int index = row * GRID_SIZE + col;
                
                // Create component visual
                StackPane component = createCircuitComponent();
                
                // Store component and add click handler
                components.add(component);
                component.setOnMouseClicked(e -> handleComponentClick(index));
                
                // Add to grid
                componentsGrid.add(component, col, row);
            }
        }
        
        boardPane.getChildren().add(componentsGrid);
        grid.add(boardPane, 0, 0);
        
        // Add holographic grid lines
        CyberpunkEffects.addHoloGridLines(boardPane, 30, 30);
        
        return grid;
    }
    
    /**
     * Create a visual representation of a circuit component
     */
    private StackPane createCircuitComponent() {
        // Component types
        int type = random.nextInt(3);
        
        StackPane component = new StackPane();
        component.setPrefSize(40, 40);
        
        switch (type) {
            case 0: // Circular component (like a capacitor)
                Circle circle = new Circle(15);
                circle.setFill(Color.web("#151525"));
                circle.setStroke(Color.web("#00FFFF"));
                circle.setStrokeWidth(1.5);
                component.getChildren().add(circle);
                break;
                
            case 1: // Square component (like a chip)
                Rectangle rect = new Rectangle(30, 30);
                rect.setFill(Color.web("#151525"));
                rect.setStroke(Color.web("#FF00A0"));
                rect.setStrokeWidth(1.5);
                component.getChildren().add(rect);
                break;
                
            case 2: // Diamond component (like a diode)
                Rectangle diamond = new Rectangle(30, 30);
                diamond.setFill(Color.web("#151525"));
                diamond.setStroke(Color.web("#FFA500"));
                diamond.setStrokeWidth(1.5);
                diamond.setRotate(45);
                component.getChildren().add(diamond);
                break;
        }
        
        // Add a glow effect that will be enabled when active
        Bloom glow = new Bloom();
        glow.setThreshold(0.3);
        component.setEffect(glow);
        component.setOpacity(0.8); // Dimmed by default
        
        // Make components slightly interactive on hover
        component.setOnMouseEntered(e -> {
            if (!showingSequence) {
                component.setScaleX(1.1);
                component.setScaleY(1.1);
            }
        });
        component.setOnMouseExited(e -> {
            if (!showingSequence) {
                component.setScaleX(1.0);
                component.setScaleY(1.0);
            }
        });
        
        return component;
    }
    
    /**
     * Show the sequence to the player
     */
    private void showSequence() {
        showingSequence = true;
        currentStep = 0;
        statusLabel.setText("MEMORIZE SEQUENCE");
        statusLabel.setTextFill(Color.web("#00FFFF"));
        
        // Disable component clicking during sequence display
        components.forEach(c -> c.setOnMouseClicked(null));
        
        // Show each step in the sequence with a delay
        Timeline sequenceTimeline = new Timeline();
        
        for (int i = 0; i < sequence.size(); i++) {
            int component = sequence.get(i);
            
            // Start frame - highlight component
            KeyFrame startFrame = new KeyFrame(Duration.seconds(i * 1), event -> {
                activateComponent(component, true);
            });
            
            // End frame - unhighlight component
            KeyFrame endFrame = new KeyFrame(Duration.seconds(i * 1 + 0.7), event -> {
                activateComponent(component, false);
            });
            
            sequenceTimeline.getKeyFrames().addAll(startFrame, endFrame);
        }
        
        // After showing sequence, enable input
        KeyFrame finishFrame = new KeyFrame(Duration.seconds(sequence.size() * 1 + 1), event -> {
            showingSequence = false;
            statusLabel.setText("REPEAT SEQUENCE");
            statusLabel.setTextFill(Color.web("#FFFFFF"));
            
            // Re-enable component clicking
            for (int i = 0; i < components.size(); i++) {
                final int index = i;
                components.get(i).setOnMouseClicked(e -> handleComponentClick(index));
            }
        });
        
        sequenceTimeline.getKeyFrames().add(finishFrame);
        sequenceTimeline.play();
    }
    
    /**
     * Activate/deactivate a component with visual effect
     */
    private void activateComponent(int index, boolean activate) {
        StackPane component = components.get(index);
        
        if (activate) {
            // Set glow intensity
            ((Bloom)component.getEffect()).setThreshold(0.1);
            
            // Increase opacity and scale
            component.setOpacity(1.0);
            component.setScaleX(1.2);
            component.setScaleY(1.2);
            
            // Add color pulse
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(component.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(350),
                    new KeyValue(component.opacityProperty(), 0.8)),
                new KeyFrame(Duration.millis(700),
                    new KeyValue(component.opacityProperty(), 1.0))
            );
            pulse.setCycleCount(1);
            pulse.play();
        } else {
            // Reset effects
            ((Bloom)component.getEffect()).setThreshold(0.3);
            component.setOpacity(0.8);
            component.setScaleX(1.0);
            component.setScaleY(1.0);
        }
    }
    
    /**
     * Handle component click during sequence input
     */
    private void handleComponentClick(int index) {
        if (showingSequence) return;
        
        // Highlight clicked component
        activateComponent(index, true);
        
        // Check if correct
        if (index == sequence.get(currentStep)) {
            // Correct input
            currentStep++;
            
            // Unhighlight after brief delay
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> activateComponent(index, false));
            pause.play();
            
            // Update status
            statusLabel.setText("COMPONENT " + currentStep + "/" + SEQUENCE_LENGTH + " CALIBRATED");
            statusLabel.setTextFill(Color.web("#00FF00"));
            
            // Check if sequence is complete
            if (currentStep >= sequence.size()) {
                // Sequence completed correctly
                showSuccessSequence();
            }
        } else {
            // Incorrect input
            statusLabel.setText("CALIBRATION ERROR");
            statusLabel.setTextFill(Color.web("#FF0000"));
            
            // Show error visual
            showErrorFeedback(index);
            
            // Reset sequence after a moment
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                currentStep = 0;
                showSequence();
            });
            pause.play();
        }
    }
    
    /**
     * Show error feedback when wrong component clicked
     */
    private void showErrorFeedback(int index) {
        StackPane component = components.get(index);
        
        // Create error effect
        Timeline errorEffect = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(component.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(150),
                new KeyValue(component.opacityProperty(), 0.4)),
            new KeyFrame(Duration.millis(300),
                new KeyValue(component.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(450),
                new KeyValue(component.opacityProperty(), 0.4)),
            new KeyFrame(Duration.millis(600),
                new KeyValue(component.opacityProperty(), 1.0))
        );
        
        // Show the correct component briefly
        StackPane correctComponent = components.get(sequence.get(currentStep));
        Timeline showCorrect = new Timeline(
            new KeyFrame(Duration.millis(800), e -> activateComponent(sequence.get(currentStep), true)),
            new KeyFrame(Duration.millis(1300), e -> activateComponent(sequence.get(currentStep), false))
        );
        
        errorEffect.play();
        showCorrect.play();
    }
    
    /**
     * Show success animation when sequence completed correctly
     */
    private void showSuccessSequence() {
        showingSequence = true;
        statusLabel.setText("CALIBRATION COMPLETE");
        statusLabel.setTextFill(Color.web("#00FF00"));
        
        // Apply success effect to the task pane
        CyberpunkEffects.styleCompletionEffect(taskPane);
        
        // Light up all components in sequence rapidly
        Timeline successAnim = new Timeline();
        
        for (int i = 0; i < components.size(); i++) {
            final int index = i;
            
            // Staggered illumination
            KeyFrame illuminateFrame = new KeyFrame(Duration.millis(i * 80), e -> {
                activateComponent(index, true);
            });
            successAnim.getKeyFrames().add(illuminateFrame);
        }
        
        successAnim.setOnFinished(e -> completeTask());
        successAnim.play();
    }
} 