package com.vpstycoon.game.manager;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;

public class CustomerRequest {
    private final CustomerType customerType;
    private final RequestType requestType;
    private final double budget;
    private final int duration;
    
    public CustomerRequest(CustomerType customerType, RequestType requestType, 
                         double budget, int duration) {
        this.customerType = customerType;
        this.requestType = requestType;
        this.budget = budget;
        this.duration = duration;
    }
    
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
} 