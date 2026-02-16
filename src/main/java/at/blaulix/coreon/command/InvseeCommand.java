package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.InvseeHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player target = sender.getServer().getPlayer(args[0]);
        if (target != null) {
            InvseeHandler invseeHandler = new InvseeHandler();
            invseeHandler.invsee(target, (Player) sender);
            return true;
        }
        return false;
    }
}
