package com.vpstycoon.service.time;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.service.time.interfaces.IGameTimeController;
import com.vpstycoon.service.time.interfaces.IGameTimeManager;
import com.vpstycoon.ui.game.rack.Rack;

import java.time.LocalDateTime;

/**
 * ควบคุมการทำงานของระบบเวลาในเกม
 */
public class GameTimeController implements IGameTimeController {
    private final IGameTimeManager timeManager;
    private Thread timeThread;

    /**
     * สร้าง GameTimeController ใหม่พร้อมกับ GameTimeManager
     */
    public GameTimeController(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.timeManager = new GameTimeManager(company, requestManager, rack, startTime);
    }

    /**
     * เริ่มระบบเวลาในเกม
     */
    @Override
    public synchronized void startTime() {
        if (timeThread != null) {
            System.out.println("สถานะ timeThread ก่อนเริ่ม: isAlive=" + timeThread.isAlive() + ", state=" + timeThread.getState());
        } else {
            System.out.println("สถานะ timeThread ก่อนเริ่ม: null (ยังไม่เคยสร้าง)");
        }
        
        System.out.println("สถานะ timeManager: running=" + timeManager.isRunning());
        
        if (timeThread != null && timeThread.isAlive()) {
            System.out.println("timeThread กำลังทำงานอยู่แล้ว ไม่ต้องเริ่มใหม่");
            
            if (!timeManager.isRunning()) {
                System.out.println("พบว่า timeManager.running=false แต่ thread ยังทำงานอยู่ กำลังแก้ไข...");
                
                stopTime();
                timeThread = null;
                
            } else {
                return; 
            }
        }
        
        timeThread = new Thread(timeManager::start);
        timeThread.setDaemon(true);
        timeThread.setName("GameTimeThread");
        timeThread.start();
        System.out.println("เริ่ม timeThread ใหม่สำเร็จ");
    }

    /**
     * หยุดระบบเวลาในเกม
     */
    @Override
    public synchronized void stopTime() {
        System.out.println("GameTimeController: กำลังหยุด timeManager และ timeThread");
        timeManager.stop();
        if (timeThread != null) {
            try {
                // ตั้งค่าให้ thread interrupt เพื่อให้หยุดเร็วขึ้น
                timeThread.interrupt();
                
                // รอ thread หยุดภายใน 1000ms
                timeThread.join(1000);
                
                // ตรวจสอบว่า thread ยังทำงานอยู่หรือไม่
                if (timeThread.isAlive()) {
                    System.out.println("WARNING: timeThread ยังทำงานอยู่หลังจากพยายามหยุดแล้ว");
                } else {
                    System.out.println("timeThread หยุดการทำงานเรียบร้อยแล้ว");
                    timeThread = null;
                }
            } catch (InterruptedException e) {
                System.err.println("เกิดข้อผิดพลาดขณะรอ timeThread หยุดทำงาน: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("timeThread เป็น null อยู่แล้ว ไม่จำเป็นต้องหยุด");
        }
    }

    /**
     * เพิ่ม listener สำหรับรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    @Override
    public void addTimeListener(IGameTimeManager.GameTimeListener listener) {
        timeManager.addTimeListener(listener);
    }

    /**
     * ลบ listener สำหรับรับการแจ้งเตือนเมื่อเวลาเปลี่ยนแปลง
     */
    @Override
    public void removeTimeListener(IGameTimeManager.GameTimeListener listener) {
        timeManager.removeTimeListener(listener);
    }

    /**
     * ดึงค่าเวลาปัจจุบันในเกม
     */
    @Override
    public LocalDateTime getGameDateTime() {
        return timeManager.getGameDateTime();
    }

    /**
     * ดึง GameTimeManager ที่ใช้งานอยู่
     */
    @Override
    public IGameTimeManager getGameTimeManager() {
        return timeManager;
    }

    /**
     * ดึงค่าเวลาในเกมในรูปแบบมิลลิวินาที
     */
    @Override
    public long getGameTimeMs() {
        return timeManager.getGameTimeMs();
    }
    
    /**
     * รีเซ็ตเวลาเกมเป็นค่าที่กำหนด
     */
    @Override
    public void resetTime(LocalDateTime startTime) {
        stopTime();
        
        if (startTime == null) {
            startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
        
        timeManager.resetTime(startTime);
        
        startTime();
        
        System.out.println("รีเซ็ตและเริ่มเวลาเกมใหม่เป็น: " + startTime);
    }
    
    /**
     * รีเซ็ตเวลาเกมเป็นค่าเริ่มต้น
     */
    @Override
    public void resetTime() {
        resetTime(null);
    }
} 