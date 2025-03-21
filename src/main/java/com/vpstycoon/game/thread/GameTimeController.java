package com.vpstycoon.game.thread;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.VPSOptimization;

import java.time.LocalDateTime;

public class GameTimeController {
    private final GameTimeManager timeManager;
    private Thread timeThread;

    public GameTimeController(Company company, RequestManager requestManager, LocalDateTime startTime) {
        this.timeManager = new GameTimeManager(company, requestManager, startTime);
    }

    public void startTime() {
        if (timeThread == null || !timeThread.isAlive()) {
            timeThread = new Thread(() -> timeManager.start());
            timeThread.setDaemon(true);
            timeThread.setName("GameTimeThread");
            timeThread.start();
        }
    }

    public void stopTime() {
        timeManager.stop();
        if (timeThread != null) {
            try {
                timeThread.join(); // รอให้เธรดหยุดทำงาน
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addVPSServer(VPSOptimization vps) {
        timeManager.addVPSServer(vps);
    }

    public void removeVPSServer(VPSOptimization vps) {
        timeManager.removeVPSServer(vps);
    }

    public void addTimeListener(GameTimeManager.GameTimeListener listener) {
        timeManager.addTimeListener(listener);
    }

    public void removeTimeListener(GameTimeManager.GameTimeListener listener) {
        timeManager.removeTimeListener(listener);
    }

    public LocalDateTime getGameDateTime() {
        return timeManager.getGameDateTime();
    }

    public long getGameTimeMs() {
        return timeManager.getGameTimeMs();
    }
}