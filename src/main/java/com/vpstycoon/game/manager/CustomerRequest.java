package com.vpstycoon.game.manager;

import com.vpstycoon.game.customer.Customer;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.vps.enums.RequestType;

import java.io.Serializable;
import java.util.Random;

public class CustomerRequest extends Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private final RequestType requestType;
    private final int duration;
    private final int requiredVCPUs;
    private final int requiredRamGB;
    private final int requiredDiskGB;
    private RentalPeriodType rentalPeriodType;
    private final double monthlyPayment;
    private boolean isActive = false;
    private boolean isExpired = false;
    private long creationTime;
    private long lastPaymentTime;
    
    // เพิ่ม field เพื่อเก็บข้อมูลว่า request นี้ถูก assign ให้กับ VM ใด
    private String assignedToVmId;

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

    public CustomerRequest(CustomerType customerType, RequestType requestType,
                           double budget, int duration) {
        super(RandomGenerateName.generateRandomName(), customerType, budget);
        this.requestType = requestType;
        this.duration = duration;

        Random random = new Random();
        switch (customerType) {
            case INDIVIDUAL:
                this.requiredVCPUs = random.nextInt(2) + 1;
                this.requiredRamGB = (random.nextInt(2) + 1) * 2;
                this.requiredDiskGB = (random.nextInt(3) + 1) * 10;
                break;
            case SMALL_BUSINESS:
                this.requiredVCPUs = random.nextInt(2) + 2;
                this.requiredRamGB = (random.nextInt(2) + 2) * 2;
                this.requiredDiskGB = (random.nextInt(3) + 3) * 10;
                break;
            case MEDIUM_BUSINESS:
                this.requiredVCPUs = random.nextInt(2) + 3;
                this.requiredRamGB = (random.nextInt(3) + 3) * 2;
                this.requiredDiskGB = (random.nextInt(5) + 5) * 10;
                break;
            case LARGE_BUSINESS:
                this.requiredVCPUs = random.nextInt(4) + 4;
                this.requiredRamGB = (random.nextInt(4) + 5) * 2;
                this.requiredDiskGB = (random.nextInt(6) + 10) * 10;
                break;
            case ENTERPRISE:
            case BUSINESS:
                this.requiredVCPUs = random.nextInt(8) + 8;
                this.requiredRamGB = (random.nextInt(8) + 8) * 2;
                this.requiredDiskGB = (random.nextInt(10) + 15) * 10;
                break;
            default:
                this.requiredVCPUs = random.nextInt(4) + 1;
                this.requiredRamGB = (random.nextInt(4) + 1) * 2;
                this.requiredDiskGB = (random.nextInt(5) + 1) * 10;
        }

        RentalPeriodType[] periodTypes = RentalPeriodType.values();
        this.rentalPeriodType = periodTypes[random.nextInt(periodTypes.length)];

        this.monthlyPayment = calculateMonthlyPayment();
        this.creationTime = System.currentTimeMillis();
        this.lastPaymentTime = 0;
    }

    private double calculateMonthlyPayment() {
        double basePrice = (requiredVCPUs * 500) + (requiredRamGB * 100) + (requiredDiskGB * 2);

        switch (customerType) {
            case INDIVIDUAL:
                basePrice *= 1.0;
                break;
            case SMALL_BUSINESS:
                basePrice *= 0.95;
                break;
            case MEDIUM_BUSINESS:
                basePrice *= 0.9;
                break;
            case LARGE_BUSINESS:
                basePrice *= 0.85;
                break;
            case ENTERPRISE:
            case BUSINESS:
                basePrice *= 0.8;
                break;
        }

        switch (rentalPeriodType) {
            case DAILY:
                basePrice *= 1.2;
                break;
            case WEEKLY:
                basePrice *= 1.1;
                break;
            case MONTHLY:
                basePrice *= 1.0;
                break;
            case YEARLY:
                basePrice *= 0.8;
                break;
        }

        return basePrice;
    }

    public double getPaymentAmount() {
        switch (rentalPeriodType) {
            case DAILY:
                return monthlyPayment / 30.0;
            case WEEKLY:
                return monthlyPayment / 4.0;
            case MONTHLY:
                return monthlyPayment;
            case YEARLY:
                return monthlyPayment * 12;
            default:
                return monthlyPayment;
        }
    }

    public boolean isPaymentDue(long currentGameTimeMs) {
        if (!isActive || lastPaymentTime == 0 || currentGameTimeMs < lastPaymentTime) {
            return false;
        }

        long timeSinceLastPaymentMs = currentGameTimeMs - lastPaymentTime;
        long paymentIntervalMs;

        // ใช้ game time จาก GameTimeManager
        switch (rentalPeriodType) {
            case DAILY:
                paymentIntervalMs = GameTimeManager.GAME_DAY_MS;
                break;
            case WEEKLY:
                paymentIntervalMs = GameTimeManager.GAME_WEEK_MS;
                break;
            case MONTHLY:
                paymentIntervalMs = GameTimeManager.GAME_MONTH_MS;
                break;
            case YEARLY:
                paymentIntervalMs = GameTimeManager.GAME_YEAR_MS;
                break;
            default:
                paymentIntervalMs = GameTimeManager.GAME_MONTH_MS;
        }

        System.out.println("isPaymentDue for " + getName() +
                " | Time since last: " + timeSinceLastPaymentMs +
                "ms | Interval: " + paymentIntervalMs + "ms | Due: " + (timeSinceLastPaymentMs >= paymentIntervalMs));

        return timeSinceLastPaymentMs >= paymentIntervalMs;
    }

    public void recordPayment(long currentGameTimeMs) {
        this.lastPaymentTime = currentGameTimeMs;
    }

    public void activate(long currentGameTimeMs) {
        this.isActive = true;
        this.lastPaymentTime = currentGameTimeMs;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void markAsExpired() {
        this.isActive = false;
        this.isExpired = true;
    }

    public boolean isExpired() {
        return isExpired;
    }

    // Setter for rentalPeriodType
    public void setRentalPeriodType(RentalPeriodType rentalPeriodType) {
        this.rentalPeriodType = rentalPeriodType;
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

    public long getRentalStartTimeMs() {
        return lastPaymentTime;
    }

    /**
     * เพิ่มเมธอด equals เพื่อเปรียบเทียบ CustomerRequest
     * โดยเปรียบเทียบจาก ID และชื่อ
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CustomerRequest other = (CustomerRequest) obj;
        
        // เปรียบเทียบจาก ID และชื่อเป็นหลัก
        if (this.getId() == other.getId() && this.getName().equals(other.getName())) {
            return true;
        }
        
        // ถ้า ID ไม่ตรงกัน ลองเปรียบเทียบจากคุณสมบัติอื่น
        return this.getName().equals(other.getName()) &&
               this.customerType == other.customerType &&
               this.requestType == other.requestType &&
               this.requiredVCPUs == other.requiredVCPUs &&
               this.requiredRamGB == other.requiredRamGB &&
               this.requiredDiskGB == other.requiredDiskGB;
    }
    
    /**
     * เพิ่มเมธอด hashCode เพื่อใช้เป็น key ใน Map
     */
    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + customerType.hashCode();
        result = 31 * result + requestType.hashCode();
        result = 31 * result + requiredVCPUs;
        result = 31 * result + requiredRamGB;
        result = 31 * result + requiredDiskGB;
        return result;
    }

    /**
     * ตรวจสอบว่า request นี้ถูก assign ให้ VM แล้วหรือไม่
     * @return true ถ้าถูก assign แล้ว, false ถ้ายังไม่ถูก assign
     */
    public boolean isAssignedToVM() {
        return assignedToVmId != null && !assignedToVmId.isEmpty();
    }
    
    /**
     * เมธอดสำหรับตั้งค่า VM ที่ request นี้ถูก assign ให้
     * @param vmId ID ของ VM ที่ถูก assign
     */
    public void assignToVM(String vmId) {
        this.assignedToVmId = vmId;
        if (vmId != null && !vmId.isEmpty() && !isActive) {
            // ถ้ามีการ assign VM ให้ request นี้ ให้ตั้งค่า isActive เป็น true
            activate(System.currentTimeMillis());
        }
    }
    
    /**
     * เมธอดสำหรับยกเลิกการ assign VM
     */
    public void unassignFromVM() {
        this.assignedToVmId = null;
    }
    
    /**
     * ดึง ID ของ VM ที่ request นี้ถูก assign ให้
     * @return ID ของ VM หรือ null ถ้าไม่ได้ถูก assign
     */
    public String getAssignedVmId() {
        return assignedToVmId;
    }
}