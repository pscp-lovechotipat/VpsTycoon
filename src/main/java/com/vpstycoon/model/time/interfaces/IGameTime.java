package com.vpstycoon.model.time.interfaces;

import java.time.LocalDateTime;


public interface IGameTime {

    
    LocalDateTime getCurrentDateTime();
    
    
    void setCurrentDateTime(LocalDateTime dateTime);
    
    
    LocalDateTime getStartDateTime();
    
    
    long getGameTimeMs();
    
    
    void setGameTimeMs(long timeMs);
    
    
    LocalDateTime calculateGameTime(long realTimeMs);
    
    
    double getTimeScale();
    
    
    void setTimeScale(double scale);
    
    
    void addRealTimeMs(long elapsedRealMs);
    
    
    void resetTime();
} 
