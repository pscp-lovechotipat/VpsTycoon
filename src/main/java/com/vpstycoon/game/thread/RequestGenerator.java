package com.vpstycoon.game.thread;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RandomGenerateName;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.enums.RequestType;

import java.util.Random;

public class RequestGenerator extends Thread {
    private final RequestManager requestManager;
    private volatile boolean running = true;
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
     * Set the maximum number of pending requests
     * @param maxRequests Maximum number of pending requests
     */
    public void setMaxPendingRequests(int maxRequests) {
        this.maxPendingRequests = maxRequests;
    }
}
