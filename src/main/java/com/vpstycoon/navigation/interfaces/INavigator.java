package com.vpstycoon.navigation.interfaces;

/**
 * Interface สำหรับการนำทางระหว่างหน้าจอต่างๆ ในแอปพลิเคชัน
 */
public interface INavigator {

    /**
     * นำทางไปยังหน้าเมนูหลัก
     */
    void navigateToMainMenu();

    /**
     * นำทางไปยังหน้าเกม
     */
    void navigateToGame();

    /**
     * นำทางไปยังหน้าตั้งค่า
     */
    void navigateToSettings();

    /**
     * นำทางไปยังหน้า cutscene
     */
    void navigateToCutscene();
    
    /**
     * บันทึกเกมปัจจุบันและออกไปยังเมนูหลัก
     */
    void saveAndExitToMainMenu();
} 