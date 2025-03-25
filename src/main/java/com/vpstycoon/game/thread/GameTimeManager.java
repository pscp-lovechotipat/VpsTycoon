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
    public static final long GAME_DAY_MS = 30000;    // 30 วินาทีต่อวัน (900,000 ÷ 30)
    public static final long GAME_WEEK_MS = GAME_DAY_MS * 7;    // 210,000 ms (3.5 นาที)
    public static final long GAME_MONTH_MS = GAME_DAY_MS * 30;  // 900,000 ms (15 นาที)
    public static final long GAME_YEAR_MS = GAME_MONTH_MS * 12; // 10,800,000 ms (180 นาที หรือ 3 ชั่วโมง)
    public static final long TICK_INTERVAL_MS = 1000;  // ยังคง tick ทุก 1 วินาที

    private final Company company;
    private final RequestManager requestManager;
    private final Rack rack;
    private LocalDateTime gameDateTime;
    private final AtomicLong gameTimeMs = new AtomicLong(0);
    private volatile boolean running = true;
    private int lastProcessedMonth = -1;

    private final List<GameTimeListener> timeListeners = new ArrayList<>();

    private static GameTimeManager instance; // เพิ่ม instance ถ้าต้องการ getInstance()

    public static GameTimeManager getInstance() {
        if (instance == null) {
            instance = new GameTimeManager(
                    ResourceManager.getInstance().getCompany(),
                    ResourceManager.getInstance().getRequestManager(),
                    ResourceManager.getInstance().getRack(),
                    ResourceManager.getInstance().getCurrentState().getLocalDateTime()
            );
        }
        return instance;
    }

    public interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
        void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period);
    }

    public GameTimeManager(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.rack = rack;
        this.gameDateTime = startTime != null ? startTime : LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        this.lastProcessedMonth = gameDateTime.getMonthValue();
    }

    public void start() {
        long lastTickTime = System.currentTimeMillis();
        long lastPaymentCheckTime = lastTickTime;
        long lastOverheadTime = lastTickTime;
        long lastRentalCheckTime = lastTickTime;
        long accumulatedMs = 0;
        final long overheadInterval = GAME_MONTH_MS;

        System.out.println("Thread TimeManager Initializing...");

        while (running) {
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - lastTickTime;
                lastTickTime = currentTime;

                accumulatedMs += elapsedMs;

                // คำนวณจำนวนวันที่ผ่านไป
                long daysElapsed = accumulatedMs / GAME_DAY_MS;
                if (daysElapsed > 0) {
                    LocalDateTime previousDateTime = gameDateTime;
                    gameDateTime = gameDateTime.plusDays(daysElapsed);
                    accumulatedMs -= daysElapsed * GAME_DAY_MS;
                    gameTimeMs.addAndGet(daysElapsed * GAME_DAY_MS);

                    if (gameDateTime.getMonthValue() != previousDateTime.getMonthValue()) {
                        processMonthlyKeepUp();
                        lastProcessedMonth = gameDateTime.getMonthValue();
                    }

                    notifyTimeListeners();
                }

                // การตรวจสอบอื่นๆ
                if (currentTime - lastPaymentCheckTime >= GAME_DAY_MS) {
                    requestManager.processPayments(gameTimeMs.get());
                    lastPaymentCheckTime = currentTime;
                }

                if (currentTime - lastOverheadTime >= overheadInterval) {
                    lastOverheadTime = currentTime;
                }

                if (currentTime - lastRentalCheckTime >= GAME_DAY_MS) {
                    checkRentalExpirations(gameTimeMs.get());
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
                        for (GameTimeListener listener : timeListeners) {
                            listener.onRentalPeriodCheck(request, period);
                            System.out.println("Rental period check completed for " + request.getName());
                        }
                        request.markAsExpired();
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

    public void addVPSServer(VPSOptimization vps) {
        rack.installVPS(vps);
    }

    public void removeVPSServer(VPSOptimization vps) {
        rack.uninstallVPS(vps);
    }

    private void notifyTimeListeners() {
        for (GameTimeListener listener : timeListeners) {
            listener.onTimeChanged(gameDateTime, gameTimeMs.get());
        }
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
        return this.gameTimeMs.get();
    }

    public void stop() {
        running = false;
    }
}