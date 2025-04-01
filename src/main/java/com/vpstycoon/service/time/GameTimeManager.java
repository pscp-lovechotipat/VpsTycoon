package com.vpstycoon.service.time;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSProduct;
import com.vpstycoon.model.time.GameTimeModel;
import com.vpstycoon.model.time.interfaces.IGameTime;
import com.vpstycoon.service.time.interfaces.IGameTimeManager;
import com.vpstycoon.ui.game.rack.Rack;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class GameTimeManager implements IGameTimeManager {
    public static final long GAME_DAY_MS = GameTimeModel.DEFAULT_GAME_DAY_MS;
    public static final long GAME_WEEK_MS = GAME_DAY_MS * 7;
    public static final long GAME_MONTH_MS = GAME_DAY_MS * 30;
    public static final long GAME_YEAR_MS = GAME_MONTH_MS * 12;
    public static final long TICK_INTERVAL_MS = 1000;
    private static final long OVERHEAD_INTERVAL = GAME_MONTH_MS;

    private final Company company;
    private RequestManager requestManager;
    private final Rack rack;
    private final IGameTime gameTimeModel;
    private volatile boolean running = true;
    private int lastProcessedMonth = -1;

    private final List<GameTimeListener> timeListeners = new ArrayList<>();

    
    public GameTimeManager(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.rack = rack;
        this.gameTimeModel = new GameTimeModel(startTime != null ? startTime : LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        this.lastProcessedMonth = gameTimeModel.getCurrentDateTime().getMonthValue();
    }

    
    @Override
    public void start() {
        running = true;
        
        long lastTickTime = System.currentTimeMillis();
        long lastPaymentCheckTime = lastTickTime;
        long lastOverheadTime = lastTickTime;
        long lastRentalCheckTime = lastTickTime;

        System.out.println("Thread TimeManager Initializing... running=" + running);

        int tickCounter = 0;
        
        try {
            while (running) {
                try {
                    long currentTime = System.currentTimeMillis();
                    long elapsedMs = currentTime - lastTickTime;
                    lastTickTime = currentTime;

                    
                    gameTimeModel.addRealTimeMs(elapsedMs);
                    
                    
                    LocalDateTime gameDateTime = gameTimeModel.getCurrentDateTime();
                    if (gameDateTime.getMonthValue() != lastProcessedMonth) {
                        processMonthlyKeepUp();
                        lastProcessedMonth = gameDateTime.getMonthValue();
                    }

                    
                    notifyTimeListeners();

                    
                    if (currentTime - lastPaymentCheckTime >= GAME_DAY_MS) {
                        if (requestManager != null) {
                            requestManager.processPayments(gameTimeModel.getGameTimeMs());
                        } else {
                            System.err.println("Warning: requestManager is null, cannot process payments");
                            if (company != null) {
                                try {
                                    ResourceManager resourceManager = ResourceManager.getInstance();
                                    RequestManager rm = resourceManager.getRequestManager();
                                    
                                    if (rm != null) {
                                        System.out.println("พบ RequestManager จาก ResourceManager, นำมาใช้งาน");
                                        requestManager = rm;
                                    } else {
                                        requestManager = new RequestManager(company);
                                        resourceManager.setRequestManager(requestManager);
                                        System.out.println("สร้าง RequestManager ใหม่สำเร็จใน GameTimeManager และตั้งค่าให้ ResourceManager");
                                    }
                                } catch (Exception e) {
                                    System.err.println("ไม่สามารถสร้าง RequestManager ใน GameTimeManager: " + e.getMessage());
                                }
                            }
                        }
                        lastPaymentCheckTime = currentTime;
                    }

                    
                    if (currentTime - lastOverheadTime >= OVERHEAD_INTERVAL) {
                        lastOverheadTime = currentTime;
                    }

                    
                    if (currentTime - lastRentalCheckTime >= GAME_DAY_MS) {
                        checkRentalExpirations(gameTimeModel.getGameTimeMs());
                        lastRentalCheckTime = currentTime;
                    }

                    
                    tickCounter++;
                    if (tickCounter % 10 == 0) {
                        System.out.println("GameTime Update: " + gameDateTime + 
                                          " (GameTimeMs: " + gameTimeModel.getGameTimeMs() + ")");
                    }

                    
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Thread TimeManager ถูก interrupt - กำลังหยุด");
                        running = false;
                        break;
                    }

                    Thread.sleep(TICK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    running = false;
                    System.out.println("Thread TimeManager Interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            System.out.println("Thread TimeManager Stopped");
            running = false;
        }
    }

    
    private void checkRentalExpirations(long currentGameTimeMs) {
        if (requestManager == null) {
            System.err.println("Warning: requestManager is null in checkRentalExpirations");
            return;
        }
        
        for (CustomerRequest request : requestManager.getRequests()) {
            if (request.isActive()) {
                long rentalStartTime = request.getLastPaymentTime();
                CustomerRequest.RentalPeriodType period = request.getRentalPeriodType();
                if (period != null && rentalStartTime > 0) {
                    long durationMs = period.getDays() * GAME_DAY_MS;

                    if (request.isPaymentDue(currentGameTimeMs)) {
                        double payment = request.getPaymentAmount();
                        company.addMoney(payment);
                        request.recordPayment(currentGameTimeMs);
                        System.out.println("Payment received from " + request.getName() + ": $" + payment +
                                " | Game time: " + currentGameTimeMs);
                    }

                    if (currentGameTimeMs >= rentalStartTime + durationMs) {
                        request.markAsExpired();

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
            company.spendMoney(totalKeepUpCost);
            System.out.println("Monthly keep-up cost deducted: $" + totalKeepUpCost +
                    " | New balance: $" + company.getMoney());
        }
    }

    
    private void notifyTimeListeners() {
        LocalDateTime currentTime = gameTimeModel.getCurrentDateTime();
        long gameTimeMs = gameTimeModel.getGameTimeMs();
        
        for (GameTimeListener listener : timeListeners) {
            listener.onTimeChanged(currentTime, gameTimeMs);
        }
    }

    
    @Override
    public void addVPSServer(VPSOptimization vps) {
        rack.installVPS(vps);
    }

    
    @Override
    public void removeVPSServer(VPSOptimization vps) {
        rack.uninstallVPS(vps);
    }

    
    @Override
    public void addTimeListener(GameTimeListener listener) {
        timeListeners.add(listener);
    }

    
    @Override
    public void removeTimeListener(GameTimeListener listener) {
        timeListeners.remove(listener);
    }

    
    @Override
    public LocalDateTime getGameDateTime() {
        return gameTimeModel.getCurrentDateTime();
    }

    
    @Override
    public long getGameTimeMs() {
        return gameTimeModel.getGameTimeMs();
    }

    
    @Override
    public void stop() {
        running = false;
    }

    
    @Override
    public void resetTime(LocalDateTime newStartDateTime) {
        stop();
        gameTimeModel.setCurrentDateTime(newStartDateTime);
        gameTimeModel.resetTime();
        lastProcessedMonth = newStartDateTime.getMonthValue();
        System.out.println("GameTimeManager: Reset time to " + newStartDateTime);
    }

    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    
    public IGameTime getGameTimeModel() {
        return gameTimeModel;
    }
} 
