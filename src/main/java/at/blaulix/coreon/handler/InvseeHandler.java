package at.blaulix.coreon.handler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

/**
 * Show another player's inventory in a GUI (invsee).
 * Supports online (live) and offline (saved YAML) targets.
 */
public class InvseeHandler {

    /**
     * Open a 54-slot GUI for `viewer` showing `target`'s inventory.
     * Uses live items for online players, or loads saved items for offline players.
     */
    public void invsee(Player viewer, OfflinePlayer target) {
        // Create a 54-slot inventory with a title showing the target's name.
        Inventory gui = Bukkit.createInventory(null, 54, "§8InvSee §7" + target.getName());

        if (target.isOnline()) {
            // Target is online: read live contents from the Player object.
            Player online = target.getPlayer();
            ItemStack[] contents = online.getInventory().getContents();

            // Copy at most 54 items into the GUI; protects against different inventory sizes.
            for (int i = 0; i < Math.min(contents.length, 54); i++) {
                gui.setItem(i, contents[i]);
            }
        }
        else {
            // Target is offline: try to load a saved YAML file for the player's inventory.
            File file = new File("plugins/Coreon/playerdata/" + target.getUniqueId() + ".yml");

            // If no file exists for this player, open an empty GUI to the viewer.
            if (!file.exists()) {
                Inventory guiEmpty = Bukkit.createInventory(null, 54, "§8InvSee §7" + target.getName());
                viewer.openInventory(guiEmpty);
                return;
            }

            // Load the YAML configuration from disk and read the list stored under inventory.content.
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<?> list = config.getList("inventory.content");

            // If the list is present, cast each element to ItemStack and populate the GUI.
            // Note: this cast assumes the YAML stores actual ItemStack objects; malformed data
            // can cause a ClassCastException at runtime. We keep the original behavior but note
            // this risk in comments.
            if (list != null) {
                for (int i = 0; i < list.size() && i < 54; i++) {
                    gui.setItem(i, (ItemStack) list.get(i));
                }
            }
        }

        // Finally open the assembled inventory for the viewer.
        viewer.openInventory(gui);
    }
}