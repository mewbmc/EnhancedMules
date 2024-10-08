package io.starseed.enhancedMules.Managers;

import io.starseed.enhancedMules.EnhancedMules;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class MuleManager implements Listener {
    private final EnhancedMules plugin;
    private final Map<UUID, EnhancedMuleData> muleDatas;
    private final NamespacedKey ENHANCED_KEY;
    private final NamespacedKey OWNER_KEY;
    private final NamespacedKey LEVEL_KEY;

    public MuleManager(EnhancedMules plugin) {
        this.plugin = plugin;
        this.muleDatas = new HashMap<>();
        this.ENHANCED_KEY = new NamespacedKey(plugin, "enhanced_mule");
        this.OWNER_KEY = new NamespacedKey(plugin, "owner");
        this.LEVEL_KEY = new NamespacedKey(plugin, "level");
    }

    public void createEnhancedMule(Location location, Player owner) {
        // Spawn a new mule at the specified location
        Mule mule = (Mule) location.getWorld().spawnEntity(location, EntityType.MULE);

        // Convert it to an enhanced mule and store the data
        convertToEnhancedMule(mule, owner);

        // Set default properties
        mule.setCustomName("Enhanced Mule (Level 1)");
        mule.setCustomNameVisible(true);
        mule.setTamed(true);
        if (owner != null) {
            mule.setOwner(owner);
        }
    }

    public EnhancedMuleData convertToEnhancedMule(Mule mule, Player owner) {
        PersistentDataContainer pdc = mule.getPersistentDataContainer();
        pdc.set(ENHANCED_KEY, PersistentDataType.BOOLEAN, true);
        if (owner != null) {
            pdc.set(OWNER_KEY, PersistentDataType.STRING, owner.getUniqueId().toString());
        }
        pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, 1);

        EnhancedMuleData muleData = new EnhancedMuleData(mule.getUniqueId(), owner != null ? owner.getUniqueId() : null);
        muleDatas.put(mule.getUniqueId(), muleData);
        return muleData;
    }

    public void removeEnhancedMule(Mule mule) {
        if (!isEnhancedMule(mule)) {
            return;
        }

        // Remove all stored data
        muleDatas.remove(mule.getUniqueId());

        // Clear persistent data
        PersistentDataContainer pdc = mule.getPersistentDataContainer();
        pdc.remove(ENHANCED_KEY);
        pdc.remove(OWNER_KEY);
        pdc.remove(LEVEL_KEY);

        // Remove the entity
        mule.remove();
    }

    public void transferOwnership(Mule mule, Player newOwner) {
        if (!isEnhancedMule(mule)) {
            return;
        }

        EnhancedMuleData muleData = getMuleData(mule.getUniqueId());
        if (muleData != null) {
            // Update the stored data
            muleData.setOwnerUUID(newOwner.getUniqueId());

            // Update persistent data
            PersistentDataContainer pdc = mule.getPersistentDataContainer();
            pdc.set(OWNER_KEY, PersistentDataType.STRING, newOwner.getUniqueId().toString());

            // Update in-game ownership
            mule.setOwner(newOwner);
        }
    }

    public boolean isEnhancedMule(Mule mule) {
        return mule.getPersistentDataContainer().has(ENHANCED_KEY, PersistentDataType.BOOLEAN);
    }

    public List<EnhancedMuleData> getPlayerMules(UUID playerUUID) {
        List<EnhancedMuleData> playerMules = new ArrayList<>();

        for (EnhancedMuleData muleData : muleDatas.values()) {
            if (playerUUID.equals(muleData.getOwnerUUID())) {
                playerMules.add(muleData);
            }
        }

        return playerMules;
    }

    public void saveAllMuleData() {
        // Save all mule data to config/database
        for (EnhancedMuleData data : muleDatas.values()) {
            data.save();
        }
    }

    public EnhancedMuleData getMuleData(UUID muleUUID) {
        return muleDatas.get(muleUUID);
    }

    /**
     * Clears all stored mule data from memory
     */
    public void clearData() {
        muleDatas.clear();
    }

    /**
     * Registers mule data in the manager
     * @param muleData The mule data to register
     */
    public void registerMuleData(EnhancedMuleData muleData) {
        muleDatas.put(muleData.getMuleUUID(), muleData);
    }

    /**
     * Gets all stored mule data
     * @return Collection of all mule data
     */
    public Collection<EnhancedMuleData> getAllMuleData() {
        return muleDatas.values();
    }

    /**
     * Gets the current count of active mules
     * @return The number of active enhanced mules
     */
    public int getActiveMuleCount() {
        return muleDatas.size();
    }

    /**
     * Reloads all mule data from storage
     */
    public void reloadData() {
        clearData();
        // The actual loading will be handled by the main class
        // through loadAllMuleData()
    }
}