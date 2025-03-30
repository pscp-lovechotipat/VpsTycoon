package com.vpstycoon.game.manager;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.RequestType;
import com.vpstycoon.ui.game.desktop.messenger.models.VMProvisioningManagerImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RequestManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final ObservableList<CustomerRequest> pendingRequests;
    private final List<CustomerRequest> completedRequests;
    private final VMProvisioningManagerImpl vmProvisioningManager;
    private final Company company;
    private final Random random = new Random();
    
    
    private static final long GAME_MONTH_MS = 15 * 60 * 1000;
    
    public RequestManager(Company company) {
        this.pendingRequests = FXCollections.observableArrayList();
        this.completedRequests = new ArrayList<>();
        this.company = company;
        this.vmProvisioningManager = new VMProvisioningManagerImpl(company);
        
        
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

    
    public CompletableFuture<VPSOptimization.VM> acceptRequest(
            CustomerRequest request, 
            VPSOptimization vps, 
            int vcpus, 
            int ramGB, 
            int diskGB) {
        
        if (!pendingRequests.contains(request)) {
            CompletableFuture<VPSOptimization.VM> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Request not found"));
            return future;
        }

        request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
        
        
        return vmProvisioningManager.provisionVM(request, vps, vcpus, ramGB, diskGB);
    }

    
    public boolean completeRequest(CustomerRequest request, VPSOptimization vps) {
        VPSOptimization.VM vm = vmProvisioningManager.getVMForRequest(request);
        
        if (vm != null) {
            
            boolean success = vmProvisioningManager.terminateVM(vm, vps);
            
            if (success) {
                
                completedRequests.add(request);
                System.out.println("Completed request: " + request.getTitle());
                return true;
            }
        }
        
        return false;
    }
    
    
    public CustomerRequest generateRandomRequest() {
        
        CustomerType selectedType = getRandomCustomerType();
        
        
        RequestType selectedRequestType = RequestType.values()[random.nextInt(RequestType.values().length)];
        
        
        double budget = getRandomBudget(selectedType);
        
        
        int requestDuration = 30 + random.nextInt(336);
        
        
        CustomerRequest newRequest = new CustomerRequest(
                selectedType,
                selectedRequestType,
                budget,
                requestDuration
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
    
    
    private double getRandomBudget(CustomerType customerType) {
        switch (customerType) {
            case INDIVIDUAL:
                return 100 + random.nextDouble() * 400;
            case SMALL_BUSINESS:
                return 500 + random.nextDouble() * 1500;
            case MEDIUM_BUSINESS:
                return 2000 + random.nextDouble() * 3000;
            case LARGE_BUSINESS:
                return 5000 + random.nextDouble() * 5000;
            case ENTERPRISE:
            case BUSINESS:
                return 10000 + random.nextDouble() * 10000;
            default:
                return 1000;
        }
    }
    
    
    public void processPayments(long currentTime) {
        vmProvisioningManager.processPayments(currentTime);
    }
    
    
    public java.util.Map<CustomerRequest, VPSOptimization.VM> getActiveRequests() {
        return vmProvisioningManager.getActiveRequests();
    }
    
    
    public List<CustomerRequest> getCompletedRequests() {
        return new ArrayList<>(completedRequests);
    }
    
    
    public VMProvisioningManagerImpl getVmProvisioningManager() {
        return vmProvisioningManager;
    }
    
    
    public static int realTimeToGameDays(long realTimeMs) {
        return (int) (realTimeMs * 30 / GAME_MONTH_MS);
    }
    
    
    public static long gameDaysToRealTime(int gameDays) {
        return gameDays * GAME_MONTH_MS / 30;
    }

    
    public void addCompletedRequests(List<CustomerRequest> requests) {
        if (requests != null) {
            this.completedRequests.addAll(requests);
            System.out.println("เพิ่ม " + requests.size() + " รายการลงใน completedRequests");
        }
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
        
        
        if (vmProvisioningManager != null) {
            vmProvisioningManager.resetAllVMs();
            System.out.println("รีเซ็ต VMProvisioningManager เรียบร้อย");
        }
    }
}
