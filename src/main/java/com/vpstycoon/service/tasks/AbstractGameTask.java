package com.vpstycoon.service.tasks;

import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.service.tasks.interfaces.IGameTask;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import java.util.Random;


public abstract class AbstractGameTask implements IGameTask {
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

    
    public AbstractGameTask(String taskName, String taskDescription, String imagePath, 
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
    
    
    protected void safePlaySound(AudioClip clip, double volume) {
        if (clip != null) {
            try {
                clip.play(volume);
            } catch (Exception e) {
                log("Error playing sound: " + e.getMessage());
            }
        }
    }

    
    @Override
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

    
    @Override
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
                
                setTaskActive(true);
            }
            
            this.onCompleteCallback = onComplete;
            
            Platform.runLater(() -> {
                try {
                    createTaskUI();
                    taskContainer.getChildren().add(contentPane);
                    taskContainer.setVisible(true);
                    
                    safePlaySound(taskStartSound, 0.3);
                    
                    startTimer();
                } catch (Exception e) {
                    log("Error showing task: " + e.getMessage());
                    e.printStackTrace();
                    completeTask(false);
                }
            });
        } catch (Exception e) {
            log("Error in showTask: " + e.getMessage());
            e.printStackTrace();
            completeTask(false);
        }
    }

    
    @Override
    public void startTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        
        timerThread = new Thread(() -> {
            try {
                int totalTimeMs = timeLimit * 1000;
                long startTime = System.currentTimeMillis();
                long elapsedTime = 0;
                int tickCounter = 0;
                
                while (elapsedTime < totalTimeMs && !Thread.currentThread().isInterrupted()) {
                    if (completed || failed) {
                        break;
                    }
                    
                    elapsedTime = System.currentTimeMillis() - startTime;
                    double progress = 1.0 - ((double) elapsedTime / totalTimeMs);
                    
                    final long finalElapsedTime = elapsedTime;
                    Platform.runLater(() -> {
                        try {
                            timerBar.setProgress(Math.max(0, progress));
                            int secondsLeft = (int) ((totalTimeMs - finalElapsedTime) / 1000);
                            timerLabel.setText(secondsLeft + "s");
                            
                            if (progress < 0.25) {
                                timerBar.setStyle("-fx-accent: red;");
                            } else if (progress < 0.5) {
                                timerBar.setStyle("-fx-accent: orange;");
                            }
                        } catch (Exception e) {
                            log("Error updating timer UI: " + e.getMessage());
                        }
                    });
                    
                    tickCounter++;
                    if (tickCounter % 10 == 0 && taskTickSound != null) {
                        safePlaySound(taskTickSound, 0.1);
                    }
                    
                    Thread.sleep(100);
                }
                
                if (!completed && !failed && elapsedTime >= totalTimeMs) {
                    log("Task timed out");
                    completeTask(false);
                }
            } catch (InterruptedException e) {
                log("Timer interrupted");
            } catch (Exception e) {
                log("Error in timer thread: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        timerThread.setDaemon(true);
        timerThread.start();
    }

    
    @Override
    public void stopTimer() {
        if (timerThread != null) {
            timerThread.interrupt();
            timerThread = null;
        }
    }
    
    
    protected void completeTask(boolean success) {
        if (completed || failed) return;
        
        if (success) {
            setCompleted(true);
            log("Task completed successfully");
            safePlaySound(taskCompleteSound, 0.3);
            showSuccess();
        } else {
            setFailed(true);
            log("Task failed");
            safePlaySound(taskFailSound, 0.3);
            showFailure();
        }
        
        stopTimer();
        
        if (onCompleteCallback != null) {
            try {
                onCompleteCallback.run();
            } catch (Exception e) {
                log("Error in onCompleteCallback: " + e.getMessage());
            }
        }
        
        Platform.runLater(() -> {
            try {
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        Platform.runLater(() -> {
                            taskContainer.getChildren().remove(contentPane);
                            taskContainer.setVisible(false);
                            setTaskActive(false);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } catch (Exception e) {
                log("Error cleaning up task: " + e.getMessage());
            }
        });
    }
    
    
    protected void log(String message) {
        System.out.println("[" + getClass().getSimpleName() + "] " + message);
    }
    
    
    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    
    @Override
    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    
    @Override
    public boolean isCompleted() {
        return completed;
    }
    
    
    @Override
    public boolean isFailed() {
        return failed;
    }
    
    
    @Override
    public String getTaskName() {
        return taskName;
    }
    
    
    @Override
    public String getTaskDescription() {
        return taskDescription;
    }
    
    
    @Override
    public int getRewardAmount() {
        return rewardAmount;
    }
    
    
    @Override
    public int getPenaltyRating() {
        return penaltyRating;
    }
    
    
    @Override
    public int getDifficultyLevel() {
        return difficultyLevel;
    }
    
    
    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    
    @Override
    public abstract void createTaskUI();
    
    
    @Override
    public abstract void showSuccess();
    
    
    @Override
    public abstract void showFailure();
} 
