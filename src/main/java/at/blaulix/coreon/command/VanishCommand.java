package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.VanishHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VanishCommand implements CommandExecutor {

    // Handle /vanish command; only players with permission can use it
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("coreon.vanish")) {
            sender.sendMessage("You don't have permission to use this command.");
            return false;
        }
        if (sender instanceof Player player) {
            VanishHandler vanishHandler = new VanishHandler();
            vanishHandler.vanishPlayer(player);
            player.sendMessage("You are now vanished!");
            return true;
        } else {
            sender.sendMessage("Only players can use this command.");
            return false;
        }
    }
}
