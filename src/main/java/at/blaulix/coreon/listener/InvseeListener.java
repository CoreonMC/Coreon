package at.blaulix.coreon.listener;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.handler.InvseeHandler;
import at.blaulix.coreon.util.Playerdata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Syncs InvSee GUI changes back to the real player's inventory.
 *
 * Online  target → live inventory sync (as before)
 * Offline target → changes are written to the player's YAML file so they
 *                  take effect the next time the player joins the server.
 */
public class InvseeListener implements Listener {

    private final Coreon plugin;

    public InvseeListener(Coreon plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Click
    // -------------------------------------------------------------------------

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(InvseeHandler.TITLE_PREFIX)) return;

        // Block separator row
        if (event.getRawSlot() >= 36 && event.getRawSlot() <= 44) {
            event.setCancelled(true);
            return;
        }

        // Only handle clicks inside the top (InvSee) inventory
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleChange(event.getInventory(), title)
        );
    }

    // -------------------------------------------------------------------------
    // Drag
    // -------------------------------------------------------------------------

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(InvseeHandler.TITLE_PREFIX)) return;

        // If any dragged slot hits the separator, cancel the whole drag
        for (int slot : event.getRawSlots()) {
            if (slot >= 36 && slot <= 44) {
                event.setCancelled(true);
                return;
            }
        }

        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleChange(event.getInventory(), title)
        );
    }

    // -------------------------------------------------------------------------
    // Central change handler
    // -------------------------------------------------------------------------

    /**
     * Decides whether the target is online or offline and routes accordingly.
     *
     * @param gui   The InvSee inventory (54 slots)
     * @param title The inventory title (may contain embedded UUID for offline)
     */
    private void handleChange(Inventory gui, String title) {
        if (title.contains(InvseeHandler.UUID_SEPARATOR)) {
            // ── OFFLINE player ──────────────────────────────────────────────
            // Title format: "§8InvSee §7<name>§r§0|<uuid>"
            String uuid = title.substring(title.indexOf(InvseeHandler.UUID_SEPARATOR)
                    + InvseeHandler.UUID_SEPARATOR.length());

            // Check whether the player came online between the event and this task
            Player nowOnline = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (nowOnline != null && nowOnline.isOnline()) {
                // Player logged in in the meantime → sync live
                syncToOnlinePlayer(gui, nowOnline);
            } else {
                // Still offline → persist to YAML
                saveToYaml(gui, uuid);
            }
        } else {
            // ── ONLINE player ────────────────────────────────────────────────
            String targetName = title.substring(InvseeHandler.TITLE_PREFIX.length());
            Player target = plugin.getServer().getPlayer(targetName);
            if (target != null && target.isOnline()) {
                syncToOnlinePlayer(gui, target);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Push GUI contents directly into an online player's inventory. */
    private void syncToOnlinePlayer(Inventory gui, Player target) {
        // Main storage (0-35)
        for (int i = 0; i < 36; i++) {
            target.getInventory().setItem(i, gui.getItem(i));
        }
        // Armor (45-48) and offhand (49)
        target.getInventory().setHelmet(gui.getItem(45));
        target.getInventory().setChestplate(gui.getItem(46));
        target.getInventory().setLeggings(gui.getItem(47));
        target.getInventory().setBoots(gui.getItem(48));
        target.getInventory().setItemInOffHand(gui.getItem(49));
        target.updateInventory();
    }

    /**
     * Persist GUI contents to the offline player's YAML file.
     * The changes will be loaded automatically when the player next joins
     * because the server reads from the same file on join (or Coreon's
     * QuitListener overwrites it only on the NEXT quit after joining).
     *
     * To guarantee the offline edits survive the player's next login we also
     * need to apply them once PlayerJoinEvent fires – see note in README.
     */
    private void saveToYaml(Inventory gui, String uuid) {
        // Reconstruct arrays from GUI slots
        ItemStack[] content = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            content[i] = gui.getItem(i);
        }

        ItemStack[] armor = new ItemStack[]{
                gui.getItem(45), // helmet
                gui.getItem(46), // chestplate
                gui.getItem(47), // leggings
                gui.getItem(48)  // boots
        };

        ItemStack offhand = gui.getItem(49);

        Playerdata.saveOfflineInventory(uuid, content, armor, offhand);
    }
}