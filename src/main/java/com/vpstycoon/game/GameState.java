package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem.SkillType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatMessage;
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
    
    
    private Map<String, Object> rackConfiguration; 
    private Map<String, Object> vpsInventoryData; 
    
    
    private int freeVmCount;
    
    
    private Map<CustomerRequest, List<ChatMessage>> chatHistory;
    
    
    private ArrayList<CustomerRequest> pendingRequests;
    private ArrayList<CustomerRequest> completedRequests;
    
    
    private Map<String, String> vmAssignments; 
    
    
    private Map<SkillType, Integer> skillLevels;

    public GameState() {
        this.company = ResourceManager.getInstance().getCompany();

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

    public GameState(Company company, List<GameObject> gameObjects) {
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

    public GameState(Company company) {
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
    
    
    public Map<CustomerRequest, List<ChatMessage>> getChatHistory() {
        return chatHistory;
    }
    
    public void setChatHistory(Map<CustomerRequest, List<ChatMessage>> chatHistory) {
        this.chatHistory = chatHistory;
    }

    
    public List<CustomerRequest> getPendingRequests() {
        return pendingRequests;
    }
    
    public void setPendingRequests(List<CustomerRequest> pendingRequests) {
        
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

    public void addGameObject(GameObject obj) {
        if (gameObjects == null) {
            gameObjects = new ArrayList<>();
        }
        gameObjects.add(obj);
    }

    
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

    
    public Map<SkillType, Integer> getSkillLevels() {
        return skillLevels;
    }
    
    public void setSkillLevels(Map<SkillType, Integer> skillLevels) {
        this.skillLevels = skillLevels;
    }
    
    
    public void clearState() {
        
        resources.clear();
        upgrades.clear();
        
        
        if (gameObjects != null) {
            gameObjects.clear();
        } else {
            gameObjects = new ArrayList<>();
        }
        
        
        if (rackConfiguration != null) {
            rackConfiguration.clear();
        } else {
            rackConfiguration = new HashMap<>();
        }
        
        
        if (vpsInventoryData != null) {
            vpsInventoryData.clear();
        } else {
            vpsInventoryData = new HashMap<>();
        }
        
        freeVmCount = 0;
        
        
        if (chatHistory != null) {
            chatHistory.clear();
        } else {
            chatHistory = new HashMap<>();
        }
        
        
        if (pendingRequests != null) {
            pendingRequests.clear();
        } else {
            pendingRequests = new ArrayList<>();
        }
        
        if (completedRequests != null) {
            completedRequests.clear();
        } else {
            completedRequests = new ArrayList<>();
        }
        
        if (vmAssignments != null) {
            vmAssignments.clear();
        } else {
            vmAssignments = new HashMap<>();
        }
        
        
        if (skillLevels != null) {
            skillLevels.clear();
        } else {
            skillLevels = new HashMap<>();
        }
        
        
        localDateTime.set(LocalDateTime.of(2000, 1, 1, 0, 0));
        gameTimeMs = 0;
        
        System.out.println("ล้างข้อมูล GameState เรียบร้อยแล้ว");
    }
}
