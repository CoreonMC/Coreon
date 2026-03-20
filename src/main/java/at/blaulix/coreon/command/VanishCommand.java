package at.blaulix.coreon.command;

import at.blaulix.coreon.handler.VanishHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VanishCommand implements CommandExecutor {

    private final VanishHandler vanishHandler;

    // Nur ein Argument im Konstruktor: der VanishHandler
    public VanishCommand(VanishHandler vanishHandler) {
        this.vanishHandler = vanishHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("coreon.vanish")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        // Die Logik wird komplett im Handler ausgeführt
        vanishHandler.vanishPlayer(player);

        // Kurzes Feedback für den Spieler
        String status = vanishHandler.getVanishedPlayers().contains(player.getUniqueId()) ? "vanished" : "visible";
        player.sendMessage("You are now " + status + "!");

        return true;
    }
}