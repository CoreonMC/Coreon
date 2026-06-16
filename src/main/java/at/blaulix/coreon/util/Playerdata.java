package at.blaulix.coreon.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Hilfsklasse zum Speichern und Wiederherstellen von Spieler-Inventory- und Enderchest-Daten
 * in YAML-Dateien unter plugins/Coreon/playerdata.
 */
public class Playerdata {

    /**
     * Save player's full inventory (content, armor, offhand) AND ender chest
     * to a YAML file under plugins/Coreon/playerdata/<uuid>.yml
     */
    /**
     * Speichert das vollständige Inventar (inkl. Rüstung und Offhand) und EnderChest eines Spielers
     * in eine YAML-Datei unter plugins/Coreon/playerdata/{uuid}.yml.
     *
     * @param player Spieler dessen Daten gespeichert werden sollen
     */
    public static void savePlayerData(Player player) {
        File folder = new File("plugins/Coreon/playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("inventory.content", Arrays.asList(player.getInventory().getStorageContents()));
        config.set("inventory.armor", Arrays.asList(player.getInventory().getArmorContents()));
        config.set("inventory.offhand", player.getInventory().getItemInOffHand());
        config.set("enderchest.content", Arrays.asList(player.getEnderChest().getContents()));
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
     */
    /**
     * Schreibt Änderungen an einem Offline-Spieler-Inventar zurück in die YAML-Datei.
     * Wird z.B. von InvseeListener aufgerufen, wenn ein Betrachter das Inventar bearbeitet.
     *
     * @param uuid    UUID des betroffenen Spielers (als String)
     * @param content Inhalte des Hauptinventars
     * @param armor   Inhalte der Rüstungsslots
     * @param offhand Offhand-Item
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

    /**
     * Save modified offline ender chest data back to the YAML file.
     * Called by EcseeListener when a viewer edits a player's ender chest.
     */
    /**
     * Schreibt Änderungen am EnderChest eines Offline-Spielers zurück in die YAML-Datei.
     * Wird z.B. von EcseeListener aufgerufen, wenn ein Betrachter das EnderChest bearbeitet.
     *
     * @param uuid     UUID des betroffenen Spielers (als String)
     * @param contents Inhalte des EnderChests
     */
    public static void saveOfflineEnderChest(String uuid, ItemStack[] contents) {
        File folder = new File("plugins/Coreon/playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, uuid + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("enderchest.content", Arrays.asList(contents));

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
