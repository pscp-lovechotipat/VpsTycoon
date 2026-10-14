package com.vpstycoon.thread;

import com.vpstycoon.manager.ResourceManager;
import com.vpstycoon.model.GameState;
import com.vpstycoon.model.company.Company;
import com.vpstycoon.ui.common.CyberpunkEffects;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.task.CalibrationTask;
import com.vpstycoon.ui.game.task.DataDecryptionTask;
import com.vpstycoon.ui.game.task.DataSortingTask;
import com.vpstycoon.ui.game.task.FileRecoveryTask;
import com.vpstycoon.ui.game.task.FirewallDefenseTask;
import com.vpstycoon.ui.game.task.GameTask;
import com.vpstycoon.ui.game.task.HackingTask;
import com.vpstycoon.ui.game.task.NetworkRoutingTask;
import com.vpstycoon.ui.game.task.PasswordCrackingTask;
import com.vpstycoon.ui.game.task.ResourceOptimizationTask;
import com.vpstycoon.ui.game.task.ServerCoolingTask;
import com.vpstycoon.ui.game.task.WireTask;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class GameEvent implements Runnable {
    private static final int INITIAL_TASK_DELAY = 5 * 60 * 1000;
    private static final int MIN_TASK_INTERVAL = 2 * 60 * 1000;
    private static final int MAX_TASK_INTERVAL = 10 * 60 * 1000;
    private static final int DEBUG_INTERVAL = 5 * 1000;

    private final GameplayContentPane gameplayContentPane;
    private final ResourceManager resourceManager = ResourceManager.getInstance();
    private final Random random = new Random();

    private volatile boolean isRunning;
    private int completedTaskCount = 0;
    private int failedTaskCount = 0;
    private long nextTaskTime = 0;
    private long lastDebugTime = 0;

    private final AtomicBoolean taskActive = new AtomicBoolean(false);

    private final boolean debugMode;

    private StackPane taskOverlay;

    private final List<Supplier<GameTask>> taskFactories = List.of(
        () -> new WireTask(random.nextInt(3) + 3),
        () -> new DataDecryptionTask(random.nextInt(3) + 3),
        FirewallDefenseTask::new,
        DataSortingTask::new,
        PasswordCrackingTask::new,
        NetworkRoutingTask::new,
        ServerCoolingTask::new,
        ResourceOptimizationTask::new,
        CalibrationTask::new,
        FileRecoveryTask::new,
        HackingTask::new
    );

    public GameEvent(GameplayContentPane gameplayContentPane, GameState gameState) {
        System.out.println("[GAMEEVENT] Initializing GameEvent system");

        if (gameplayContentPane == null) {
            throw new IllegalArgumentException("gameplayContentPane cannot be null");
        }

        if (gameState == null) {
            throw new IllegalArgumentException("gameState cannot be null");
        }

        this.gameplayContentPane = gameplayContentPane;
        this.isRunning = false;
        this.debugMode = gameplayContentPane.isShowDebug();

        initializeTaskOverlay();

        System.out.println("[GAMEEVENT] GameEvent system successfully initialized");
    }

    private void initializeTaskOverlay() {
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
                StackPane rootStack = gameplayContentPane.getRootStack();
                if (rootStack == null) {
                    System.err.println("[GAMEEVENT] Error: rootStack is null");
                    return;
                }

                rootStack.getChildren().remove(taskOverlay);
                rootStack.getChildren().add(taskOverlay);

                taskOverlay.prefWidthProperty().bind(rootStack.widthProperty());
                taskOverlay.prefHeightProperty().bind(rootStack.heightProperty());

                StackPane.setAlignment(taskOverlay, Pos.CENTER);

                taskOverlay.setClip(new Rectangle(taskOverlay.getWidth(), taskOverlay.getHeight()));

                taskOverlay.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) ->
                    taskOverlay.setClip(new Rectangle(newBounds.getWidth(), newBounds.getHeight()))
                );

                System.out.println("[GAMEEVENT] Task overlay successfully initialized and added to UI");
            } catch (Exception e) {
                System.err.println("[GAMEEVENT] Error initializing task overlay: " + e.getMessage());
                e.printStackTrace();
            }
        });
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

            System.out.println("[GAMEEVENT] Task system started. First task in " + formatTime(INITIAL_TASK_DELAY));

            logStatus();

            while (isRunning) {
                long currentTime = System.currentTimeMillis();

                if (debugMode && (currentTime - lastDebugTime) >= DEBUG_INTERVAL) {
                    lastDebugTime = currentTime;
                    logStatus();
                }

                if (currentTime >= nextTaskTime && !taskActive.get()) {
                    triggerRandomTask();

                    logStatus();
                }

                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            isRunning = false;
            System.out.println("[GAMEEVENT] GameEvent thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void logStatus() {
        if (!debugMode) return;

        String status = taskActive.get() ? "TASK ACTIVE" : "NEXT TASK: " + formatTime(getTimeUntilNextTask());
        System.out.println("[GAMEEVENT] " + status + " | " +
                          "Completed: " + completedTaskCount + " | " +
                          "Failed: " + failedTaskCount);
    }

    private void handleTaskAbort() {
        try {
            System.out.println("[GAMEEVENT] Task aborted from notification.");

            resourceManager.getAudioManager().playSoundEffect("failure.mp3");

            resourceManager.pushCenterNotificationAutoClose(
                "Task Aborted",
                "You decided to skip this task.\nAnother task will be available soon.",
                "/images/notification/failure.png",
                3000
            );

            scheduleNextTask();
        } catch (Exception e) {
            System.out.println("[GAMEEVENT] Error handling task abort: " + e.getMessage());
            taskActive.set(false);
        }
    }

    private void triggerRandomTask() {
        if (!taskActive.compareAndSet(false, true)) {
            System.out.println("[GAMEEVENT] Cannot trigger new task. A task is already active.");
            return;
        }

        GameTask task = taskFactories.get(random.nextInt(taskFactories.size())).get();

        System.out.println("[GAMEEVENT] Triggering task: " + task.getTaskName() +
                          ", difficulty: " + task.getDifficultyLevel() +
                          ", reward: $" + task.getRewardAmount() +
                          ", time limit: " + task.getTimeLimit() + "s");

        Platform.runLater(() -> {
            try {
                resourceManager.getAudioManager().playSoundEffect("task-alert.mp3");

                resourceManager.getCenterNotificationView().createAndShowTaskNotification(
                    "INCOMING TASK: " + task.getTaskName(),
                    "A system task requires your attention!\nCompleting this will earn you rewards.",
                    "/images/notification/task_alert.png",
                    () -> showTask(task),
                    this::handleTaskAbort
                );
            } catch (Exception e) {
                System.out.println("[GAMEEVENT] Error showing task notification: " + e.getMessage());
                taskActive.set(false);
            }
        });
    }

    private void showTask(GameTask task) {
        System.out.println("[GAMEEVENT] Attempting to show task UI: " + task.getTaskName());

        Platform.runLater(() -> {
            try {
                taskOverlay.getChildren().clear();

                task.setTaskContainer(taskOverlay);

                taskOverlay.setOpacity(0);
                taskOverlay.setVisible(true);

                gameplayContentPane.getRootStack().getChildren().remove(taskOverlay);
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

                System.out.println("[GAMEEVENT] Task UI displayed successfully: " + task.getTaskName());
            } catch (Exception e) {
                System.out.println("[GAMEEVENT] Error showing task UI: " + e.getMessage());
                e.printStackTrace();

                taskActive.set(false);
            }
        });
    }

    private void processTaskCompletion(GameTask task) {
        if (task.isCompleted()) {
            System.out.println("[GAMEEVENT] Task completed: " + task.getTaskName());

            completedTaskCount++;

            if (completedTaskCount % 5 == 0) {
                awardMilestoneBonus();
            }
        } else if (task.isFailed()) {
            System.out.println("[GAMEEVENT] Task failed: " + task.getTaskName());

            failedTaskCount++;
        }

        scheduleNextTask();
    }

    private void awardMilestoneBonus() {
        long bonus = 10000L * (completedTaskCount / 5);
        Company company = resourceManager.getCurrentState().getCompany();
        company.setMoney(company.getMoney() + bonus);

        System.out.println("[GAMEEVENT] Milestone bonus: $" + bonus + " for " + completedTaskCount + " completed tasks");

        int milestone = completedTaskCount;
        Thread bonusThread = new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> resourceManager.pushCenterNotificationAutoClose(
                    "TASK MILESTONE ACHIEVED",
                    "You've completed " + milestone + " tasks!\nBonus payment: $" + bonus,
                    "/images/notification/bonus.png",
                    5000
                ));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        bonusThread.setDaemon(true);
        bonusThread.start();
    }

    private void scheduleNextTask() {
        int interval = MIN_TASK_INTERVAL + random.nextInt(MAX_TASK_INTERVAL - MIN_TASK_INTERVAL);
        nextTaskTime = System.currentTimeMillis() + interval;

        System.out.println("[GAMEEVENT] Task completed/failed. Next task in " + formatTime(interval));

        taskActive.set(false);

        logStatus();
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
            Thread eventThread = new Thread(this);
            eventThread.setDaemon(true);
            eventThread.start();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long getTimeUntilNextTask() {
        return Math.max(0, nextTaskTime - System.currentTimeMillis());
    }
}
