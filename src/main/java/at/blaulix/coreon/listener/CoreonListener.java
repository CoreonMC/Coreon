package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.CoreonHandler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CoreonListener implements Listener {

    // Handles GUI logic and module actions
    private final CoreonHandler handler;

    public CoreonListener(CoreonHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        // Ensure the clicker is a player
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Get inventory title (remove colors, lowercase for comparison)
        String title = ChatColor.stripColor(event.getView().getTitle()).toLowerCase();

        // Get "modules" section from config
        ConfigurationSection modulesSection = handler.getPlugin().getConfig().getConfigurationSection("modules");
        if (modulesSection == null) return;

        // Get clicked item
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Get item display name (remove colors, lowercase)
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        // Main settings GUI
        if (title.equals("coreon settings")) {
            event.setCancelled(true); // Prevent item movement

            // Check if module exists in config
            if (!modulesSection.contains(itemName)) return;

            // Open module submenu
            handler.partSettings(itemName, player);
            return;
        }

        // Module submenu
        if (modulesSection.contains(title)) {
            event.setCancelled(true); // Prevent item movement

            // Open activation/deactivation menu
            handler.deActiveSettings(title, player);
            return;
        }

        // Confirmation menu for (de-)activation
        if (title.startsWith("(de-)activation ")) {
            event.setCancelled(true);

            // Extract module key from title
            String key = title.substring("(de-)activation ".length());

            // Confirm activation toggle
            if (itemName.startsWith("confirm")) {
                handler.changeActive(key, player);
            }

            // Return to module menu
            if (itemName.startsWith("cancel")) {
                handler.partSettings(key, player);
            }
        }
    }
}