package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class VMProvisioningManagerImpl implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    private final Map<String, CustomerRequest> vmToRequestMap;
    
    
    private final Map<CustomerRequest, VPSOptimization.VM> activeRequests;
    
    
    private final Company company;
    
    

    private final SkillPointsSystem skillPointsSystem;

    public VMProvisioningManagerImpl(Company company) {
        this.vmToRequestMap = new HashMap<>();
        this.activeRequests = new HashMap<>();
        this.company = company;
        this.skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
    }

    
    public Company getCompany() {
        return company;
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

