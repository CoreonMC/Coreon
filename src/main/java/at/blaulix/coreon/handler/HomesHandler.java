package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class HomesHandler {
    Coreon plugin;
    private final JavaPlugin javaPlugin;
    private final Database database;

    public HomesHandler(JavaPlugin plugin, Database database) {
        this.javaPlugin = plugin;
        this.database = database;
    }

    /**
     * NICHT FERTIG!!!!!!!!!!!!!
     */
    public void setHome(Player player, String homeName) {
        UUID playerUUID = player.getUniqueId();
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
            database.saveHome(player.getUniqueId(), homeName, loc);
            Bukkit.getScheduler().runTask(javaPlugin, () ->
                    player.sendMessage(editedMessage));
        });
    }

    public int getHomeCount(Player player) {
        return database.getHomeCount(player.getUniqueId());
    }

    public void teleportToHome(Player player, String homeName) {
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            Location loc = database.getHome(player.getUniqueId(), homeName);

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

    public void listHomes(Player player) {
        UUID playerUUID = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            //List[] homes = database.getHomes(playerUUID);
            String message = plugin.getHomesConfig().getPath() + ".message.home-list";
            //String editedMessage = message.replace("%homes%", String.join(", ", homes));
            //Bukkit.getScheduler().runTask(javaPlugin, () ->
            //        player.sendMessage(editedMessage));
        });
    }
}