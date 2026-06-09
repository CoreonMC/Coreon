package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.util.Formats;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.Collections;

/**
 * GUI handler for module settings.
 * Builds and opens inventories to view/toggle modules.
 */
public class CoreonHandler {

    // Plugin instance for config and utilities
    private final Coreon plugin;

    public CoreonHandler(Coreon plugin) {
        this.plugin = plugin;
    }

    public Coreon getPlugin() {
        return plugin;
    }

    // Temporary inventory used while constructing GUIs
    private Inventory inv;

    /**
     * Open the main settings GUI showing all modules.
     */
    public void coreonSettings(Player player) {

        ConfigurationSection modulesSection = plugin.getConfig().getConfigurationSection("modules");
        if (modulesSection == null) {
            player.sendMessage("§cNo modules section found in config!");
            return;
        }

        // 36-slot inventory titled "Coreon Settings"
        inv = Bukkit.createInventory(null, 36, "Coreon Settings");

        // Exit button (barrier)
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        ItemStack search = new ItemStack(Material.COMPASS);
        ItemMeta searchMeta = search.getItemMeta();
        ItemStack page = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = page.getItemMeta();
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();

        if (exitMeta != null) {
            exitMeta.setDisplayName("§l§4Exit");
            exit.setItemMeta(exitMeta);
        }

        if (searchMeta != null) {
            searchMeta.setDisplayName("§l§eSearch");
            search.setItemMeta(searchMeta);
        }

        // Fill inventory and open for player
        loadSettings(inv, exit, search, page, next, previous);
        player.openInventory(inv);
    }

    /**
     * Open per-module GUI to view and toggle a single module.
     */
    public void partSettings(String key, Player player) {

        boolean value = plugin.getConfig().getBoolean("modules." + key);
        String descriptionString = plugin.getCommandDescriptions().getPath() + "." + key.toLowerCase();

        String invTitle = "§l§2" + Formats.capitalizeFirstChar(key);
        inv = Bukkit.createInventory(null, 36, invTitle);

        // Items: toggle lever, description paper, back button
        ItemStack toggle = new ItemStack(Material.LEVER);
        ItemMeta toggleMeta = toggle.getItemMeta();

        ItemStack description = new ItemStack(Material.PAPER);
        ItemMeta descriptionMeta = description.getItemMeta();

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();

        if (toggleMeta != null) {
            toggleMeta.setDisplayName("§l§bToggle " + Formats.capitalizeFirstChar(key));
            toggleMeta.setLore(Collections.singletonList("§5Activated: " + value));
            toggle.setItemMeta(toggleMeta);
        }

        if (descriptionMeta != null) {
            descriptionMeta.setDisplayName("§l§eDescription");
            descriptionMeta.setLore(Collections.singletonList("§5" + descriptionString));
            description.setItemMeta(descriptionMeta);
        }

        if (backMeta != null) {
            backMeta.setDisplayName("§l§4Back");
            back.setItemMeta(backMeta);
        }

        inv.clear();
        inv.setItem(13, toggle);
        inv.setItem(35, back);

        player.openInventory(inv);
    }

    /**
     * Open confirmation GUI for enabling/disabling a module.
     */
    public void deActiveSettings(String key, Player player) {

        boolean value = plugin.getConfig().getBoolean("modules." + key);

        String invTitle = "§l§2(De-)Activation " + Formats.capitalizeFirstChar(key);
        inv = Bukkit.createInventory(null, 36, invTitle);

        ItemStack toggle = new ItemStack(Material.LEVER);
        ItemMeta toggleMeta = toggle.getItemMeta();

        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
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
            cancelMeta.setDisplayName("§l§4Cancel");
            cancel.setItemMeta(cancelMeta);
        }

        inv.clear();
        inv.setItem(13, toggle);
        inv.setItem(21, confirm);
        inv.setItem(23, cancel);

        player.openInventory(inv);
    }

    /**
     * Toggle the module value in config and reopen its GUI.
     */
    public void changeActive(String key, Player player) {

        boolean current = plugin.getConfig().getBoolean("modules." + key);
        boolean newValue = !current;

        plugin.getConfig().set("modules." + key, newValue);
        plugin.saveConfig();

        // Live-Update: Command aktivieren oder deaktivieren ohne Restart
        plugin.applyModule(key);

        partSettings(key, player);
    }

    public void searchAnvil(Player player, String title) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.ANVIL, Component.text(title));
        if (inv instanceof AnvilInventory anvilInv) {
            anvilInv.setFirstItem(new ItemStack(Material.PAPER));
        }
        player.openInventory(inv);

        if (player.getOpenInventory() instanceof AnvilView anvilView) {
            anvilView.setRepairCost(0);
        }
    }

    /**
     * Fill inventory with one book per module and an exit button.
     */
    private void loadSettings(Inventory inv, ItemStack exit, ItemStack search, ItemStack page, ItemStack next, ItemStack previous) {

        ConfigurationSection modulesSection = plugin.getConfig().getConfigurationSection("modules");
        if (modulesSection == null) return;

        inv.clear();
        inv.setItem(35, exit);
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