package io.starseed.enhancedMules.Managers;

import io.starseed.enhancedMules.EnhancedMules;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class MuleInventoryManager {
    private final EnhancedMules plugin;
    private final HashMap<UUID, MuleInventoryHolder> inventoryHolders;

    public MuleInventoryManager(EnhancedMules plugin) {
        this.plugin = plugin;
        this.inventoryHolders = new HashMap<>();
    }

    public void reloadData() {
    }

    public char[] getLoadedInventoryCount() {
        return new char[0];
    }

    public class MuleInventoryHolder {
        private final Inventory mainInventory;
        private final Inventory craftingTable;
        private final Inventory furnace;
        private final UUID muleUUID;
        private boolean furnaceProcessing;
        private int furnaceProgress;
        private ItemStack furnaceResult;

        public MuleInventoryHolder(UUID muleUUID) {
            this.muleUUID = muleUUID;
            this.mainInventory = Bukkit.createInventory(null, 54, "Enhanced Mule Storage");
            this.craftingTable = Bukkit.createInventory(null, InventoryType.WORKBENCH, "Mule Crafting Table");
            this.furnace = Bukkit.createInventory(null, InventoryType.FURNACE, "Mule Furnace");
            this.furnaceProcessing = false;
            this.furnaceProgress = 0;
        }

        public void openInventory(Player player, InventoryType type) {
            switch (type) {
                case CHEST:
                    player.openInventory(mainInventory);
                    break;
                case WORKBENCH:
                    player.openInventory(craftingTable);
                    break;
                case FURNACE:
                    player.openInventory(furnace);
                    break;
            }
        }

        // Save all inventories to file
        public void saveInventories() {
            File dataFile = new File(plugin.getDataFolder(), "mules/" + muleUUID.toString() + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

            // Save main inventory
            for (int i = 0; i < mainInventory.getSize(); i++) {
                ItemStack item = mainInventory.getItem(i);
                if (item != null) {
                    config.set("inventory.main." + i, item);
                }
            }

            // Save furnace inventory
            for (int i = 0; i < furnace.getSize(); i++) {
                ItemStack item = furnace.getItem(i);
                if (item != null) {
                    config.set("inventory.furnace." + i, item);
                }
            }

            // Save furnace state
            config.set("furnace.processing", furnaceProcessing);
            config.set("furnace.progress", furnaceProgress);

            try {
                config.save(dataFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save inventory for mule: " + muleUUID);
                e.printStackTrace();
            }
        }

        // Load all inventories from file
        public void loadInventories() {
            File dataFile = new File(plugin.getDataFolder(), "mules/" + muleUUID.toString() + ".yml");
            if (!dataFile.exists()) return;

            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

            // Load main inventory
            if (config.contains("inventory.main")) {
                for (String key : config.getConfigurationSection("inventory.main").getKeys(false)) {
                    int slot = Integer.parseInt(key);
                    ItemStack item = config.getItemStack("inventory.main." + key);
                    mainInventory.setItem(slot, item);
                }
            }

            // Load furnace inventory
            if (config.contains("inventory.furnace")) {
                for (String key : config.getConfigurationSection("inventory.furnace").getKeys(false)) {
                    int slot = Integer.parseInt(key);
                    ItemStack item = config.getItemStack("inventory.furnace." + key);
                    furnace.setItem(slot, item);
                }
            }

            // Load furnace state
            furnaceProcessing = config.getBoolean("furnace.processing", false);
            furnaceProgress = config.getInt("furnace.progress", 0);
        }

        // Getters
        public Inventory getMainInventory() { return mainInventory; }
        public Inventory getCraftingTable() { return craftingTable; }
        public Inventory getFurnace() { return furnace; }
    }

    // Manager methods
    public MuleInventoryHolder getInventoryHolder(UUID muleUUID) {
        return inventoryHolders.computeIfAbsent(muleUUID, uuid -> {
            MuleInventoryHolder holder = new MuleInventoryHolder(uuid);
            holder.loadInventories();
            return holder;
        });
    }

    public void saveAllInventories() {
        for (MuleInventoryHolder holder : inventoryHolders.values()) {
            holder.saveInventories();
        }
    }
}