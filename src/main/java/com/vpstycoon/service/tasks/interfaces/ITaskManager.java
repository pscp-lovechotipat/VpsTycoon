package com.vpstycoon.service.tasks.interfaces;

import java.util.List;


public interface ITaskManager {
    
    
    void registerTask(IGameTask task);
    
    
    void unregisterTask(IGameTask task);
    
    
    List<IGameTask> getAllTasks();
    
    
    IGameTask getActiveTask();
    
    
    boolean isTaskActive();
    
    
    IGameTask createRandomTask();
    
    
    void startTask(IGameTask task);
    
    
    void completeTask(boolean success);
    
    
    void setTaskCompletionCallback(TaskCompletionCallback callback);
    
    
    interface TaskCompletionCallback {
        void onTaskCompleted(IGameTask task, boolean success);
    }
} 
