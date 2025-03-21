package com.vpstycoon.game;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.thread.RequestGenerator;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.VPSOptimization;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    
    // Game components
    private RequestManager requestManager;
    private GameTimeManager timeManager;
    private RequestGenerator requestGenerator;
    private VPSInventory vpsInventory;
    private List<VPSOptimization> installedServers;
    
    // Game state
    private boolean gameRunning = false;

    private GameManager() {
        installedServers = new ArrayList<>();
        vpsInventory = new VPSInventory();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    /**
     * Initialize a new game
     * @param company The player's company
     */
    public void initializeNewGame(Company company) {
        // Create request manager with the company
        requestManager = ResourceManager.getInstance().getRequestManager();
        
        // Create time manager
        timeManager = ResourceManager.getInstance().getGameTimeManager();
        
        // Create request generator
        requestGenerator = new RequestGenerator(requestManager);
        
        // Create VPS inventory
        vpsInventory = new VPSInventory();
        
        // Create installed servers list
        installedServers = new ArrayList<>();
        
        // Add initial server to inventory
        VPSOptimization initialServer = createInitialServer();
        vpsInventory.addVPS("initial-server", initialServer);
        
        // Save initial state
        saveState();
    }
    
    /**
     * Create the initial server for a new game
     * @return The initial VPS server
     */
    private VPSOptimization createInitialServer() {
        VPSOptimization server = new VPSOptimization();
        server.setVCPUs(4);
        server.setRamInGB(16);
        server.setDiskInGB(500);
        return server;
    }

    /**
     * Save the current game state
     */
    public void saveState() {
        GameState currentState = new GameState();
        
        // Save company
        if (requestManager != null) {
            currentState.setCompany(requestManager.getVmProvisioningManager().getCompany());
        }
        
        // Save date/time if time manager is running
        if (timeManager != null) {
            currentState.setLocalDateTime(timeManager.getGameDateTime());
        }
        
        // Add all game objects
        for (VPSOptimization server : installedServers) {
            // Since VPSOptimization now extends GameObject, we can add it directly
            currentState.addGameObject(server);
        }
        
        ResourceManager.getInstance().saveGameState(currentState);
    }

    /**
     * Load a saved game state
     */
    public void loadState() {
        GameState savedState = ResourceManager.getInstance().loadGameState();
        
        if (savedState != null) {
            // Get company from saved state
            Company company = savedState.getCompany();
            
            // Initialize game with saved company
            initializeNewGame(company);
            
            // Set date/time from saved state
            if (savedState.getLocalDateTime() != null) {
                timeManager = ResourceManager.getInstance().getGameTimeManager();
            }
            
            // Load installed servers
            installedServers.clear();
            for (GameObject obj : savedState.getGameObjects()) {
                if (obj instanceof VPSOptimization) {
                    VPSOptimization server = (VPSOptimization) obj;
                    if (server.isInstalled()) {
                        installedServers.add(server);
                        timeManager.addVPSServer(server);
                    } else {
                        vpsInventory.addVPS("server-" + installedServers.size(), server);
                    }
                }
            }
        }
    }

    /**
     * Get the current game state
     * @return The current game state
     */
    public GameState getCurrentState() {
        return ResourceManager.getInstance().getCurrentState();
    }

    /**
     * Delete the saved game
     */
    public void deleteSavedGame() {
        ResourceManager.getInstance().deleteSaveFile();
    }

    /**
     * Check if there is a saved game
     * @return true if there is a saved game, false otherwise
     */
    public boolean hasSavedGame() {
        return ResourceManager.getInstance().hasSaveFile();
    }
    
    /**
     * Install a server from inventory
     * @param serverId The server ID in the inventory
     * @return true if successful, false otherwise
     */
    public boolean installServer(String serverId) {
        VPSOptimization server = vpsInventory.getVPS(serverId);
        
        if (server != null) {
            // Remove from inventory
            vpsInventory.removeVPS(serverId);
            
            // Mark as installed
            server.setInstalled(true);
            
            // Add to installed servers
            installedServers.add(server);
            
            // Add to time manager for overhead costs
            timeManager.addVPSServer(server);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Uninstall a server and return it to inventory
     * @param server The server to uninstall
     * @return true if successful, false otherwise
     */
    public boolean uninstallServer(VPSOptimization server) {
        if (installedServers.contains(server)) {
            // Remove from installed servers
            installedServers.remove(server);
            
            // Mark as not installed
            server.setInstalled(false);
            
            // Add to inventory
            vpsInventory.addVPS("server-" + System.currentTimeMillis(), server);
            
            // Remove from time manager
            timeManager.removeVPSServer(server);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Buy a new server and add it to inventory
     * @param vcpus Number of vCPUs
     * @param ramGB Amount of RAM in GB
     * @param diskGB Amount of disk space in GB
     * @param cost The cost of the server
     * @return true if successful, false otherwise
     */
    public boolean buyServer(int vcpus, int ramGB, int diskGB, long cost) {
        if (requestManager == null || requestManager.getVmProvisioningManager() == null) {
            return false;
        }
        
        Company company = requestManager.getVmProvisioningManager().getCompany();
        
        // Check if company has enough money
        if (company.getMoney() < cost) {
            return false;
        }
        
        // Deduct cost
        company.setMoney(company.getMoney() - cost);
        
        // Create new server
        VPSOptimization server = new VPSOptimization();
        server.setVCPUs(vcpus);
        server.setRamInGB(ramGB);
        server.setDiskInGB(diskGB);
        
        // Add to inventory
        vpsInventory.addVPS("server-" + System.currentTimeMillis(), server);
        
        return true;
    }
    
    // Getters
    
    public RequestManager getRequestManager() {
        return requestManager;
    }
    
    public GameTimeManager getTimeManager() {
        return timeManager;
    }
    
    public RequestGenerator getRequestGenerator() {
        return requestGenerator;
    }
    
    public VPSInventory getVpsInventory() {
        return vpsInventory;
    }
    
    public List<VPSOptimization> getInstalledServers() {
        return new ArrayList<>(installedServers);
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
}