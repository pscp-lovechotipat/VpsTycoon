package com.vpstycoon.manager;

import java.util.ArrayList;
import java.util.List;
import com.vpstycoon.vps.enums.RequestType;

public class RequestManager {
    private List<CustomerRequest> activeRequests;
    
    public RequestManager() {
        this.activeRequests = new ArrayList<>();
    }
    
    public void addRequest(CustomerRequest request) {
        activeRequests.add(request);
    }
    
    public List<CustomerRequest> getActiveRequests() {
        return new ArrayList<>(activeRequests);
    }
} 