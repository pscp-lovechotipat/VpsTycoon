package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.*;
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


public class CalibrationTask extends GameTask {

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
                5500, 
                15,   
                2,    
                40    
        );
        
        
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
        
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(500, 400);
        taskPane.setPadding(new Insets(20));
        
        
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        
        Text descText = CyberpunkEffects.createTaskDescription("Calibrate components by repeating the sequence");
        headerBox.getChildren().addAll(descText);
        taskPane.setTop(headerBox);
        
        
        GridPane circuitBoard = createCircuitBoard();
        taskPane.setCenter(circuitBoard);
        
        
        statusLabel = new Label("MEMORIZE SEQUENCE");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        statusLabel.setAlignment(Pos.CENTER);
        
        
        Button resetButton = CyberpunkEffects.createCyberpunkButton("RESET SEQUENCE", false);
        resetButton.setOnAction(e -> {
            if (!showingSequence) {
                currentStep = 0;
                statusLabel.setText("SEQUENCE RESET");
                statusLabel.setTextFill(Color.web("#FFA500"));
                
                
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(event -> showSequence());
                pause.play();
            }
        });
        
        
        VBox bottomPanel = new VBox(15);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.getChildren().addAll(statusLabel, resetButton);
        taskPane.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(20, 0, 10, 0));
        
        
        gamePane.getChildren().add(taskPane);
        
        
        PauseTransition startDelay = new PauseTransition(Duration.seconds(1));
        startDelay.setOnFinished(e -> showSequence());
        startDelay.play();
    }
    
    
    private GridPane createCircuitBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        
        
        Rectangle background = new Rectangle(300, 300);
        background.setFill(Color.web("#0A0A2A"));
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setStroke(Color.web("#303060"));
        background.setStrokeWidth(2);
        StackPane boardPane = new StackPane(background);
        
        
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
        
        
        GridPane componentsGrid = new GridPane();
        componentsGrid.setAlignment(Pos.CENTER);
        componentsGrid.setHgap(45);
        componentsGrid.setVgap(45);
        
        
        components.clear();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                final int index = row * GRID_SIZE + col;
                
                
                StackPane component = createCircuitComponent();
                
                
                components.add(component);
                component.setOnMouseClicked(e -> handleComponentClick(index));
                
                
                componentsGrid.add(component, col, row);
            }
        }
        
        boardPane.getChildren().add(componentsGrid);
        grid.add(boardPane, 0, 0);
        
        
        CyberpunkEffects.addHoloGridLines(boardPane, 30, 30);
        
        return grid;
    }
    
    
    private StackPane createCircuitComponent() {
        
        int type = random.nextInt(3);
        
        StackPane component = new StackPane();
        component.setPrefSize(40, 40);
        
        switch (type) {
            case 0: 
                Circle circle = new Circle(15);
                circle.setFill(Color.web("#151525"));
                circle.setStroke(Color.web("#00FFFF"));
                circle.setStrokeWidth(1.5);
                component.getChildren().add(circle);
                break;
                
            case 1: 
                Rectangle rect = new Rectangle(30, 30);
                rect.setFill(Color.web("#151525"));
                rect.setStroke(Color.web("#FF00A0"));
                rect.setStrokeWidth(1.5);
                component.getChildren().add(rect);
                break;
                
            case 2: 
                Rectangle diamond = new Rectangle(30, 30);
                diamond.setFill(Color.web("#151525"));
                diamond.setStroke(Color.web("#FFA500"));
                diamond.setStrokeWidth(1.5);
                diamond.setRotate(45);
                component.getChildren().add(diamond);
                break;
        }
        
        
        Bloom glow = new Bloom();
        glow.setThreshold(0.3);
        component.setEffect(glow);
        component.setOpacity(0.8); 
        
        
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
    
    
    private void showSequence() {
        showingSequence = true;
        currentStep = 0;
        statusLabel.setText("MEMORIZE SEQUENCE");
        statusLabel.setTextFill(Color.web("#00FFFF"));
        
        
        components.forEach(c -> c.setOnMouseClicked(null));
        
        
        Timeline sequenceTimeline = new Timeline();
        
        for (int i = 0; i < sequence.size(); i++) {
            int component = sequence.get(i);
            
            
            KeyFrame startFrame = new KeyFrame(Duration.seconds(i), event -> {
                activateComponent(component, true);
            });
            
            
            KeyFrame endFrame = new KeyFrame(Duration.seconds(i + 0.7), event -> {
                activateComponent(component, false);
            });
            
            sequenceTimeline.getKeyFrames().addAll(startFrame, endFrame);
        }
        
        
        KeyFrame finishFrame = new KeyFrame(Duration.seconds(sequence.size() + 1), event -> {
            showingSequence = false;
            statusLabel.setText("REPEAT SEQUENCE");
            statusLabel.setTextFill(Color.web("#FFFFFF"));
            
            
            for (int i = 0; i < components.size(); i++) {
                final int index = i;
                components.get(i).setOnMouseClicked(e -> handleComponentClick(index));
            }
        });
        
        sequenceTimeline.getKeyFrames().add(finishFrame);
        sequenceTimeline.play();
    }
    
    
    private void activateComponent(int index, boolean activate) {
        StackPane component = components.get(index);
        
        if (activate) {
            
            ((Bloom)component.getEffect()).setThreshold(0.1);
            
            
            component.setOpacity(1.0);
            component.setScaleX(1.2);
            component.setScaleY(1.2);
            
            
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
            
            ((Bloom)component.getEffect()).setThreshold(0.3);
            component.setOpacity(0.8);
            component.setScaleX(1.0);
            component.setScaleY(1.0);
        }
    }
    
    
    private void handleComponentClick(int index) {
        if (showingSequence) return;
        
        
        activateComponent(index, true);
        
        
        if (index == sequence.get(currentStep)) {
            
            currentStep++;
            
            
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> activateComponent(index, false));
            pause.play();
            
            
            statusLabel.setText("COMPONENT " + currentStep + "/" + SEQUENCE_LENGTH + " CALIBRATED");
            statusLabel.setTextFill(Color.web("#00FF00"));
            
            
            if (currentStep >= sequence.size()) {
                
                showSuccessSequence();
            }
        } else {
            
            statusLabel.setText("CALIBRATION ERROR");
            statusLabel.setTextFill(Color.web("#FF0000"));
            
            
            showErrorFeedback(index);
            
            
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                currentStep = 0;
                showSequence();
            });
            pause.play();
        }
    }
    
    
    private void showErrorFeedback(int index) {
        StackPane component = components.get(index);
        
        
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
        
        
        StackPane correctComponent = components.get(sequence.get(currentStep));
        Timeline showCorrect = new Timeline(
            new KeyFrame(Duration.millis(800), e -> activateComponent(sequence.get(currentStep), true)),
            new KeyFrame(Duration.millis(1300), e -> activateComponent(sequence.get(currentStep), false))
        );
        
        errorEffect.play();
        showCorrect.play();
    }
    
    
    private void showSuccessSequence() {
        showingSequence = true;
        statusLabel.setText("CALIBRATION COMPLETE");
        statusLabel.setTextFill(Color.web("#00FF00"));
        
        
        CyberpunkEffects.styleCompletionEffect(taskPane);
        
        
        Timeline successAnim = new Timeline();
        
        for (int i = 0; i < components.size(); i++) {
            final int index = i;
            
            
            KeyFrame illuminateFrame = new KeyFrame(Duration.millis(i * 80), e -> {
                activateComponent(index, true);
            });
            successAnim.getKeyFrames().add(illuminateFrame);
        }
        
        successAnim.setOnFinished(e -> completeTask());
        successAnim.play();
    }
} 

