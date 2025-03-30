package com.vpstycoon.model.request.interfaces;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * อินเตอร์เฟซสำหรับคำขอของลูกค้า
 */
public interface ICustomerRequest extends Serializable {
    
    /**
     * รับ ID ของคำขอ
     */
    String getId();
    
    /**
     * รับชื่อลูกค้า
     */
    String getCustomerName();
    
    /**
     * ตั้งชื่อลูกค้า
     */
    void setCustomerName(String customerName);
    
    /**
     * รับสถานะการจัดการคำขอ
     */
    boolean isHandled();
    
    /**
     * ตั้งค่าสถานะการจัดการคำขอ
     */
    void setHandled(boolean handled);
    
    /**
     * รับสถานะการสำเร็จของคำขอ
     */
    boolean isCompleted();
    
    /**
     * ตั้งค่าสถานะการสำเร็จของคำขอ
     */
    void setCompleted(boolean completed);
    
    /**
     * รับคำอธิบายคำขอ
     */
    String getDescription();
    
    /**
     * ตั้งคำอธิบายคำขอ
     */
    void setDescription(String description);
    
    /**
     * รับเวลาที่สร้างคำขอ
     */
    LocalDateTime getCreatedAt();
    
    /**
     * ตั้งเวลาที่สร้างคำขอ
     */
    void setCreatedAt(LocalDateTime createdAt);
    
    /**
     * รับเวลาที่จัดการคำขอ
     */
    LocalDateTime getHandledAt();
    
    /**
     * ตั้งเวลาที่จัดการคำขอ
     */
    void setHandledAt(LocalDateTime handledAt);
    
    /**
     * รับค่าตอบแทนของคำขอ
     */
    long getReward();
    
    /**
     * ตั้งค่าตอบแทนของคำขอ
     */
    void setReward(long reward);
    
    /**
     * รับหัวข้อของคำขอ
     */
    String getTitle();
    
    /**
     * ตั้งหัวข้อของคำขอ
     */
    void setTitle(String title);
    
    /**
     * รับประเภทของคำขอ
     */
    String getRequestType();
    
    /**
     * ตั้งประเภทของคำขอ
     */
    void setRequestType(String requestType);
    
    /**
     * คำนวณและรับผลกระทบต่อความพึงพอใจของลูกค้า
     */
    int calculateSatisfactionImpact();
} 