package com.vpstycoon.model.request.enums;


public enum RentalPeriodType {
    DAILY("Daily", 1, 1.0),
    WEEKLY("Weekly", 7, 6.5),
    MONTHLY("Monthly", 30, 25.0),
    QUARTERLY("Quarterly", 90, 70.0),
    HALF_YEARLY("Half-Year", 180, 130.0),
    YEARLY("Yearly", 365, 240.0);

    private final String displayName;
    private final int days;
    private final double priceMultiplier;

    
    RentalPeriodType(String displayName, int days, double priceMultiplier) {
        this.displayName = displayName;
        this.days = days;
        this.priceMultiplier = priceMultiplier;
    }

    
    public String getDisplayName() {
        return displayName;
    }

    
    public int getDays() {
        return days;
    }

    
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    
    public double calculatePrice(double basePrice) {
        return basePrice * priceMultiplier;
    }
} 
