package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.PvPTimerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvPTimerListener implements Listener {

    private final PvPTimerHandler handler;

    public PvPTimerListener(PvPTimerHandler handler) {
        this.handler = handler;
    }

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