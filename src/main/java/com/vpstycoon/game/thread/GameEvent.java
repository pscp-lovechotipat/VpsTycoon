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
import javafx.scene.layout.Region;
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
    private static final int INITIAL_TASK_DELAY = 5 * 1000; // 20 seconds initial delay before tasks start
    private static final int MIN_TASK_INTERVAL = 5 * 1000; // Minimum 30 seconds between tasks
    private static final int MAX_TASK_INTERVAL = 5 * 1000; // Maximum 90 seconds between tasks
    private static final int DEBUG_INTERVAL = 1 * 1000;

//    private static final int INITIAL_TASK_DELAY = 5 * 60 * 1000; // 20 seconds initial delay before tasks start
//    private static final int MIN_TASK_INTERVAL = 3 * 60 * 1000; // Minimum 30 seconds between tasks
//    private static final int MAX_TASK_INTERVAL = 7 * 60 * 1000; // Maximum 90 seconds between tasks
//    private static final int DEBUG_INTERVAL = 5 * 1000; // Debug output every 5 seconds

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
        // () -> new WireTask(random.nextInt(3) + 3), // 3-5 wires
        
        // Data decryption task
    // () -> new DataDecryptionTask(random.nextInt(3) + 3), // 3-5 digits
//
//        // Firewall Defense Task
     () -> new FirewallDefenseTask(),
