package com.vpstycoon.service.tasks.interfaces;

import javafx.scene.layout.StackPane;

/**
 * กำหนดโครงสร้างสำหรับงาน (task) ในเกม ที่ผู้เล่นต้องทำให้สำเร็จ
 */
public interface IGameTask {
    
    /**
     * กำหนด container สำหรับแสดงผล task
     */
    void setTaskContainer(StackPane container);
    
    /**
     * แสดง task ให้ผู้เล่น
     */
    void showTask(Runnable onComplete);
    
    /**
     * สร้าง UI สำหรับ task
     */
    void createTaskUI();
    
    /**
     * เริ่มจับเวลาสำหรับทำ task
     */
    void startTimer();
    
    /**
     * หยุดจับเวลา
     */
    void stopTimer();
    
    /**
     * แสดงผลลัพธ์เมื่อทำ task สำเร็จ
     */
    void showSuccess();
    
    /**
     * แสดงผลลัพธ์เมื่อทำ task ไม่สำเร็จ
     */
    void showFailure();
    
    /**
     * เก็บค่าว่า task ถูกทำเสร็จสิ้นแล้ว
     */
    void setCompleted(boolean completed);
    
    /**
     * เก็บค่าว่า task ล้มเหลว
     */
    void setFailed(boolean failed);
    
    /**
     * ตรวจสอบว่า task ทำสำเร็จแล้วหรือไม่
     */
    boolean isCompleted();
    
    /**
     * ตรวจสอบว่า task ล้มเหลวหรือไม่
     */
    boolean isFailed();
    
    /**
     * ดึงชื่อ task
     */
    String getTaskName();
    
    /**
     * ดึงคำอธิบาย task
     */
    String getTaskDescription();
    
    /**
     * ดึงค่ารางวัลเมื่อทำ task สำเร็จ
     */
    int getRewardAmount();
    
    /**
     * ดึงบทลงโทษเมื่อทำ task ไม่สำเร็จ
     */
    int getPenaltyRating();
    
    /**
     * ดึงระดับความยากของ task
     */
    int getDifficultyLevel();
    
    /**
     * ดึงระยะเวลาที่จำกัดในการทำ task
     */
    int getTimeLimit();
} 