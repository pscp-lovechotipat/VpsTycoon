package com.vpstycoon.game.thread.task;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vpstycoon.game.company.SkillPointsSystem;

/**
 * Abstract class representing a game task in cyberpunk theme similar to Among Us tasks.
 * Each task has a UI component that can be displayed to the player.
 */
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
    
    // Management skill effect
    protected double managementEfficiency = 1.0;
    protected int managementLevel = 1;

    // New fields for overlay approach
    protected StackPane taskContainer;
    
    // Timer components
    protected ProgressBar timerBar;
    protected Label timerLabel;
    
    // Content panes - for subclasses to use
    protected Pane taskPane;
    protected BorderPane contentPane;
    protected StackPane gamePane;
    protected VBox controlPane;
    
    // Stage for the task window
    protected Stage taskStage;

    // สถานะการแสดงของ container
    private static boolean isTaskActive = false;

    // Lock object สำหรับ synchronize การทำงาน
    private static final Object taskLock = new Object();
    
    // Thread สำหรับ timer
    private Thread timerThread;
    
    // Sound effects
    private AudioClip taskStartSound;
    private AudioClip taskCompleteSound;
    private AudioClip taskFailSound;
    private AudioClip taskTickSound;

    /**
     * Constructor for a game task
     * 
     * @param taskName The name of the task
     * @param taskDescription A brief description of what the player needs to do
     * @param imagePath Path to an image icon for the task
     * @param rewardAmount Money reward for completing the task
     * @param penaltyRating Rating penalty for failing the task
     * @param difficultyLevel Difficulty level (1-5)
     * @param timeLimit Time limit in seconds
     */
    public GameTask(String taskName, String taskDescription, String imagePath, 
                    int rewardAmount, int penaltyRating, int difficultyLevel, int timeLimit) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.imagePath = imagePath;
        this.rewardAmount = rewardAmount;
        this.penaltyRating = penaltyRating;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
        
        // Load sound effects with multiple fallback paths
        loadSoundEffects();
        
        // Get management efficiency from SkillPointsSystem
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        if (skillPointsSystem != null) {
            managementEfficiency = skillPointsSystem.getManagementEfficiency();
            managementLevel = skillPointsSystem.getSkillLevel(SkillPointsSystem.SkillType.MANAGEMENT);
            
            // Adjust time limit based on management efficiency (higher efficiency = more time)
            if (managementLevel > 1) {
                timeLimit = (int)(timeLimit * (1.0 + (managementEfficiency - 1.0) * 0.5));
            }
        }
    }

    /**
     * Load sound effects with fallback paths
     */
    private void loadSoundEffects() {
        // List of possible paths to try for each sound
        String[][] soundPaths = {
            // Main path, fallback paths
            {"/sounds/task_start.mp3", "/audio/task_start.mp3", "/sfx/task_start.mp3"},
            {"/sounds/task_complete.mp3", "/audio/task_complete.mp3", "/sfx/task_complete.mp3"},
            {"/sounds/task_fail.mp3", "/audio/task_fail.mp3", "/sfx/task_fail.mp3"},
            {"/sounds/task_tick.mp3", "/audio/task_tick.mp3", "/sfx/task_tick.mp3"}
        };
        
        try {
            // Try to load start sound
            taskStartSound = loadSoundWithFallback(soundPaths[0]);
            
            // Try to load complete sound
            taskCompleteSound = loadSoundWithFallback(soundPaths[1]);
            
            // Try to load fail sound
            taskFailSound = loadSoundWithFallback(soundPaths[2]);
            
            // Try to load tick sound
            taskTickSound = loadSoundWithFallback(soundPaths[3]);
        } catch (Exception e) {
            log("Could not load sound effects: " + e.getMessage());
        }
    }
    
    /**
     * Load sound with fallback paths
     * 
     * @param paths Array of paths to try
     * @return AudioClip if found, null if not found
     */
    private AudioClip loadSoundWithFallback(String[] paths) {
        for (String path : paths) {
            try {
                java.net.URL resource = getClass().getResource(path);
                if (resource != null) {
                    return new AudioClip(resource.toExternalForm());
                }
            } catch (Exception e) {
                // Ignore and try next path
            }
        }
        
        // If we get here, no path worked
        return null;
    }
    
    /**
     * Safely play an audio clip
     * 
     * @param clip The audio clip to play
     * @param volume Volume (0.0 to 1.0)
     */
    private void safePlaySound(AudioClip clip, double volume) {
        if (clip != null) {
            try {
                clip.play(volume);
            } catch (Exception e) {
                log("Error playing sound: " + e.getMessage());
            }
        }
    }

    /**
     * Set the container to use for displaying this task
     * 
     * @param container The StackPane container from the game UI
     */
    public void setTaskContainer(StackPane container) {
        this.taskContainer = container;
    }

    /**
     * ตรวจสอบว่ามี task กำลังทำงานอยู่หรือไม่
     * 
     * @return true ถ้ามี task กำลังทำงาน
     */
    public static boolean isTaskActive() {
        synchronized(taskLock) {
            return isTaskActive;
        }
    }

    /**
     * ตั้งค่าสถานะการทำงานของ task
     * 
     * @param active true ถ้า task กำลังทำงาน
     */
    protected static void setTaskActive(boolean active) {
        synchronized(taskLock) {
            isTaskActive = active;
        }
    }

    /**
     * Show the task UI to the player
     * 
     * @param onComplete Callback for when the task is completed/failed
     */
    public void showTask(Runnable onComplete) {
        try {
            // ตรวจสอบว่ามี task กำลังแสดงอยู่หรือไม่
            synchronized(taskLock) {
                if (isTaskActive) {
                    log("Cannot show task. Another task is currently active.");
                    return;
                }
                
                // ตรวจสอบ container
                if (taskContainer == null) {
                    log("Task container is null. Cannot show task.");
                    return;
                }
                
                // ตั้งค่าสถานะ task เป็นกำลังทำงาน
                isTaskActive = true;
            }
            
            log("Starting task: " + getTaskName());
            this.onCompleteCallback = onComplete;
            
            // Play task start sound
            safePlaySound(taskStartSound, 0.8);
            
            // Initialize UI on JavaFX thread
            Platform.runLater(() -> {
                try {
                    System.out.println("[GAMETASK] Initializing task UI for: " + getTaskName());
                    
                    // Verify container is accessible
                    if (taskContainer == null) {
                        System.err.println("[GAMETASK] Error: taskContainer became null, aborting");
                        cleanupTask();
                        return;
                    }
                    
                    // Clear the container first
                    taskContainer.getChildren().clear();
                    
                    // Initialize the base UI components
                    initializeUI();
                    
                    // Initialize task-specific UI elements
                    initializeTaskSpecifics();
                    
                    // Verify taskPane was created successfully
                    if (taskPane == null) {
                        System.err.println("[GAMETASK] Error: taskPane is null after initialization");
                        cleanupTask();
                        return;
                    }
                    
                    // Add the task UI to the container with proper centering
                    StackPane.setAlignment(taskPane, Pos.CENTER);
                    
                    // Report task dimensions for debugging
                    System.out.println("[GAMETASK] Task pane size: " + 
                                      taskPane.getPrefWidth() + "x" + taskPane.getPrefHeight());
                    
                    // Add task to container
                    taskContainer.getChildren().add(taskPane);
                    
                    // Make sure container is visible
                    taskContainer.setVisible(true);
                    
                    // Make sure the task is properly sized and positioned
                    taskPane.setVisible(true);
                    
                    // เริ่มจับเวลา
                    startTimer();
                    
                    debugEvent("Task timeout", getTimeLimit());
                    
                    System.out.println("[GAMETASK] Task UI successfully displayed for: " + getTaskName());
                } catch (Exception e) {
                    System.err.println("[GAMETASK] Error initializing task UI: " + e.getMessage());
                    e.printStackTrace();
                    cleanupTask();
                    
                    // Ensure callback is executed to prevent blocking the game flow
                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[GAMETASK] Error showing task: " + e.getMessage());
            e.printStackTrace();
            
            // Reset task active state
            synchronized(taskLock) {
                isTaskActive = false;
            }
            
            // Ensure callback is executed to prevent blocking
            if (onCompleteCallback != null) {
                try {
                    onCompleteCallback.run();
                } catch (Exception ignored) {
                    // Ignore callback errors
                }
            }
        }
    }
    
    /**
     * Initialize the task UI components
     */
    protected void initializeUI() {
        // Create a main container that will hold both the task content and the footer
        StackPane mainContainer = new StackPane();
        mainContainer.setAlignment(Pos.CENTER);
        
        // Set fixed size to ensure consistent layout
        mainContainer.setPrefSize(800, 600);
        mainContainer.setMaxSize(800, 600);
        mainContainer.setMinSize(800, 600);
        
        // Create main content pane with cyberpunk theme (without footer)
        contentPane = new BorderPane();
        contentPane.setPrefSize(800, 580); // Reduced height to accommodate separate footer
        contentPane.setMaxSize(800, 580); // Set max size to prevent overflow
        
        // Apply cyberpunk styling
        CyberpunkEffects.styleTaskPane(contentPane);
        
        // Title and description area
        Text titleText = CyberpunkEffects.createTaskTitle(taskName);
        Text descText = CyberpunkEffects.createTaskDescription(taskDescription);
        
        // Difficulty indicator
        HBox difficultyBox = new HBox(5);
        difficultyBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            Rectangle dot = new Rectangle(10, 10);
            dot.setArcWidth(10);
            dot.setArcHeight(10);
            
            if (i < difficultyLevel) {
                // Color based on difficulty
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
                
                // Add glow effect
                DropShadow glow = new DropShadow();
                glow.setRadius(5);
                glow.setColor(dotColor);
                dot.setEffect(glow);
            } else {
                dot.setFill(Color.GRAY.darker());
            }
            
            difficultyBox.getChildren().add(dot);
        }
        
        // Timer components with enhanced styling
        timerBar = new ProgressBar(1.0);
        timerBar.setPrefWidth(700);
        
        // Create gradient for timer
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#00FFFF")),   // Cyan
            new Stop(0.5, Color.web("#FFFFFF")), // White
            new Stop(1, Color.web("#FF00FF"))    // Pink
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        
        // Apply gradient to timer
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
        
        // Abort button with enhanced styling
        Button closeButton = CyberpunkEffects.createCyberpunkButton("ABORT TASK", false);
        closeButton.setOnAction(e -> {
            // Mark the task as failed
            failed = true;
            
            // Apply failure effect
            CyberpunkEffects.styleFailureEffect(gamePane);
            
            // Apply penalty
            applyPenalty();
            
            // Clean up resources
            cleanupTask();
            
            // Call completion callback to close the task overlay
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }
        });
        
        // Create reward display
        Label rewardLabel = new Label("REWARD: $" + rewardAmount);
        rewardLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        rewardLabel.setTextFill(Color.web("#39FF14")); // Neon green
        
        // Make reward label pulse
        CyberpunkEffects.pulseNode(rewardLabel);
        
        // Header with title, description - more compact to save space
        VBox headerBox = new VBox(5); // Reduced spacing
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10)); // Reduced padding
        headerBox.getChildren().addAll(titleText, descText, difficultyBox);
        headerBox.setMaxHeight(120); // Limit max height
        
        // Game area - will be populated by subclasses - constrained with min/max height
        gamePane = new StackPane();
        gamePane.setStyle("-fx-background-color: rgba(0, 20, 40, 0.8);");
        gamePane.setPadding(new Insets(10)); // Reduced padding
        gamePane.setMinHeight(300); // Set minimum height
        gamePane.setMaxHeight(480); // Set maximum height to prevent overflow
        
        // Add scanning effect to game pane
        CyberpunkEffects.addScanningEffect(gamePane);
        
        // Control area - will be populated by subclasses
        controlPane = new VBox(10); // Reduced spacing
        controlPane.setAlignment(Pos.CENTER);
        controlPane.setPadding(new Insets(10)); // Reduced padding
        
        // Set up the BorderPane with header and game content only
        contentPane.setTop(headerBox);
        contentPane.setCenter(gamePane);
        
        // Create a separate footer StackPane for the timer and buttons
        StackPane footerPane = new StackPane();
        footerPane.setAlignment(Pos.BOTTOM_CENTER);
        footerPane.setPrefHeight(100);
        footerPane.setMaxHeight(100);
        footerPane.setStyle("-fx-background-color: rgba(10, 15, 30, 0.9); -fx-border-color: #00ffff; -fx-border-width: 1px 0 0 0;");
        
        // Create the footer content
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
        
        // Adjust padding for header to keep content within bounds
        headerBox.setPadding(new Insets(15,0,0,0)); // Reduced top padding
        
        // Add scroll constraints to ensure all content is visible
        BorderPane.setAlignment(headerBox, Pos.TOP_CENTER);
        BorderPane.setAlignment(gamePane, Pos.CENTER);
        
        // Position the two main components in the StackPane
        StackPane.setAlignment(contentPane, Pos.TOP_CENTER);
        StackPane.setAlignment(footerPane, Pos.BOTTOM_CENTER);
        
        // Add the BorderPane and footer to the main container
        mainContainer.getChildren().addAll(contentPane, footerPane);
        
        // Center the task in the screen
        StackPane.setAlignment(mainContainer, Pos.CENTER);
        
        // Enable scaling to fit content
        mainContainer.setScaleX(0.99);
        mainContainer.setScaleY(0.99);
        mainContainer.setPadding(new Insets(20));
        
        // Add drop shadow for better visibility
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);
        shadow.setSpread(0.2);
        shadow.setColor(Color.BLACK);
        mainContainer.setEffect(shadow);
        
        // Add CSS styles
        contentPane.getStyleClass().add("cyberpunk-task");
        
        // IMPORTANT: Set the mainContainer as our primary node for the task
        // This changes the taskPane reference to be our mainContainer
        this.taskPane = mainContainer;
    }
    
    // Helper method to convert Color to RGB code string
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    /**
     * Initialize task-specific UI elements and behaviors
     */
    protected abstract void initializeTaskSpecifics();
    
    /**
     * Start the timer for the task
     */
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
                        
                        // Update color based on time
                        if (progress < 0.25) {
                            timerBar.setStyle("-fx-accent: #ff0000; -fx-background-color: #1a1a2a;"); // Red
                            timerLabel.setTextFill(Color.RED);
                            if (!playedWarningSound.get()) {
                                safePlaySound(taskTickSound, 0.5);
                            }
                        } else if (progress < 0.5) {
                            timerBar.setStyle("-fx-accent: #ffff00; -fx-background-color: #1a1a2a;"); // Yellow
                            timerLabel.setTextFill(Color.YELLOW);
                        } else {
                            timerBar.setStyle("-fx-accent: #00ffff; -fx-background-color: #1a1a2a;"); // Cyan
                            timerLabel.setTextFill(Color.CYAN);
                        }
                    }
                });
                
                try {
                    Thread.sleep(1000);
                    
                    // Play tick sound when time is running low
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
    
    /**
     * Apply reward for completing task
     */
    protected void applyReward() {
        GameState gameState = resourceManager.getCurrentState();
        Company company = gameState.getCompany();
        
        // Apply management efficiency to reward (higher efficiency = higher reward)
        long adjustedReward = (long)(rewardAmount * managementEfficiency);
        
        company.setMoney(company.getMoney() + adjustedReward);
        
        // If management level > 1, chance to earn skill point
        if (managementLevel > 1 && random.nextDouble() < 0.25 * (managementLevel - 1)) {
            SkillPointsSystem skillSystem = ResourceManager.getInstance().getSkillPointsSystem();
            if (skillSystem != null) {
                skillSystem.addPoints(1);
            }
        }
        
        // Play success sound
        safePlaySound(taskCompleteSound, 0.8);
        
        Platform.runLater(() -> {
            try {
                resourceManager.getAudioManager().playSoundEffect("reward.mp3");
            } catch (Exception e) {
                log("Error playing reward sound: " + e.getMessage());
            }
            
            String rewardMsg = "Well done cyberpunk! You've completed the task.\nReward: $" + adjustedReward;
            
            // Add management bonus info for levels > 1
            if (managementLevel > 1) {
                int bonusPercent = (int)((managementEfficiency - 1.0) * 100);
                rewardMsg += " (includes " + bonusPercent + "% Management bonus)";
            }
            
            resourceManager.pushCenterNotification(
                "Task Completed: " + taskName,
                rewardMsg,
                "/images/notification/success.png"
            );
        });
    }
    
    /**
     * Apply penalty for failing task
     */
    protected void applyPenalty() {
        GameState gameState = resourceManager.getCurrentState();
        Company company = gameState.getCompany();
        
        // Apply management efficiency to reduce penalty (higher efficiency = lower penalty)
        double penaltyReduction = Math.min(0.5, (managementEfficiency - 1.0) * 0.5);
        double adjustedPenalty = penaltyRating * (1.0 - penaltyReduction);
        
        // Set minimum penalty of 1 point
        adjustedPenalty = Math.max(1.0, adjustedPenalty);
        
        double newRating = Math.max(1.0, company.getRating() - adjustedPenalty);
        company.setRating(newRating);
        
        // Play failure sound
        safePlaySound(taskFailSound, 0.8);
        
        Platform.runLater(() -> {
            try {
                resourceManager.getAudioManager().playSoundEffect("failure.mp3");
            } catch (Exception e) {
                log("Error playing failure sound: " + e.getMessage());
            }
            
            String penaltyMsg = "You failed to complete the task in time.\nRating dropped to: " + String.format("%.1f", newRating);
            
            // Add management protection info for levels > 1
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
    
    /**
     * Complete the task successfully
     */
    protected void completeTask() {
        if (!completed && !failed) {
            completed = true;
            log("Task completed successfully");
            
            // Apply visual completion effect
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
    
    /**
     * Set the task as failed
     */
    protected void failTask() {
        if (!completed && !failed) {
            failed = true;
            log("Task failed");
            
            // Apply visual failure effect
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
    
    // Getters
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

    // เปลี่ยนจาก Logger เป็น simple print
    protected void log(String message) {
        System.out.println("[" + getTaskName() + "] " + message);
    }
    
    protected void debugEvent(String eventName, int secondsFromNow) {
        System.out.println("[DEBUG][" + getTaskName() + "] " + eventName + " จะเกิดขึ้นในอีก " + secondsFromNow + " วินาที");
    }

    /**
     * Clean up resources and reset task state
     */
    protected void cleanupTask() {
        // Stop the timer thread if it's running
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        
        // Reset static task active flag
        synchronized(taskLock) {
            isTaskActive = false;
        }
        
        // Clear references to UI elements
        timerBar = null;
        timerLabel = null;
        gamePane = null;
        controlPane = null;
        contentPane = null;
        taskPane = null;
    }
} 
