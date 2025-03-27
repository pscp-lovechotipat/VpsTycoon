package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Server Cooling Task
 * Player must manage cooling systems to prevent server overheating
 */
public class ServerCoolingTask extends GameTask {

    private static final Logger LOGGER = Logger.getLogger(ServerCoolingTask.class.getName());
    private static final Random random = new Random();
    
    private static final int NUM_SERVERS = 4;
    private static final double MAX_TEMPERATURE = 100.0;
    private static final double CRITICAL_TEMP = 90.0;
    private static final double SAFE_TEMP = 60.0;
    private static final double MIN_COOLING_EFFECT = 0.5;
    private static final double MAX_COOLING_EFFECT = 2.5;
    
    private final List<Double> serverTemperatures = new ArrayList<>();
    private final List<Double> coolingRates = new ArrayList<>();
    private final List<ProgressBar> tempBars = new ArrayList<>();
    private final List<Label> tempLabels = new ArrayList<>();
    private final List<Slider> coolingSliders = new ArrayList<>();
    private final List<Rectangle> serverRects = new ArrayList<>();
    
    private double totalPower = 100.0;
    private ProgressBar powerBar;
    private Label powerLabel;
    private Label statusLabel;
    private BorderPane taskPane;
    private boolean emergencyMode = false;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread temperatureThread;

    public ServerCoolingTask() {
        super(
                "Server Cooling",
                "Manage cooling systems to prevent server overheating",
                "/images/task/cooling_task.png",
                6500, // reward
                30,   // penalty (0.3 * 100)
                3,    // difficulty
                60    // time limit in seconds
        );
        
        // Initialize server temperatures (60-85% hot)
        for (int i = 0; i < NUM_SERVERS; i++) {
            serverTemperatures.add(60.0 + random.nextDouble() * 25.0);
            coolingRates.add(1.0); // Default cooling rate
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        // Create main task container
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(600, 450);
        taskPane.setPadding(new Insets(20));
        
        // Add animated background
        CyberpunkEffects.addAnimatedBackground(taskPane);
        
        // Create title area
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Text titleText = CyberpunkEffects.createTaskTitle("SERVER COOLING MANAGEMENT");
        Text descText = CyberpunkEffects.createTaskDescription("Balance cooling resources to stabilize server temperatures");
        headerBox.getChildren().addAll(titleText, descText);
        taskPane.setTop(headerBox);
        
        // Create main content with server status and controls
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        
        // Power allocation bar
        HBox powerBox = new HBox(15);
        powerBox.setAlignment(Pos.CENTER_LEFT);
        Label powerHeaderLabel = CyberpunkEffects.createGlowingLabel("COOLING POWER: ", "#00FFFF");
        
        powerBar = new ProgressBar(1.0);
        powerBar.setPrefWidth(200);
        powerBar.setStyle("-fx-accent: #39FF14;");
        
        powerLabel = new Label("100%");
        powerLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        powerLabel.setTextFill(Color.web("#39FF14"));
        
        powerBox.getChildren().addAll(powerHeaderLabel, powerBar, powerLabel);
        centerContent.getChildren().add(powerBox);
        
        // Server status grid
        GridPane serverGrid = new GridPane();
        serverGrid.setHgap(15);
        serverGrid.setVgap(20);
        serverGrid.setAlignment(Pos.CENTER);
        
        // Create server displays and controls
        for (int i = 0; i < NUM_SERVERS; i++) {
            VBox serverBox = createServerBox(i);
            serverGrid.add(serverBox, i % 2, i / 2);
        }
        
        centerContent.getChildren().add(serverGrid);
        
        // Status label
        statusLabel = new Label("MONITORING SERVER TEMPERATURE");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        CyberpunkEffects.pulseNode(statusLabel);
        centerContent.getChildren().add(statusLabel);
        
        // Control buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button emergencyButton = CyberpunkEffects.createCyberpunkButton("EMERGENCY COOLING", false);
        emergencyButton.setOnAction(e -> activateEmergencyCooling());
        
        Button balanceButton = CyberpunkEffects.createCyberpunkButton("AUTO-BALANCE", true);
        balanceButton.setOnAction(e -> autoBalanceCooling());
        
        buttonBox.getChildren().addAll(emergencyButton, balanceButton);
        centerContent.getChildren().add(buttonBox);
        
        taskPane.setCenter(centerContent);
        
        // Add scanning effect
        CyberpunkEffects.addScanningEffect(taskPane);
        
        // Add the task pane to the game pane
        gamePane.getChildren().add(taskPane);
        
        // Start temperature simulation thread
        startTemperatureSimulation();
    }
    
    /**
     * Create a box for displaying and controlling a server
     */
    private VBox createServerBox(int serverIndex) {
        VBox serverBox = new VBox(10);
        serverBox.setAlignment(Pos.CENTER);
        serverBox.setPadding(new Insets(10));
        serverBox.setStyle("-fx-background-color: rgba(20, 20, 40, 0.7); -fx-border-color: #303060; -fx-border-width: 1px;");
        
        // Server visual
        StackPane serverVisual = new StackPane();
        serverVisual.setPrefSize(120, 80);
        
        Rectangle serverRect = new Rectangle(120, 80);
        serverRect.setFill(getTemperatureColor(serverTemperatures.get(serverIndex)));
        serverRect.setArcWidth(5);
        serverRect.setArcHeight(5);
        serverRects.add(serverRect);
        
        Label serverLabel = new Label("SERVER " + (serverIndex + 1));
        serverLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 14));
        serverLabel.setTextFill(Color.WHITE);
        
