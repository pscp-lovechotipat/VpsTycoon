package com.vpstycoon.game.object;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;

public class RackObject extends GameObject {
    private Runnable onClickAction;




    public RackObject(String id , String type, int x , int y) {
        super(id , type, x, y);
    }

    public void upgrade(GameState gameState) {
        long upgradeCost = calculateUpgradeCost();
        if (super.getCompany().getMoney() >= upgradeCost) {

            super.getCompany().setMoney(super.getCompany().getMoney() - upgradeCost);

            super.upgrade(gameState);
        } else {
            System.out.println("No money to upgrade your VPS");
        }
    }

    private long calculateUpgradeCost() {
        
        return (long) (1000 * (super.getLevel() * 1.085));
    }


}


