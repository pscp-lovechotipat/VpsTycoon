package com.vpstycoon.game.thread.task;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

import java.util.Random;
import java.util.function.Supplier;


public class TaskManager {
    private static final int INITIAL_TASK_DELAY = 10 * 60 * 1000; 
    private static final int MIN_TASK_INTERVAL = 5 * 60 * 1000;  
    private static final int MAX_TASK_INTERVAL = 15 * 60 * 1000; 
    
    private final Random random = new Random();
    private final ResourceManager resourceManager;
    private final StackPane gameUIContainer;
    
    private volatile boolean isRunning;
    private int completedTaskCount = 0;
    private int failedTaskCount = 0;
    private long nextTaskTime = 0;
    
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
        
        
        () -> new FileRecoveryTask()
    };
    
    
    public TaskManager(StackPane gameUIContainer) {
        this.gameUIContainer = gameUIContainer;
        this.resourceManager = ResourceManager.getInstance();
        this.isRunning = false;
    }
    
    
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        nextTaskTime = System.currentTimeMillis() + INITIAL_TASK_DELAY;
        
        Thread taskThread = new Thread(this::taskLoop);
        taskThread.setDaemon(true);
        taskThread.start();
        
        System.out.println("Task system started. First task in " + (INITIAL_TASK_DELAY / 1000 / 60) + " minutes");
    }
    
    
    public void stop() {
        isRunning = false;
    }
    
    
    private void taskLoop() {
        while (isRunning) {
            try {
                long currentTime = System.currentTimeMillis();
                long timeUntilNextTask = nextTaskTime - currentTime;
                
                if (timeUntilNextTask <= 0) {
                    triggerRandomTask();
                    
                    
                    int interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
                    nextTaskTime = System.currentTimeMillis() + interval;
                    
                    
                    int minutes = interval / 1000 / 60;
                    int seconds = (interval / 1000) % 60;
                    System.out.println("Next task in " + minutes + "m " + seconds + "s");
                }
                
                
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    
    private void triggerRandomTask() {
        int taskIndex = random.nextInt(taskFactories.length);
        GameTask task = taskFactories[taskIndex].get();
        
        Platform.runLater(() -> {
            
            resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");
            resourceManager.pushCenterNotification(
                "INCOMING TASK: " + task.getTaskName(),
                "A system task requires your attention!\nCompleting this will earn you rewards.",
                "/images/notification/task_alert.png"
            );
            
            
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
    
    
    private void showTask(GameTask task) {
        task.showTask(() -> {
            if (task.isCompleted()) {
                completedTaskCount++;
                
                
                if (completedTaskCount % 5 == 0) {
                    
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
    
    
    public void debugTriggerTask() {
        triggerRandomTask();
    }
    
    
    public void debugTriggerSpecificTask(int taskIndex) {
        if (taskIndex >= 0 && taskIndex < taskFactories.length) {
            GameTask task = taskFactories[taskIndex].get();
            Platform.runLater(() -> showTask(task));
        }
    }
    
    
    public long getTimeUntilNextTask() {
        return Math.max(0, nextTaskTime - System.currentTimeMillis());
    }
    
    
    public String getFormattedTimeUntilNextTask() {
        long timeMillis = getTimeUntilNextTask();
        long seconds = (timeMillis / 1000) % 60;
        long minutes = timeMillis / (60 * 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    
    public int getCompletedTaskCount() {
        return completedTaskCount;
    }
    
    
    public int getFailedTaskCount() {
        return failedTaskCount;
    }
    
    
    public boolean isRunning() {
        return isRunning;
    }
}

