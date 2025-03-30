package com.vpstycoon.service.time.interfaces;

/**
 * สร้างคำขอจากลูกค้าโดยอัตโนมัติ ทำงานในรูปแบบ background thread
 */
public interface IRequestGenerator {
    
    /**
     * หยุดการทำงานของตัวสร้างคำขอ
     */
    void stopGenerator();
    
    /**
     * รีเซ็ตตัวสร้างคำขอกลับเป็นค่าเริ่มต้น
     */
    void resetGenerator();
    
    /**
     * หยุดการทำงานชั่วคราว
     */
    void pauseGenerator();
    
    /**
     * เริ่มการทำงานต่อหลังจากหยุดชั่วคราว
     */
    void resumeGenerator();
    
    /**
     * ตรวจสอบว่ากำลังหยุดชั่วคราวอยู่หรือไม่
     */
    boolean isPaused();
    
    /**
     * กำหนดจำนวนคำขอสูงสุดที่รอตอบรับได้
     */
    void setMaxPendingRequests(int maxRequests);
} 