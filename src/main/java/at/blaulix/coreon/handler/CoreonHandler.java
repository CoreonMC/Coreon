package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.util.Formats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class CoreonHandler {

    private final Coreon plugin;

    public CoreonHandler(Coreon plugin) {
        this.plugin = plugin;
    }

    public Coreon getPlugin() {
        return plugin;
    }

    public void coreonSettings(Player player) {

        ConfigurationSection modulesSection = plugin.getConfig().getConfigurationSection("modules");
        if (modulesSection == null) {
            player.sendMessage("§cNo modules section found in config!");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 36, "Coreon Settings");

        loadSettings(inv);

        player.openInventory(inv);
    }

    public void partSettings(String key, Player player) {

        boolean value = plugin.getConfig().getBoolean("modules." + key);

        String invTitle = "§l§2" + Formats.capitalizeFirstChar(key);
        Inventory settingsInv = Bukkit.createInventory(null, 36, invTitle);

        ItemStack toggle = new ItemStack(Material.LEVER);
        ItemMeta toggleMeta = toggle.getItemMeta();

        if (toggleMeta != null) {
            toggleMeta.setDisplayName("§l§bToggle " + Formats.capitalizeFirstChar(key));
            toggleMeta.setLore(Collections.singletonList("§5Activated: " + value));
            toggle.setItemMeta(toggleMeta);
        }

        settingsInv.setItem(13, toggle);

        player.openInventory(settingsInv);
    }

    public void deActiveSettings(String key, Player player) {

        boolean value = plugin.getConfig().getBoolean("modules." + key);

        String invTitle = "§l§2(De-)Activation " + Formats.capitalizeFirstChar(key);
        Inventory deActivationInv = Bukkit.createInventory(null, 36, invTitle);

        ItemStack toggle = new ItemStack(Material.LEVER);
        ItemMeta toggleMeta = toggle.getItemMeta();

        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();

        if (toggleMeta != null) {
            toggleMeta.setDisplayName("§l§bToggle " + Formats.capitalizeFirstChar(key));
            toggleMeta.setLore(Collections.singletonList("§5Activated: " + value));
            toggle.setItemMeta(toggleMeta);
        }
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§l§aConfirm");
            confirmMeta.setLore(Collections.singletonList("§5Click to turn " + Formats.capitalizeFirstChar(key) + " " + (!value)));
            confirm.setItemMeta(confirmMeta);
        }
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§l§cCancel");
            cancel.setItemMeta(cancelMeta);
        }

        deActivationInv.setItem(13, toggle);
        deActivationInv.setItem(21, confirm);
        deActivationInv.setItem(23, cancel);


        player.openInventory(deActivationInv);
    }

    public void changeActive(String key, Player player) {

        boolean current = plugin.getConfig().getBoolean("modules." + key);
        boolean newValue = !current;

        plugin.getConfig().set("modules." + key, newValue);
        plugin.saveConfig();

        partSettings(key, player);
    }

    private void loadSettings(Inventory inv) {

        ConfigurationSection modulesSection = plugin.getConfig().getConfigurationSection("modules");
        if (modulesSection == null) return;

        for (String key : modulesSection.getKeys(false)) {

            boolean value = modulesSection.getBoolean(key);

            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta meta = book.getItemMeta();

            if (meta != null) {
                meta.setDisplayName("§l§2" + Formats.capitalizeFirstChar(key));
                meta.setLore(Collections.singletonList("§bActivated: " + value));
                book.setItemMeta(meta);
            }

            inv.addItem(book);
        }
    }
}
