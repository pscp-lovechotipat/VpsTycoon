package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.desktop.messenger.controllers.MessengerController;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryManager implements Serializable{
    private static ChatHistoryManager instance;
    private Map<CustomerRequest, List<ChatMessage>> customerChatHistory;
    private static final String CHAT_HISTORY_FILE = "save.dat";
    private transient MessengerController messengerController;

    public ChatHistoryManager() {
        
        customerChatHistory = loadChatHistoryFromGameState();
        if (customerChatHistory.isEmpty()) {
            customerChatHistory = loadChatHistoryFromFile();
            
            if (!customerChatHistory.isEmpty()) {
                saveChatHistoryToGameState();
                
                deleteChatHistoryFile();
            }
        }
    }

    public static ChatHistoryManager getInstance() {
        if (instance == null) {
            instance = new ChatHistoryManager();
        }
        return instance;
    }
    
    
    public static void resetInstance() {
        
        if (instance != null) {
            instance.resetAllChatData();
        }
        
        
        instance = null;
        System.out.println("ChatHistoryManager instance has been reset");
    }

    
    public void resetAllChatData() {
        
        if (customerChatHistory != null) {
            customerChatHistory.clear();
        } else {
            customerChatHistory = new HashMap<>();
        }
        
        
        deleteChatHistoryFile();
        
        
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null) {
            currentState.setChatHistory(new HashMap<>());
        }
        
        System.out.println("All chat data has been reset (memory, file, and GameState)");
    }

    
    public void setMessengerController(MessengerController controller) {
        this.messengerController = controller;
    }

    
    public void updateCustomerRequestReferences() {
        if (messengerController == null) {
            System.err.println("ไม่สามารถปรับปรุงการอ้างอิง CustomerRequest เนื่องจากไม่ได้ตั้งค่า MessengerController");
            return;
        }
        
        Map<CustomerRequest, List<ChatMessage>> updatedHistory = new HashMap<>();
        
        
        List<CustomerRequest> currentRequests = new ArrayList<>();
        
        
        if (messengerController.getRequestManager() != null) {
            currentRequests.addAll(messengerController.getRequestManager().getRequests());
            
            
            currentRequests.addAll(messengerController.getRequestManager().getCompletedRequests());
        }
        
        System.out.println("กำลังปรับปรุงการอ้างอิง CustomerRequest...");
        System.out.println("จำนวน CustomerRequest ปัจจุบันในเกม: " + currentRequests.size());
        System.out.println("จำนวน CustomerRequest ในประวัติแชท: " + customerChatHistory.size());
        
        
        Map<CustomerRequest, CustomerRequest> customerRequestMap = new HashMap<>();
        
        
        for (CustomerRequest historyChatRequest : customerChatHistory.keySet()) {
            boolean found = false;
            
            
            for (CustomerRequest currentRequest : currentRequests) {
                if (historyChatRequest.equals(currentRequest)) {
                    customerRequestMap.put(historyChatRequest, currentRequest);
                    System.out.println("พบ CustomerRequest ตรงกันโดยใช้ equals(): " + currentRequest.getName());
                    found = true;
                    break;
                }
            }
            
            
            if (!found) {
                CustomerRequest matchedRequest = messengerController.findMatchingCustomerRequest(historyChatRequest);
                if (matchedRequest != null) {
                    customerRequestMap.put(historyChatRequest, matchedRequest);
                    System.out.println("พบ CustomerRequest ตรงกันโดยใช้ findMatchingCustomerRequest(): " + matchedRequest.getName());
                    found = true;
                }
            }
            
            
            if (!found) {
                customerRequestMap.put(historyChatRequest, historyChatRequest);
                System.out.println("ไม่พบ CustomerRequest ที่ตรงกับ: " + historyChatRequest.getName() + " - คงใช้ตัวเดิม");
            }
        }
        
        
        for (Map.Entry<CustomerRequest, List<ChatMessage>> entry : customerChatHistory.entrySet()) {
            CustomerRequest historyChatRequest = entry.getKey();
            List<ChatMessage> messages = entry.getValue();
            
            
            CustomerRequest matchedRequest = customerRequestMap.get(historyChatRequest);
            if (matchedRequest != null) {
                updatedHistory.put(matchedRequest, messages);
            } else {
                updatedHistory.put(historyChatRequest, messages);
            }
        }
        
        
        customerChatHistory.clear();
        customerChatHistory.putAll(updatedHistory);
        
        System.out.println("ปรับปรุงการอ้างอิง CustomerRequest เสร็จสิ้น");
        System.out.println("จำนวน CustomerRequest หลังปรับปรุง: " + customerChatHistory.size());
    }

    public List<ChatMessage> getChatHistory(CustomerRequest request) {
        return customerChatHistory.getOrDefault(request, new ArrayList<>());
    }

    public void addMessage(CustomerRequest request, ChatMessage message) {
        customerChatHistory.computeIfAbsent(request, k -> new ArrayList<>()).add(message);
    }
    
    
    public void clearChatHistory() {
        customerChatHistory.clear();
        System.out.println("Chat history cleared from memory");
    }
    
    
    public boolean deleteChatHistoryFile() {
        File chatFile = new File(CHAT_HISTORY_FILE);
        if (chatFile.exists()) {
            boolean deleted = chatFile.delete();
            if (deleted) {
                System.out.println("Chat history file deleted: " + chatFile.getAbsolutePath());
            } else {
                System.err.println("Failed to delete chat history file: " + chatFile.getAbsolutePath());
            }
            return deleted;
        } else {
            System.out.println("Chat history file does not exist: " + chatFile.getAbsolutePath());
            return true; 
        }
    }

    
    public void saveChatHistory() {
        saveChatHistoryToGameState();
        
        saveChatHistoryToFile();
    }
    
    
    private void saveChatHistoryToGameState() {
        try {
            GameState currentState = ResourceManager.getInstance().getCurrentState();
            if (currentState != null) {
                currentState.setChatHistory(customerChatHistory);
                
                
                if (messengerController != null && messengerController.getRequestManager() != null) {
                    currentState.setPendingRequests(messengerController.getRequestManager().getRequests());
                    currentState.setCompletedRequests(messengerController.getRequestManager().getCompletedRequests());
                    
                    
                    Map<String, String> vmAssignments = new HashMap<>();
                    for (CustomerRequest request : customerChatHistory.keySet()) {
                        if (request.isAssignedToVM()) {
                            vmAssignments.put(request.getAssignedVmId(), request.getName());
                        }
                    }
                    
                    currentState.setVmAssignments(vmAssignments);
                }
                
                System.out.println("Chat history saved successfully to GameState with " + 
                      customerChatHistory.size() + " conversations");
            } else {
                System.err.println("Cannot save chat history: GameState is null");
                
                saveChatHistoryToFile();
            }
        } catch (Exception e) {
            System.err.println("Error saving chat history to GameState: " + e.getMessage());
            e.printStackTrace();
            
            saveChatHistoryToFile();
        }
    }
    
    
    private void saveChatHistoryToFile() {
        try {
            
            ChatHistorySaveData saveData = new ChatHistorySaveData();
            saveData.setChatHistory(customerChatHistory);
            
            
            if (messengerController != null && messengerController.getRequestManager() != null) {
                
                List<CustomerRequest> pendingRequestsList = new ArrayList<>(messengerController.getRequestManager().getRequests());
                saveData.setPendingRequests(pendingRequestsList);
                
                List<CustomerRequest> completedRequestsList = new ArrayList<>(messengerController.getRequestManager().getCompletedRequests());
                saveData.setCompletedRequests(completedRequestsList);
                
                
                Map<String, String> vmAssignments = new HashMap<>();
                for (CustomerRequest request : customerChatHistory.keySet()) {
                    if (request.isAssignedToVM()) {
                        vmAssignments.put(request.getAssignedVmId(), request.getName());
                    }
                }
                
                saveData.setVmAssignments(vmAssignments);
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CHAT_HISTORY_FILE))) {
                oos.writeObject(saveData);
                System.out.println("Chat history saved successfully to file " + new File(CHAT_HISTORY_FILE).getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error saving chat history to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    @SuppressWarnings("unchecked")
    private Map<CustomerRequest, List<ChatMessage>> loadChatHistoryFromGameState() {
        try {
            GameState currentState = ResourceManager.getInstance().getCurrentState();
            if (currentState != null && currentState.getChatHistory() != null) {
                Map<CustomerRequest, List<ChatMessage>> history = currentState.getChatHistory();
                
                
                if (currentState.getPendingRequests() != null && 
                    messengerController != null && 
                    messengerController.getRequestManager() != null) {
                    
                    
                    messengerController.getRequestManager().setRequests(currentState.getPendingRequests());
                    
                    
                    if (currentState.getCompletedRequests() != null) {
                        messengerController.getRequestManager().setCompletedRequests(currentState.getCompletedRequests());
                    }
                    
                    
                    if (currentState.getVmAssignments() != null) {
                        for (Map.Entry<String, String> entry : currentState.getVmAssignments().entrySet()) {
                            String vmId = entry.getKey();
                            String requestName = entry.getValue();
                            
                            
                            for (CustomerRequest request : history.keySet()) {
                                if (request.getName().equals(requestName)) {
                                    request.assignToVM(vmId);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                System.out.println("Chat history loaded successfully from GameState with " + 
                      history.size() + " conversations");
                return history;
            } else {
                System.out.println("No chat history found in GameState");
                return new HashMap<>();
            }
        } catch (Exception e) {
            System.err.println("Error loading chat history from GameState: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    
    @SuppressWarnings("unchecked")
    private Map<CustomerRequest, List<ChatMessage>> loadChatHistoryFromFile() {
        File file = new File(CHAT_HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("Chat history file does not exist at: " + file.getAbsolutePath());
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            Map<CustomerRequest, List<ChatMessage>> history;
            
            
            if (obj instanceof ChatHistorySaveData) {
                ChatHistorySaveData saveData = (ChatHistorySaveData) obj;
                history = saveData.getChatHistory();
                
                
                if (messengerController != null && messengerController.getRequestManager() != null) {
                    RequestManager requestManager = messengerController.getRequestManager();
                    
                    if (saveData.getPendingRequests() != null) {
                        
                        List<CustomerRequest> pendingList = new ArrayList<>(saveData.getPendingRequests());
                        requestManager.setRequests(pendingList);
                    }
                    
                    if (saveData.getCompletedRequests() != null) {
                        
                        List<CustomerRequest> completedList = new ArrayList<>(saveData.getCompletedRequests());
                        requestManager.setCompletedRequests(completedList);
                    }
                    
                    
                    if (saveData.getVmAssignments() != null) {
                        for (Map.Entry<String, String> entry : saveData.getVmAssignments().entrySet()) {
                            String vmId = entry.getKey();
                            String requestName = entry.getValue();
                            
                            
                            for (CustomerRequest request : history.keySet()) {
                                if (request.getName().equals(requestName)) {
                                    request.assignToVM(vmId);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (obj instanceof Map) {
                
                try {
                    history = (Map<CustomerRequest, List<ChatMessage>>) obj;
                } catch (ClassCastException e) {
                    System.err.println("Cannot cast saved object to Map<CustomerRequest, List<ChatMessage>>: " + e.getMessage());
                    return new HashMap<>();
                }
            } else {
                System.err.println("Unknown object type in chat history file: " + obj.getClass().getName());
                return new HashMap<>();
            }
            
            System.out.println("Chat history loaded successfully from file " + file.getAbsolutePath() + 
                  " with " + history.size() + " conversations");
            return history;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading chat history from file: " + e.getMessage());
            e.printStackTrace();
            
            if (file.exists()) {
                File backupFile = new File(CHAT_HISTORY_FILE + ".bak");
                file.renameTo(backupFile);
                System.out.println("Renamed corrupt chat history file to " + backupFile.getAbsolutePath());
            }
            return new HashMap<>();
        }
    }
}
