package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RandomGenerateName;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.enums.RequestType;

import java.util.Random;

public class GameLoop extends Thread {
    private GameState gameState;
    private Company company;
    private final RequestManager requestManager; // สมมติว่ามี RequestManager เพื่อเก็บ CustomerRequest
    private boolean running = true;

    public GameLoop(GameState gameState, RequestManager requestManager, Company company) {
        this.gameState = gameState;
        this.requestManager = requestManager;
        this.company = company;
    }

    @Override
    public void run() {

    }

    // เมื่อต้องการหยุด GameLoop
    public void stopLoop() {
        running = false;
        this.interrupt(); // เรียก interrupt() ด้วยจะได้หลุดจาก sleep ถ้ากำลังหน่วงอยู่
    }
}
