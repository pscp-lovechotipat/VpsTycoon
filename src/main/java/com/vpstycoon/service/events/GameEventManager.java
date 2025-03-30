package com.vpstycoon.service.events;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.task.*;
import com.vpstycoon.service.events.interfaces.IGameEventManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * จัดการกิจกรรม (event) ในเกม ซึ่งรวมถึงการสร้างและแสดง task ต่างๆให้ผู้เล่น
 */
public class GameEventManager implements IGameEventManager, Runnable {

    private static final Logger LOGGER = Logger.getLogger(GameEventManager.class.getName());

    // Timing constants for tasks (เวลาตั้งต้น)
    private static final int INITIAL_TASK_DELAY = 5 * 1000; // 5 นาทีก่อน task แรกจะปรากฎ
    private static final int MIN_TASK_INTERVAL = 5 * 1000; // ต่ำสุด 3 นาทีระหว่าง task
    private static final int MAX_TASK_INTERVAL = 5 * 1000; // สูงสุด 7 นาทีระหว่าง task
    private static final int DEBUG_INTERVAL = 1 * 1000; // แสดงข้อมูล debug ทุก 5 วินาที

//    private static final int INITIAL_TASK_DELAY = 5 * 60 * 1000; // 5 นาทีก่อน task แรกจะปรากฎ
//    private static final int MIN_TASK_INTERVAL = 3 * 60 * 1000; // ต่ำสุด 3 นาทีระหว่าง task
//    private static final int MAX_TASK_INTERVAL = 7 * 60 * 1000; // สูงสุด 7 นาทีระหว่าง task
//    private static final int DEBUG_INTERVAL = 5 * 1000; // แสดงข้อมูล debug ทุก 5 วินาที

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
    
    // ความถี่ในการสร้าง task (สามารถปรับได้)
    private int minTaskInterval = MIN_TASK_INTERVAL;
    private int maxTaskInterval = MAX_TASK_INTERVAL;

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

    /**
     * สร้าง GameEventManager ด้วยข้อมูลตั้งต้น
     */
    public GameEventManager(GameplayContentPane gameplayContentPane, GameState gameState) {
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

    /**
     * เตรียม overlay สำหรับแสดง task
     */
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
                    System.out.println("[GAMEEVENT] Task overlay added to root stack");
                    
                    if (debugMode) {
                        if (rootStack.getChildren().contains(debugOverlay)) {
                            rootStack.getChildren().remove(debugOverlay);
                        }
                        rootStack.getChildren().add(debugOverlay);
                        System.out.println("[GAMEEVENT] Debug overlay added to root stack");
                    }
                } catch (Exception e) {
                    System.err.println("[GAMEEVENT] Error adding overlays: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error initializing task overlay: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * เพิ่มเอฟเฟคกลิตช์ (glitch) ให้กับ UI
     */
    private void addGlitchEffect(StackPane pane) {
        try {
            Rectangle glitchRect = new Rectangle();
            glitchRect.widthProperty().bind(pane.widthProperty());
            glitchRect.heightProperty().bind(pane.heightProperty());
            glitchRect.setFill(Color.TRANSPARENT);
            
            Glow glow = new Glow(0.3);
            glitchRect.setEffect(glow);
            
            pane.getChildren().add(0, glitchRect);
            
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            
            for (int i = 0; i < 10; i++) {
                timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.seconds(i * 0.5 + random.nextDouble() * 2), 
                                new KeyValue(glow.levelProperty(), 0.1 + random.nextDouble() * 0.4))
                );
            }
            
            timeline.play();
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error adding glitch effect: " + e.getMessage());
        }
    }

    /**
     * เริ่มการทำงานของระบบกิจกรรม
     */
    @Override
    public void start() {
        if (isRunning) {
            System.out.println("[GAMEEVENT] System is already running");
            return;
        }
        
        System.out.println("[GAMEEVENT] Starting event system");
        isRunning = true;
        nextTaskTime = System.currentTimeMillis() + INITIAL_TASK_DELAY;
        
        Thread eventThread = new Thread(this);
        eventThread.setDaemon(true);
        eventThread.setName("GameEventThread");
        eventThread.start();
    }

    /**
     * หยุดการทำงานของระบบกิจกรรม
     */
    @Override
    public void stop() {
        System.out.println("[GAMEEVENT] Stopping event system");
        isRunning = false;
    }

