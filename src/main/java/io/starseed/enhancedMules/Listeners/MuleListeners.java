package io.starseed.enhancedMules.Listeners;

import io.starseed.enhancedMules.EnhancedMules;
import io.starseed.enhancedMules.Managers.MuleManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MuleListeners implements Listener {
    private final EnhancedMules plugin;
    private final MuleManager muleManager;

    public MuleListeners(EnhancedMules plugin) {
        this.plugin = plugin;
        this.muleManager = plugin.getMuleManager();
    }

    @EventHandler
    public void onMuleInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Mule)) return;

        Mule mule = (Mule) event.getRightClicked();
        Player player = event.getPlayer();

        if (!muleManager.isEnhancedMule(mule)) return;

        EnhancedMuleData muleData = muleManager.getMuleData(mule.getUniqueId());
        if (muleData == null) return;

        // Handle different interaction types
        if (player.isSneaking()) {
            if (player.hasPermission("enhancedmules.use")) {
                if (event.getHand() == EquipmentSlot.HAND) {
                    // Open crafting table
                    player.openInventory(muleData.getCraftingInventory());
                } else {
                    // Open furnace
                    player.openInventory(muleData.getFurnaceInventory());
                }
                event.setCancelled(true);
            }
        } else {
            // Normal interaction - either mount or open inventory
            if (!player.isSneaking()) {
                player.openInventory(muleData.getMainInventory());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMuleCombat(EntityDamageByEntityEvent event) {
        // Handle mule combat
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            // Check nearby mules that could defend their owner
            for (Entity nearby : victim.getNearbyEntities(5, 5, 5)) {
                if (nearby instanceof Mule) {
                    Mule mule = (Mule) nearby;
                    if (!muleManager.isEnhancedMule(mule)) continue;

                    EnhancedMuleData muleData = muleManager.getMuleData(mule.getUniqueId());
                    if (muleData == null) continue;

                    // If the victim is the owner, mule should attack the attacker
                    if (victim.getUniqueId().equals(muleData.getOwnerUUID())) {
                        if (event.getDamager() instanceof Entity) {
                            Entity attacker = (Entity) event.getDamager();
                            // Attack back
                            mule.setTarget(attacker);
                        }
                    }
                }
            }
        }
    }
}