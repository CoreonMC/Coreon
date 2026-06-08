package at.blaulix.coreon.command.homes;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.HomesHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TpHomeCommand implements CommandExecutor {
    private final JavaPlugin javaPlugin;
    private final HomeDatabase homeDatabase;

    public TpHomeCommand(JavaPlugin plugin, HomeDatabase homeDatabase) {
        this.javaPlugin = plugin;
        this.homeDatabase = homeDatabase;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return false;
        }
        HomesHandler homesHandler = new HomesHandler((Coreon) javaPlugin, homeDatabase);
        if (!sender.hasPermission("coreon.homes.teleport")) {
            sender.sendMessage("§cYou don't have permission to teleport to your homes.");
            return false;
        }
        if (args[0].isEmpty()) {
            homesHandler.helpHomes(player);
            return false;
        }

        homesHandler.teleportToHome(player, args[0]);
        return true;
    }
}
