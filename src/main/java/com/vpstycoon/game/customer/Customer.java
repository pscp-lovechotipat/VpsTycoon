package com.vpstycoon.game.customer;

import com.vpstycoon.game.customer.enums.CustomerType;

public class Customer {
    private int id = 0;
    private String name;
    protected CustomerType customerType;
    protected double budget;

    public Customer(String name, CustomerType customerType, double budget) {
        this.id++;
        this.name = name;
        this.customerType = customerType;
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
