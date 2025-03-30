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
import java.util.HashSet;

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
        
        // รวบรวม CustomerRequest ทั้งหมดที่มีอยู่ในปัจจุบัน
        List<CustomerRequest> currentRequests = new ArrayList<>();
        
        // เพิ่ม request ที่กำลังดำเนินการและ request ที่เสร็จสิ้นแล้ว
        if (messengerController.getRequestManager() != null) {
            RequestManager requestManager = messengerController.getRequestManager();
            if (requestManager.getRequests() != null) {
                currentRequests.addAll(requestManager.getRequests());
            }
            
            if (requestManager.getCompletedRequests() != null) {
                currentRequests.addAll(requestManager.getCompletedRequests());
            }
        }
        
        System.out.println("กำลังปรับปรุงการอ้างอิง CustomerRequest...");
        System.out.println("จำนวน CustomerRequest ปัจจุบันในเกม: " + currentRequests.size());
        System.out.println("จำนวน CustomerRequest ในประวัติแชท: " + customerChatHistory.size());
        
        // สร้าง Map ที่จับคู่ระหว่าง CustomerRequest ในประวัติกับ CustomerRequest ปัจจุบัน
        Map<String, CustomerRequest> nameToCurrentRequest = new HashMap<>();
        Map<Long, CustomerRequest> idToCurrentRequest = new HashMap<>();
        
        // จัดเก็บ CustomerRequest ปัจจุบันตามชื่อและ ID
        for (CustomerRequest request : currentRequests) {
            if (request.getName() != null && !request.getName().isEmpty()) {
                nameToCurrentRequest.put(request.getName(), request);
            }
            if (request.getId() != 0) {
                idToCurrentRequest.put(Long.valueOf(request.getId()), request);
            }
        }
        
        // จับคู่ CustomerRequest จากประวัติกับ CustomerRequest ปัจจุบัน
        for (Map.Entry<CustomerRequest, List<ChatMessage>> entry : customerChatHistory.entrySet()) {
            CustomerRequest historyChatRequest = entry.getKey();
            List<ChatMessage> messages = entry.getValue();
            CustomerRequest matchedRequest = null;
            
            // ค้นหาตาม ID
            if (historyChatRequest.getId() != 0 && idToCurrentRequest.containsKey(Long.valueOf(historyChatRequest.getId()))) {
                matchedRequest = idToCurrentRequest.get(Long.valueOf(historyChatRequest.getId()));
                System.out.println("พบ CustomerRequest ตรงกันโดยใช้ ID: " + matchedRequest.getId());
            }
            // ค้นหาตามชื่อถ้าไม่พบตาม ID
            else if (historyChatRequest.getName() != null && !historyChatRequest.getName().isEmpty() 
                    && nameToCurrentRequest.containsKey(historyChatRequest.getName())) {
                matchedRequest = nameToCurrentRequest.get(historyChatRequest.getName());
                System.out.println("พบ CustomerRequest ตรงกันโดยใช้ชื่อ: " + matchedRequest.getName());
            }
            // ใช้วิธีค้นหาแบบละเอียดถ้าไม่พบด้วยวิธีข้างต้น
            else {
                matchedRequest = messengerController.findMatchingCustomerRequest(historyChatRequest);
                if (matchedRequest != null) {
                    System.out.println("พบ CustomerRequest ตรงกันโดยใช้ findMatchingCustomerRequest(): " + matchedRequest.getName());
                }
            }
            
            // เมื่อหา request ที่ตรงกันได้แล้ว ให้ปรับปรุงข้อมูลใน VM ด้วยถ้าจำเป็น
            if (matchedRequest != null) {
                // ถ้า request ในประวัติมีการกำหนด VM แต่ request ปัจจุบันยังไม่มี
                if (historyChatRequest.isAssignedToVM() && !matchedRequest.isAssignedToVM()) {
                    matchedRequest.assignToVM(historyChatRequest.getAssignedVmId());
                }
                updatedHistory.put(matchedRequest, messages);
            } else {
                // ถ้าไม่พบคู่ที่ตรงกัน ให้ใช้ request จากประวัติต่อไป
                System.out.println("ไม่พบ CustomerRequest ที่ตรงกับ: " + 
                    (historyChatRequest.getName() != null ? historyChatRequest.getName() : "ไม่มีชื่อ") + 
                    " - คงใช้ตัวเดิม");
                updatedHistory.put(historyChatRequest, messages);
            }
        }
        
        // ปรับปรุงข้อมูลประวัติแชท
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
            // สร้าง object สำหรับบันทึกข้อมูลแชท
            ChatHistorySaveData saveData = new ChatHistorySaveData();
            saveData.setChatHistory(customerChatHistory);
            
            // เพิ่มข้อมูลเกี่ยวกับ requests ที่กำลังดำเนินการและเสร็จสิ้นแล้ว
            if (messengerController != null && messengerController.getRequestManager() != null) {
                RequestManager requestManager = messengerController.getRequestManager();
                
                // จัดเก็บ pending requests
                if (requestManager.getRequests() != null) {
                    List<CustomerRequest> pendingRequestsList = new ArrayList<>(requestManager.getRequests());
                    saveData.setPendingRequests(pendingRequestsList);
                }
                
                // จัดเก็บ completed requests
                if (requestManager.getCompletedRequests() != null) {
                    List<CustomerRequest> completedRequestsList = new ArrayList<>(requestManager.getCompletedRequests());
                    saveData.setCompletedRequests(completedRequestsList);
                }
                
                // จัดเก็บข้อมูลการเชื่อมโยงระหว่าง VM และ request
                Map<String, String> vmAssignments = new HashMap<>();
                for (CustomerRequest request : customerChatHistory.keySet()) {
                    if (request != null && request.isAssignedToVM()) {
                        String vmId = request.getAssignedVmId();
                        String requestId = String.valueOf(request.getId());
                        String requestName = request.getName();
                        
                        // เก็บทั้ง vmId -> requestId และ vmId -> requestName เพื่อช่วยในการกู้คืนข้อมูล
                        vmAssignments.put(vmId + "_id", requestId);
                        vmAssignments.put(vmId + "_name", requestName);
                    }
                }
                
                saveData.setVmAssignments(vmAssignments);
            }
            
            // บันทึกข้อมูลลงไฟล์
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CHAT_HISTORY_FILE))) {
                oos.writeObject(saveData);
                System.out.println("บันทึกประวัติแชทสำเร็จลงไฟล์ " + new File(CHAT_HISTORY_FILE).getAbsolutePath() + 
                      " มีการสนทนา " + customerChatHistory.size() + " รายการ");
            }
        } catch (IOException e) {
            System.err.println("เกิดข้อผิดพลาดในการบันทึกประวัติแชทลงไฟล์: " + e.getMessage());
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
            System.out.println("ไม่พบไฟล์ประวัติแชทที่: " + file.getAbsolutePath());
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            Map<CustomerRequest, List<ChatMessage>> history;
            
            // ตรวจสอบประเภทของข้อมูลที่โหลดมา
            if (obj instanceof ChatHistorySaveData) {
                ChatHistorySaveData saveData = (ChatHistorySaveData) obj;
                history = saveData.getChatHistory();
                
                // กู้คืน requests และ VM assignments ถ้ามี MessengerController
                if (messengerController != null && messengerController.getRequestManager() != null) {
                    RequestManager requestManager = messengerController.getRequestManager();
                    
                    // กู้คืน pending requests
                    if (saveData.getPendingRequests() != null) {
                        List<CustomerRequest> pendingList = new ArrayList<>(saveData.getPendingRequests());
                        requestManager.setRequests(pendingList);
                        System.out.println("โหลด pending requests จำนวน " + pendingList.size() + " รายการ");
                    }
                    
                    // กู้คืน completed requests
                    if (saveData.getCompletedRequests() != null) {
                        List<CustomerRequest> completedList = new ArrayList<>(saveData.getCompletedRequests());
                        requestManager.setCompletedRequests(completedList);
                        System.out.println("โหลด completed requests จำนวน " + completedList.size() + " รายการ");
                    }
                    
                    // กู้คืนข้อมูลการกำหนด VM โดยใช้ทั้ง ID และชื่อ
                    if (saveData.getVmAssignments() != null) {
                        Map<String, String> vmAssignments = saveData.getVmAssignments();
                        Map<String, CustomerRequest> idToRequest = new HashMap<>();
                        Map<String, CustomerRequest> nameToRequest = new HashMap<>();
                        
                        // สร้าง maps เพื่อค้นหา requests ตาม ID และชื่อได้เร็วขึ้น
                        for (CustomerRequest request : history.keySet()) {
                            if (request.getId() != 0) {
                                idToRequest.put(String.valueOf(request.getId()), request);
                            }
                            if (request.getName() != null && !request.getName().isEmpty()) {
                                nameToRequest.put(request.getName(), request);
                            }
                        }
                        
                        // ตรวจสอบทุก VM assignment
                        for (String vmId : new HashSet<>(vmAssignments.keySet())) {
                            if (vmId.endsWith("_id")) {
                                String originalVmId = vmId.substring(0, vmId.length() - 3);
                                String requestId = vmAssignments.get(vmId);
                                String requestName = vmAssignments.get(originalVmId + "_name");
                                
                                // ค้นหา request ตาม ID ก่อน แล้วค่อยใช้ชื่อถ้าไม่พบตาม ID
                                CustomerRequest request = idToRequest.get(requestId);
                                if (request == null && requestName != null) {
                                    request = nameToRequest.get(requestName);
                                }
                                
                                // กำหนด VM ให้กับ request ที่พบ
                                if (request != null) {
                                    request.assignToVM(originalVmId);
                                    System.out.println("กำหนด VM " + originalVmId + " ให้กับ request " + 
                                          (request.getName() != null ? request.getName() : "ไม่มีชื่อ"));
                                }
                            }
                        }
                    }
                }
            } else if (obj instanceof Map) {
                // กรณีรูปแบบข้อมูลเก่า (รองรับการอ่านข้อมูลเดิม)
                try {
                    history = (Map<CustomerRequest, List<ChatMessage>>) obj;
                } catch (ClassCastException e) {
                    System.err.println("ไม่สามารถแปลงข้อมูลที่บันทึกไว้เป็น Map<CustomerRequest, List<ChatMessage>>: " + e.getMessage());
                    return new HashMap<>();
                }
            } else {
                System.err.println("พบข้อมูลประเภทที่ไม่รู้จักในไฟล์ประวัติแชท: " + obj.getClass().getName());
                return new HashMap<>();
            }
            
            System.out.println("โหลดประวัติแชทสำเร็จจากไฟล์ " + file.getAbsolutePath() + 
                  " มีการสนทนา " + history.size() + " รายการ");
            return history;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดประวัติแชทจากไฟล์: " + e.getMessage());
            e.printStackTrace();
            
            // สำรองไฟล์ที่มีปัญหาไว้
            if (file.exists()) {
                File backupFile = new File(CHAT_HISTORY_FILE + ".bak");
                boolean renamed = file.renameTo(backupFile);
                if (renamed) {
                    System.out.println("ย้ายไฟล์ประวัติแชทที่เสียหายไปที่ " + backupFile.getAbsolutePath());
                } else {
                    System.err.println("ไม่สามารถย้ายไฟล์ประวัติแชทที่เสียหาย");
                }
            }
            return new HashMap<>();
        }
    }
}
