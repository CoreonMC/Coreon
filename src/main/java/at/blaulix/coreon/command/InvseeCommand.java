package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.InvseeHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 1. Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player viewer = (Player) sender;

        // 2. Check for correct arguments
        if (args.length != 1) {
            viewer.sendMessage("§cUsage: /invsee <player>");
            return true;
        }

        // 3. Get the target (using getOfflinePlayer to support your handler's offline logic)
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        // 4. Call the handler with the correct order: (Viewer, Target)
        InvseeHandler invseeHandler = new InvseeHandler();
        invseeHandler.invsee(viewer, target);

        return true;
    }
}