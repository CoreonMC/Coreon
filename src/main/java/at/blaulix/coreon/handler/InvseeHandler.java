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
 * Open and populate an InvSee GUI for viewing another player's inventory.
 */
public class InvseeHandler {

    /**
     * Open a 54-slot GUI for `viewer` showing `target`'s inventory.
     */
    public void invsee(Player viewer, OfflinePlayer target) {
        // Create a 54-slot inventory (6 rows)
        Inventory gui = Bukkit.createInventory(null, 54, "§8InvSee §7" + target.getName());

        if (target.isOnline() && target.getPlayer() != null) {
            Player online = target.getPlayer();

            // Fill main inventory (Slots 0-35)
            ItemStack[] contents = online.getInventory().getStorageContents();
            for (int i = 0; i < contents.length && i < 36; i++) {
                gui.setItem(i, contents[i]);
            }

            // Separator (Slots 36-44)
            for (int i = 36; i < 45; i++) {
                gui.setItem(i, createSeparator());
            }

            // Armor and Offhand in bottom row (45-49)
            gui.setItem(45, online.getInventory().getHelmet());
            gui.setItem(46, online.getInventory().getChestplate());
            gui.setItem(47, online.getInventory().getLeggings());
            gui.setItem(48, online.getInventory().getBoots());
            gui.setItem(49, online.getInventory().getItemInOffHand());
        }
        else {
            // Target is offline: load saved YAML data
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

            // Separator
            for (int i = 36; i < 45; i++) {
                gui.setItem(i, createSeparator());
            }

            // Load armor from YAML (map order to slots)
            List<?> armor = config.getList("inventory.armor");
            if (armor != null) {
                for (int i = 0; i < armor.size() && i < 4; i++) {
                    gui.setItem(48 - i, (ItemStack) armor.get(i));
                }
            }

            // Load offhand if exists
            if (config.contains("inventory.offhand")) {
                gui.setItem(49, config.getItemStack("inventory.offhand"));
            }
        }

        viewer.openInventory(gui);
    }

    /**
     * Create a gray glass separator item.
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