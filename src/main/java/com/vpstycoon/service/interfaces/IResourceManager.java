package com.vpstycoon.service.interfaces;

import com.vpstycoon.model.common.GameObject;
import com.vpstycoon.model.common.GameState;
import com.vpstycoon.model.company.interfaces.ICompany;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;


public interface IResourceManager {
    
    
    public interface ResourceLoadingListener {
        
        void onResourceLoading(String resourcePath);
        
        
        void onResourceLoadingComplete(int totalLoaded);
    }
    
    
    void saveGameState(GameState state);
    GameState loadGameState();
    GameState getCurrentState();
    void setCurrentState(GameState state);
    
    
    ICompany getCompany();
    void setCompany(ICompany company);
    
    
    void pushNotification(String title, String content);
    void pushMouseNotification(String content);
    void pushCenterNotification(String title, String content);
    void pushCenterNotification(String title, String content, String image);
    void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis);
    
    
    Image getPreloadedImage(String path);
    void preloadAssets();
    boolean isPreloadComplete();
    boolean waitForPreload(long timeoutMs);
    void clearCache();
    String getText(String path);
    
    
    void resetGameTime();
    void resetMessengerData();
    void resetRackAndInventory();
    
    
    void deleteSaveFile();
    boolean hasSaveFile();
    
    
    GameObject createGameObject(String id, String type, int gridX, int gridY);
    
    
    void setResourceLoadingListener(ResourceLoadingListener listener);
    
    
    static String getImagePath(String name) { return "/images/" + name; }
    static String getSoundPath(String name) { return "/sounds/" + name; }
    static String getMusicPath(String name) { return "/music/" + name; }
    static String getTextPath(String name) { return "/text/" + name; }
    static URL getResource(String path) { return IResourceManager.class.getResource(path); }
    static InputStream getResourceAsStream(String path) { return IResourceManager.class.getResourceAsStream(path); }
} 

