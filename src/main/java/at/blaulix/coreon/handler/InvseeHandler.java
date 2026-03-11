package at.blaulix.coreon.handler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;

/**
 * Show another player's inventory in a GUI (invsee).
 * Supports online (live) and offline (saved YAML) targets.
 */
public class InvseeHandler {

    /**
     * Open a 54-slot GUI for `viewer` showing `target`'s inventory.
     * Armor and Offhand are displayed in the bottom row.
     */
    public void invsee(Player viewer, OfflinePlayer target) {
        // Create a 54-slot inventory (6 rows)
        Inventory gui = Bukkit.createInventory(null, 54, "§8InvSee §7" + target.getName());

        if (target.isOnline() && target.getPlayer() != null) {
            Player online = target.getPlayer();

            // 1. Fill main inventory (Slots 0-35)
            ItemStack[] contents = online.getInventory().getStorageContents();
            for (int i = 0; i < contents.length && i < 36; i++) {
                gui.setItem(i, contents[i]);
            }

            // 2. Add a separator line in the 5th row (Slots 36-44)
            for (int i = 36; i < 45; i++) {
                gui.setItem(i, createSeparator());
            }

            // 3. Fill Armor and Offhand in the bottom row (Slots 45-49)
            gui.setItem(45, online.getInventory().getHelmet());
            gui.setItem(46, online.getInventory().getChestplate());
            gui.setItem(47, online.getInventory().getLeggings());
            gui.setItem(48, online.getInventory().getBoots());
            gui.setItem(49, online.getInventory().getItemInOffHand());
        }
        else {
            // CASE: Target is offline
            File file = new File("plugins/Coreon/playerdata/" + target.getUniqueId() + ".yml");

            if (!file.exists()) {
                viewer.openInventory(gui);
                return;
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Load main storage contents (Slots 0-35)
            List<?> storage = config.getList("inventory.content");
            if (storage != null) {
                for (int i = 0; i < storage.size() && i < 36; i++) {
                    gui.setItem(i, (ItemStack) storage.get(i));
                }
            }

            // Add separator for offline view as well
            for (int i = 36; i < 45; i++) {
                gui.setItem(i, createSeparator());
            }

            // Load armor from YAML (Slots 45-48)
            List<?> armor = config.getList("inventory.armor");
            if (armor != null) {
                // Typical order in Bukkit armor list: Boots(0), Legs(1), Chest(2), Helm(3)
                // We map them to: Helm(45), Chest(46), Legs(47), Boots(48)
                for (int i = 0; i < armor.size() && i < 4; i++) {
                    gui.setItem(48 - i, (ItemStack) armor.get(i));
                }
            }

            // Load offhand if exists (Slot 49)
            if (config.contains("inventory.offhand")) {
                gui.setItem(49, config.getItemStack("inventory.offhand"));
            }
        }

        viewer.openInventory(gui);
    }

    /**
     * Simple helper to create a separator item (Gray Glass Pane).
     */
    private ItemStack createSeparator() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }
}