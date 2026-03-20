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

public class VanishListener implements Listener {

    private final Plugin plugin;
    private final VanishHandler vanishHandler;

    public VanishListener(Plugin plugin, VanishHandler vanishHandler) {
        this.plugin = plugin;
        this.vanishHandler = vanishHandler;
    }

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

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (vanishHandler.getVanishedPlayers().contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPhysicalInteract(PlayerInteractEvent event) {
        // Wir prüfen direkt, ob die Action PHYSICAL ist (Druckplatten, Stolperdrähte, Felder)
        if (event.getAction() == Action.PHYSICAL) {
            if (vanishHandler.getVanishedPlayers().contains(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

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