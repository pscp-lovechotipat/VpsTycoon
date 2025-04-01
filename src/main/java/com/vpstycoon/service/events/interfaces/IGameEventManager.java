package com.vpstycoon.service.events.interfaces;

import com.vpstycoon.game.thread.task.GameTask;


public interface IGameEventManager {
    
    
    void start();
    
    
    void stop();
    
    
    void createRandomTask();
    
    
    void showTask(GameTask task);
    
    
    void setTaskFrequency(int minIntervalMs, int maxIntervalMs);
    
    
    void onTaskCompleted(GameTask task, boolean success);
    
    
    void setDebugMode(boolean debugMode);
    
    
    boolean isRunning();
    
    
    boolean isTaskActive();
} 
