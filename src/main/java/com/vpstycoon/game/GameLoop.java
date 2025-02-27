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
        Random random = new Random();

        while (running) {
            try {
                // 1) สุ่ม CustomerType
                CustomerType[] cTypes = CustomerType.values();
                CustomerType selectedType = cTypes[random.nextInt(cTypes.length)];

                // 2) สุ่ม RequestType (ใช้ค่าจาก enum ที่คุณสร้าง)
                RequestType selectedRequestType = RequestType.values()[random.nextInt(RequestType.values().length)];

                // 3) สร้างเวลาหน่วง (อาจอิงจาก requiredPoints ก็ได้ ถ้าต้องการ)
                int randomTime = 10_000 + random.nextInt(60_000); // ตัวอย่างสุ่ม 1-3 วินาที
                Thread.sleep(randomTime);
//                Thread.sleep(2000);

                // 4) สร้าง CustomerRequest ใหม่
                String randomName = RandomGenerateName.generateRandomName(6);
                double budget = 1000.0;
                int duration = 30;

                CustomerRequest newRequest = new CustomerRequest(
                        selectedType,
                        selectedRequestType, // <- สุ่มที่เราหามา
                        budget,
                        duration
                );

                // add to requestManager
                requestManager.addRequest(new CustomerRequest(
                        CustomerType.INDIVIDUAL,
                        RequestType.SECURITY_FOCUSED,
                        100.0,
                        30
                ));



                System.out.println(
                        "สร้าง Customer ใหม่: " + newRequest.getName()
                                + " | CustomerType: " + selectedType
                                + " | RequestType: " + selectedRequestType
                                + " | Sleep(ms): " + randomTime
                );

            } catch (InterruptedException e) {
                // ถ้า Thread ถูก interrupt (เช่นตอนเรียก stopLoop())
                System.out.println("GameLoop interrupted, stopping loop...");
                running = false;
            }
        }
    }

    // เมื่อต้องการหยุด GameLoop
    public void stopLoop() {
        running = false;
        this.interrupt(); // เรียก interrupt() ด้วยจะได้หลุดจาก sleep ถ้ากำลังหน่วงอยู่
    }
}
