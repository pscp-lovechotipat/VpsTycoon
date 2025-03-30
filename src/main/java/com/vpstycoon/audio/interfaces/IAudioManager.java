package com.vpstycoon.audio.interfaces;

/**
 * อินเตอร์เฟซสำหรับจัดการเสียงในเกม
 */
public interface IAudioManager {
    
    /**
     * เล่นเสียงเอฟเฟกต์
     * 
     * @param sound ชื่อไฟล์เสียง
     */
    void playSound(String sound);
    
    /**
     * เล่นเสียงเอฟเฟกต์ด้วยความดังที่กำหนด
     * 
     * @param sound ชื่อไฟล์เสียง
     * @param volume ความดัง (0.0 - 1.0)
     */
    void playSound(String sound, double volume);
    
    /**
     * เล่นเพลงพื้นหลัง
     * 
     * @param music ชื่อไฟล์เพลง
     */
    void playMusic(String music);
    
    /**
     * เล่นเพลงพื้นหลังด้วยความดังที่กำหนด
     * 
     * @param music ชื่อไฟล์เพลง
     * @param volume ความดัง (0.0 - 1.0)
     */
    void playMusic(String music, double volume);
    
    /**
     * หยุดเพลงพื้นหลัง
     */
    void stopMusic();
    
    /**
     * หยุดเล่นเพลงชั่วคราว
     */
    void pauseMusic();
    
    /**
     * เล่นเพลงต่อหลังจากหยุดชั่วคราว
     */
    void resumeMusic();
    
    /**
     * ตั้งค่าความดังเสียงเอฟเฟกต์
     * 
     * @param volume ความดัง (0.0 - 1.0)
     */
    void setSoundVolume(double volume);
    
    /**
     * ตั้งค่าความดังเพลงพื้นหลัง
     * 
     * @param volume ความดัง (0.0 - 1.0)
     */
    void setMusicVolume(double volume);
    
    /**
     * รับความดังเสียงเอฟเฟกต์ปัจจุบัน
     * 
     * @return ความดังเสียงเอฟเฟกต์ (0.0 - 1.0)
     */
    double getSoundVolume();
    
    /**
     * รับความดังเพลงพื้นหลังปัจจุบัน
     * 
     * @return ความดังเพลงพื้นหลัง (0.0 - 1.0)
     */
    double getMusicVolume();
    
    /**
     * เปิด/ปิดเสียงทั้งหมด
     * 
     * @param muted true เพื่อปิดเสียง, false เพื่อเปิดเสียง
     */
    void setMuted(boolean muted);
    
    /**
     * ตรวจสอบว่าเสียงถูกปิดอยู่หรือไม่
     * 
     * @return true ถ้าเสียงถูกปิด, false ถ้าเสียงเปิดอยู่
     */
    boolean isMuted();
} 