//
//        // Data Sorting Task
//        () -> new DataSortingTask(),
//
//        // Password Cracking Task
//        () -> new PasswordCrackingTask(),
//
//        // Network Routing Task
//        () -> new NetworkRoutingTask(),
//
//        // Server Cooling Task
//        () -> new ServerCoolingTask(),
//
//        // Resource Optimization Task
//        () -> new ResourceOptimizationTask(),
//
//        // Calibration Task
//        () -> new CalibrationTask(),
//
//        // File Recovery Task
//        () -> new FileRecoveryTask(),
//
//        // New Hacking Grid Task
//        () -> new HackingTask()
    };

    /**
     * Creates a new GameEvent manager
     * 
     * @param gameplayContentPane The gameplay UI container
     * @param gameState The game state
     */
    public GameEvent(GameplayContentPane gameplayContentPane, GameState gameState) {
        try {
            System.out.println("[GAMEEVENT] Initializing GameEvent system");
            
            if (gameplayContentPane == null) {
                throw new IllegalArgumentException("gameplayContentPane cannot be null");
            }
            
            if (gameState == null) {
                throw new IllegalArgumentException("gameState cannot be null");
            }
            
            this.gameplayContentPane = gameplayContentPane;
            this.gameState = gameState;
            this.isRunning = false;
            this.resourceManager = ResourceManager.getInstance();
            
            if (this.resourceManager == null) {
                System.err.println("[GAMEEVENT] Warning: ResourceManager is null");
            }
            
            // Initialize debug label
            this.debugLabel = new Label("Initializing task system...");
            this.debugLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: white; -fx-padding: 5;");
            
            // Initialize debug overlay
            this.debugOverlay = new StackPane(debugLabel);
            this.debugOverlay.setAlignment(Pos.TOP_RIGHT);
            this.debugOverlay.setPadding(new Insets(10));
            this.debugOverlay.setMouseTransparent(true);
            
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
            
            System.out.println("[GAMEEVENT] GameEvent system successfully initialized");
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error initializing GameEvent: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to notify calling code
        }
    }
    
    /**
     * Initialize the task overlay that will be added to the game UI
     */
    private void initializeTaskOverlay() {
        try {
            System.out.println("[GAMEEVENT] Initializing task overlay");
            
            taskOverlay = new StackPane();
            taskOverlay.setVisible(false);
            taskOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); // More opaque for cyberpunk look
            taskOverlay.setAlignment(Pos.CENTER); // Ensure everything is centered
            taskOverlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE); // Cover entire area
            taskOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            taskOverlay.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            
            // Add cyberpunk-styled glitch effects to overlay
            addGlitchEffect(taskOverlay);
            
            // Add the overlay to the main UI stack
            Platform.runLater(() -> {
                try {
                    // First check if the rootStack exists
                    if (gameplayContentPane == null) {
                        System.err.println("[GAMEEVENT] Error: gameplayContentPane is null");
                        return;
                    }
                    
                    StackPane rootStack = gameplayContentPane.getRootStack();
                    if (rootStack == null) {
                        System.err.println("[GAMEEVENT] Error: rootStack is null");
                        return;
                    }
                    
                    // Remove the overlay if it already exists
                    if (rootStack.getChildren().contains(taskOverlay)) {
                        rootStack.getChildren().remove(taskOverlay);
                    }
                    
                    // Add to the top layer to ensure it's visible above other UI elements
                    rootStack.getChildren().add(taskOverlay);
                    
                    // Ensure the overlay is resized to match the parent container
                    taskOverlay.prefWidthProperty().bind(rootStack.widthProperty());
                    taskOverlay.prefHeightProperty().bind(rootStack.heightProperty());
                    
                    // Set layer priority to ensure it appears on top
                    StackPane.setAlignment(taskOverlay, Pos.CENTER);
                    
                    // Add viewport constraints to ensure content stays within the screen
                    taskOverlay.setClip(new Rectangle(taskOverlay.getWidth(), taskOverlay.getHeight()));
                    
                    // Ensure the overlay adapts to window size
                    taskOverlay.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                        Rectangle clip = new Rectangle(
                            newBounds.getWidth(), 
                            newBounds.getHeight()
                        );
                        taskOverlay.setClip(clip);
                    });
                    
                    System.out.println("[GAMEEVENT] Task overlay successfully initialized and added to UI");
                } catch (Exception e) {
                    System.err.println("[GAMEEVENT] Error initializing task overlay: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error creating task overlay: " + e.getMessage());
            e.printStackTrace();
        }
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
            
            // เรียก updateDebugLabel เพื่อแสดงข้อมูลเริ่มต้น
            updateDebugLabel();

            while (isRunning) {
                long currentTime = System.currentTimeMillis();
                
                // Output debug information with increased frequency
                if (debugMode && (currentTime - lastDebugTime) >= DEBUG_INTERVAL) {
                    lastDebugTime = currentTime;
                    updateDebugLabel();
                }
                
                // Check if it's time for a task and no task is currently active
                if (currentTime >= nextTaskTime && !taskActive.get()) {
                    triggerRandomTask();
                    // อัพเดตทันทีหลังจากเริ่ม task
                    updateDebugLabel();
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
            // อัพเดตข้อความโดยไม่ต้อง clear และ add ใหม่ทุกครั้ง
            debugLabel.setText(String.format("%s | COMPLETED: %d | FAILED: %d", 
                                        status, completedTaskCount, failedTaskCount));
            
            // ตรวจสอบว่า debugLabel อยู่ใน children ของ debugOverlay หรือไม่
            if (!debugOverlay.getChildren().contains(debugLabel)) {
                debugOverlay.getChildren().setAll(debugLabel);
            }
            
            // ตรวจสอบว่า debugOverlay มีการแสดงผลตาม debugMode
            debugOverlay.setVisible(debugMode);
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
     * Show enhanced notification with separate callbacks for starting and aborting tasks
     */
    private void showTaskNotification(String title, String message, String imagePath, Runnable startCallback, Runnable abortCallback) {
        // Check if resource manager is available
        if (resourceManager == null) {
            LOGGER.warning("Cannot show task notification: ResourceManager is null");
            System.out.println("[GAMEEVENT] Cannot show task notification: ResourceManager is null");
            return;
        }
        
        // Use the built-in notification system
        CenterNotificationView notificationView = resourceManager.getCenterNotificationView();
        
        // Check if notification view is available
        if (notificationView == null) {
            LOGGER.warning("Cannot show task notification: CenterNotificationView is null");
            System.out.println("[GAMEEVENT] Cannot show task notification: CenterNotificationView is null");
            return;
        }
        
        // Show notification with proper null checks for callbacks
        notificationView.createAndShowTaskNotification(
            title,
            message,
            imagePath,
            startCallback != null ? startCallback : () -> {},
            abortCallback != null ? abortCallback : () -> {}
        );
    }
    
    /**
     * Handle task abortion - called when the user aborts a task from the notification
     */
    private void handleTaskAbort() {
        try {
            LOGGER.info("Task aborted from notification.");
            System.out.println("[GAMEEVENT] Task aborted from notification.");
            
            // Play failure sound if available
            if (resourceManager != null && resourceManager.getAudioManager() != null) {
                resourceManager.getAudioManager().playSoundEffect("task-fail.mp3");
            }
            
            // Show notification if resource manager is available
            if (resourceManager != null) {
                resourceManager.pushCenterNotificationAutoClose(
                    "Task Aborted",
                    "You decided to skip this task.\nAnother task will be available soon.",
                    "/images/notification/failure.png",
                    3000 // หายไปเองหลัง 3 วินาที
                );
            }
            
            // Schedule the next task
            scheduleNextTask();
            
            // Update debug overlay
            updateDebugLabel();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling task abort", e);
            System.out.println("[GAMEEVENT] Error handling task abort: " + e.getMessage());
            
            // Make sure to reset task active state in case of error
            if (taskActive != null) {
                taskActive.set(false);
            }
        }
    }
    
    /**
     * Trigger a random task for the player
     */
    private void triggerRandomTask() {
        // Don't trigger a new task if one is already active
        if (taskActive.get()) {
            LOGGER.info("Cannot trigger new task. A task is already active.");
            System.out.println("[GAMEEVENT] Cannot trigger new task. A task is already active.");
            return;
        }
        
        // Check if taskFactories array is properly initialized
        if (taskFactories == null || taskFactories.length == 0) {
            LOGGER.warning("Cannot trigger task: No task factories available");
            System.out.println("[GAMEEVENT] Cannot trigger task: No task factories available");
            return;
        }
        
        // Set task active flag to prevent additional tasks
        taskActive.set(true);
        
        // Select a random task
        int taskIndex = random.nextInt(taskFactories.length);
        GameTask task = taskFactories[taskIndex].get();
        
        if (task == null) {
            LOGGER.warning("Failed to create task: Task factory returned null");
            System.out.println("[GAMEEVENT] Failed to create task: Task factory returned null");
            taskActive.set(false); // Reset the flag since we're not proceeding
            return;
        }
        
        LOGGER.info("Triggering task: " + task.getTaskName() + 
                  ", difficulty: " + task.getDifficultyLevel() + 
                  ", reward: $" + task.getRewardAmount() + 
                  ", time limit: " + task.getTimeLimit() + "s");
        
        System.out.println("[GAMEEVENT] Triggering task: " + task.getTaskName() + 
                          ", difficulty: " + task.getDifficultyLevel() + 
                          ", reward: $" + task.getRewardAmount() + 
                          ", time limit: " + task.getTimeLimit() + "s");
        
        // Store the task for access in closure
        final GameTask finalTask = task;
        
        Platform.runLater(() -> {
            try {
                // Check if resource manager is available
                if (resourceManager != null && resourceManager.getAudioManager() != null) {
                    // Play alert sound
                    resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");
                }
                
                // Show task notification with separate callbacks for START and ABORT
                showTaskNotification(
                    "INCOMING TASK: " + finalTask.getTaskName(),
                    "A system task requires your attention!\nCompleting this will earn you rewards.",
                    "/images/notification/task_alert.png",
                    () -> showTask(finalTask),  // START TASK callback
                    this::handleTaskAbort      // ABORT TASK callback
                );
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing task notification", e);
                System.out.println("[GAMEEVENT] Error showing task notification: " + e.getMessage());
                taskActive.set(false); // Reset flag on error
            }
        });
    }
    
    /**
     * Show a task to the player using the task overlay
     * 
     * @param task The task to show
     */
    private void showTask(GameTask task) {
        try {
            // Log attempt to show task
            LOGGER.info("Attempting to show task UI: " + task.getTaskName());
            System.out.println("[GAMEEVENT] Attempting to show task UI: " + task.getTaskName());
            
            // Clear any previous content
            Platform.runLater(() -> {
                taskOverlay.getChildren().clear();
                
                // Configure the task to use our overlay instead of creating its own window
                task.setTaskContainer(taskOverlay);
                
                // Show the overlay with a fade-in animation
                taskOverlay.setOpacity(0);
                taskOverlay.setVisible(true);
                
                // Ensure taskOverlay is at the front (top of stack)
                if (gameplayContentPane.getRootStack().getChildren().contains(taskOverlay)) {
                    gameplayContentPane.getRootStack().getChildren().remove(taskOverlay);
                }
                gameplayContentPane.getRootStack().getChildren().add(taskOverlay);
                
                // Start the task in the overlay with callback that will run when the task is completed/failed
                task.showTask(() -> {
                    // Hide the overlay with a fade-out animation
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(250), taskOverlay);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(e -> {
                        // Completely clear and hide the overlay
                        taskOverlay.setVisible(false);
                        taskOverlay.getChildren().clear(); // Remove task UI
                        
                        // Process the task completion
                        processTaskCompletion(task);
                        
                        // Ensure the taskActive flag is set to false
                        taskActive.set(false);
                    });
                    fadeOut.play();
                });
                
                // Apply fade-in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), taskOverlay);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                
                // Log successful display
                LOGGER.info("Task UI displayed successfully: " + task.getTaskName());
                System.out.println("[GAMEEVENT] Task UI displayed successfully: " + task.getTaskName());
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task UI", e);
            System.out.println("[GAMEEVENT] Error showing task UI: " + e.getMessage());
            e.printStackTrace();
            
            // Reset task active state to prevent blocking future tasks
            taskActive.set(false);
        }
    }
    
    /**
     * Process task completion
     * 
     * @param task The completed task
     */
    private void processTaskCompletion(GameTask task) {
        // Check if the task was completed or failed
        if (task.isCompleted()) {
            LOGGER.info("Task completed: " + task.getTaskName());
            System.out.println("[GAMEEVENT] Task completed: " + task.getTaskName());
            
            // Increment completed task count
            completedTaskCount++;
            
            // Play completion sound
            resourceManager.getAudioManager().playSoundEffect("task-complete.mp3");
            
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
        } else if (task.isFailed()) {
            LOGGER.info("Task failed: " + task.getTaskName());
            System.out.println("[GAMEEVENT] Task failed: " + task.getTaskName());
            
            // Increment failed task count
            failedTaskCount++;
            
            // Play failure sound
            resourceManager.getAudioManager().playSoundEffect("task-fail.mp3");
            
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
        
        // Schedule the next task
        scheduleNextTask();
        
        // Update debug overlay
        updateDebugLabel();
    }
    
    /**
     * Schedule the next task after a completed/failed task
     */
    private void scheduleNextTask() {
        try {
            // Calculate next task time with safety check
            int interval;
            if (MAX_TASK_INTERVAL > MIN_TASK_INTERVAL) {
                interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
            } else {
                // Fallback if MAX <= MIN
                interval = MIN_TASK_INTERVAL;
            }
            nextTaskTime = System.currentTimeMillis() + interval;
            
            LOGGER.info("Task completed/failed. Next task in " + formatTime(interval));
            System.out.println("[GAMEEVENT] Task completed/failed. Next task in " + formatTime(interval));
            
            // Set task active flag to false to allow new tasks
            taskActive.set(false);
            
            // อัพเดตทันทีหลังจากกำหนดเวลา task ถัดไป 
            updateDebugLabel();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error scheduling next task", e);
            System.out.println("[GAMEEVENT] Error scheduling next task: " + e.getMessage());
            // Ensure task active is reset even in case of error
            taskActive.set(false);
        }
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
        
        // Update debug overlay visibility and refresh data
        Platform.runLater(() -> {
            // ตรวจสอบว่า overlay ถูกสร้างแล้ว
            if (debugOverlay != null) {
                debugOverlay.setVisible(enabled);
                
                // ถ้าเปิดใช้งาน debug ให้อัพเดตข้อมูลทันที
                if (enabled) {
                    // ตรวจสอบว่า label ถูกเพิ่มไปแล้วหรือไม่
                    if (!debugOverlay.getChildren().contains(debugLabel)) {
                        debugOverlay.getChildren().add(debugLabel);
                    }
                    
                    // อัพเดตข้อมูล
                    updateDebugLabel();
                }
                
                // ตรวจสอบว่า overlay อยู่ใน scene หรือไม่
                if (!gameplayContentPane.getRootStack().getChildren().contains(debugOverlay) && enabled) {
                    gameplayContentPane.getRootStack().getChildren().add(debugOverlay);
                }
            }
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