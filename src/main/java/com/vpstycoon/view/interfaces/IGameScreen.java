package com.vpstycoon.view.interfaces;

import javafx.scene.Parent;

/**
 * Interface สำหรับหน้าจอเกมทุกหน้า
 */
public interface IGameScreen extends IView {
    
    /**
     * ดึง root element ของหน้าจอ
     * @return Parent element ที่เป็น root ของหน้าจอ
     */
    Parent getRoot();
    
    /**
     * ถูกเรียกเมื่อหน้าจอถูกแสดง
     */
    void onShow();
    
    /**
     * ถูกเรียกเมื่อหน้าจอถูกซ่อน
     */
    void onHide();
    
    /**
     * ถูกเรียกเมื่อหน้าจอถูกปรับขนาด
     * @param width ความกว้างใหม่
     * @param height ความสูงใหม่
     */
    void onResize(double width, double height);
} 