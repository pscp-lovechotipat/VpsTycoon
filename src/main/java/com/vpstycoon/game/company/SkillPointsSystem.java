package com.vpstycoon.game.company;

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

    private static final Map<SkillType, Integer> skillLevels = new HashMap<>();
    private final Company company;

    // Skill types
    public enum SkillType {
        RACK_SLOTS("Rack Slots", "Increases the number of slots available in each rack", 4),
        NETWORK_SPEED("Network Speed", "Increases the speed of your network connections", 4),
        SERVER_EFFICIENCY("Server Efficiency", "Improves server performance and reduces costs", 4),
        MARKETING("Marketing", "Improves your company's visibility and attracts more customers", 4),
        SECURITY("Security", "Enhances your security systems and reduces the risk of attacks", 4),
        MANAGEMENT("Management", "Improves your ability to manage resources and staff", 4);
        
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

        // Initialize all skills at level 1
        for (SkillType skill : SkillType.values()) {
            skillLevels.put(skill, 1);
        }
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
        return skillLevels.getOrDefault(skillType, 0);
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
                return "Level " + level + ": +" + (level * 2) + " rack slots";
            case NETWORK_SPEED:
                return "Level " + level + ": +" + (level * 20) + "% network speed";
            case SERVER_EFFICIENCY:
                return "Level " + level + ": +" + (level * 15) + "% server efficiency";
            case MARKETING:
                return "Level " + level + ": +" + (level * 25) + "% customer acquisition";
            case SECURITY:
                if (level == 2) {
                    return "Level " + level + ": Unlocks firewall management";
                }
                return "Level " + level + ": +" + (level * 50) + "% security level";
            case MANAGEMENT:
                return "Level " + level + ": +" + (level * 20) + "% management efficiency";
            default:
                return "Unknown skill type. Cannot be upgraded further.";
        }
    }
} 