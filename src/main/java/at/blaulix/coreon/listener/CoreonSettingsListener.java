package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.CoreonHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CoreonSettingsListener implements Listener {

    private final CoreonHandler handler;
    private final ConfigurationSection modulesSection;

    public CoreonSettingsListener(CoreonHandler handler, ConfigurationSection modulesSection) {
        this.handler = handler;
        this.modulesSection = modulesSection;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getView().getTitle().equals("Coreon Settings")) {

            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String displayName = clicked.getItemMeta().getDisplayName();

            String key = ChatColor.stripColor(displayName).toLowerCase();

            if (!modulesSection.contains(key)) {
                return;
            }
            boolean value = modulesSection.getBoolean(key);

            handler.settings(key, value);
        }
    }
}
