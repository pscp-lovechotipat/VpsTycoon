package com.vpstycoon.view.interfaces;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Interface สำหรับคลาสที่ควบคุมการแสดงผล Scene หลัก
 */
public interface ISceneController {
    
    /**
     * ตั้งค่าเนื้อหาใน Scene
     * @param content Parent element ที่จะแสดงใน Scene
     */
    void setContent(Parent content);
    
    /**
     * อัปเดตความละเอียดของหน้าจอ
     */
    void updateResolution();
    
    /**
     * ดึง Stage หลัก
     * @return Stage หลักของแอปพลิเคชัน
     */
    Stage getStage();
    
    /**
     * ดึง Scene หลัก
     * @return Scene หลักของแอปพลิเคชัน
     */
    Scene getScene();
} 