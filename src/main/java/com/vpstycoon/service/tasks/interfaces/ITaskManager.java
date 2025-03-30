package com.vpstycoon.service.tasks.interfaces;

import java.util.List;

/**
 * จัดการงาน (task) ต่างๆในเกม โดยเป็นตัวกลางระหว่าง GameEventManager และ Task แต่ละตัว
 */
public interface ITaskManager {
    
    /**
     * ลงทะเบียน task ใหม่กับระบบ
     */
    void registerTask(IGameTask task);
    
    /**
     * ลบ task ออกจากระบบ
     */
    void unregisterTask(IGameTask task);
    
    /**
     * ดึงรายการ task ทั้งหมดที่มีในระบบ
     */
    List<IGameTask> getAllTasks();
    
    /**
     * ดึง task ที่กำลังทำงานอยู่
     */
    IGameTask getActiveTask();
    
    /**
     * ตรวจสอบว่ามี task ที่กำลังทำงานอยู่หรือไม่
     */
    boolean isTaskActive();
    
    /**
     * สร้าง task สุ่มใหม่
     */
    IGameTask createRandomTask();
    
    /**
     * เริ่มทำ task ที่กำหนด
     */
    void startTask(IGameTask task);
    
    /**
     * จบการทำ task ที่กำลังทำงานอยู่ด้วยสถานะสำเร็จหรือไม่สำเร็จ
     */
    void completeTask(boolean success);
    
    /**
     * กำหนด callback เมื่อ task เสร็จสิ้น
     */
    void setTaskCompletionCallback(TaskCompletionCallback callback);
    
    /**
     * callback สำหรับเมื่อ task เสร็จสิ้น
     */
    interface TaskCompletionCallback {
        void onTaskCompleted(IGameTask task, boolean success);
    }
} 