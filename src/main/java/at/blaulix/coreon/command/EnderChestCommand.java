package at.blaulix.coreon.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for the EnderChest feature.
 * Allows a player to open their own ender chest via command.
 * <p>
 * <b>Command Syntax:</b> {@code /ec}
 * <p>
 * <b>Behavior:</b>
 * <ul>
 *   <li>Only players can use this command (console usage is rejected)</li>
 *   <li>Opens the player's own ender chest inventory directly</li>
 * </ul>
 *
 * @author Coreon Team
 */
public class EnderChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(player.getEnderChest());
        return true;
    }
}
