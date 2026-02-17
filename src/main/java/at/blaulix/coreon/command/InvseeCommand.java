package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.InvseeHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /invsee <player>");
            return false;
        }
        Player target = sender.getServer().getPlayer(args[0]);
        if (target != null) {
            InvseeHandler invseeHandler = new InvseeHandler();
            invseeHandler.invsee(target, (Player) sender);
            return true;
        }
        return false;
    }
}
