package com.vpstycoon.service;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.audio.interfaces.IAudioManager;
import com.vpstycoon.model.common.GameObject;
import com.vpstycoon.model.common.GameState;
import com.vpstycoon.model.company.Company;
import com.vpstycoon.model.company.interfaces.ICompany;
import com.vpstycoon.service.interfaces.IResourceManager;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ResourceManager implements Serializable, IResourceManager {
    private static final long serialVersionUID = 1L;

    private static final ResourceManager instance = new ResourceManager();

    private static final String IMAGES_PATH = "/images/";
    private static final String SOUNDS_PATH = "/sounds/";
    private static final String MUSIC_PATH = "/music/";
    private static final String TEXT_PATH = "/text/";
    private static final String SAVE_FILE = "savegame.dat";
    private static final String BACKUP_DIR = "backups";

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    private ICompany company;
    private GameState currentState;
    private final IAudioManager audioManager;
    
    private boolean musicRunning = true;
    private boolean preloadComplete = false;
    private final Object preloadLock = new Object();
    
    private ResourceLoadingListener resourceLoadingListener;

    private ResourceManager() {
        this.company = new Company();
        this.audioManager = new AudioManager();
        createBackupDirectory();
        if (currentState == null) {
            currentState = new GameState(this.company);
        }
    }

    public static ResourceManager getInstance() {
        return instance;
    }

    
    private void createBackupDirectory() {
        try {
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }
        } catch (IOException e) {
            System.err.println("ไม่สามารถสร้างไดเรกทอรีสำรองข้อมูล: " + e.getMessage());
        }
    }

    
    private void createCorruptedFileBackup(File originalFile) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backupPath = Paths.get(BACKUP_DIR, "corrupted_" + timestamp + "_" + originalFile.getName());
            Files.copy(originalFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("สร้างสำเนาไฟล์ที่เสียหาย: " + backupPath);
        } catch (IOException e) {
            System.err.println("ไม่สามารถสร้างสำเนาไฟล์ที่เสียหาย: " + e.getMessage());
        }
    }

    
    @Override
    public void clearCache() {
        imageCache.clear();
        textCache.clear();
    }

    
    @Override
    public void deleteSaveFile() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("ลบไฟล์เซฟเกมเรียบร้อยแล้ว");
            } else {
                System.err.println("ไม่สามารถลบไฟล์เซฟเกม");
            }
        }
    }

    
    @Override
    public boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    
    @Override
    public String getText(String path) {
        return textCache.computeIfAbsent(path, k -> {
            try (InputStream is = getResourceAsStream(getTextPath(k))) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                System.err.println("ไม่สามารถอ่านไฟล์ข้อความ " + k + ": " + e.getMessage());
            }
            return "";
        });
    }

    
    @Override
    public GameState getCurrentState() {
        if (currentState == null) {
            currentState = new GameState(company);
        }
        return currentState;
    }

    
    @Override
    public void setCurrentState(GameState state) {
        if (state != null) {
            this.currentState = state;
            if (state.getCompany() != null) {
                this.company = state.getCompany();
            }
        } else {
            System.err.println("พยายามตั้งค่าสถานะเกมเป็น null");
        }
    }

    
    @Override
    public ICompany getCompany() {
        return company;
    }

    
    @Override
    public void setCompany(ICompany company) {
        this.company = company;
    }

    
    public IAudioManager getAudioManager() {
        return audioManager;
    }

    
    @Override
    public GameObject createGameObject(String id, String type, int gridX, int gridY) {
        return new GameObject(id, type, gridX, gridY);
    }

    
    public boolean isMusicRunning() {
        return musicRunning;
    }

    
    public void setMusicRunning(boolean running) {
        this.musicRunning = running;
        if (running) {
            audioManager.resumeMusic();
        } else {
            audioManager.pauseMusic();
        }
    }

    
    @Override
    public void setResourceLoadingListener(ResourceLoadingListener listener) {
        this.resourceLoadingListener = listener;
    }

    
    @Override
    public boolean isPreloadComplete() {
        return preloadComplete;
    }

    
    @Override
    public boolean waitForPreload(long timeoutMs) {
        if (preloadComplete) {
            return true;
        }
        
        synchronized (preloadLock) {
            try {
                preloadLock.wait(timeoutMs);
                return preloadComplete;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    
    @Override
    public Image getPreloadedImage(String path) {
        return imageCache.get(path);
    }

    
    @Override
    public void preloadAssets() {
        
        preloadComplete = true;
    }

    
    @Override
    public void resetMessengerData() {
        
    }

    
    @Override
    public void resetGameTime() {
        
    }

    
    @Override
    public void resetRackAndInventory() {
        
    }

    
    @Override
    public void saveGameState(GameState state) {
        
    }

    
    @Override
    public GameState loadGameState() {
        
        return new GameState(company);
    }

    
    public static String getImagePath(String name) {
        return IMAGES_PATH + name;
    }

    
    public static String getSoundPath(String name) {
        return SOUNDS_PATH + name;
    }

    
    public static String getMusicPath(String name) {
        return MUSIC_PATH + name;
    }

    
    public static URL getResource(String path) {
        return ResourceManager.class.getResource(path);
    }

    
    public static String getTextPath(String name) {
        return TEXT_PATH + name;
    }

    
    public static InputStream getResourceAsStream(String path) {
        return ResourceManager.class.getResourceAsStream(path);
    }

    
    @Override
    public void pushNotification(String title, String content) {
        
    }

    
    @Override
    public void pushMouseNotification(String content) {
        
    }

    
    @Override
    public void pushCenterNotification(String title, String content) {
        pushCenterNotification(title, content, null);
    }

    
    @Override
    public void pushCenterNotification(String title, String content, String image) {
        
    }

    
    @Override
    public void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis) {
        
    }
} 

