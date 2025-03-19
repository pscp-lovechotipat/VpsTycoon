package com.vpstycoon.game.object;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;

public class RackObject extends GameObject {
    private Runnable onClickAction;

    public RackObject() {
        super();
    }

    public RackObject(String id , int level) {
        super(id ,level);
    }

    public RackObject(String id, int x, int y) {
        super(id, x, y);
    }

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
        // กำหนดสูตรคำนวณค่าใช้จ่ายอัพเกรดตามความต้องการ
        return (long) (1000 * (super.getLevel() * 1.085));
    }

    public void setOnClick(Runnable action) {
        this.onClickAction = action;
    }

    public void click() {
        if (onClickAction != null) {
            onClickAction.run();
        }
    }
}
