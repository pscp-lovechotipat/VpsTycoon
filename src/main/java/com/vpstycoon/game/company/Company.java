package com.vpstycoon.game.company;

import java.io.IOException;
import java.io.Serializable;
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
    
    
    public interface RatingObserver {
        void onRatingChanged(double newRating);
    }
    
    
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
        this.money = 100_000; 
        this.totalRevenue = 0;
        this.totalExpenses = 0;
        this.customerSatisfaction = 50; 
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

    
    public void recordCompletedRequest(int satisfactionChange) {
        completedRequests++;
        
        
        customerSatisfaction += satisfactionChange;
        if (customerSatisfaction < 0) customerSatisfaction = 0;
        if (customerSatisfaction > 100) customerSatisfaction = 100;
        
        
        updateRating();
    }
    
    
    public void recordFailedRequest() {
        failedRequests++;
        
        
        customerSatisfaction -= 5;
        if (customerSatisfaction < 0) customerSatisfaction = 0;
        
        
        updateRating();
    }
    
    
    private void updateRating() {
        
        double satisfactionRating = customerSatisfaction / 33.33; 
        
        
        double completionRatio = 0;
        if (completedRequests + failedRequests > 0) {
            completionRatio = (double) completedRequests / (completedRequests + failedRequests);
        }
        
        
        double financialRating = 0;
        if (totalRevenue > 0) {
            double profitRatio = (double) (totalRevenue - totalExpenses) / totalRevenue;
            financialRating = Math.min(1.0, Math.max(0, profitRatio));
        }
        
        
        double oldRating = this.rating;
        
        
        rating = satisfactionRating + completionRatio + financialRating;
        
        
        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;
        
        
        if (oldRating != this.rating) {
            notifyRatingObservers();
        }
    }

    
    
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
        
        this.rating = Math.max(0, Math.min(5, rating));
        
        
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
        
        
        long difference = this.money - oldMoney;
        if (difference != 0) {
            boolean isIncome = difference > 0;
            notifyMoneyObservers(difference, this.money, isIncome);
            
            
            if (isIncome) {
                this.totalRevenue += difference;
            } else {
                this.totalExpenses += Math.abs(difference);
            }
            
            
            pushMoneyNotification(difference, this.money, isIncome);
        }
    }
    
    
    private void pushMoneyNotification(long amount, long newBalance, boolean isIncome) {
        if (amount == 0) return;
        
        
        com.vpstycoon.game.resource.ResourceManager resourceManager = com.vpstycoon.game.resource.ResourceManager.getInstance();
        
        String title = isIncome ? "รายได้เข้า" : "ค่าใช้จ่าย";
        String amountStr = String.format("%,d", Math.abs(amount));
        String balanceStr = String.format("%,d", newBalance);
        String content = isIncome 
            ? "ได้รับเงิน ฿" + amountStr + "\nยอดคงเหลือ: ฿" + balanceStr
            : "จ่ายเงิน ฿" + amountStr + "\nยอดคงเหลือ: ฿" + balanceStr;
        
        resourceManager.pushNotification(title, content);
    }
    
    
    public void addMoney(double amount) {
        long amountLong = (long) amount;
        this.money += amountLong;
        this.totalRevenue += amountLong;
        
        
        notifyMoneyObservers(amountLong, this.money, true);
        
        
        pushMoneyNotification(amountLong, this.money, true);
    }
    
    
    public boolean spendMoney(double amount) {
        long amountLong = (long) amount;
        if (this.money < amountLong) {
            return false;
        }
        
        this.money -= amountLong;
        this.totalExpenses += amountLong;
        
        
        notifyMoneyObservers(-amountLong, this.money, false);
        
        
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
    
    
    public int getStarRating() {
        return (int) Math.ceil(rating);
    }

    public int getAvailableVMs() {
        return availableVMs;
    }

    public void setAvailableVMs(int availableVMs) {
        this.availableVMs = availableVMs;
    }
    
    
    public double calculateVMAssignmentRatingChange(
            int requestedCpu, int requestedRam, int requestedStorage,
            int providedCpu, int providedRam, int providedStorage) {
        
        double ratingChange = 0.0;
        
        
        ratingChange += calculateSpecRatingImpact(requestedCpu, providedCpu, 1);
        
        
        int[] ramTiers = {1, 2, 4, 8, 16, 32, 64};
        ratingChange += calculateSpecTierImpact(requestedRam, providedRam, ramTiers);
        
        
        int[] storageTiers = {10, 20, 50, 100, 250, 500, 1000};
        ratingChange += calculateSpecTierImpact(requestedStorage, providedStorage, storageTiers);
        
        return ratingChange;
    }
    
    
    private double calculateSpecRatingImpact(int requested, int provided, double impactPerUnit) {
        int difference = provided - requested;
        
        if (difference < 0) {
            
            return difference * impactPerUnit; 
        } else if (difference > 0) {
            
            return Math.min(0.3, difference * (impactPerUnit * 0.5)); 
        }
        
        
        return 0.1; 
    }
    
    
    private double calculateSpecTierImpact(int requested, int provided, int[] tiers) {
        
        int requestedTierIndex = findTierIndex(requested, tiers);
        int providedTierIndex = findTierIndex(provided, tiers);
        
        
        int tierDifference = providedTierIndex - requestedTierIndex;
        
        if (tierDifference < 0) {
            
            return tierDifference * 0.1; 
        } else if (tierDifference > 0) {
            
            return Math.min(0.2, tierDifference * 0.05); 
        }
        
        
        return 0.1; 
    }
    
    
    private int findTierIndex(int value, int[] tiers) {
        
        if (value < tiers[0]) {
            return -1;
        }
        
        
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (value >= tiers[i]) {
                return i;
            }
        }
        
        return 0; 
    }
    
    
    public void addRatingObserver(RatingObserver observer) {
        if (observer != null && !ratingObservers.contains(observer)) {
            ratingObservers.add(observer);
        }
    }
    
    
    public void removeRatingObserver(RatingObserver observer) {
        ratingObservers.remove(observer);
    }
    
    
    private void notifyRatingObservers() {
        for (RatingObserver observer : ratingObservers) {
            observer.onRatingChanged(this.rating);
        }
    }
    
    
    public void addMoneyObserver(MoneyTransactionObserver observer) {
        if (observer != null && !moneyObservers.contains(observer)) {
            moneyObservers.add(observer);
        }
    }
    
    
    public void removeMoneyObserver(MoneyTransactionObserver observer) {
        moneyObservers.remove(observer);
    }
    
    
    private void notifyMoneyObservers(long amount, long newBalance, boolean isIncome) {
        for (MoneyTransactionObserver observer : moneyObservers) {
            observer.onMoneyTransaction(amount, newBalance, isIncome);
        }
    }
    
    
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

    
    public void reduceRating(double amount) {
        if (amount > 0) {
            double oldRating = this.rating;
            this.rating = Math.max(0, this.rating - amount);
            
            
            if (oldRating != this.rating) {
                notifyRatingObservers();
            }
        }
    }
} 
