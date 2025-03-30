package com.vpstycoon.service.interfaces;

import java.io.InputStream;
import java.net.URL;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;

import javafx.scene.image.Image;

public interface IResourceManager {
    
    public interface ResourceLoadingListener {
        void onResourceLoading(String resourcePath);
        void onResourceLoadingComplete(int totalLoaded);
    }
    
    
    void saveGameState(GameState state);
    GameState loadGameState();
    GameState getCurrentState();
    void setCurrentState(GameState state);
    
    
    Company getCompany();
    void setCompany(Company company);
    
    
    void pushNotification(String title, String content);
    void pushMouseNotification(String content);
    void pushCenterNotification(String title, String content);
    void pushCenterNotification(String title, String content, String image);
    void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis);
    
    
    AudioManager getAudioManager();
    boolean isMusicRunning();
    void setMusicRunning(boolean running);
    
    
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
    
    
    static String getImagePath(String name) { return ""; }
    static String getSoundPath(String name) { return ""; }
    static String getMusicPath(String name) { return ""; }
    static String getTextPath(String name) { return ""; }
    static URL getResource(String path) { return null; }
    static InputStream getResourceAsStream(String path) { return null; }
} 
