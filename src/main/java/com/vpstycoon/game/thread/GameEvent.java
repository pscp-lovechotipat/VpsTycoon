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

import com.vpstycoon.ui.game.notification.center.CenterNotificationView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

/**
 * Manages game tasks (Among Us style minigames).
 */
public class GameEvent implements Runnable {
    // Logger for debug information
    private static final Logger LOGGER = Logger.getLogger(GameEvent.class.getName());
    
    // Timing constants for tasks
    private static final int INITIAL_TASK_DELAY = 20 * 1000; // 20 seconds initial delay before tasks start
    private static final int MIN_TASK_INTERVAL = 1 * 1000; // Minimum 1 second between tasks
    private static final int MAX_TASK_INTERVAL = 60 * 1000; // Maximum 10 seconds between tasks

    private final GameplayContentPane gameplayContentPane;
    private final ResourceManager resourceManager;
    private final GameState gameState;
    private final Random random = new Random();
    
    // Task tracking
    private volatile boolean isRunning;
    private int completedTaskCount = 0;
    private int failedTaskCount = 0;
    private long nextTaskTime = 0;
    
    // Debug mode flag
    private boolean debugMode = true;
    
    // Task overlay container
    private StackPane taskOverlay;
    
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
        () -> new FileRecoveryTask()
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
        taskOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Semi-transparent background
        taskOverlay.setAlignment(Pos.CENTER);
        
        // Add the overlay to the main UI stack
        Platform.runLater(() -> {
            gameplayContentPane.getRootStack().getChildren().add(taskOverlay);
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
            
            // Log startup information
            LOGGER.info("Task system started. First task in " + formatTime(INITIAL_TASK_DELAY));

            while (isRunning) {
                long currentTime = System.currentTimeMillis();
                
                // Debug logging
                if (debugMode && currentTime % (30 * 1000) == 0) { // Log every 30 seconds
                    long taskTimeLeft = nextTaskTime - currentTime;
                    LOGGER.info("Next task in: " + formatTime(taskTimeLeft));
                }
                
                // Check if it's time for a task
                if (currentTime >= nextTaskTime) {
                    triggerRandomTask();
                    // Calculate next task time
                    int interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
                    nextTaskTime = System.currentTimeMillis() + interval;
                    LOGGER.info("Task triggered. Next task in " + formatTime(interval));
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
     * Trigger a random task for the player
     */
    private void triggerRandomTask() {
        int taskIndex = random.nextInt(taskFactories.length);
        GameTask task = taskFactories[taskIndex].get();
        
        // Log task details
        LOGGER.info("Triggering task: " + task.getTaskName() + 
                   ", difficulty: " + task.getDifficultyLevel() + 
                   ", reward: $" + task.getRewardAmount() + 
                   ", time limit: " + task.getTimeLimit() + "s");
        
        Platform.runLater(() -> {
            // 1. Preview notification ที่ต้องคลิกเพื่อเริ่มมินิเกม
            resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");
            
            // สร้าง notification แบบ center ที่ต้องคลิกเพื่อเริ่มเกม
            CenterNotificationView notificationView = resourceManager.getCenterNotificationView();
            notificationView.createAndShowTaskNotification(
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
        
        // Show the overlay
        taskOverlay.setVisible(true);
        
        // Start the task in the overlay with callback ที่จะแสดงผลลัพธ์หลังจบมินิเกม
        task.showTask(() -> {
            // Hide the overlay when task is complete
            Platform.runLater(() -> {
                taskOverlay.setVisible(false);
            
                // 4. แสดง notification ผลลัพธ์หลังจากเล่นเสร็จหรือหมดเวลา
                if (task.isCompleted()) {
                    completedTaskCount++;
                    LOGGER.info("Task completed: " + task.getTaskName() + 
                               ", reward: $" + task.getRewardAmount() + 
                               ", total completed: " + completedTaskCount);
                    
                    // notification แสดงผลสำเร็จ แบบหายไปเอง
                    resourceManager.pushCenterNotificationAutoClose(
                        "Task Completed: " + task.getTaskName(),
                        "Well done! You've successfully completed the task.\nReward: $" + task.getRewardAmount(),
                        "/images/notification/success.png",
                        5000 // หายไปเองหลัง 5 วินาที
                    );
                    
                    // Apply bonus for completing multiple tasks
                    if (completedTaskCount % 5 == 0) {
                        // Bonus after every 5 completed tasks
                        GameState gameState = resourceManager.getCurrentState();
                        Company company = gameState.getCompany();
                        long bonus = 10000L * (completedTaskCount / 5);
                        company.setMoney(company.getMoney() + bonus);
                        
                        LOGGER.info("Milestone bonus: $" + bonus + " for " + completedTaskCount + " completed tasks");
                        
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
                    
                    // notification แสดงผลล้มเหลว แบบหายไปเอง
                    resourceManager.pushCenterNotificationAutoClose(
                        "Task Failed: " + task.getTaskName(),
                        "You failed to complete the task in time.\nTry to be quicker next time!",
                        "/images/notification/failure.png",
                        5000 // หายไปเองหลัง 5 วินาที
                    );
                }
            });
        });
    }
    
    /**
     * Force a task to appear immediately (for debugging)
     */
    public void debugTriggerTask() {
        LOGGER.info("Manually triggering random task (debug)");
        triggerRandomTask();
    }
    
    /**
     * Force a specific task to appear (for debugging)
     * 
     * @param taskIndex Index of the task to trigger
     */
    public void debugTriggerSpecificTask(int taskIndex) {
        if (taskIndex >= 0 && taskIndex < taskFactories.length) {
            GameTask task = taskFactories[taskIndex].get();
            LOGGER.info("Manually triggering specific task: " + task.getTaskName() + " (debug)");
            Platform.runLater(() -> showTask(task));
        } else {
            LOGGER.warning("Invalid task index: " + taskIndex);
        }
    }

    /**
     * Stop the task system
     */
    public void stopEvent() {
        LOGGER.info("Stopping task system");
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
     * Set debug mode flag
     * 
     * @param enabled Whether debug mode is enabled
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        LOGGER.setLevel(enabled ? Level.INFO : Level.WARNING);
        LOGGER.info("Debug mode " + (enabled ? "enabled" : "disabled"));
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
}