    /**
     * ฟังก์ชันหลักที่ใช้ในการทำงานบน thread
     */
    @Override
    public void run() {
        System.out.println("[GAMEEVENT] Thread started");
        
        try {
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                long currentTime = System.currentTimeMillis();
                
                // ตรวจสอบว่าถึงเวลาสร้าง task ใหม่หรือไม่
                if (currentTime >= nextTaskTime && !taskActive.get()) {
                    createRandomTask();
                    int interval = minTaskInterval + random.nextInt(maxTaskInterval - minTaskInterval);
                    nextTaskTime = currentTime + interval;
                    System.out.println("[GAMEEVENT] Scheduled next task in " + (interval / 1000) + " seconds");
                }
                
                // แสดงข้อมูล debug ถ้าเปิดโหมด debug
                if (debugMode && currentTime - lastDebugTime > DEBUG_INTERVAL) {
                    lastDebugTime = currentTime;
                    final long timeRemaining = Math.max(0, nextTaskTime - currentTime);
                    
                    Platform.runLater(() -> {
                        try {
                            if (debugLabel != null) {
                                String status = taskActive.get() ? "ACTIVE" : "WAITING";
                                debugLabel.setText(
                                    "Task Status: " + status + "\n" +
                                    "Next Task: " + (timeRemaining / 1000) + "s\n" +
                                    "Completed: " + completedTaskCount + "\n" +
                                    "Failed: " + failedTaskCount
                                );
                            }
                        } catch (Exception e) {
                            System.err.println("[GAMEEVENT] Error updating debug: " + e.getMessage());
                        }
                    });
                }
                
                Thread.sleep(100); 
            }
        } catch (InterruptedException e) {
            System.out.println("[GAMEEVENT] Thread interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error in event thread: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("[GAMEEVENT] Thread ended");
        }
    }

