package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Handles the global PvP timer logic.
 */
public class PvPTimerHandler {

    private final Coreon plugin;
    private int remainingSeconds = 0;
    private BukkitTask task;

    public PvPTimerHandler(Coreon plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts a PvP-disable timer for a specific amount of minutes.
     */
    public void startTimer(int minutes) {
        // Cancel existing timer if one is running
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
        }, 0L, 20L); // Run every second (20 ticks)
    }

    /**
     * @return true if PvP is currently blocked by the timer.
     */
    public boolean isPvPDisabled() {
        return remainingSeconds > 0;
    }
}