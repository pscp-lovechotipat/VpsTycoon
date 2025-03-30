package com.vpstycoon.model.request.interfaces;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;
import com.vpstycoon.model.request.enums.RentalPeriodType;

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
    RequestType getRequestType();
    
    /**
     * ตั้งประเภทของคำขอ
     */
    void setRequestType(RequestType requestType);
    
    /**
     * คำนวณและรับผลกระทบต่อความพึงพอใจของลูกค้า
     */
    int calculateSatisfactionImpact();
    
    /**
     * ดึงชื่อลูกค้าที่สร้างคำขอ
     */
    String getName();
    
    /**
     * ดึงประเภทของลูกค้า
     */
    CustomerType getCustomerType();
    
    /**
     * ดึงประเภทของระยะเวลาเช่า
     */
    RentalPeriodType getRentalPeriodType();
    
    /**
     * ดึงจำนวนเงินที่ต้องชำระ
     */
    double getPaymentAmount();
    
    /**
     * ดึงเวลาที่ชำระครั้งล่าสุด
     */
    long getLastPaymentTime();
    
    /**
     * บันทึกการชำระเงินด้วยเวลาที่ระบุ
     */
    void recordPayment(long paymentTime);
    
    /**
     * ตรวจสอบว่าคำขอยังใช้งานอยู่หรือไม่
     */
    boolean isActive();
    
    /**
     * ตรวจสอบว่าคำขอหมดอายุแล้วหรือไม่
     */
    boolean isExpired();
    
    /**
     * ตรวจสอบว่าคำขอถูกยอมรับแล้วหรือไม่
     */
    boolean isAccepted();
    
    /**
     * ยอมรับคำขอ
     */
    void accept();
    
    /**
     * ปฏิเสธคำขอ
     */
    void reject();
    
    /**
     * กำหนดให้คำขอหมดอายุ
     */
    void markAsExpired();
    
    /**
     * ตรวจสอบว่าถึงเวลาชำระเงินหรือไม่
     */
    boolean isPaymentDue(long currentTime);
    
    /**
     * กำหนดจำนวนเงินที่ต้องชำระ
     */
    void setPaymentAmount(double amount);
} 