package com.vpstycoon.service.tasks.interfaces;

import javafx.scene.layout.StackPane;


public interface IGameTask {
    
    
    void setTaskContainer(StackPane container);
    
    
    void showTask(Runnable onComplete);
    
    
    void createTaskUI();
    
    
    void startTimer();
    
    
    void stopTimer();
    
    
    void showSuccess();
    
    
    void showFailure();
    
    
    void setCompleted(boolean completed);
    
    
    void setFailed(boolean failed);
    
    
    boolean isCompleted();
    
    
    boolean isFailed();
    
    
    String getTaskName();
    
    
    String getTaskDescription();
    
    
    int getRewardAmount();
    
    
    int getPenaltyRating();
    
    
    int getDifficultyLevel();
    
    
    int getTimeLimit();
} 
