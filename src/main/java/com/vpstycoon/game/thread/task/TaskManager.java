package com.vpstycoon.game.thread.task;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Manages the creation and tracking of game tasks
 */
public class TaskManager {
    private static final int INITIAL_TASK_DELAY = 10 * 60 * 1000; // 10 minutes initial delay
    private static final int MIN_TASK_INTERVAL = 5 * 60 * 1000;  // 5 minutes minimum interval
    private static final int MAX_TASK_INTERVAL = 15 * 60 * 1000; // 15 minutes maximum interval
    
    private final Random random = new Random();
    private final ResourceManager resourceManager;
    private final StackPane gameUIContainer;
    
    private volatile boolean isRunning;
    private int completedTaskCount = 0;
    private int failedTaskCount = 0;
    private long nextTaskTime = 0;
    
    @SuppressWarnings("unchecked")
    private final Supplier<GameTask>[] taskFactories = new Supplier[] {
        // Wire connection task
        () -> new WireTask(random.nextInt(3) + 3), // 3-5 wires
        
        // Data decryption task
        () -> new DataDecryptionTask(random.nextInt(3) + 3), // 3-5 digits
        
        // Firewall Defense Task (placeholder)
        () -> new FirewallDefenseTask(),
        
        // Data Sorting Task (placeholder)
        () -> new DataSortingTask(),
        
        // Password Cracking Task (placeholder)
        () -> new PasswordCrackingTask(),
        
        // Network Routing Task (placeholder)
        () -> new NetworkRoutingTask(),
        
        // Server Cooling Task (placeholder)
        () -> new ServerCoolingTask(),
        
        // Resource Optimization Task (placeholder)
        () -> new ResourceOptimizationTask(),
        
        // Calibration Task (placeholder)
        () -> new CalibrationTask(),
        
        // File Recovery Task (placeholder)
        () -> new FileRecoveryTask()
    };
    
    /**
     * Create a new TaskManager
     * 
     * @param gameUIContainer The StackPane to add task UIs to
     */
    public TaskManager(StackPane gameUIContainer) {
        this.gameUIContainer = gameUIContainer;
        this.resourceManager = ResourceManager.getInstance();
        this.isRunning = false;
    }
    
    /**
     * Start the task manager
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        nextTaskTime = System.currentTimeMillis() + INITIAL_TASK_DELAY;
        
        Thread taskThread = new Thread(this::taskLoop);
        taskThread.setDaemon(true);
        taskThread.start();
        
        System.out.println("Task system started. First task in " + (INITIAL_TASK_DELAY / 1000 / 60) + " minutes");
    }
    
    /**
     * Stop the task manager
     */
    public void stop() {
        isRunning = false;
    }
    
    /**
     * Main task loop - periodically triggers random tasks
     */
    private void taskLoop() {
        while (isRunning) {
            try {
                long currentTime = System.currentTimeMillis();
                long timeUntilNextTask = nextTaskTime - currentTime;
                
                if (timeUntilNextTask <= 0) {
                    triggerRandomTask();
                    
                    // Calculate next task time
                    int interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
                    nextTaskTime = System.currentTimeMillis() + interval;
                    
                    // Print debug info
                    int minutes = interval / 1000 / 60;
                    int seconds = (interval / 1000) % 60;
                    System.out.println("Next task in " + minutes + "m " + seconds + "s");
                }
                
                // Sleep for a bit to avoid busy-waiting
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Trigger a random task for the player
     */
    private void triggerRandomTask() {
        int taskIndex = random.nextInt(taskFactories.length);
        GameTask task = taskFactories[taskIndex].get();
        
        Platform.runLater(() -> {
            // Preview notification
            resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");
            resourceManager.pushCenterNotification(
                "INCOMING TASK: " + task.getTaskName(),
                "A system task requires your attention!\nCompleting this will earn you rewards.",
                "/images/notification/task_alert.png"
            );
            
            // Show task after a short delay
            Thread showTaskThread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> showTask(task));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            showTaskThread.setDaemon(true);
            showTaskThread.start();
        });
    }
    
    /**
     * Show a task to the player
     * 
     * @param task The task to show
     */
    private void showTask(GameTask task) {
        task.showTask(() -> {
            if (task.isCompleted()) {
                completedTaskCount++;
                
                // Apply bonus for completing multiple tasks
                if (completedTaskCount % 5 == 0) {
                    // Bonus after every 5 completed tasks
                    GameState gameState = resourceManager.getCurrentState();
                    Company company = gameState.getCompany();
                    long bonus = 10000L * (completedTaskCount / 5);
                    company.setMoney(company.getMoney() + bonus);
                    
                    Platform.runLater(() -> {
                        resourceManager.pushCenterNotification(
                            "TASK MILESTONE ACHIEVED",
                            "You've completed " + completedTaskCount + " tasks!\nBonus payment: $" + bonus,
                            "/images/notification/bonus.png"
                        );
                    });
                }
            } else if (task.isFailed()) {
                failedTaskCount++;
            }
        });
    }
    
    /**
     * Force a task to appear immediately (for debugging)
     */
    public void debugTriggerTask() {
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
            Platform.runLater(() -> showTask(task));
        }
    }
    
    /**
     * Get the time until next task in milliseconds
     * 
     * @return Time until next task in milliseconds
     */
    public long getTimeUntilNextTask() {
        return Math.max(0, nextTaskTime - System.currentTimeMillis());
    }
    
    /**
     * Get the time until next task formatted as a string
     * 
     * @return Formatted time string (mm:ss)
     */
    public String getFormattedTimeUntilNextTask() {
        long timeMillis = getTimeUntilNextTask();
        long seconds = (timeMillis / 1000) % 60;
        long minutes = timeMillis / (60 * 1000);
        return String.format("%02d:%02d", minutes, seconds);
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
     * Check if the task manager is running
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }
}