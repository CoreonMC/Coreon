package at.blaulix.coreon.listener;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.handler.EcseeHandler;
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
 * Handles inventory synchronization for the EcSee feature.
 * <p>
 * Captures click and drag events from EcSee GUI inventories and
 * synchronizes changes back to the target player's actual ender chest.
 * <p>
 * Online target: Changes are synced directly to the player's live ender chest.
 * Offline target: Changes are persisted to the player's YAML file.
 *
 * @author Coreon Team
 * @see EcseeHandler
 */
public class EcseeListener implements Listener {

    private final Coreon plugin;

    public EcseeListener(Coreon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(EcseeHandler.TITLE_PREFIX)) return;

        Inventory topInv = event.getView().getTopInventory();

        // Normal click in top inventory, OR shift-click from bottom inventory into top
        boolean inTop = event.getRawSlot() < topInv.getSize();
        boolean shiftFromBottom = event.isShiftClick() && event.getRawSlot() >= topInv.getSize();

        if (!inTop && !shiftFromBottom) return;

        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleChange(topInv, title)
        );
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(EcseeHandler.TITLE_PREFIX)) return;

        Inventory topInv = event.getView().getTopInventory();

        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleChange(topInv, title)
        );
    }

    private void handleChange(Inventory gui, String title) {
        if (title.contains(EcseeHandler.UUID_SEPARATOR)) {
            // Offline player
            String uuid = title.substring(title.indexOf(EcseeHandler.UUID_SEPARATOR)
                    + EcseeHandler.UUID_SEPARATOR.length());

            Player nowOnline = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (nowOnline != null && nowOnline.isOnline()) {
                syncToOnlinePlayer(gui, nowOnline);
            } else {
                saveToYaml(gui, uuid);
            }
        } else {
            // Online player
            String targetName = title.substring(EcseeHandler.TITLE_PREFIX.length());
            Player target = plugin.getServer().getPlayer(targetName);
            if (target != null && target.isOnline()) {
                syncToOnlinePlayer(gui, target);
            }
        }
    }

    private void syncToOnlinePlayer(Inventory gui, Player target) {
        ItemStack[] contents = new ItemStack[27];
        for (int i = 0; i < 27; i++) contents[i] = gui.getItem(i);

        // Debug: log what we're writing
        plugin.getLogger().info("[EcSee DEBUG] Syncing to " + target.getName() + ":");
        for (int i = 0; i < 27; i++) {
            if (contents[i] != null) {
                plugin.getLogger().info("  slot " + i + ": " + contents[i].getType() + " x" + contents[i].getAmount());
            }
        }

        target.getEnderChest().setContents(contents);
        target.updateInventory();
        Playerdata.saveOfflineEnderChest(target.getUniqueId().toString(), contents);
    }

    private void saveToYaml(Inventory gui, String uuid) {
        ItemStack[] contents = new ItemStack[27];
        for (int i = 0; i < 27; i++) contents[i] = gui.getItem(i);
        Playerdata.saveOfflineEnderChest(uuid, contents);
    }
}