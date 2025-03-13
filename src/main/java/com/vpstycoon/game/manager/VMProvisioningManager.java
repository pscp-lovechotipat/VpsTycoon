package com.vpstycoon.game.manager;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.vps.VPSOptimization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the provisioning of VMs for customer requests and handles customer satisfaction ratings.
 */
public class VMProvisioningManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Map to track which VM is assigned to which customer request
    private final Map<String, CustomerRequest> vmToRequestMap;
    
    // Map to track active customer requests and their payment status
    private final Map<CustomerRequest, VPSOptimization.VM> activeRequests;
    
    // Reference to the company
    private final Company company;
    
    // Random generator for VM creation time
    private final Random random = new Random();
    
    public VMProvisioningManager(Company company) {
        this.vmToRequestMap = new HashMap<>();
        this.activeRequests = new HashMap<>();
        this.company = company;
    }
    
    /**
     * Get the company associated with this VM provisioning manager
     * @return The company
     */
    public Company getCompany() {
        return company;
    }
    
    /**
     * Provision a new VM for a customer request
     * @param request The customer request
     * @param vps The VPS to create the VM on
     * @param vcpus Number of vCPUs to allocate
     * @param ramGB Amount of RAM in GB
     * @param diskGB Amount of disk space in GB
     * @return CompletableFuture that completes when the VM is ready
     */
    public CompletableFuture<VPSOptimization.VM> provisionVM(
            CustomerRequest request, 
            VPSOptimization vps, 
            int vcpus, 
            int ramGB, 
            int diskGB) {
        
        // Create a CompletableFuture to represent the async VM creation
        CompletableFuture<VPSOptimization.VM> future = new CompletableFuture<>();
        
        // Calculate VM creation time (random between 5 seconds and 1 minute)
        int creationTimeMs = 5000 + random.nextInt(55000);
        
        // Start a new thread to simulate VM creation time
        new Thread(() -> {
            try {
                System.out.println("Creating VM for " + request.getName() + 
                        " with " + vcpus + " vCPUs, " + ramGB + "GB RAM, " + 
                        diskGB + "GB disk. ETA: " + (creationTimeMs / 1000) + " seconds");
                
                // Simulate VM creation time
                Thread.sleep(creationTimeMs);
                
                // Create the VM
                String vmName = "vm-" + request.getName().toLowerCase().replace(" ", "-");
                String vmIp = generateRandomIp();
                VPSOptimization.VM vm = new VPSOptimization.VM(
                        vmIp, 
                        vmName, 
                        vcpus, 
                        ramGB + " GB", 
                        diskGB + " GB", 
                        "Running");
                
                // Add VM to VPS
                vps.addVM(vm);
                
                // Calculate customer satisfaction rating
                double ratingChange = calculateRatingChange(request, vcpus, ramGB, diskGB);
                
                // Update company rating
                company.setRating(company.getRating() + ratingChange);
                
                // Track this VM and its associated request
                String vmKey = vmIp + ":" + vmName;
                vmToRequestMap.put(vmKey, request);
                activeRequests.put(request, vm);
                
                // Activate the request to start receiving payments
                request.activate();
                
                // Complete the future with the new VM
                future.complete(vm);
                
                System.out.println("VM created successfully for " + request.getName() + 
                        ". Rating change: " + String.format("%.2f", ratingChange));
                
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        }).start();
        
        return future;
    }
    
    /**
     * Calculate the rating change based on how well the provided VM matches the customer's requirements
     * @param request The customer request
     * @param providedVCPUs Number of vCPUs provided
     * @param providedRamGB Amount of RAM provided in GB
     * @param providedDiskGB Amount of disk space provided in GB
     * @return The rating change (positive or negative)
     */
    private double calculateRatingChange(
            CustomerRequest request, 
            int providedVCPUs, 
            int providedRamGB, 
            int providedDiskGB) {
        
        // Get the required resources
        int requiredVCPUs = request.getRequiredVCPUs();
        int requiredRamGB = request.getRequiredRamGB();
        int requiredDiskGB = request.getRequiredDiskGB();
        
        // Calculate the difference between provided and required resources
        int vcpuDiff = providedVCPUs - requiredVCPUs;
        int ramDiff = providedRamGB - requiredRamGB;
        int diskDiff = providedDiskGB - requiredDiskGB;
        
        // Base rating change
        double ratingChange = 0.0;
        
        // Check if any resource is below requirements
        if (vcpuDiff < 0 || ramDiff < 0 || diskDiff < 0) {
            // Negative rating for under-provisioning
            ratingChange -= 0.1 * Math.abs(Math.min(0, vcpuDiff));
            ratingChange -= 0.05 * Math.abs(Math.min(0, ramDiff));
            ratingChange -= 0.02 * Math.abs(Math.min(0, diskDiff));
        } 
        // If all requirements are met exactly
        else if (vcpuDiff == 0 && ramDiff == 0 && diskDiff == 0) {
            // Perfect match bonus
            ratingChange += 0.2;
        } 
        // If over-provisioned
        else {
            // Positive rating for meeting requirements, but halved for over-provisioning
            ratingChange += 0.1 * Math.min(2, vcpuDiff) / 2;
            ratingChange += 0.05 * Math.min(4, ramDiff) / 2;
            ratingChange += 0.02 * Math.min(10, diskDiff) / 2;
        }
        
        // Adjust rating based on customer type (higher-tier customers are harder to please)
        switch (request.getCustomerType()) {
            case INDIVIDUAL:
                ratingChange *= 1.2; // Individuals are easier to please
                break;
            case SMALL_BUSINESS:
                ratingChange *= 1.1;
                break;
            case MEDIUM_BUSINESS:
                ratingChange *= 1.0; // Neutral
                break;
            case LARGE_BUSINESS:
                ratingChange *= 0.9;
                break;
            case ENTERPRISE:
            case BUSINESS:
                ratingChange *= 0.8; // Enterprises are harder to please
                break;
        }
        
        // Limit the rating change to a reasonable range
        return Math.max(-0.5, Math.min(0.5, ratingChange));
    }
    
    /**
     * Process payments for all active customer requests
     * @param currentTime Current game time
     * @return Total payment received
     */
    public double processPayments(long currentTime) {
        double totalPayment = 0.0;
        
        for (Map.Entry<CustomerRequest, VPSOptimization.VM> entry : activeRequests.entrySet()) {
            CustomerRequest request = entry.getKey();
            
            // Check if payment is due
            if (request.isPaymentDue(currentTime)) {
                // Calculate payment amount
                double paymentAmount = request.getPaymentAmount();
                
                // Add to total
                totalPayment += paymentAmount;
                
                // Record payment time
                request.recordPayment(currentTime);
                
                System.out.println("Received payment of " + String.format("%.2f", paymentAmount) + 
                        " from " + request.getName() + " (" + request.getRentalPeriodType().getDisplayName() + ")");
            }
        }
        
        // Update company money
        if (totalPayment > 0) {
            company.setMoney(company.getMoney() + (long)totalPayment);
        }
        
        return totalPayment;
    }
    
    /**
     * Terminate a VM and deactivate the associated customer request
     * @param vm The VM to terminate
     * @param vps The VPS the VM is running on
     * @return true if successful, false otherwise
     */
    public boolean terminateVM(VPSOptimization.VM vm, VPSOptimization vps) {
        String vmKey = vm.getIp() + ":" + vm.getName();
        CustomerRequest request = vmToRequestMap.get(vmKey);
        
        if (request != null) {
            // Remove VM from VPS
            vps.removeVM(vm);
            
            // Deactivate request
            request.deactivate();
            
            // Remove from tracking maps
            vmToRequestMap.remove(vmKey);
            activeRequests.remove(request);
            
            System.out.println("Terminated VM for " + request.getName());
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the customer request associated with a VM
     * @param vm The VM
     * @return The associated customer request, or null if not found
     */
    public CustomerRequest getRequestForVM(VPSOptimization.VM vm) {
        String vmKey = vm.getIp() + ":" + vm.getName();
        return vmToRequestMap.get(vmKey);
    }
    
    /**
     * Get the VM associated with a customer request
     * @param request The customer request
     * @return The associated VM, or null if not found
     */
    public VPSOptimization.VM getVMForRequest(CustomerRequest request) {
        return activeRequests.get(request);
    }
    
    /**
     * Get all active customer requests
     * @return Map of active requests to their VMs
     */
    public Map<CustomerRequest, VPSOptimization.VM> getActiveRequests() {
        return new HashMap<>(activeRequests);
    }
    
    /**
     * Generate a random IP address
     * @return A random IP address string
     */
    private String generateRandomIp() {
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }
} 