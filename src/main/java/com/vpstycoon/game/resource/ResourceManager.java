package com.vpstycoon.game.resource;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.thread.GameEvent;
import com.vpstycoon.game.thread.GameTimeController;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.enums.VPSSize;
import com.vpstycoon.ui.game.GameplayContentPane;
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

    private RequestManager requestManager;
    private final AudioManager audioManager;
    private GameTimeController gameTimeController;

    private SkillPointsSystem skillPointsSystem;

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

    private ResourceManager() {
        this.company = new Company();
        this.audioManager = new AudioManager();
        this.rack = new Rack(); // สร้าง Rack เริ่มต้นใน ResourceManager

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
        
        // บันทึกข้อมูล Rack
        System.out.println("กำลังบันทึกข้อมูล Rack...");
        if (rack != null) {
            try {
                // เก็บข้อมูลการตั้งค่า Rack
                Map<String, Object> rackConfig = new HashMap<>();
                rackConfig.put("maxRacks", rack.getMaxRacks());
                rackConfig.put("currentRackIndex", rack.getCurrentRackIndex());
                rackConfig.put("maxSlotUnits", rack.getMaxSlotUnits());
                rackConfig.put("unlockedSlotUnits", rack.getUnlockedSlotUnits());
                rackConfig.put("occupiedSlotUnits", rack.getOccupiedSlotUnits());
                
                // เก็บ VPS ที่ถูกติดตั้งใน Rack
                List<String> installedVpsIds = new ArrayList<>();
                for (VPSOptimization vps : rack.getInstalledVPS()) {
                    installedVpsIds.add(vps.getVpsId());
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
                rackConfig.put("installedVpsIds", installedVpsIds);
                
                // บันทึกข้อมูล Rack ลงใน GameState
                state.setRackConfiguration(rackConfig);
                System.out.println("บันทึกข้อมูล Rack สำเร็จ: " + rack.getInstalledVPS().size() + " VPS ถูกติดตั้ง");
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
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists() || saveFile.length() == 0) {
            System.out.println("No save game file found or file is empty.");
            GameState newState = new GameState();
            newState.setLocalDateTime(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
            this.currentState = newState;
            this.company = newState.getCompany();
            this.requestManager = new RequestManager(this.company);
            this.rack = new Rack(); // สร้าง Rack ใหม่เพราะไม่มีข้อมูล
            if (gameplayContentPane != null) {
                initializeGameEvent(gameplayContentPane); // Start events for new game
            }
            return newState;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            GameState state = (GameState) ois.readObject();
            System.out.println("Game loaded successfully from: " + SAVE_FILE);
            
            // ตรวจสอบข้อมูลที่โหลดมา
            if (state.getCompany() != null) {
                System.out.println("บริษัทที่โหลด: " + state.getCompany().getName());
                System.out.println("เงินที่มีอยู่: $" + state.getCompany().getMoney());
                System.out.println("Rating: " + state.getCompany().getRating());
            } else {
                System.out.println("ไม่พบข้อมูลบริษัท ใช้ค่าเริ่มต้น");
                state.setCompany(new Company());
            }
            
            // บันทึกสถานะเกมและข้อมูลบริษัท
            this.currentState = state;
            this.company = state.getCompany();
            
            // สร้าง RequestManager ใหม่โดยใช้ข้อมูลบริษัทจากไฟล์บันทึก
            this.requestManager = new RequestManager(this.company);
            
            // สร้าง Rack ใหม่
            this.rack = new Rack();
            
            // โหลดการตั้งค่า Rack จาก GameState (ถ้ามี)
            Map<String, Object> rackConfig = state.getRackConfiguration();
            if (rackConfig != null && !rackConfig.isEmpty()) {
                System.out.println("กำลังโหลดข้อมูล Rack จาก GameState...");
                
                // 1. สร้าง rack ตามจำนวนที่บันทึกไว้
                if (rackConfig.containsKey("maxRacks")) {
                    int maxRacks = (int) rackConfig.get("maxRacks");
                    for (int i = 0; i < maxRacks; i++) {
                        rack.addRack(10); // สร้าง rack ด้วยสล็อต 10 ช่อง (ค่าเริ่มต้น)
                    }
                    System.out.println("สร้าง " + maxRacks + " rack เรียบร้อยแล้ว");
                } else {
                    // ถ้าไม่มีข้อมูล maxRacks ให้สร้าง rack แรกด้วยค่าเริ่มต้น
                    rack.addRack(10);
                    System.out.println("ไม่พบข้อมูล maxRacks สร้าง 1 rack เริ่มต้น");
                }
                
                // 2. ตั้งค่า currentRackIndex
                if (rackConfig.containsKey("currentRackIndex")) {
                    int currentRackIndex = (int) rackConfig.get("currentRackIndex");
                    rack.setRackIndex(currentRackIndex);
                    System.out.println("ตั้งค่า currentRackIndex = " + currentRackIndex);
                }
                
                // 3. ปลดล็อคสล็อตตามจำนวนที่บันทึกไว้
                if (rackConfig.containsKey("unlockedSlotUnits")) {
                    int unlockedSlots = (int) rackConfig.get("unlockedSlotUnits");
                    // Rack.upgrade() จะปลดล็อคสล็อตทีละช่อง
                    for (int i = 1; i < unlockedSlots; i++) { // เริ่มจาก 1 เพราะมี 1 slot ปลดล็อคอยู่แล้ว
                        rack.upgrade();
                    }
                    System.out.println("ปลดล็อค " + unlockedSlots + " slots เรียบร้อยแล้ว");
                }
            } else {
                // ถ้าไม่มีข้อมูล Rack ให้สร้าง rack แรกด้วยค่าเริ่มต้น
                rack.addRack(10);
                System.out.println("ไม่พบข้อมูล Rack สร้าง 1 rack เริ่มต้น");
            }
            
            // ตรวจสอบ GameObject ทั้งหมดเพื่อหา VPSOptimization
            List<VPSOptimization> foundVPSs = new ArrayList<>();
            // สร้าง VPSInventory ใหม่สำหรับโหลดข้อมูล
            VPSInventory tempInventory = new VPSInventory();
            
            if (state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                System.out.println("กำลังโหลด " + state.getGameObjects().size() + " GameObject จากไฟล์บันทึก");
                
                // ค้นหา VPSOptimization ใน gameObjects
                for (GameObject obj : state.getGameObjects()) {
                    System.out.println("กำลังตรวจสอบ GameObject: " + obj.getType() + " - " + obj.getName());
                    
                    if (obj instanceof VPSOptimization) {
                        VPSOptimization vps = (VPSOptimization) obj;
                        foundVPSs.add(vps);
                        
                        // ตรวจสอบข้อมูล VPS ละเอียดขึ้น
                        System.out.println("พบ VPS: " + vps.getName());
                        System.out.println("  - CPU: " + vps.getVCPUs() + " vCPU");
                        System.out.println("  - RAM: " + vps.getRamInGB() + " GB");
                        System.out.println("  - Size: " + vps.getSize());
                        System.out.println("  - Installed: " + vps.isInstalled());
                        
                        // ถ้า VPS ได้รับการติดตั้งแล้ว ให้ติดตั้งลงใน rack
                        if (vps.isInstalled()) {
                            System.out.println("กำลังติดตั้ง VPS ลงใน Rack: " + vps.getName());
                            // ติดตั้ง VPS ลงใน rack 
                            rack.installVPS(vps);
                        } else {
                            // ถ้า VPS ยังไม่ได้ติดตั้ง ให้เพิ่มเข้า inventory
                            if (vps.getVpsId() != null && !vps.getVpsId().isEmpty()) {
                                tempInventory.addVPS(vps.getVpsId(), vps);
                                System.out.println("เพิ่ม VPS เข้า Inventory ชั่วคราว: " + vps.getVpsId());
                            } else {
                                System.out.println("VPS ไม่มี ID ไม่สามารถเพิ่มเข้า Inventory ได้");
                            }
                        }
                    }
                }
                
                System.out.println("พบ VPS ทั้งหมด " + foundVPSs.size() + " เครื่อง");
                
                // ถ้ามีการบันทึกข้อมูล VPS Inventory ให้โหลดข้อมูลเพิ่มเติม
                Map<String, Object> vpsInventoryData = state.getVpsInventoryData();
                if (vpsInventoryData != null && !vpsInventoryData.isEmpty()) {
                    System.out.println("พบข้อมูล VPS Inventory:");
                    try {
                        if (vpsInventoryData.containsKey("vpsIds")) {
                            List<String> vpsIds = (List<String>) vpsInventoryData.get("vpsIds");
                            System.out.println("  - VPS ที่ยังไม่ได้ติดตั้ง: " + vpsIds.size() + " เครื่อง");
                            
                            // โหลดข้อมูลเพิ่มเติมของ VPS จาก vpsDetails (ถ้ามี)
                            if (vpsInventoryData.containsKey("vpsDetails")) {
                                Map<String, Map<String, Object>> vpsDetails = 
                                    (Map<String, Map<String, Object>>) vpsInventoryData.get("vpsDetails");
                                
                                for (String vpsId : vpsIds) {
                                    // ตรวจสอบว่า VPS นี้มีอยู่ใน inventory ชั่วคราวหรือไม่
                                    if (!tempInventory.getAllVPSIds().contains(vpsId)) {
                                        // ถ้าไม่มีให้สร้าง VPS ใหม่
                                        if (vpsDetails.containsKey(vpsId)) {
                                            Map<String, Object> details = vpsDetails.get(vpsId);
                                            VPSOptimization newVPS = new VPSOptimization();
                                            newVPS.setVpsId(vpsId);
                                            
                                            if (details.containsKey("vCPUs")) {
                                                newVPS.setVCPUs((Integer) details.get("vCPUs"));
                                            }
                                            
                                            if (details.containsKey("ramInGB")) {
                                                newVPS.setRamInGB((Integer) details.get("ramInGB"));
                                            }
                                            
                                            if (details.containsKey("diskInGB")) {
                                                newVPS.setDiskInGB((Integer) details.get("diskInGB"));
                                            }
                                            
                                            if (details.containsKey("name") && details.get("name") != null) {
                                                newVPS.setName((String) details.get("name"));
                                            } else {
                                                newVPS.setName("VPS-" + vpsId);
                                            }
                                            
                                            if (details.containsKey("size") && details.get("size") != null) {
                                                String sizeStr = (String) details.get("size");
                                                try {
                                                    newVPS.setSize(VPSSize.valueOf(sizeStr));
                                                } catch (IllegalArgumentException e) {
                                                    // เป็นค่าที่ไม่ถูกต้อง ใช้ค่าเริ่มต้น
                                                    newVPS.setSize(VPSSize.SIZE_1U);
                                                }
                                            } else {
                                                newVPS.setSize(VPSSize.SIZE_1U);
                                            }
                                            
                                            // กำหนดสถานะเป็นยังไม่ได้ติดตั้ง
                                            newVPS.setInstalled(false);
                                            
                                            // เพิ่มเข้า inventory ชั่วคราว
                                            tempInventory.addVPS(vpsId, newVPS);
                                            
                                            // เพิ่มเข้า gameObjects ด้วย
                                            state.addGameObject(newVPS);
                                            
                                            System.out.println("สร้าง VPS ใหม่จากข้อมูลในไฟล์บันทึก: " + vpsId);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูล VPS Inventory: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("ไม่พบข้อมูล GameObject หรือ gameObjects เป็นค่าว่าง");
            }
            
            // ถ้ามี GameplayContentPane ให้ตั้งค่า VPSInventory
            if (gameplayContentPane != null) {
                // สร้าง VPSInventory ใหม่และเพิ่ม VPS จาก inventory ชั่วคราว
                VPSInventory contentPaneInventory = gameplayContentPane.getVpsInventory();
                
                // ตรวจสอบว่ามี inventory อยู่แล้วหรือไม่
                if (contentPaneInventory != null) {
                    // ล้างข้อมูลเดิมทั้งหมด
                    contentPaneInventory.clear();
                    
                    // เพิ่ม VPS จาก inventory ชั่วคราว
                    for (String vpsId : tempInventory.getAllVPSIds()) {
                        VPSOptimization vps = tempInventory.getVPS(vpsId);
                        contentPaneInventory.addVPS(vpsId, vps);
                    }
                    
                    System.out.println("อัปเดต VPSInventory ใน GameplayContentPane: " + contentPaneInventory.getSize() + " รายการ");
                } else {
                    System.out.println("ไม่พบ VPSInventory ใน GameplayContentPane");
                }
            }
            
            // สร้าง GameTimeController โดยใช้ข้อมูลเวลาจากไฟล์บันทึก
            this.gameTimeController = new GameTimeController(
                this.company,
                this.requestManager,
                this.rack,
                state.getLocalDateTime()
            );
            
            System.out.println("โหลดเวลาเกม: " + state.getLocalDateTime());
            System.out.println("โหลดเวลา ms: " + state.getGameTimeMs());
            
            // เริ่มต้น game events ใหม่
            if (gameplayContentPane != null) {
                initializeGameEvent(gameplayContentPane);
            }
            
            return state;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            e.printStackTrace();
            createCorruptedFileBackup(saveFile);
            
            // สร้าง state ใหม่ในกรณีที่เกิดข้อผิดพลาด
            GameState newState = new GameState();
            this.currentState = newState;
            this.company = newState.getCompany();
            this.rack = new Rack();
            
            if (gameplayContentPane != null) {
                initializeGameEvent(gameplayContentPane); // Start events for new game
            }
            return newState;
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
        return currentState;
    }

    public void setCurrentState(GameState state) {
        this.currentState = state;
        this.company = state.getCompany();
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

    public GameObject createGameObject(String id, String type, int gridX, int gridY) {
        return new GameObject(id, type, gridX, gridY);
    }

    public boolean isMusicRunning() {
        return musicRunning;
    }

    public void setMusicRunning(boolean running) {
        this.musicRunning = running;
    }
}