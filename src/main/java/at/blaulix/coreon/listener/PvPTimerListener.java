package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.PvPTimerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Listener, der PvP-Schaden blockiert, wenn der PvP-Timer aktiv ist.
 */
public class PvPTimerListener implements Listener {

    private final PvPTimerHandler handler;

    /**
     * Konstruktor.
     *
     * @param handler PvPTimerHandler
     */
    public PvPTimerListener(PvPTimerHandler handler) {
        this.handler = handler;
    }

    /**
     * Event-Handler, der eingehenden PvP-Schaden unterbindet, wenn PvP deaktiviert ist.
     *
     * @param event EntityDamageByEntityEvent
     */
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        // Check if both involved entities are players
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (handler.isPvPDisabled()) {
                event.setCancelled(true);
                event.getDamager().sendMessage("§cGlobal PvP is currently disabled by a timer.");
            }
        }
    }
}