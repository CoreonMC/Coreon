package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CoreonHandler {
    Coreon plugin;

    FileConfiguration config = plugin.getConfig();
    ConfigurationSection functionsSection = config.getConfigurationSection("functions");



    Inventory coreonSettingsP1 = Bukkit.createInventory(null, 36, "Coreon Settings");



    public void coreonSettings(Player player) {
        ItemStack book = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = book.getItemMeta();

        Map<String, Boolean> functionsMap = new HashMap<>();
        for (String key : functionsSection.getKeys(false)) {
            boolean value = functionsSection.getBoolean(key);
            functionsMap.put(key, value);

            meta.setDisplayName(key);
            meta.setLore(Collections.singletonList("Activated:" + value));
            book.setItemMeta(meta);

            coreonSettingsP1.addItem(book);
        }

        player.openInventory(coreonSettingsP1);
    }
}
