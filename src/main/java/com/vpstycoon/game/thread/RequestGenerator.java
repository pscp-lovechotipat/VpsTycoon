package com.vpstycoon.game.thread;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;

import java.util.Random;

public class RequestGenerator extends Thread {
    private final RequestManager requestManager;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final int minDelayMs = 30_000; // Minimum delay between requests (30 seconds)
    private final int maxDelayMs = 90_000; // Maximum delay between requests (90 seconds)
    private final int rateLimitSleepTime = 10_000; // Sleep time when rate limit is reached
    private int maxPendingRequests = 10; // Maximum number of pending requests - no longer final
    
    // Company rating affects request generation rate
    private double requestRateMultiplier = 1.0;

    public RequestGenerator(RequestManager requestManager) {
        this.requestManager = requestManager;
        this.setDaemon(true);
        this.setName("RequestGenerator");
    }

    @Override
    public void run() {
        Random random = new Random();

        while (!interrupted() && running) {
            try {
                // Check if generator is paused
                if (paused) {
                    synchronized (this) {
                        while (paused) {
                            System.out.println("RequestGenerator paused, waiting to resume...");
                            wait(); // Wait until notify() is called
                        }
                    }
                }
                
                // Check if we've reached the maximum number of pending requests
                if (requestManager.getRequests().size() >= maxPendingRequests) {
                    System.out.println("RequestGenerator: request limit reached (" + maxPendingRequests + ")");
                    Thread.sleep(rateLimitSleepTime);
                    continue;
                }

                // Calculate delay based on company rating
                updateRequestRateMultiplier();
                int adjustedMinDelay = (int)(minDelayMs / requestRateMultiplier);
                int adjustedMaxDelay = (int)(maxDelayMs / requestRateMultiplier);
                int delay = adjustedMinDelay + random.nextInt(adjustedMaxDelay - adjustedMinDelay);
                
                // Sleep for the calculated delay
                Thread.sleep(delay);

                // Generate a new random request
                CustomerRequest newRequest = requestManager.generateRandomRequest();
                
                // Add the request to the manager
                requestManager.addRequest(newRequest);
                
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
     * Update the request rate multiplier based on company rating
     * Higher rating = more requests
     */
    private void updateRequestRateMultiplier() {
        double companyRating = 1.0;
        try {
            companyRating = requestManager.getVmProvisioningManager().getCompany().getRating();
        } catch (Exception e) {
            // If we can't get the rating, use default
            companyRating = 1.0;
        }
        
        // Rating affects request rate: 1.0 rating = 1x, 5.0 rating = 3x
        requestRateMultiplier = 1.0 + (companyRating - 1.0) * 0.5;
        
        // Clamp to reasonable range
        requestRateMultiplier = Math.max(0.5, Math.min(3.0, requestRateMultiplier));
    }

    /**
     * Stop the request generator
     */
    public void stopGenerator() {
        running = false;
        this.interrupt();
    }
    
    /**
     * รีเซ็ต RequestGenerator ให้กลับเป็นค่าเริ่มต้น
     */
    public void resetGenerator() {
        // หยุดการทำงานก่อน
        running = false;
        this.interrupt();
        
        try {
            // รีเซ็ตค่าต่างๆ กลับเป็นค่าเริ่มต้น
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
     * Pause the request generator
     */
    public synchronized void pauseGenerator() {
        System.out.println("RequestGenerator is pausing");
        paused = true;
    }
    
    /**
     * Resume the request generator
     */
    public synchronized void resumeGenerator() {
        if (paused) {
            System.out.println("RequestGenerator is resuming");
            paused = false;
            
            try {
                notify(); // Wake up the waiting thread
                System.out.println("RequestGenerator notify() called successfully");
            } catch (Exception e) {
                System.err.println("Error in resumeGenerator: " + e.getMessage());
                e.printStackTrace();
                
                // เพิ่มการตรวจสอบ Thread state ถ้าเกิดข้อผิดพลาด
                if (!isAlive()) {
                    System.err.println("RequestGenerator thread is not alive, attempting to restart");
                    try {
                        // ถ้า thread ไม่ทำงานแล้ว ให้ลองสร้าง thread ใหม่และเริ่มทำงานใหม่
                        // ทำได้เฉพาะกรณีที่ไม่เคยเริ่ม start อีกทางคือต้องสร้าง RequestGenerator ใหม่
                        this.start();
                    } catch (IllegalThreadStateException ex) {
                        System.err.println("Cannot restart thread: " + ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Check if the generator is paused
     * @return True if the generator is paused
     */
    public boolean isPaused() {
        return paused;
    }
    
    /**
     * Set the maximum number of pending requests
     * @param maxRequests Maximum number of pending requests
     */
    public void setMaxPendingRequests(int maxRequests) {
        this.maxPendingRequests = maxRequests;
    }
}
