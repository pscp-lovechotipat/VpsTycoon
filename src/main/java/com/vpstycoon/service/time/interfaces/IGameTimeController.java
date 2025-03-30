package com.vpstycoon.service.time.interfaces;

import java.time.LocalDateTime;

/**
 * ควบคุมการทำงานของระบบเวลาในเกม มีหน้าที่จัดการการเริ่ม หยุด และจัดการ thread
 */
public interface IGameTimeController {
    
    /**
     * เริ่มระบบเวลาในเกม
     */
    void startTime();
    
    /**
     * หยุดระบบเวลาในเกม
     */
    void stopTime();
    
    /**
     * เพิ่ม listener สำหรับรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    void addTimeListener(IGameTimeManager.GameTimeListener listener);
    
    /**
     * ลบ listener สำหรับรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    void removeTimeListener(IGameTimeManager.GameTimeListener listener);
    
    /**
     * ดึงค่าเวลาปัจจุบันในเกม
     */
    LocalDateTime getGameDateTime();
    
    /**
     * ดึง GameTimeManager ที่ใช้งานอยู่
     */
    IGameTimeManager getGameTimeManager();
    
    /**
     * ดึงค่าเวลาในเกมในรูปแบบมิลลิวินาที
     */
    long getGameTimeMs();
    
    /**
     * รีเซ็ตเวลาเกมเป็นค่าที่กำหนด
     */
    void resetTime(LocalDateTime startTime);
    
    /**
     * รีเซ็ตเวลาเกมเป็นค่าเริ่มต้น
     */
    void resetTime();
} 