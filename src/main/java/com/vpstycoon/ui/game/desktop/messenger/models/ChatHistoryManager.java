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

    public List<ChatMessage> getChatHistory(CustomerRequest request) {
        return customerChatHistory.getOrDefault(request, new ArrayList<>());
    }

    public void addMessage(CustomerRequest request, ChatMessage message) {
        customerChatHistory.computeIfAbsent(request, k -> new ArrayList<>()).add(message);
    }

    public void saveChatHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CHAT_HISTORY_FILE))) {
            oos.writeObject(customerChatHistory);
            System.out.println("Chat history saved successfully");
        } catch (IOException e) {
            System.err.println("Error saving chat history: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<CustomerRequest, List<ChatMessage>> loadChatHistory() {
        File file = new File(CHAT_HISTORY_FILE);
        if (!file.exists()) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<CustomerRequest, List<ChatMessage>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading chat history: " + e.getMessage());
            return new HashMap<>();
        }
    }
}