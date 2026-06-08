package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.database.HomeDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class HomesHandler {
    // Plugin instance (Coreon) for config access
    private final Coreon plugin;
    // JavaPlugin reference used for scheduling tasks
    private final JavaPlugin javaPlugin;
    private final HomeDatabase homeDatabase;

    // Accept Coreon to access custom methods like getHomesConfig()
    public HomesHandler(Coreon plugin, HomeDatabase homeDatabase) {
        this.plugin = plugin;
        this.javaPlugin = plugin;
        this.homeDatabase = homeDatabase;
    }

    /**
     * Save a home for the player asynchronously.
     * Checks limit and informs player.
     */
    public void setHome(Player player, String homeName) {
        int homeLimit = plugin.getConfig().getInt("max-homes");
        if (getHomeCount(player) >= homeLimit) {
            String raw = plugin.getHomesConfig().getString("messages.max-homes",
                    "&cYou have reached the maximum number of homes (%limit%)!");
            String msg = ChatColor.translateAlternateColorCodes('&',
                    raw.replace("%limit%", String.valueOf(homeLimit)));
            player.sendMessage(msg);
            return;
        }

        Location loc = player.getLocation();
        String raw = plugin.getHomesConfig().getString("messages.home-set",
                "&aHome &b%home%&a has been set!");
        String editedMessage = ChatColor.translateAlternateColorCodes('&',
                raw.replace("%home%", homeName));
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            homeDatabase.saveHome(player.getUniqueId(), homeName, loc);
            Bukkit.getScheduler().runTask(javaPlugin, () ->
                    player.sendMessage(editedMessage));
        });
    }

    public void deleteHome(Player player, String homeName){
        Bukkit.getScheduler().runTask(javaPlugin, () -> {
            homeDatabase.deleteHome(player.getUniqueId(), homeName);
             String raw = plugin.getHomesConfig().getString("messages.home-deleted",
                    "&aHome &b%home%&a has been deleted!");
            String editedMessage = ChatColor.translateAlternateColorCodes('&',
                    raw.replace("%home%", homeName));
            player.sendMessage(editedMessage);
        });
    }

    // Return number of homes for player
    public int getHomeCount(Player player) {
        return homeDatabase.getHomeCount(player.getUniqueId());
    }

    // Teleport player to named home asynchronously
    public void teleportToHome(Player player, String homeName) {
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            Location loc = homeDatabase.getHome(player.getUniqueId(), homeName);

            String tpRaw = plugin.getHomesConfig().getString("messages.home-teleported",
                    "&aTeleported to home &b%home%&a!");
            String tpMsg = ChatColor.translateAlternateColorCodes('&',
                    tpRaw.replace("%home%", homeName));

            String notExistRaw = plugin.getHomesConfig().getString("messages.home-not-exist",
                    "&cNo home named &b%home%&c found!");
            String notExistMsg = ChatColor.translateAlternateColorCodes('&',
                    notExistRaw.replace("%home%", homeName));

            Bukkit.getScheduler().runTask(javaPlugin, () -> {
                if (loc != null) {
                    player.teleport(loc);
                    player.sendMessage(tpMsg);
                } else {
                    player.sendMessage(notExistMsg);
                }
            });
        });
    }

    // List player's homes and send them a message
    public void listHomes(Player player) {
        UUID playerUUID = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            List<String> homes = homeDatabase.getHomesList(playerUUID);
            String raw = plugin.getHomesConfig().getString("messages.home-list",
                    "&aYour homes: &b%homes%");
            String msg = ChatColor.translateAlternateColorCodes('&',
                    raw.replace("%homes%", homes.isEmpty() ? "none" : String.join(", ", homes)));
            Bukkit.getScheduler().runTask(javaPlugin, () ->
                    player.sendMessage(msg));
        });
    }

    public void helpHomes(Player player){
        String raw = plugin.getHomesConfig().getString("messages.home-help",
                "&aHome Command Help:\n" +
                "&b/home set <name> &7- Set a home at your current location\n" +
                "&b/home delete <name> &7- Delete a home\n" +
                "&b/home list &7- List your homes\n" +
                "&b/home <name> &7- Teleport to a home");
        String msg = ChatColor.translateAlternateColorCodes('&', raw);
        player.sendMessage(msg);
    }
}