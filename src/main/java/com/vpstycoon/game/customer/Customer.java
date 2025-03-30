package com.vpstycoon.game.customer;

import com.vpstycoon.game.customer.enums.CustomerType;

import java.io.Serializable;

public class Customer implements Serializable {
    private static int nextId = 1; 
    private final int id;          
    private String name;
    protected CustomerType customerType; 
    protected double budget;

    public Customer(String name, CustomerType customerType, double budget) {
        this.id = nextId++;        
        this.name = name;
        this.customerType = customerType;
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public double getBudget() {
        return budget;
    }
}
