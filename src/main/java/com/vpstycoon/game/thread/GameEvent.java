package com.vpstycoon.game.thread;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.task.*;
import com.vpstycoon.ui.game.GameplayContentPane;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vpstycoon.ui.game.notification.center.CenterNotificationView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Manages game tasks (Among Us style minigames) with cyberpunk theme.
 * Features visually enhanced task interfaces with modern cyberpunk styling.
 */
public class GameEvent implements Runnable {
    // Logger for debug information
    private static final Logger LOGGER = Logger.getLogger(GameEvent.class.getName());
    
    // Timing constants for tasks
    private static final int INITIAL_TASK_DELAY = 20 * 1000; // 20 seconds initial delay before tasks start
    private static final int MIN_TASK_INTERVAL = 30 * 1000; // Minimum 30 seconds between tasks
    private static final int MAX_TASK_INTERVAL = 90 * 1000; // Maximum 90 seconds between tasks
    private static final int DEBUG_INTERVAL = 5 * 1000; // Debug output every 5 seconds

    private final GameplayContentPane gameplayContentPane;
    private final ResourceManager resourceManager;
    private final GameState gameState;
    private final Random random = new Random();
    
    // Task tracking
    private volatile boolean isRunning;
    private int completedTaskCount = 0;
    private int failedTaskCount = 0;
    private long nextTaskTime = 0;
    private long lastDebugTime = 0;
    
    // Flag to indicate if a task is currently active
    private final AtomicBoolean taskActive = new AtomicBoolean(false);
    
    // Debug mode flag
    private boolean debugMode = true;
    
    // Task overlay container
    private StackPane taskOverlay;
    
    // Debug overlay
    private StackPane debugOverlay;
    private Label debugLabel;
    
    // Task definitions
    @SuppressWarnings("unchecked")
    private final Supplier<GameTask>[] taskFactories = new Supplier[] {
        // Wire connection task 
        () -> new WireTask(random.nextInt(3) + 3), // 3-5 wires
        
        // Data decryption task
        () -> new DataDecryptionTask(random.nextInt(3) + 3), // 3-5 digits
        
        // Firewall Defense Task
        () -> new FirewallDefenseTask(),
        
        // Data Sorting Task
        () -> new DataSortingTask(),
        
        // Password Cracking Task
        () -> new PasswordCrackingTask(),
        
        // Network Routing Task
        () -> new NetworkRoutingTask(),
        
        // Server Cooling Task
        () -> new ServerCoolingTask(),
        
        // Resource Optimization Task
        () -> new ResourceOptimizationTask(),
        
        // Calibration Task
        () -> new CalibrationTask(),
        
        // File Recovery Task
        () -> new FileRecoveryTask(),
        
        // New Hacking Grid Task
        () -> new HackingTask()
    };

    /**
     * Creates a new GameEvent manager
     * 
     * @param gameplayContentPane The gameplay UI container
     * @param gameState The game state
     */
    public GameEvent(GameplayContentPane gameplayContentPane, GameState gameState) {
        this.gameplayContentPane = gameplayContentPane;
        this.gameState = gameState;
        this.isRunning = false;
        this.resourceManager = ResourceManager.getInstance();
        
        // Initialize task overlay
        initializeTaskOverlay();
        
        // Initialize debug overlay
        initializeDebugOverlay();
        
        // Check if debug mode is enabled
        this.debugMode = gameplayContentPane.isShowDebug();
        
        // Set up debug logging
        if (debugMode) {
            LOGGER.setLevel(Level.INFO);
            LOGGER.info("GameEvent initialized with debug logging enabled");
        } else {
            LOGGER.setLevel(Level.WARNING);
        }
    }
    
