package at.blaulix.coreon.command;

import at.blaulix.coreon.Coreon; // Import deiner Hauptklasse
import at.blaulix.coreon.handler.VanishHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VanishCommand implements CommandExecutor {

    private final Coreon plugin;

    // Konstruktor hinzugefügt, um das Plugin zu empfangen
    public VanishCommand(Coreon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("coreon.vanish")) {
            sender.sendMessage("You don't have permission to use this command.");
            return false;
        }
        if (sender instanceof Player player) {
            // Plugin-Instanz an den Handler weitergeben
            VanishHandler vanishHandler = new VanishHandler(plugin);
            vanishHandler.vanishPlayer(player);

            // Nachricht angepasst, damit sie zum Status passt
            String status = player.isInvisible() ? "vanished" : "visible";
            player.sendMessage("You are now " + status + "!");
            return true;
        } else {
            sender.sendMessage("Only players can use this command.");
            return false;
        }
    }
}