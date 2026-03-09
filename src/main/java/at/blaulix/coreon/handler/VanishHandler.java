package at.blaulix.coreon.handler;

import org.bukkit.Color;
import org.bukkit.entity.Player;

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
        // Lookup an online Player by UUID (may be null if not found)
        Player allPlayer = getServer().getPlayer(player.getUniqueId());

        // Make the player invisible so they are not visible to others
        player.setInvisible(true);

        // Make the player silent so they do not produce sounds
        player.setSilent(true);

        // Send a simple notification message (this will NPE if allPlayer is null)
        allPlayer.sendMessage(Color.YELLOW + "" + player + " left the game.");
    }
}
