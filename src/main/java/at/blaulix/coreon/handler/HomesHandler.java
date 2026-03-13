package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.database.HomeDatabase;
import org.bukkit.Bukkit;
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
        int homeLimit = plugin.getConfig().getInt("max-homes.default");
        if(getHomeCount(player) >= homeLimit){
            String message = plugin.getHomesConfig().getPath() + ".message.home-limit-reached";
            String editedMessage = message.replace("%limit%", String.valueOf(homeLimit));
            player.sendMessage(editedMessage);
            return;
        }

        Location loc = player.getLocation();
        String message = plugin.getHomesConfig().getPath() + ".message.sethome";
        String editedMessage = message.replace("%home%", homeName);
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            homeDatabase.saveHome(player.getUniqueId(), homeName, loc);
            Bukkit.getScheduler().runTask(javaPlugin, () ->
                    player.sendMessage(editedMessage));
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

            String tpMessage = plugin.getHomesConfig().getPath() + ".message.home-teleported";
            String tpMessageEdited = tpMessage.replace("%home%", homeName);

            String notExistMessage = plugin.getHomesConfig().getPath() + ".message.home-not-exist";
            String notExistMessageEdited = notExistMessage.replace("%home%", homeName);
            Bukkit.getScheduler().runTask(javaPlugin, () -> {
                if (loc != null) {
                    player.teleport(loc);
                    player.sendMessage(tpMessageEdited);
                } else {
                    player.sendMessage(notExistMessageEdited);
                }
            });
        });
    }

    // List player's homes and send them a message
    public void listHomes(Player player) {
        UUID playerUUID = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            List<String> homes = homeDatabase.getHomesList(playerUUID);
            String message = plugin.getHomesConfig().getPath() + ".message.home-list";
            String editedMessage = message.replace("%homes%", String.join(", ", homes));
            Bukkit.getScheduler().runTask(javaPlugin, () ->
                    player.sendMessage(editedMessage));
        });
    }
}