package com.vpstycoon.game.thread;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSProduct;
import com.vpstycoon.ui.game.rack.Rack; // เพิ่ม import

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
    private final Rack rack; // เพิ่ม reference ไปยัง Rack
    private LocalDateTime gameDateTime;
    private final AtomicLong gameTimeMs = new AtomicLong(0);
    private volatile boolean running = true;
    private int lastProcessedMonth = -1;

    private final List<GameTimeListener> timeListeners = new ArrayList<>();

    public interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
    }

    public GameTimeManager(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.rack = rack; // รับ Rack เข้ามาใน constructor
        this.gameDateTime = startTime;
        this.lastProcessedMonth = gameDateTime.getMonthValue();
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
                    LocalDateTime previousDateTime = gameDateTime;
                    gameDateTime = gameDateTime.plus(daysElapsed, ChronoUnit.DAYS);

                    if (gameDateTime.getMonthValue() != previousDateTime.getMonthValue()) {
                        processMonthlyKeepUp();
                        lastProcessedMonth = gameDateTime.getMonthValue();
                    }

                    notifyTimeListeners();
                }

                if (currentTime - lastPaymentCheckTime >= 1000) {
                    requestManager.processPayments(newGameTimeMs);
                    lastPaymentCheckTime = currentTime;
                }

                if (currentTime - lastOverheadTime >= overheadInterval) {
//                    processOverheadCosts(overheadCost);
                    lastOverheadTime = currentTime;
                }

                Thread.sleep(TICK_INTERVAL_MS);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void processMonthlyKeepUp() {
        long totalKeepUpCost = 0;

        // ดึง VPS ที่ติดตั้งจาก Rack แทนการใช้ vpsServers
        List<VPSOptimization> installedVPS = rack.getInstalledVPS();

        for (VPSOptimization vps : installedVPS) {
            for (VPSProduct product : VPSProduct.values()) {
                if (product.getCpu() == vps.getVCPUs() &&
                        product.getRam() == vps.getRamInGB() &&
                        product.getStorage() == vps.getDiskInGB() &&
                        product.getSize() == vps.getSize()) {
                    totalKeepUpCost += product.getKeepUp();
                    break;
                }
            }
        }

        if (totalKeepUpCost > 0) {
            long currentMoney = ResourceManager.getInstance().getCompany().getMoney();
            ResourceManager.getInstance().getCompany().setMoney(currentMoney - totalKeepUpCost);
            System.out.println("Monthly keep-up cost deducted: $" + totalKeepUpCost +
                    " | New balance: $" + ResourceManager.getInstance().getCompany().getMoney());
        }
    }

    private void processOverheadCosts(long overheadCost) {
        long totalCost = overheadCost;
        for (VPSOptimization vps : rack.getInstalledVPS()) { // เปลี่ยนจาก vpsServers เป็น rack.getInstalledVPS()
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
        rack.installVPS(vps);
    }

    public void removeVPSServer(VPSOptimization vps) {
        rack.uninstallVPS(vps);
    }

    // ลบเมธอด addVPSServer และ removeVPSServer ออก เพราะจะใช้ Rack แทน
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