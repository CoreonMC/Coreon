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

public class InvseeListener implements Listener {

    private final Coreon plugin;

    public InvseeListener(Coreon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(InvseeHandler.TITLE_PREFIX)) return;

        if (event.getRawSlot() >= 36 && event.getRawSlot() <= 44) {
            event.setCancelled(true);
            return;
        }

        Inventory topInv = event.getView().getTopInventory();

        boolean inTop = event.getRawSlot() < topInv.getSize();
        boolean shiftFromBottom = event.isShiftClick() && event.getRawSlot() >= topInv.getSize();

        if (!inTop && !shiftFromBottom) return;

        int delay = shiftFromBottom ? 2 : 1;
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                handleChange(topInv, title), delay
        );
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(InvseeHandler.TITLE_PREFIX)) return;

        for (int slot : event.getRawSlots()) {
            if (slot >= 36 && slot <= 44) {
                event.setCancelled(true);
                return;
            }
        }

        Inventory topInv = event.getView().getTopInventory();
        plugin.getServer().getScheduler().runTask(plugin, () ->
                handleChange(topInv, title)
        );
    }

    private void handleChange(Inventory gui, String title) {
        if (title.contains(InvseeHandler.UUID_SEPARATOR)) {
            String uuid = title.substring(title.indexOf(InvseeHandler.UUID_SEPARATOR)
                    + InvseeHandler.UUID_SEPARATOR.length());

            Player nowOnline = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
            if (nowOnline != null && nowOnline.isOnline()) {
                syncToOnlinePlayer(gui, nowOnline);
            } else {
                saveToYaml(gui, uuid);
            }
        } else {
            String targetName = title.substring(InvseeHandler.TITLE_PREFIX.length());
            Player target = plugin.getServer().getPlayer(targetName);
            if (target != null && target.isOnline()) {
                syncToOnlinePlayer(gui, target);
            }
        }
    }

    private void syncToOnlinePlayer(Inventory gui, Player target) {
        for (int i = 0; i < 36; i++) target.getInventory().setItem(i, gui.getItem(i));
        target.getInventory().setHelmet(gui.getItem(45));
        target.getInventory().setChestplate(gui.getItem(46));
        target.getInventory().setLeggings(gui.getItem(47));
        target.getInventory().setBoots(gui.getItem(48));
        target.getInventory().setItemInOffHand(gui.getItem(49));
        target.updateInventory();

        // Persist to YAML so JoinListener doesn't overwrite with stale data
        ItemStack[] content = new ItemStack[36];
        for (int i = 0; i < 36; i++) content[i] = gui.getItem(i);
        ItemStack[] armor = {gui.getItem(45), gui.getItem(46), gui.getItem(47), gui.getItem(48)};
        Playerdata.saveOfflineInventory(target.getUniqueId().toString(), content, armor, gui.getItem(49));
    }

    private void saveToYaml(Inventory gui, String uuid) {
        ItemStack[] content = new ItemStack[36];
        for (int i = 0; i < 36; i++) content[i] = gui.getItem(i);
        ItemStack[] armor = {gui.getItem(45), gui.getItem(46), gui.getItem(47), gui.getItem(48)};
        Playerdata.saveOfflineInventory(uuid, content, armor, gui.getItem(49));
    }
}
