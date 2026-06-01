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
 *
 * Title format:
 *   Online  player → "§8InvSee §7<name>"
 *   Offline player → "§8InvSee §7<name>§r§0|<uuid>"
 *
 * The hidden §r§0|<uuid> suffix lets InvseeListener identify the offline
 * player's YAML file so it can persist changes without the target being online.
 */
public class InvseeHandler {

    public static final String TITLE_PREFIX = "§8InvSee §7";
    public static final String UUID_SEPARATOR = "§r§0|";

    /**
     * Open a 54-slot GUI for {@code viewer} showing {@code target}'s inventory.
     */
    public void invsee(Player viewer, OfflinePlayer target) {
        boolean online = target.isOnline() && target.getPlayer() != null;

        // For offline players we embed the UUID in the title so the listener can
        // write changes back to the correct YAML file.
        String title = online
                ? TITLE_PREFIX + target.getName()
                : TITLE_PREFIX + target.getName() + UUID_SEPARATOR + target.getUniqueId();

        Inventory gui = Bukkit.createInventory(null, 54, title);

        if (online) {
            Player onlinePlayer = target.getPlayer();

            // Main storage (Slots 0-35)
            ItemStack[] contents = onlinePlayer.getInventory().getStorageContents();
            for (int i = 0; i < contents.length && i < 36; i++) {
                gui.setItem(i, contents[i]);
            }

            // Separator (Slots 36-44)
            for (int i = 36; i < 45; i++) {
                gui.setItem(i, createSeparator());
            }

            // Armor + Offhand (Slots 45-49)
            gui.setItem(45, onlinePlayer.getInventory().getHelmet());
            gui.setItem(46, onlinePlayer.getInventory().getChestplate());
            gui.setItem(47, onlinePlayer.getInventory().getLeggings());
            gui.setItem(48, onlinePlayer.getInventory().getBoots());
            gui.setItem(49, onlinePlayer.getInventory().getItemInOffHand());

        } else {
            // Target is offline: load saved YAML data
            File file = new File("plugins/Coreon/playerdata/" + target.getUniqueId() + ".yml");

            if (!file.exists()) {
                viewer.sendMessage("§cKeine gespeicherten Inventardaten für diesen Spieler gefunden.");
                viewer.openInventory(gui);
                return;
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // Main storage (Slots 0-35)
            List<?> storage = config.getList("inventory.content");
            if (storage != null) {
                for (int i = 0; i < storage.size() && i < 36; i++) {
                    gui.setItem(i, (ItemStack) storage.get(i));
                }
            }

            // Separator (Slots 36-44)
            for (int i = 36; i < 45; i++) {
                gui.setItem(i, createSeparator());
            }

            // Armor: stored as [helmet, chestplate, leggings, boots] → slots 45-48
            List<?> armor = config.getList("inventory.armor");
            if (armor != null) {
                int[] armorSlots = {45, 46, 47, 48};
                for (int i = 0; i < armor.size() && i < 4; i++) {
                    gui.setItem(armorSlots[i], (ItemStack) armor.get(i));
                }
            }

            // Offhand (Slot 49)
            if (config.contains("inventory.offhand")) {
                gui.setItem(49, config.getItemStack("inventory.offhand"));
            }
        }

        viewer.openInventory(gui);
    }

    /** Create a gray glass separator item (non-interactive). */
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