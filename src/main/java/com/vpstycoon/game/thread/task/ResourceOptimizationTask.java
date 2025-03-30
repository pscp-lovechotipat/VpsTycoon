package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public class ResourceOptimizationTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(ResourceOptimizationTask.class.getName());
    private static final int NUM_RESOURCES = 4;
    private static final Random random = new Random();
    
    private final List<Double> targetValues = new ArrayList<>();
    private final List<Slider> sliders = new ArrayList<>();
    private final List<Rectangle> indicators = new ArrayList<>();
    private final List<Label> statusLabels = new ArrayList<>();
    
    private int optimizedCount = 0;
    private Label systemStatusLabel;
    private Button optimizeButton;
    private boolean taskActive = true;

    public ResourceOptimizationTask() {
        super(
                "Resource Allocation",
                "Optimize system resources for maximum efficiency",
                "/images/task/resource_task.png",
                7000, 
                25,  
                4,    
                60    
        );
        
        
        for (int i = 0; i < NUM_RESOURCES; i++) {
            targetValues.add(0.3 + random.nextDouble() * 0.6);
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        
        BorderPane taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(600, 400);
        taskPane.setPadding(new Insets(20));
        
        
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Text titleText = CyberpunkEffects.createTaskTitle("RESOURCE OPTIMIZATION SYSTEMS");
        Text descText = CyberpunkEffects.createTaskDescription("Adjust resource allocation sliders to match optimal levels");
        headerBox.getChildren().addAll(titleText, descText);
        taskPane.setTop(headerBox);
        
        
        VBox slidersContainer = new VBox(15);
        slidersContainer.setPadding(new Insets(20, 0, 20, 0));
        slidersContainer.setAlignment(Pos.CENTER);
        
        
        String[] resourceNames = {
            "CPU ALLOCATION", 
            "MEMORY UTILIZATION", 
            "NETWORK BANDWIDTH",
            "STORAGE I/O PRIORITY"
        };
        
        
        for (int i = 0; i < NUM_RESOURCES; i++) {
            final int index = i;
            HBox resourceBox = new HBox(15);
            resourceBox.setAlignment(Pos.CENTER_LEFT);
            
            
            Label nameLabel = CyberpunkEffects.createGlowingLabel(resourceNames[i], "#00FFFF");
            
            
            Slider slider = new Slider(0, 1, 0.5);
            slider.setPrefWidth(200);
            slider.setStyle(
                    "-fx-control-inner-background: #151530;" +
                    "-fx-accent: " + CyberpunkEffects.NEON_COLORS[i % CyberpunkEffects.NEON_COLORS.length].toString().replace("0x", "#") + ";"
            );
            
            
            Pane indicatorPane = new Pane();
            indicatorPane.setPrefSize(200, 30);
            indicatorPane.setStyle("-fx-background-color: #151530; -fx-border-color: #303060; -fx-border-width: 1;");
            
            
            Rectangle levelIndicator = new Rectangle(0, 5, 10, 20);
            levelIndicator.setFill(CyberpunkEffects.NEON_COLORS[i % CyberpunkEffects.NEON_COLORS.length]);
            indicatorPane.getChildren().add(levelIndicator);
            
            
            Rectangle targetZone = new Rectangle(0, 0, 3, 30);
            targetZone.setFill(Color.WHITE);
            targetZone.setOpacity(0.7);
            indicatorPane.getChildren().add(targetZone);
            
            
            double targetX = targetValues.get(i) * 200;
            targetZone.setX(targetX);
            
            
            Label statusLabel = new Label("SUBOPTIMAL");
            statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 12));
            statusLabel.setTextFill(Color.ORANGERED);
            
            
            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double x = newVal.doubleValue() * 200 - 5;
                levelIndicator.setX(x);
                
                
                if (Math.abs(newVal.doubleValue() - targetValues.get(index)) < 0.05) {
                    if (!statusLabel.getText().equals("OPTIMIZED")) {
                        optimizedCount++;
                        statusLabel.setText("OPTIMIZED");
                        statusLabel.setTextFill(Color.LIMEGREEN);
                        updateSystemStatus();
                    }
                } else {
                    if (statusLabel.getText().equals("OPTIMIZED")) {
                        optimizedCount--;
                        statusLabel.setText("SUBOPTIMAL");
                        statusLabel.setTextFill(Color.ORANGERED);
                        updateSystemStatus();
                    }
                }
            });
            
            
            resourceBox.getChildren().addAll(nameLabel, slider, indicatorPane, statusLabel);
            slidersContainer.getChildren().add(resourceBox);
            
            
            sliders.add(slider);
            indicators.add(levelIndicator);
            statusLabels.add(statusLabel);
        }
        
        
        systemStatusLabel = new Label("SYSTEM EFFICIENCY: 0%");
        systemStatusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 16));
        systemStatusLabel.setTextFill(Color.web("#FF9500"));
        CyberpunkEffects.pulseNode(systemStatusLabel);
        
        
        optimizeButton = CyberpunkEffects.createCyberpunkButton("FINALIZE OPTIMIZATION", true);
        optimizeButton.setDisable(true);
        optimizeButton.setOnAction(e -> {
            if (optimizedCount == NUM_RESOURCES) {
                taskActive = false;
                CyberpunkEffects.styleCompletionEffect(slidersContainer);
                completeTask();
            }
        });
        
        
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(systemStatusLabel, optimizeButton);
        
        
        CyberpunkEffects.addScanningEffect(taskPane);
        
        
        taskPane.setCenter(slidersContainer);
        taskPane.setBottom(bottomBox);
        BorderPane.setMargin(bottomBox, new Insets(0, 0, 20, 0));
        
        
        addInstructionsWithGlitch(taskPane);
        
        
        gamePane.getChildren().add(taskPane);
    }
    
    
    private void addInstructionsWithGlitch(Pane pane) {
        Label instructionsLabel = new Label("DRAG SLIDERS TO MATCH TARGET INDICATORS");
        instructionsLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        instructionsLabel.setTextFill(Color.web("#00FFFF"));
        instructionsLabel.setTranslateX(20);
        instructionsLabel.setTranslateY(20);
        instructionsLabel.setOpacity(0.8);
        
        
        Timeline glitchTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(instructionsLabel.opacityProperty(), 0.8)),
            new KeyFrame(Duration.millis(100),
                new KeyValue(instructionsLabel.translateXProperty(), instructionsLabel.getTranslateX() + 5),
                new KeyValue(instructionsLabel.opacityProperty(), 0.9)),
            new KeyFrame(Duration.millis(200),
                new KeyValue(instructionsLabel.translateXProperty(), instructionsLabel.getTranslateX() - 2),
                new KeyValue(instructionsLabel.opacityProperty(), 0.7)),
            new KeyFrame(Duration.millis(300),
                new KeyValue(instructionsLabel.translateXProperty(), instructionsLabel.getTranslateX()),
                new KeyValue(instructionsLabel.opacityProperty(), 0.8))
        );
        glitchTimeline.setCycleCount(Timeline.INDEFINITE);
        glitchTimeline.setAutoReverse(true);
        glitchTimeline.play();
        
        pane.getChildren().add(instructionsLabel);
    }
    
    
    private void updateSystemStatus() {
        int percentage = (int)((optimizedCount / (double)NUM_RESOURCES) * 100);
        systemStatusLabel.setText("SYSTEM EFFICIENCY: " + percentage + "%");
        
        if (percentage < 25) {
            systemStatusLabel.setTextFill(Color.web("#FF0000"));
        } else if (percentage < 50) {
            systemStatusLabel.setTextFill(Color.web("#FF9500"));
        } else if (percentage < 100) {
            systemStatusLabel.setTextFill(Color.web("#FFFF00"));
        } else {
            systemStatusLabel.setTextFill(Color.web("#00FF00"));
        }
        
        
        optimizeButton.setDisable(optimizedCount < NUM_RESOURCES);
    }
} 
