package com.vpstycoon.model.common;

import com.vpstycoon.model.company.interfaces.ICompany;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * คลาสพื้นฐานสำหรับวัตถุในเกม
 */
public class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /**
     * สร้างวัตถุเกมเปล่า
     */
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

    /**
     * สร้างวัตถุเกมใหม่ด้วยพารามิเตอร์ที่ระบุ
     */
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

    /**
     * สร้างวัตถุเกมใหม่ด้วย ID และระดับที่ระบุ
     */
    public GameObject(String id, int level) {
        this.id = id;
        this.properties = new HashMap<>();
        this.level = level;
        this.active = true;
        this.status = "Active";
    }

    /**
     * สร้างวัตถุเกมใหม่ด้วย ID และตำแหน่งกริด
     */
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

    /**
     * แปลงตำแหน่งกริด X เป็นตำแหน่งพิกเซล
     */
    public double getX() {
        return gridX * CELL_SIZE;
    }

    /**
     * แปลงตำแหน่งกริด Y เป็นตำแหน่งพิกเซล
     */
    public double getY() {
        return gridY * CELL_SIZE;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    /**
     * ตั้งค่าตำแหน่งกริดของวัตถุ
     */
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

    /**
     * อัพเกรดวัตถุเกม เพิ่มระดับและอัพเดทสถานะ
     */
    public void upgrade(GameState gameState) {
        this.level++;
        this.status = "Upgraded (Level " + this.level + ")";
    }

    /**
     * หยุดการทำงานของวัตถุเกม
     */
    public void stop() {
        // ให้คลาสลูกทำการ override
    }

    public ICompany getCompany() {
        return company;
    }
    
    public void setCompany(ICompany company) {
        this.company = company;
    }
} 