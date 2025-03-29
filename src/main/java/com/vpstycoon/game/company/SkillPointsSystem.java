package com.vpstycoon.game.company;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.enums.VPSProduct;

/**
 * Manages the skill points system and unlockable features in the game
 */
public class SkillPointsSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<SkillType, Integer> skillLevels = new HashMap<>();
    private final Company company;

    // Skill types
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
        initializeSkillLevels(); // เรียกเมธอดแยกเพื่อกำหนดค่า
    }

    /**
     * Constructor that loads skill levels from a saved game state
     * @param company The company object
     * @param savedSkillLevels The previously saved skill levels map
     */
    public SkillPointsSystem(Company company, Map<SkillType, Integer> savedSkillLevels) {
        this.company = company;
        
        // If we have saved skill levels, use them
        if (savedSkillLevels != null && !savedSkillLevels.isEmpty()) {
            this.skillLevels.putAll(savedSkillLevels);
            
            // Ensure all skill types have a level
            for (SkillType skill : SkillType.values()) {
                if (!this.skillLevels.containsKey(skill)) {
                    this.skillLevels.put(skill, 1); // Default to level 1 for any missing skills
                }
            }
        } else {
            // Otherwise initialize with defaults
            initializeSkillLevels();
        }
    }

    private void initializeSkillLevels() {
        for (SkillType skill : SkillType.values()) {
            skillLevels.put(skill, 1); // Initialize all skills at level 1
        }
    }

    /**
     * Get the current skill levels map for saving
     * @return Map of skill types and their levels
     */
    public Map<SkillType, Integer> getSkillLevelsMap() {
        return new HashMap<>(skillLevels);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // No need to reinitialize on loading since skillLevels is no longer static
    }

    /**
     * Add skill points to the available pool
     * @param points Number of points to add
     */
    public void addPoints(int points) {
        this.company.addSkillPoints(points);
    }

    /**
     * Get the current available skill points
     * @return Number of available skill points
     */
    public int getAvailablePoints() {
        return company.getSkillPointsAvailable();
    }
    
    /**
     * Get the current level of a specific skill
     * @param skillType The skill type to check
     * @return Current level of the skill
     */
    public int getSkillLevel(SkillType skillType) {
        return skillLevels.getOrDefault(skillType, 1); // ค่าเริ่มต้นเป็น 1 หากไม่มีข้อมูล
    }
    
    /**
     * Get the rack slot upgrade discount percentage based on the RACK_SLOTS skill level
     * @return Discount percentage (0-100)
     */
    public int getRackSlotUpgradeDiscount() {
        int skillLevel = getSkillLevel(SkillType.RACK_SLOTS);
        // Level 1: 0%, Level 2: 10%, Level 3: 20%, Level 4: 30%
        return (skillLevel > 1) ? (skillLevel - 1) * 10 : 0;
    }
    
    /**
     * Upgrade a skill if enough points are available
     * @param skillType The skill to upgrade
     * @return true if upgrade was successful, false otherwise
     */
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

    /**
     * Calculate the cost to upgrade a skill based on its current level
     * @param currentLevel Current level of the skill
     * @return Cost in skill points
     */
    public int calculateUpgradeCost(int currentLevel) {
        // Base cost is 1, increases by 1 for each level
        return currentLevel + 1;
    }
    
    /**
     * Get the number of rack slots based on the RACK_SLOTS skill level
     * @return Number of available rack slots
     */
    public int getAvailableRackSlots() {
        // Base slots + additional slots from skill level
        return 4 + (getSkillLevel(SkillType.RACK_SLOTS) * 2);
    }
    
    /**
     * Get the network speed multiplier based on the NETWORK_SPEED skill level
     * @return Network speed multiplier
     */
    public double getNetworkSpeedMultiplier() {
        // Base multiplier is 1.0, each level adds 0.2
        return 1.0 + (getSkillLevel(SkillType.NETWORK_SPEED) * 0.2);
    }
    
    /**
     * Get the server efficiency multiplier based on the SERVER_EFFICIENCY skill level
     * @return Server efficiency multiplier
     */
    public double getServerEfficiencyMultiplier() {
        // Base multiplier is 1.0, each level adds 0.15
        return 1.0 + (getSkillLevel(SkillType.SERVER_EFFICIENCY) * 0.15);
    }
    
    /**
     * Get the marketing bonus based on the MARKETING skill level
     * @return Marketing bonus multiplier
     */
    public double getMarketingBonus() {
        // Base multiplier is 1.0, each level adds 0.25
        return 1.0 + (getSkillLevel(SkillType.MARKETING) * 0.25);
    }
    
    /**
     * Get the market discount percentage based on the MARKETING skill level
     * @return Discount percentage (0-30)
     */
    public int getMarketDiscount() {
        int skillLevel = getSkillLevel(SkillType.MARKETING);
        // Level 1: 0%, Level 2: 10%, Level 3: 20%, Level 4: 30%
        return skillLevel > 1 ? (skillLevel - 1) * 10 : 0;
    }
    
    /**
     * Get the security level based on the SECURITY skill level
     * @return Security level
     */
    public double getSecurityLevel() {
        // Base level is 1.0, each skill level adds 0.5
        return 1.0 + (getSkillLevel(SkillType.SECURITY) * 0.5);
    }
    
    /**
     * Get the management efficiency based on the MANAGEMENT skill level
     * @return Management efficiency multiplier
     */
    public double getManagementEfficiency() {
        // Base multiplier is 1.0, each level adds 0.2
        return 1.0 + (getSkillLevel(SkillType.MANAGEMENT) * 0.2);
    }
    
    /**
     * Get the deployment time reduction percentage based on the DEPLOY skill level
     * @return Deployment time reduction percentage (0-100)
     */
    public double getDeploymentTimeReduction() {
        int skillLevel = getSkillLevel(SkillType.DEPLOY);
        // Level 1: 0%, Level 2: 20%, Level 3: 40%, Level 4: 60%
        return switch (skillLevel) {
            case 1 -> 0.0;
            case 2 -> 0.2;
            case 3 -> 0.4;
            case 4 -> 0.6;
            default -> 0.0;
        };
    }
    
    /**
     * Check if firewall management is unlocked
     * @return true if firewall management is unlocked
     */
    public boolean isFirewallManagementUnlocked() {
        // Firewall management is unlocked at SECURITY level 2
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

    /**
     * Get a description of what each skill level unlocks
     * @param skillType The skill type
     * @param level The level to get description for
     * @return Description of what this level unlocks
     */
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

    /**
     * Get the rack network speed multiplier based on the RACK_SLOTS skill level
     * @return Network speed increment in Gbps (added to base 10 Gbps)
     */
    public int getRackNetworkSpeedBonus() {
        int skillLevel = getSkillLevel(SkillType.RACK_SLOTS);
        // Level 1: +0 Gbps, Level 2: +10 Gbps, Level 3: +20 Gbps, Level 4: +30 Gbps
        return (skillLevel > 1) ? (skillLevel - 1) * 10 : 0;
    }
    
    /**
     * Get the payment bonus percentage based on the SECURITY skill level
     * @return Payment bonus percentage (0%, 3%, 5%, or 10%)
     */
    public double getSecurityPaymentBonus() {
        int skillLevel = getSkillLevel(SkillType.SECURITY);
        return switch (skillLevel) {
            case 1 -> 0.0; // Level 1: 0% bonus
            case 2 -> 0.03; // Level 2: 3% bonus
            case 3 -> 0.05; // Level 3: 5% bonus
            case 4 -> 0.10; // Level 4: 10% bonus
            default -> 0.0;
        };
    }

    /**
     * รีเซ็ตทักษะทั้งหมดให้กลับเป็นค่าเริ่มต้น (ระดับ 1)
     */
    public void resetSkills() {
        // ลบค่าทั้งหมดออกก่อน
        skillLevels.clear();
        
        // สร้างใหม่ด้วยค่าเริ่มต้น
        initializeSkillLevels();
        
        // ตั้งค่า skill points ให้เป็น 0
        if (company != null) {
            company.setSkillPointsAvailable(0);
        }
        
        System.out.println("รีเซ็ตทักษะทั้งหมดเป็นค่าเริ่มต้นแล้ว");
    }
    
    /**
     * Get the bonus to rack network speed based on the NETWORK_SPEED skill
     * @return Percentage bonus to network speed
     */
    public double getNetworkSpeedBonus() {
        int skillLevel = getSkillLevel(SkillType.NETWORK_SPEED);
        // Level 1: +0%, Level 2: +10%, Level 3: +20%, Level 4: +30%
        return skillLevel > 1 ? (skillLevel - 1) * 10 : 0;
    }
} 