package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.util.Formats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.Collections;

public class CoreonHandler {

    private final Coreon plugin;
    private final FileConfiguration config;
    private final ConfigurationSection modulesSection;
    private final File commandDescriptions;
    private static Inventory inv;


    public CoreonHandler(Coreon plugin) {
        this.plugin = plugin;

        this.config = plugin.getConfig();
        this.modulesSection = config.getConfigurationSection("modules");

        this.commandDescriptions = plugin.getCommandDescriptions();
    }

    public void coreonSettings(Player player) {

        if (modulesSection == null) {
            player.sendMessage("§cNo modules section found in config!");
            return;
        }
        inv = Bukkit.createInventory(null, 36, "Coreon Settings");

        loadSettings();

        player.openInventory(inv);
    }

    public void settings(String key, boolean value) {

        boolean newValue = !value;

        config.set("modules." + key, newValue);
        loadSettings();
        plugin.saveConfig();

        Bukkit.broadcastMessage("§aModule " + key + " set to " + newValue);
    }

    public void loadSettings(){
        inv.clear();
        for (String key : modulesSection.getKeys(false)) {

            boolean value = modulesSection.getBoolean(key);

            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta meta = book.getItemMeta();
            String displayName = "§l§2" + Formats.capitalizeFirstChar(key);


            if (meta != null) {
                meta.setDisplayName(displayName);
                meta.setLore(Collections.singletonList("§bActivated: " + value));
                book.setItemMeta(meta);
            }

            inv.addItem(book);
        }
    }
}
