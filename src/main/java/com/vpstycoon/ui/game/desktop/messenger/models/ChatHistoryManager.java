package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.manager.CustomerRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryManager {
    private static ChatHistoryManager instance;
    private Map<CustomerRequest, List<ChatMessage>> customerChatHistory;
    private static final String CHAT_HISTORY_FILE = "chat_history.dat";

    public ChatHistoryManager() {
        customerChatHistory = loadChatHistory();
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

    public void saveChatHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CHAT_HISTORY_FILE))) {
            oos.writeObject(customerChatHistory);
            System.out.println("Chat history saved successfully to " + new File(CHAT_HISTORY_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving chat history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<CustomerRequest, List<ChatMessage>> loadChatHistory() {
        File file = new File(CHAT_HISTORY_FILE);
        if (!file.exists()) {
            System.out.println("Chat history file does not exist at: " + file.getAbsolutePath());
            return new HashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<CustomerRequest, List<ChatMessage>> history = (Map<CustomerRequest, List<ChatMessage>>) ois.readObject();
            System.out.println("Chat history loaded successfully from " + file.getAbsolutePath() + 
                  " with " + history.size() + " conversations");
            return history;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading chat history: " + e.getMessage());
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