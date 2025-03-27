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

public class ChatHistoryManager {
    private static ChatHistoryManager instance;
    private Map<CustomerRequest, List<ChatMessage>> customerChatHistory;
    private static final String CHAT_HISTORY_FILE = "chat_history.dat";
    private MessengerController messengerController;

    public ChatHistoryManager() {
        // โหลดประวัติแชทจาก GameState ก่อน ถ้าไม่มีค่อยโหลดจากไฟล์แยก
        customerChatHistory = loadChatHistoryFromGameState();
        if (customerChatHistory.isEmpty()) {
            customerChatHistory = loadChatHistoryFromFile();
            // ถ้าโหลดได้จากไฟล์แยก ให้บันทึกลง GameState ทันที
            if (!customerChatHistory.isEmpty()) {
                saveChatHistoryToGameState();
                // ลบไฟล์เดิมเพื่อไม่ให้เกิดความสับสน
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
    
    /**
     * ล้างข้อมูล instance และกำหนดให้เป็น null เพื่อให้สร้างใหม่ในครั้งต่อไป
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * ตั้งค่า MessengerController เพื่อใช้ในการค้นหา CustomerRequest ที่ตรงกัน
     * @param controller MessengerController ที่จะใช้งาน
     */
    public void setMessengerController(MessengerController controller) {
        this.messengerController = controller;
    }

    /**
     * ปรับปรุงการอ้างอิง CustomerRequest ให้ตรงกับในเกมปัจจุบัน
     * ต้องเรียกหลังจากโหลดเกมและตั้งค่า MessengerController แล้ว
     */
    public void updateCustomerRequestReferences() {
        if (messengerController == null) {
            System.err.println("ไม่สามารถปรับปรุงการอ้างอิง CustomerRequest เนื่องจากไม่ได้ตั้งค่า MessengerController");
            return;
        }
        
        Map<CustomerRequest, List<ChatMessage>> updatedHistory = new HashMap<>();
        
        // ดึงรายการ CustomerRequest ทั้งหมดจาก RequestManager ผ่าน MessengerController
        List<CustomerRequest> currentRequests = new ArrayList<>();
        
        // เพิ่ม pendingRequests ถ้ามี
        if (messengerController.getRequestManager() != null) {
            currentRequests.addAll(messengerController.getRequestManager().getRequests());
            
            // เพิ่ม completedRequests ถ้ามี
            currentRequests.addAll(messengerController.getRequestManager().getCompletedRequests());
        }
        
        System.out.println("กำลังปรับปรุงการอ้างอิง CustomerRequest...");
        System.out.println("จำนวน CustomerRequest ปัจจุบันในเกม: " + currentRequests.size());
        System.out.println("จำนวน CustomerRequest ในประวัติแชท: " + customerChatHistory.size());
        
        // สร้าง customerRequestMap เพื่อเก็บการจับคู่ระหว่าง CustomerRequest ในแชทกับในเกม
        Map<CustomerRequest, CustomerRequest> customerRequestMap = new HashMap<>();
        
        // ตรวจสอบการเปรียบเทียบ CustomerRequest ทั้งหมด
        for (CustomerRequest historyChatRequest : customerChatHistory.keySet()) {
            boolean found = false;
            
            // ลองเปรียบเทียบโดยตรงโดยใช้ equals()
            for (CustomerRequest currentRequest : currentRequests) {
                if (historyChatRequest.equals(currentRequest)) {
                    customerRequestMap.put(historyChatRequest, currentRequest);
                    System.out.println("พบ CustomerRequest ตรงกันโดยใช้ equals(): " + currentRequest.getName());
                    found = true;
                    break;
                }
            }
            
            // ถ้ายังไม่พบ ลองค้นหาด้วยการเปรียบเทียบคุณสมบัติ
            if (!found) {
                CustomerRequest matchedRequest = messengerController.findMatchingCustomerRequest(historyChatRequest);
                if (matchedRequest != null) {
                    customerRequestMap.put(historyChatRequest, matchedRequest);
                    System.out.println("พบ CustomerRequest ตรงกันโดยใช้ findMatchingCustomerRequest(): " + matchedRequest.getName());
                    found = true;
                }
            }
            
            // ถ้ายังไม่พบอีก ให้ใช้ตัวเดิมไปก่อน
            if (!found) {
                customerRequestMap.put(historyChatRequest, historyChatRequest);
                System.out.println("ไม่พบ CustomerRequest ที่ตรงกับ: " + historyChatRequest.getName() + " - คงใช้ตัวเดิม");
            }
        }
        
        // สร้าง updatedHistory โดยใช้ customerRequestMap
        for (Map.Entry<CustomerRequest, List<ChatMessage>> entry : customerChatHistory.entrySet()) {
            CustomerRequest historyChatRequest = entry.getKey();
            List<ChatMessage> messages = entry.getValue();
            
            // ดึง CustomerRequest ที่ตรงกันจาก customerRequestMap
            CustomerRequest matchedRequest = customerRequestMap.get(historyChatRequest);
            if (matchedRequest != null) {
                updatedHistory.put(matchedRequest, messages);
            } else {
                updatedHistory.put(historyChatRequest, messages);
            }
        }
        
        // อัพเดต customerChatHistory ด้วยข้อมูลใหม่
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
    
    /**
     * ล้างข้อมูลประวัติแชททั้งหมดในหน่วยความจำ
     */
    public void clearChatHistory() {
        customerChatHistory.clear();
        System.out.println("Chat history cleared from memory");
    }
    
    /**
     * ลบไฟล์ chat_history.dat
     * @return true ถ้าลบสำเร็จ, false ถ้าล้มเหลวหรือไม่พบไฟล์
     */
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
            return true; // ถือว่าสำเร็จเนื่องจากไม่มีไฟล์อยู่แล้ว
        }
    }

    /**
     * บันทึกประวัติแชทลงใน GameState
     */
    public void saveChatHistory() {
        saveChatHistoryToGameState();
    }
    
    /**
     * บันทึกประวัติแชทลงใน GameState
     */
    private void saveChatHistoryToGameState() {
        try {
            GameState currentState = ResourceManager.getInstance().getCurrentState();
            if (currentState != null) {
                currentState.setChatHistory(customerChatHistory);
                System.out.println("Chat history saved successfully to GameState with " + 
                      customerChatHistory.size() + " conversations");
            } else {
                System.err.println("Cannot save chat history: GameState is null");
                // ถ้าไม่มี GameState ให้บันทึกลงไฟล์แยกแทน
                saveChatHistoryToFile();
            }
        } catch (Exception e) {
            System.err.println("Error saving chat history to GameState: " + e.getMessage());
            e.printStackTrace();
            // ถ้าเกิดข้อผิดพลาด ให้ลองบันทึกลงไฟล์แยกแทน
            saveChatHistoryToFile();
        }
    }
    
    /**
     * บันทึกประวัติแชทลงไฟล์แยก (ใช้เป็น fallback)
     */
    private void saveChatHistoryToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CHAT_HISTORY_FILE))) {
            oos.writeObject(customerChatHistory);
            System.out.println("Chat history saved successfully to file " + new File(CHAT_HISTORY_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving chat history to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * โหลดประวัติแชทจาก GameState
     */
    @SuppressWarnings("unchecked")
    private Map<CustomerRequest, List<ChatMessage>> loadChatHistoryFromGameState() {
        try {
            GameState currentState = ResourceManager.getInstance().getCurrentState();
            if (currentState != null && currentState.getChatHistory() != null) {
                Map<CustomerRequest, List<ChatMessage>> history = currentState.getChatHistory();
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

    /**
     * โหลดประวัติแชทจากไฟล์แยก (ใช้เป็น fallback)
     */
    @SuppressWarnings("unchecked")
    private Map<CustomerRequest, List<ChatMessage>> loadChatHistoryFromFile() {
        File file = new File(CHAT_HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("Chat history file does not exist at: " + file.getAbsolutePath());
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<CustomerRequest, List<ChatMessage>> history = (Map<CustomerRequest, List<ChatMessage>>) ois.readObject();
            System.out.println("Chat history loaded successfully from file " + file.getAbsolutePath() + 
                  " with " + history.size() + " conversations");
            return history;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading chat history from file: " + e.getMessage());
            e.printStackTrace();
            // ถ้าโหลดไม่สำเร็จ ให้สำรองไฟล์เดิมและสร้างใหม่
            if (file.exists()) {
                File backupFile = new File(CHAT_HISTORY_FILE + ".bak");
                file.renameTo(backupFile);
                System.out.println("Renamed corrupt chat history file to " + backupFile.getAbsolutePath());
            }
            return new HashMap<>();
        }
    }
}