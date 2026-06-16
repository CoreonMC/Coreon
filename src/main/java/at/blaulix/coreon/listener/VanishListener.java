package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.VanishHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Listener für das Vanish-System: verhindert Interaktionen und Sichtbarkeit
 * für versteckte Spieler und versteckt bereits versteckte Spieler beim Join
 * für neu verbindende Spieler.
 */
public class VanishListener implements Listener {

    private final Plugin plugin;
    private final VanishHandler vanishHandler;

    /**
     * Konstruktor.
     *
     * @param plugin        Plugin-Instanz (benötigt für hide/showPlayer)
     * @param vanishHandler VanishHandler mit der Menge versteckter Spieler
     */
    public VanishListener(Plugin plugin, VanishHandler vanishHandler) {
        this.plugin = plugin;
        this.vanishHandler = vanishHandler;
    }

    /**
     * Versteckt beim Join alle Spieler, die aktuell im Vanish sind, vor dem
     * gerade verbundenen Spieler.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();

        for (UUID vanishedUUID : vanishHandler.getVanishedPlayers()) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedUUID);
            if (vanishedPlayer != null) {
                joinedPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    /**
     * Verhindert das Aufnehmen von Items durch Spieler, die sich im Vanish-Modus befinden.
     *
     * @param event EntityPickupItemEvent
     */
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (vanishHandler.getVanishedPlayers().contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Verhindert physikalische Interaktionen (z.B. Druckplatten) von vanished Spielern.
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler
    public void onPhysicalInteract(PlayerInteractEvent event) {
        // Wir prüfen direkt, ob die Action PHYSICAL ist (Druckplatten, Stolperdrähte, Felder)
        if (event.getAction() == Action.PHYSICAL) {
            if (vanishHandler.getVanishedPlayers().contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Verhindert, dass Mobs Spieler im Vanish als Ziel wählen.
     *
     * @param event EntityTargetEvent
     */
    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (vanishHandler.getVanishedPlayers().contains(player.getUniqueId())) {
                event.setTarget(null);
                event.setCancelled(true);
            }
        }
    }
}