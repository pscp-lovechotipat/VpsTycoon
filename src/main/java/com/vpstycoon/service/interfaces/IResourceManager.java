package com.vpstycoon.service.interfaces;

import com.vpstycoon.model.common.GameObject;
import com.vpstycoon.model.common.GameState;
import com.vpstycoon.model.company.interfaces.ICompany;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;

/**
 * อินเตอร์เฟซสำหรับจัดการทรัพยากรในเกม
 */
public interface IResourceManager {
    
    /**
     * อินเตอร์เฟซสำหรับติดตามสถานะการโหลดทรัพยากร
     */
    public interface ResourceLoadingListener {
        /**
         * เรียกเมื่อกำลังโหลดทรัพยากร
         */
        void onResourceLoading(String resourcePath);
        
        /**
         * เรียกเมื่อโหลดทรัพยากรเสร็จสิ้น
         */
        void onResourceLoadingComplete(int totalLoaded);
    }
    
    // การจัดการสถานะเกม
    void saveGameState(GameState state);
    GameState loadGameState();
    GameState getCurrentState();
    void setCurrentState(GameState state);
    
    // การจัดการบริษัท
    ICompany getCompany();
    void setCompany(ICompany company);
    
    // การจัดการการแจ้งเตือน
    void pushNotification(String title, String content);
    void pushMouseNotification(String content);
    void pushCenterNotification(String title, String content);
    void pushCenterNotification(String title, String content, String image);
    void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis);
    
    // การจัดการทรัพยากรภาพ
    Image getPreloadedImage(String path);
    void preloadAssets();
    boolean isPreloadComplete();
    boolean waitForPreload(long timeoutMs);
    void clearCache();
    String getText(String path);
    
    // การจัดการข้อมูลเกม
    void resetGameTime();
    void resetMessengerData();
    void resetRackAndInventory();
    
    // การจัดการไฟล์เซฟเกม
    void deleteSaveFile();
    boolean hasSaveFile();
    
    // การสร้างอ็อบเจกต์ในเกม
    GameObject createGameObject(String id, String type, int gridX, int gridY);
    
    // การจัดการผู้ฟังเหตุการณ์ (Listener)
    void setResourceLoadingListener(ResourceLoadingListener listener);
    
    // Helper methods
    static String getImagePath(String name) { return "/images/" + name; }
    static String getSoundPath(String name) { return "/sounds/" + name; }
    static String getMusicPath(String name) { return "/music/" + name; }
    static String getTextPath(String name) { return "/text/" + name; }
    static URL getResource(String path) { return IResourceManager.class.getResource(path); }
    static InputStream getResourceAsStream(String path) { return IResourceManager.class.getResourceAsStream(path); }
} 
