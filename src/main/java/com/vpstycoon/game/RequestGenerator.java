package com.vpstycoon.game;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RandomGenerateName;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.enums.RequestType;

import java.util.Random;

public class RequestGenerator extends Thread {
    private final RequestManager requestManager;
    private volatile boolean running = true;

    public RequestGenerator(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (running) {
            try {
                // รอเวลาสุ่มระหว่าง 10-60 วินาที
                int delay = 10_000 + random.nextInt(50_000);
                Thread.sleep(delay);

                // สุ่มสร้าง CustomerRequest
                CustomerType selectedType = CustomerType.values()[random.nextInt(CustomerType.values().length)];
                RequestType selectedRequestType = RequestType.values()[random.nextInt(RequestType.values().length)];
                String randomName = RandomGenerateName.generateRandomName(6);
                double budget = 1000.0;
                int requestDuration = 30;

                CustomerRequest newRequest = new CustomerRequest(
                        selectedType,
                        selectedRequestType,
                        budget,
                        requestDuration
                );

                requestManager.addRequest(newRequest);
                System.out.println("New Customer Request: " + randomName +
                        " | Type: " + selectedType +
                        " | Request: " + selectedRequestType +
                        " | Delay(ms): " + delay);

            } catch (InterruptedException e) {
                System.out.println("RequestGenerator interrupted, stopping...");
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopGenerator() {
        running = false;
        this.interrupt();
    }
}
