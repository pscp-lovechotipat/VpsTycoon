package com.vpstycoon.game.company;

import java.io.Serializable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Company implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private double rating;
    private int marketingPoints;
    private int skillPointsAvailable;
    private int availableVMs;

    private long money;
    private long totalRevenue;
    private long totalExpenses;
    private int customerSatisfaction;
    private int completedRequests;
    private int failedRequests;
    
    // ใช้ functional interface เพื่อแจ้งเตือนเมื่อค่า rating เปลี่ยน
    public interface RatingObserver {
        void onRatingChanged(double newRating);
    }
    
    // ใช้ functional interface เพื่อแจ้งเตือนเมื่อเงินเข้าหรือออก
    public interface MoneyTransactionObserver {
        void onMoneyTransaction(long amount, long newBalance, boolean isIncome);
    }
    
    private final transient List<RatingObserver> ratingObservers = new ArrayList<>();
    private final transient List<MoneyTransactionObserver> moneyObservers = new ArrayList<>();

    public Company() {
        this.name = "New Company";
        this.rating = 1.0;
        this.marketingPoints = 0;
        this.skillPointsAvailable = 1000;
        this.money = 100_000; // Starting money: 100,000 THB
        this.totalRevenue = 0;
        this.totalExpenses = 0;
        this.customerSatisfaction = 50; // 0-100 scale
        this.completedRequests = 0;
        this.failedRequests = 0;
        this.availableVMs = 0;
    }

    public int getSkillPointsAvailable() {
        return skillPointsAvailable;
    }

    public void setSkillPointsAvailable(int skillPointsAvailable) {
        this.skillPointsAvailable = skillPointsAvailable;
    }

    public void addSkillPoints(int points) {
        if (points > 0) {
            this.skillPointsAvailable += points;
        }
    }

    /**
     * Record a completed request
     * @param satisfactionChange Change in customer satisfaction (-100 to 100)
     */
    public void recordCompletedRequest(int satisfactionChange) {
        completedRequests++;
        
        // Update customer satisfaction (0-100 scale)
        customerSatisfaction += satisfactionChange;
        if (customerSatisfaction < 0) customerSatisfaction = 0;
        if (customerSatisfaction > 100) customerSatisfaction = 100;
        
        // Update rating based on satisfaction and completion ratio
        updateRating();
    }
    
    /**
     * Record a failed request
     */
    public void recordFailedRequest() {
        failedRequests++;
        
        // Failed requests reduce satisfaction
        customerSatisfaction -= 5;
        if (customerSatisfaction < 0) customerSatisfaction = 0;
        
        // Update rating
        updateRating();
    }
    
    /**
     * Update the company rating based on various factors
     */
    private void updateRating() {
        // Base rating from customer satisfaction (0-3 points)
        double satisfactionRating = customerSatisfaction / 33.33; // 0-3 scale
        
        // Completion ratio rating (0-1 points)
        double completionRatio = 0;
        if (completedRequests + failedRequests > 0) {
            completionRatio = (double) completedRequests / (completedRequests + failedRequests);
        }
        
        // Financial health rating (0-1 points)
        double financialRating = 0;
        if (totalRevenue > 0) {
            double profitRatio = (double) (totalRevenue - totalExpenses) / totalRevenue;
            financialRating = Math.min(1.0, Math.max(0, profitRatio));
        }
        
        // เก็บค่า rating เดิมไว้เพื่อตรวจสอบการเปลี่ยนแปลง
        double oldRating = this.rating;
        
        // Calculate final rating (0-5 scale)
        rating = satisfactionRating + completionRatio + financialRating;
        
        // Ensure rating is within bounds
        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;
        
        // ถ้าค่า rating เปลี่ยน ให้แจ้งเตือน observers
        if (oldRating != this.rating) {
            notifyRatingObservers();
        }
    }

    // Getters and setters
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        double oldRating = this.rating;
        // Allow external rating changes (e.g., from VM provisioning)
        this.rating = Math.max(0, Math.min(5, rating));
        
        // ถ้าค่า rating เปลี่ยน ให้แจ้งเตือน observers
        if (oldRating != this.rating) {
            notifyRatingObservers();
        }
    }

    public int getMarketingPoints() {
        return marketingPoints;
    }

    public void setMarketingPoints(int marketingPoints) {
        this.marketingPoints = marketingPoints;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        long oldMoney = this.money;
        this.money = money;
        
        // คำนวณส่วนต่างและแจ้งเตือน observers
        long difference = this.money - oldMoney;
        if (difference != 0) {
            boolean isIncome = difference > 0;
            notifyMoneyObservers(difference, this.money, isIncome);
            
            // บันทึกรายได้หรือค่าใช้จ่าย
            if (isIncome) {
                this.totalRevenue += difference;
            } else {
                this.totalExpenses += Math.abs(difference);
            }
            
            // แสดงการแจ้งเตือนบนหน้าจอ
            pushMoneyNotification(difference, this.money, isIncome);
        }
    }
    
    /**
     * Push notification for money transactions to UI
     * @param amount Amount changed
     * @param newBalance New balance
     * @param isIncome True if income, false if expense
     */
    private void pushMoneyNotification(long amount, long newBalance, boolean isIncome) {
        if (amount == 0) return;
        
        // Import ResourceManager to use pushNotification
        com.vpstycoon.game.resource.ResourceManager resourceManager = com.vpstycoon.game.resource.ResourceManager.getInstance();
        
        String title = isIncome ? "รายได้เข้า" : "ค่าใช้จ่าย";
        String amountStr = String.format("%,d", Math.abs(amount));
        String balanceStr = String.format("%,d", newBalance);
        String content = isIncome 
            ? "ได้รับเงิน ฿" + amountStr + "\nยอดคงเหลือ: ฿" + balanceStr
            : "จ่ายเงิน ฿" + amountStr + "\nยอดคงเหลือ: ฿" + balanceStr;
        
        resourceManager.pushNotification(title, content);
    }
    
    /**
     * Add money to the company's balance
     * @param amount Amount to add
     */
    public void addMoney(double amount) {
        long amountLong = (long) amount;
        this.money += amountLong;
        this.totalRevenue += amountLong;
        
        // แจ้งเตือน observers ว่ามีเงินเข้า
        notifyMoneyObservers(amountLong, this.money, true);
        
        // แสดงการแจ้งเตือนบนหน้าจอ
        pushMoneyNotification(amountLong, this.money, true);
    }
    
    /**
     * Spend money from the company's balance
     * @param amount Amount to spend
     * @return true if successful, false if insufficient funds
     */
    public boolean spendMoney(double amount) {
        long amountLong = (long) amount;
        if (this.money < amountLong) {
            return false;
        }
        
        this.money -= amountLong;
        this.totalExpenses += amountLong;
        
        // แจ้งเตือน observers ว่ามีการใช้จ่ายเงิน
        notifyMoneyObservers(-amountLong, this.money, false);
        
        // แสดงการแจ้งเตือนบนหน้าจอ
        pushMoneyNotification(-amountLong, this.money, false);
        
        return true;
    }
    
    public long getTotalRevenue() {
        return totalRevenue;
    }
    
    public long getTotalExpenses() {
        return totalExpenses;
    }
    
    public long getProfit() {
        return totalRevenue - totalExpenses;
    }
    
    public int getCustomerSatisfaction() {
        return customerSatisfaction;
    }
    
    public void setCustomerSatisfaction(int customerSatisfaction) {
        this.customerSatisfaction = Math.max(0, Math.min(100, customerSatisfaction));
        updateRating();
    }
    
    public int getCompletedRequests() {
        return completedRequests;
    }
    
    public int getFailedRequests() {
        return failedRequests;
    }
    
    /**
     * Get the company's star rating (1-5 stars)
     * @return Star rating (1-5)
     */
    public int getStarRating() {
        return (int) Math.ceil(rating);
    }

    public int getAvailableVMs() {
        return availableVMs;
    }

    public void setAvailableVMs(int availableVMs) {
        this.availableVMs = availableVMs;
    }
    
    /**
     * Calculate rating change when assigning VM to a customer based on spec matching
     * 
     * @param requestedCpu CPU cores requested by customer
     * @param requestedRam RAM GB requested by customer
     * @param requestedStorage Storage GB requested by customer
     * @param providedCpu CPU cores provided
     * @param providedRam RAM GB provided
     * @param providedStorage Storage GB provided
     * @return Rating change value (positive or negative)
     */
    public double calculateVMAssignmentRatingChange(
            int requestedCpu, int requestedRam, int requestedStorage,
            int providedCpu, int providedRam, int providedStorage) {
        
        double ratingChange = 0.0;
        
        // CPU comparison
        ratingChange += calculateSpecRatingImpact(requestedCpu, providedCpu, 1);
        
        // RAM comparison
        int[] ramTiers = {1, 2, 4, 8, 16, 32, 64};
        ratingChange += calculateSpecTierImpact(requestedRam, providedRam, ramTiers);
        
        // Storage comparison
        int[] storageTiers = {10, 20, 50, 100, 250, 500, 1000};
        ratingChange += calculateSpecTierImpact(requestedStorage, providedStorage, storageTiers);
        
        return ratingChange;
    }
    
    /**
     * Calculate spec impact for direct numerical comparison (like CPU cores)
     * 
     * @param requested Requested value
     * @param provided Provided value
     * @param impactPerUnit Impact per unit difference (typically 0.1)
     * @return Rating impact
     */
    private double calculateSpecRatingImpact(int requested, int provided, double impactPerUnit) {
        int difference = provided - requested;
        
        if (difference < 0) {
            // Provided less than requested - negative impact
            return difference * impactPerUnit; // Will return negative value
        } else if (difference > 0) {
            // Provided more than requested - positive impact
            return Math.min(0.3, difference * (impactPerUnit * 0.5)); // Positive but limited boost
        }
        
        // Exact match
        return 0.1; // Small bonus for exact match
    }
    
    /**
     * Calculate spec impact for tiered resources (RAM, Storage)
     * 
     * @param requested Requested value
     * @param provided Provided value
     * @param tiers Available tiers for this resource
     * @return Rating impact
     */
    private double calculateSpecTierImpact(int requested, int provided, int[] tiers) {
        // Find tier indexes
        int requestedTierIndex = findTierIndex(requested, tiers);
        int providedTierIndex = findTierIndex(provided, tiers);
        
        // Calculate tier difference
        int tierDifference = providedTierIndex - requestedTierIndex;
        
        if (tierDifference < 0) {
            // Under-provisioned resource
            return tierDifference * 0.1; // 0.1 penalty per tier below
        } else if (tierDifference > 0) {
            // Over-provisioned resource (diminishing returns)
            return Math.min(0.2, tierDifference * 0.05); // Max 0.2 bonus for overprovisioning
        }
        
        // Exact match
        return 0.1; // Small bonus for exact match
    }
    
    /**
     * Find the index of a value in a tier array (or closest lower tier)
     */
    private int findTierIndex(int value, int[] tiers) {
        // If below lowest tier, return -1 (severe penalty)
        if (value < tiers[0]) {
            return -1;
        }
        
        // Find exact match or next lower tier
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (value >= tiers[i]) {
                return i;
            }
        }
        
        return 0; // Fallback to lowest tier
    }
    
    /**
     * เพิ่ม observer เพื่อรับการแจ้งเตือนเมื่อค่า rating เปลี่ยนแปลง
     * @param observer Observer ที่ต้องการลงทะเบียน
     */
    public void addRatingObserver(RatingObserver observer) {
        if (observer != null && !ratingObservers.contains(observer)) {
            ratingObservers.add(observer);
        }
    }
    
    /**
     * ลบ observer ออกจากรายการ
     * @param observer Observer ที่ต้องการเพิกถอน
     */
    public void removeRatingObserver(RatingObserver observer) {
        ratingObservers.remove(observer);
    }
    
    /**
     * แจ้งเตือน observers ทั้งหมดเมื่อค่า rating เปลี่ยนแปลง
     */
    private void notifyRatingObservers() {
        for (RatingObserver observer : ratingObservers) {
            observer.onRatingChanged(this.rating);
        }
    }
    
    /**
     * เพิ่ม observer เพื่อรับการแจ้งเตือนเมื่อมีการเปลี่ยนแปลงเงิน
     * @param observer Observer ที่ต้องการลงทะเบียน
     */
    public void addMoneyObserver(MoneyTransactionObserver observer) {
        if (observer != null && !moneyObservers.contains(observer)) {
            moneyObservers.add(observer);
        }
    }
    
    /**
     * ลบ observer ออกจากรายการ
     * @param observer Observer ที่ต้องการเพิกถอน
     */
    public void removeMoneyObserver(MoneyTransactionObserver observer) {
        moneyObservers.remove(observer);
    }
    
    /**
     * แจ้งเตือน observers ทั้งหมดเมื่อมีการเปลี่ยนแปลงเงิน
     * @param amount จำนวนเงินที่เปลี่ยนแปลง
     * @param newBalance ยอดเงินใหม่
     * @param isIncome true หากเป็นรายได้, false หากเป็นค่าใช้จ่าย
     */
    private void notifyMoneyObservers(long amount, long newBalance, boolean isIncome) {
        for (MoneyTransactionObserver observer : moneyObservers) {
            observer.onMoneyTransaction(amount, newBalance, isIncome);
        }
    }
    
    /**
     * Reinitializes transient fields during deserialization
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        java.lang.reflect.Field field;
        try {
            field = Company.class.getDeclaredField("ratingObservers");
            field.setAccessible(true);
            field.set(this, new ArrayList<>());
            
            field = Company.class.getDeclaredField("moneyObservers");
            field.setAccessible(true);
            field.set(this, new ArrayList<>());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error reinitializing observers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reduce the company rating by specified amount
     * @param amount Amount to reduce rating by
     */
    public void reduceRating(double amount) {
        if (amount > 0) {
            double oldRating = this.rating;
            this.rating = Math.max(0, this.rating - amount);
            
            // ถ้าค่า rating เปลี่ยน ให้แจ้งเตือน observers
            if (oldRating != this.rating) {
                notifyRatingObservers();
            }
        }
    }
} 