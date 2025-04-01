package com.vpstycoon.service.time.interfaces;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.vps.VPSOptimization;

import java.time.LocalDateTime;


public interface IGameTimeManager {
    
    
    void start();
    
    
    void stop();
    
    
    void resetTime(LocalDateTime newStartDateTime);
    
    
    void addVPSServer(VPSOptimization vps);
    
    
    void removeVPSServer(VPSOptimization vps);
    
    
    void addTimeListener(GameTimeListener listener);
    
    
    void removeTimeListener(GameTimeListener listener);
    
    
    LocalDateTime getGameDateTime();
    
    
    long getGameTimeMs();
    
    
    boolean isRunning();
    
    
    interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
        void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period);
    }
} 
