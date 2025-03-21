package com.vpstycoon.game.resource;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.ui.game.rack.Rack;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private GameTimeManager gameTimeManager;

    private ResourceManager() {
        this.company = new Company();
        this.audioManager = new AudioManager();
        this.requestManager = new RequestManager(this.company);
        this.rack = new Rack(10, 3); // สร้าง Rack เริ่มต้นใน ResourceManager

        this.gameTimeManager = new GameTimeManager(
                this.company,
                this.requestManager,
                this.rack,
                LocalDateTime.of(2000, 1, 1, 0, 0, 0)
        );

        createBackupDirectory();
        if (currentState == null) {
            currentState = new GameState(this.company);
        }
    }

    public static ResourceManager getInstance() {
        return instance;
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
        state.setLocalDateTime(gameTimeManager.getGameDateTime());
        state.setGameTimeMs(gameTimeManager.getGameTimeMs());

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
            this.rack = new Rack(10, 3); // รีเซ็ต Rack ถ้าไม่มี save
            return newState;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            GameState state = (GameState) ois.readObject();
            System.out.println("Game loaded successfully from: " + SAVE_FILE);
            this.currentState = state;
            this.company = state.getCompany();
            this.requestManager = new RequestManager(this.company);
            this.gameTimeManager = new GameTimeManager(
                    this.company,
                    this.requestManager,
                    this.rack,
                    state.getLocalDateTime()
            );
            this.gameTimeManager.getGameTimeMs();
            // หาก Rack ถูกเก็บใน GameState ด้วย จะต้องโหลดจาก state ด้วย
            // ถ้าไม่เก็บใน state จะใช้ค่าเริ่มต้น
            this.rack = new Rack(10, 3); // หรือโหลดจาก state ถ้ามี
            return state;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            e.printStackTrace();
            createCorruptedFileBackup(saveFile);
            GameState newState = new GameState();
            this.currentState = newState;
            this.company = newState.getCompany();
            this.rack = new Rack(10, 3);
            return newState;
        }
    }

    // Getter และ Setter สำหรับ Rack
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

    public GameTimeManager getGameTimeManager() {
        return gameTimeManager;
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
}