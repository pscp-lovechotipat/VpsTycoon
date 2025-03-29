package com.vpstycoon.game.resource;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.thread.GameEvent;
import com.vpstycoon.game.thread.GameTimeController;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.thread.RequestGenerator;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;
import com.vpstycoon.ui.game.notification.NotificationController;
import com.vpstycoon.ui.game.notification.NotificationModel;
import com.vpstycoon.ui.game.notification.NotificationView;
import com.vpstycoon.ui.game.notification.center.CenterNotificationController;
import com.vpstycoon.ui.game.notification.center.CenterNotificationModel;
import com.vpstycoon.ui.game.notification.center.CenterNotificationView;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationController;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationModel;
import com.vpstycoon.ui.game.notification.onMouse.MouseNotificationView;
import com.vpstycoon.ui.game.rack.Rack;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceManager implements Serializable {
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

    private Company company = new Company();
    private GameState currentState;
    private Rack rack; // เพิ่ม field สำหรับ Rack
    private GameManager gameManager; // เพิ่ม field สำหรับ GameManager

    private RequestManager requestManager;
    private final AudioManager audioManager;
    private GameTimeController gameTimeController;

    private SkillPointsSystem skillPointsSystem;

    // Interface for rack UI update notifications
    public interface RackUIUpdateListener {
        void onRackUIUpdate();
    }
    
    private List<RackUIUpdateListener> rackUIUpdateListeners = new ArrayList<>();
    
    /**
     * Add a listener to be notified when rack UI should be updated
     * @param listener The listener to add
     */
    public void addRackUIUpdateListener(RackUIUpdateListener listener) {
        if (listener != null && !rackUIUpdateListeners.contains(listener)) {
            rackUIUpdateListeners.add(listener);
        }
    }
    
    /**
     * Remove a rack UI update listener
     * @param listener The listener to remove
     */
    public void removeRackUIUpdateListener(RackUIUpdateListener listener) {
        rackUIUpdateListeners.remove(listener);
    }
    
    /**
     * Notify all registered listeners that rack UI should be updated
     * This is called when rack-related skills are upgraded
     */
    public void notifyRackUIUpdate() {
        for (RackUIUpdateListener listener : new ArrayList<>(rackUIUpdateListeners)) {
            listener.onRackUIUpdate();
        }
    }

    private NotificationModel notificationModel;
    private NotificationView notificationView;
    private NotificationController notificationController;

    private CenterNotificationModel centerNotificationModel;
    private CenterNotificationView centerNotificationView;
    private CenterNotificationController centerNotificationController;

    private MouseNotificationModel mouseNotificationModel;
    private MouseNotificationView mouseNotificationView;
    private MouseNotificationController mouseNotificationController;

    private GameEvent gameEvent;
    private GameplayContentPane gameplayContentPane;

    private boolean musicRunning = true;

    // Performance optimization flags
    private boolean emergencyPerformanceMode = false;
    private long lastPerformanceCheck = 0;
    private static final long PERFORMANCE_CHECK_INTERVAL = 1000; // ms
    
    /**
     * Checks if the application should run in emergency performance mode
     * to reduce stuttering and improve responsiveness
     * @return true if the app is in emergency performance mode
     */
    public boolean isEmergencyPerformanceMode() {
        // Check performance only periodically to avoid overhead
        long now = System.currentTimeMillis();
        if (now - lastPerformanceCheck > PERFORMANCE_CHECK_INTERVAL) {
            lastPerformanceCheck = now;
            
            // Check system memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long allocatedMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            
            // Calculate used memory percentage
            double usedMemoryPct = (double)(allocatedMemory - freeMemory) / maxMemory * 100;
            
            // If memory usage is high, enable emergency mode
            emergencyPerformanceMode = usedMemoryPct > 80;
            
            if (emergencyPerformanceMode) {
                System.out.println("Warning: High memory usage. Enabling emergency performance mode.");
            }
        }
        
        return emergencyPerformanceMode;
    }
    
    /**
     * Force emergency performance mode on or off
     * @param mode true to enable emergency performance mode
     */
    public void setEmergencyPerformanceMode(boolean mode) {
        this.emergencyPerformanceMode = mode;
    }

    // Flag to track preloading status
    private boolean preloadComplete = false;
    private final Object preloadLock = new Object();

    // Resource loading progress listener
    public interface ResourceLoadingListener {
        void onResourceLoading(String resourcePath);
        void onResourceLoadingComplete(int totalLoaded);
    }
    
    private ResourceLoadingListener resourceLoadingListener;
    
    /**
     * Set a listener to receive notifications about resource loading progress
     * @param listener The listener to notify
     */
    public void setResourceLoadingListener(ResourceLoadingListener listener) {
        this.resourceLoadingListener = listener;
    }
    
    /**
     * Notify listener that a resource is being loaded
     * @param resourcePath Path of the resource being loaded
     */
    private void notifyResourceLoading(String resourcePath) {
        if (resourceLoadingListener != null) {
            // Create a copy to avoid modifying the parameter in the lambda
            final String path = resourcePath;
            // Use Platform.runLater to ensure UI updates happen on the JavaFX application thread
            javafx.application.Platform.runLater(() -> {
                resourceLoadingListener.onResourceLoading(path);
            });
        }
    }
    
    /**
     * Notify listener that resource loading is complete
     * @param totalLoaded Total number of resources loaded
     */
    private void notifyResourceLoadingComplete(int totalLoaded) {
        if (resourceLoadingListener != null) {
            // Store the value to avoid capture of mutable variable
            final int total = totalLoaded;
            // Use Platform.runLater to ensure UI updates happen on the JavaFX application thread
            javafx.application.Platform.runLater(() -> {
                resourceLoadingListener.onResourceLoadingComplete(total);
            });
        }
    }

    private ResourceManager() {
        this.company = new Company();
        this.audioManager = new AudioManager();
        this.rack = new Rack(); // สร้าง Rack เริ่มต้นใน ResourceManager
        this.gameManager = GameManager.getInstance(); // เก็บ instance ของ GameManager

        createBackupDirectory();
        if (currentState == null) {
            currentState = new GameState(this.company);
        }
    }

    public static ResourceManager getInstance() {
        return instance;
    }

    public void initializeGameEvent(GameplayContentPane pane) {
        this.gameplayContentPane = pane;
        if (gameEvent == null || !gameEvent.isRunning()) {
            gameEvent = new GameEvent(gameplayContentPane, currentState);
            Thread eventThread = new Thread(gameEvent);
            eventThread.setDaemon(true); // Set as daemon thread
            eventThread.start();
        }
    }

    private void initiaizeSkillPointsSystem() {
        if (skillPointsSystem == null) {
            this.skillPointsSystem = new SkillPointsSystem(this.company);
        }
    }

    private void initiaizeRequestManager() {
        if (requestManager == null) {
            this.requestManager = new RequestManager(this.company);
        }
    }

    private void initiaizeGameTimeController() {
        initiaizeRequestManager();
        if (gameTimeController == null) {
            this.gameTimeController = new GameTimeController(this.company,
                    this.requestManager,
                    this.rack,
                    currentState.getLocalDateTime()
            );
            gameTimeController.getGameTimeManager().getGameTimeMs();
        }
    }

    // Lazy initialization for notification components
    private void initializeNotifications() {
        if (notificationModel == null) {
            this.notificationModel = new NotificationModel();
            this.notificationView = new NotificationView();
            this.notificationView.setAudioManager(this.audioManager);
            this.notificationController = new NotificationController(notificationModel, notificationView);
        }
        if (centerNotificationModel == null) {
            this.centerNotificationModel = new CenterNotificationModel();
            this.centerNotificationView = new CenterNotificationView();
            this.centerNotificationView.setAudioManager(this.audioManager);
            this.centerNotificationController = new CenterNotificationController(centerNotificationModel, centerNotificationView);
        }

        if (mouseNotificationModel == null) {
            this.mouseNotificationModel = new MouseNotificationModel();
            this.mouseNotificationView = new MouseNotificationView();
            this.mouseNotificationView.setAudioManager(this.audioManager);
            this.mouseNotificationController = new MouseNotificationController(mouseNotificationModel, mouseNotificationView);
        }
    }

    // เมธอดสำหรับจัดการทรัพยากร (เช่น รูปภาพ เสียง) คงไว้เหมือนเดิม
    public static Image loadImage(String name) {
        return getInstance().imageCache.computeIfAbsent(name, k -> {
            try (InputStream is = ResourceManager.class.getResourceAsStream(IMAGES_PATH + k)) {
                if (is == null) {
                    throw new RuntimeException("Image not found: " + k);
                }
                return new Image(is);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load image: " + k, e);
            }
        });
    }

    public void saveGameState(GameState state) {
        initiaizeGameTimeController();
        state.setLocalDateTime(gameTimeController.getGameTimeManager().getGameDateTime());
        state.setGameTimeMs(gameTimeController.getGameTimeManager().getGameTimeMs());
        
        // เก็บข้อมูล Free VM ลงใน GameState
        if (this.company != null) {
            System.out.println("กำลังบันทึกข้อมูล Free VM count: " + this.company.getAvailableVMs());
            state.setFreeVmCount(this.company.getAvailableVMs());
        }

        // บันทึกข้อมูล SkillPointsSystem
        if (this.skillPointsSystem != null) {
            System.out.println("กำลังบันทึกข้อมูล Skill Points...");
            state.setSkillLevels(this.skillPointsSystem.getSkillLevelsMap());
        }
        
        // เก็บข้อมูล pendingRequests และ completedRequests ถ้ามี
        try {
            if (requestManager != null) {
                // อัพเดตข้อมูล pendingRequests และ completedRequests ใน GameState ก่อนบันทึก
                if (requestManager.getRequests() != null) {
                    state.setPendingRequests(new ArrayList<>(requestManager.getRequests()));
                    System.out.println("บันทึกข้อมูล pendingRequests: " + requestManager.getRequests().size() + " รายการ");
                }
                
                if (requestManager.getCompletedRequests() != null) {
                    state.setCompletedRequests(new ArrayList<>(requestManager.getCompletedRequests()));
                    System.out.println("บันทึกข้อมูล completedRequests: " + requestManager.getCompletedRequests().size() + " รายการ");
                }
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Requests: " + e.getMessage());
            e.printStackTrace();
        }
        
        // บันทึกข้อมูล Chat History จาก ChatHistoryManager ลงใน GameState (ถ้ามี)
        try {
            ChatHistoryManager chatManager = getChatHistory();
            if (chatManager != null) {
                // บันทึกข้อมูลลงใน GameState โดยตรง (ไม่ต้องเรียก saveChatHistory เพราะเดี๋ยวจะเรียกวนกัน)
                System.out.println("กำลังบันทึกข้อมูล Chat History ลงใน GameState...");
                
                // บันทึกลงใน save.dat ด้วย (ใช้ ChatHistoryManager จัดการ)
                chatManager.saveChatHistory();
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Chat History: " + e.getMessage());
            e.printStackTrace();
        }
        
        // บันทึกข้อมูล Rack
        System.out.println("กำลังบันทึกข้อมูล Rack...");
        if (rack != null) {
            try {
                // เก็บข้อมูลการตั้งค่า Rack
                Map<String, Object> rackConfig = new HashMap<>();
                rackConfig.put("maxRacks", rack.getMaxRacks());
                rackConfig.put("currentRackIndex", rack.getCurrentRackIndex());
                
                // บันทึกรายละเอียดของแต่ละ rack
                List<Map<String, Object>> allRacksData = new ArrayList<>();
                
                // จำนวน Rack ทั้งหมด
                int totalRacks = rack.getMaxRacks();
                
                // เก็บข้อมูล slot ของแต่ละ rack
                List<Integer> slotCounts = new ArrayList<>();
                for (int i = 0; i < totalRacks; i++) {
                    // สร้าง index ปัจจุบันไว้
                    int currentIndex = rack.getCurrentRackIndex();
                    
                    // เปลี่ยน rack ไปที่ index ที่ต้องการเพื่อดึงข้อมูล
                    rack.setRackIndex(i);
                    
                    Map<String, Object> rackData = new HashMap<>();
                    rackData.put("rackIndex", i);
                    rackData.put("maxSlotUnits", rack.getMaxSlotUnits());
                    rackData.put("unlockedSlotUnits", rack.getUnlockedSlotUnits());
                    rackData.put("occupiedSlotUnits", rack.getOccupiedSlotUnits());
                    rackData.put("availableSlotUnits", rack.getAvailableSlotUnits());
                    
                    // เก็บข้อมูล VPS ที่ติดตั้งในแต่ละ rack
                    List<String> rackInstalledVpsIds = new ArrayList<>();
                    for (VPSOptimization vps : rack.getInstalledVPS()) {
                        rackInstalledVpsIds.add(vps.getVpsId());
                        
                        // ตรวจสอบว่า VPS นี้อยู่ใน gameObjects หรือไม่
                        boolean found = false;
                        for (GameObject obj : state.getGameObjects()) {
                            if (obj instanceof VPSOptimization && ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                                found = true;
                                break;
                            }
                        }
                        // ถ้าไม่พบ VPS นี้ใน gameObjects ให้เพิ่มเข้าไป
                        if (!found) {
                            state.addGameObject(vps);
                        }
                    }
                    rackData.put("installedVpsIds", rackInstalledVpsIds);
                    
                    // เพิ่มข้อมูล rack นี้เข้าไปในรายการ
                    allRacksData.add(rackData);
                    
                    // เก็บจำนวน slot ของ rack นี้
                    slotCounts.add(rack.getMaxSlotUnits());
                    
                    // เปลี่ยนกลับไปที่ rack เดิม
                    rack.setRackIndex(currentIndex);
                }
                
                // บันทึกข้อมูลทุก rack
                rackConfig.put("allRacksData", allRacksData);
                rackConfig.put("slotCounts", slotCounts);
                
                // บันทึกรายการ unlockedSlotUnits สำหรับทุก rack
                rackConfig.put("unlockedSlotUnitsList", rack.getUnlockedSlotUnitsList());
                
                // รวบรวม VPS ที่ติดตั้งในทุก rack
                List<String> allInstalledVpsIds = new ArrayList<>();
                for (VPSOptimization vps : rack.getAllInstalledVPS()) {
                    allInstalledVpsIds.add(vps.getVpsId());
                }
                rackConfig.put("installedVpsIds", allInstalledVpsIds);
                
                // บันทึกข้อมูล Rack ลงใน GameState
                state.setRackConfiguration(rackConfig);
                System.out.println("บันทึกข้อมูล Rack สำเร็จ: " + totalRacks + " racks, " + 
                                  allInstalledVpsIds.size() + " VPS ที่ติดตั้ง");
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Rack: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // บันทึกข้อมูล VPS Inventory
        System.out.println("กำลังบันทึกข้อมูล VPS Inventory...");
        try {
            VPSInventory vpsInventory = null;
            
            // ดึง VPSInventory จาก GameplayContentPane (ซึ่งเป็นที่เก็บ VPSInventory หลัก)
            if (gameplayContentPane != null) {
                vpsInventory = gameplayContentPane.getVpsInventory();
                System.out.println("พบ VPSInventory จาก GameplayContentPane: " + 
                    (vpsInventory != null ? vpsInventory.getSize() + " รายการ" : "ไม่พบ"));
            }
            
            // ถ้าไม่พบ VPSInventory จาก GameplayContentPane ให้ค้นหาจาก GameObject
            if (vpsInventory == null || vpsInventory.isEmpty()) {
                vpsInventory = new VPSInventory();
                
                // ค้นหา VPS ที่ยังไม่ได้ติดตั้ง
                for (GameObject obj : state.getGameObjects()) {
                    if (obj instanceof VPSOptimization) {
                        VPSOptimization vps = (VPSOptimization) obj;
                        if (!vps.isInstalled()) {
                            vpsInventory.addVPS(vps.getVpsId(), vps);
                            System.out.println("เพิ่ม VPS ที่ยังไม่ได้ติดตั้งเข้า Inventory: " + vps.getVpsId());
                        }
                    }
                }
            }
            
            // บันทึกข้อมูล VPS ทั้งหมดใน inventory ไปยัง gameObjects ถ้ายังไม่มี
            for (VPSOptimization vps : vpsInventory.getAllVPS()) {
                boolean found = false;
                for (GameObject obj : state.getGameObjects()) {
                    if (obj instanceof VPSOptimization && 
                        ((VPSOptimization) obj).getVpsId() != null && 
                        ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    state.addGameObject(vps);
                    System.out.println("เพิ่ม VPS จาก Inventory เข้า GameObjects: " + vps.getVpsId());
                }
            }
            
            // เก็บข้อมูล VPS Inventory
            Map<String, Object> inventoryData = new HashMap<>();
            List<String> vpsIds = vpsInventory.getAllVPSIds();
            inventoryData.put("vpsIds", vpsIds);
            
            // บันทึกข้อมูลเพิ่มเติมของแต่ละ VPS
            Map<String, Map<String, Object>> vpsDetails = new HashMap<>();
            for (String vpsId : vpsIds) {
                VPSOptimization vps = vpsInventory.getVPS(vpsId);
                Map<String, Object> details = new HashMap<>();
                details.put("vCPUs", vps.getVCPUs());
                details.put("ramInGB", vps.getRamInGB());
                details.put("diskInGB", vps.getDiskInGB());
                details.put("size", vps.getSize().toString());
                details.put("name", vps.getName());
                vpsDetails.put(vpsId, details);
            }
            inventoryData.put("vpsDetails", vpsDetails);
            
            // บันทึกข้อมูล VPS Inventory ลงใน GameState
            state.setVpsInventoryData(inventoryData);
            
            System.out.println("บันทึกข้อมูล VPS Inventory สำเร็จ: " + vpsIds.size() + " VPS ที่ยังไม่ได้ติดตั้ง");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล VPS Inventory: " + e.getMessage());
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(state);
            System.out.println("Game saved successfully to: " + SAVE_FILE);
            this.currentState = state;
            this.company = state.getCompany();
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public GameState loadGameState() {
        GameState state = null;
        File saveFile = new File(SAVE_FILE);
        
        if (!saveFile.exists() || saveFile.length() == 0) {
            System.out.println("ไม่พบไฟล์เซฟเกม หรือไฟล์เซฟว่างเปล่า");
            return new GameState();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            state = (GameState) ois.readObject();
            
            // โหลดข้อมูล Free VM Count
            if (state.getFreeVmCount() > 0) {
                System.out.println("โหลดข้อมูล Free VM Count: " + state.getFreeVmCount());
                if (this.company != null) {
                    this.company.setAvailableVMs(state.getFreeVmCount());
                    System.out.println("อัปเดตข้อมูล Free VM Count ใน Company แล้ว: " + this.company.getAvailableVMs());
                }
            } else {
                // ถ้าไม่มีข้อมูล Free VM Count ให้นับจาก VPS ที่ติดตั้งแล้วใน Rack
                int freeVmCount = 0;
                if (state.getGameObjects() != null) {
                    for (Object obj : state.getGameObjects()) {
                        if (obj instanceof VPSOptimization) {
                            VPSOptimization vps = (VPSOptimization) obj;
                            if (vps.isInstalled()) {
                                freeVmCount += vps.getVms().stream()
                                    .filter(vm -> "Running".equals(vm.getStatus()))
                                    .count();
                            }
                        }
                    }
                    if (freeVmCount > 0) {
                        state.setFreeVmCount(freeVmCount);
                        if (this.company != null) {
                            this.company.setAvailableVMs(freeVmCount);
                        }
                        System.out.println("คำนวณ Free VM Count จาก GameObjects: " + freeVmCount);
                    }
                }
            }
            
            // โหลดข้อมูล Rack
            loadRackDataFromGameState(state);
            
            // Update the current state and company
            this.company = state.getCompany();
            this.currentState = state;
            
            // Reinitialize the SkillPointsSystem with the loaded company and skill levels
            if (this.company != null) {
                // Check if there are saved skill levels
                if (state.getSkillLevels() != null && !state.getSkillLevels().isEmpty()) {
                    System.out.println("โหลดข้อมูล Skill Levels จาก GameState...");
                    this.skillPointsSystem = new SkillPointsSystem(this.company, state.getSkillLevels());
                } else {
                    System.out.println("ไม่พบข้อมูล Skill Levels ใน GameState สร้างใหม่...");
                    this.skillPointsSystem = new SkillPointsSystem(this.company);
                }
            }
            
            System.out.println("โหลดข้อมูลเกมสำเร็จ จาก: " + saveFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูลเกม: " + e.getMessage());
            e.printStackTrace();
            return new GameState();
        }
        
        return state;
    }
    
    /**
     * โหลดข้อมูล Rack จาก GameState ที่โหลดมา
     * @param state GameState ที่ต้องการโหลดข้อมูล Rack จาก
     * @return true หากโหลดข้อมูลสำเร็จ, false หากมีข้อผิดพลาด
     */
    public boolean loadRackDataFromGameState(GameState state) {
        if (state == null) {
            System.out.println("ไม่สามารถโหลดข้อมูล Rack ได้: GameState เป็น null");
            return false;
        }
        
        // รับข้อมูลการตั้งค่า Rack จาก GameState
        Map<String, Object> rackConfig = state.getRackConfiguration();
        if (rackConfig == null || rackConfig.isEmpty()) {
            System.out.println("ไม่พบข้อมูลการตั้งค่า Rack ใน GameState");
            
            // ถ้าไม่มีข้อมูล Rack ให้สร้าง Rack เริ่มต้น
            if (this.rack == null) {
                this.rack = new Rack();
                this.rack.addRack(10); // สร้าง rack พร้อม 10 slots
                System.out.println("สร้าง Rack เริ่มต้นเรียบร้อยแล้ว");
            }
            return false;
        }
        
        // สร้าง VPSInventory ถ้ายังไม่มี
        VPSInventory inventory = new VPSInventory();
        
        // สร้าง Rack ใหม่
        if (this.rack == null) {
            this.rack = new Rack();
        } else {
            // ล้างข้อมูล Rack เดิม เพื่อเตรียมโหลดข้อมูลใหม่
            this.rack = new Rack();
        }
        
        try {
            // ข้อมูลพื้นฐานของ Rack
            int maxRacks = rackConfig.containsKey("maxRacks") ? (Integer) rackConfig.get("maxRacks") : 0;
            int currentRackIndex = rackConfig.containsKey("currentRackIndex") ? (Integer) rackConfig.get("currentRackIndex") : 0;
            
            // ตรวจสอบว่ามีข้อมูลรายละเอียดของแต่ละ rack หรือไม่
            List<Map<String, Object>> allRacksData = null;
            if (rackConfig.containsKey("allRacksData")) {
                allRacksData = (List<Map<String, Object>>) rackConfig.get("allRacksData");
                System.out.println("พบข้อมูลรายละเอียดของ Rack: " + allRacksData.size() + " racks");
            }
            
            // ดึงข้อมูลขนาดของแต่ละ rack (จำนวน slot)
            List<Integer> slotCounts = null;
            if (rackConfig.containsKey("slotCounts")) {
                slotCounts = (List<Integer>) rackConfig.get("slotCounts");
                System.out.println("พบข้อมูลขนาด slot ของ Rack: " + slotCounts);
            }
            
            // สร้าง rack ในจำนวนที่บันทึกไว้
            for (int i = 0; i < maxRacks; i++) {
                int slotCount = 10; // ค่าเริ่มต้น
                if (slotCounts != null && i < slotCounts.size()) {
                    slotCount = slotCounts.get(i);
                }
                // สร้าง rack ใหม่
                this.rack.addRack(slotCount);
                System.out.println("สร้าง Rack #" + (i+1) + " พร้อม " + slotCount + " slots");
            }
            
            // ถ้ามีข้อมูล unlockedSlotUnitsList ให้ตั้งค่า
            if (rackConfig.containsKey("unlockedSlotUnitsList")) {
                List<Integer> unlockedList = (List<Integer>) rackConfig.get("unlockedSlotUnitsList");
                if (unlockedList != null && !unlockedList.isEmpty()) {
                    this.rack.setUnlockedSlotUnitsList(unlockedList);
                    System.out.println("ตั้งค่า unlockedSlotUnitsList: " + unlockedList);
                }
            }
            
            // โหลดข้อมูล VPS ที่ติดตั้งใน Rack
            if (allRacksData != null && !allRacksData.isEmpty()) {
                // โหลดข้อมูลรายละเอียดของแต่ละ rack
                for (Map<String, Object> rackData : allRacksData) {
                    int rackIndex = (Integer) rackData.get("rackIndex");
                    List<String> rackInstalledVpsIds = (List<String>) rackData.get("installedVpsIds");
                    
                    if (rackInstalledVpsIds != null && !rackInstalledVpsIds.isEmpty()) {
                        // เปลี่ยนไปที่ rack ที่ต้องการ
                        this.rack.setRackIndex(rackIndex);
                        
                        // ติดตั้ง VPS ใน rack นี้
                        for (String vpsId : rackInstalledVpsIds) {
                            boolean found = false;
                            // ค้นหา VPS จาก gameObjects
                            for (GameObject obj : state.getGameObjects()) {
                                if (obj instanceof VPSOptimization) {
                                    VPSOptimization vps = (VPSOptimization) obj;
                                    if (vps.getVpsId().equals(vpsId)) {
                                        // ติดตั้ง VPS ใน Rack
                                        if (this.rack.installVPS(vps)) {
                                            found = true;
                                            System.out.println("ติดตั้ง VPS " + vpsId + " ใน Rack #" + (rackIndex + 1));
                                        } else {
                                            System.out.println("ไม่สามารถติดตั้ง VPS " + vpsId + " ใน Rack #" + (rackIndex + 1) + " ได้");
                                        }
                                        break;
                                    }
                                }
                            }
                            
                            if (!found) {
                                System.out.println("ไม่พบ VPS " + vpsId + " ใน GameObjects");
                            }
                        }
                    }
                }
            } else if (rackConfig.containsKey("installedVpsIds")) {
                // ใช้ข้อมูลเก่า (ถ้าไม่มีข้อมูลรายละเอียดของแต่ละ rack)
                List<String> installedVpsIds = (List<String>) rackConfig.get("installedVpsIds");
                
                if (installedVpsIds != null && !installedVpsIds.isEmpty()) {
                    // ติดตั้ง VPS ใน rack แรก (ค่าเริ่มต้น)
                    this.rack.setRackIndex(0);
                    
                    for (String vpsId : installedVpsIds) {
                        boolean found = false;
                        // ค้นหา VPS จาก gameObjects
                        for (GameObject obj : state.getGameObjects()) {
                            if (obj instanceof VPSOptimization) {
                                VPSOptimization vps = (VPSOptimization) obj;
                                if (vps.getVpsId().equals(vpsId)) {
                                    // ติดตั้ง VPS ใน Rack
                                    if (this.rack.installVPS(vps)) {
                                        found = true;
                                        System.out.println("ติดตั้ง VPS " + vpsId + " ใน Rack ตามข้อมูลเก่า");
                                    } else {
                                        System.out.println("ไม่สามารถติดตั้ง VPS " + vpsId + " ใน Rack ตามข้อมูลเก่าได้");
                                    }
                                    break;
                                }
                            }
                        }
                        
                        if (!found) {
                            System.out.println("ไม่พบ VPS " + vpsId + " ใน GameObjects");
                        }
                    }
                }
            }
            
            // กลับไปที่ index ที่ถูกต้อง
            if (currentRackIndex >= 0 && currentRackIndex < maxRacks) {
                this.rack.setRackIndex(currentRackIndex);
                System.out.println("ตั้งค่า currentRackIndex เป็น " + currentRackIndex);
            }
            
            System.out.println("โหลดข้อมูล Rack สำเร็จ: " + this.rack.getMaxRacks() + " racks, " + 
                              this.rack.getAllInstalledVPS().size() + " VPS ที่ติดตั้ง");
            return true;
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูล Rack: " + e.getMessage());
            e.printStackTrace();
            
            // กรณีเกิดข้อผิดพลาด ให้สร้าง Rack เริ่มต้น
            this.rack = new Rack();
            this.rack.addRack(10); // สร้าง rack พร้อม 10 slots
            System.out.println("เกิดข้อผิดพลาด สร้าง Rack เริ่มต้นแทน");
            return false;
        }
    }

    // Notification methods
    public void pushNotification(String title, String content) {
        initializeNotifications();
        notificationModel.addNotification(new NotificationModel.Notification(title, content));
        notificationView.addNotificationPane(title, content);
    }

    public void pushMouseNotification(String content) {
        initializeNotifications();
        mouseNotificationController.addNotification(content);
    }

    public void pushCenterNotification(String title, String content) {
        initializeNotifications();
        centerNotificationController.push(title, content);
    }

    public void pushCenterNotification(String title, String content, String image) {
        initializeNotifications();
        centerNotificationController.push(title, content, image);
    }

    /**
     * แสดงการแจ้งเตือนแบบกลางจอที่จะหายไปเองอัตโนมัติหลังจากเวลาที่กำหนด
     * 
     * @param title หัวข้อ notification
     * @param content เนื้อหา notification
     * @param image รูปภาพประกอบ
     * @param autoCloseMillis เวลาในหน่วย millisecond ที่จะปิด notification อัตโนมัติ
     */
    public void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis) {
        initializeNotifications();
        centerNotificationController.pushAutoClose(title, content, image, autoCloseMillis);
    }

    // Notification getters
    public NotificationModel getNotificationModel() {
        initializeNotifications();
        return notificationModel;
    }

    public NotificationView getNotificationView() {
        initializeNotifications();
        return notificationView;
    }

    public NotificationController getNotificationController() {
        initializeNotifications();
        return notificationController;
    }

    public CenterNotificationModel getCenterNotificationModel() {
        initializeNotifications();
        return centerNotificationModel;
    }

    public CenterNotificationView getCenterNotificationView() {
        initializeNotifications();
        return centerNotificationView;
    }

    public CenterNotificationController getCenterNotificationController() {
        initializeNotifications();
        return centerNotificationController;
    }

    public MouseNotificationModel getMouseNotificationModel() {
        initializeNotifications();
        return mouseNotificationModel;
    }

    public MouseNotificationView getMouseNotificationView() {
        initializeNotifications();
        return mouseNotificationView;
    }

    public MouseNotificationController getMouseNotificationController() {
        initializeNotifications();
        return mouseNotificationController;
    }

    public GameEvent getGameEvent() {
        return gameEvent;
    }

    // Getter และ Setter สำหรับ Rack
    public Rack getRack() {
        return rack;
    }

    public void setRack(Rack rack) {
        this.rack = rack;
    }

    public RequestManager getRequestManager() {
        initiaizeRequestManager();
        return requestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public SkillPointsSystem getSkillPointsSystem() {
        initiaizeSkillPointsSystem();
        return skillPointsSystem;
    }

    public GameTimeManager getGameTimeManager() {
        return gameTimeController.getGameTimeManager();
    }

    public GameTimeController getGameTimeController() {
        initiaizeGameTimeController();
        return gameTimeController;
    }

    /**
     * Get the request generator from GameManager
     * @return The request generator
     */
    public RequestGenerator getRequestGenerator() {
        return getGameManager().getRequestGenerator();
    }

    // เมธอดอื่นๆ คงเดิม
    public void deleteSaveFile() {
        File saveFile = new File(SAVE_FILE);
        if (saveFile.exists()) {
            boolean deleted = saveFile.delete();
            if (deleted) {
                System.out.println("Deleted game save: " + saveFile.getAbsolutePath());
            } else {
                System.err.println("Failed to delete game save: " + saveFile.getAbsolutePath());
            }
        }
    }

    public boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    private void createBackupDirectory() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }
    }

    private void createCorruptedFileBackup(File originalFile) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File(BACKUP_DIR + "/corrupted_save_" + timestamp + ".bak");
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error backing up corrupted save: " + e.getMessage());
        }
    }

    public void clearCache() {
        imageCache.clear();
        textCache.clear();
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

    public String getText(String path) {
        return textCache.computeIfAbsent(path, k -> {
            try (InputStream is = getClass().getResourceAsStream("/text/" + k)) {
                if (is == null) {
                    throw new RuntimeException("Text file not found: " + k);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load text: " + k, e);
            }
        });
    }

    public GameState getCurrentState() {
        if (currentState == null) {
            currentState = new GameState(this.company);
        }
        return currentState;
    }

    public void setCurrentState(GameState state) {
        this.currentState = state;
        this.company = state.getCompany();
        
        // Reinitialize SkillPointsSystem with the loaded company and skill levels
        if (this.company != null) {
            if (state.getSkillLevels() != null && !state.getSkillLevels().isEmpty()) {
                this.skillPointsSystem = new SkillPointsSystem(this.company, state.getSkillLevels());
            } else {
                this.skillPointsSystem = new SkillPointsSystem(this.company);
            }
        }
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * ดึงข้อมูล ChatHistoryManager
     * @return ChatHistoryManager instance
     */
    public ChatHistoryManager getChatHistory() {
        return ChatHistoryManager.getInstance();
    }

    public GameObject createGameObject(String id, String type, int gridX, int gridY) {
        return new GameObject(id, type, gridX, gridY);
    }

    public boolean isMusicRunning() {
        return musicRunning;
    }

    public void setMusicRunning(boolean running) {
        this.musicRunning = running;
    }

    /**
     * Preload common assets to improve performance during transitions
     */
    public void preloadAssets() {
        System.out.println("===== เริ่มโหลดทรัพยากรของเกม =====");
        preloadComplete = false;
        
        // List of resources to preload
        final String[] imagesToPreload = {
            "/images/rooms/room.gif",
            "/images/Moniter/MoniterF2.png",
            "/images/servers/server2.gif",
            "/images/Object/Keroro.png",
            "/images/Object/MusicboxOn.gif",
            "/images/Object/MusicboxOff.png",
            "/images/Object/Table.png"
        };
        
        final String[] soundsToPreload = {
            "hover.wav",
            "click.wav",
            "click_app.wav",
            "server.mp3"
        };
        
        // Run in a separate thread to avoid blocking the UI
        Thread preloadThread = new Thread(() -> {
            try {
                // Preload common images used in transitions
                for (String imagePath : imagesToPreload) {
                    System.out.println("กำลังโหลด: " + imagePath);
                    notifyResourceLoading(imagePath);
                    preloadImage(imagePath);
                    
                    // Small delay to make progress visible and avoid UI thread overload
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // Preload common sound effects
                for (String soundPath : soundsToPreload) {
                    System.out.println("กำลังโหลดเสียง: " + soundPath);
                    notifyResourceLoading("เสียง: " + soundPath);
                    audioManager.preloadSoundEffect(soundPath);
                    
                    // Small delay to make progress visible and avoid UI thread overload
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                int totalResources = imagesToPreload.length + soundsToPreload.length;
                System.out.println("===== โหลดทรัพยากรของเกมเสร็จสมบูรณ์ =====");
                System.out.println("จำนวนทรัพยากรที่โหลด: " + totalResources);
                
                notifyResourceLoadingComplete(totalResources);
                
                synchronized(preloadLock) {
                    preloadComplete = true;
                    preloadLock.notifyAll();
                }
            } catch (Exception e) {
                System.err.println("Error preloading assets: " + e.getMessage());
                e.printStackTrace();
                
                synchronized(preloadLock) {
                    preloadComplete = true; // Mark as complete even on error so app doesn't hang
                    preloadLock.notifyAll();
                }
            }
        });
        
        preloadThread.setDaemon(true);
        preloadThread.start();
    }
    
    /**
     * Wait for preloading to complete
     * @param timeoutMs maximum time to wait in milliseconds, or 0 for no timeout
     * @return true if preloading completed, false if timed out
     */
    public boolean waitForPreload(long timeoutMs) {
        if (preloadComplete) {
            return true;
        }
        
        synchronized(preloadLock) {
            if (!preloadComplete) {
                try {
                    if (timeoutMs > 0) {
                        preloadLock.wait(timeoutMs);
                    } else {
                        preloadLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        return preloadComplete;
    }
    
    /**
     * Check if preloading is complete
     * @return true if preloading is complete
     */
    public boolean isPreloadComplete() {
        return preloadComplete;
    }
    
    /**
     * Preload an image and store it in the cache
     * @param path Path to the image resource
     * @return The loaded image
     */
    private Image preloadImage(String path) {
        if (!imageCache.containsKey(path)) {
            try {
                // Load with background loading enabled
                Image image = new Image(path, true);
                imageCache.put(path, image);
                return image;
            } catch (Exception e) {
                System.err.println("Error loading image " + path + ": " + e.getMessage());
            }
        }
        return imageCache.get(path);
    }
    
    /**
     * Get a preloaded image from the cache
     * @param path Path to the image resource
     * @return The cached image or null if not found
     */
    public Image getPreloadedImage(String path) {
        return imageCache.get(path);
    }

    /**
     * รีเซ็ตข้อมูล messenger ทั้งหมด
     */
    public void resetMessengerData() {
        try {
            System.out.println("กำลังรีเซ็ตข้อมูล Messenger และประวัติแชท...");
            
            // ล้างข้อมูลใน ChatHistoryManager
            ChatHistoryManager chatManager = getChatHistory();
            if (chatManager != null) {
                chatManager.resetAllChatData();
            }
            
            // รีเซ็ต ChatHistoryManager เพื่อสร้าง instance ใหม่
            ChatHistoryManager.resetInstance();
            
            // รีเซ็ต RequestManager ถ้ามี
            if (requestManager != null) {
                requestManager.resetRequests();
            } else {
                System.out.println("ยังไม่มี RequestManager");
                // สร้าง RequestManager ใหม่ถ้าจำเป็น
                initiaizeRequestManager();
                if (requestManager != null) {
                    requestManager.resetRequests();
                }
            }
            
            System.out.println("รีเซ็ตข้อมูล Messenger และประวัติแชทเรียบร้อย");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ตข้อมูล Messenger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * รีเซ็ตเวลาของเกมให้กลับไปที่ค่าเริ่มต้น
     */
    public void resetGameTime() {
        try {
            System.out.println("กำลังรีเซ็ตเวลาเกม...");
            
            // หยุดเวลาก่อนถ้า GameTimeController มีอยู่
            if (gameTimeController != null) {
                // ใช้ resetTime ของ GameTimeController ซึ่งจะเรียก resetTime ของ GameTimeManager ด้วย
                LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                gameTimeController.resetTime(startTime);
                System.out.println("รีเซ็ตเวลาด้วย GameTimeController เรียบร้อย");
            } else {
                System.out.println("ไม่พบ GameTimeController จำเป็นต้องสร้างใหม่");
                
                // สร้าง GameTimeController ใหม่ถ้าจำเป็น
                if (currentState != null) {
                    // รีเซ็ตเวลาใน GameState กลับเป็นค่าเริ่มต้น
                    LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                    currentState.setLocalDateTime(startTime);
                    currentState.setGameTimeMs(0);
                    System.out.println("รีเซ็ตเวลาใน GameState เป็น: " + startTime);
                    
                    // สร้าง GameTimeController ใหม่
                    initiaizeGameTimeController();
                    System.out.println("สร้าง GameTimeController ใหม่");
                } else {
                    System.out.println("ไม่พบ currentState จึงไม่สามารถรีเซ็ตเวลาได้");
                }
            }
            
            System.out.println("รีเซ็ตเวลาเกมเรียบร้อย");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ตเวลาเกม: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * รีเซ็ต Rack และ VPSInventory เมื่อเริ่มเกมใหม่
     */
    public void resetRackAndInventory() {
        try {
            System.out.println("กำลังรีเซ็ต Rack และ VPSInventory...");
            // สร้าง Rack ใหม่
            this.rack = new Rack();
            System.out.println("รีเซ็ต Rack เรียบร้อย");
            
            // แจ้งให้ listeners รู้ว่ามีการอัพเดท Rack
            notifyRackUIUpdate();
            
            // รีเซ็ตทักษะทั้งหมด
            if (skillPointsSystem != null) {
                skillPointsSystem.resetSkills();
                System.out.println("รีเซ็ตทักษะทั้งหมดเรียบร้อย");
            } else {
                System.out.println("ยังไม่มี skillPointsSystem จึงไม่สามารถรีเซ็ตทักษะได้");
                initiaizeSkillPointsSystem();
                if (skillPointsSystem != null) {
                    skillPointsSystem.resetSkills();
                    System.out.println("สร้างและรีเซ็ตทักษะเรียบร้อย");
                }
            }
            
            // รีเซ็ตเวลาเกม
            resetGameTime();
            
            // รีเซ็ตข้อมูล Messenger
            resetMessengerData();
            
            System.out.println("รีเซ็ตระบบคลังสินค้า VPS เรียบร้อย");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ต Rack และ VPSInventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * รับ instance ของ GameManager
     * @return GameManager instance
     */
    public GameManager getGameManager() {
        if (gameManager == null) {
            gameManager = GameManager.getInstance();
        }
        return gameManager;
    }
    
    /**
     * เซ็ต instance ของ GameManager (ใช้สำหรับการรีเซ็ตระบบ)
     * @param manager GameManager instance ใหม่
     */
    public void setGameManager(GameManager manager) {
        this.gameManager = manager;
    }
}