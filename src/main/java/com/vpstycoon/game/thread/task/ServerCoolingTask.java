package com.vpstycoon.game.thread.task;

import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
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

public class ServerCoolingTask extends GameTask {

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
    private Thread autoAssistThread;

    private double managementEfficiency = 1.0;
    private int managementLevel = 1;

    public ServerCoolingTask() {
        super(
                "Server Cooling",
                "Manage cooling systems to prevent server overheating",
                "/images/task/cooling_task.png",
                6500, 
                30,   
                3,    
                60    
        );
        
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        if (skillPointsSystem != null) {
            managementEfficiency = skillPointsSystem.getManagementEfficiency();
            managementLevel = skillPointsSystem.getSkillLevel(SkillPointsSystem.SkillType.MANAGEMENT);
        }
        
        for (int i = 0; i < NUM_SERVERS; i++) {
            
            double temperatureReduction = (managementEfficiency - 1.0) * 15.0;
            double baseTemp = 60.0 + random.nextDouble() * 25.0;
            double adjustedTemp = Math.max(50.0, baseTemp - temperatureReduction);
            serverTemperatures.add(adjustedTemp);
            coolingRates.add(1.0); 
        }
    }

    @Override
    protected void initializeTaskSpecifics() {
        
        taskPane = new BorderPane();
        CyberpunkEffects.styleTaskPane(taskPane);
        taskPane.setPrefSize(600, 450);
        taskPane.setPadding(new Insets(20));
        
        CyberpunkEffects.addAnimatedBackground(taskPane);

        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        Text titleText = CyberpunkEffects.createTaskTitle("SERVER COOLING MANAGEMENT");
        Text descText = CyberpunkEffects.createTaskDescription("Balance cooling resources to stabilize server temperatures");

        if (managementLevel > 1) {
            Text managementText = new Text("Management Skill Level " + managementLevel + 
                                         " (+" + (int)((managementEfficiency - 1.0) * 100) + "% Efficiency)");
            managementText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
            managementText.setFill(Color.web("#39FF14"));
            headerBox.getChildren().addAll(titleText, descText, managementText);
        } else {
            headerBox.getChildren().addAll(titleText, descText);
        }

        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);

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

        GridPane serverGrid = new GridPane();
        serverGrid.setHgap(15);
        serverGrid.setVgap(20);
        serverGrid.setAlignment(Pos.CENTER);

        for (int i = 0; i < NUM_SERVERS; i++) {
            VBox serverBox = createServerBox(i);
            serverGrid.add(serverBox, i % 2, i / 2);
        }
        
        centerContent.getChildren().add(serverGrid);

