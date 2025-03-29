package com.vpstycoon.view.base;

import com.vpstycoon.view.interfaces.IGameScreen;
import javafx.scene.Parent;

/**
 * คลาสพื้นฐานสำหรับหน้าจอเกมทุกหน้า
 */
public abstract class GameScreen implements IGameScreen {
    
    protected Parent root;
    
    /**
     * ดึง root element ของหน้าจอ
     * @return Parent element ที่เป็น root ของหน้าจอ
     */
    @Override
    public Parent getRoot() {
        return root;
    }
    
    /**
     * ถูกเรียกเมื่อหน้าจอถูกแสดง
     */
    @Override
    public abstract void onShow();
    
    /**
     * ถูกเรียกเมื่อหน้าจอถูกซ่อน
     */
    @Override
    public abstract void onHide();
    
    /**
     * ถูกเรียกเมื่อหน้าจอถูกปรับขนาด
     * @param width ความกว้างใหม่
     * @param height ความสูงใหม่
     */
    @Override
    public abstract void onResize(double width, double height);
} 