package at.blaulix.coreon.command.homes;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.HomesHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command zum Setzen eines Homes (/home set &lt;name&gt;).
 */
public class SetHomeCommand implements CommandExecutor {
    private final Coreon plugin;
    private final HomeDatabase homeDatabase;

    /**
     * Konstruktor.
     *
     * @param plugin       Coreon Plugin-Instanz
     * @param homeDatabase HomeDatabase-Wrapper
     */
    public SetHomeCommand(Coreon plugin, HomeDatabase homeDatabase) {
        this.plugin = plugin;
        this.homeDatabase = homeDatabase;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HomesHandler homesHandler = new HomesHandler(plugin, homeDatabase);
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("coreon.homes.set")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        if (args.length == 0 || args[0].trim().isEmpty()) {
            homesHandler.helpHomes(player);
            return true;
        }

        homesHandler.setHome(player, args[0]);
        return true;
    }
}
