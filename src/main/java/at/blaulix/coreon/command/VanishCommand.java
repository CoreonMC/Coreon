package at.blaulix.coreon.command;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.handler.VanishHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VanishCommand implements CommandExecutor {
    private final Coreon plugin = Coreon.getInstance();
    private final VanishHandler vanishHandler;
    private final boolean VanishModuleEnabled = plugin.getConfig().getBoolean("vanishModuleEnabled");

    public VanishCommand(VanishHandler vanishHandler) {
        this.vanishHandler = vanishHandler;
    }



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if(VanishModuleEnabled) {
            if(!sender.hasPermission("coreon.vanish")) {
                sender.sendMessage("You don't have permission to use this command.");
                return false;
            }
            if (sender instanceof Player player) {
                vanishHandler.vanishPlayer(player);
                player.sendMessage("You are now vanished!");
                return true;
            } else {
                sender.sendMessage("Only players can use this command.");
                return false;
            }
        }
        return false;
    }
}
