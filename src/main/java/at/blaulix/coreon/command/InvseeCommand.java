package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.InvseeHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for the InvSee feature, allowing players to inspect and
 * edit other players' inventories.
 * <p>
 * <b>Command Syntax:</b> {@code /invsee <player>}
 * <p>
 * <b>Behavior:</b>
 * <ul>
 *   <li>Only players can use this command (console usage is rejected)</li>
 *   <li>Exactly one argument (the target player name) is required</li>
 *   <li>Supports both online and offline players via {@link OfflinePlayer}</li>
 *   <li>Opens a 54-slot GUI displaying the target's inventory</li>
 * </ul>
 * <p>
 * <b>Offline Player Support:</b>
 * If the target player is offline, their saved inventory data is loaded from
 * the Coreon playerdata folder. Any modifications made to an offline player's
 * inventory will be persisted to their YAML file.
 *
 * @author Coreon Team
 * @see InvseeHandler
 */
public class InvseeCommand implements CommandExecutor {

    /**
     * Executes the InvSee command to open a player's inventory GUI.
     * <p>
     * <b>Validation:</b>
     * <ul>
     *   <li>Sender must be a player (not console)</li>
     *   <li>Exactly one argument must be provided (the target player name)</li>
     * </ul>
     * <p>
     * On success, opens the InvSee GUI for the command executor, displaying the
     * target player's inventory.
     *
     * @param sender  the entity that executed the command
     * @param command the command object
     * @param label   the command label
     * @param args    the command arguments (should be [targetPlayerName])
     * @return {@code true} if the command is valid, {@code false} otherwise
     */
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