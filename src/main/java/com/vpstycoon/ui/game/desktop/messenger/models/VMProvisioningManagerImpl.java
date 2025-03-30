package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.CustomerRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;


public class VMProvisioningManagerImpl implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    private final Map<String, CustomerRequest> vmToRequestMap;
    
    
    private final Map<CustomerRequest, VPSOptimization.VM> activeRequests;
    
    
    private final Company company;
    
    
    private final Random random = new Random();

    private final SkillPointsSystem skillPointsSystem;
    private int deployLevel = 1;

    public VMProvisioningManagerImpl(Company company) {
        this.vmToRequestMap = new HashMap<>();
        this.activeRequests = new HashMap<>();
        this.company = company;
        this.skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
    }

    
    public Company getCompany() {
        return company;
    }
    
    
    public CompletableFuture<VPSOptimization.VM> provisionVM(
            CustomerRequest request, 
            VPSOptimization vps, 
            int vcpus, 
            int ramGB, 
            int diskGB) {
        
        
        CompletableFuture<VPSOptimization.VM> future = new CompletableFuture<>();

        int baseTimeMs = 5000 + random.nextInt(55000);

        
        SkillPointsSystem skillSystem = ResourceManager.getInstance().getSkillPointsSystem();
        int deployLevel = skillSystem.getSkillLevel(SkillPointsSystem.SkillType.DEPLOY);

        
        double reductionRate = skillSystem.getDeploymentTimeReduction();

        int creationTimeMs = (int)(baseTimeMs * (1.0 - reductionRate));

        
        Thread vmCreationThread = new Thread(() -> {
            try {
                System.out.println("Creating VM for " + request.getName() + 
                        " with " + vcpus + " vCPUs, " + ramGB + "GB RAM, " + 
                        diskGB + "GB disk. ETA: " + (creationTimeMs / 1000) + " seconds");
                
                
                Thread.sleep(creationTimeMs);
                
                
                String vmName = "vm-" + request.getName().toLowerCase().replace(" ", "-");
                VPSOptimization.VM vm = new VPSOptimization.VM(
                        vmName, 
                        vcpus, 
                        ramGB, 
                        diskGB
                );
                
                
                vm.setIp(generateRandomIp());
                vm.setStatus("Running");
                
                
                vps.addVM(vm);
                
                
                double ratingChange = calculateRatingChange(request, vcpus, ramGB, diskGB);
                
                
                company.setRating(company.getRating() + ratingChange);
                
                
                String vmKey = vm.getIp() + ":" + vm.getName();
                vmToRequestMap.put(vmKey, request);
                activeRequests.put(request, vm);
                
                
                request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs());
                
                
                double paymentAmount = request.getPaymentAmount();
                
                
                double securityBonus = skillPointsSystem.getSecurityPaymentBonus();
                if (securityBonus > 0) {
                    double bonusAmount = paymentAmount * securityBonus;
                    paymentAmount += bonusAmount;
                    System.out.println("Security bonus applied: +" + String.format("%.2f", bonusAmount) + 
                            " (" + (securityBonus * 100) + "%)");
                }
                
                
                company.addMoney(paymentAmount);
                
                System.out.println("Received initial payment of " + String.format("%.2f", paymentAmount) + 
                        " from " + request.getName() + " (" + request.getRentalPeriodType().getDisplayName() + ")");
                
                
                future.complete(vm);
                
                System.out.println("VM created successfully for " + request.getName() + 
                        ". Rating change: " + String.format("%.2f", ratingChange));
                
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        vmCreationThread.setDaemon(true);
        vmCreationThread.start();
        
        return future;
    }
    
    
    private double calculateRatingChange(
            CustomerRequest request, 
            int providedVCPUs, 
            int providedRamGB, 
            int providedDiskGB) {
        
        
        int requiredVCPUs = request.getRequiredVCPUs();
        int requiredRamGB = request.getRequiredRamGB();
        int requiredDiskGB = request.getRequiredDiskGB();
        
        
        double ratingChange = company.calculateVMAssignmentRatingChange(
            requiredVCPUs, requiredRamGB, requiredDiskGB,
            providedVCPUs, providedRamGB, providedDiskGB
        );
        
        
        switch (request.getCustomerType()) {
            case INDIVIDUAL:
                ratingChange *= 1.2; 
                break;
            case SMALL_BUSINESS:
                ratingChange *= 1.1;
                break;
            case MEDIUM_BUSINESS:
                ratingChange *= 1.0; 
                break;
            case LARGE_BUSINESS:
                ratingChange *= 0.9;
                break;
            case ENTERPRISE:
            case BUSINESS:
                ratingChange *= 0.8; 
                break;
        }
        
        
        return Math.max(-0.5, Math.min(0.5, ratingChange));
    }
    
    
    public double processPayments(long currentTime) {
        double totalPayment = 0.0;
        
        for (Map.Entry<CustomerRequest, VPSOptimization.VM> entry : activeRequests.entrySet()) {
            CustomerRequest request = entry.getKey();
            
            
            if (request.isPaymentDue(currentTime)) {
                
                double paymentAmount = request.getPaymentAmount();
                
                
                double securityBonus = skillPointsSystem.getSecurityPaymentBonus();
                if (securityBonus > 0) {
                    double bonusAmount = paymentAmount * securityBonus;
                    paymentAmount += bonusAmount;
                    System.out.println("Security bonus applied: +" + String.format("%.2f", bonusAmount) + 
                            " (" + (securityBonus * 100) + "%)");
                }
                
                
                totalPayment += paymentAmount;
                
                
                request.recordPayment(currentTime);
                
                System.out.println("Received payment of " + String.format("%.2f", paymentAmount) + 
                        " from " + request.getName() + " (" + request.getRentalPeriodType().getDisplayName() + ")");
            }
        }
        
        
        if (totalPayment > 0) {
            company.addMoney(totalPayment);
        }
        
        return totalPayment;
    }
    
    
    public boolean terminateVM(VPSOptimization.VM vm, VPSOptimization vps) {
        String vmKey = vm.getIp() + ":" + vm.getName();
        CustomerRequest request = vmToRequestMap.get(vmKey);
        
        if (request != null) {
            
            vps.removeVM(vm);
            
            
            request.deactivate();
            
            
            vmToRequestMap.remove(vmKey);
            activeRequests.remove(request);
            
            System.out.println("Terminated VM for " + request.getName());
            return true;
        }
        
        return false;
    }
    
    
    public CustomerRequest getRequestForVM(VPSOptimization.VM vm) {
        if (vm == null) {
            return null;
        }
        
        
        String vmKey = vm.getIp() + ":" + vm.getName();
        
        
        CustomerRequest request = vmToRequestMap.get(vmKey);
        
        
        if (request == null) {
            for (Map.Entry<CustomerRequest, VPSOptimization.VM> entry : activeRequests.entrySet()) {
                if (entry.getValue().equals(vm)) {
                    return entry.getKey();
                }
            }
        }
        
        return request;
    }
    
    
    public VPSOptimization.VM getVMForRequest(CustomerRequest request) {
        return activeRequests.get(request);
    }
    
    
    public Map<CustomerRequest, VPSOptimization.VM> getActiveRequests() {
        return new HashMap<>(activeRequests);
    }
    
    
    private String generateRandomIp() {
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }

    
    public void resetAllVMs() {
        if (vmToRequestMap != null) {
            vmToRequestMap.clear();
            System.out.println("รีเซ็ต vmToRequestMap เรียบร้อย");
        }
        
        if (activeRequests != null) {
            activeRequests.clear();
            System.out.println("รีเซ็ต activeRequests เรียบร้อย");
        }
    }
} 
