package at.blaulix.coreon.listener;

import at.blaulix.coreon.util.Playerdata;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener, der beim Verlassen eines Spielers dessen Inventar-/Enderchest-Daten
 * in die playerdata YAML-Datei speichert.
 */
public class QuitListener implements Listener {

    /**
     * Wird aufgerufen, wenn ein Spieler den Server verlässt und speichert die
     * Inventar-/Enderchest-Daten des Spielers.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        // Get the player who is leaving the server
        Player player = event.getPlayer();

        // Save the player's data on disconnect
        Playerdata.savePlayerData(player);
    }
}