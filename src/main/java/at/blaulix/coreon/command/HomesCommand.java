package at.blaulix.coreon.command;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.HomesHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HomesCommand implements CommandExecutor {
    private final JavaPlugin javaPlugin;
    private final HomeDatabase homeDatabase;

    public HomesCommand(JavaPlugin plugin, HomeDatabase homeDatabase) {
        this.javaPlugin = plugin;
        this.homeDatabase = homeDatabase;
    }


    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        HomesHandler homesHandler = new HomesHandler((Coreon) javaPlugin, homeDatabase);
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("set")) {
            homesHandler.setHome(player, args[1]);
        }else if(args[0].equalsIgnoreCase("tp")){
            homesHandler.teleportToHome(player, args[1]);
        }else if(args[0].equalsIgnoreCase("list")){
            homesHandler.listHomes(player);
        }

        return false;
    }
}
