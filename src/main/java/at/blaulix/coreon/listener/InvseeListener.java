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
 * Handles inventory synchronization for the InvSee feature.
 * <p>
 * This listener captures click and drag events from InvSee GUI inventories and
 * synchronizes any changes back to the target player's actual inventory.
 * <p>
 * <b>Behavior depends on player online status:</b>
 * <ul>
 *   <li><b>Online target:</b> Changes are synced directly to the player's live inventory</li>
 *   <li><b>Offline target:</b> Changes are persisted to the player's YAML file and will
 *       take effect when the player joins the server</li>
 * </ul>
 * <p>
 * The listener automatically blocks interaction with the separator row (slots 36-44)
 * and prevents drags that would cross into the separator.
 *
 * @author Coreon Team
 * @see InvseeHandler
 * @see Playerdata
 */
public class InvseeListener implements Listener {

    private final Coreon plugin;

    /**
     * Constructs an InvseeListener with the Coreon plugin instance.
     *
     * @param plugin the Coreon plugin instance used for task scheduling
     */
    public InvseeListener(Coreon plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Click
    // -------------------------------------------------------------------------

    /**
     * Handles inventory click events in InvSee GUIs.
     * <p>
     * Blocks clicks on the separator row (slots 36-44) and routes inventory changes
     * to the change handler synchronously. Only processes clicks in the top inventory.
     *
     * @param event the inventory click event
     */
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

    /**
     * Handles inventory drag events in InvSee GUIs.
     * <p>
     * Cancels the entire drag operation if any affected slot intersects with the
     * separator row (slots 36-44). Otherwise, routes the inventory change to the
     * change handler.
     *
     * @param event the inventory drag event
     */
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
     * Processes inventory changes and routes them to the appropriate sync method.
     * <p>
     * Detects whether the target player is online or offline by checking for a UUID
     * separator in the inventory title. If the separator is found, the UUID is extracted
     * and used to determine the player's current status. If the player was offline but
     * has since come online, the change is synced directly. Otherwise, the change is
     * persisted to the YAML file.
     * <p>
     * For online players, the change is synced directly to the player's inventory.
     *
     * @param gui   the InvSee inventory containing the modified items (54 slots)
     * @param title the inventory title, which may contain an embedded UUID for offline players
     *              in the format: "§8InvSee §7&lt;name&gt;§r§0|&lt;uuid&gt;"
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

    /**
     * Synchronizes the InvSee GUI inventory contents directly to an online player's
     * actual inventory.
     * <p>
     * Copies inventory items from the following GUI slots:
     * <ul>
     *   <li>Slots 0-35: Main inventory storage</li>
     *   <li>Slot 45: Helmet</li>
     *   <li>Slot 46: Chestplate</li>
     *   <li>Slot 47: Leggings</li>
     *   <li>Slot 48: Boots</li>
     *   <li>Slot 49: Offhand item</li>
     * </ul>
     * After the sync, the player's inventory is updated to reflect all changes.
     *
     * @param gui    the InvSee GUI inventory containing the items to sync
     * @param target the online player whose inventory will be updated
     */
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
     * Persists InvSee GUI changes to an offline player's YAML file.
     * <p>
     * Extracts the inventory contents from the GUI and writes them to the player's
     * YAML file, preserving:
     * <ul>
     *   <li>Main inventory storage (36 items)</li>
     *   <li>Armor pieces (4 items: helmet, chestplate, leggings, boots)</li>
     *   <li>Offhand item</li>
     * </ul>
     * <p>
     * The persisted data will be loaded and applied when the player logs in to the
     * server next time, effectively updating their offline inventory permanently.
     * <p>
     * <b>Note:</b> To ensure offline edits survive a player's next login, they will
     * also be reapplied at the JOIN event - see README for additional details.
     *
     * @param gui  the InvSee GUI inventory containing items to save
     * @param uuid the string representation of the offline player's UUID
     * @see Playerdata#saveOfflineInventory(String, ItemStack[], ItemStack[], ItemStack)
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