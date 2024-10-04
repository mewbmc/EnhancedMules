package io.starseed.enhancedMules.Commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import io.starseed.enhancedMules.Utils.MuleUtils;
import io.starseed.enhancedMules.Utils.MuleUtils.Debug;
import io.starseed.enhancedMules.EnhancedMules;
import com.example.enhancedmules.EnhancedMuleData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuleCommands implements CommandExecutor, TabCompleter {
    private final EnhancedMules plugin;

    public MuleCommands(EnhancedMules plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "debug":
                    return handleDebug(sender, args);
                case "create":
                    return handleCreate(sender, args);
                case "remove":
                    return handleRemove(sender, args);
                case "info":
                    return handleInfo(sender, args);
                case "list":
                    return handleList(sender);
                case "transfer":
                    return handleTransfer(sender, args);
                case "reload":
                    return handleReload(sender);
                default:
                    sendHelp(sender);
                    return true;
            }
        } catch (Exception e) {
            MuleUtils.sendError(sender, "An error occurred while executing the command.");
            Debug.error("Command execution error", e);
            return true;
        }
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission(MuleUtils.Permissions.DEBUG)) {
            MuleUtils.sendError(sender, "You don't have permission to use debug commands.");
            return true;
        }

        if (args.length < 2) {
            MuleUtils.Debug.toggleDebug();
            MuleUtils.sendMessage(sender, "Debug mode: " + (MuleUtils.Debug.isDebugMode() ? "enabled" : "disabled"));
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "info":
                if (!(sender instanceof Player)) {
                    MuleUtils.sendError(sender, "This command can only be used by players.");
                    return true;
                }
                Player player = (Player) sender;
                // Get the mule the player is looking at within 5 blocks
                Mule targetMule = null;
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof Mule && player.hasLineOfSight(entity)) {
                        targetMule = (Mule) entity;
                        break;
                    }
                }
                if (targetMule == null) {
                    MuleUtils.sendError(sender, "You must be looking at a mule.");
                    return true;
                }
                sendDetailedMuleInfo(sender, targetMule);
                break;
            case "dump":
                plugin.dumpDebugInfo();
                MuleUtils.sendSuccess(sender, "Debug data dumped to plugins/EnhancedMules/debug/");
                break;
            default:
                MuleUtils.sendError(sender, "Unknown debug command.");
                break;
        }
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MuleUtils.sendError(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!MuleUtils.Permissions.hasPermission(player, MuleUtils.Permissions.CREATE)) {
            MuleUtils.sendError(sender, "You don't have permission to create enhanced mules.");
            return true;
        }

        try {
            Location spawnLoc = player.getLocation();
            plugin.getMuleManager().createEnhancedMule(spawnLoc, player);
            MuleUtils.sendSuccess(sender, "Enhanced mule created successfully!");
        } catch (Exception e) {
            MuleUtils.sendError(sender, "Failed to create enhanced mule.");
            Debug.error("Mule creation error", e);
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission(MuleUtils.Permissions.REMOVE)) {
            MuleUtils.sendError(sender, "You don't have permission to remove mules.");
            return true;
        }

        if (!(sender instanceof Player)) {
            MuleUtils.sendError(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Mule targetMule = null;
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Mule && player.hasLineOfSight(entity)) {
                targetMule = (Mule) entity;
                break;
            }
        }

        if (targetMule == null) {
            MuleUtils.sendError(sender, "You must be looking at a mule to remove it.");
            return true;
        }

        plugin.getMuleManager().removeEnhancedMule(targetMule);
        MuleUtils.sendSuccess(sender, "Enhanced mule removed successfully!");
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MuleUtils.sendError(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Mule targetMule = null;
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Mule && player.hasLineOfSight(entity)) {
                targetMule = (Mule) entity;
                break;
            }
        }

        if (targetMule == null) {
            MuleUtils.sendError(sender, "You must be looking at a mule.");
            return true;
        }

        sendDetailedMuleInfo(sender, targetMule);
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MuleUtils.sendError(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        List<EnhancedMuleData> mules = plugin.getMuleManager().getPlayerMules(player.getUniqueId());

        if (mules.isEmpty()) {
            MuleUtils.sendMessage(sender, "You don't have any enhanced mules.");
            return true;
        }

        MuleUtils.sendMessage(sender, "Your enhanced mules:");
        for (EnhancedMuleData mule : mules) {
            MuleUtils.sendMessage(sender, "- Level " + mule.getLevel() + " Mule (ID: " + mule.getMuleUUID() + ")");
        }
        return true;
    }

    private boolean handleTransfer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MuleUtils.sendError(sender, "This command can only be used by players.");
            return true;
        }

        if (args.length < 2) {
            MuleUtils.sendError(sender, "Usage: /mule transfer <player>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            MuleUtils.sendError(sender, "Player not found.");
            return true;
        }

        Mule targetMule = null;
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Mule && player.hasLineOfSight(entity)) {
                targetMule = (Mule) entity;
                break;
            }
        }

        if (targetMule == null) {
            MuleUtils.sendError(sender, "You must be looking at a mule to transfer it.");
            return true;
        }

        plugin.getMuleManager().transferOwnership(targetMule, target);
        MuleUtils.sendSuccess(sender, "Mule transferred to " + target.getName() + "!");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(MuleUtils.Permissions.ADMIN)) {
            MuleUtils.sendError(sender, "You don't have permission to reload the plugin.");
            return true;
        }

        plugin.reloadPlugin();
        MuleUtils.sendSuccess(sender, "Plugin reloaded successfully!");
        return true;
    }

    private void sendDetailedMuleInfo(CommandSender sender, Mule mule) {
        EnhancedMuleData data = plugin.getMuleManager().getMuleData(mule.getUniqueId());
        if (data == null) {
            MuleUtils.sendMessage(sender, "Basic Mule Info:");
            MuleUtils.sendMessage(sender, "UUID: " + mule.getUniqueId());
            MuleUtils.sendMessage(sender, "Enhanced: No");
            return;
        }

        MuleUtils.sendMessage(sender, "Enhanced Mule Info:");
        MuleUtils.sendMessage(sender, "UUID: " + mule.getUniqueId());
        MuleUtils.sendMessage(sender, "Owner: " + (data.getOwnerUUID() == null ? "None" : Bukkit.getOfflinePlayer(data.getOwnerUUID()).getName()));
        MuleUtils.sendMessage(sender, "Level: " + data.getLevel());
        MuleUtils.sendMessage(sender, "Experience: " + data.getExperience());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            if (sender.hasPermission(MuleUtils.Permissions.USE)) {
                commands.add("info");
                commands.add("list");
            }
            if (sender.hasPermission(MuleUtils.Permissions.CREATE)) {
                commands.add("create");
            }
            if (sender.hasPermission(MuleUtils.Permissions.ADMIN)) {
                commands.add("debug");
                commands.add("reload");
                commands.add("remove");
                commands.add("transfer");
            }

            for (String cmd : commands) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("transfer")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }

    private void sendHelp(CommandSender sender) {
        MuleUtils.sendMessage(sender, "Enhanced Mules Commands:");
        if (sender.hasPermission(MuleUtils.Permissions.USE)) {
            MuleUtils.sendMessage(sender, "/mule info - Show information about a mule");
            MuleUtils.sendMessage(sender, "/mule list - List all your enhanced mules");
        }
        if (sender.hasPermission(MuleUtils.Permissions.CREATE)) {
            MuleUtils.sendMessage(sender, "/mule create - Create a new enhanced mule");
        }
        if (sender.hasPermission(MuleUtils.Permissions.ADMIN)) {
            MuleUtils.sendMessage(sender, "/mule debug - Toggle debug mode");
            MuleUtils.sendMessage(sender, "/mule reload - Reload the plugin configuration");
            MuleUtils.sendMessage(sender, "/mule remove <id> - Remove an enhanced mule");
            MuleUtils.sendMessage(sender, "/mule transfer <player> - Transfer mule ownership");
        }
    }
}