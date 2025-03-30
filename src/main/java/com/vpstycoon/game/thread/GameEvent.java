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


public class GameEvent implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(GameEvent.class.getName());
    
    // Timing constants for tasks
//     private static final int INITIAL_TASK_DELAY = 5 * 1000; // 20 seconds initial delay before tasks start
//     private static final int MIN_TASK_INTERVAL = 5 * 1000; // Minimum 30 seconds between tasks
//     private static final int MAX_TASK_INTERVAL = 5 * 1000; // Maximum 90 seconds between tasks
//     private static final int DEBUG_INTERVAL = 1 * 1000;

   private static final int INITIAL_TASK_DELAY = 5 * 60 * 1000; // 30 seconds initial delay before tasks start
   private static final int MIN_TASK_INTERVAL = 2 * 60 * 1000; // Minimum 1 minute between tasks
   private static final int MAX_TASK_INTERVAL = 10 * 60 * 1000; // Maximum 2 minutes between tasks
   private static final int DEBUG_INTERVAL = 5 * 1000; // Debug output every 5 seconds

    private final GameplayContentPane gameplayContentPane;
    private final ResourceManager resourceManager;
    private final GameState gameState;
    private final Random random = new Random();
    

    private volatile boolean isRunning;
    private int completedTaskCount = 0;
    private int failedTaskCount = 0;
    private long nextTaskTime = 0;
    private long lastDebugTime = 0;
    

    private final AtomicBoolean taskActive = new AtomicBoolean(false);
    

    private boolean debugMode = true;
    

    private StackPane taskOverlay;
    

    private StackPane debugOverlay;
    private Label debugLabel;
    

    @SuppressWarnings("unchecked")
    private final Supplier<GameTask>[] taskFactories = new Supplier[] {

        () -> new WireTask(random.nextInt(3) + 3),

        
        () -> new DataDecryptionTask(random.nextInt(3) + 3),


        () -> new FirewallDefenseTask(),
        

        () -> new DataSortingTask(),
        

        () -> new PasswordCrackingTask(),
        

        () -> new NetworkRoutingTask(),
        

        () -> new ServerCoolingTask(),
        

        () -> new ResourceOptimizationTask(),
        

        () -> new CalibrationTask(),
        

        () -> new FileRecoveryTask(),
        

        () -> new HackingTask()
    };


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
            

            this.debugLabel = new Label("Initializing task system...");
            this.debugLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: white; -fx-padding: 5;");
            

            this.debugOverlay = new StackPane(debugLabel);
            this.debugOverlay.setAlignment(Pos.TOP_RIGHT);
            this.debugOverlay.setPadding(new Insets(10));
            this.debugOverlay.setMouseTransparent(true);
            

            initializeTaskOverlay();
            

            this.debugMode = gameplayContentPane.isShowDebug();
            

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
            throw e;
        }
    }
    

    private void initializeTaskOverlay() {
        try {
            System.out.println("[GAMEEVENT] Initializing task overlay");
            
            taskOverlay = new StackPane();
            taskOverlay.setVisible(false);
            taskOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
            taskOverlay.setAlignment(Pos.CENTER);
            taskOverlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            taskOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            taskOverlay.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            

            addGlitchEffect(taskOverlay);
            

            Platform.runLater(() -> {
                try {

                    if (gameplayContentPane == null) {
                        System.err.println("[GAMEEVENT] Error: gameplayContentPane is null");
                        return;
                    }
                    
                    StackPane rootStack = gameplayContentPane.getRootStack();
                    if (rootStack == null) {
                        System.err.println("[GAMEEVENT] Error: rootStack is null");
                        return;
                    }
                    

                    if (rootStack.getChildren().contains(taskOverlay)) {
                        rootStack.getChildren().remove(taskOverlay);
                    }
                    

                    rootStack.getChildren().add(taskOverlay);
                    

                    taskOverlay.prefWidthProperty().bind(rootStack.widthProperty());
                    taskOverlay.prefHeightProperty().bind(rootStack.heightProperty());
                    

                    StackPane.setAlignment(taskOverlay, Pos.CENTER);
                    

                    taskOverlay.setClip(new Rectangle(taskOverlay.getWidth(), taskOverlay.getHeight()));
                    

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
    

    private void addGlitchEffect(Pane pane) {
        Timeline glitchEffect = new Timeline(
            new KeyFrame(Duration.seconds(2), event -> {
                if (random.nextDouble() < 0.15 && taskActive.get()) {

                    double width = random.nextDouble() * pane.getWidth() * 0.3;
                    double height = random.nextDouble() * 15 + 3;
                    double x = random.nextDouble() * (pane.getWidth() - width);
                    double y = random.nextDouble() * (pane.getHeight() - height);
                    

                    Color glitchColor = CyberpunkEffects.getRandomNeonColor();
                    
                    Rectangle glitch = new Rectangle(x, y, width, height);
                    glitch.setFill(glitchColor.deriveColor(1, 1, 1, 0.3));
                    glitch.setMouseTransparent(true);
                    
                    Platform.runLater(() -> {
                        pane.getChildren().add(glitch);
                        

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
    

    private String formatTime(long timeMillis) {
        long seconds = (timeMillis / 1000) % 60;
        long minutes = timeMillis / (60 * 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void run() {
        isRunning = true;
        
        try {

            nextTaskTime = System.currentTimeMillis() + INITIAL_TASK_DELAY;
            lastDebugTime = System.currentTimeMillis();
            

            LOGGER.info("Task system started. First task in " + formatTime(INITIAL_TASK_DELAY));
            System.out.println("[GAMEEVENT] Task system started. First task in " + formatTime(INITIAL_TASK_DELAY));
            

            updateDebugLabel();

            while (isRunning) {
                long currentTime = System.currentTimeMillis();
                

                if (debugMode && (currentTime - lastDebugTime) >= DEBUG_INTERVAL) {
                    lastDebugTime = currentTime;
                    updateDebugLabel();
                }
                

                if (currentTime >= nextTaskTime && !taskActive.get()) {
                    triggerRandomTask();

                    updateDebugLabel();
                }
                

                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            isRunning = false;
            LOGGER.warning("GameEvent thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    

    private void updateDebugLabel() {
        if (!debugMode) return;
        
        long timeUntil = getTimeUntilNextTask();
        String timeFormatted = formatTime(timeUntil);
        String status = taskActive.get() ? "TASK ACTIVE" : "NEXT TASK: " + timeFormatted;
        
        Platform.runLater(() -> {

            debugLabel.setText(String.format("%s | COMPLETED: %d | FAILED: %d", 
                                        status, completedTaskCount, failedTaskCount));
            

            if (!debugOverlay.getChildren().contains(debugLabel)) {
                debugOverlay.getChildren().setAll(debugLabel);
            }
            

            debugOverlay.setVisible(debugMode);
        });
        

        System.out.println("[GAMEEVENT] " + status + " | " + 
                          "Completed: " + completedTaskCount + " | " + 
                          "Failed: " + failedTaskCount);
    }
    

    private void showEnhancedNotification(String title, String message, String imagePath, Runnable clickCallback) {

        Text titleText = new Text(title);
        titleText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 20));
        titleText.setFill(Color.web("#00FFFF"));
        titleText.setEffect(new Glow(0.8));
        
        Text messageText = new Text(message);
        messageText.setFont(Font.font(CyberpunkEffects.CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        messageText.setFill(Color.LIGHTCYAN);
        

        CenterNotificationView notificationView = resourceManager.getCenterNotificationView();
        notificationView.createAndShowTaskNotification(
            title,
            message,
            imagePath,
            clickCallback
        );
    }
    

    private void showTaskNotification(String title, String message, String imagePath, Runnable startCallback, Runnable abortCallback) {

        if (resourceManager == null) {
            LOGGER.warning("Cannot show task notification: ResourceManager is null");
            System.out.println("[GAMEEVENT] Cannot show task notification: ResourceManager is null");
            return;
        }
        

        CenterNotificationView notificationView = resourceManager.getCenterNotificationView();
        

        if (notificationView == null) {
            LOGGER.warning("Cannot show task notification: CenterNotificationView is null");
            System.out.println("[GAMEEVENT] Cannot show task notification: CenterNotificationView is null");
            return;
        }
        

        notificationView.createAndShowTaskNotification(
            title,
            message,
            imagePath,
            startCallback != null ? startCallback : () -> {},
            abortCallback != null ? abortCallback : () -> {}
        );
    }
    

    private void handleTaskAbort() {
        try {
            LOGGER.info("Task aborted from notification.");
            System.out.println("[GAMEEVENT] Task aborted from notification.");
            

            if (resourceManager != null && resourceManager.getAudioManager() != null) {
                resourceManager.getAudioManager().playSoundEffect("task-fail.mp3");
            }
            

            if (resourceManager != null) {
                resourceManager.pushCenterNotificationAutoClose(
                    "Task Aborted",
                    "You decided to skip this task.\nAnother task will be available soon.",
                    "/images/notification/failure.png",
                    3000
                );
            }
            

            scheduleNextTask();
            

            updateDebugLabel();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling task abort", e);
            System.out.println("[GAMEEVENT] Error handling task abort: " + e.getMessage());
            

            if (taskActive != null) {
                taskActive.set(false);
            }
        }
    }
    

    private void triggerRandomTask() {

        if (taskActive.get()) {
            LOGGER.info("Cannot trigger new task. A task is already active.");
            System.out.println("[GAMEEVENT] Cannot trigger new task. A task is already active.");
            return;
        }
        

        if (taskFactories == null || taskFactories.length == 0) {
            LOGGER.warning("Cannot trigger task: No task factories available");
            System.out.println("[GAMEEVENT] Cannot trigger task: No task factories available");
            return;
        }
        

        taskActive.set(true);
        

        int taskIndex = random.nextInt(taskFactories.length);
        GameTask task = taskFactories[taskIndex].get();
        
        if (task == null) {
            LOGGER.warning("Failed to create task: Task factory returned null");
            System.out.println("[GAMEEVENT] Failed to create task: Task factory returned null");
            taskActive.set(false);
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
        

        final GameTask finalTask = task;
        
        Platform.runLater(() -> {
            try {

                if (resourceManager != null && resourceManager.getAudioManager() != null) {

                    resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");
                }
                

                showTaskNotification(
                    "INCOMING TASK: " + finalTask.getTaskName(),
                    "A system task requires your attention!\nCompleting this will earn you rewards.",
                    "/images/notification/task_alert.png",
                    () -> showTask(finalTask),
                    this::handleTaskAbort
                );
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing task notification", e);
                System.out.println("[GAMEEVENT] Error showing task notification: " + e.getMessage());
                taskActive.set(false);
            }
        });
    }
    

    private void showTask(GameTask task) {
        try {

            LOGGER.info("Attempting to show task UI: " + task.getTaskName());
            System.out.println("[GAMEEVENT] Attempting to show task UI: " + task.getTaskName());
            

            Platform.runLater(() -> {
                taskOverlay.getChildren().clear();
                

                task.setTaskContainer(taskOverlay);
                

                taskOverlay.setOpacity(0);
                taskOverlay.setVisible(true);
                

                if (gameplayContentPane.getRootStack().getChildren().contains(taskOverlay)) {
                    gameplayContentPane.getRootStack().getChildren().remove(taskOverlay);
                }
                gameplayContentPane.getRootStack().getChildren().add(taskOverlay);
                

                task.showTask(() -> {

                    FadeTransition fadeOut = new FadeTransition(Duration.millis(250), taskOverlay);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(e -> {

                        taskOverlay.setVisible(false);
                        taskOverlay.getChildren().clear();
                        

                        processTaskCompletion(task);
                        

                        taskActive.set(false);
                    });
                    fadeOut.play();
                });
                

                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), taskOverlay);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
                

                LOGGER.info("Task UI displayed successfully: " + task.getTaskName());
                System.out.println("[GAMEEVENT] Task UI displayed successfully: " + task.getTaskName());
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task UI", e);
            System.out.println("[GAMEEVENT] Error showing task UI: " + e.getMessage());
            e.printStackTrace();
            

            taskActive.set(false);
        }
    }
    

    private void processTaskCompletion(GameTask task) {

        if (task.isCompleted()) {
            LOGGER.info("Task completed: " + task.getTaskName());
            System.out.println("[GAMEEVENT] Task completed: " + task.getTaskName());
            

            completedTaskCount++;
            

            resourceManager.getAudioManager().playSoundEffect("task-complete.mp3");
            

            GameState gameState = resourceManager.getCurrentState();
            Company company = gameState.getCompany();
            company.setMoney(company.getMoney() + task.getRewardAmount());
            

            resourceManager.pushCenterNotificationAutoClose(
                "Task Completed: " + task.getTaskName(),
                "Well done! You've successfully completed the task.\nReward: $" + task.getRewardAmount(),
                "/images/notification/success.png",
                5000
            );
            

            if (completedTaskCount % 5 == 0) {

                long bonus = 10000L * (completedTaskCount / 5);
                company.setMoney(company.getMoney() + bonus);
                
                LOGGER.info("Milestone bonus: $" + bonus + " for " + completedTaskCount + " completed tasks");
                System.out.println("[GAMEEVENT] Milestone bonus: $" + bonus + " for " + completedTaskCount + " completed tasks");
                

                Thread bonusThread = new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        Platform.runLater(() -> {
                            resourceManager.pushCenterNotificationAutoClose(
                                "TASK MILESTONE ACHIEVED",
                                "You've completed " + completedTaskCount + " tasks!\nBonus payment: $" + bonus,
                                "/images/notification/bonus.png",
                                5000
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
            

            failedTaskCount++;
            

            resourceManager.getAudioManager().playSoundEffect("task-fail.mp3");
            

            if (task.getPenaltyRating() > 0) {
                GameState gameState = resourceManager.getCurrentState();
                Company company = gameState.getCompany();
                company.reduceRating(task.getPenaltyRating());
                
                LOGGER.info("Applied penalty: -" + task.getPenaltyRating() + " rating");
                System.out.println("[GAMEEVENT] Applied penalty: -" + task.getPenaltyRating() + " rating");
            }
            

            resourceManager.pushCenterNotificationAutoClose(
                "Task Failed: " + task.getTaskName(),
                "You failed to complete the task in time.\nTry to be quicker next time!",
                "/images/notification/failure.png",
                5000
            );
        }
        

        scheduleNextTask();
        

        updateDebugLabel();
    }
    

    private void scheduleNextTask() {
        try {

            int interval;
            if (MAX_TASK_INTERVAL > MIN_TASK_INTERVAL) {
                interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
            } else {

                interval = MIN_TASK_INTERVAL;
            }
            nextTaskTime = System.currentTimeMillis() + interval;
            
            LOGGER.info("Task completed/failed. Next task in " + formatTime(interval));
            System.out.println("[GAMEEVENT] Task completed/failed. Next task in " + formatTime(interval));
            

            taskActive.set(false);
            

            updateDebugLabel();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error scheduling next task", e);
            System.out.println("[GAMEEVENT] Error scheduling next task: " + e.getMessage());

            taskActive.set(false);
        }
    }
    

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


    public void stopEvent() {
        System.out.println("[GAMEEVENT] Stopping game event system");
        this.isRunning = false;
    }

    public void pauseEvent() {
        System.out.println("[GAMEEVENT] Pausing game event system");
        this.isRunning = false;
    }

    public void resumeEvent() {
        System.out.println("[GAMEEVENT] Resuming game event system");
        if (!this.isRunning) {
            this.isRunning = true;
            new Thread(this).start();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
    

    public boolean isTaskActive() {
        return taskActive.get();
    }
    

    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        LOGGER.setLevel(enabled ? Level.INFO : Level.WARNING);
        LOGGER.info("Debug mode " + (enabled ? "enabled" : "disabled"));
        System.out.println("[GAMEEVENT] Debug mode " + (enabled ? "enabled" : "disabled"));
        

        Platform.runLater(() -> {

            if (debugOverlay != null) {
                debugOverlay.setVisible(enabled);
                

                if (enabled) {

                    if (!debugOverlay.getChildren().contains(debugLabel)) {
                        debugOverlay.getChildren().add(debugLabel);
                    }
                    

                    updateDebugLabel();
                }
                

                if (!gameplayContentPane.getRootStack().getChildren().contains(debugOverlay) && enabled) {
                    gameplayContentPane.getRootStack().getChildren().add(debugOverlay);
                }
            }
        });
    }
    

    public long getTimeUntilNextTask() {
        return Math.max(0, nextTaskTime - System.currentTimeMillis());
    }
    

    public String getFormattedTimeUntilNextTask() {
        return formatTime(getTimeUntilNextTask());
    }
    

    public int getCompletedTaskCount() {
        return completedTaskCount;
    }
    

    public int getFailedTaskCount() {
        return failedTaskCount;
    }
    

    public StackPane getTaskOverlay() {
        return taskOverlay;
    }
    

    public void resetStats() {
        completedTaskCount = 0;
        failedTaskCount = 0;
        LOGGER.info("Task statistics reset");
        System.out.println("[GAMEEVENT] Task statistics reset");
        updateDebugLabel();
    }
}