        serverVisual.getChildren().addAll(serverRect, serverLabel);
        
        // Temperature bar
        ProgressBar tempBar = new ProgressBar(serverTemperatures.get(serverIndex) / MAX_TEMPERATURE);
        tempBar.setPrefWidth(120);
        tempBar.setStyle("-fx-accent: " + getTemperatureColorString(serverTemperatures.get(serverIndex)) + ";");
        tempBars.add(tempBar);
        
        // Temperature label
        Label tempLabel = new Label(String.format("%.1f°C", serverTemperatures.get(serverIndex)));
        tempLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        tempLabel.setTextFill(getTemperatureColor(serverTemperatures.get(serverIndex)));
        tempLabels.add(tempLabel);
        
        // Cooling slider
        HBox coolingControl = new HBox(10);
        coolingControl.setAlignment(Pos.CENTER);
        
        Label coolingLabel = new Label("COOLING:");
        coolingLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 12));
        coolingLabel.setTextFill(Color.LIGHTCYAN);
        
        Slider coolingSlider = new Slider(0, 2, 1);
        coolingSlider.setPrefWidth(100);
        coolingSlider.setStyle(
                "-fx-control-inner-background: #151530;" +
                "-fx-accent: #00FFFF;"
        );
        coolingSliders.add(coolingSlider);
        
        // Update cooling rate when slider is adjusted
        final int index = serverIndex;
        coolingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double oldCooling = coolingRates.get(index);
            double newCooling = MIN_COOLING_EFFECT + newVal.doubleValue() * (MAX_COOLING_EFFECT - MIN_COOLING_EFFECT) / 2.0;
            
            // Adjust total power
            totalPower -= (newCooling - oldCooling) * 25;
            updatePowerDisplay();
            
            // Update cooling rate
            coolingRates.set(index, newCooling);
        });
        
        coolingControl.getChildren().addAll(coolingLabel, coolingSlider);
        
        // Add components to server box
        serverBox.getChildren().addAll(serverVisual, tempBar, tempLabel, coolingControl);
        
        return serverBox;
    }
    
    /**
     * Start temperature simulation in a separate thread
     */
    private void startTemperatureSimulation() {
        temperatureThread = new Thread(() -> {
            while (running.get()) {
                try {
                    // Update temperatures
                    updateTemperatures();
                    
                    // Update UI
                    Platform.runLater(this::updateUI);
                    
                    // Check for server failure
                    boolean failed = checkServerFailure();
                    if (failed) {
                        running.set(false);
                    }
                    
                    // Check for task completion (all servers in safe range)
                    boolean allSafe = checkAllServersSafe();
                    if (allSafe) {
                        Platform.runLater(() -> {
                            statusLabel.setText("ALL SERVERS STABILIZED");
                            statusLabel.setTextFill(Color.web("#00FF00"));
                            
                            // Complete task after servers remain stable for a while
                            Timeline completeTimer = new Timeline(
                                new KeyFrame(Duration.seconds(3), e -> {
                                    CyberpunkEffects.styleCompletionEffect(taskPane);
                                    completeTask();
                                })
                            );
                            completeTimer.play();
                        });
                        running.set(false);
                    }
                    
                    // Sleep before next update
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        temperatureThread.setDaemon(true);
        temperatureThread.start();
    }
    
    /**
     * Update server temperatures based on cooling rates
     */
    private void updateTemperatures() {
        for (int i = 0; i < NUM_SERVERS; i++) {
            double currentTemp = serverTemperatures.get(i);
            double coolingRate = coolingRates.get(i);
            
            // Heat generation (varies by server)
            double heatGeneration = 0.5 + (i * 0.2) + (random.nextDouble() * 0.6);
            
            // Random temperature spike
            if (random.nextDouble() < 0.05) { // 5% chance
                heatGeneration += 1.0 + random.nextDouble() * 2.0;
                LOGGER.info("Temperature spike on server " + (i + 1));
            }
            
            // Calculate new temperature
            double tempChange = heatGeneration - coolingRate;
            double newTemp = currentTemp + tempChange;
            
            // Clamp temperature
            newTemp = Math.max(20.0, Math.min(MAX_TEMPERATURE, newTemp));
            
            // Update temperature
            serverTemperatures.set(i, newTemp);
        }
        
        // Restore some power over time (cooling systems optimize)
        if (totalPower < 100.0 && !emergencyMode) {
            totalPower = Math.min(100.0, totalPower + 0.2);
        }
    }
    
    /**
     * Update the UI with current temperatures and status
     */
    private void updateUI() {
        // Update each server display
        for (int i = 0; i < NUM_SERVERS; i++) {
            double temp = serverTemperatures.get(i);
            
            // Update temperature bar
            tempBars.get(i).setProgress(temp / MAX_TEMPERATURE);
            tempBars.get(i).setStyle("-fx-accent: " + getTemperatureColorString(temp) + ";");
            
            // Update temperature label
            tempLabels.get(i).setText(String.format("%.1f°C", temp));
            tempLabels.get(i).setTextFill(getTemperatureColor(temp));
            
            // Update server visual
            Rectangle serverRect = serverRects.get(i);
            serverRect.setFill(getTemperatureColor(temp));
            
            // Pulse effect for critical temperature
            if (temp > CRITICAL_TEMP) {
                if (!tempLabels.get(i).getStyleClass().contains("critical")) {
                    tempLabels.get(i).getStyleClass().add("critical");
                    CyberpunkEffects.pulseNode(tempLabels.get(i));
                }
            } else {
                tempLabels.get(i).getStyleClass().remove("critical");
            }
        }
        
        // Update power display
        updatePowerDisplay();
        
        // Update status message
        int criticalCount = 0;
        int safeCount = 0;
        
        for (Double temp : serverTemperatures) {
            if (temp > CRITICAL_TEMP) {
                criticalCount++;
            }
            if (temp < SAFE_TEMP) {
                safeCount++;
            }
        }
        
        if (criticalCount > 0) {
            statusLabel.setText(criticalCount + " SERVER(S) CRITICAL!");
            statusLabel.setTextFill(Color.web("#FF0000"));
        } else if (safeCount == NUM_SERVERS) {
            statusLabel.setText("ALL SERVERS OPERATING NORMALLY");
            statusLabel.setTextFill(Color.web("#00FF00"));
        } else {
            statusLabel.setText("MONITORING SERVER TEMPERATURE");
            statusLabel.setTextFill(Color.web("#00FFFF"));
        }
    }
    
    /**
     * Update the power allocation display
     */
    private void updatePowerDisplay() {
        double powerRatio = totalPower / 100.0;
        powerBar.setProgress(powerRatio);
        powerLabel.setText(String.format("%.0f%%", totalPower));
        
        // Update power bar color based on remaining power
        if (powerRatio < 0.3) {
            powerBar.setStyle("-fx-accent: #FF0000;"); // Red
            powerLabel.setTextFill(Color.web("#FF0000"));
        } else if (powerRatio < 0.6) {
            powerBar.setStyle("-fx-accent: #FFA500;"); // Orange
            powerLabel.setTextFill(Color.web("#FFA500"));
        } else {
            powerBar.setStyle("-fx-accent: #39FF14;"); // Green
            powerLabel.setTextFill(Color.web("#39FF14"));
        }
    }
    
    /**
     * Activate emergency cooling for all servers
     */
    private void activateEmergencyCooling() {
        if (totalPower < 30.0) {
            // Not enough power for emergency cooling
            statusLabel.setText("INSUFFICIENT POWER FOR EMERGENCY COOLING");
            statusLabel.setTextFill(Color.web("#FF0000"));
            
            // Flash the power bar to indicate problem
            Timeline flashAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(powerBar.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(100), new KeyValue(powerBar.opacityProperty(), 0.3)),
                new KeyFrame(Duration.millis(200), new KeyValue(powerBar.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(300), new KeyValue(powerBar.opacityProperty(), 0.3)),
                new KeyFrame(Duration.millis(400), new KeyValue(powerBar.opacityProperty(), 1.0))
            );
            flashAnimation.play();
            
            return;
        }
        
        // Activate emergency cooling
        emergencyMode = true;
        totalPower -= 30.0;
        
        // Max cooling for all servers temporarily
        for (int i = 0; i < NUM_SERVERS; i++) {
            coolingRates.set(i, MAX_COOLING_EFFECT);
            coolingSliders.get(i).setValue(2.0);
        }
        
        statusLabel.setText("EMERGENCY COOLING ACTIVATED");
        statusLabel.setTextFill(Color.web("#00FFFF"));
        
        // Emergency cooling effect
        DropShadow blueGlow = new DropShadow();
        blueGlow.setColor(Color.web("#00FFFF"));
        blueGlow.setRadius(20);
        blueGlow.setSpread(0.5);
        taskPane.setEffect(blueGlow);
        
        // Reset after a few seconds
        Timeline resetTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> {
                emergencyMode = false;
                taskPane.setEffect(null);
                statusLabel.setText("EMERGENCY COOLING DEACTIVATED");
                
                // Reset sliders but keep cooling rates (they'll be adjusted by users)
                for (Slider slider : coolingSliders) {
                    slider.setValue(1.0);
                }
            })
        );
        resetTimeline.play();
    }
    
    /**
     * Auto-balance cooling resources based on server temperatures
     */
    private void autoBalanceCooling() {
        // Calculate total needed cooling based on temperatures
        double totalNeeded = 0;
        for (Double temp : serverTemperatures) {
            // More cooling needed for higher temperatures
            double needed = 0.5 + (temp / MAX_TEMPERATURE) * 1.5;
            totalNeeded += needed;
        }
        
        // Scale to available power
        double scaleFactor = Math.min(1.0, 4.0 / totalNeeded);
        
        // Allocate cooling rates proportionally
        for (int i = 0; i < NUM_SERVERS; i++) {
            double temp = serverTemperatures.get(i);
            double needed = 0.5 + (temp / MAX_TEMPERATURE) * 1.5;
            double newCooling = needed * scaleFactor;
            
            // Update cooling rate
            coolingRates.set(i, newCooling);
            
            // Update slider position (normalized to 0-2 range)
            double sliderValue = (newCooling - MIN_COOLING_EFFECT) / (MAX_COOLING_EFFECT - MIN_COOLING_EFFECT) * 2.0;
            coolingSliders.get(i).setValue(sliderValue);
        }
        
        // Recalculate total power usage
        totalPower = 100.0 - (coolingRates.stream().mapToDouble(Double::doubleValue).sum() * 25 - NUM_SERVERS * 25);
        updatePowerDisplay();
        
        statusLabel.setText("COOLING RESOURCES AUTO-BALANCED");
        statusLabel.setTextFill(Color.web("#00FFFF"));
    }
    
    /**
     * Check if any server has failed (temp > MAX_TEMPERATURE)
     */
    private boolean checkServerFailure() {
        for (int i = 0; i < NUM_SERVERS; i++) {
            if (serverTemperatures.get(i) >= MAX_TEMPERATURE) {
                // Server failure
                final int failedServer = i + 1;
                Platform.runLater(() -> {
                    statusLabel.setText("SERVER " + failedServer + " CRITICAL FAILURE!");
                    statusLabel.setTextFill(Color.web("#FF0000"));
                    
                    // Apply failure effect
                    CyberpunkEffects.styleFailureEffect(taskPane);
                    
                    // Failed task
                    failTask();
                });
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if all servers are in safe temperature range
     */
    private boolean checkAllServersSafe() {
        for (Double temp : serverTemperatures) {
            if (temp > SAFE_TEMP) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get color based on temperature
     */
    private Color getTemperatureColor(double temperature) {
        if (temperature >= CRITICAL_TEMP) {
            return Color.web("#FF0000"); // Red
        } else if (temperature >= 80.0) {
            return Color.web("#FF6600"); // Orange
        } else if (temperature >= SAFE_TEMP) {
            return Color.web("#FFCC00"); // Yellow
        } else {
            return Color.web("#00CC00"); // Green
        }
    }
    
    /**
     * Get color string based on temperature
     */
    private String getTemperatureColorString(double temperature) {
        if (temperature >= CRITICAL_TEMP) {
            return "#FF0000"; // Red
        } else if (temperature >= 80.0) {
            return "#FF6600"; // Orange
        } else if (temperature >= SAFE_TEMP) {
            return "#FFCC00"; // Yellow
        } else {
            return "#00CC00"; // Green
        }
    }
    
    /**
     * Clean up resources when task is done
     */
    private void cleanupResources() {
        // Stop temperature thread
        running.set(false);
        if (temperatureThread != null) {
            temperatureThread.interrupt();
        }
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