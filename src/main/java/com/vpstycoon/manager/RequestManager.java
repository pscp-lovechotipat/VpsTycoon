package com.vpstycoon.manager;

import com.vpstycoon.model.GameState;
import com.vpstycoon.model.company.Company;
import com.vpstycoon.model.customer.CustomerRequest;
import com.vpstycoon.model.customer.CustomerType;
import com.vpstycoon.model.customer.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestManager implements Serializable {
    private final ObservableList<CustomerRequest> pendingRequests;
    private final List<CustomerRequest> completedRequests;
    private final Company company;
    private final Random random = new Random();

    public RequestManager(Company company) {
        this.pendingRequests = FXCollections.observableArrayList();
        this.completedRequests = new ArrayList<>();
        this.company = company;

        GameState currentState = ResourceManager.getInstance().getCurrentState();

        if (currentState != null && currentState.getPendingRequests() != null
            && !currentState.getPendingRequests().isEmpty()) {
            pendingRequests.addAll(currentState.getPendingRequests());
            System.out.println("โหลด pendingRequests จาก GameState: " + pendingRequests.size() + " รายการ");

            if (currentState.getCompletedRequests() != null) {
                completedRequests.addAll(currentState.getCompletedRequests());
                System.out.println("โหลด completedRequests จาก GameState: " + completedRequests.size() + " รายการ");
            }
        }
    }

    public void addRequest(CustomerRequest request) {
        pendingRequests.add(request);
        System.out.println("New request added: " + request.getTitle());
    }

    public ObservableList<CustomerRequest> getRequests() {
        return pendingRequests;
    }

    public CustomerRequest generateRandomRequest() {
        CustomerType selectedType = getRandomCustomerType();

        RequestType selectedRequestType = RequestType.values()[random.nextInt(RequestType.values().length)];

        int requestDuration = 30 + random.nextInt(336);

        CustomerRequest newRequest = new CustomerRequest(
                selectedType, selectedRequestType, requestDuration
        );

        return newRequest;
    }

    private CustomerType getRandomCustomerType() {
        double rating = company.getRating();
        double rand = random.nextDouble() * 100;

        if (rating < 2.0) {
            if (rand < 80) return CustomerType.INDIVIDUAL;
            else if (rand < 95) return CustomerType.SMALL_BUSINESS;
            else return CustomerType.MEDIUM_BUSINESS;
        } else if (rating < 3.0) {
            if (rand < 50) return CustomerType.INDIVIDUAL;
            else if (rand < 80) return CustomerType.SMALL_BUSINESS;
            else if (rand < 95) return CustomerType.MEDIUM_BUSINESS;
            else return CustomerType.LARGE_BUSINESS;
        } else if (rating < 4.0) {
            if (rand < 30) return CustomerType.INDIVIDUAL;
            else if (rand < 50) return CustomerType.SMALL_BUSINESS;
            else if (rand < 75) return CustomerType.MEDIUM_BUSINESS;
            else if (rand < 95) return CustomerType.LARGE_BUSINESS;
            else return CustomerType.ENTERPRISE;
        } else {
            if (rand < 15) return CustomerType.INDIVIDUAL;
            else if (rand < 30) return CustomerType.SMALL_BUSINESS;
            else if (rand < 50) return CustomerType.MEDIUM_BUSINESS;
            else if (rand < 75) return CustomerType.LARGE_BUSINESS;
            else return CustomerType.ENTERPRISE;
        }
    }

    public List<CustomerRequest> getCompletedRequests() {
        return new ArrayList<>(completedRequests);
    }

    public Company getCompany() {
        return company;
    }

    public void setRequests(List<CustomerRequest> requests) {
        if (requests == null) {
            return;
        }
        pendingRequests.clear();
        pendingRequests.addAll(requests);
        System.out.println("Updated pendingRequests: " + pendingRequests.size() + " requests");
    }

    public void setCompletedRequests(List<CustomerRequest> requests) {
        if (requests == null) {
            return;
        }
        completedRequests.clear();
        completedRequests.addAll(requests);
        System.out.println("Updated completedRequests: " + completedRequests.size() + " requests");
    }

    public void resetRequests() {
        if (pendingRequests != null) {
            pendingRequests.clear();
            System.out.println("รีเซ็ต pendingRequests เรียบร้อย");
        }

        if (completedRequests != null) {
            completedRequests.clear();
            System.out.println("รีเซ็ต completedRequests เรียบร้อย");
        }
    }
}
