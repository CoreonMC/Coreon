package at.blaulix.coreon.listener;

import at.blaulix.coreon.Coreon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

/**
 * Applies any offline inventory/enderchest edits (made via InvSee/EcSee while
 * the player was offline) to the player when they join the server.
 *
 * File I/O runs async to avoid blocking the server thread.
 * Inventory changes are then applied back on the main thread.
 */
public class JoinListener implements Listener {

    private final Coreon plugin;

    public JoinListener(Coreon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File file = new File("plugins/Coreon/playerdata/" + player.getUniqueId() + ".yml");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!file.exists()) return;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                List<?> storage = config.getList("inventory.content");
                if (storage != null) {
                    for (int i = 0; i < storage.size() && i < 36; i++)
                        player.getInventory().setItem(i, (ItemStack) storage.get(i));
                }

                List<?> armor = config.getList("inventory.armor");
                if (armor != null) {
                    if (armor.size() > 0) player.getInventory().setHelmet((ItemStack) armor.get(0));
                    if (armor.size() > 1) player.getInventory().setChestplate((ItemStack) armor.get(1));
                    if (armor.size() > 2) player.getInventory().setLeggings((ItemStack) armor.get(2));
                    if (armor.size() > 3) player.getInventory().setBoots((ItemStack) armor.get(3));
                }

                if (config.contains("inventory.offhand"))
                    player.getInventory().setItemInOffHand(config.getItemStack("inventory.offhand"));

                List<?> enderChest = config.getList("enderchest.content");
                if (enderChest != null) {
                    for (int i = 0; i < enderChest.size() && i < 27; i++)
                        player.getEnderChest().setItem(i, (ItemStack) enderChest.get(i));
                }

                player.updateInventory();
            });
        });
    }
}
