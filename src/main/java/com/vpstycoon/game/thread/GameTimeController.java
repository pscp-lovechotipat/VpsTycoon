package com.vpstycoon.game.thread;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.rack.Rack;

import java.time.LocalDateTime;

public class GameTimeController {
    private final GameTimeManager timeManager;
    private Thread timeThread;

    public GameTimeController(Company company, RequestManager requestManager, Rack rack, LocalDateTime startTime) {
        this.timeManager = new GameTimeManager(company, requestManager, rack, startTime);
    }

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

    

    public void addTimeListener(GameTimeManager.GameTimeListener listener) {
        timeManager.addTimeListener(listener);
    }

    public void removeTimeListener(GameTimeManager.GameTimeListener listener) {
        timeManager.removeTimeListener(listener);
    }

    public LocalDateTime getGameDateTime() {
        return timeManager.getGameDateTime();
    }

    public GameTimeManager getGameTimeManager() {
        return timeManager;
    }

    public long getGameTimeMs() {
        return timeManager.getGameTimeMs();
    }
    
    
    public void resetTime(LocalDateTime startTime) {
        
        stopTime();
        
        
        if (startTime == null) {
            startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
        
        
        timeManager.resetTime(startTime);
        
        
        startTime();
        
        System.out.println("รีเซ็ตและเริ่มเวลาเกมใหม่เป็น: " + startTime);
    }
    
    
    public void resetTime() {
        resetTime(null);
    }
}
