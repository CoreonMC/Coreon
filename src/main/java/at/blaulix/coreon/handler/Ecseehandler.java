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
 * Handles the opening and population of EcSee inventory GUIs.
 * <p>
 * This handler creates a 27-slot inventory interface for viewing and editing
 * another player's ender chest, supporting both online and offline players.
 * <p>
 * <b>Title Format:</b>
 * <ul>
 *   <li>Online player: "{@code §8EcSee §7<name>}"</li>
 *   <li>Offline player: "{@code §8EcSee §7<name>§r§0|<uuid>}"</li>
 * </ul>
 * <p>
 * The hidden {@code §r§0|<uuid>} suffix in offline player titles allows the
 * {@link EcseeListener} to identify which player's YAML file to persist changes to.
 *
 * @author Coreon Team
 * @see EcseeListener
 */
public class EcseeHandler {

    /**
     * The colored prefix used in EcSee inventory titles for all players.
     * Value: {@code "§8EcSee §7"}
     */
    public static final String TITLE_PREFIX = "§8EcSee §7";

    /**
     * The separator string used to append a UUID to EcSee titles for offline players.
     * Value: {@code "§r§0|"} (hidden formatting codes)
     */
    public static final String UUID_SEPARATOR = "§r§0|";

    /**
     * Opens a 27-slot EcSee GUI for the viewer to inspect and modify the target
     * player's ender chest.
     * <p>
     * For online players, loads the ender chest directly from the live inventory.
     * For offline players, loads from their saved YAML file under
     * {@code plugins/Coreon/playerdata/<uuid>.yml}.
     *
     * @param viewer the player who will view the GUI
     * @param target the player whose ender chest will be displayed
     */
    public void ecsee(Player viewer, OfflinePlayer target) {
        boolean online = target.isOnline() && target.getPlayer() != null;

        String title = online
                ? TITLE_PREFIX + target.getName()
                : TITLE_PREFIX + target.getName() + UUID_SEPARATOR + target.getUniqueId();

        Inventory gui = Bukkit.createInventory(null, 27, title);

        if (online) {
            Player onlinePlayer = target.getPlayer();
            ItemStack[] contents = onlinePlayer.getEnderChest().getContents();
            for (int i = 0; i < contents.length && i < 27; i++) {
                gui.setItem(i, contents[i]);
            }
        } else {
            File file = new File("plugins/Coreon/playerdata/" + target.getUniqueId() + ".yml");

            if (!file.exists()) {
                viewer.sendMessage("§cKeine gespeicherten Enderchest-Daten für diesen Spieler gefunden.");
                viewer.openInventory(gui);
                return;
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            List<?> enderChest = config.getList("enderchest.content");
            if (enderChest != null) {
                for (int i = 0; i < enderChest.size() && i < 27; i++) {
                    gui.setItem(i, (ItemStack) enderChest.get(i));
                }
            }
        }

        viewer.openInventory(gui);
    }
}