package com.vpstycoon.service.time.interfaces;

import java.time.LocalDateTime;


public interface IGameTimeController {
    
    
    void startTime();
    
    
    void stopTime();
    
    
    void addTimeListener(IGameTimeManager.GameTimeListener listener);
    
    
    void removeTimeListener(IGameTimeManager.GameTimeListener listener);
    
    
    LocalDateTime getGameDateTime();
    
    
    IGameTimeManager getGameTimeManager();
    
    
    long getGameTimeMs();
    
    
    void resetTime(LocalDateTime startTime);
    
    
    void resetTime();
} 
