package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.manager.CustomerRequest;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * คลาสสำหรับเก็บข้อมูลที่จะบันทึกลงในไฟล์ save.dat
 * เก็บข้อมูลทั้งหมดที่เกี่ยวข้องกับการสนทนา รวมถึงสถานะของคำขอและการกำหนด VM
 */
public class ChatHistorySaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // ประวัติการสนทนากับลูกค้า
    private Map<CustomerRequest, List<ChatMessage>> chatHistory;
    
    // รายการคำขอที่ยังไม่เสร็จสิ้น (ใช้ ArrayList แทน ObservableList)
    private ArrayList<CustomerRequest> pendingRequests;
    
    // รายการคำขอที่เสร็จสิ้นแล้ว
    private ArrayList<CustomerRequest> completedRequests;
    
    // การจับคู่ระหว่าง VM ID กับ Request
    private Map<String, String> vmAssignments;
    
    /**
     * Constructor สร้าง object ว่างๆ
     */
    public ChatHistorySaveData() {
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
        this.vmAssignments = new HashMap<>();
    }
    
    /**
     * ตั้งค่าประวัติการสนทนา
     * @param chatHistory ประวัติการสนทนาที่ต้องการบันทึก
     */
    public void setChatHistory(Map<CustomerRequest, List<ChatMessage>> chatHistory) {
        this.chatHistory = chatHistory;
    }
    
    /**
     * รับประวัติการสนทนา
     * @return ประวัติการสนทนาที่บันทึกไว้
     */
    public Map<CustomerRequest, List<ChatMessage>> getChatHistory() {
        return chatHistory;
    }
    
    /**
     * ตั้งค่ารายการคำขอที่ยังไม่เสร็จสิ้น
     * @param pendingRequests รายการคำขอที่ต้องการบันทึก
     */
    public void setPendingRequests(List<CustomerRequest> pendingRequests) {
        // แปลงเป็น ArrayList เพื่อความปลอดภัยในการ serialize
        this.pendingRequests = new ArrayList<>(pendingRequests);
    }
    
    /**
     * รับรายการคำขอที่ยังไม่เสร็จสิ้น
     * @return รายการคำขอที่บันทึกไว้
     */
    public ArrayList<CustomerRequest> getPendingRequests() {
        return pendingRequests;
    }
    
    /**
     * ตั้งค่ารายการคำขอที่เสร็จสิ้นแล้ว
     * @param completedRequests รายการคำขอที่ต้องการบันทึก
     */
    public void setCompletedRequests(List<CustomerRequest> completedRequests) {
        // แปลงเป็น ArrayList เพื่อความปลอดภัยในการ serialize
        this.completedRequests = new ArrayList<>(completedRequests);
    }
    
    /**
     * รับรายการคำขอที่เสร็จสิ้นแล้ว
     * @return รายการคำขอที่บันทึกไว้
     */
    public ArrayList<CustomerRequest> getCompletedRequests() {
        return completedRequests;
    }
    
    /**
     * ตั้งค่าการจับคู่ระหว่าง VM ID กับ Request
     * @param vmAssignments การจับคู่ที่ต้องการบันทึก
     */
    public void setVmAssignments(Map<String, String> vmAssignments) {
        this.vmAssignments = vmAssignments;
    }
    
    /**
     * รับการจับคู่ระหว่าง VM ID กับ Request
     * @return การจับคู่ที่บันทึกไว้
     */
    public Map<String, String> getVmAssignments() {
        return vmAssignments;
    }
} 