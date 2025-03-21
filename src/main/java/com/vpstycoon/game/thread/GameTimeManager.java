package com.vpstycoon.game.thread;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.VPSOptimization;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GameTimeManager {
    private static final long GAME_MONTH_MS = 15 * 60 * 1000; // 15 minutes
    private static final long TICK_INTERVAL_MS = 1000; // Update every second

    private final Company company;
    private final RequestManager requestManager;
    private final List<VPSOptimization> vpsServers;
    private LocalDateTime gameDateTime;
    private final AtomicLong gameTimeMs = new AtomicLong(0);
    private volatile boolean running = true;

    private final List<GameTimeListener> timeListeners = new ArrayList<>();

    public interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
    }

    public GameTimeManager(Company company, RequestManager requestManager, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.gameDateTime = startTime;
        this.vpsServers = new ArrayList<>();
    }

    public void start() {
        long lastTickTime = System.currentTimeMillis();
        long lastPaymentCheckTime = lastTickTime;
        long lastOverheadTime = lastTickTime;
        final long overheadInterval = GAME_MONTH_MS;
        final long overheadCost = 5000;

        while (running) {
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - lastTickTime;
                lastTickTime = currentTime;

                long newGameTimeMs = gameTimeMs.addAndGet(elapsedMs);
                long daysElapsed = elapsedMs / (GAME_MONTH_MS / 30);
                if (daysElapsed > 0) {
                    gameDateTime = gameDateTime.plus(daysElapsed, ChronoUnit.DAYS);
                    notifyTimeListeners();
                }

                if (currentTime - lastPaymentCheckTime >= 1000) {
                    requestManager.processPayments(newGameTimeMs);
                    lastPaymentCheckTime = currentTime;
                }

                if (currentTime - lastOverheadTime >= overheadInterval) {
                    processOverheadCosts(overheadCost);
                    lastOverheadTime = currentTime;
                }

                Thread.sleep(TICK_INTERVAL_MS);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void processOverheadCosts(long overheadCost) {
        long totalCost = overheadCost;
        for (VPSOptimization vps : vpsServers) {
            totalCost += vps.getVCPUs() * 1000;
        }
        long currentMoney = company.getMoney();
        company.setMoney(currentMoney - totalCost);
    }

    private void notifyTimeListeners() {
        for (GameTimeListener listener : timeListeners) {
            listener.onTimeChanged(gameDateTime, gameTimeMs.get());
        }
    }

    public void addVPSServer(VPSOptimization vps) {
        vpsServers.add(vps);
    }

    public void removeVPSServer(VPSOptimization vps) {
        vpsServers.remove(vps);
    }

    public void addTimeListener(GameTimeListener listener) {
        timeListeners.add(listener);
    }

    public void removeTimeListener(GameTimeListener listener) {
        timeListeners.remove(listener);
    }

    public LocalDateTime getGameDateTime() {
        return gameDateTime;
    }

    public long getGameTimeMs() {
        return gameTimeMs.get();
    }

    public void stop() {
        running = false;
    }
}