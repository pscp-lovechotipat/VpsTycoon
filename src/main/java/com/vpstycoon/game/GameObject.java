package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;

import java.io.Serializable;

public class GameObject implements Serializable {



    

    private String name;
    private int level;

    private Company company;

    public GameObject() {
        this.level = 1;
        this.name = "";

        this.company = ResourceManager.getInstance().getCompany();
    }

    
    public GameObject(String id, String type, int gridX, int gridY) {
        this.level = 1;
        this.name = type;

        this.company = ResourceManager.getInstance().getCompany();

        
        setGridPosition(gridX, gridY);
    }





    


    
    
    

    


    
    public void setGridPosition(int gridX, int gridY) {
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
    }


    public Company getCompany() {
        return company;
    }
}

