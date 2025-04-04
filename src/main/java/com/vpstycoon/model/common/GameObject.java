package com.vpstycoon.model.common;

import com.vpstycoon.model.company.interfaces.ICompany;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class GameObject implements Serializable {

    private static final double CELL_SIZE = 64.0;  

    private String id;
    private String type;
    private int gridX;
    private int gridY;
    private boolean active;
    private final Map<String, Object> properties;
    private String name;
    private int level;
    private String status;
    private ICompany company;

    
    public GameObject() {
        this.id = "";
        this.type = "";
        this.gridX = 0;
        this.gridY = 0;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.name = "";
        this.status = "Active";
    }

    
    public GameObject(String id, String type, int gridX, int gridY) {
        this.id = id;
        this.type = type;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.name = type;
        this.status = "Active";
        setGridPosition(gridX, gridY);
    }

    
    public GameObject(String id, int level) {
        this.id = id;
        this.properties = new HashMap<>();
        this.level = level;
        this.active = true;
        this.status = "Active";
    }

    
    public GameObject(String id, int x, int y) {
        this.id = id;
        this.gridX = x;
        this.gridY = y;
        this.properties = new HashMap<>();
        this.active = true;
        this.level = 1;
        this.status = "Active";
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    
    public double getX() {
        return gridX * CELL_SIZE;
    }

    
    public double getY() {
        return gridY * CELL_SIZE;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    
    public void setGridPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
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

    
    public void stop() {
        
    }

    public ICompany getCompany() {
        return company;
    }
    
    public void setCompany(ICompany company) {
        this.company = company;
    }
} 
