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

/**
 * จัดการเวลาในเกม รวมทั้งการคำนวณเวลาและการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
 */
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

    /**
     * สร้าง GameTimeManager ใหม่ด้วยข้อมูลเริ่มต้น
     */
    public GameTimeManager(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.company = company;
        this.requestManager = requestManager;
        this.rack = rack;
        this.gameTimeModel = new GameTimeModel(startTime != null ? startTime : LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        this.lastProcessedMonth = gameTimeModel.getCurrentDateTime().getMonthValue();
    }

    /**
     * เริ่มการทำงานของระบบเวลา ใช้สำหรับรันเป็น thread
     */
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

                    // เพิ่มเวลาที่ผ่านไปให้กับโมเดลเวลา
                    gameTimeModel.addRealTimeMs(elapsedMs);
                    
                    // ตรวจสอบว่าต้องทำงานเดือนใหม่หรือไม่
                    LocalDateTime gameDateTime = gameTimeModel.getCurrentDateTime();
                    if (gameDateTime.getMonthValue() != lastProcessedMonth) {
                        processMonthlyKeepUp();
                        lastProcessedMonth = gameDateTime.getMonthValue();
                    }

                    // แจ้งเตือน listeners
                    notifyTimeListeners();

                    // ตรวจสอบการจ่ายเงิน
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

                    // ตรวจสอบค่าใช้จ่าย overhead
                    if (currentTime - lastOverheadTime >= OVERHEAD_INTERVAL) {
                        lastOverheadTime = currentTime;
                    }

                    // ตรวจสอบการหมดอายุของการเช่า VPS
                    if (currentTime - lastRentalCheckTime >= GAME_DAY_MS) {
                        checkRentalExpirations(gameTimeModel.getGameTimeMs());
                        lastRentalCheckTime = currentTime;
                    }

                    // แสดง debug ข้อมูล
                    tickCounter++;
                    if (tickCounter % 10 == 0) {
                        System.out.println("GameTime Update: " + gameDateTime + 
                                          " (GameTimeMs: " + gameTimeModel.getGameTimeMs() + ")");
                    }

                    // ตรวจสอบการ interrupt
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

    /**
     * ตรวจสอบการหมดอายุของการเช่า VPS
     */
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

    /**
     * คำนวณและหักค่าใช้จ่ายประจำเดือน
     */
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

    /**
     * แจ้งเตือน listeners ทุกตัวเมื่อเวลาเปลี่ยนแปลง
     */
    private void notifyTimeListeners() {
        LocalDateTime currentTime = gameTimeModel.getCurrentDateTime();
        long gameTimeMs = gameTimeModel.getGameTimeMs();
        
        for (GameTimeListener listener : timeListeners) {
            listener.onTimeChanged(currentTime, gameTimeMs);
        }
    }

    /**
     * เพิ่ม VPS เข้าไปในระบบ
     */
    @Override
    public void addVPSServer(VPSOptimization vps) {
        rack.installVPS(vps);
    }

    /**
     * ลบ VPS ออกจากระบบ
     */
    @Override
    public void removeVPSServer(VPSOptimization vps) {
        rack.uninstallVPS(vps);
    }

    /**
     * เพิ่ม listener สำหรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    @Override
    public void addTimeListener(GameTimeListener listener) {
        timeListeners.add(listener);
    }

    /**
     * ลบ listener สำหรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    @Override
    public void removeTimeListener(GameTimeListener listener) {
        timeListeners.remove(listener);
    }

    /**
     * ดึงค่าเวลาปัจจุบันในเกม
     */
    @Override
    public LocalDateTime getGameDateTime() {
        return gameTimeModel.getCurrentDateTime();
    }

    /**
     * ดึงค่าเวลาในเกมในรูปแบบมิลลิวินาที
     */
    @Override
    public long getGameTimeMs() {
        return gameTimeModel.getGameTimeMs();
    }

    /**
     * หยุดการทำงานของระบบเวลา
     */
    @Override
    public void stop() {
        running = false;
    }

    /**
     * รีเซ็ตเวลาเกมเป็นค่าที่กำหนด
     */
    @Override
    public void resetTime(LocalDateTime newStartDateTime) {
        stop();
        gameTimeModel.setCurrentDateTime(newStartDateTime);
        gameTimeModel.resetTime();
        lastProcessedMonth = newStartDateTime.getMonthValue();
        System.out.println("GameTimeManager: Reset time to " + newStartDateTime);
    }

    /**
     * ตรวจสอบว่าระบบเวลากำลังทำงานอยู่หรือไม่
     */
    @Override
    public boolean isRunning() {
        return running;
    }
    
    /**
     * ดึงโมเดลเวลาที่ใช้งานอยู่
     */
    public IGameTime getGameTimeModel() {
        return gameTimeModel;
    }
} 