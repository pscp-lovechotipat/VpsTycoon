package com.vpstycoon.game.object;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;

public class VPSObject extends GameObject {
    public VPSObject() {
        super();
    }

    public VPSObject(String id , int level) {
        super(id ,level);
    }

    public VPSObject(String id, int x, int y) {
        super(id, x, y);
    }

    public VPSObject(String id , String type, int x , int y) {
        super(id , type, x, y);
    }

    public void upgrade(GameState gameState) {
        long upgradeCost = calculateUpgradeCost();
        if (gameState.getMoney() >= upgradeCost) {

            gameState.setMoney(gameState.getMoney() - upgradeCost);

            super.upgrade(gameState);
        } else {
            System.out.println("No money to upgrade your VPS");
        }
    }

    private long calculateUpgradeCost() {
        // กำหนดสูตรคำนวณค่าใช้จ่ายอัพเกรดตามความต้องการ
        return (long) (1000 * (super.getLevel() * 1.085));
    }
}
