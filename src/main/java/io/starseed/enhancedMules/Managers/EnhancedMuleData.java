package io.starseed.enhancedMules.Managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnhancedMuleData implements ConfigurationSerializable {
    private final UUID muleUUID;
    private UUID ownerUUID;
    private int level;
    private double experience;
    private double maxHealth;
    private double speed;
    private double jumpStrength;
    private Inventory mainInventory;
    private Inventory craftingInventory;
    private Inventory furnaceInventory;

    // Constructor for new mules
    public EnhancedMuleData(UUID muleUUID, UUID ownerUUID) {
        this.muleUUID = muleUUID;
        this.ownerUUID = ownerUUID;
        this.level = 1;
        this.experience = 0;
        this.maxHealth = 20.0; // Base health
        this.speed = 0.2;      // Base speed
        this.jumpStrength = 0.5; // Base jump strength

        // Initialize inventories with custom titles
        this.mainInventory = Bukkit.createInventory(null, 54, "Enhanced Mule Storage");
        this.craftingInventory = Bukkit.createInventory(null, 9, "Enhanced Mule Crafting");
        this.furnaceInventory = Bukkit.createInventory(null, 3, "Enhanced Mule Furnace");
    }

    // Constructor for loading from configuration
    public EnhancedMuleData(Map<String, Object> data) {
        this.muleUUID = UUID.fromString((String) data.get("muleUUID"));
        this.ownerUUID = data.get("ownerUUID") != null ? UUID.fromString((String) data.get("ownerUUID")) : null;
        this.level = (int) data.get("level");
        this.experience = (double) data.get("experience");
        this.maxHealth = (double) data.get("maxHealth");
        this.speed = (double) data.get("speed");
        this.jumpStrength = (double) data.get("jumpStrength");

        // Load inventories
        this.mainInventory = Bukkit.createInventory(null, 54, "Enhanced Mule Storage");
        this.craftingInventory = Bukkit.createInventory(null, 9, "Enhanced Mule Crafting");
        this.furnaceInventory = Bukkit.createInventory(null, 3, "Enhanced Mule Furnace");

        // Load inventory contents if they exist
        if (data.containsKey("mainInventory")) {
            ItemStack[] contents = ((Map<String, ItemStack[]>) data.get("mainInventory")).get("contents");
            if (contents != null) {
                this.mainInventory.setContents(contents);
            }
        }
        // Similarly load other inventories...
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("muleUUID", muleUUID.toString());
        if (ownerUUID != null) {
            data.put("ownerUUID", ownerUUID.toString());
        }
        data.put("level", level);
        data.put("experience", experience);
        data.put("maxHealth", maxHealth);
        data.put("speed", speed);
        data.put("jumpStrength", jumpStrength);

        // Save inventory contents
        Map<String, ItemStack[]> inventoryMap = new HashMap<>();
        inventoryMap.put("contents", mainInventory.getContents());
        data.put("mainInventory", inventoryMap);

        // Similarly save other inventories...
        return data;
    }

    // Leveling system methods
    public void addExperience(double amount) {
        this.experience += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        double experienceNeeded = calculateExperienceNeeded();
        while (this.experience >= experienceNeeded) {
            levelUp();
            experienceNeeded = calculateExperienceNeeded();
        }
    }

    private void levelUp() {
        this.level++;
        // Increase stats based on level
        this.maxHealth += 2.0;
        this.speed += 0.01;
        this.jumpStrength += 0.05;
    }

    private double calculateExperienceNeeded() {
        // Example formula: each level requires more experience
        return 100 * Math.pow(1.5, level - 1);
    }

    // Getters and setters
    public UUID getMuleUUID() { return muleUUID; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public int getLevel() { return level; }
    public double getExperience() { return experience; }
    public double getMaxHealth() { return maxHealth; }
    public double getSpeed() { return speed; }
    public double getJumpStrength() { return jumpStrength; }
    public Inventory getMainInventory() { return mainInventory; }
    public Inventory getCraftingInventory() { return craftingInventory; }
    public Inventory getFurnaceInventory() { return furnaceInventory; }

    // Save/Load methods
    public void save() {
        // TODO: Implement saving to config/database
        // This will be implemented based on your chosen storage method
    }

    public void load() {
        // TODO: Implement loading from config/database
        // This will be implemented based on your chosen storage method
    }
}