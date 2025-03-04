package com.vpstycoon.game.customer;

import com.vpstycoon.game.customer.enums.CustomerType;

import java.io.Serializable;

public class Customer implements Serializable {
    private static int nextId = 1; // ใช้ static เพื่อนับ ID ถัดไป
    private final int id;          // ทำให้ id เป็น final เพราะไม่ควรเปลี่ยนหลังจากกำหนด
    private String name;
    protected CustomerType customerType; // ใช้ protected เพื่อให้ subclass เข้าถึงได้
    protected double budget;

    public Customer(String name, CustomerType customerType, double budget) {
        this.id = nextId++;        // กำหนด ID และเพิ่มค่า nextId
        this.name = name;
        this.customerType = customerType;
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public double getBudget() {
        return budget;
    }
}