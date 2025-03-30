package com.vpstycoon.service.time;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.service.time.interfaces.IRequestGenerator;

import java.util.Random;

/**
 * สร้างคำขอจากลูกค้าโดยอัตโนมัติ ทำงานในรูปแบบ background thread
 */
public class RequestGenerator extends Thread implements IRequestGenerator {
    private final RequestManager requestManager;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final int minDelayMs = 30_000; 
    private final int maxDelayMs = 90_000; 
    private final int rateLimitSleepTime = 10_000; 
    private int maxPendingRequests = 10; 
    private double requestRateMultiplier = 1.0;

    /**
     * สร้าง RequestGenerator ใหม่ที่ใช้ RequestManager ที่กำหนด
     */
    public RequestGenerator(RequestManager requestManager) {
        this.requestManager = requestManager;
        this.setDaemon(true);
        this.setName("RequestGenerator");
    }

    /**
     * เริ่มการทำงานของตัวสร้างคำขอ
     */
    @Override
    public void run() {
        Random random = new Random();

        while (!interrupted() && running) {
            try {
                if (paused) {
                    synchronized (this) {
                        while (paused) {
                            System.out.println("RequestGenerator paused, waiting to resume...");
                            wait(); 
                        }
                    }
                }
                
                if (requestManager.getRequests().size() >= maxPendingRequests) {
                    System.out.println("RequestGenerator: request limit reached (" + maxPendingRequests + ")");
                    Thread.sleep(rateLimitSleepTime);
                    continue;
                }

                updateRequestRateMultiplier();
                int adjustedMinDelay = (int)(minDelayMs / requestRateMultiplier);
                int adjustedMaxDelay = (int)(maxDelayMs / requestRateMultiplier);
                int delay = adjustedMinDelay + random.nextInt(adjustedMaxDelay - adjustedMinDelay);
                
                Thread.sleep(delay);

                CustomerRequest newRequest = requestManager.generateRandomRequest();
                requestManager.addRequest(newRequest);
                
                // บันทึกข้อมูลเกมเมื่อมีคำขอใหม่
                try {
                    if (ResourceManager.getInstance().getCurrentState() != null) {
                        ResourceManager.getInstance().saveGameState(ResourceManager.getInstance().getCurrentState());
                        System.out.println("บันทึกข้อมูลเกมหลังจากได้รับคำขอใหม่");
                    } else {
                        System.out.println("ไม่พบ GameState สำหรับบันทึกข้อมูลหลังจากได้รับคำขอใหม่");
                    }
                } catch (Exception e) {
                    System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูลเกมหลังจากได้รับคำขอใหม่: " + e.getMessage());
                }
                
                System.out.println("New Customer Request: " + newRequest.getName() +
                        " | Type: " + newRequest.getCustomerType() +
                        " | Request: " + newRequest.getRequestType() +
                        " | Period: " + newRequest.getRentalPeriodType().getDisplayName() +
                        " | Payment: " + String.format("%.2f", newRequest.getPaymentAmount()) +
                        " | Delay(ms): " + delay);

            } catch (InterruptedException e) {
                System.out.println("RequestGenerator interrupted, stopping...");
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * ปรับค่า multiplier สำหรับอัตราการสร้างคำขอ ตามคะแนนของบริษัท
     */
    private void updateRequestRateMultiplier() {
        double companyRating = 1.0;
        try {
            companyRating = requestManager.getVmProvisioningManager().getCompany().getRating();
        } catch (Exception e) {
            companyRating = 1.0;
        }
        
        requestRateMultiplier = 1.0 + (companyRating - 1.0) * 0.5;
        requestRateMultiplier = Math.max(0.5, Math.min(3.0, requestRateMultiplier));
    }

    /**
     * หยุดการทำงานของตัวสร้างคำขอ
     */
    @Override
    public void stopGenerator() {
        running = false;
        this.interrupt();
    }
    
    /**
     * รีเซ็ตตัวสร้างคำขอกลับเป็นค่าเริ่มต้น
     */
    @Override
    public void resetGenerator() {
        running = false;
        this.interrupt();
        
        try {
            this.paused = false;
            this.maxPendingRequests = 10;
            this.requestRateMultiplier = 1.0;
            
            System.out.println("RequestGenerator ถูกรีเซ็ตกลับเป็นค่าเริ่มต้นแล้ว");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ต RequestGenerator: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * หยุดการทำงานชั่วคราว
     */
    @Override
    public synchronized void pauseGenerator() {
        System.out.println("RequestGenerator is pausing");
        paused = true;
    }
    
    /**
     * เริ่มการทำงานต่อหลังจากหยุดชั่วคราว
     */
    @Override
    public synchronized void resumeGenerator() {
        if (paused) {
            System.out.println("RequestGenerator is resuming");
            paused = false;
            
            try {
                notify(); 
                System.out.println("RequestGenerator notify() called successfully");
            } catch (Exception e) {
                System.err.println("Error in resumeGenerator: " + e.getMessage());
                e.printStackTrace();
                
                if (!isAlive()) {
                    System.err.println("RequestGenerator thread is not alive, attempting to restart");
                    try {
                        this.start();
                    } catch (IllegalThreadStateException ex) {
                        System.err.println("Cannot restart thread: " + ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * ตรวจสอบว่ากำลังหยุดชั่วคราวอยู่หรือไม่
     */
    @Override
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * กำหนดจำนวนคำขอสูงสุดที่รอตอบรับได้
     */
    @Override
    public void setMaxPendingRequests(int maxRequests) {
        this.maxPendingRequests = maxRequests;
    }
} 