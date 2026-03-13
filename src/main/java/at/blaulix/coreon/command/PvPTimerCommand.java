package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.PvPTimerHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PvPTimerCommand implements CommandExecutor {

    private final PvPTimerHandler handler;

    public PvPTimerCommand(PvPTimerHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("coreon.pvptimer")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /pvptimer <minutes>");
            return true;
        }

        try {
            int minutes = Integer.parseInt(args[0]);
            handler.startTimer(minutes);
            sender.sendMessage("§aPvP timer started for " + minutes + " minutes.");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cPlease enter a valid number.");
        }

        return true;
    }
}