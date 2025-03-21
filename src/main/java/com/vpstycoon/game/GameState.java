package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
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
    
    public GameState() {
        this.company = ResourceManager.getInstance().getCompany();

        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.lastSaveTime = System.currentTimeMillis();
        this.gameObjects = new ArrayList<>();

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
    }

    public GameState(Company company) {
        this.company = company;
        this.resources = new HashMap<>();
        this.upgrades = new HashMap<>();
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
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
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
}