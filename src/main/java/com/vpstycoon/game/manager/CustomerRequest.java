package com.vpstycoon.game.manager;

import com.vpstycoon.game.customer.Customer;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;

import java.util.Random;

public class CustomerRequest extends Customer {
    private final RequestType requestType;
    private final int duration; // Duration in days
    private final int requiredVCPUs;
    private final int requiredRamGB; // Changed to int for easier comparison
    private final int requiredDiskGB; // Changed to int for easier comparison
    private final RentalPeriodType rentalPeriodType; // New field for rental period type
    private final double monthlyPayment; // Monthly payment amount
    private boolean isActive = false; // Whether this request is currently active
    private boolean isExpired = false; // Whether this request has expired
    private long creationTime; // When this request was created
    private long lastPaymentTime; // When the last payment was made

    // Enum for rental period types
    public enum RentalPeriodType {
        DAILY(1, "Daily"),
        WEEKLY(7, "Weekly"),
        MONTHLY(30, "Monthly"),
        YEARLY(365, "Yearly");
        
        private final int days;
        private final String displayName;
        
        RentalPeriodType(int days, String displayName) {
            this.days = days;
            this.displayName = displayName;
        }
        
        public int getDays() {
            return days;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructor with explicit requirements
    public CustomerRequest(CustomerType customerType, RequestType requestType,
                           double budget, int duration, int requiredVCPUs,
                           int requiredRamGB, int requiredDiskGB, RentalPeriodType rentalPeriodType) {
        super(RandomGenerateName.generateRandomName(), customerType, budget);
        this.requestType = requestType;
        this.duration = duration;
        this.requiredVCPUs = requiredVCPUs;
        this.requiredRamGB = requiredRamGB;
        this.requiredDiskGB = requiredDiskGB;
        this.rentalPeriodType = rentalPeriodType;
        this.monthlyPayment = calculateMonthlyPayment();
        this.creationTime = System.currentTimeMillis();
        this.lastPaymentTime = 0;
    }

    // Constructor with random requirements
    public CustomerRequest(CustomerType customerType, RequestType requestType,
                           double budget, int duration) {
        super(RandomGenerateName.generateRandomName(), customerType, budget);
        this.requestType = requestType;
        this.duration = duration;

        // Generate random requirements based on customer type
        Random random = new Random();
        
        // Determine requirements based on customer type
        switch (customerType) {
            case INDIVIDUAL:
                this.requiredVCPUs = random.nextInt(2) + 1; // 1-2 vCPUs
                this.requiredRamGB = (random.nextInt(2) + 1) * 2; // 2-4 GB RAM
                this.requiredDiskGB = (random.nextInt(3) + 1) * 10; // 10-30 GB Disk
                break;
            case SMALL_BUSINESS:
                this.requiredVCPUs = random.nextInt(2) + 2; // 2-3 vCPUs
                this.requiredRamGB = (random.nextInt(2) + 2) * 2; // 4-6 GB RAM
                this.requiredDiskGB = (random.nextInt(3) + 3) * 10; // 30-50 GB Disk
                break;
            case MEDIUM_BUSINESS:
                this.requiredVCPUs = random.nextInt(2) + 3; // 3-4 vCPUs
                this.requiredRamGB = (random.nextInt(3) + 3) * 2; // 6-10 GB RAM
                this.requiredDiskGB = (random.nextInt(5) + 5) * 10; // 50-90 GB Disk
                break;
            case LARGE_BUSINESS:
                this.requiredVCPUs = random.nextInt(4) + 4; // 4-7 vCPUs
                this.requiredRamGB = (random.nextInt(4) + 5) * 2; // 10-16 GB RAM
                this.requiredDiskGB = (random.nextInt(6) + 10) * 10; // 100-150 GB Disk
                break;
            case ENTERPRISE:
            case BUSINESS:
                this.requiredVCPUs = random.nextInt(8) + 8; // 8-15 vCPUs
                this.requiredRamGB = (random.nextInt(8) + 8) * 2; // 16-30 GB RAM
                this.requiredDiskGB = (random.nextInt(10) + 15) * 10; // 150-240 GB Disk
                break;
            default:
                this.requiredVCPUs = random.nextInt(4) + 1;
                this.requiredRamGB = (random.nextInt(4) + 1) * 2;
                this.requiredDiskGB = (random.nextInt(5) + 1) * 10;
        }
        
        // Randomly select rental period type
        RentalPeriodType[] periodTypes = RentalPeriodType.values();
        this.rentalPeriodType = periodTypes[random.nextInt(periodTypes.length)];
        
        this.monthlyPayment = calculateMonthlyPayment();
        this.creationTime = System.currentTimeMillis();
        this.lastPaymentTime = 0;
    }

    // Calculate monthly payment based on requirements and customer type
    private double calculateMonthlyPayment() {
        // Base price calculation
        double basePrice = (requiredVCPUs * 500) + (requiredRamGB * 100) + (requiredDiskGB * 2);
        
        // Adjust price based on customer type (larger customers get volume discounts)
        switch (customerType) {
            case INDIVIDUAL:
                basePrice *= 1.0; // No discount
                break;
            case SMALL_BUSINESS:
                basePrice *= 0.95; // 5% discount
                break;
            case MEDIUM_BUSINESS:
                basePrice *= 0.9; // 10% discount
                break;
            case LARGE_BUSINESS:
                basePrice *= 0.85; // 15% discount
                break;
            case ENTERPRISE:
            case BUSINESS:
                basePrice *= 0.8; // 20% discount
                break;
        }
        
        // Adjust price based on rental period (longer periods get discounts)
        switch (rentalPeriodType) {
            case DAILY:
                basePrice *= 1.2; // 20% premium for daily rentals
                break;
            case WEEKLY:
                basePrice *= 1.1; // 10% premium for weekly rentals
                break;
            case MONTHLY:
                basePrice *= 1.0; // Standard price for monthly
                break;
            case YEARLY:
                basePrice *= 0.8; // 20% discount for yearly commitment
                break;
        }
        
        return basePrice;
    }

    // Calculate payment amount based on rental period
    public double getPaymentAmount() {
        switch (rentalPeriodType) {
            case DAILY:
                return monthlyPayment / 30.0; // Daily rate
            case WEEKLY:
                return monthlyPayment / 4.0; // Weekly rate
            case MONTHLY:
                return monthlyPayment; // Monthly rate
            case YEARLY:
                return monthlyPayment * 12; // Yearly rate
            default:
                return monthlyPayment;
        }
    }

    // Check if payment is due based on game time
    public boolean isPaymentDue(long currentGameTime) {
        if (!isActive || lastPaymentTime == 0) {
            return false;
        }
        
        long timeSinceLastPayment = currentGameTime - lastPaymentTime;
        long paymentInterval;
        
        // Convert real time to game time (1 month = 15 minutes)
        switch (rentalPeriodType) {
            case DAILY:
                paymentInterval = 15 * 60 * 1000 / 30; // 30 seconds in real time
                break;
            case WEEKLY:
                paymentInterval = 15 * 60 * 1000 / 4; // 3.75 minutes in real time
                break;
            case MONTHLY:
                paymentInterval = 15 * 60 * 1000; // 15 minutes in real time
                break;
            case YEARLY:
                paymentInterval = 15 * 60 * 1000 * 12; // 3 hours in real time
                break;
            default:
                paymentInterval = 15 * 60 * 1000;
        }
        
        return timeSinceLastPayment >= paymentInterval;
    }

    // Record a payment
    public void recordPayment(long currentTime) {
        this.lastPaymentTime = currentTime;
    }

    // Activate the request
    public void activate() {
        this.isActive = true;
        this.lastPaymentTime = System.currentTimeMillis();
    }

    // Deactivate the request
    public void deactivate() {
        this.isActive = false;
    }

    // Mark the request as expired
    public void markAsExpired() {
        this.isActive = false;
        this.isExpired = true;
    }

    // Check if the request is expired
    public boolean isExpired() {
        return isExpired;
    }

    // Getters
    public CustomerType getCustomerType() {
        return customerType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public double getBudget() {
        return budget;
    }

    public int getDuration() {
        return duration;
    }

    public int getRequiredVCPUs() {
        return requiredVCPUs;
    }

    public int getRequiredRamGB() {
        return requiredRamGB;
    }

    public String getRequiredRam() {
        return requiredRamGB + " GB";
    }

    public int getRequiredDiskGB() {
        return requiredDiskGB;
    }

    public String getRequiredDisk() {
        return requiredDiskGB + " GB";
    }

    public RentalPeriodType getRentalPeriodType() {
        return rentalPeriodType;
    }

    /**
     * Get the rental period in days
     * @return The rental period in days
     */
    public int getRentalPeriod() {
        return rentalPeriodType.getDays();
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public boolean isActive() {
        return isActive;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastPaymentTime() {
        return lastPaymentTime;
    }

    public String getTitle() {
        return getName() + " - " + requestType.toString() + " (" + rentalPeriodType.getDisplayName() + ")";
    }
}