package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.CoreonHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CoreonListener implements Listener {

    private final CoreonHandler handler;

    public CoreonListener(CoreonHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        Inventory inv = event.getInventory();

        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle()).toLowerCase();

        ConfigurationSection modulesSection = handler.getPlugin().getConfig().getConfigurationSection("modules");

        if (modulesSection == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        // MAIN GUI
        if (title.equals("coreon settings")) {

            event.setCancelled(true);

            if (itemName.equals("exit")) {
                player.closeInventory();
                return;
            }

            if (itemName.equals("search")) {
                handler.searchAnvil(player, "Search for Modules");
                return;
            }

            if (!modulesSection.contains(itemName)) return;

            handler.partSettings(itemName, player);
            return;
        }

        // SEARCH GUI
        if (title.equalsIgnoreCase("search for modules")) {

            event.setCancelled(true);

            // Nur rechter Slot
            if (event.getRawSlot() != 2) return;

            if (!(event.getView().getTopInventory() instanceof AnvilInventory anvil)) {
                return;
            }

            String searchText = "";

            ItemStack result = anvil.getItem(2);

            if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {

                searchText = ChatColor.stripColor(result.getItemMeta().getDisplayName());
            }

            final String finalSearchText = searchText.toLowerCase().trim();

            player.closeInventory();

            Bukkit.getScheduler().runTask(handler.getPlugin(), () -> handler.openFilteredSettings(player, finalSearchText));

            return;

        }

        // MODULE MENU
        if (modulesSection.contains(title)) {

            event.setCancelled(true);

            if (itemName.equals("back")) {
                handler.coreonSettings(player);
                return;
            }

            handler.deActiveSettings(title, player);
            return;
        }

        // ACTIVATE / DEACTIVATE
        if (title.startsWith("(de-)activation ")) {

            event.setCancelled(true);

            String key = title.substring("(de-)activation ".length());

            if (itemName.startsWith("confirm")) {
                handler.changeActive(key, player);
            }

            if (itemName.startsWith("cancel")) {
                handler.partSettings(key, player);
            }
        }
    }
}
