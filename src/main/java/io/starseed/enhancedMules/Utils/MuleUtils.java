package io.starseed.enhancedMules.Utils;

import io.starseed.enhancedMules.EnhancedMules;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;
import java.util.UUID;

public class MuleUtils {
    private static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "EnhancedMules" + ChatColor.GRAY + "] ";

    public static class Debug {
        private static boolean debugMode = false;

        public static void log(String message) {
            if (debugMode) {
                EnhancedMules.getInstance().getLogger().info("[Debug] " + message);
            }
        }

        public static void error(String message, Exception e) {
            EnhancedMules.getInstance().getLogger().log(Level.SEVERE, message, e);
        }

        public static void sendDebug(CommandSender sender, String message) {
            if (debugMode && sender.hasPermission("enhancedmules.debug")) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "[Debug] " + ChatColor.WHITE + message);
            }
        }

        public static void toggleDebug() {
            debugMode = !debugMode;
        }

        public static boolean isDebugMode() {
            return debugMode;
        }
    }

    public static class Permissions {
        public static final String USE = "enhancedmules.use";
        public static final String ADMIN = "enhancedmules.admin";
        public static final String DEBUG = "enhancedmules.debug";
        public static final String CREATE = "enhancedmules.create";
        public static final String MODIFY = "enhancedmules.modify";
        public static final String REMOVE = "enhancedmules.remove";

        public static boolean hasPermission(Player player, String permission) {
            if (player.hasPermission(ADMIN)) return true;
            return player.hasPermission(permission);
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.WHITE + message);
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + message);
    }
}