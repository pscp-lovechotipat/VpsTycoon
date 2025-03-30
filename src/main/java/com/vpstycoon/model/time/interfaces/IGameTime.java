package com.vpstycoon.model.time.interfaces;

import java.time.LocalDateTime;

/**
 * ข้อมูลเวลาของเกม เก็บค่าเวลาปัจจุบันและสเกลเวลา
 */
public interface IGameTime {

    /**
     * ดึงค่าเวลาปัจจุบันในเกม
     */
    LocalDateTime getCurrentDateTime();
    
    /**
     * กำหนดค่าเวลาปัจจุบันในเกม
     */
    void setCurrentDateTime(LocalDateTime dateTime);
    
    /**
     * ดึงค่าเวลาเริ่มต้นของเกม
     */
    LocalDateTime getStartDateTime();
    
    /**
     * ดึงเวลาเกมในรูปแบบมิลลิวินาที
     */
    long getGameTimeMs();
    
    /**
     * กำหนดเวลาเกมในรูปแบบมิลลิวินาที
     */
    void setGameTimeMs(long timeMs);
    
    /**
     * คำนวณเวลาเกมจากเวลาจริง
     */
    LocalDateTime calculateGameTime(long realTimeMs);
    
    /**
     * ดึงความเร็วของเวลาเกมเทียบกับเวลาจริง
     */
    double getTimeScale();
    
    /**
     * กำหนดความเร็วของเวลาเกมเทียบกับเวลาจริง
     */
    void setTimeScale(double scale);
    
    /**
     * คำนวณเวลาที่จะผ่านไปในเกมเมื่อเวลาจริงผ่านไป elapsedRealMs
     */
    void addRealTimeMs(long elapsedRealMs);
    
    /**
     * รีเซ็ตเวลาเกมกลับเป็นเวลาเริ่มต้น
     */
    void resetTime();
} 