    /**
     * Initialize the task overlay that will be added to the game UI
     */
    private void initializeTaskOverlay() {
        taskOverlay = new StackPane();
        taskOverlay.setVisible(false);
        taskOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); // More opaque for cyberpunk look
        taskOverlay.setAlignment(Pos.CENTER);
        taskOverlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); // Cover entire area
        
        // Add cyberpunk-styled glitch effects to overlay
        addGlitchEffect(taskOverlay);
        
        // Add the overlay to the main UI stack
        Platform.runLater(() -> {
            gameplayContentPane.getRootStack().getChildren().add(taskOverlay);
        });
    }
    
    /**
     * Add random glitch effects to the task overlay for cyberpunk aesthetic
     * 
     * @param pane The pane to add effects to
     */
    private void addGlitchEffect(Pane pane) {
        Timeline glitchEffect = new Timeline(
            new KeyFrame(Duration.seconds(2), event -> {
                if (random.nextDouble() < 0.15 && taskActive.get()) { // Only show when task is active
                    // Create a glitch rectangle
                    double width = random.nextDouble() * pane.getWidth() * 0.3;
                    double height = random.nextDouble() * 15 + 3;
                    double x = random.nextDouble() * (pane.getWidth() - width);
                    double y = random.nextDouble() * (pane.getHeight() - height);
                    
                    // Get random neon color
                    Color glitchColor = CyberpunkEffects.getRandomNeonColor();
                    
                    Rectangle glitch = new Rectangle(x, y, width, height);
                    glitch.setFill(glitchColor.deriveColor(1, 1, 1, 0.3));
                    glitch.setMouseTransparent(true); // Don't block mouse events
                    
                    Platform.runLater(() -> {
                        pane.getChildren().add(glitch);
                        
                        // Fade out
                        FadeTransition fade = new FadeTransition(Duration.millis(200), glitch);
                        fade.setFromValue(0.7);
                        fade.setToValue(0);
                        fade.setOnFinished(e -> pane.getChildren().remove(glitch));
                        fade.play();
                    });
                }
            })
        );
        glitchEffect.setCycleCount(Timeline.INDEFINITE);
        glitchEffect.play();
    }
    
    /**
     * Initialize debug overlay for showing task timer
     */
    private void initializeDebugOverlay() {
        debugOverlay = new StackPane();
        debugOverlay.setMouseTransparent(true); // Don't capture mouse events
        debugOverlay.setPickOnBounds(false);
        debugOverlay.setAlignment(Pos.TOP_RIGHT);
        debugOverlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setMargin(debugOverlay, new Insets(40,40,0,0));
        
        // Create debug label with cyberpunk style
        debugLabel = new Label("NEXT TASK: --:--");
        debugLabel.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        debugLabel.setTextFill(Color.CYAN);
        debugLabel.setEffect(new Glow(0.8));
        debugLabel.setStyle("-fx-background-color: rgba(0, 30, 60, 0.7); " +
                           "-fx-padding: 5 10; " +
                           "-fx-background-radius: 5; " +
                           "-fx-border-color: #00ffff; " +
                           "-fx-border-width: 1; " +
                           "-fx-border-radius: 5;");
        
        debugOverlay.getChildren().add(debugLabel);
        debugOverlay.setVisible(debugMode);
        
        // Add to UI
        Platform.runLater(() -> {
            gameplayContentPane.getRootStack().getChildren().add(debugOverlay);
        });
    }

    /**
     * Format time in mm:ss format
     * 
     * @param timeMillis Time in milliseconds
     * @return Formatted time string
     */
    private String formatTime(long timeMillis) {
        long seconds = (timeMillis / 1000) % 60;
        long minutes = timeMillis / (60 * 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void run() {
        isRunning = true;
        
        try {
            // Initialize task timer
            nextTaskTime = System.currentTimeMillis() + INITIAL_TASK_DELAY;
            lastDebugTime = System.currentTimeMillis();
            
            // Log startup information
            LOGGER.info("Task system started. First task in " + formatTime(INITIAL_TASK_DELAY));
            System.out.println("[GAMEEVENT] Task system started. First task in " + formatTime(INITIAL_TASK_DELAY));
            updateDebugLabel();

            while (isRunning) {
                long currentTime = System.currentTimeMillis();
                
                // Output debug information
                if (debugMode && (currentTime - lastDebugTime) >= DEBUG_INTERVAL) {
                    lastDebugTime = currentTime;
                    updateDebugLabel();
                }
                
                // Check if it's time for a task and no task is currently active
                if (currentTime >= nextTaskTime && !taskActive.get()) {
                    triggerRandomTask();
                }
                
                // Sleep briefly to avoid busy-waiting
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            isRunning = false;
            LOGGER.warning("GameEvent thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Update the debug label with time until next task
     */
    private void updateDebugLabel() {
        if (!debugMode) return;
        
        long timeUntil = getTimeUntilNextTask();
        String timeFormatted = formatTime(timeUntil);
        String status = taskActive.get() ? "TASK ACTIVE" : "NEXT TASK: " + timeFormatted;
        
        Platform.runLater(() -> {
            debugLabel.setText(String.format("%s | COMPLETED: %d | FAILED: %d", 
                                            status, completedTaskCount, failedTaskCount));
            debugOverlay.getChildren().clear();
            debugOverlay.getChildren().setAll(debugLabel);
        });
        
        // Also print to console for debug purposes
        System.out.println("[GAMEEVENT] " + status + " | " + 
                          "Completed: " + completedTaskCount + " | " + 
                          "Failed: " + failedTaskCount);
    }
    
    /**
     * Create a visually enhanced notification for tasks
     * 
     * @param title Notification title
     * @param message Notification message
     * @param imagePath Path to notification image
     * @param clickCallback Callback when notification is clicked
     */
    private void showEnhancedNotification(String title, String message, String imagePath, Runnable clickCallback) {
        // Create a cyberpunk-styled notification
        Text titleText = new Text(title);
        titleText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 20));
        titleText.setFill(Color.web("#00FFFF"));
        titleText.setEffect(new Glow(0.8));
        
        Text messageText = new Text(message);
        messageText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        messageText.setFill(Color.LIGHTCYAN);
        
        // Use the built-in notification system
        CenterNotificationView notificationView = resourceManager.getCenterNotificationView();
        notificationView.createAndShowTaskNotification(
            title,
            message,
            imagePath,
            clickCallback
        );
    }
    
    /**
     * Trigger a random task for the player
     */
    private void triggerRandomTask() {
        // Set task active flag to prevent new tasks from being triggered
        taskActive.set(true);
        
        int taskIndex = random.nextInt(taskFactories.length);
        GameTask task = taskFactories[taskIndex].get();
        
        // Log task details
        LOGGER.info("Triggering task: " + task.getTaskName() + 
                   ", difficulty: " + task.getDifficultyLevel() + 
                   ", reward: $" + task.getRewardAmount() + 
                   ", time limit: " + task.getTimeLimit() + "s");
        
        System.out.println("[GAMEEVENT] Triggering task: " + task.getTaskName() + 
                          ", difficulty: " + task.getDifficultyLevel() + 
                          ", reward: $" + task.getRewardAmount() + 
                          ", time limit: " + task.getTimeLimit() + "s");
        
        Platform.runLater(() -> {
            // Play alert sound
            resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");
            
            // Show enhanced cyberpunk notification
            showEnhancedNotification(
                "INCOMING TASK: " + task.getTaskName(),
                "A system task requires your attention!\nCompleting this will earn you rewards.",
                "/images/notification/task_alert.png",
                () -> showTask(task) // callback เมื่อคลิกที่ notification
            );
        });
    }
    
    /**
     * Show a task to the player using the task overlay
     * 
     * @param task The task to show
     */
    private void showTask(GameTask task) {
        // Clear any previous content
        taskOverlay.getChildren().clear();
        
        // Configure the task to use our overlay instead of creating its own window
        task.setTaskContainer(taskOverlay);
        
        // Show the overlay with a fade-in animation
        taskOverlay.setOpacity(0);
        taskOverlay.setVisible(true);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), taskOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Start the task in the overlay with callback ที่จะแสดงผลลัพธ์หลังจบมินิเกม
        task.showTask(() -> {
            // Hide the overlay with a fade-out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), taskOverlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                taskOverlay.setVisible(false);
                processTaskCompletion(task);
            });
            fadeOut.play();
        });
    }
    
    /**
     * Process the completion or failure of a task
     * 
     * @param task The completed/failed task
     */
    private void processTaskCompletion(GameTask task) {
        // Handle task result
        if (task.isCompleted()) {
            completedTaskCount++;
            LOGGER.info("Task completed: " + task.getTaskName() + 
                        ", reward: $" + task.getRewardAmount() + 
                        ", total completed: " + completedTaskCount);
            
            System.out.println("[GAMEEVENT] Task completed: " + task.getTaskName() + 
                                ", reward: $" + task.getRewardAmount() + 
                                ", total completed: " + completedTaskCount);
            
            // Apply reward
            GameState gameState = resourceManager.getCurrentState();
            Company company = gameState.getCompany();
            company.setMoney(company.getMoney() + task.getRewardAmount());
            
            // Notification แสดงผลสำเร็จ แบบหายไปเอง
            resourceManager.pushCenterNotificationAutoClose(
                "Task Completed: " + task.getTaskName(),
                "Well done! You've successfully completed the task.\nReward: $" + task.getRewardAmount(),
                "/images/notification/success.png",
                5000 // หายไปเองหลัง 5 วินาที
            );
            
            // Apply bonus for completing multiple tasks
            if (completedTaskCount % 5 == 0) {
                // Bonus after every 5 completed tasks
                long bonus = 10000L * (completedTaskCount / 5);
                company.setMoney(company.getMoney() + bonus);
                
                LOGGER.info("Milestone bonus: $" + bonus + " for " + completedTaskCount + " completed tasks");
                System.out.println("[GAMEEVENT] Milestone bonus: $" + bonus + " for " + completedTaskCount + " completed tasks");
                
                // แสดง notification bonus หลังจาก notification ผลลัพธ์ แบบหายไปเอง
                Thread bonusThread = new Thread(() -> {
                    try {
                        Thread.sleep(3000); // รอให้ notification แรกแสดงไปก่อน
                        Platform.runLater(() -> {
                            resourceManager.pushCenterNotificationAutoClose(
                                "TASK MILESTONE ACHIEVED",
                                "You've completed " + completedTaskCount + " tasks!\nBonus payment: $" + bonus,
                                "/images/notification/bonus.png",
                                5000 // หายไปเองหลัง 5 วินาที
                            );
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                bonusThread.setDaemon(true);
                bonusThread.start();
            }
        } else {
            failedTaskCount++;
            LOGGER.info("Task failed: " + task.getTaskName() + ", total failed: " + failedTaskCount);
            System.out.println("[GAMEEVENT] Task failed: " + task.getTaskName() + ", total failed: " + failedTaskCount);
            
            // Apply penalty if exists
            if (task.getPenaltyRating() > 0) {
                GameState gameState = resourceManager.getCurrentState();
                Company company = gameState.getCompany();
                company.reduceRating(task.getPenaltyRating());
                
                LOGGER.info("Applied penalty: -" + task.getPenaltyRating() + " rating");
                System.out.println("[GAMEEVENT] Applied penalty: -" + task.getPenaltyRating() + " rating");
            }
            
            // Notification แสดงผลล้มเหลว แบบหายไปเอง
            resourceManager.pushCenterNotificationAutoClose(
                "Task Failed: " + task.getTaskName(),
                "You failed to complete the task in time.\nTry to be quicker next time!",
                "/images/notification/failure.png",
                5000 // หายไปเองหลัง 5 วินาที
            );
        }
        
        // Now that the task is complete, schedule the next task
        scheduleNextTask();
        
        // Update debug overlay
        updateDebugLabel();
    }
    
    /**
     * Schedule the next task after a completed/failed task
     */
    private void scheduleNextTask() {
        // Calculate next task time
        int interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
        nextTaskTime = System.currentTimeMillis() + interval;
        
        LOGGER.info("Task completed/failed. Next task in " + formatTime(interval));
        System.out.println("[GAMEEVENT] Task completed/failed. Next task in " + formatTime(interval));
        
        // Set task active flag to false to allow new tasks
        taskActive.set(false);
        
        // Update debug overlay
        updateDebugLabel();
    }
    
    /**
     * Force a task to appear immediately (for debugging)
     */
    public void debugTriggerTask() {
        if (!taskActive.get()) {
            LOGGER.info("Manually triggering random task (debug)");
            System.out.println("[GAMEEVENT] Manually triggering random task (debug)");
            triggerRandomTask();
        } else {
            LOGGER.info("Cannot trigger new task. A task is already active (debug)");
            System.out.println("[GAMEEVENT] Cannot trigger new task. A task is already active (debug)");
        }
    }
    
    /**
     * Force a specific task to appear (for debugging)
     * 
     * @param taskIndex Index of the task to trigger
     */
    public void debugTriggerSpecificTask(int taskIndex) {
        if (taskActive.get()) {
            LOGGER.info("Cannot trigger new task. A task is already active (debug)");
            System.out.println("[GAMEEVENT] Cannot trigger new task. A task is already active (debug)");
            return;
        }
        
        if (taskIndex >= 0 && taskIndex < taskFactories.length) {
            taskActive.set(true);
            GameTask task = taskFactories[taskIndex].get();
            LOGGER.info("Manually triggering specific task: " + task.getTaskName() + " (debug)");
            System.out.println("[GAMEEVENT] Manually triggering specific task: " + task.getTaskName() + " (debug)");
            Platform.runLater(() -> showTask(task));
        } else {
            LOGGER.warning("Invalid task index: " + taskIndex);
            System.out.println("[GAMEEVENT] Invalid task index: " + taskIndex);
        }
    }

    /**
     * Stop the task system
     */
    public void stopEvent() {
        LOGGER.info("Stopping task system");
        System.out.println("[GAMEEVENT] Stopping task system");
        isRunning = false;
    }

    /**
     * Check if the task system is running
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Check if a task is currently active
     * 
     * @return true if a task is active
     */
    public boolean isTaskActive() {
        return taskActive.get();
    }
    
    /**
     * Set debug mode flag
     * 
     * @param enabled Whether debug mode is enabled
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        LOGGER.setLevel(enabled ? Level.INFO : Level.WARNING);
        LOGGER.info("Debug mode " + (enabled ? "enabled" : "disabled"));
        System.out.println("[GAMEEVENT] Debug mode " + (enabled ? "enabled" : "disabled"));
        
        // Update debug overlay visibility
        Platform.runLater(() -> {
            debugOverlay.setVisible(enabled);
        });
    }
    
    /**
     * Get time until next task in milliseconds
     * 
     * @return Time until next task in milliseconds
     */
    public long getTimeUntilNextTask() {
        return Math.max(0, nextTaskTime - System.currentTimeMillis());
    }
    
    /**
     * Get formatted time until next task
     * 
     * @return Formatted time string
     */
    public String getFormattedTimeUntilNextTask() {
        return formatTime(getTimeUntilNextTask());
    }
    
    /**
     * Get the number of completed tasks
     * 
     * @return Number of completed tasks
     */
    public int getCompletedTaskCount() {
        return completedTaskCount;
    }
    
    /**
     * Get the number of failed tasks
     * 
     * @return Number of failed tasks
     */
    public int getFailedTaskCount() {
        return failedTaskCount;
    }
    
    /**
     * Get the task overlay
     * 
     * @return The StackPane used for displaying tasks
     */
    public StackPane getTaskOverlay() {
        return taskOverlay;
    }
    
    /**
     * Reset all task statistics
     */
    public void resetStats() {
        completedTaskCount = 0;
        failedTaskCount = 0;
        LOGGER.info("Task statistics reset");
        System.out.println("[GAMEEVENT] Task statistics reset");
        updateDebugLabel();
    }
}