package at.blaulix.coreon.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.minimessage.tag.standard.StandardTags.color;
import static org.bukkit.Bukkit.getServer;

/**
 * Simple handler for vanishing a player (makes them invisible and silent)
 * and notifying another player on the server.
 */
public class VanishHandler {
    /**
     * Make the given player vanish: mark invisible and silent, then send a
     * short message to the server player returned by UUID lookup.
     * Note: the server lookup may return null in some cases.
     */
    public void vanishPlayer(Player player) {
        Player allPlayer = getServer().getPlayer(player.getUniqueId());

        // Wir starten mit der Farbe Gold/Gelb
        Component message = Component.text().color(NamedTextColor.YELLOW).build();

        if(!player.isInvisible()) {
            player.setInvisible(true);
            player.setSilent(true);

            // WICHTIG: message = ... zuweisen!
            message = message.append(player.displayName())
                    .append(Component.text(" left the game."));

            allPlayer.sendMessage(message);
        } else {
            player.setInvisible(false);
            player.setSilent(false);

            // WICHTIG: message = ... zuweisen!
            message = message.append(player.displayName())
                    .append(Component.text(" joined the game."));

            allPlayer.sendMessage(message);
        }
    }

}
