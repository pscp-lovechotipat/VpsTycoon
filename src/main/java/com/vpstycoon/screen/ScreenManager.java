package com.vpstycoon.screen;

import com.vpstycoon.view.base.GameScreen;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface ScreenManager {
    void setResolution(ScreenResolution resolution);
    void setFullscreen(boolean fullscreen);
    void applySettings(Stage stage, Scene scene);
    void switchScreen(Node screen);
    
    // เพิ่ม method สำหรับเปลี่ยนหน้าจอที่เป็น GameScreen
    void switchScreen(GameScreen screen);
    
    // เตรียมหน้าจอไว้โดยไม่เปลี่ยนไปทันที (สำหรับการแสดงพร้อมกับหน้า loading)
    void prepareScreen(Node screen);
    
    // เตรียมหน้าจอไว้โดยไม่เปลี่ยนไปทันที (สำหรับ GameScreen)
    void prepareScreen(GameScreen screen);
    
    // เพิ่ม method อัปเดตความละเอียดหน้าจอ
    void updateScreenResolution();
} 