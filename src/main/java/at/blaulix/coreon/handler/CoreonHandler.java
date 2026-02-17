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

public class CoreonHandler {

    private final Coreon plugin; // <-- ADDED (final + sauber gekapselt)

    private final FileConfiguration config; // <-- ADDED (nicht mehr oben initialisiert)
    private final ConfigurationSection modulesSection; // <-- ADDED (nicht mehr oben initialisiert)

    private final Inventory coreonSettingsP1; // <-- ADDED (jetzt im Konstruktor erstellt)

    public CoreonHandler(Coreon plugin) {
        this.plugin = plugin; // <-- ADDED (Plugin korrekt setzen)

        this.config = plugin.getConfig(); // <-- MOVED (war vorher oben → verursachte NPE)
        this.modulesSection = config.getConfigurationSection("modules"); // <-- MOVED

        this.coreonSettingsP1 = Bukkit.createInventory(null, 36, "Coreon Settings"); // <-- MOVED
    }

    public void coreonSettings(Player player) {

        if (modulesSection == null) { // <-- ADDED (Null-Schutz)
            player.sendMessage("§cNo modules section found in config!");
            return;
        }

        for (String key : modulesSection.getKeys(false)) {

            boolean value = modulesSection.getBoolean(key);

            ItemStack book = new ItemStack(Material.BOOK); // <-- MOVED (war außerhalb der Schleife)
            ItemMeta meta = book.getItemMeta();

            if (meta != null) { // <-- ADDED (Null-Schutz)
                meta.setDisplayName(key);
                meta.setLore(Collections.singletonList("Activated: " + value));
                book.setItemMeta(meta);
            }

            coreonSettingsP1.addItem(book);
        }

        player.openInventory(coreonSettingsP1);
    }
}
