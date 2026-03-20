package at.blaulix.coreon.handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishHandler {
    private final Plugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void vanishPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (!vanishedPlayers.contains(uuid)) {
            vanishedPlayers.add(uuid);
            // Unsichtbarkeit & Kollision
            player.setInvisible(true);
            player.setCollidable(false);
            player.setInvulnerable(true);
            player.setSilent(true);

            // Für alle anderen Spieler auf dem Server verstecken
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!target.equals(player)) {
                    target.hidePlayer(plugin, player);
                }
            }
        } else {
            vanishedPlayers.remove(uuid);
            // Wieder sichtbar machen
            player.setInvisible(false);
            player.setCollidable(true);
            player.setInvulnerable(false);
            player.setSilent(false);

            // Für alle wieder anzeigen
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!target.equals(player)) {
                    target.showPlayer(plugin, player);
                }
            }
        }
    }

    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers;
    }
}