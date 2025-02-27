package com.vpstycoon.game.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RequestManager {
    private final ObservableList<CustomerRequest> requests;

    public RequestManager() {
        this.requests = FXCollections.observableArrayList();
    }

    public void addRequest(CustomerRequest request) {
        requests.add(request);
        System.out.println("New request added: " + request.getTitle());
    }

    public ObservableList<CustomerRequest> getRequests() {
        return requests; // ✅ คืนค่า ObservableList
    }

    public void acceptRequest(String requestTitle) {
        requests.removeIf(req -> req.getTitle().equals(requestTitle));
        System.out.println("Accepted request: " + requestTitle);
    }
}
