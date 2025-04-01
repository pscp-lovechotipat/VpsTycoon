package com.vpstycoon.game.company;

import com.vpstycoon.game.vps.enums.VPSProduct;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SkillPointsSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<SkillType, Integer> skillLevels = new HashMap<>();
    private final Company company;

    
    public enum SkillType {
        RACK_SLOTS("Rack Slots", "Increases the number of slots available in each rack", 4),
        NETWORK_SPEED("Network Speed", "Increases the speed of your network connections", 4),
        SERVER_EFFICIENCY("Server Efficiency", "Improves server performance and reduces costs", 4),
        MARKETING("Marketing", "Improves your company's visibility and attracts more customers", 4),
        SECURITY("Security", "Enhances your security systems and reduces the risk of attacks", 4),
        MANAGEMENT("Management", "Improves your ability to manage resources and staff", 4),
        DEPLOY("Deploy", "Reduces VM deployment time to customers", 4);
        
        private final String name;
        private final String description;
        private final int maxLevel;

        SkillType(String name, String description, int maxLevel) {
            this.name = name;
            this.description = description;
            this.maxLevel = maxLevel;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getMaxLevel() {
            return maxLevel;
        }
    }

    public SkillPointsSystem(Company company) {
        this.company = company;
        initializeSkillLevels(); 
    }

    
    public SkillPointsSystem(Company company, Map<SkillType, Integer> savedSkillLevels) {
        this.company = company;
        
        
        if (savedSkillLevels != null && !savedSkillLevels.isEmpty()) {
            this.skillLevels.putAll(savedSkillLevels);
            
            
            for (SkillType skill : SkillType.values()) {
                if (!this.skillLevels.containsKey(skill)) {
                    this.skillLevels.put(skill, 1); 
                }
            }
        } else {
            
            initializeSkillLevels();
        }
    }

    private void initializeSkillLevels() {
        for (SkillType skill : SkillType.values()) {
            skillLevels.put(skill, 1); 
        }
    }

    
    public Map<SkillType, Integer> getSkillLevelsMap() {
        return new HashMap<>(skillLevels);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
    }

    
    public void addPoints(int points) {
        this.company.addSkillPoints(points);
    }

    
    public int getAvailablePoints() {
        return company.getSkillPointsAvailable();
    }
    
    
    public int getSkillLevel(SkillType skillType) {
        return skillLevels.getOrDefault(skillType, 1); 
    }
    
    
    public int getRackSlotUpgradeDiscount() {
        int skillLevel = getSkillLevel(SkillType.RACK_SLOTS);
        
        return (skillLevel > 1) ? (skillLevel - 1) * 10 : 0;
    }
    
    
    public boolean upgradeSkill(SkillType skillType) {
        int currentLevel = getSkillLevel(skillType);
        if (currentLevel >= skillType.getMaxLevel()) {
            return false;
        }

        int cost = calculateUpgradeCost(currentLevel);
        if (company.getSkillPointsAvailable() >= cost) {
            company.setSkillPointsAvailable(company.getSkillPointsAvailable() - cost);
            skillLevels.put(skillType, currentLevel + 1);
            if (skillType == SkillType.MARKETING) {
                addPoints(5);
            }
            return true;
        }
        return false;
    }

    
    public int calculateUpgradeCost(int currentLevel) {
        
        return currentLevel * 100;
    }
    
    
    public int getAvailableRackSlots() {
        
        return 4 + (getSkillLevel(SkillType.RACK_SLOTS) * 2);
    }
    
    
    public double getNetworkSpeedMultiplier() {
        
        return 1.0 + (getSkillLevel(SkillType.NETWORK_SPEED) * 0.2);
    }
    
    
    public double getServerEfficiencyMultiplier() {
        
        return 1.0 + (getSkillLevel(SkillType.SERVER_EFFICIENCY) * 0.15);
    }
    
    
    public double getMarketingBonus() {
        
        return 1.0 + (getSkillLevel(SkillType.MARKETING) * 0.25);
    }
    
    
    public int getMarketDiscount() {
        int skillLevel = getSkillLevel(SkillType.MARKETING);
        
        return skillLevel > 1 ? (skillLevel - 1) * 10 : 0;
    }
    
    
    public double getSecurityLevel() {
        
        return 1.0 + (getSkillLevel(SkillType.SECURITY) * 0.5);
    }
    
    
    public double getManagementEfficiency() {
        
        return 1.0 + (getSkillLevel(SkillType.MANAGEMENT) * 0.2);
    }
    
    
    public double getDeploymentTimeReduction() {
        int skillLevel = getSkillLevel(SkillType.DEPLOY);
        
        return switch (skillLevel) {
            case 1 -> 0.0;
            case 2 -> 0.2;
            case 3 -> 0.4;
            case 4 -> 0.6;
            default -> 0.0;
        };
    }
    
    
    public boolean isFirewallManagementUnlocked() {
        
        return getSkillLevel(SkillType.SECURITY) >= 2;
    }

    public boolean canUnlockVPS(VPSProduct product) {
        int marketingLevel = getSkillLevel(SkillType.MARKETING);
        System.out.println("Current Marketing Level: " + marketingLevel);

        if (product == VPSProduct.BASIC_VPS || product == VPSProduct.STANDARD_VPS || product == VPSProduct.PREMIUM_VPS) {
            return marketingLevel >= 1;
        } else if (product == VPSProduct.ENTERPRISE_VPS || product == VPSProduct.BLADE_SERVER || product == VPSProduct.TOWER_SERVER) {
            return marketingLevel >= 2;
        } else if (product == VPSProduct.ADVANCED_CLUSTER || product == VPSProduct.SUPERCOMPUTER_NODE || product == VPSProduct.AI_TRAINING_RIG) {
            return marketingLevel >= 3;
        } else if (product == VPSProduct.QUANTUM_VPS || product == VPSProduct.HYBRID_CLOUD_SERVER || product == VPSProduct.GLOBAL_DATA_CENTER) {
            return marketingLevel >= 4;
        }
        return false;
    }

    
    public String getSkillLevelDescription(SkillType skillType, int level) {
        if (level < 0) {
            return "Level " + level + ": Nothing. Cannot be upgraded further. HACKER";
        }
        if (level > skillType.getMaxLevel()) {
            return "Max Level. Cannot be upgraded further. Coming Soon...";
        }
        
        switch (skillType) {
            case RACK_SLOTS:
                String slotBonus = "+" + (level * 2) + " rack slots";
                String discountBonus = (level > 1) ? ", " + ((level - 1) * 10) + "% discount on rack slot upgrades" : "";
                String networkBonus = (level > 1) ? ", +" + ((level - 1) * 10) + " Gbps network speed per rack" : "";
                return "Level " + level + ": " + slotBonus + discountBonus + networkBonus;
            case NETWORK_SPEED:
                return "Level " + level + ": +" + (level * 20) + "% network speed";
            case SERVER_EFFICIENCY:
                return "Level " + level + ": +" + (level * 15) + "% server efficiency";
            case MARKETING:
                String marketingBonus = "+" + (level * 25) + "% customer acquisition";
                String marketDiscountBonus = (level > 1) ? ", " + ((level - 1) * 10) + "% discount on market purchases" : "";
                return "Level " + level + ": " + marketingBonus + marketDiscountBonus;
            case SECURITY:
                if (level == 1) {
                    return "Level " + level + ": +" + (level * 50) + "% security level";
                } else if (level == 2) {
                    return "Level " + level + ": +" + (level * 50) + "% security level, Unlocks firewall management, +3% payment bonus";
                } else if (level == 3) {
                    return "Level " + level + ": +" + (level * 50) + "% security level, Unlocks firewall management, +5% payment bonus";
                } else if (level == 4) {
                    return "Level " + level + ": +" + (level * 50) + "% security level, Unlocks firewall management, +10% payment bonus";
                }
                return "Level " + level + ": +" + (level * 50) + "% security level";
            case MANAGEMENT:
                return "Level " + level + ": +" + (level * 20) + "% management efficiency";
            case DEPLOY:
                if (level == 1) {
                    return "Level " + level + ": No deployment time reduction";
                } else if (level == 2) {
                    return "Level " + level + ": 20% deployment time reduction";
                } else if (level == 3) {
                    return "Level " + level + ": 40% deployment time reduction";
                } else if (level == 4) {
                    return "Level " + level + ": 60% deployment time reduction";
                }
                return "Level " + level + ": Reduces VM deployment time to customers";
            default:
                return "Unknown skill type. Cannot be upgraded further.";
        }
    }

    
    public int getRackNetworkSpeedBonus() {
        int skillLevel = getSkillLevel(SkillType.RACK_SLOTS);
        
        return (skillLevel > 1) ? (skillLevel - 1) * 10 : 0;
    }
    
    
    public double getSecurityPaymentBonus() {
        int skillLevel = getSkillLevel(SkillType.SECURITY);
        return switch (skillLevel) {
            case 1 -> 0.0; 
            case 2 -> 0.03; 
            case 3 -> 0.05; 
            case 4 -> 0.10; 
            default -> 0.0;
        };
    }

    
    public void resetSkills() {
        
        skillLevels.clear();
        
        
        initializeSkillLevels();
        
        
        if (company != null) {
            company.setSkillPointsAvailable(0);
        }
        
        System.out.println("รีเซ็ตทักษะทั้งหมดเป็นค่าเริ่มต้นแล้ว");
    }
    
    
    public double getNetworkSpeedBonus() {
        int skillLevel = getSkillLevel(SkillType.NETWORK_SPEED);
        
        return skillLevel > 1 ? (skillLevel - 1) * 10 : 0;
    }
} 
