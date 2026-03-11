package at.blaulix.coreon.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class Playerdata {
    /** Save player's inventory to a YAML file under plugins/Coreon/playerdata */
    public static void savePlayerData(Player player){
        File folder = new File("plugins/Coreon/playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Write inventory to config (Bukkit can store ItemStacks directly)
        config.set("inventory.content", player.getInventory().getContents());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
