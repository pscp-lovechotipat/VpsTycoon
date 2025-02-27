package com.vpstycoon.game.manager;

import com.sun.net.httpserver.Request;
import com.vpstycoon.game.customer.Customer;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;

public class CustomerRequest extends Customer {
    private final RequestType requestType;
    private final int duration;
    
    public CustomerRequest(CustomerType customerType, RequestType requestType, 
                         double budget, int duration) {
        super(RandomGenerateName.generateRandomName(10), customerType, budget);
        this.requestType = requestType;
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