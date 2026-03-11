package at.blaulix.coreon.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import static org.bukkit.Bukkit.getServer;

/**
 * Handler to toggle a player's vanish state (invisible + silent) and notify
 * a server player by UUID. Comments are short and in English.
 */
public class VanishHandler {
    /**
     * Toggle vanish for the given player and notify the player found by UUID.
     * If the lookup returns null, no message is sent.
     */
    public void vanishPlayer(Player player) {
        Player allPlayer = getServer().getPlayer(player.getUniqueId());

        // Start with yellow text color for the message
        Component message = Component.text().color(NamedTextColor.YELLOW).build();

        if(!player.isInvisible()) {
            player.setInvisible(true);
            player.setSilent(true);
            player.setInvulnerable(true);

            // Reassign appended parts back to message (Component is immutable)
            message = message.append(player.displayName())
                    .append(Component.text(" left the game."));

            // allPlayer can be null if not found; check before sending
            if (allPlayer != null) {
                allPlayer.sendMessage(message);
            }
        } else {
            player.setInvisible(false);
            player.setSilent(false);
            player.setInvulnerable(false);

            // Reassign appended parts back to message
            message = message.append(player.displayName())
                    .append(Component.text(" joined the game."));

            // allPlayer can be null; check before sending
            if (allPlayer != null) {
                allPlayer.sendMessage(message);
            }
        }
    }

}
