package at.blaulix.coreon.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import static org.bukkit.Bukkit.getServer;

/**
 * Toggle player's vanish state and notify a server player by UUID.
 */
public class VanishHandler {
    /**
     * Toggle vanish and send a short message if player found.
     */
    public void vanishPlayer(Player player) {
        Player allPlayer = getServer().getPlayer(player.getUniqueId());

        // Base message (yellow)
        Component message = Component.text().color(NamedTextColor.YELLOW).build();

        if(!player.isInvisible()) {
            player.setInvisible(true);
            player.setSilent(true);
            player.setInvulnerable(true);

            message = message.append(player.displayName())
                    .append(Component.text(" left the game."));

            if (allPlayer != null) {
                allPlayer.sendMessage(message);
            }
        } else {
            player.setInvisible(false);
            player.setSilent(false);
            player.setInvulnerable(false);

            message = message.append(player.displayName())
                    .append(Component.text(" joined the game."));

            if (allPlayer != null) {
                allPlayer.sendMessage(message);
            }
        }
    }

}
