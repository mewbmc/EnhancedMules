package io.starseed.enhancedMules;

import io.starseed.enhancedMules.Listeners.MuleListeners;
import io.starseed.enhancedMules.Managers.MuleInventoryManager;
import io.starseed.enhancedMules.Managers.MuleManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import io.starseed.enhancedMules.Utils.MuleUtils;
import io.starseed.enhancedMules.Utils.MuleUtils.Debug;
import io.starseed.enhancedMules.Commands.MuleCommands;

import java.io.File;
import java.util.logging.Level;

public class EnhancedMules extends JavaPlugin implements Listener {
    private FileConfiguration config;
    private MuleManager muleManager;
    private MuleInventoryManager inventoryManager;
    private static EnhancedMules instance;
    private File debugFolder;

    // NamespacedKeys for persistent data
    private NamespacedKey enhancedKey;
    private NamespacedKey ownerKey;
    private NamespacedKey levelKey;
    private NamespacedKey experienceKey;

    @Override
    public void onEnable() {
        try {
            // Initialize plugin instance
            instance = this;

            // Create necessary directories
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            debugFolder = new File(getDataFolder(), "debug");
            if (!debugFolder.exists()) {
                debugFolder.mkdirs();
            }

            // Initialize NamespacedKeys
            initializeKeys();

            // Load configuration
            loadConfiguration();

            // Initialize managers
            initializeManagers();

            // Register events
            getServer().getPluginManager().registerEvents(this, this);
            getServer().getPluginManager().registerEvents(new MuleListeners(this), this);

            // Register commands
            MuleCommands muleCommands = new MuleCommands(this);
            getCommand("mule").setExecutor(muleCommands);
            getCommand("mule").setTabCompleter(muleCommands);

            // Log successful enable
            getLogger().info("EnhancedMules has been successfully enabled!");
            Debug.log("Plugin initialization complete");

        } catch (Exception e) {
            getLogger().severe("Failed to enable EnhancedMules!");
            Debug.error("Plugin initialization failed", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            if (muleManager != null) {
                muleManager.saveAllMuleData();
            }
            if (inventoryManager != null) {
                inventoryManager.saveAllInventories();
            }

            getLogger().info("EnhancedMules has been disabled!");
        } catch (Exception e) {
            getLogger().severe("Error occurred while disabling EnhancedMules!");
            e.printStackTrace();
        }
    }

    private void initializeKeys() {
        enhancedKey = new NamespacedKey(this, "enhanced_mule");
        ownerKey = new NamespacedKey(this, "owner");
        levelKey = new NamespacedKey(this, "level");
        experienceKey = new NamespacedKey(this, "experience");
    }

    private void loadConfiguration() {
        try {
            saveDefaultConfig();
            config = getConfig();

            // Add default configuration values
            config.addDefault("mule.inventory.rows", 6);
            config.addDefault("mule.combat.base-damage", 4.0);
            config.addDefault("mule.combat.attack-range", 3.0);
            config.addDefault("mule.leveling.max-level", 30);
            config.addDefault("mule.leveling.exp-per-level", 1000);
            config.addDefault("mule.features.whistle-range", 50);
            config.addDefault("mule.breeding.enhanced-chance", 0.1);

            config.options().copyDefaults(true);
            saveConfig();

            Debug.log("Configuration loaded successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to load configuration!");
            Debug.error("Configuration loading failed", e);
        }
    }

    private void initializeManagers() {
        try {
            muleManager = new MuleManager(this);
            inventoryManager = new MuleInventoryManager(this);
            Debug.log("Managers initialized successfully");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize managers!");
            Debug.error("Manager initialization failed", e);
            throw e; // Rethrow to trigger plugin disable
        }
    }

    public void reloadPlugin() {
        try {
            reloadConfig();
            config = getConfig();
            muleManager.reloadData();
            inventoryManager.reloadData();
            Debug.log("Plugin reloaded successfully");
            getLogger().info("EnhancedMules has been reloaded!");
        } catch (Exception e) {
            getLogger().severe("Failed to reload plugin!");
            Debug.error("Plugin reload failed", e);
        }
    }

    // Event Handlers
    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        try {
            if (event.getEntity() instanceof Mule) {
                // Random chance to create an enhanced mule
                if (Math.random() < config.getDouble("mule.breeding.enhanced-chance", 0.1)) {
                    muleManager.convertToEnhancedMule((Mule) event.getEntity(), null);
                    Debug.log("Enhanced mule created through breeding");
                }
            }
        } catch (Exception e) {
            Debug.error("Error in breeding event", e);
        }
    }

    // Utility Methods
    public File getDebugFolder() {
        return debugFolder;
    }

    public static EnhancedMules getInstance() {
        return instance;
    }

    public MuleManager getMuleManager() {
        return muleManager;
    }

    public MuleInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public NamespacedKey getEnhancedKey() {
        return enhancedKey;
    }

    public NamespacedKey getOwnerKey() {
        return ownerKey;
    }

    public NamespacedKey getLevelKey() {
        return levelKey;
    }

    public NamespacedKey getExperienceKey() {
        return experienceKey;
    }

    // Debug Methods
    public void dumpDebugInfo() {
        try {
            File debugFile = new File(debugFolder, "debug_" + System.currentTimeMillis() + ".log");
            StringBuilder debug = new StringBuilder();

            debug.append("=== EnhancedMules Debug Info ===\n");
            debug.append("Version: ").append(getDescription().getVersion()).append("\n");
            debug.append("Server version: ").append(getServer().getVersion()).append("\n");
            debug.append("Active enhanced mules: ").append(muleManager.getActiveMuleCount()).append("\n");
            debug.append("Loaded inventories: ").append(inventoryManager.getLoadedInventoryCount()).append("\n");
            debug.append("\nConfiguration:\n");
            debug.append(getConfig().saveToString());

            // Write to file
            java.nio.file.Files.write(debugFile.toPath(), debug.toString().getBytes());
            Debug.log("Debug info dumped to " + debugFile.getName());
        } catch (Exception e) {
            Debug.error("Failed to dump debug info", e);
        }
    }

    // Error handling method for general plugin operations
    public void handleError(String operation, Exception e, Player player) {
        Debug.error("Error during " + operation, e);
        if (player != null) {
            MuleUtils.sendError(player, "An error occurred during " + operation + ". Please check the console for details.");
        }
    }

    // Plugin metrics and stats
    public void updateStats() {
        try {
            // TODO: Implement metrics collection
            Debug.log("Stats updated successfully");
        } catch (Exception e) {
            Debug.error("Failed to update stats", e);
        }
    }
}