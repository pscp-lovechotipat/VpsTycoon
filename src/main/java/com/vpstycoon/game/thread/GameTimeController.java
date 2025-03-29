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

    public void startTime() {
        if (timeThread == null || !timeThread.isAlive()) {
            timeThread = new Thread(timeManager::start);
            timeThread.setDaemon(true);
            timeThread.setName("GameTimeThread");
            timeThread.start();
        }
    }

    public void stopTime() {
        timeManager.stop();
        if (timeThread != null) {
            try {
                timeThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ลบ addVPSServer และ removeVPSServer ออก เพราะจัดการผ่าน Rack

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
    
    /**
     * รีเซ็ตเวลากลับเป็นค่าเริ่มต้นและเริ่มการเดินเวลาใหม่
     * @param startTime เวลาเริ่มต้นที่ต้องการรีเซ็ต (ถ้าเป็น null จะใช้ค่าเริ่มต้น 2000-01-01)
     */
    public void resetTime(LocalDateTime startTime) {
        // หยุดเวลาก่อน
        stopTime();
        
        // กำหนดค่าเริ่มต้นใหม่
        if (startTime == null) {
            startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
        
        // กำหนดค่าเริ่มต้นใหม่ใน timeManager
        timeManager.resetTime(startTime);
        
        // เริ่มการเดินเวลาใหม่
        startTime();
        
        System.out.println("รีเซ็ตและเริ่มเวลาเกมใหม่เป็น: " + startTime);
    }
    
    /**
     * รีเซ็ตเวลากลับเป็นค่าเริ่มต้นและเริ่มการเดินเวลาใหม่ (ใช้ค่าเริ่มต้น 2000-01-01)
     */
    public void resetTime() {
        resetTime(null);
    }
}