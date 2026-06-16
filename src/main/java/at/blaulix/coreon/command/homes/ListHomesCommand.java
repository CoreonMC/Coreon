package at.blaulix.coreon.command.homes;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.HomesHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Command zum Auflisten der Homes (/home list). Unterstützt optional das Auflisten
 * von Homes anderer Spieler, falls die Permission gesetzt ist.
 */
public class ListHomesCommand implements CommandExecutor {
    private final JavaPlugin javaPlugin;
    private final HomeDatabase homeDatabase;

    /**
     * Konstruktor.
     *
     * @param plugin       Plugin-Instanz
     * @param homeDatabase HomeDatabase-Wrapper
     */
    public ListHomesCommand(JavaPlugin plugin, HomeDatabase homeDatabase) {
        this.javaPlugin = plugin;
        this.homeDatabase = homeDatabase;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HomesHandler homesHandler = new HomesHandler((Coreon) javaPlugin, homeDatabase);
        Player player = (Player) sender;
        if (!player.hasPermission("coreon.homes.list")) {
            player.sendMessage("§cYou don't have permission to list your homes.");
            return false;
        } else if (args[0].isEmpty()) {
            homesHandler.listHomes(player);
            return true;
        } else {
            if (!player.hasPermission("coreon.homes.list.others")) {
                player.sendMessage("§cYou don't have permission to list other players homes.");
                return false;
            }
            Player targetPlayer = javaPlugin.getServer().getPlayer(args[0]);
            if (targetPlayer == null) {
                player.sendMessage("§cPlayer not found.");
                return false;
            }
            homesHandler.listHomes(targetPlayer);
            return true;
        }
    }
}
