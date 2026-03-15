package at.blaulix.coreon.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class VanishHandler {

    private final Plugin plugin;

    // Wir brauchen das Plugin für die hidePlayer Methode
    public VanishHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void vanishPlayer(Player player) {
        Component message = (Component) Component.text().color(NamedTextColor.YELLOW);

        if (!player.isInvisible()) {
            player.setInvisible(true);
            player.setSilent(true);
            player.setInvulnerable(true);

            // Entfernt den Spieler für alle anderen aus der Tabliste und der Welt
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }

            message = message.append(player.displayName()).append(Component.text(" left the game."));
            Bukkit.broadcast(message); // Nachricht an alle senden
        } else {
            player.setInvisible(false);
            player.setSilent(false);
            player.setInvulnerable(false);

            // Fügt den Spieler für alle wieder zur Tabliste und Welt hinzu
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }

            message = message.append(player.displayName()).append(Component.text(" joined the game."));
            Bukkit.broadcast(message); // Nachricht an alle senden
        }
    }
}