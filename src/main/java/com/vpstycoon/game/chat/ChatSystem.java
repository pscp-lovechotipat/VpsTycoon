package com.vpstycoon.game.chat;

import com.vpstycoon.game.customer.enums.CustomerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatSystem {
    private int chatLevel;
    private List<CustomerType> unlockedCustomerTypes;
    private Map<CustomerType, Integer> customerTypePoints;
    
    public ChatSystem() {
        this.chatLevel = 1;
        this.unlockedCustomerTypes = new ArrayList<>();
        this.customerTypePoints = new HashMap<>();
        initializeCustomerTypes();
    }
    
    private void initializeCustomerTypes() {
        unlockedCustomerTypes.add(CustomerType.INDIVIDUAL); 
        
        
        customerTypePoints.put(CustomerType.INDIVIDUAL, 10);
        customerTypePoints.put(CustomerType.SMALL_BUSINESS, 20);
        customerTypePoints.put(CustomerType.MEDIUM_BUSINESS, 30);
        customerTypePoints.put(CustomerType.LARGE_BUSINESS, 50);
    }
    
    public void unlockCustomerType(CustomerType type, int marketingPoints) {
        if (marketingPoints >= type.getRequiredPoints() && !unlockedCustomerTypes.contains(type)) {
            unlockedCustomerTypes.add(type);
        }
    }

    public int getChatLevel() {
        return chatLevel;
    }

    public void upgradeChatLevel() {
        this.chatLevel++;
    }

    public List<CustomerType> getUnlockedCustomerTypes() {
        return new ArrayList<>(unlockedCustomerTypes);
    }

    public int getPointsForCustomerType(CustomerType type) {
        return customerTypePoints.getOrDefault(type, 0);
    }
} 
