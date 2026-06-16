package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Handles global PvP disable timer and notifications.
 */
public class PvPTimerHandler {

    private final Coreon plugin;
    private int remainingSeconds = 0;
    private BukkitTask task;

    /**
     * Erzeugt einen neuen PvPTimerHandler.
     *
     * @param plugin Plugin-Instanz
     */
    public PvPTimerHandler(Coreon plugin) {
        this.plugin = plugin;
    }

    /**
     * Startet einen Timer, der PvP für die angegebene Anzahl Minuten deaktiviert
     * und periodisch Broadcast-Nachrichten sendet.
     *
     * @param minutes Dauer in Minuten
     */
    public void startTimer(int minutes) {
        // Cancel existing timer
        if (task != null) {
            task.cancel();
        }

        remainingSeconds = minutes * 60;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingSeconds > 0) {
                if (remainingSeconds % 60 == 0 || remainingSeconds <= 10) {
                    Bukkit.broadcastMessage("§6[PvP] §7PvP is disabled for another §e" + (remainingSeconds >= 60 ? (remainingSeconds / 60) + "m" : remainingSeconds + "s") + "§7.");
                }
                remainingSeconds--;
            } else {
                Bukkit.broadcastMessage("§6[PvP] §aPvP is now enabled again! Watch out!");
                task.cancel();
                task = null;
            }
        }, 0L, 20L); // run every second
    }

    /**
     * Prüft, ob PvP aktuell deaktiviert ist.
     *
     * @return {@code true}, wenn der Timer aktiv ist und PvP deaktiviert ist
     */
    public boolean isPvPDisabled() {
        return remainingSeconds > 0;
    }
}