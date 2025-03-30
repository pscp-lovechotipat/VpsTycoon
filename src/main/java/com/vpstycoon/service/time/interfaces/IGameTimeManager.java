package com.vpstycoon.service.time.interfaces;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.manager.CustomerRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ให้บริการจัดการเวลาในเกม รวมทั้งการคำนวณเวลาและการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
 */
public interface IGameTimeManager {
    
    /**
     * เริ่มการทำงานของระบบเวลา
     */
    void start();
    
    /**
     * หยุดการทำงานของระบบเวลา
     */
    void stop();
    
    /**
     * รีเซ็ตเวลาเกมเป็นค่าที่กำหนด
     */
    void resetTime(LocalDateTime newStartDateTime);
    
    /**
     * เพิ่ม VPS เข้าไปในระบบ
     */
    void addVPSServer(VPSOptimization vps);
    
    /**
     * ลบ VPS ออกจากระบบ
     */
    void removeVPSServer(VPSOptimization vps);
    
    /**
     * เพิ่ม listener สำหรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    void addTimeListener(GameTimeListener listener);
    
    /**
     * ลบ listener สำหรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    void removeTimeListener(GameTimeListener listener);
    
    /**
     * ดึงค่าเวลาปัจจุบันในเกม
     */
    LocalDateTime getGameDateTime();
    
    /**
     * ดึงค่าเวลาในเกมในรูปแบบมิลลิวินาที
     */
    long getGameTimeMs();
    
    /**
     * ตรวจสอบว่าระบบเวลากำลังทำงานอยู่หรือไม่
     */
    boolean isRunning();
    
    /**
     * interface สำหรับรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    interface GameTimeListener {
        void onTimeChanged(LocalDateTime newTime, long gameTimeMs);
        void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period);
    }
} 