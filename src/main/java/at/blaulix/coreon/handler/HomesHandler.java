package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        Location loc = player.getLocation();
        String message = plugin.getHomesConfig().getPath() + ".message.sethome".replace("{home}", homeName);
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            database.saveHome(player.getUniqueId(), homeName, loc);
            Bukkit.getScheduler().runTask(javaPlugin, () ->
                    player.sendMessage("§aHome '" + message + "' wurde gesetzt!"));
        });
    }

    public void teleportToHome(Player player, String homeName) {
        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {
            Location loc = database.getHome(player.getUniqueId(), homeName);
            Bukkit.getScheduler().runTask(javaPlugin, () -> {
                if (loc != null) {
                    player.teleport(loc);
                    player.sendMessage("§aDu wurdest zu '" + homeName + "' teleportiert.");
                } else {
                    player.sendMessage("§cHome '" + homeName + "' existiert nicht!");
                }
            });
        });
    }
}