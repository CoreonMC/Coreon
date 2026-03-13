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

    public PvPTimerHandler(Coreon plugin) {
        this.plugin = plugin;
    }

    /**
     * Start a timer that disables PvP for given minutes.
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

    // true if PvP is currently disabled
    public boolean isPvPDisabled() {
        return remainingSeconds > 0;
    }
}