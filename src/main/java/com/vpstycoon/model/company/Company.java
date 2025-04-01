package com.vpstycoon.model.company;

import com.vpstycoon.model.company.interfaces.ICompany;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Company implements Serializable, ICompany {
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

    @Override
    public int getSkillPointsAvailable() {
        return skillPointsAvailable;
    }

    @Override
    public void setSkillPointsAvailable(int skillPointsAvailable) {
        this.skillPointsAvailable = skillPointsAvailable;
    }

    @Override
    public void addSkillPoints(int points) {
        if (points > 0) {
            this.skillPointsAvailable += points;
        }
    }

    @Override
    public void recordCompletedRequest(int satisfactionChange) {
        completedRequests++;
        
        customerSatisfaction += satisfactionChange;
        if (customerSatisfaction < 0) customerSatisfaction = 0;
        if (customerSatisfaction > 100) customerSatisfaction = 100;
        
        updateRating();
    }
    
    @Override
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
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getRating() {
        return rating;
    }

    @Override
    public void setRating(double rating) {
        double oldRating = this.rating;
        
        this.rating = Math.max(0, Math.min(5, rating));
        
        if (oldRating != this.rating) {
            notifyRatingObservers();
        }
    }

    @Override
    public int getMarketingPoints() {
        return marketingPoints;
    }

    @Override
    public void setMarketingPoints(int marketingPoints) {
        this.marketingPoints = marketingPoints;
    }

    @Override
    public long getMoney() {
        return money;
    }

    @Override
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

    @Override
    public void addMoney(long amount) {
        if (amount > 0) {
            setMoney(this.money + amount);
            this.totalRevenue += amount;
        }
    }

    @Override
    public boolean spendMoney(long amount) {
        if (amount > 0) {
            if (this.money >= amount) {
                setMoney(this.money - amount);
                this.totalExpenses += amount;
                return true;
            }
            return false;
        }
        return true;
    }

    
    private void pushMoneyNotification(long amount, long newBalance, boolean isIncome) {
        
    }

    @Override
    public void addRatingObserver(RatingObserver observer) {
        if (observer != null && !ratingObservers.contains(observer)) {
            ratingObservers.add(observer);
        }
    }

    @Override
    public void removeRatingObserver(RatingObserver observer) {
        ratingObservers.remove(observer);
    }

    @Override
    public void addMoneyObserver(MoneyTransactionObserver observer) {
        if (observer != null && !moneyObservers.contains(observer)) {
            moneyObservers.add(observer);
        }
    }

    @Override
    public void removeMoneyObserver(MoneyTransactionObserver observer) {
        moneyObservers.remove(observer);
    }

    
    private void notifyRatingObservers() {
        for (RatingObserver observer : new ArrayList<>(ratingObservers)) {
            observer.onRatingChanged(this.rating);
        }
    }
    
    
    private void notifyMoneyObservers(long amount, long newBalance, boolean isIncome) {
        for (MoneyTransactionObserver observer : new ArrayList<>(moneyObservers)) {
            observer.onMoneyTransaction(amount, newBalance, isIncome);
        }
    }
    
    
    @Serial
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
            System.err.println("เกิดข้อผิดพลาดในการเริ่มต้นผู้ติดตาม: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int getCustomerSatisfaction() {
        return customerSatisfaction;
    }
    
    @Override
    public void setCustomerSatisfaction(int customerSatisfaction) {
        this.customerSatisfaction = Math.max(0, Math.min(100, customerSatisfaction));
        updateRating();
    }
    
    @Override
    public int getCompletedRequests() {
        return completedRequests;
    }
    
    @Override
    public int getFailedRequests() {
        return failedRequests;
    }
    
    @Override
    public int getStarRating() {
        return (int) Math.ceil(rating);
    }

    @Override
    public int getAvailableVMs() {
        return availableVMs;
    }

    @Override
    public void setAvailableVMs(int availableVMs) {
        this.availableVMs = availableVMs;
    }
    
    @Override
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
