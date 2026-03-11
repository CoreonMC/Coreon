package at.blaulix.coreon.listener;

import at.blaulix.coreon.Coreon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Synchronizes the virtual InvSee GUI with the actual player's inventory.
 */
public class InvseeListener implements Listener {

    private final Coreon plugin;

    // Constructor to get the plugin instance for the scheduler
    public InvseeListener(Coreon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Check if the inventory is an InvSee GUI
        if (!title.startsWith("§8InvSee §7")) return;

        // Prevent interaction with the separator (Gray Glass Panes)
        if (event.getRawSlot() >= 36 && event.getRawSlot() <= 44) {
            event.setCancelled(true);
            return;
        }

        // Only handle clicks within the top inventory (the GUI)
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;

        Player viewer = (Player) event.getWhoClicked();
        String targetName = title.replace("§8InvSee §7", "");
        Player target = plugin.getServer().getPlayer(targetName);

        // Live sync only works if the target is online
        if (target == null || !target.isOnline()) return;

        // Use the plugin instance to run the task
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            syncToPlayer(event.getInventory(), target);
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§8InvSee §7")) return;

        Player viewer = (Player) event.getWhoClicked();
        String targetName = title.replace("§8InvSee §7", "");
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null || !target.isOnline()) return;

        // Sync after drag using the plugin instance
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            syncToPlayer(event.getInventory(), target);
        });
    }

    private void syncToPlayer(Inventory gui, Player target) {
        // Sync main storage (0-35)
        for (int i = 0; i < 36; i++) {
            target.getInventory().setItem(i, gui.getItem(i));
        }

        // Sync Armor and Offhand (45-49)
        target.getInventory().setHelmet(gui.getItem(45));
        target.getInventory().setChestplate(gui.getItem(46));
        target.getInventory().setLeggings(gui.getItem(47));
        target.getInventory().setBoots(gui.getItem(48));
        target.getInventory().setItemInOffHand(gui.getItem(49));

        target.updateInventory();
    }
}