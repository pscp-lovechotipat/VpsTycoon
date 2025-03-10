package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final double CELL_SIZE = 64.0;  // << กำหนดขนาดกริด

    private String id;
    private String type;

    // เปลี่ยนจาก double x, y เป็น int gridX, gridY
    private int gridX;
    private int gridY;

    private boolean active;
    private final Map<String, Object> properties;
    private String name;
    private int level;
    private String status;

    private Company company;

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

        this.company = ResourceManager.getInstance().getCompany();
    }

    // Constructor หลักที่ใช้ gridX, gridY
    public GameObject(String id, String type, int gridX, int gridY) {
        this.id = id;
        this.type = type;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.name = type;
        this.status = "Active";

        this.company = ResourceManager.getInstance().getCompany();

        // เรียกเมธอด setGridPosition() เพื่อเซตค่า
        setGridPosition(gridX, gridY);
    }

    public GameObject(String id, int level) {
        this.id = id;
        this.properties = new HashMap<>();
        this.level = level;
    }

    public GameObject(String id, int x, int y) {
        this.id = id;
        this.gridX = x;
        this.gridY = y;
        this.properties = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    // คืนค่าเป็นพิกเซล
    public double getX() {
        return gridX * CELL_SIZE;
    }

    public double getY() {
        return gridY * CELL_SIZE;
    }

    // ถ้าอยากให้แก้ได้ด้วยการ setX / setY ก็สามารถทำ Overload หรือไม่ทำก็ได้
    // public void setX(double x) { this.gridX = (int)Math.round(x / CELL_SIZE); }
    // public void setY(double y) { this.gridY = (int)Math.round(y / CELL_SIZE); }

    // คืนค่า gridX, gridY ตรง ๆ (ถ้าต้องการ)
    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    // เมธอดหลักสำหรับกำหนดตำแหน่งในกริด
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
        // ถ้ามีการหยุดการทำงานใน subclasses
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
}