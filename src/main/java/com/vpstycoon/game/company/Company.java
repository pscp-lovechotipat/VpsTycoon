package com.vpstycoon.game.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Company implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private double rating;
    private int marketingPoints;
    private long money;
    private long totalRevenue;
    private long totalExpenses;
    private int customerSatisfaction;
    private int completedRequests;
    private int failedRequests;
    
    // Financial history for tracking
    private final List<FinancialRecord> financialHistory;
    
    // Inner class for financial records
    public static class FinancialRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final long timestamp;
        private final long revenue;
        private final long expenses;
        private final long profit;
        private final String description;
        
        public FinancialRecord(long timestamp, long revenue, long expenses, String description) {
            this.timestamp = timestamp;
            this.revenue = revenue;
            this.expenses = expenses;
            this.profit = revenue - expenses;
            this.description = description;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public long getRevenue() {
            return revenue;
        }
        
        public long getExpenses() {
            return expenses;
        }
        
        public long getProfit() {
            return profit;
        }
        
        public String getDescription() {
            return description;
        }
    }

    public Company() {
        this.name = "New Company";
        this.rating = 1.0;
        this.marketingPoints = 0;
        this.money = 100_000; // Starting money: 100,000 THB
        this.totalRevenue = 0;
        this.totalExpenses = 0;
        this.customerSatisfaction = 50; // 0-100 scale
        this.completedRequests = 0;
        this.failedRequests = 0;
        this.financialHistory = new ArrayList<>();
    }
    
    /**
     * Add a financial record to the history
     * @param revenue Revenue amount
     * @param expenses Expense amount
     * @param description Description of the transaction
     */
    public void addFinancialRecord(long revenue, long expenses, String description) {
        FinancialRecord record = new FinancialRecord(
                System.currentTimeMillis(),
                revenue,
                expenses,
                description
        );
        
        financialHistory.add(record);
        
        // Update totals
        totalRevenue += revenue;
        totalExpenses += expenses;
    }
    
    /**
     * Add revenue to the company
     * @param amount Amount to add
     * @param description Description of the revenue
     */
    public void addRevenue(long amount, String description) {
        money += amount;
        addFinancialRecord(amount, 0, description);
    }
    
    /**
     * Add expense to the company
     * @param amount Amount to deduct
     * @param description Description of the expense
     */
    public void addExpense(long amount, String description) {
        money -= amount;
        addFinancialRecord(0, amount, description);
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
        
        // Calculate final rating (0-5 scale)
        rating = satisfactionRating + completionRatio + financialRating;
        
        // Ensure rating is within bounds
        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;
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
        // Allow external rating changes (e.g., from VM provisioning)
        this.rating = Math.max(0, Math.min(5, rating));
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
        this.money = money;
    }
    
    /**
     * Add money to the company's balance
     * @param amount Amount to add (can be negative to subtract)
     */
    public void addMoney(double amount) {
        this.money += (long) amount;
        
        // Record as revenue if positive, expense if negative
        if (amount > 0) {
            addFinancialRecord((long) amount, 0, "VM Rental Payment");
        } else if (amount < 0) {
            addFinancialRecord(0, (long) Math.abs(amount), "Expense");
        }
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
    
    public List<FinancialRecord> getFinancialHistory() {
        return new ArrayList<>(financialHistory);
    }
    
    /**
     * Get the company's star rating (1-5 stars)
     * @return Star rating (1-5)
     */
    public int getStarRating() {
        return (int) Math.ceil(rating);
    }
} 