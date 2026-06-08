package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.EcseeHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for the EcSee feature, allowing players to inspect and
 * edit other players' ender chests.
 * <p>
 * <b>Command Syntax:</b> {@code /ecsee <player>}
 * <p>
 * <b>Behavior:</b>
 * <ul>
 *   <li>Only players can use this command (console usage is rejected)</li>
 *   <li>Exactly one argument (the target player name) is required</li>
 *   <li>Supports both online and offline players via {@link OfflinePlayer}</li>
 *   <li>Opens a 27-slot GUI displaying the target's ender chest</li>
 * </ul>
 *
 * @author Coreon Team
 * @see EcseeHandler
 */
public class EcseeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player viewer = (Player) sender;

        if (args.length != 1) {
            viewer.sendMessage("§cUsage: /ecsee <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        EcseeHandler ecseeHandler = new EcseeHandler();
        ecseeHandler.ecsee(viewer, target);

        return true;
    }
}