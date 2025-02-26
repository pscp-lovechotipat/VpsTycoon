package com.vpstycoon.game.company;

public class Company {
    private String name;
    private double rating;
    private int marketingPoints;
    private long money;

    public Company() {
        this.name = "New Company";
        this.rating = 1.0;
        this.marketingPoints = 0;
        this.money = 10000; // เริ่มต้นด้วยเงิน 10,000
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
        this.rating = rating;
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
} 