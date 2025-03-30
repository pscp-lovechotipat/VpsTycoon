package com.vpstycoon.game.thread.task;

import com.vpstycoon.application.FontLoader;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class GameTask {
    protected final ResourceManager resourceManager = ResourceManager.getInstance();
    protected final Random random = new Random();
    
    private String taskName;
    private String taskDescription;
    private String imagePath;
    private int rewardAmount;
    private int penaltyRating;
    private int difficultyLevel;
    private int timeLimit;
    protected boolean completed = false;
    protected boolean failed = false;
    protected Runnable onCompleteCallback;
    
    
    protected double managementEfficiency = 1.0;
    protected int managementLevel = 1;

    
    protected StackPane taskContainer;
    
    
    protected ProgressBar timerBar;
    protected Label timerLabel;
    
    
    protected Pane taskPane;
    protected BorderPane contentPane;
    protected StackPane gamePane;
    protected VBox controlPane;
    
    
    protected Stage taskStage;

    
    private static boolean isTaskActive = false;

    
    private static final Object taskLock = new Object();
    
    
    private Thread timerThread;
    
    
    private AudioClip taskStartSound;
    private AudioClip taskCompleteSound;
    private AudioClip taskFailSound;
    private AudioClip taskTickSound;

    
    public GameTask(String taskName, String taskDescription, String imagePath, 
                    int rewardAmount, int penaltyRating, int difficultyLevel, int timeLimit) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.imagePath = imagePath;
        this.rewardAmount = rewardAmount;
        this.penaltyRating = penaltyRating;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
        
        
        loadSoundEffects();
        
        
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        if (skillPointsSystem != null) {
            managementEfficiency = skillPointsSystem.getManagementEfficiency();
            managementLevel = skillPointsSystem.getSkillLevel(SkillPointsSystem.SkillType.MANAGEMENT);
            
            
            if (managementLevel > 1) {
                timeLimit = (int)(timeLimit * (1.0 + (managementEfficiency - 1.0) * 0.5));
            }
        }
    }

    
    private void loadSoundEffects() {
        
        String[][] soundPaths = {
            
            {"/sounds/task_start.mp3", "/audio/task_start.mp3", "/sfx/task_start.mp3"},
            {"/sounds/task_complete.mp3", "/audio/task_complete.mp3", "/sfx/task_complete.mp3"},
            {"/sounds/task_fail.mp3", "/audio/task_fail.mp3", "/sfx/task_fail.mp3"},
            {"/sounds/task_tick.mp3", "/audio/task_tick.mp3", "/sfx/task_tick.mp3"}
        };
        
        try {
            
            taskStartSound = loadSoundWithFallback(soundPaths[0]);
            
            
            taskCompleteSound = loadSoundWithFallback(soundPaths[1]);
            
            
            taskFailSound = loadSoundWithFallback(soundPaths[2]);
            
            
            taskTickSound = loadSoundWithFallback(soundPaths[3]);
        } catch (Exception e) {
            log("Could not load sound effects: " + e.getMessage());
        }
    }
    
    
    private AudioClip loadSoundWithFallback(String[] paths) {
        for (String path : paths) {
            try {
                java.net.URL resource = getClass().getResource(path);
                if (resource != null) {
                    return new AudioClip(resource.toExternalForm());
                }
            } catch (Exception e) {
                
            }
        }
        
        
        return null;
    }
    
    
    private void safePlaySound(AudioClip clip, double volume) {
        if (clip != null) {
            try {
                clip.play(volume);
            } catch (Exception e) {
                log("Error playing sound: " + e.getMessage());
            }
        }
    }

    
    public void setTaskContainer(StackPane container) {
        this.taskContainer = container;
    }

    
    public static boolean isTaskActive() {
        synchronized(taskLock) {
            return isTaskActive;
        }
    }

    
    protected static void setTaskActive(boolean active) {
        synchronized(taskLock) {
            isTaskActive = active;
        }
    }

    
    public void showTask(Runnable onComplete) {
        try {
            
            synchronized(taskLock) {
                if (isTaskActive) {
                    log("Cannot show task. Another task is currently active.");
                    return;
                }
                
                
                if (taskContainer == null) {
                    log("Task container is null. Cannot show task.");
                    return;
                }
                
                
                isTaskActive = true;
            }
            
            log("Starting task: " + getTaskName());
            this.onCompleteCallback = onComplete;
            
            
            safePlaySound(taskStartSound, 0.8);
            
            
            Platform.runLater(() -> {
                try {
                    System.out.println("[GAMETASK] Initializing task UI for: " + getTaskName());
                    
                    
                    if (taskContainer == null) {
                        System.err.println("[GAMETASK] Error: taskContainer became null, aborting");
                        cleanupTask();
                        return;
                    }
                    
                    
                    taskContainer.getChildren().clear();
                    
                    
                    initializeUI();
                    
                    
                    initializeTaskSpecifics();
                    
                    
                    if (taskPane == null) {
                        System.err.println("[GAMETASK] Error: taskPane is null after initialization");
                        cleanupTask();
                        return;
                    }
                    
                    
                    StackPane.setAlignment(taskPane, Pos.CENTER);
                    
                    
                    System.out.println("[GAMETASK] Task pane size: " + 
                                      taskPane.getPrefWidth() + "x" + taskPane.getPrefHeight());
                    
                    
                    taskContainer.getChildren().add(taskPane);
                    
                    
                    taskContainer.setVisible(true);
                    
                    
                    taskPane.setVisible(true);
                    
                    
                    startTimer();
                    
                    debugEvent("Task timeout", getTimeLimit());
                    
                    System.out.println("[GAMETASK] Task UI successfully displayed for: " + getTaskName());
                } catch (Exception e) {
                    System.err.println("[GAMETASK] Error initializing task UI: " + e.getMessage());
                    e.printStackTrace();
                    cleanupTask();
                    
                    
                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[GAMETASK] Error showing task: " + e.getMessage());
            e.printStackTrace();
            
            
            synchronized(taskLock) {
                isTaskActive = false;
            }
            
            
            if (onCompleteCallback != null) {
                try {
                    onCompleteCallback.run();
                } catch (Exception ignored) {
                    
                }
            }
        }
    }
    
    
    protected void initializeUI() {
        
        StackPane mainContainer = new StackPane();
        mainContainer.setAlignment(Pos.CENTER);
        
        
        mainContainer.setPrefSize(800, 650);
        mainContainer.setMaxSize(800, 700);
        mainContainer.setMinSize(800, 700);

        
        contentPane = new BorderPane();
        contentPane.setPrefSize(800, 580); 
        contentPane.setMaxSize(800, 580); 
        
        
        CyberpunkEffects.styleTaskPane(contentPane);
        
        
        Text titleText = CyberpunkEffects.createTaskTitle(taskName);
        Text descText = CyberpunkEffects.createTaskDescription(taskDescription);
        
        
        HBox difficultyBox = new HBox(5);
        difficultyBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            Rectangle dot = new Rectangle(10, 10);
            dot.setArcWidth(10);
            dot.setArcHeight(10);
            
            if (i < difficultyLevel) {
                
                Color dotColor;
                switch (difficultyLevel) {
                    case 1: dotColor = Color.LIGHTGREEN; break;
                    case 2: dotColor = Color.LIGHTBLUE; break;
                    case 3: dotColor = Color.YELLOW; break;
                    case 4: dotColor = Color.ORANGE; break;
                    case 5: dotColor = Color.RED; break;
                    default: dotColor = Color.GRAY;
                }
                dot.setFill(dotColor);
                
                
                DropShadow glow = new DropShadow();
                glow.setRadius(5);
                glow.setColor(dotColor);
                dot.setEffect(glow);
            } else {
                dot.setFill(Color.GRAY.darker());
            }
            
            difficultyBox.getChildren().add(dot);
        }
        
        
        timerBar = new ProgressBar(1.0);
        timerBar.setPrefWidth(700);
        
        
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#00FFFF")),   
            new Stop(0.5, Color.web("#FFFFFF")), 
            new Stop(1, Color.web("#FF00FF"))    
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        
        
        Rectangle timerFill = new Rectangle(0, 0, 700, 15);
        timerFill.setFill(gradient);
        timerBar.setStyle(
            "-fx-accent: " + toRGBCode(gradient.getStops().get(0).getColor()) + ";" +
            "-fx-background-color: #1a1a2a;" +
            "-fx-border-color: #333366;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 2px;"
        );
        
        timerLabel = CyberpunkEffects.createGlowingLabel(timeLimit + "s", "#00FFFF");
        
        
        Button closeButton = CyberpunkEffects.createCyberpunkButton("ABORT TASK", false);
        closeButton.setOnAction(e -> {
            
            failed = true;
            
            
            CyberpunkEffects.styleFailureEffect(gamePane);
            
            
            applyPenalty();
            
            
            cleanupTask();
            
            
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }
        });
        
        
        Label rewardLabel = new Label("REWARD: " + rewardAmount);

        rewardLabel.setFont(FontLoader.SECTION_FONT);
        rewardLabel.setTextFill(Color.web("#39FF14")); 
        
        
        CyberpunkEffects.pulseNode(rewardLabel);
        
        
        VBox headerBox = new VBox(5); 
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10)); 
        headerBox.getChildren().addAll(titleText, descText, difficultyBox);
        headerBox.setMaxHeight(120); 
        
        
        gamePane = new StackPane();
        gamePane.setStyle("-fx-background-color: rgba(0, 20, 40, 0.8);");
        gamePane.setPadding(new Insets(10)); 
        gamePane.setMinHeight(300); 
        gamePane.setMaxHeight(480); 
        
        
        CyberpunkEffects.addScanningEffect(gamePane);
        
        
        controlPane = new VBox(10); 
        controlPane.setAlignment(Pos.CENTER);
        controlPane.setPadding(new Insets(10)); 
        
        
        contentPane.setTop(headerBox);
        contentPane.setCenter(gamePane);
        
        
        StackPane footerPane = new StackPane();
        footerPane.setAlignment(Pos.BOTTOM_CENTER);
        footerPane.setPrefHeight(100);
        footerPane.setMaxHeight(100);
        footerPane.setStyle("-fx-background-color: rgba(10, 15, 30, 0.9); -fx-border-color: #00ffff; -fx-border-width: 1px 0 0 0;");
        
        
        VBox footerContent = new VBox(5);
        footerContent.setAlignment(Pos.CENTER);
        footerContent.setPadding(new Insets(10));
        
        HBox timerBox = new HBox(10);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.getChildren().addAll(timerBar, timerLabel);
        
        HBox actionBox = new HBox(30);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.getChildren().addAll(rewardLabel, closeButton);
        
        footerContent.getChildren().addAll(timerBox, actionBox);
        footerPane.getChildren().add(footerContent);
        
        
        headerBox.setPadding(new Insets(15,0,0,0)); 
        
        
        BorderPane.setAlignment(headerBox, Pos.TOP_CENTER);
        BorderPane.setAlignment(gamePane, Pos.CENTER);
        
        
        StackPane.setAlignment(contentPane, Pos.TOP_CENTER);
        StackPane.setAlignment(footerPane, Pos.BOTTOM_CENTER);
        
        
        mainContainer.getChildren().addAll(contentPane, footerPane);
        
        
        StackPane.setAlignment(mainContainer, Pos.CENTER);
        
        
        mainContainer.setScaleX(0.99);
        mainContainer.setScaleY(0.99);
        mainContainer.setPadding(new Insets(20));
        
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setSpread(0.2);
        shadow.setColor(Color.BLACK);
        mainContainer.setEffect(shadow);
        
        
        contentPane.getStyleClass().add("cyberpunk-task");
        
        
        
        this.taskPane = mainContainer;
    }
    
    
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    
    protected abstract void initializeTaskSpecifics();
    
    
    private void startTimer() {
        timerThread = new Thread(() -> {
            int remainingTime = timeLimit;
            AtomicBoolean playedWarningSound = new AtomicBoolean(false);
            
            while (remainingTime > 0 && !completed && !failed) {
                final int currentTime = remainingTime;
                Platform.runLater(() -> {
                    if (timerLabel != null) {
                        timerLabel.setText(currentTime + "s");
                    }
                    if (timerBar != null) {
                        double progress = (double) currentTime / timeLimit;
                        timerBar.setProgress(progress);
                        
                        
                        if (progress < 0.25) {
                            timerBar.setStyle("-fx-accent: #ff0000; -fx-background-color: #1a1a2a;"); 
                            timerLabel.setTextFill(Color.RED);
                            if (!playedWarningSound.get()) {
                                safePlaySound(taskTickSound, 0.5);
                            }
                        } else if (progress < 0.5) {
                            timerBar.setStyle("-fx-accent: #ffff00; -fx-background-color: #1a1a2a;"); 
                            timerLabel.setTextFill(Color.YELLOW);
                        } else {
                            timerBar.setStyle("-fx-accent: #00ffff; -fx-background-color: #1a1a2a;"); 
                            timerLabel.setTextFill(Color.CYAN);
                        }
                    }
                });
                
                try {
                    Thread.sleep(1000);
                    
                    
                    if (remainingTime <= 5) {
                        playedWarningSound.set(true);
                        safePlaySound(taskTickSound, 0.3);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("Timer interrupted");
                    break;
                }
                remainingTime--;
            }
            
            if (!completed && !failed) {
                failed = true;
                log("Time expired, task failed");
                Platform.runLater(() -> {
                    applyPenalty();
                    cleanupTask();
                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }
                });
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }
    
    
    protected void applyReward() {
        GameState gameState = resourceManager.getCurrentState();
        Company company = gameState.getCompany();
        
        
        long adjustedReward = (long)(rewardAmount * managementEfficiency);
        
        company.setMoney(company.getMoney() + adjustedReward);
        
        
        if (managementLevel > 1 && random.nextDouble() < 0.25 * (managementLevel - 1)) {
            SkillPointsSystem skillSystem = ResourceManager.getInstance().getSkillPointsSystem();
            if (skillSystem != null) {
                skillSystem.addPoints(1);
            }
        }
        
        
        safePlaySound(taskCompleteSound, 0.8);
        
        Platform.runLater(() -> {
            try {
                resourceManager.getAudioManager().playSoundEffect("reward.mp3");
            } catch (Exception e) {
                log("Error playing reward sound: " + e.getMessage());
            }
            
            String rewardMsg = "Well done cyberpunk! You've completed the task.\nReward: $" + adjustedReward;
            
            
            if (managementLevel > 1) {
                int bonusPercent = (int)((managementEfficiency - 1.0) * 100);
                rewardMsg += " (includes " + bonusPercent + "% Management bonus)";
            }
        });
    }
    
    
    protected void applyPenalty() {
        GameState gameState = resourceManager.getCurrentState();
        Company company = gameState.getCompany();
        
        
        double penaltyReduction = Math.min(0.5, (managementEfficiency - 1.0) * 0.5);
        double adjustedPenalty = penaltyRating * (1.0 - penaltyReduction);
        
        
        adjustedPenalty = Math.max(1.0, adjustedPenalty);
        
        double newRating = Math.max(1.0, company.getRating() - adjustedPenalty);
        company.setRating(newRating);
        
        
        safePlaySound(taskFailSound, 0.8);
        
        Platform.runLater(() -> {
            try {
                resourceManager.getAudioManager().playSoundEffect("failure.mp3");
            } catch (Exception e) {
                log("Error playing failure sound: " + e.getMessage());
            }
            
            String penaltyMsg = "You failed to complete the task in time.\nRating dropped to: " + String.format("%.1f", newRating);
            
            
            if (managementLevel > 1) {
                int protectionPercent = (int)(penaltyReduction * 100);
                penaltyMsg += " (" + protectionPercent + "% penalty reduction from Management skill)";
            }
            
            resourceManager.pushCenterNotification(
                "Task Failed: " + taskName,
                penaltyMsg,
                "/images/notification/failure.png"
            );
        });
    }
    
    
    protected void completeTask() {
        if (!completed && !failed) {
            completed = true;
            log("Task completed successfully");
            
            
            Platform.runLater(() -> {
                if (gamePane != null) {
                    CyberpunkEffects.styleCompletionEffect(gamePane);
                }
            });
            
            applyReward();
            Platform.runLater(() -> {
                cleanupTask();
                if (onCompleteCallback != null) {
                    onCompleteCallback.run();
                }
            });
        }
    }
    
    
    protected void failTask() {
        if (!completed && !failed) {
            failed = true;
            log("Task failed");
            
            
            Platform.runLater(() -> {
                if (gamePane != null) {
                    CyberpunkEffects.styleFailureEffect(gamePane);
                }
            });
            
            applyPenalty();
            Platform.runLater(() -> {
                cleanupTask();
                if (onCompleteCallback != null) {
                    onCompleteCallback.run();
                }
            });
        }
    }
    
    
    public String getTaskName() {
        return taskName;
    }
    
    public String getTaskDescription() {
        return taskDescription;
    }
    
    public String getTaskImage() {
        return imagePath;
    }
    
    public long getRewardAmount() {
        return rewardAmount;
    }
    
    public double getPenaltyRating() {
        return penaltyRating;
    }
    
    public int getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public boolean isFailed() {
        return failed;
    }

    
    protected void log(String message) {
        System.out.println("[" + getTaskName() + "] " + message);
    }
    
    protected void debugEvent(String eventName, int secondsFromNow) {
        System.out.println("[DEBUG][" + getTaskName() + "] " + eventName + " จะเกิดขึ้นในอีก " + secondsFromNow + " วินาที");
    }

    
    protected void cleanupTask() {
        
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        
        
        synchronized(taskLock) {
            isTaskActive = false;
        }
        
        
        timerBar = null;
        timerLabel = null;
        gamePane = null;
        controlPane = null;
        contentPane = null;
        taskPane = null;
    }
} 

