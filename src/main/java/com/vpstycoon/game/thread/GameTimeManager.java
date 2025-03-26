package com.vpstycoon.game.thread;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSProduct;
import com.vpstycoon.ui.game.rack.Rack;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class GameTimeManager {
    public static final long GAME_DAY_MS = 30000;    // 30 วินาทีจริง = 1 วันในเกม
    public static final long GAME_WEEK_MS = GAME_DAY_MS * 7;
    public static final long GAME_MONTH_MS = GAME_DAY_MS * 30;
    public static final long GAME_YEAR_MS = GAME_MONTH_MS * 12;
    public static final long TICK_INTERVAL_MS = 1000; // ใส่ประมาณนี้พอ เดี่ยว observer แตกกระจุย
    private static final long OVERHEAD_INTERVAL = GAME_MONTH_MS;
    private static final double SCALE_FACTOR = 86400000.0 / GAME_DAY_MS; // ใน 1 วัน มี 86,400,000 ms  / 30,000 ms

    private final Company company;
    private final RequestManager requestManager;
    private final Rack rack;
    private final LocalDateTime startDateTime; // เพิ่มตัวแปรสำหรับจุดเริ่มต้น
    private LocalDateTime gameDateTime;
    private final AtomicLong realTimeMs = new AtomicLong(0); // มิลลิวินาทีจริงที่ผ่านไป
    private volatile boolean running = true;
    private int lastProcessedMonth = -1;

    private final List<GameTimeListener> timeListeners = new ArrayList<>();

    public interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
        void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period);
    }

    public GameTimeManager(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.rack = rack;
        this.startDateTime = startTime != null ? startTime : LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        this.gameDateTime = this.startDateTime;
        this.lastProcessedMonth = gameDateTime.getMonthValue();
    }

    public void start() {
        long lastTickTime = System.currentTimeMillis();
        long lastPaymentCheckTime = lastTickTime;
        long lastOverheadTime = lastTickTime;
        long lastRentalCheckTime = lastTickTime;

        System.out.println("Thread TimeManager Initializing...");

        while (running) {
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - lastTickTime;
                lastTickTime = currentTime;

                realTimeMs.addAndGet(elapsedMs);

                long gameMs = (long) (realTimeMs.get() * SCALE_FACTOR);

                gameDateTime = startDateTime.plus(gameMs, ChronoUnit.MILLIS);

                // ตรวจสอบการเปลี่ยนเดือน
                if (gameDateTime.getMonthValue() != lastProcessedMonth) {
                    processMonthlyKeepUp();
                    lastProcessedMonth = gameDateTime.getMonthValue();
                }

                notifyTimeListeners();

                if (currentTime - lastPaymentCheckTime >= GAME_DAY_MS) {
                    requestManager.processPayments(realTimeMs.get());
                    lastPaymentCheckTime = currentTime;
                }

                if (currentTime - lastOverheadTime >= OVERHEAD_INTERVAL) {
                    lastOverheadTime = currentTime;
                }

                if (currentTime - lastRentalCheckTime >= GAME_DAY_MS) {
                    checkRentalExpirations(realTimeMs.get());
                    lastRentalCheckTime = currentTime;
                }

                Thread.sleep(TICK_INTERVAL_MS);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void checkRentalExpirations(long currentGameTimeMs) {
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.isActive()) {
                long rentalStartTime = request.getLastPaymentTime();
                CustomerRequest.RentalPeriodType period = request.getRentalPeriodType();
                if (period != null && rentalStartTime > 0) {
                    long durationMs = period.getDays() * GAME_DAY_MS;
                    long timeSinceLastPaymentMs = currentGameTimeMs - rentalStartTime;

                    // ตรวจสอบการชำระเงินตามรอบ
                    if (request.isPaymentDue(currentGameTimeMs)) {
                        double payment = request.getPaymentAmount();
                        company.addMoney(payment);
                        request.recordPayment(currentGameTimeMs);
                        System.out.println("Payment received from " + request.getName() + ": $" + payment +
                                " | Game time: " + currentGameTimeMs);
                    }

                    // ตรวจสอบการหมดสัญญา
                    if (currentGameTimeMs >= rentalStartTime + durationMs) {
                        // Mark the request as expired BEFORE notifying listeners
                        // This ensures that listeners can check the correct status
                        request.markAsExpired();
                        
                        // Notify listeners to handle the contract expiration
                        // For example, to decide whether to renew the contract
                        for (GameTimeListener listener : timeListeners) {
                            listener.onRentalPeriodCheck(request, period);
                            System.out.println("Rental period check completed for " + request.getName());
                        }
                    }
                }
            }
        }
    }

    private void processMonthlyKeepUp() {
        long totalKeepUpCost = 0;
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
            long currentMoney = company.getMoney();
            company.setMoney(currentMoney - totalKeepUpCost);
            System.out.println("Monthly keep-up cost deducted: $" + totalKeepUpCost +
                    " | New balance: $" + company.getMoney());
        }
    }

    private void notifyTimeListeners() {
        for (GameTimeListener listener : timeListeners) {
            listener.onTimeChanged(gameDateTime, realTimeMs.get());
        }
    }

    public void addVPSServer(VPSOptimization vps) {
        rack.installVPS(vps);
    }

    public void removeVPSServer(VPSOptimization vps) {
        rack.uninstallVPS(vps);
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
        return this.realTimeMs.get();
    }

    public void stop() {
        running = false;
    }
}