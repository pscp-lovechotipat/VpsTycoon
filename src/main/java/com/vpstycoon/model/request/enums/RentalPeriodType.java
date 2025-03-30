package com.vpstycoon.model.request.enums;

/**
 * ประเภทของระยะเวลาเช่า VPS
 */
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

    /**
     * สร้าง RentalPeriodType ใหม่ด้วยค่าที่กำหนด
     */
    RentalPeriodType(String displayName, int days, double priceMultiplier) {
        this.displayName = displayName;
        this.days = days;
        this.priceMultiplier = priceMultiplier;
    }

    /**
     * ดึงชื่อที่แสดงของระยะเวลาเช่า
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * ดึงจำนวนวันของระยะเวลาเช่า
     */
    public int getDays() {
        return days;
    }

    /**
     * ดึงตัวคูณราคาของระยะเวลาเช่า
     */
    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    /**
     * คำนวณราคาสุทธิตามระยะเวลาเช่า
     */
    public double calculatePrice(double basePrice) {
        return basePrice * priceMultiplier;
    }
} 