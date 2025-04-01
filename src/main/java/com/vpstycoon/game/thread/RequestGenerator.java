package com.vpstycoon.game.thread;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;

import java.util.Random;

public class RequestGenerator extends Thread {
    private final RequestManager requestManager;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final int minDelayMs = 30_000;
    private final int maxDelayMs = 120_000;
    private final int rateLimitSleepTime = 5_000;
    private int maxPendingRequests = 20;
    
    
    private double requestRateMultiplier = 1.0;

    public RequestGenerator(RequestManager requestManager) {
        this.requestManager = requestManager;
        this.setDaemon(true);
        this.setName("RequestGenerator");
    }

    @Override
    public void run() {
        Random random = new Random();
        System.out.println("===== RequestGenerator เริ่มทำงาน (Thread ID: " + this.getId() + ") =====");
        long startTime = System.currentTimeMillis();
        int generatedRequestCount = 0;

        while (!interrupted() && running) {
            try {
                
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > 10000) {
                    System.out.println("RequestGenerator ทำงานมา " + ((currentTime - startTime) / 1000) + " วินาที, สร้าง " + generatedRequestCount + " requests");
                    startTime = currentTime;
                }
                
                
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
                generatedRequestCount++;
                
                
                try {
                    com.vpstycoon.game.resource.ResourceManager.getInstance().pushNotification(
                        "คำขอใหม่จาก " + newRequest.getName(),
                        "ต้องการ VM: " + newRequest.getRequiredVCPUs() + " vCPUs, " +
                        newRequest.getRequiredRam() + " RAM, " +
                        newRequest.getRequiredDisk() + " Disk"
                    );
                } catch (Exception e) {
                    System.err.println("ไม่สามารถแสดง notification สำหรับคำขอใหม่: " + e.getMessage());
                }
                
                
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

                System.out.println("[RequestGenerator] สร้าง Request ใหม่: " + newRequest.getName() +
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

    
    public void stopGenerator() {
        running = false;
        this.interrupt();
    }
    
    
    public void resetGenerator() {
        
        running = false;
        this.interrupt();
        
        try {
            
            this.paused = false;
            this.maxPendingRequests = 20;
            this.requestRateMultiplier = 1.0;
            
            System.out.println("RequestGenerator ถูกรีเซ็ตกลับเป็นค่าเริ่มต้นแล้ว");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ต RequestGenerator: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    public synchronized void pauseGenerator() {
        System.out.println("RequestGenerator is pausing");
        paused = true;
    }
    
    
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
    
    
    public boolean isPaused() {
        return paused;
    }
    
    
    public void setMaxPendingRequests(int maxRequests) {
        this.maxPendingRequests = maxRequests;
    }
}


