package at.blaulix.coreon.listener;

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
 * Applies any offline inventory edits (made via InvSee while the player was
 * offline) to the player's actual inventory when they join the server.
 *
 * Bukkit/Paper restores the player's inventory from its own playerdata NBT
 * before this event fires. We then overwrite that with our YAML data so the
 * admin's changes take effect immediately.
 *
 * Priority HIGHEST ensures we run after other plugins that may modify
 * inventories on join, but before MONITOR listeners that only observe.
 */
public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File file = new File("plugins/Coreon/playerdata/" + player.getUniqueId() + ".yml");

        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Main storage (0-35)
        List<?> storage = config.getList("inventory.content");
        if (storage != null) {
            for (int i = 0; i < storage.size() && i < 36; i++) {
                player.getInventory().setItem(i, (ItemStack) storage.get(i));
            }
        }

        // Armor: [helmet, chestplate, leggings, boots]
        List<?> armor = config.getList("inventory.armor");
        if (armor != null) {
            if (armor.size() > 0) player.getInventory().setHelmet((ItemStack) armor.get(0));
            if (armor.size() > 1) player.getInventory().setChestplate((ItemStack) armor.get(1));
            if (armor.size() > 2) player.getInventory().setLeggings((ItemStack) armor.get(2));
            if (armor.size() > 3) player.getInventory().setBoots((ItemStack) armor.get(3));
        }

        // Offhand
        if (config.contains("inventory.offhand")) {
            player.getInventory().setItemInOffHand(config.getItemStack("inventory.offhand"));
        }

        player.updateInventory();
    }
}
