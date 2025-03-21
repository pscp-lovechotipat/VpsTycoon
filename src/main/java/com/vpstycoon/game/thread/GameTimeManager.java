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
    public static final long GAME_DAY_MS = 30_000;    // 30 วินาทีต่อวันในเกม
    public static final long GAME_WEEK_MS = GAME_DAY_MS * 7;
    public static final long GAME_MONTH_MS = GAME_DAY_MS * 30;
    public static final long GAME_YEAR_MS = GAME_MONTH_MS * 12;
    public static final long TICK_INTERVAL_MS = 1000;

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
        this.gameDateTime = startTime;
        this.lastProcessedMonth = gameDateTime.getMonthValue();
    }

    public void start() {
        long lastTickTime = System.currentTimeMillis();
        long lastPaymentCheckTime = lastTickTime;
        long lastOverheadTime = lastTickTime;
        long lastRentalCheckTime = lastTickTime;
        final long overheadInterval = GAME_MONTH_MS;

        while (running) {
            try {
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - lastTickTime;
                lastTickTime = currentTime;

                long newGameTimeMs = gameTimeMs.addAndGet(elapsedMs);
                long daysElapsed = (long) Math.floor((double) elapsedMs / GAME_DAY_MS);
                if (daysElapsed > 0) {
                    LocalDateTime previousDateTime = gameDateTime;
                    gameDateTime = gameDateTime.plus(daysElapsed, ChronoUnit.DAYS);

                    if (gameDateTime.getMonthValue() != previousDateTime.getMonthValue()) {
                        processMonthlyKeepUp();
                        lastProcessedMonth = gameDateTime.getMonthValue();
                    }

                    notifyTimeListeners();
                }

                if (currentTime - lastPaymentCheckTime >= GAME_DAY_MS) {
                    requestManager.processPayments(newGameTimeMs);
                    lastPaymentCheckTime = currentTime;
                }

                if (currentTime - lastOverheadTime >= overheadInterval) {
                    lastOverheadTime = currentTime;
                }

                if (currentTime - lastRentalCheckTime >= GAME_DAY_MS) {
                    checkRentalExpirations(newGameTimeMs);
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

                    System.out.println("Checking request: " + request.getName() +
                            " | Time since last payment: " + timeSinceLastPaymentMs +
                            "ms | Payment interval: " + (period.getDays() * GAME_DAY_MS) + "ms");
                    // ตรวจสอบการชำระเงินตามรอบ
                    if (request.isPaymentDue(currentGameTimeMs)) {
                        double payment = request.getPaymentAmount();
                        company.addMoney(payment); // เพิ่มเงินให้บริษัท
                        request.recordPayment(currentGameTimeMs); // บันทึกเวลาชำระเงิน
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