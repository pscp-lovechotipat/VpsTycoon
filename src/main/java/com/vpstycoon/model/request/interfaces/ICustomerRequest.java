package com.vpstycoon.model.request.interfaces;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;
import com.vpstycoon.model.request.enums.RentalPeriodType;

import java.io.Serializable;
import java.time.LocalDateTime;


public interface ICustomerRequest extends Serializable {
    
    
    String getId();
    
    
    String getCustomerName();
    
    
    void setCustomerName(String customerName);
    
    
    boolean isHandled();
    
    
    void setHandled(boolean handled);
    
    
    boolean isCompleted();
    
    
    void setCompleted(boolean completed);
    
    
    String getDescription();
    
    
    void setDescription(String description);
    
    
    LocalDateTime getCreatedAt();
    
    
    void setCreatedAt(LocalDateTime createdAt);
    
    
    LocalDateTime getHandledAt();
    
    
    void setHandledAt(LocalDateTime handledAt);
    
    
    long getReward();
    
    
    void setReward(long reward);
    
    
    String getTitle();
    
    
    void setTitle(String title);
    
    
    RequestType getRequestType();
    
    
    void setRequestType(RequestType requestType);
    
    
    int calculateSatisfactionImpact();
    
    
    String getName();
    
    
    CustomerType getCustomerType();
    
    
    RentalPeriodType getRentalPeriodType();
    
    
    double getPaymentAmount();
    
    
    long getLastPaymentTime();
    
    
    void recordPayment(long paymentTime);
    
    
    boolean isActive();
    
    
    boolean isExpired();
    
    
    boolean isAccepted();
    
    
    void accept();
    
    
    void reject();
    
    
    void markAsExpired();
    
    
    boolean isPaymentDue(long currentTime);
    
    
    void setPaymentAmount(double amount);
} 
