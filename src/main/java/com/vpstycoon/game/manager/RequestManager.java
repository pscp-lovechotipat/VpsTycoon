package com.vpstycoon.game.manager;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.RequestType;
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
    private final VMProvisioningManager vmProvisioningManager;
    private final Company company;
    private final Random random = new Random();
    
    // Game time settings (1 month = 15 minutes)
    private static final long GAME_MONTH_MS = 15 * 60 * 1000;
    
    public RequestManager(Company company) {
        this.pendingRequests = FXCollections.observableArrayList();
        this.completedRequests = new ArrayList<>();
        this.company = company;
        this.vmProvisioningManager = new VMProvisioningManager(company);
        
        // ตรวจสอบใน GameState ก่อนว่ามี pendingRequests ที่บันทึกไว้หรือไม่
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        boolean hasExistingRequests = false;
        
        if (currentState != null && currentState.getPendingRequests() != null 
            && !currentState.getPendingRequests().isEmpty()) {
            // มีข้อมูล pendingRequests ที่บันทึกไว้ ไม่ต้องสร้างใหม่
            pendingRequests.addAll(currentState.getPendingRequests());
            System.out.println("โหลด pendingRequests จาก GameState: " + pendingRequests.size() + " รายการ");
            hasExistingRequests = true;
            
            // โหลด completedRequests ถ้ามี
            if (currentState.getCompletedRequests() != null) {
                completedRequests.addAll(currentState.getCompletedRequests());
                System.out.println("โหลด completedRequests จาก GameState: " + completedRequests.size() + " รายการ");
            }
        }
        
        // ถ้าไม่มีข้อมูลที่บันทึกไว้ ให้สร้าง request ตัวอย่าง
        if (!hasExistingRequests) {
            initializeSampleRequests();
        }
    }

    private void initializeSampleRequests() {
        // Add initial requests with random requirements
        pendingRequests.add(new CustomerRequest(CustomerType.INDIVIDUAL, RequestType.WEB_HOSTING,
                100.0, 30));
        pendingRequests.add(new CustomerRequest(CustomerType.SMALL_BUSINESS, RequestType.DATABASE,
                500.0, 60));
        pendingRequests.add(new CustomerRequest(CustomerType.MEDIUM_BUSINESS, RequestType.APP_SERVER,
                1000.0, 90));
    }

    /**
     * Add a new customer request
     * @param request The request to add
     */
    public void addRequest(CustomerRequest request) {
        pendingRequests.add(request);
        System.out.println("New request added: " + request.getTitle());
    }

    /**
     * Get all pending customer requests
     * @return Observable list of pending requests
     */
    public ObservableList<CustomerRequest> getRequests() {
        return pendingRequests;
    }

    /**
     * Accept a request and provision a VM for it
     * @param request The request to accept
     * @param vps The VPS to provision the VM on
     * @param vcpus Number of vCPUs to allocate
     * @param ramGB Amount of RAM in GB
     * @param diskGB Amount of disk space in GB
     * @return CompletableFuture that completes when the VM is ready
     */
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
        
        // ไม่ลบ request ออกจาก pendingRequests เพื่อให้ยังแสดงในรายการ
        // pendingRequests.remove(request);
        
        // แทนที่จะลบออก เราจะทำเครื่องหมายว่า request นี้ได้รับการจัดการแล้ว
        // โดยการเรียกใช้ activate() บน request
        request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
        
        // Provision VM
        return vmProvisioningManager.provisionVM(request, vps, vcpus, ramGB, diskGB);
    }

    /**
     * Complete a request and terminate its VM
     * @param request The request to complete
     * @param vps The VPS the VM is running on
     * @return true if successful, false otherwise
     */
    public boolean completeRequest(CustomerRequest request, VPSOptimization vps) {
        VPSOptimization.VM vm = vmProvisioningManager.getVMForRequest(request);
        
        if (vm != null) {
            // Terminate VM
            boolean success = vmProvisioningManager.terminateVM(vm, vps);
            
            if (success) {
                // Add to completed requests
                completedRequests.add(request);
                System.out.println("Completed request: " + request.getTitle());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Generate a new random customer request
     * @return The generated request
     */
    public CustomerRequest generateRandomRequest() {
        // Random customer type weighted by company rating
        CustomerType selectedType = getRandomCustomerType();
        
        // Random request type
        RequestType selectedRequestType = RequestType.values()[random.nextInt(RequestType.values().length)];
        
        // Random budget based on customer type
        double budget = getRandomBudget(selectedType);
        
        // Random duration (30-365 days)
        int requestDuration = 30 + random.nextInt(336);
        
        // Create and return the request
        CustomerRequest newRequest = new CustomerRequest(
                selectedType,
                selectedRequestType,
                budget,
                requestDuration
        );
        
        return newRequest;
    }
    
    /**
     * Get a random customer type weighted by company rating
     * @return Random customer type
     */
    private CustomerType getRandomCustomerType() {
        double rating = company.getRating();
        double rand = random.nextDouble() * 100;
        
        // Higher rating increases chances of better customers
        if (rating < 2.0) {
            // Low rating - mostly individuals
            if (rand < 80) return CustomerType.INDIVIDUAL;
            else if (rand < 95) return CustomerType.SMALL_BUSINESS;
            else return CustomerType.MEDIUM_BUSINESS;
        } else if (rating < 3.0) {
            // Medium rating - more small businesses
            if (rand < 50) return CustomerType.INDIVIDUAL;
            else if (rand < 80) return CustomerType.SMALL_BUSINESS;
            else if (rand < 95) return CustomerType.MEDIUM_BUSINESS;
            else return CustomerType.LARGE_BUSINESS;
        } else if (rating < 4.0) {
            // Good rating - more medium and large businesses
            if (rand < 30) return CustomerType.INDIVIDUAL;
            else if (rand < 50) return CustomerType.SMALL_BUSINESS;
            else if (rand < 75) return CustomerType.MEDIUM_BUSINESS;
            else if (rand < 95) return CustomerType.LARGE_BUSINESS;
            else return CustomerType.ENTERPRISE;
        } else {
            // Excellent rating - more enterprises
            if (rand < 15) return CustomerType.INDIVIDUAL;
            else if (rand < 30) return CustomerType.SMALL_BUSINESS;
            else if (rand < 50) return CustomerType.MEDIUM_BUSINESS;
            else if (rand < 75) return CustomerType.LARGE_BUSINESS;
            else return CustomerType.ENTERPRISE;
        }
    }
    
    /**
     * Get a random budget based on customer type
     * @param customerType The customer type
     * @return Random budget
     */
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
    
    /**
     * Process payments for all active customer requests
     *
     * @param currentTime Current game time
     */
    public void processPayments(long currentTime) {
        vmProvisioningManager.processPayments(currentTime);
    }
    
    /**
     * Get all active customer requests
     * @return Map of active requests to their VMs
     */
    public java.util.Map<CustomerRequest, VPSOptimization.VM> getActiveRequests() {
        return vmProvisioningManager.getActiveRequests();
    }
    
    /**
     * Get all completed customer requests
     * @return List of completed requests
     */
    public List<CustomerRequest> getCompletedRequests() {
        return new ArrayList<>(completedRequests);
    }
    
    /**
     * Get the VM provisioning manager
     * @return The VM provisioning manager
     */
    public VMProvisioningManager getVmProvisioningManager() {
        return vmProvisioningManager;
    }
    
    /**
     * Convert real time to game time
     * @param realTimeMs Real time in milliseconds
     * @return Game time in days
     */
    public static int realTimeToGameDays(long realTimeMs) {
        return (int) (realTimeMs * 30 / GAME_MONTH_MS);
    }
    
    /**
     * Convert game time to real time
     * @param gameDays Game time in days
     * @return Real time in milliseconds
     */
    public static long gameDaysToRealTime(int gameDays) {
        return gameDays * GAME_MONTH_MS / 30;
    }

    /**
     * Add multiple completed requests at once (used when loading from save file)
     * @param requests List of completed requests to add
     */
    public void addCompletedRequests(List<CustomerRequest> requests) {
        if (requests != null) {
            this.completedRequests.addAll(requests);
            System.out.println("เพิ่ม " + requests.size() + " รายการลงใน completedRequests");
        }
    }
}