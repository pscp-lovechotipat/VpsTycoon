package com.vpstycoon.game.resource;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.RequestManager;
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
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceManager implements Serializable {

    private static final ResourceManager instance = new ResourceManager();

    private static final String IMAGES_PATH = "/images/";
    private static final String SOUNDS_PATH = "/sounds/";
    private static final String MUSIC_PATH = "/music/";
    private static final String TEXT_PATH = "/text/";
    private static final String GAME_FOLDER = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "VpsTycoon";
    private static final String SAVE_FILE = GAME_FOLDER + File.separator + "savegame.dat";
    private static final String BACKUP_DIR = GAME_FOLDER + File.separator + "backups";

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private final Map<String, String> textCache = new ConcurrentHashMap<>();

    private Company company = new Company();
    private GameState currentState;
    private Rack rack; 
    private GameManager gameManager; 

    private RequestManager requestManager;
    private RequestGenerator requestGenerator;
    private final AudioManager audioManager;
    private GameTimeController gameTimeController;

    private SkillPointsSystem skillPointsSystem;

    public interface RackUIUpdateListener {
        void onRackUIUpdate();
    }
    
    private List<RackUIUpdateListener> rackUIUpdateListeners = new ArrayList<>();
    
    public void addRackUIUpdateListener(RackUIUpdateListener listener) {
        if (listener != null && !rackUIUpdateListeners.contains(listener)) {
            rackUIUpdateListeners.add(listener);
        }
    }
    
    public void removeRackUIUpdateListener(RackUIUpdateListener listener) {
        rackUIUpdateListeners.remove(listener);
    }
    
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

    private boolean emergencyPerformanceMode = false;
    private long lastPerformanceCheck = 0;
    private static final long PERFORMANCE_CHECK_INTERVAL = 1000; 
    
    public boolean isEmergencyPerformanceMode() {
        long now = System.currentTimeMillis();
        if (now - lastPerformanceCheck > PERFORMANCE_CHECK_INTERVAL) {
            lastPerformanceCheck = now;
            
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long allocatedMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            
            double usedMemoryPct = (double)(allocatedMemory - freeMemory) / maxMemory * 100;
            
            emergencyPerformanceMode = usedMemoryPct > 80;
            
            if (emergencyPerformanceMode) {
                System.out.println("Warning: High memory usage. Enabling emergency performance mode.");
            }
        }
        
        return emergencyPerformanceMode;
    }
    
    public void setEmergencyPerformanceMode(boolean mode) {
        this.emergencyPerformanceMode = mode;
    }

    private boolean preloadComplete = false;
    private final Object preloadLock = new Object();

    public interface ResourceLoadingListener {
        void onResourceLoading(String resourcePath);
        void onResourceLoadingComplete(int totalLoaded);
    }
    
    private ResourceLoadingListener resourceLoadingListener;
    
    public void setResourceLoadingListener(ResourceLoadingListener listener) {
        this.resourceLoadingListener = listener;
    }
    
    private void notifyResourceLoading(String resourcePath) {
        if (resourceLoadingListener != null) {
            final String path = resourcePath;
            
            javafx.application.Platform.runLater(() -> {
                resourceLoadingListener.onResourceLoading(path);
            });
        }
    }
    
    private void notifyResourceLoadingComplete(int totalLoaded) {
        if (resourceLoadingListener != null) {
            final int total = totalLoaded;
            
            javafx.application.Platform.runLater(() -> {
                resourceLoadingListener.onResourceLoadingComplete(total);
            });
        }
    }

    private ResourceManager() {
        this.company = new Company();
        this.audioManager = new AudioManager();
        this.rack = new Rack(); 
        this.gameManager = GameManager.getInstance(); 

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
            eventThread.setDaemon(true); 
            eventThread.start();
        }
    }

    private void initiaizeSkillPointsSystem() {
        if (skillPointsSystem == null) {
            this.skillPointsSystem = new SkillPointsSystem(this.company);
        }
    }

    private void initiaizeGameTimeController() {
        if (gameTimeController == null) {
            System.out.println("กำลังสร้าง GameTimeController ใหม่...");
            
            
            if (requestManager == null) {
                try {
                    System.out.println("สร้าง RequestManager ก่อนเริ่มต้น GameTimeController");
                    this.requestManager = new com.vpstycoon.game.manager.RequestManager(this.company);
                } catch (Exception e) {
                    System.err.println("ไม่สามารถสร้าง RequestManager: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            this.gameTimeController = new GameTimeController(this.company,
                    this.requestManager,
                    this.rack,
                    currentState.getLocalDateTime()
            );
            
            System.out.println("เริ่มการทำงานของ GameTimeController หลังจากสร้างใหม่");
            gameTimeController.startTime();
        }
    }

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
        
        if (this.company != null) {
            System.out.println("กำลังบันทึกข้อมูล Free VM count: " + this.company.getAvailableVMs());
            state.setFreeVmCount(this.company.getAvailableVMs());
        }

        if (this.skillPointsSystem != null) {
            System.out.println("กำลังบันทึกข้อมูล Skill Points...");
            state.setSkillLevels(this.skillPointsSystem.getSkillLevelsMap());
        }
        
        try {
            if (requestManager != null) {
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
        
        try {
            ChatHistoryManager chatManager = getChatHistory();
            if (chatManager != null) {
                System.out.println("กำลังบันทึกข้อมูล Chat History ลงใน GameState...");
                
                chatManager.saveChatHistory();
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Chat History: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("กำลังบันทึกข้อมูล Rack...");
        if (rack != null) {
            try {
                Map<String, Object> rackConfig = new HashMap<>();
                rackConfig.put("maxRacks", rack.getMaxRacks());
                rackConfig.put("currentRackIndex", rack.getCurrentRackIndex());
                
                List<Map<String, Object>> allRacksData = new ArrayList<>();
                
                int totalRacks = rack.getMaxRacks();
                
                List<Integer> slotCounts = new ArrayList<>();
                for (int i = 0; i < totalRacks; i++) {
                    int currentIndex = rack.getCurrentRackIndex();
                    
                    rack.setRackIndex(i);
                    
                    Map<String, Object> rackData = new HashMap<>();
                    rackData.put("rackIndex", i);
                    rackData.put("maxSlotUnits", rack.getMaxSlotUnits());
                    rackData.put("unlockedSlotUnits", rack.getUnlockedSlotUnits());
                    rackData.put("occupiedSlotUnits", rack.getOccupiedSlotUnits());
                    rackData.put("availableSlotUnits", rack.getAvailableSlotUnits());
                    
                    List<String> rackInstalledVpsIds = new ArrayList<>();
                    for (VPSOptimization vps : rack.getInstalledVPS()) {
                        rackInstalledVpsIds.add(vps.getVpsId());
                        
                        boolean found = false;
                        for (GameObject obj : state.getGameObjects()) {
                            if (obj instanceof VPSOptimization && ((VPSOptimization) obj).getVpsId().equals(vps.getVpsId())) {
                                found = true;
                                break;
                            }
                        }
                        
                        if (!found) {
                            state.addGameObject(vps);
                        }
                    }
                    rackData.put("installedVpsIds", rackInstalledVpsIds);
                    
                    allRacksData.add(rackData);
                    
                    slotCounts.add(rack.getMaxSlotUnits());
                    
                    rack.setRackIndex(currentIndex);
                }
                
                rackConfig.put("allRacksData", allRacksData);
                rackConfig.put("slotCounts", slotCounts);
                
                rackConfig.put("unlockedSlotUnitsList", rack.getUnlockedSlotUnitsList());
                
                List<String> allInstalledVpsIds = new ArrayList<>();
                for (VPSOptimization vps : rack.getAllInstalledVPS()) {
                    allInstalledVpsIds.add(vps.getVpsId());
                }
                rackConfig.put("installedVpsIds", allInstalledVpsIds);
                
                state.setRackConfiguration(rackConfig);
                System.out.println("บันทึกข้อมูล Rack สำเร็จ: " + totalRacks + " racks, " + 
                                  allInstalledVpsIds.size() + " VPS ที่ติดตั้ง");
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการบันทึกข้อมูล Rack: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("กำลังบันทึกข้อมูล VPS Inventory...");
        try {
            VPSInventory vpsInventory = null;
            
            if (gameplayContentPane != null) {
                vpsInventory = gameplayContentPane.getVpsInventory();
                System.out.println("พบ VPSInventory จาก GameplayContentPane: " + 
                    (vpsInventory != null ? vpsInventory.getSize() + " รายการ" : "ไม่พบ"));
            }
            
            if (vpsInventory == null || vpsInventory.isEmpty()) {
                vpsInventory = new VPSInventory();
                
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
            
            Map<String, Object> inventoryData = new HashMap<>();
            List<String> vpsIds = vpsInventory.getAllVPSIds();
            inventoryData.put("vpsIds", vpsIds);
            
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
            
            if (state.getFreeVmCount() > 0) {
                System.out.println("โหลดข้อมูล Free VM Count: " + state.getFreeVmCount());
                if (this.company != null) {
                    this.company.setAvailableVMs(state.getFreeVmCount());
                    System.out.println("อัปเดตข้อมูล Free VM Count ใน Company แล้ว: " + this.company.getAvailableVMs());
                }
            } else {
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
            
            loadRackDataFromGameState(state);
            
            this.company = state.getCompany();
            this.currentState = state;
            
            if (this.company != null) {
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
    
    public boolean loadRackDataFromGameState(GameState state) {
        if (state == null) {
            System.out.println("ไม่สามารถโหลดข้อมูล Rack ได้: GameState เป็น null");
            return false;
        }
        
        Map<String, Object> rackConfig = state.getRackConfiguration();
        if (rackConfig == null || rackConfig.isEmpty()) {
            System.out.println("ไม่พบข้อมูลการตั้งค่า Rack ใน GameState");
            
            if (this.rack == null) {
                this.rack = new Rack();
                this.rack.addRack(10); 
                System.out.println("สร้าง Rack เริ่มต้นเรียบร้อยแล้ว");
            }
            return false;
        }
        
        VPSInventory inventory = new VPSInventory();
        
        if (this.rack == null) {
            this.rack = new Rack();
        } else {
            this.rack = new Rack();
        }
        
        try {
            int maxRacks = rackConfig.containsKey("maxRacks") ? (Integer) rackConfig.get("maxRacks") : 0;
            int currentRackIndex = rackConfig.containsKey("currentRackIndex") ? (Integer) rackConfig.get("currentRackIndex") : 0;
            
            List<Map<String, Object>> allRacksData = null;
            if (rackConfig.containsKey("allRacksData")) {
                allRacksData = (List<Map<String, Object>>) rackConfig.get("allRacksData");
                System.out.println("พบข้อมูลรายละเอียดของ Rack: " + allRacksData.size() + " racks");
            }
            
            List<Integer> slotCounts = null;
            if (rackConfig.containsKey("slotCounts")) {
                slotCounts = (List<Integer>) rackConfig.get("slotCounts");
                System.out.println("พบข้อมูลขนาด slot ของ Rack: " + slotCounts);
            }
            
            for (int i = 0; i < maxRacks; i++) {
                int slotCount = 10; 
                if (slotCounts != null && i < slotCounts.size()) {
                    slotCount = slotCounts.get(i);
                }
                
                this.rack.addRack(slotCount);
                System.out.println("สร้าง Rack #" + (i+1) + " พร้อม " + slotCount + " slots");
            }
            
            if (rackConfig.containsKey("unlockedSlotUnitsList")) {
                List<Integer> unlockedList = (List<Integer>) rackConfig.get("unlockedSlotUnitsList");
                if (unlockedList != null && !unlockedList.isEmpty()) {
                    this.rack.setUnlockedSlotUnitsList(unlockedList);
                    System.out.println("ตั้งค่า unlockedSlotUnitsList: " + unlockedList);
                }
            }
            
            if (allRacksData != null && !allRacksData.isEmpty()) {
                for (Map<String, Object> rackData : allRacksData) {
                    int rackIndex = (Integer) rackData.get("rackIndex");
                    List<String> rackInstalledVpsIds = (List<String>) rackData.get("installedVpsIds");
                    
                    if (rackInstalledVpsIds != null && !rackInstalledVpsIds.isEmpty()) {
                        this.rack.setRackIndex(rackIndex);
                        
                        for (String vpsId : rackInstalledVpsIds) {
                            boolean found = false;
                            
                            for (GameObject obj : state.getGameObjects()) {
                                if (obj instanceof VPSOptimization) {
                                    VPSOptimization vps = (VPSOptimization) obj;
                                    if (vps.getVpsId().equals(vpsId)) {
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
                List<String> installedVpsIds = (List<String>) rackConfig.get("installedVpsIds");
                
                if (installedVpsIds != null && !installedVpsIds.isEmpty()) {
                    this.rack.setRackIndex(0);
                    
                    for (String vpsId : installedVpsIds) {
                        boolean found = false;
                        
                        for (GameObject obj : state.getGameObjects()) {
                            if (obj instanceof VPSOptimization) {
                                VPSOptimization vps = (VPSOptimization) obj;
                                if (vps.getVpsId().equals(vpsId)) {
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
            
            this.rack = new Rack();
            this.rack.addRack(10); 
            System.out.println("เกิดข้อผิดพลาด สร้าง Rack เริ่มต้นแทน");
            return false;
        }
    }

    public void pushNotification(String title, String content) {
        initializeNotifications();
        notificationModel.addNotification(new NotificationModel.Notification(title, content));
        javafx.application.Platform.runLater(() -> {
            try {
                notificationView.addNotificationPane(title, content);
            } catch (Exception e) {
                System.err.println("Error showing notification: " + e.getMessage());
            }
        });
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

    public void pushCenterNotificationAutoClose(String title, String content, String image, long autoCloseMillis) {
        initializeNotifications();
        centerNotificationController.pushAutoClose(title, content, image, autoCloseMillis);
    }

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

    public Rack getRack() {
        return rack;
    }

    public void setRack(Rack rack) {
        this.rack = rack;
    }

    public RequestManager getRequestManager() {
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

    public RequestGenerator getRequestGenerator() {
        return requestGenerator;
    }
    
    public void setRequestGenerator(RequestGenerator generator) {
        this.requestGenerator = generator;
        System.out.println("ตั้งค่า RequestGenerator ใหม่ใน ResourceManager");
    }

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
        try {
            File gameDir = new File(GAME_FOLDER);
            if (!gameDir.exists()) {
                boolean created = gameDir.mkdirs();
                if (created) {
                    System.out.println("สร้างโฟลเดอร์เกม: " + gameDir.getAbsolutePath());
                } else {
                    System.err.println("ไม่สามารถสร้างโฟลเดอร์เกม: " + gameDir.getAbsolutePath());
                }
            }
            
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                boolean created = backupDir.mkdirs();
                if (created) {
                    System.out.println("สร้างโฟลเดอร์สำรองข้อมูล: " + backupDir.getAbsolutePath());
                } else {
                    System.err.println("ไม่สามารถสร้างโฟลเดอร์สำรองข้อมูล: " + backupDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างโฟลเดอร์: " + e.getMessage());
            e.printStackTrace();
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

    public void preloadAssets() {
        System.out.println("===== เริ่มโหลดทรัพยากรของเกม =====");
        preloadComplete = false;
        
        final String[] imagesToPreload = {
            "/images/rooms/room.gif",
            "/images/Moniter/MoniterF2.png",
            "/images/servers/server2.gif",
            "/images/Object/Keroro.png",
            "/images/Object/MusicboxOn.gif",
            "/images/Object/MusicboxOff.png",
            "/images/Object/Table.png",
            "/images/wallpaper/Desktop.gif",
            "/images/buttons/MessengerDesktop.png",
            "/images/buttons/MarketDesktop.png",
            "/images/buttons/RoomDesktop.gif",
            "/images/buttons/ServerDesktop.gif",
            "/images/home/logo.gif",
            "/images/home/itstudent.gif",
            "/images/home/background.png",
            "/images/others/Credits.gif"
        };
        
        final String[] soundsToPreload = {
            "hover.wav",
            "click.wav",
            "click_app.wav",
            "server.mp3"
        };
        
        Thread preloadThread = new Thread(() -> {
            try {
                for (String imagePath : imagesToPreload) {
                    notifyResourceLoading(imagePath);
                    preloadImage(imagePath);
                }
                
                for (String soundPath : soundsToPreload) {
                    notifyResourceLoading("เสียง: " + soundPath);
                    audioManager.preloadSoundEffect(soundPath);
                }
                
                try {
                    Class.forName("com.vpstycoon.ui.game.desktop.DesktopScreen")
                         .getMethod("preloadImages")
                         .invoke(null);
                } catch (Exception e) {
                    System.err.println("Error preloading DesktopScreen images: " + e.getMessage());
                }
                
                int totalResources = imagesToPreload.length + soundsToPreload.length;
                System.out.println("===== โหลดทรัพยากรของเกมเสร็จสมบูรณ์: " + totalResources + " รายการ =====");
                notifyResourceLoadingComplete(totalResources);
            } catch (Exception e) {
                System.err.println("Error preloading assets: " + e.getMessage());
                e.printStackTrace();
            } finally {
                synchronized(preloadLock) {
                    preloadComplete = true;
                    preloadLock.notifyAll();
                }
            }
        });
        
        preloadThread.setDaemon(true);
        preloadThread.start();
    }
    
    public boolean waitForPreload(long timeoutMs) {
        if (preloadComplete) return true;
        
        synchronized(preloadLock) {
            if (!preloadComplete) {
                try {
                    preloadLock.wait(timeoutMs > 0 ? timeoutMs : 0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        return preloadComplete;
    }
    
    public boolean isPreloadComplete() {
        return preloadComplete;
    }
    
    private Image preloadImage(String path) {
        if (!imageCache.containsKey(path)) {
            try {
                imageCache.put(path, new Image(path, true));
            } catch (Exception e) {
                System.err.println("Error loading image " + path + ": " + e.getMessage());
            }
        }
        return imageCache.get(path);
    }
    
    public Image getPreloadedImage(String path) {
        return imageCache.get(path);
    }

    public void resetMessengerData() {
        try {
            System.out.println("กำลังรีเซ็ตข้อมูล Messenger และประวัติแชท...");
            
            ChatHistoryManager chatManager = getChatHistory();
            if (chatManager != null) {
                chatManager.resetAllChatData();
            }
            
            ChatHistoryManager.resetInstance();
            
            if (requestManager != null) {
                requestManager.resetRequests();
            } else {
                System.out.println("ยังไม่มี RequestManager");
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
    
    public void resetGameTime() {
        try {
            System.out.println("กำลังรีเซ็ตเวลาเกม...");
            
            if (gameTimeController != null) {
                LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                gameTimeController.resetTime(startTime);
                System.out.println("รีเซ็ตเวลาด้วย GameTimeController เรียบร้อย");
            } else {
                System.out.println("ไม่พบ GameTimeController จำเป็นต้องสร้างใหม่");
                
                if (currentState != null) {
                    LocalDateTime startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
                    currentState.setLocalDateTime(startTime);
                    currentState.setGameTimeMs(0);
                    System.out.println("รีเซ็ตเวลาใน GameState เป็น: " + startTime);
                    
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
    
    public void resetRackAndInventory() {
        try {
            System.out.println("กำลังรีเซ็ต Rack และ VPSInventory...");
            
            this.rack = new Rack();
            System.out.println("รีเซ็ต Rack เรียบร้อย");
            
            notifyRackUIUpdate();
            
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
            
            resetGameTime();
            
            resetMessengerData();
            
            System.out.println("รีเซ็ตระบบคลังสินค้า VPS เรียบร้อย");
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการรีเซ็ต Rack และ VPSInventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public GameManager getGameManager() {
        if (gameManager == null) {
            gameManager = GameManager.getInstance();
        }
        return gameManager;
    }
    
    public void setGameManager(GameManager manager) {
        this.gameManager = manager;
    }

    public void resetSkillPointsSystem() {
        if (this.company != null) {
            System.out.println("รีเซ็ต SkillPointsSystem");
            this.skillPointsSystem = new SkillPointsSystem(this.company);
        } else {
            System.out.println("ไม่สามารถรีเซ็ต SkillPointsSystem ได้เนื่องจาก company เป็น null");
        }
    }
}

