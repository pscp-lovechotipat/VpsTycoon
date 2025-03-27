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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Random;

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

    // New fields for overlay approach
    protected StackPane taskContainer;
    
    // Timer components
    protected ProgressBar timerBar;
    protected Label timerLabel;
    
    // Content panes - for subclasses to use
    protected BorderPane taskPane;
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
        
        // ล้าง container ก่อนเริ่ม task ใหม่
        Platform.runLater(() -> {
            taskContainer.getChildren().clear();
            
            // Initialize the base UI components
            initializeUI();
            
            // Initialize task-specific UI elements
            initializeTaskSpecifics();
            
            // เพิ่ม task เข้าใน container
            taskContainer.getChildren().add(taskPane);
            
            // เริ่มจับเวลา
            startTimer();
            
            debugEvent("Task timeout", getTimeLimit());
        });
    }
    
    /**
     * Initialize the task UI components
     */
    protected void initializeUI() {
        taskPane = new BorderPane();
        taskPane.setPrefSize(800, 600);
        taskPane.getStyleClass().add("cyberpunk-task");
        taskPane.setStyle("-fx-background-color: #0a0a2a; -fx-border-color: #00ffff; -fx-border-width: 3px;");
        
        // Title area
        Text titleText = new Text(taskName);
        titleText.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));
        titleText.setFill(Color.CYAN);
        titleText.setStyle("-fx-effect: dropshadow(gaussian, #00ffff, 10, 0.6, 0, 0);");
        
        // Description
        Text descText = new Text(taskDescription);
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Timer components
        timerBar = new ProgressBar(1.0);
        timerBar.setPrefWidth(700);
        timerBar.setStyle("-fx-accent: linear-gradient(to right, #ff00ff, #00ffff);");
        
        timerLabel = new Label(timeLimit + "s");
        timerLabel.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 18));
        timerLabel.setTextFill(Color.WHITE);
        
        // Close button
        Button closeButton = new Button("ABORT");
        closeButton.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 14));
        closeButton.setStyle(
            "-fx-background-color: #ff3366; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #ff0066; " +
            "-fx-border-width: 2px; " +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> {
            failed = true;
            applyPenalty();
            if (taskContainer != null) {
                taskContainer.getChildren().remove(taskPane);
            }
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }
        });
        
        // Header with title, description
        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20));
        headerBox.getChildren().addAll(titleText, descText);
        
        // Footer with timer and close button
        VBox footerBox = new VBox(10);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(20));
        footerBox.getChildren().addAll(timerBar, timerLabel, closeButton);
        
        // Game area - will be populated by subclasses
        gamePane = new StackPane();
        gamePane.setStyle("-fx-background-color: rgba(0, 20, 40, 0.8);");
        
        // Control area - will be populated by subclasses
        controlPane = new VBox(15);
        controlPane.setAlignment(Pos.CENTER);
        controlPane.setPadding(new Insets(20));
        
        // Layout
        taskPane.setTop(headerBox);
        taskPane.setCenter(gamePane);
        taskPane.setBottom(footerBox);
        
        // Add CSS styles
        taskPane.getStyleClass().add("cyberpunk-task");
    }
    
    /**
     * Initialize task-specific UI elements and behaviors
     */
    protected abstract void initializeTaskSpecifics();
    
    /**
     * Start the countdown timer for the task
     */
    protected void startTimer() {
        timerThread = new Thread(() -> {
            int remainingTime = timeLimit;
            while (remainingTime > 0 && !completed && !failed) {
                final int currentTime = remainingTime;
                Platform.runLater(() -> {
                    if (timerLabel != null) {
                        timerLabel.setText(currentTime + "s");
                    }
                    if (timerBar != null) {
                        timerBar.setProgress((double) currentTime / timeLimit);
                        
                        // Update color based on time
                        if (currentTime < timeLimit * 0.25) {
                            timerBar.setStyle("-fx-accent: #ff0000;"); // Red
                        } else if (currentTime < timeLimit * 0.5) {
                            timerBar.setStyle("-fx-accent: #ffff00;"); // Yellow
                        }
                    }
                });
                
                try {
                    Thread.sleep(1000);
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
     * Apply the reward for completing the task
     */
    protected void applyReward() {
        GameState gameState = resourceManager.getCurrentState();
        Company company = gameState.getCompany();
        company.setMoney(company.getMoney() + rewardAmount);
        
        Platform.runLater(() -> {
            resourceManager.getAudioManager().playSoundEffect("reward.mp3");
            resourceManager.pushCenterNotification(
                "Task Completed: " + taskName,
                "Well done cyberpunk! You've completed the task.\nReward: $" + rewardAmount,
                "/images/notification/success.png"
            );
        });
    }
    
    /**
     * Apply the penalty for failing the task
     */
    protected void applyPenalty() {
        GameState gameState = resourceManager.getCurrentState();
        Company company = gameState.getCompany();
        double newRating = Math.max(1.0, company.getRating() - penaltyRating);
        company.setRating(newRating);
        
        Platform.runLater(() -> {
            resourceManager.getAudioManager().playSoundEffect("failure.mp3");
            resourceManager.pushCenterNotification(
                "Task Failed: " + taskName,
                "You failed to complete the task in time.\nRating dropped to: " + String.format("%.1f", newRating),
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
     * ทำความสะอาด resources ทั้งหมดและเตรียมสำหรับ task ถัดไป
     */
    private void cleanupTask() {
        // ล้าง UI
        if (taskContainer != null) {
            taskContainer.getChildren().clear();
        }
        
        // หยุด timer thread
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        
        // คืนสถานะ task
        setTaskActive(false);
    }
} 