        statusLabel = new Label("MONITORING SERVER TEMPERATURE");
        statusLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web("#00FFFF"));
        CyberpunkEffects.pulseNode(statusLabel);
        centerContent.getChildren().add(statusLabel);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button emergencyButton = CyberpunkEffects.createCyberpunkButton("EMERGENCY COOLING", false);
        emergencyButton.setOnAction(e -> activateEmergencyCooling());
        
        Button balanceButton = CyberpunkEffects.createCyberpunkButton("AUTO-BALANCE", true);
        balanceButton.setOnAction(e -> autoBalanceCooling());
        
        buttonBox.getChildren().addAll(emergencyButton, balanceButton);
        centerContent.getChildren().add(buttonBox);
        
        taskPane.setCenter(centerContent);

        CyberpunkEffects.addScanningEffect(taskPane);

        gamePane.getChildren().add(taskPane);

        startTemperatureSimulation();

        if (managementLevel >= 3) {
            startAutoAssist();
        }
    }

    private VBox createServerBox(int serverIndex) {

        VBox serverBox = new VBox(10);
        serverBox.setAlignment(Pos.CENTER);
        serverBox.setPadding(new Insets(10));
        serverBox.setStyle("-fx-background-color: rgba(20, 20, 40, 0.7); -fx-border-color: #303060; -fx-border-width: 1px;");

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
        
        ProgressBar tempBar = new ProgressBar(serverTemperatures.get(serverIndex) / MAX_TEMPERATURE);
        tempBar.setPrefWidth(120);
        tempBar.setStyle("-fx-accent: " + getTemperatureColorString(serverTemperatures.get(serverIndex)) + ";");
        tempBars.add(tempBar);
        
        Label tempLabel = new Label(String.format("%.1f°C", serverTemperatures.get(serverIndex)));
        tempLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        tempLabel.setTextFill(getTemperatureColor(serverTemperatures.get(serverIndex)));
        tempLabels.add(tempLabel);

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
        
        final int index = serverIndex;
        coolingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double oldCooling = coolingRates.get(index);
            double newCooling = MIN_COOLING_EFFECT + newVal.doubleValue() * (MAX_COOLING_EFFECT - MIN_COOLING_EFFECT) / 2.0;
            
            totalPower -= (newCooling - oldCooling) * 25;
            updatePowerDisplay();
            
            coolingRates.set(index, newCooling);
        });
        
        coolingControl.getChildren().addAll(coolingLabel, coolingSlider);
        
        serverBox.getChildren().addAll(serverVisual, tempBar, tempLabel, coolingControl);

        return serverBox;
    }
    

    private void startTemperatureSimulation() {
        temperatureThread = new Thread(() -> {
            while (running.get()) {
                try {
                    
                    updateTemperatures();

                    if (checkServerFailure()) {
                        break;
                    }
                    
                    if (checkAllServersSafe()) {
                        completed = true;
                        Platform.runLater(this::completeTask);
                        break;
                    }

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

    private void updateTemperatures() {
        Platform.runLater(() -> {

            double baseHeatIncrease = 0.3 / managementEfficiency;
            
            for (int i = 0; i < NUM_SERVERS; i++) {
                
                double heatChange = baseHeatIncrease - coolingRates.get(i) * 0.15;
                

                double newTemp = serverTemperatures.get(i) + heatChange;


                newTemp = Math.max(20.0, Math.min(MAX_TEMPERATURE, newTemp));


                serverTemperatures.set(i, newTemp);


                updateServerUI(i);
            }
        });
    }

    private void updatePowerDisplay() {
        double powerRatio = totalPower / 100.0;
        powerBar.setProgress(powerRatio);
        powerLabel.setText(String.format("%.0f%%", totalPower));
        
        
        if (powerRatio < 0.3) {
            powerBar.setStyle("-fx-accent: #FF0000;"); 
            powerLabel.setTextFill(Color.web("#FF0000"));
        } else if (powerRatio < 0.6) {
            powerBar.setStyle("-fx-accent: #FFA500;"); 
            powerLabel.setTextFill(Color.web("#FFA500"));
        } else {
            powerBar.setStyle("-fx-accent: #39FF14;"); 
            powerLabel.setTextFill(Color.web("#39FF14"));
        }
    }

    private void activateEmergencyCooling() {
        
        double emergencyCost = 30.0 / managementEfficiency;
        
        if (totalPower < emergencyCost) {
            
            statusLabel.setText("INSUFFICIENT POWER FOR EMERGENCY COOLING");
            statusLabel.setTextFill(Color.web("#FF0000"));
            
            
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

        emergencyMode = true;
        totalPower -= emergencyCost;

        double coolingBoost = MAX_COOLING_EFFECT * (1.0 + (managementEfficiency - 1.0) * 0.5);
        
        for (int i = 0; i < NUM_SERVERS; i++) {
            coolingRates.set(i, coolingBoost);
            coolingSliders.get(i).setValue(2.0);
        }
        
        statusLabel.setText("EMERGENCY COOLING ACTIVATED");
        statusLabel.setTextFill(Color.web("#00FFFF"));

        DropShadow blueGlow = new DropShadow();
        blueGlow.setColor(Color.web("#00FFFF"));
        blueGlow.setRadius(20);
        blueGlow.setSpread(0.5);
        taskPane.setEffect(blueGlow);

        int duration = 5 + (int)((managementEfficiency - 1.0) * 5.0);
        
        Timeline resetTimeline = new Timeline(
            new KeyFrame(Duration.seconds(duration), e -> {
                emergencyMode = false;
                taskPane.setEffect(null);
                statusLabel.setText("EMERGENCY COOLING DEACTIVATED");
                
                
                for (Slider slider : coolingSliders) {
                    slider.setValue(1.0);
                }
            })
        );
        resetTimeline.play();
    }
    
    
    private void autoBalanceCooling() {
        
        double totalNeeded = 0;
        for (Double temp : serverTemperatures) {
            
            double needed = 0.5 + (temp / MAX_TEMPERATURE) * 1.5;
            totalNeeded += needed;
        }

        double autoBalanceBonus = 1.0 + (managementEfficiency - 1.0) * 0.5;
        double scaleFactor = Math.min(1.0, 4.0 * autoBalanceBonus / totalNeeded);

        for (int i = 0; i < NUM_SERVERS; i++) {
            double temp = serverTemperatures.get(i);
            double needed = 0.5 + (temp / MAX_TEMPERATURE) * 1.5;
            double newCooling = needed * scaleFactor;

            coolingRates.set(i, newCooling);

            double sliderValue = (newCooling - MIN_COOLING_EFFECT) / (MAX_COOLING_EFFECT - MIN_COOLING_EFFECT) * 2.0;
            coolingSliders.get(i).setValue(sliderValue);
        }

        double powerUsage = coolingRates.stream().mapToDouble(Double::doubleValue).sum() * 25 - NUM_SERVERS * 25;
        
        powerUsage = powerUsage / (1.0 + (managementEfficiency - 1.0) * 0.3);
        
        totalPower = 100.0 - powerUsage;
        updatePowerDisplay();
        
        statusLabel.setText("COOLING RESOURCES AUTO-BALANCED");
        statusLabel.setTextFill(Color.web("#00FFFF"));
    }

    private boolean checkServerFailure() {
        for (int i = 0; i < NUM_SERVERS; i++) {
            if (serverTemperatures.get(i) >= MAX_TEMPERATURE) {
                
                final int failedServer = i + 1;
                Platform.runLater(() -> {
                    statusLabel.setText("SERVER " + failedServer + " CRITICAL FAILURE!");
                    statusLabel.setTextFill(Color.web("#FF0000"));

                    CyberpunkEffects.styleFailureEffect(taskPane);

                    failTask();
                });
                return true;
            }
        }
        return false;
    }

    private boolean checkAllServersSafe() {
        for (Double temp : serverTemperatures) {
            if (temp > SAFE_TEMP) {
                return false;
            }
        }
        return true;
    }

    private Color getTemperatureColor(double temperature) {
        if (temperature >= CRITICAL_TEMP) {
            return Color.web("#FF0000"); 
        } else if (temperature >= 80.0) {
            return Color.web("#FF6600"); 
        } else if (temperature >= SAFE_TEMP) {
            return Color.web("#FFCC00"); 
        } else {
            return Color.web("#00CC00"); 
        }
    }

    private String getTemperatureColorString(double temperature) {
        if (temperature >= CRITICAL_TEMP) {
            return "#FF0000"; 
        } else if (temperature >= 80.0) {
            return "#FF6600"; 
        } else if (temperature >= SAFE_TEMP) {
            return "#FFCC00"; 
        } else {
            return "#00CC00"; 
        }
    }

    private void cleanupResources() {
        running.set(false);
        if (temperatureThread != null) {
            temperatureThread.interrupt();
        }
        if (autoAssistThread != null) {
            autoAssistThread.interrupt();
        }
    }

    @Override
    protected void cleanupTask() {
        cleanupResources();
        super.cleanupTask();
    }

    private void updateServerUI(int serverIndex) {
        
        tempBars.get(serverIndex).setProgress(serverTemperatures.get(serverIndex) / MAX_TEMPERATURE);
        tempBars.get(serverIndex).setStyle("-fx-accent: " + 
            getTemperatureColorString(serverTemperatures.get(serverIndex)) + ";");

        tempLabels.get(serverIndex).setText(String.format("%.1f°C", serverTemperatures.get(serverIndex)));
        tempLabels.get(serverIndex).setTextFill(getTemperatureColor(serverTemperatures.get(serverIndex)));

        serverRects.get(serverIndex).setFill(getTemperatureColor(serverTemperatures.get(serverIndex)));

        if (serverTemperatures.get(serverIndex) >= CRITICAL_TEMP) {
            if (!tempLabels.get(serverIndex).getStyleClass().contains("critical")) {
                tempLabels.get(serverIndex).getStyleClass().add("critical");
                CyberpunkEffects.pulseNode(tempLabels.get(serverIndex));
            }
        } else {
            tempLabels.get(serverIndex).getStyleClass().remove("critical");
        }
    }

    private void startAutoAssist() {
        autoAssistThread = new Thread(() -> {
            while (running.get()) {
                try {

                    double assistChance = managementLevel == 3 ? 0.15 : 0.30;
                    
                    if (random.nextDouble() < assistChance) {
                        
                        int hottestServerIndex = findHottestServerIndex();
                        if (hottestServerIndex >= 0) {
                            
                            Platform.runLater(() -> {
                                
                                double currentCooling = coolingRates.get(hottestServerIndex);
                                double assistCooling = MAX_COOLING_EFFECT * 1.5;
                                coolingRates.set(hottestServerIndex, assistCooling);
                                
                                
                                animateSliderValue(coolingSliders.get(hottestServerIndex), 2.0);
                                
                                
                                String assistMessage = managementLevel == 3 ? 
                                    "Management Assistant stabilizing Server " + (hottestServerIndex + 1) : 
                                    "Advanced Management AI optimizing Server " + (hottestServerIndex + 1);
                                
                                
                                showAssistNotification(hottestServerIndex, assistMessage);
                                
                                
                                Timeline resetTimeline = new Timeline(
                                    new KeyFrame(Duration.seconds(3), e -> {
                                        coolingRates.set(hottestServerIndex, currentCooling);
                                        animateSliderValue(coolingSliders.get(hottestServerIndex), 
                                            (currentCooling - MIN_COOLING_EFFECT) / (MAX_COOLING_EFFECT - MIN_COOLING_EFFECT) * 2.0);
                                    })
                                );
                                resetTimeline.play();
                            });
                        }
                    }
                    
                    
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        autoAssistThread.setDaemon(true);
        autoAssistThread.start();
    }

    
    private int findHottestServerIndex() {
        int hottestIndex = -1;
        double maxTemp = SAFE_TEMP; 
        
        for (int i = 0; i < serverTemperatures.size(); i++) {
            if (serverTemperatures.get(i) > maxTemp) {
                maxTemp = serverTemperatures.get(i);
                hottestIndex = i;
            }
        }
        
        return hottestIndex;
    }

    private void animateSliderValue(Slider slider, double targetValue) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(slider.valueProperty(), slider.getValue())),
            new KeyFrame(Duration.millis(500), new KeyValue(slider.valueProperty(), targetValue))
        );
        timeline.play();
    }

    private void showAssistNotification(int serverIndex, String message) {
        
        Label assistLabel = new Label(message);
        assistLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 12));
        assistLabel.setTextFill(Color.web("#00FFFF"));
        assistLabel.setStyle("-fx-background-color: rgba(0, 30, 60, 0.7); -fx-padding: 5px;");
        
        Glow glow = new Glow(0.8);
        assistLabel.setEffect(glow);
        
        Bounds serverBounds = serverRects.get(serverIndex).localToScene(serverRects.get(serverIndex).getBoundsInLocal());
        
        StackPane.setMargin(assistLabel, new Insets(
            serverBounds.getMinY() - 30, 0, 0, serverBounds.getMinX() - 20
        ));
        
        gamePane.getChildren().add(assistLabel);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(2.5), assistLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(1.5));
        fadeOut.setOnFinished(e -> gamePane.getChildren().remove(assistLabel));
        fadeOut.play();
    }
} 