    /**
     * สร้าง task สุ่มใหม่และนำเสนอให้ผู้เล่น
     */
    @Override
    public void createRandomTask() {
        try {
            if (taskActive.get()) {
                System.out.println("[GAMEEVENT] Cannot create new task, another task is active");
                return;
            }
            
            if (taskFactories.length == 0) {
                System.err.println("[GAMEEVENT] No task factories available");
                return;
            }
            
            // เลือก task ที่จะสร้างแบบสุ่ม
            int taskIndex = random.nextInt(taskFactories.length);
            GameTask task = taskFactories[taskIndex].get();
            
            // ตรวจสอบว่าค่าที่ได้ไม่ใช่ null
            if (task == null) {
                System.err.println("[GAMEEVENT] Factory returned null task at index " + taskIndex);
                return;
            }
            
            showTask(task);
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error creating random task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * แสดง task ที่กำหนด
     */
    @Override
    public void showTask(GameTask task) {
        try {
            if (taskActive.get()) {
                System.out.println("[GAMEEVENT] Cannot show task, another task is active");
                return;
            }
            
            if (task == null) {
                System.err.println("[GAMEEVENT] Cannot show null task");
                return;
            }
            
            taskActive.set(true);
            System.out.println("[GAMEEVENT] Showing task: " + task.getTaskName());
            
            task.setTaskContainer(taskOverlay);
            
            // กำหนด callback เมื่อ task เสร็จสิ้น
            task.showTask(() -> onTaskCompleted(task, task.isCompleted()));
            
            // แสดงการแจ้งเตือนว่ามี task ใหม่
            Platform.runLater(() -> {
                try {
                    // ใช้วิธีแสดงการแจ้งเตือนแบบง่ายๆแทน ถ้า CenterNotificationView.showNotification ไม่สามารถใช้ได้
                    Label notificationLabel = new Label("New Task Available: " + task.getTaskName());
                    notificationLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-text-fill: white; -fx-padding: 10;");
                    notificationLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                    
                    StackPane notification = new StackPane(notificationLabel);
                    notification.setAlignment(Pos.TOP_CENTER);
                    notification.setPadding(new Insets(20));
                    
                    gameplayContentPane.getRootStack().getChildren().add(notification);
                    
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), notification);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setDelay(Duration.seconds(2));
                    fadeOut.setOnFinished(e -> gameplayContentPane.getRootStack().getChildren().remove(notification));
                    fadeOut.play();
                } catch (Exception e) {
                    System.err.println("[GAMEEVENT] Error showing notification: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error showing task: " + e.getMessage());
            e.printStackTrace();
            taskActive.set(false);
        }
    }

    /**
     * ตั้งค่าความถี่ในการสร้าง task
     */
    @Override
    public void setTaskFrequency(int minIntervalMs, int maxIntervalMs) {
        if (minIntervalMs > 0 && maxIntervalMs > minIntervalMs) {
            this.minTaskInterval = minIntervalMs;
            this.maxTaskInterval = maxIntervalMs;
            System.out.println("[GAMEEVENT] Task frequency set to min: " + (minIntervalMs / 1000) + 
                               "s, max: " + (maxIntervalMs / 1000) + "s");
        } else {
            System.err.println("[GAMEEVENT] Invalid task frequency parameters");
        }
    }

    /**
     * รับการแจ้งเตือนเมื่อเสร็จสิ้น task
     */
    @Override
    public void onTaskCompleted(GameTask task, boolean success) {
        try {
            taskActive.set(false);
            
            if (success) {
                completedTaskCount++;
                System.out.println("[GAMEEVENT] Task completed successfully: " + task.getTaskName() + 
                                    " - Reward: $" + task.getRewardAmount());
                
                if (gameState != null) {
                    gameState.getCompany().addMoney(task.getRewardAmount());
                    // ปรับเพิ่มความน่าเชื่อถือของบริษัทโดยเรียกใช้เมธอดที่มีอยู่
                    double currentRating = gameState.getCompany().getRating();
                    gameState.getCompany().setRating(currentRating + 0.05);
                    
                    Platform.runLater(() -> {
                        try {
                            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), debugOverlay);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);
                            fadeIn.play();
                            
                            Text rewardText = new Text("+" + task.getRewardAmount() + "$");
                            rewardText.setFont(Font.font("System", FontWeight.BOLD, 24));
                            rewardText.setFill(Color.GREEN);
                            
                            StackPane rewardPane = new StackPane(rewardText);
                            rewardPane.setAlignment(Pos.CENTER);
                            
                            gameplayContentPane.getRootStack().getChildren().add(rewardPane);
                            
                            Timeline timeline = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(rewardText.opacityProperty(), 1.0)),
                                new KeyFrame(Duration.seconds(0.5), new KeyValue(rewardText.translateYProperty(), -50)),
                                new KeyFrame(Duration.seconds(1.5), 
                                            new KeyValue(rewardText.opacityProperty(), 0.0), 
                                            new KeyValue(rewardText.translateYProperty(), -100))
                            );
                            
                            timeline.setOnFinished(e -> gameplayContentPane.getRootStack().getChildren().remove(rewardPane));
                            timeline.play();
                        } catch (Exception e) {
                            System.err.println("[GAMEEVENT] Error showing reward: " + e.getMessage());
                        }
                    });
                }
            } else {
                failedTaskCount++;
                System.out.println("[GAMEEVENT] Task failed: " + task.getTaskName() + 
                                     " - Penalty: -" + task.getPenaltyRating() + " rating points");
                
                if (gameState != null) {
                    double penaltyFactor = task.getPenaltyRating() * 0.01;
                    // ปรับลดความน่าเชื่อถือของบริษัทโดยเรียกใช้เมธอดที่มีอยู่
                    double currentRating = gameState.getCompany().getRating();
                    gameState.getCompany().setRating(currentRating - penaltyFactor);
                }
            }
            
            System.out.println("[GAMEEVENT] Tasks complete: " + completedTaskCount + 
                               ", failed: " + failedTaskCount);
        } catch (Exception e) {
            System.err.println("[GAMEEVENT] Error in onTaskCompleted: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ปรับโหมดการทำงาน (debug / non-debug)
     */
    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (debugMode) {
            LOGGER.setLevel(Level.INFO);
        } else {
            LOGGER.setLevel(Level.WARNING);
        }
        
        Platform.runLater(() -> {
            try {
                if (gameplayContentPane != null && gameplayContentPane.getRootStack() != null) {
                    if (debugMode) {
                        if (!gameplayContentPane.getRootStack().getChildren().contains(debugOverlay)) {
                            gameplayContentPane.getRootStack().getChildren().add(debugOverlay);
                        }
                        debugOverlay.setVisible(true);
                    } else {
                        debugOverlay.setVisible(false);
                    }
                }
            } catch (Exception e) {
                System.err.println("[GAMEEVENT] Error toggling debug overlay: " + e.getMessage());
            }
        });
    }

    /**
     * ตรวจสอบว่ากำลังทำงานอยู่หรือไม่
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * ตรวจสอบว่ามี task ที่กำลังทำงานอยู่หรือไม่
     */
    @Override
    public boolean isTaskActive() {
        return taskActive.get();
    }
} 