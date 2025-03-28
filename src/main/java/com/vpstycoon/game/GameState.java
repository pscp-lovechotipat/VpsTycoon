package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.ui.game.rack.Rack;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatMessage;
import com.vpstycoon.game.company.SkillPointsSystem.SkillType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {
    private transient int temporaryValue;

    private static final long serialVersionUID = 1L;
    private transient ObjectProperty<LocalDateTime> localDateTime = new SimpleObjectProperty<>();
    private long gameTimeMs;

    private Company company;

    private final Map<String, Integer> resources;
    private final Map<String, Boolean> upgrades;

    private long lastSaveTime;
    private List<GameObject> gameObjects;
    
    // เพิ่มฟิลด์สำหรับเก็บข้อมูล Rack และ VPS Inventory
    private Map<String, Object> rackConfiguration; // เก็บข้อมูลการตั้งค่า Rack
    private Map<String, Object> vpsInventoryData; // เก็บข้อมูล VPS ที่ยังไม่ได้ติดตั้ง
    
    // เพิ่มฟิลด์สำหรับเก็บจำนวน free VM
    private int freeVmCount;
    
    // เพิ่มฟิลด์สำหรับเก็บประวัติแชท
    private Map<CustomerRequest, List<ChatMessage>> chatHistory;
    
    // เพิ่มฟิลด์สำหรับเก็บข้อมูล pendingRequests และ completedRequests
    private ArrayList<CustomerRequest> pendingRequests;
    private ArrayList<CustomerRequest> completedRequests;
    
    // เพิ่มฟิลด์สำหรับเก็บการเชื่อมโยงระหว่าง VM และ CustomerRequest
    private Map<String, String> vmAssignments; // vmId -> requestId
    
    // เพิ่มฟิลด์สำหรับเก็บระดับทักษะ (skill levels)
    private Map<SkillType, Integer> skillLevels;

    public GameState() {
        this.company = ResourceManager.getInstance().getCompany();

        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.lastSaveTime = System.currentTimeMillis();
        this.gameObjects = new ArrayList<>();
        
        // เพิ่มการเริ่มต้นค่าสำหรับฟิลด์ใหม่
        this.rackConfiguration = new HashMap<>();
        this.vpsInventoryData = new HashMap<>();
        this.freeVmCount = 0;
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
        this.vmAssignments = new HashMap<>();
        this.skillLevels = new HashMap<>();

        this.localDateTime.set(LocalDateTime.of(2000, 1, 1, 0, 0));
    }
    
    public GameState(ArrayList<GameObject> gameObjects) {
        this();
        this.gameObjects = new ArrayList<>(gameObjects);
    }

    public GameState(Company company, List<GameObject> gameObjects) {
        this.company = company;
        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.gameObjects = new ArrayList<>(gameObjects);
        
        // เพิ่มการเริ่มต้นค่าสำหรับฟิลด์ใหม่
        this.rackConfiguration = new HashMap<>();
        this.vpsInventoryData = new HashMap<>();
        this.freeVmCount = 0;
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
    }

    public GameState(Company company) {
        this.company = company;
        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        
        // เพิ่มการเริ่มต้นค่าสำหรับฟิลด์ใหม่
        this.rackConfiguration = new HashMap<>();
        this.vpsInventoryData = new HashMap<>();
        this.freeVmCount = 0;
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
    }

    // Getter และ Setter สำหรับ freeVmCount
    public int getFreeVmCount() {
        return freeVmCount;
    }
    
    public void setFreeVmCount(int freeVmCount) {
        this.freeVmCount = freeVmCount;
    }

    // Getter และ Setter สำหรับ Rack Configuration
    public Map<String, Object> getRackConfiguration() {
        return rackConfiguration;
    }
    
    public void setRackConfiguration(Map<String, Object> rackConfiguration) {
        this.rackConfiguration = rackConfiguration;
    }
    
    // Getter และ Setter สำหรับ VPS Inventory Data
    public Map<String, Object> getVpsInventoryData() {
        return vpsInventoryData;
    }
    
    public void setVpsInventoryData(Map<String, Object> vpsInventoryData) {
        this.vpsInventoryData = vpsInventoryData;
    }
    
    // Getter และ Setter สำหรับประวัติแชท
    public Map<CustomerRequest, List<ChatMessage>> getChatHistory() {
        return chatHistory;
    }
    
    public void setChatHistory(Map<CustomerRequest, List<ChatMessage>> chatHistory) {
        this.chatHistory = chatHistory;
    }

    // Add getters and setters for pendingRequests and completedRequests
    public List<CustomerRequest> getPendingRequests() {
        return pendingRequests;
    }
    
    public void setPendingRequests(List<CustomerRequest> pendingRequests) {
        // สร้าง ArrayList ใหม่ในกรณีที่ได้รับ ObservableList เข้ามา
        if (pendingRequests != null) {
            this.pendingRequests = new ArrayList<>(pendingRequests);
        } else {
            this.pendingRequests = new ArrayList<>();
        }
    }
    
    public List<CustomerRequest> getCompletedRequests() {
        return completedRequests;
    }
    
    public void setCompletedRequests(List<CustomerRequest> completedRequests) {
        // สร้าง ArrayList ใหม่ในกรณีที่ได้รับ ObservableList เข้ามา
        if (completedRequests != null) {
            this.completedRequests = new ArrayList<>(completedRequests);
        } else {
            this.completedRequests = new ArrayList<>();
        }
    }

    // Getter และ Setter สำหรับ vmAssignments
    public Map<String, String> getVmAssignments() {
        return vmAssignments;
    }
    
    public void setVmAssignments(Map<String, String> vmAssignments) {
        this.vmAssignments = vmAssignments;
    }

    public void addGameObject(GameObject obj) {
        if (gameObjects == null) {
            gameObjects = new ArrayList<>();
        }
        gameObjects.add(obj);
    }

    // Getters and setters
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void setGameObjects(List<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
    }


    public void removeGameObject(GameObject obj) {
        if (gameObjects != null) {
            gameObjects.remove(obj);
        }
    }
    
    public Map<String, Integer> getResources() {
        return resources;
    }
    
    public Map<String, Boolean> getUpgrades() {
        return upgrades;
    }
    
    public long getLastSaveTime() {
        return lastSaveTime;
    }
    
    public void setLastSaveTime(long lastSaveTime) {
        this.lastSaveTime = lastSaveTime;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime.get();
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime.set(localDateTime);
    }

    public ObjectProperty<LocalDateTime> localDateTimeProperty() {
        return localDateTime;
    }

    // ต้องจัดการ serialization ด้วย เพราะ ObjectProperty ไม่ได้ implements Serializable โดยตรง
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(localDateTime.get());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        localDateTime = new SimpleObjectProperty<>((LocalDateTime) in.readObject());
    }

    public void setGameTimeMs(long gameTimeMs) {
        this.gameTimeMs = gameTimeMs;
    }

    public long getGameTimeMs() {
        return gameTimeMs;
    }

    // Getter และ Setter สำหรับ skillLevels
    public Map<SkillType, Integer> getSkillLevels() {
        return skillLevels;
    }
    
    public void setSkillLevels(Map<SkillType, Integer> skillLevels) {
        this.skillLevels = skillLevels;
    }
}