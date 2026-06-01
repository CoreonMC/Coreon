package at.blaulix.coreon.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Playerdata {

    /**
     * Save player's full inventory (content, armor, offhand) to a YAML file
     * under plugins/Coreon/playerdata/<uuid>.yml
     */
    public static void savePlayerData(Player player) {
        File folder = new File("plugins/Coreon/playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Main storage (36 slots)
        config.set("inventory.content", Arrays.asList(player.getInventory().getStorageContents()));

        // Armor (4 slots: helmet, chestplate, leggings, boots)
        config.set("inventory.armor", Arrays.asList(player.getInventory().getArmorContents()));

        // Offhand
        config.set("inventory.offhand", player.getInventory().getItemInOffHand());

        // Save player name for lookup
        config.set("name", player.getName());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save modified offline inventory data back to the YAML file.
     * Called by InvseeListener when a viewer edits an offline player's inventory.
     *
     * @param uuid    UUID of the offline player (as String)
     * @param content Main storage contents (slots 0-35)
     * @param armor   Armor contents [helmet, chestplate, leggings, boots]
     * @param offhand Offhand item
     */
    public static void saveOfflineInventory(String uuid, ItemStack[] content, ItemStack[] armor, ItemStack offhand) {
        File folder = new File("plugins/Coreon/playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, uuid + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("inventory.content", Arrays.asList(content));
        config.set("inventory.armor", Arrays.asList(armor));
        config.set("inventory.offhand", offhand);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
