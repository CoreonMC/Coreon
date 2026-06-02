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
 * Handles the opening and population of InvSee inventory GUIs.
 * <p>
 * This handler creates a 54-slot inventory interface for viewing and editing
 * another player's inventory, supporting both online and offline players.
 * <p>
 * <b>Title Format:</b>
 * <ul>
 *   <li>Online player: "{@code §8InvSee §7<name>}"</li>
 *   <li>Offline player: "{@code §8InvSee §7<name>§r§0|<uuid>}"</li>
 * </ul>
 * <p>
 * The hidden {@code §r§0|<uuid>} suffix in offline player titles allows the
 * {@link InvseeListener} to identify which player's YAML file to persist changes to,
 * enabling inventory modifications to be saved without the target player being online.
 * <p>
 * <b>GUI Layout (54 slots):</b>
 * <ul>
 *   <li>Slots 0-35: Main inventory storage</li>
 *   <li>Slots 36-44: Separator row (non-interactive gray glass panes)</li>
 *   <li>Slots 45-48: Armor (helmet, chestplate, leggings, boots)</li>
 *   <li>Slot 49: Offhand item</li>
 * </ul>
 *
 * @author Coreon Team
 * @see InvseeListener
 */
public class InvseeHandler {

    /**
     * The colored prefix used in InvSee inventory titles for all players.
     * Value: {@code "§8InvSee §7"}
     */
    public static final String TITLE_PREFIX = "§8InvSee §7";

    /**
     * The separator string used to append a UUID to InvSee titles for offline players.
     * Value: {@code "§r§0|"} (hidden formatting codes)
     */
    public static final String UUID_SEPARATOR = "§r§0|";

    /**
     * Opens a 54-slot InvSee GUI for the viewer to inspect and modify the target
     * player's inventory.
     * <p>
     * <b>For online players:</b>
     * Creates a GUI populated with the target's current inventory state (main storage,
     * armor, and offhand). Changes made in the GUI are synced back to the player's
     * inventory in real-time.
     * <p>
     * <b>For offline players:</b>
     * Attempts to load the target's saved inventory data from their YAML file
     * ({@code plugins/Coreon/playerdata/<uuid>.yml}). The UUID is embedded in the
     * inventory title to allow the listener to persist GUI changes. If no saved data
     * exists, displays an error message and opens an empty GUI.
     * <p>
     * <b>GUI Layout:</b>
     * <ul>
     *   <li>Slots 0-35: Main inventory items</li>
     *   <li>Slots 36-44: Gray separator panes (read-only)</li>
     *   <li>Slot 45: Helmet</li>
     *   <li>Slot 46: Chestplate</li>
     *   <li>Slot 47: Leggings</li>
     *   <li>Slot 48: Boots</li>
     *   <li>Slot 49: Offhand item</li>
     * </ul>
     *
     * @param viewer the player who will view the GUI
     * @param target the player whose inventory will be displayed
     * @see InvseeListener
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

    /**
     * Creates a visual separator item used to divide sections in the InvSee GUI.
     * <p>
     * The separator is a gray stained glass pane with a blank display name,
     * making it non-interactive while providing a clear visual break between
     * the main inventory section and armor/offhand section.
     *
     * @return a new gray glass separator ItemStack
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