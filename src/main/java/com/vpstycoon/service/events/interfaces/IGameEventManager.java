package com.vpstycoon.service.events.interfaces;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.thread.task.GameTask;
import com.vpstycoon.ui.game.GameplayContentPane;

/**
 * จัดการกิจกรรม (event) ในเกม เช่น การสร้างและจัดการ Task ต่างๆ
 */
public interface IGameEventManager {
    
    /**
     * เริ่มการทำงานของระบบกิจกรรม
     */
    void start();
    
    /**
     * หยุดการทำงานของระบบกิจกรรม
     */
    void stop();
    
    /**
     * สร้าง task ใหม่และนำเสนอให้ผู้เล่น
     */
    void createRandomTask();
    
    /**
     * แสดง task ที่กำหนด
     */
    void showTask(GameTask task);
    
    /**
     * ตั้งค่าความถี่ในการสร้าง task
     */
    void setTaskFrequency(int minIntervalMs, int maxIntervalMs);
    
    /**
     * รับการแจ้งเตือนเมื่อเสร็จสิ้น task
     */
    void onTaskCompleted(GameTask task, boolean success);
    
    /**
     * ปรับโหมดการทำงาน (debug / non-debug)
     */
    void setDebugMode(boolean debugMode);
    
    /**
     * ตรวจสอบว่ากำลังทำงานอยู่หรือไม่
     */
    boolean isRunning();
    
    /**
     * ตรวจสอบว่ามี task ที่กำลังทำงานอยู่หรือไม่
     */
    boolean isTaskActive();
} 