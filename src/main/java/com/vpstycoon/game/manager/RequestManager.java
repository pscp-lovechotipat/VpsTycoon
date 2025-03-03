package com.vpstycoon.game.manager;

import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.vps.enums.RequestType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RequestManager {
    private final ObservableList<CustomerRequest> requests;

    public RequestManager() {
        this.requests = FXCollections.observableArrayList();
        initializeSampleRequests();
    }

    private void initializeSampleRequests() {
        // เพิ่ม request โดยใช้ constructor ที่สุ่ม requirement
        requests.add(new CustomerRequest(CustomerType.INDIVIDUAL, RequestType.WEB_HOSTING,
                100.0, 30));
        requests.add(new CustomerRequest(CustomerType.BUSINESS, RequestType.DATABASE,
                500.0, 60));
        requests.add(new CustomerRequest(CustomerType.ENTERPRISE, RequestType.APP_SERVER,
                1000.0, 90));
    }

    public void addRequest(CustomerRequest request) {
        requests.add(request);
        System.out.println("New request added: " + request.getTitle());
    }

    public ObservableList<CustomerRequest> getRequests() {
        return requests;
    }

    public void acceptRequest(String requestTitle) {
        CustomerRequest request = requests.stream()
                .filter(req -> req.getTitle().equals(requestTitle))
                .findFirst()
                .orElse(null);
        if (request != null) {
            System.out.println("Accepted request: " + requestTitle);
        } else {
            System.out.println("Request not found: " + requestTitle);
        }
    }

    public void completeRequest(CustomerRequest request) {
        if (requests.remove(request)) {
            System.out.println("Completed and removed request: " + request.getTitle());
        } else {
            System.out.println("Failed to complete request: " + request.getTitle());
        }
    }
}