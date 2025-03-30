package com.vpstycoon.service;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.audio.interfaces.IAudioManager;
import com.vpstycoon.model.common.GameObject;
import com.vpstycoon.model.common.GameState;
import com.vpstycoon.model.company.Company;
import com.vpstycoon.model.company.interfaces.ICompany;
import com.vpstycoon.service.interfaces.IResourceManager;

import javafx.scene.image.Image;

/**
 * คลาสจัดการทรัพยากรในเกม
 */
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

    /**
     * สร้างไดเรกทอรีสำรองข้อมูล
     */
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

    /**
     * สร้างสำเนาไฟล์ที่เสียหาย
     */
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

    /**
     * ล้างแคชทั้งหมด
     */
    @Override
    public void clearCache() {
        imageCache.clear();
        textCache.clear();
    }

    /**
     * ลบไฟล์เซฟเกม
     */
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

    /**
     * ตรวจสอบว่ามีไฟล์เซฟเกมหรือไม่
     */
    @Override
    public boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    /**
     * รับข้อความจากไฟล์
     */
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

    /**
     * รับสถานะเกมปัจจุบัน
     */
    @Override
    public GameState getCurrentState() {
        if (currentState == null) {
            currentState = new GameState(company);
        }
        return currentState;
    }

    /**
     * ตั้งค่าสถานะเกมปัจจุบัน
     */
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

    /**
     * รับบริษัทปัจจุบัน
     */
    @Override
    public ICompany getCompany() {
        return company;
    }

    /**
     * ตั้งค่าบริษัทปัจจุบัน
     */
    @Override
    public void setCompany(ICompany company) {
        this.company = company;
    }

    /**
     * รับตัวจัดการเสียง
     */
    public IAudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * สร้างวัตถุในเกมใหม่
     */
    @Override
    public GameObject createGameObject(String id, String type, int gridX, int gridY) {
        return new GameObject(id, type, gridX, gridY);
    }

    /**
     * ตรวจสอบว่าดนตรีกำลังเล่นอยู่หรือไม่
     */
    public boolean isMusicRunning() {
        return musicRunning;
    }

    /**
     * ตั้งค่าสถานะการเล่นดนตรี
     */
    public void setMusicRunning(boolean running) {
        this.musicRunning = running;
        if (running) {
            audioManager.resumeMusic();
        } else {
            audioManager.pauseMusic();
        }
    }

    /**
     * ตั้งค่าผู้ฟังการโหลดทรัพยากร
     */
    @Override
    public void setResourceLoadingListener(ResourceLoadingListener listener) {
        this.resourceLoadingListener = listener;
    }

    /**
     * ตรวจสอบว่าการโหลดทรัพยากรล่วงหน้าเสร็จสิ้นหรือไม่
     */
    @Override
    public boolean isPreloadComplete() {
        return preloadComplete;
    }

    /**
     * รอการโหลดทรัพยากรล่วงหน้าให้เสร็จสิ้น
     */
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

    /**
     * รับภาพที่โหลดล่วงหน้า
     */
    @Override
    public Image getPreloadedImage(String path) {
        return imageCache.get(path);
    }

    /**
     * โหลดทรัพยากรล่วงหน้า
     */
    @Override
    public void preloadAssets() {
        // ต้องเพิ่มการโหลดทรัพยากรล่วงหน้าในอนาคต
        preloadComplete = true;
    }

    /**
     * รีเซ็ตข้อมูล Messenger
     */
    @Override
    public void resetMessengerData() {
        // ต้องเพิ่มการรีเซ็ตข้อมูล Messenger ในอนาคต
    }

    /**
     * รีเซ็ตเวลาเกม
     */
    @Override
    public void resetGameTime() {
        // ต้องเพิ่มการรีเซ็ตเวลาเกมในอนาคต
    }

    /**
     * รีเซ็ต Rack และ Inventory
     */
    @Override
    public void resetRackAndInventory() {
        // ต้องเพิ่มการรีเซ็ต Rack และ Inventory ในอนาคต
    }

    /**
     * บันทึกสถานะเกม
     */
    @Override
    public void saveGameState(GameState state) {
        // ต้องเพิ่มการบันทึกสถานะเกมในอนาคต
    }

    /**
     * โหลดสถานะเกม
     */
    @Override
    public GameState loadGameState() {
        // ต้องเพิ่มการโหลดสถานะเกมในอนาคต
        return new GameState(company);
    }

    /**
     * รับข้อความจากไฟล์
     */
    public static String getImagePath(String name) {
        return IMAGES_PATH + name;
    }

    /**
     * รับข้อความจากไฟล์
     */
    public static String getSoundPath(String name) {
        return SOUNDS_PATH + name;
    }

    /**
     * รับข้อความจากไฟล์
     */
    public static String getMusicPath(String name) {
        return MUSIC_PATH + name;
    }

    /**
     * รับข้อความจากไฟล์
     */
    public static URL getResource(String path) {
        return ResourceManager.class.getResource(path);
    }

    /**
     * รับข้อความจากไฟล์
     */
    public static String getTextPath(String name) {
        return TEXT_PATH + name;
    }

    /**
     * รับข้อความจากไฟล์
     */
    public static InputStream getResourceAsStream(String path) {
        return ResourceManager.class.getResourceAsStream(path);
    }

    /**
     * รับข้อความจากไฟล์
     */
    @Override
    public void pushNotification(String title, String content) {
        // ต้องเพิ่มการส่งการแจ้งเตือนในอนาคต
    }

    /**
     * รับข้อความจากไฟล์
     */
    @Override
    public void pushMouseNotification(String content) {
        // ต้องเพิ่มการส่งการแจ้งเตือนในอนาคต
    }

    /**
     * รับข้อความจากไฟล์
     */
    @Override
    public void pushCenterNotification(String title, String content) {
        pushCenterNotification(title, content, null);
    }

    /**
     * รับข้อความจากไฟล์
     */
    @Override
    public void pushCenterNotification(String title, String content, String image) {
        // ต้องเพิ่มการส่งการแจ้งเตือนในอนาคต
    }

    /**
     * รับข้อความจากไฟล์
     */
    @Override
    public void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis) {
        // ต้องเพิ่มการส่งการแจ้งเตือนในอนาคต
    }
} 
