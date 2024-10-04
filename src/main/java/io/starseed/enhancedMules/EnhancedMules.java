package io.starseed.enhancedMules;

import io.starseed.enhancedMules.Commands.MuleCommands;
import io.starseed.enhancedMules.Managers.EnhancedMuleData;
import io.starseed.enhancedMules.Managers.MuleManager;
import io.starseed.enhancedMules.Listeners.MuleListeners;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class EnhancedMules extends JavaPlugin {
    private MuleManager muleManager;
    private File muleDataFile;
    private YamlConfiguration muleConfig;
    private static EnhancedMules instance;

    @Override
    public void onEnable() {
        // Set instance for static access
        instance = this;

        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Register configuration serializable classes
        ConfigurationSerialization.registerClass(EnhancedMuleData.class);

        // Initialize config files
        saveDefaultConfig();
        initializeMuleDataFile();

        // Initialize managers
        this.muleManager = new MuleManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new MuleListeners(this), this);
        getServer().getPluginManager().registerEvents(muleManager, this);

        // Register commands
        getCommand("mule").setExecutor(new MuleCommands(this));
        getCommand("mule").setTabCompleter(new MuleCommands(this));

        // Load all mule data
        loadAllMuleData();

        // Start auto-save task (every 5 minutes)
        Bukkit.getScheduler().runTaskTimer(this, this::saveMuleData, 6000L, 6000L);

        getLogger().info("EnhancedMules has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data before shutdown
        saveMuleData();

        getLogger().info("EnhancedMules has been disabled!");
    }

    private void initializeMuleDataFile() {
        muleDataFile = new File(getDataFolder(), "muledata.yml");
        if (!muleDataFile.exists()) {
            saveResource("muledata.yml", false);
        }
        muleConfig = YamlConfiguration.loadConfiguration(muleDataFile);
    }

    private void loadAllMuleData() {
        try {
            ConfigurationSection mulesSection = muleConfig.getConfigurationSection("mules");
            if (mulesSection != null) {
                for (String key : mulesSection.getKeys(false)) {
                    ConfigurationSection muleSection = mulesSection.getConfigurationSection(key);
                    if (muleSection != null) {
                        EnhancedMuleData muleData = EnhancedMuleData.deserialize(muleSection.getValues(true));
                        muleManager.registerMuleData(muleData);
                    }
                }
            }
            getLogger().info("Successfully loaded all mule data!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading mule data:", e);
        }
    }

    public void saveMuleData() {
        try {
            muleConfig.set("mules", null); // Clear existing data
            ConfigurationSection mulesSection = muleConfig.createSection("mules");

            for (EnhancedMuleData muleData : muleManager.getAllMuleData()) {
                ConfigurationSection muleSection = mulesSection.createSection(muleData.getMuleUUID().toString());
                Map<String, Object> serializedData = muleData.serialize();
                for (Map.Entry<String, Object> entry : serializedData.entrySet()) {
                    muleSection.set(entry.getKey(), entry.getValue());
                }
            }

            muleConfig.save(muleDataFile);
            getLogger().info("Successfully saved all mule data!");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save mule data:", e);
        }
    }

    public void reloadPlugin() {
        // Save current data
        saveMuleData();

        // Reload configs
        reloadConfig();
        muleConfig = YamlConfiguration.loadConfiguration(muleDataFile);

        // Clear current data
        muleManager.clearData();

        // Load fresh data
        loadAllMuleData();

        getLogger().info("Plugin reloaded successfully!");
    }

    public MuleManager getMuleManager() {
        return muleManager;
    }

    public static EnhancedMules getInstance() {
        return instance;
    }

    public YamlConfiguration getMuleConfig() {
        return muleConfig;
    }

    public void dumpDebugInfo() {
    }
}