package com.vpstycoon.model.common;

import com.vpstycoon.model.company.interfaces.ICompany;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameState implements Serializable {
    private transient ObjectProperty<LocalDateTime> localDateTime = new SimpleObjectProperty<>();
    private long gameTimeMs;
    private ICompany company;
    private final Map<String, Integer> resources;
    private final Map<String, Boolean> upgrades;
    private long lastSaveTime;
    private List<GameObject> gameObjects;
    private Map<String, Object> rackConfiguration; 
    private Map<String, Object> vpsInventoryData; 
    private int freeVmCount;
    private Map<Object, List<Object>> chatHistory;
    private List<Object> pendingRequests;
    private List<Object> completedRequests;
    private Map<String, String> vmAssignments; 
    private Map<String, Integer> skillLevels;

    
    public GameState() {
        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.lastSaveTime = System.currentTimeMillis();
        this.gameObjects = new ArrayList<>();
        this.rackConfiguration = new HashMap<>();
        this.vpsInventoryData = new HashMap<>();
        this.freeVmCount = 0;
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
        this.vmAssignments = new HashMap<>();
        this.skillLevels = new HashMap<>();
        this.localDateTime = new SimpleObjectProperty<>();
        this.localDateTime.set(LocalDateTime.of(2000, 1, 1, 0, 0));
    }

    
    public GameState(ArrayList<GameObject> gameObjects) {
        this();  
        if (gameObjects != null) {
            this.gameObjects = new ArrayList<>(gameObjects);
        }
    }

    
    public GameState(ICompany company, List<GameObject> gameObjects) {
        this.company = company;
        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.gameObjects = new ArrayList<>(gameObjects != null ? gameObjects : new ArrayList<>());
        this.lastSaveTime = System.currentTimeMillis();
        this.rackConfiguration = new HashMap<>();
        this.vpsInventoryData = new HashMap<>();
        this.freeVmCount = 0;
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
        this.vmAssignments = new HashMap<>();
        this.skillLevels = new HashMap<>();
        this.localDateTime = new SimpleObjectProperty<>(LocalDateTime.of(2000, 1, 1, 0, 0));
    }

    
    public GameState(ICompany company) {
        this.company = company;
        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.gameObjects = new ArrayList<>();
        this.lastSaveTime = System.currentTimeMillis();
        this.rackConfiguration = new HashMap<>();
        this.vpsInventoryData = new HashMap<>();
        this.freeVmCount = 0;
        this.chatHistory = new HashMap<>();
        this.pendingRequests = new ArrayList<>();
        this.completedRequests = new ArrayList<>();
        this.vmAssignments = new HashMap<>();
        this.skillLevels = new HashMap<>();
        this.localDateTime = new SimpleObjectProperty<>(LocalDateTime.of(2000, 1, 1, 0, 0));
    }

    
    public void addGameObject(GameObject obj) {
        if (gameObjects == null) {
            gameObjects = new ArrayList<>();
        }
        gameObjects.add(obj);
    }

    
    public void removeGameObject(GameObject obj) {
        if (gameObjects != null) {
            gameObjects.remove(obj);
        }
    }

    public int getFreeVmCount() {
        return freeVmCount;
    }
    
    public void setFreeVmCount(int freeVmCount) {
        this.freeVmCount = freeVmCount;
    }

    public Map<String, Object> getRackConfiguration() {
        return rackConfiguration;
    }
    
    public void setRackConfiguration(Map<String, Object> rackConfiguration) {
        this.rackConfiguration = rackConfiguration;
    }
    
    public Map<String, Object> getVpsInventoryData() {
        return vpsInventoryData;
    }
    
    public void setVpsInventoryData(Map<String, Object> vpsInventoryData) {
        this.vpsInventoryData = vpsInventoryData;
    }
    
    public Map<Object, List<Object>> getChatHistory() {
        return chatHistory;
    }
    
    public void setChatHistory(Map<Object, List<Object>> chatHistory) {
        this.chatHistory = chatHistory;
    }

    public List<Object> getPendingRequests() {
        return pendingRequests;
    }
    
    public void setPendingRequests(List<Object> pendingRequests) {
        if (pendingRequests != null) {
            this.pendingRequests = new ArrayList<>(pendingRequests);
        } else {
            this.pendingRequests = new ArrayList<>();
        }
    }
    
    public List<Object> getCompletedRequests() {
        return completedRequests;
    }
    
    public void setCompletedRequests(List<Object> completedRequests) {
        if (completedRequests != null) {
            this.completedRequests = new ArrayList<>(completedRequests);
        } else {
            this.completedRequests = new ArrayList<>();
        }
    }

    public Map<String, String> getVmAssignments() {
        return vmAssignments;
    }
    
    public void setVmAssignments(Map<String, String> vmAssignments) {
        this.vmAssignments = vmAssignments;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void setGameObjects(List<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
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

    public ICompany getCompany() {
        return company;
    }

    public void setCompany(ICompany company) {
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

    
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(localDateTime.get());
    }

    
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        localDateTime = new SimpleObjectProperty<>((LocalDateTime) in.readObject());
    }

    public void setGameTimeMs(long gameTimeMs) {
        this.gameTimeMs = gameTimeMs;
    }

    public long getGameTimeMs() {
        return gameTimeMs;
    }

    public Map<String, Integer> getSkillLevels() {
        return skillLevels;
    }
    
    public void setSkillLevels(Map<String, Integer> skillLevels) {
        this.skillLevels = skillLevels;
    }
} 
