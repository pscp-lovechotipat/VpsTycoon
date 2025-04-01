package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.manager.CustomerRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatHistorySaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    private Map<CustomerRequest, List<ChatMessage>> chatHistory;
    
    
    private ArrayList<CustomerRequest> pendingRequests;
    
    
    private ArrayList<CustomerRequest> completedRequests;
    
    
    private Map<String, String> vmAssignments;
    
    
    public ChatHistorySaveData() {
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
        this.vmAssignments = new HashMap<>();
    }
    
    
    public void setChatHistory(Map<CustomerRequest, List<ChatMessage>> chatHistory) {
        this.chatHistory = chatHistory;
    }
    
    
    public Map<CustomerRequest, List<ChatMessage>> getChatHistory() {
        return chatHistory;
    }
    
    
    public void setPendingRequests(List<CustomerRequest> pendingRequests) {
        
        this.pendingRequests = new ArrayList<>(pendingRequests);
    }
    
    
    public ArrayList<CustomerRequest> getPendingRequests() {
        return pendingRequests;
    }
    
    
    public void setCompletedRequests(List<CustomerRequest> completedRequests) {
        
        this.completedRequests = new ArrayList<>(completedRequests);
    }
    
    
    public ArrayList<CustomerRequest> getCompletedRequests() {
        return completedRequests;
    }
    
    
    public void setVmAssignments(Map<String, String> vmAssignments) {
        this.vmAssignments = vmAssignments;
    }
    
    
    public Map<String, String> getVmAssignments() {
        return vmAssignments;
    }
} 

