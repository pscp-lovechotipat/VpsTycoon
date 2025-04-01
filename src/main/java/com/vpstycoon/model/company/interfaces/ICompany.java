package com.vpstycoon.model.company.interfaces;


public interface ICompany {
    
    
    interface RatingObserver {
        
        void onRatingChanged(double newRating);
    }
    
    
    interface MoneyTransactionObserver {
        
        void onMoneyTransaction(long amount, long newBalance, boolean isIncome);
    }
    
    
    String getName();
    
    
    void setName(String name);
    
    
    double getRating();
    
    
    void setRating(double rating);
    
    
    int getMarketingPoints();
    
    
    void setMarketingPoints(int marketingPoints);
    
    
    long getMoney();
    
    
    void setMoney(long money);
    
    
    void addMoney(long amount);
    
    
    boolean spendMoney(long amount);
    
    
    int getCustomerSatisfaction();
    
    
    void setCustomerSatisfaction(int customerSatisfaction);
    
    
    int getCompletedRequests();
    
    
    int getFailedRequests();
    
    
    void recordCompletedRequest(int satisfactionChange);
    
    
    void recordFailedRequest();
    
    
    int getStarRating();
    
    
    int getAvailableVMs();
    
    
    void setAvailableVMs(int availableVMs);
    
    
    int getSkillPointsAvailable();
    
    
    void setSkillPointsAvailable(int skillPointsAvailable);
    
    
    void addSkillPoints(int points);
    
    
    void reduceRating(double amount);
    
    
    void addRatingObserver(RatingObserver observer);
    
    
    void removeRatingObserver(RatingObserver observer);
    
    
    void addMoneyObserver(MoneyTransactionObserver observer);
    
    
    void removeMoneyObserver(MoneyTransactionObserver observer);
} 
