package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;

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

    
    public GameObject(String id, String type, int gridX, int gridY) {
        this.id = id;
        this.type = type;
        this.active = true;
        this.properties = new HashMap<>();
        this.level = 1;
        this.name = type;
        this.status = "Active";

        this.company = ResourceManager.getInstance().getCompany();

        
        setGridPosition(gridX, gridY);
    }





    


    
    
    

    


    
    public void setGridPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
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


    public void upgrade(GameState gameState) {
        this.level++;
        this.status = "Upgraded (Level " + this.level + ")";
    }


    public Company getCompany() {
        return company;
    }
}

