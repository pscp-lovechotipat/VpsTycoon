package com.vpstycoon.service.time.interfaces;


public interface IRequestGenerator {
    
    
    void stopGenerator();
    
    
    void resetGenerator();
    
    
    void pauseGenerator();
    
    
    void resumeGenerator();
    
    
    boolean isPaused();
    
    
    void setMaxPendingRequests(int maxRequests);
} 
