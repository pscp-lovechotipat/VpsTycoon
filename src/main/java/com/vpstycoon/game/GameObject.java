package com.vpstycoon.game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String type;
    private double x;
    private double y;
    private boolean active;
    private final Map<String, Object> properties;
    private String name;
    private int level;
    private String status;

    public GameObject() {
        this.id = "";
        this.type = "";
        this.x = 0;
        this.y = 0;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.name = "";
        this.status = "Active";
    }

    public GameObject(String id, int level) {
        this.id = id;
        this.level = level;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.status = "Active"; // Default status
    }

    public GameObject(String id, String type, double x, double y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.name = type; // Default name to type
        this.status = "Active"; // Default status
    }
    
    // Overloaded constructor for integer coordinates
    public GameObject(String id, int x, int y) {
        this(id, "default", x, y);
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void upgrade(GameState gameState) {
        this.level++;
        this.status = "Upgraded (Level " + this.level + ")";
    }

    /**
     * หยุดการทำงานทั้งหมดของ GameObject
     * เช่น timers, animations, หรือการทำงานอื่นๆ
     */
    public void stop() {
        // Override this method in subclasses if they need to stop any ongoing operations
    }
} 