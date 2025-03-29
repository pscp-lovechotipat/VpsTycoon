package com.vpstycoon.application;

import javafx.application.Application;

// คลาส Bootstrap สำหรับเริ่มต้นแอปพลิเคชัน
// ทำหน้าที่เป็นจุดเริ่มต้นการทำงานของเกม
public class Bootstrap {
    
    // เมธอดหลักที่เรียกเมื่อโปรแกรมเริ่มทำงาน
    public static void main(String[] args) {
        System.out.println("VPS Tycoon เริ่มทำงาน...");
        
        // เรียกใช้ GameApplication ผ่าน JavaFX Application.launch
        Application.launch(GameApplication.class, args);
    }
} 