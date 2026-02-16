package at.blaulix.coreon.command;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.handler.CoreonHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoreonCommand implements CommandExecutor {

    private final Coreon plugin;

    public CoreonCommand(Coreon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        if (!sender.hasPermission("coreon.admin")) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (sender instanceof Player player) {
            CoreonHandler coreonHandler = new CoreonHandler(plugin);
            coreonHandler.coreonSettings(player);
            return true;
        }

        return true;
    }
}
