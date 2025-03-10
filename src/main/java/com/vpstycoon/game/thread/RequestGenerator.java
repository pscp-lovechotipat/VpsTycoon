package com.vpstycoon.game.thread;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RandomGenerateName;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.vps.enums.RequestType;

import java.util.Random;

public class RequestGenerator extends Thread {
    private final RequestManager requestManager;
    private volatile boolean running = true;
    private final int rateLimitSleepTime = 10_000;
    private final int maxRequests = 10;

    public RequestGenerator(RequestManager requestManager) {
        this.requestManager = requestManager;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        Random random = new Random();

        while (!interrupted()) {
            try {
                if (requestManager.getRequests().size() > maxRequests) {
                    System.out.println("RequestGenerator: request limit reached");
                    Thread.sleep(rateLimitSleepTime); // ให้หลับไป 10 วิ แล้วไปเช็คใหม่
                    continue;
                }

                // รอเวลาสุ่มระหว่าง 30-90 วินาที
                int delay = 30_000 + random.nextInt(60_000);
                Thread.sleep(delay);

                // สุ่มสร้าง CustomerRequest
                CustomerType selectedType = CustomerType.values()[random.nextInt(CustomerType.values().length)];
                RequestType selectedRequestType = RequestType.values()[random.nextInt(RequestType.values().length)];
                String randomName = RandomGenerateName.generateRandomName();
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
