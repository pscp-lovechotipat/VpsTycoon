package com.vpstycoon.game.thread;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.VPSOptimization;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages game time progression and periodic events.
 * In this game, 1 month = 15 minutes of real time.
 */
public class GameTimeManager extends Thread {
    // Game time settings
    private static final long GAME_MONTH_MS = 15 * 60 * 1000; // 15 minutes in milliseconds
    private static final long TICK_INTERVAL_MS = 1000; // Update every second
    
    // Game state
    private final Company company;
    private final RequestManager requestManager;
    private final List<VPSOptimization> vpsServers;
    private LocalDateTime gameDateTime;
    private final AtomicLong gameTimeMs = new AtomicLong(0);
    private volatile boolean running = true;
    
    // Listeners for time changes
    private final List<GameTimeListener> timeListeners = new ArrayList<>();
    
    // Interface for time change listeners
    public interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
    }
    
    public GameTimeManager(Company company, RequestManager requestManager, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.gameDateTime = startTime;
        this.vpsServers = new ArrayList<>();
        this.setDaemon(true);
        this.setName("GameTimeManager");
    }
    
    @Override
    public void run() {
        long lastTickTime = System.currentTimeMillis();
        long lastPaymentCheckTime = lastTickTime;
        long lastOverheadTime = lastTickTime;
        
        // Monthly overhead cost (every 15 minutes of real time)
        final long overheadInterval = GAME_MONTH_MS;
        final long overheadCost = 5000; // 5,000 THB per month
        
        while (!interrupted() && running) {
            try {
                // Calculate elapsed time since last tick
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - lastTickTime;
                lastTickTime = currentTime;
                
                // Update game time
                long newGameTimeMs = gameTimeMs.addAndGet(elapsedMs);
                
                // Update game date/time (1 month = 15 minutes)
                // 1 day = 30 seconds
                long daysElapsed = elapsedMs / (GAME_MONTH_MS / 30);
                if (daysElapsed > 0) {
                    gameDateTime = gameDateTime.plus(daysElapsed, ChronoUnit.DAYS);
                    
                    // Notify listeners of time change
                    notifyTimeListeners();
                }
                
                // Process payments (check every second)
                if (currentTime - lastPaymentCheckTime >= 1000) {
                    requestManager.processPayments(newGameTimeMs);
                    lastPaymentCheckTime = currentTime;
                }
                
                // Process monthly overhead costs
                if (currentTime - lastOverheadTime >= overheadInterval) {
                    processOverheadCosts(overheadCost);
                    lastOverheadTime = currentTime;
                }
                
                // Sleep until next tick
                Thread.sleep(TICK_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                System.out.println("GameTimeManager interrupted, stopping...");
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Process monthly overhead costs
     * @param overheadCost The base overhead cost
     */
    private void processOverheadCosts(long overheadCost) {
        // Calculate total overhead cost (base + servers)
        long totalCost = overheadCost;
        
        // Add server maintenance costs
        for (VPSOptimization vps : vpsServers) {
            // Each server costs 1,000 THB per month per vCPU
            totalCost += vps.getVCPUs() * 1000;
        }
        
        // Deduct from company money
        long currentMoney = company.getMoney();
        company.setMoney(currentMoney - totalCost);
        
        System.out.println("Monthly overhead costs: " + totalCost + " THB");
        System.out.println("Company money after overhead: " + company.getMoney() + " THB");
    }
    
    /**
     * Add a VPS server to be managed by the time manager
     * @param vps The VPS server to add
     */
    public void addVPSServer(VPSOptimization vps) {
        vpsServers.add(vps);
    }
    
    /**
     * Remove a VPS server from management
     * @param vps The VPS server to remove
     */
    public void removeVPSServer(VPSOptimization vps) {
        vpsServers.remove(vps);
    }
    
    /**
     * Add a listener for time changes
     * @param listener The listener to add
     */
    public void addTimeListener(GameTimeListener listener) {
        timeListeners.add(listener);
    }
    
    /**
     * Remove a time change listener
     * @param listener The listener to remove
     */
    public void removeTimeListener(GameTimeListener listener) {
        timeListeners.remove(listener);
    }
    
    /**
     * Notify all time listeners of a time change
     */
    private void notifyTimeListeners() {
        for (GameTimeListener listener : timeListeners) {
            listener.onTimeChanged(gameDateTime, gameTimeMs.get());
        }
    }
    
    /**
     * Get the current game date/time
     * @return The current game date/time
     */
    public LocalDateTime getGameDateTime() {
        return gameDateTime;
    }
    
    /**
     * Get the current game time in milliseconds
     * @return The current game time in milliseconds
     */
    public long getGameTimeMs() {
        return gameTimeMs.get();
    }
    
    /**
     * Stop the game time manager
     */
    public void stopTimeManager() {
        running = false;
        this.interrupt();
    }
    
    /**
     * Convert real time to game time
     * @param realTimeMs Real time in milliseconds
     * @return Game time in days
     */
    public static int realTimeToGameDays(long realTimeMs) {
        return (int) (realTimeMs * 30 / GAME_MONTH_MS);
    }
    
    /**
     * Convert game time to real time
     * @param gameDays Game time in days
     * @return Real time in milliseconds
     */
    public static long gameDaysToRealTime(int gameDays) {
        return gameDays * GAME_MONTH_MS / 30;
    }
} 