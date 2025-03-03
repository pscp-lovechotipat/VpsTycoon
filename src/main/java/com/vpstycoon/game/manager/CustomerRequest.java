package com.vpstycoon.game.manager;

import com.vpstycoon.game.customer.Customer;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;
import java.util.Random;

public class CustomerRequest extends Customer {
    private final RequestType requestType;
    private final int duration;
    private final int requiredVCPUs;
    private final String requiredRam;
    private final String requiredDisk;

    // Constructor เดิมที่ระบุ requirement ชัดเจน
    public CustomerRequest(CustomerType customerType, RequestType requestType,
                           double budget, int duration, int requiredVCPUs,
                           String requiredRam, String requiredDisk) {
        super(RandomGenerateName.generateRandomName(10), customerType, budget);
        this.requestType = requestType;
        this.duration = duration;
        this.requiredVCPUs = requiredVCPUs;
        this.requiredRam = requiredRam;
        this.requiredDisk = requiredDisk;
    }

    // Constructor ใหม่ที่สุ่ม requirement
    public CustomerRequest(CustomerType customerType, RequestType requestType,
                           double budget, int duration) {
        super(RandomGenerateName.generateRandomName(10), customerType, budget);
        this.requestType = requestType;
        this.duration = duration;

        // สุ่ม requirement
        Random random = new Random();
        this.requiredVCPUs = random.nextInt(4) + 1; // สุ่ม vCPUs 1-4
        this.requiredRam = (random.nextInt(4) + 1) * 2 + " GB"; // สุ่ม RAM 2, 4, 6, 8 GB
        this.requiredDisk = (random.nextInt(5) + 1) * 10 + " GB"; // สุ่ม Disk 10, 20, 30, 40, 50 GB
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public double getBudget() {
        return budget;
    }

    public int getDuration() {
        return duration;
    }

    public int getRequiredVCPUs() {
        return requiredVCPUs;
    }

    public String getRequiredRam() {
        return requiredRam;
    }

    public String getRequiredDisk() {
        return requiredDisk;
    }

    public String getTitle() {
        return getName() + " - " + requestType.toString();
    }
}