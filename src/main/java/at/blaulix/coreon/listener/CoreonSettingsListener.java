package at.blaulix.coreon.listener;

import at.blaulix.coreon.handler.CoreonHandler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CoreonSettingsListener implements Listener {

    private final CoreonHandler handler;

    public CoreonSettingsListener(CoreonHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle()).toLowerCase();

        ConfigurationSection modulesSection = handler.getPlugin().getConfig().getConfigurationSection("modules");
        if (modulesSection == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();


        if (title.equals("coreon settings")) {
            event.setCancelled(true);

            if (!modulesSection.contains(itemName)) return;

            handler.partSettings(itemName, player);
            return;
        }

        if (modulesSection.contains(title)) {
            event.setCancelled(true);
            handler.deActiveSettings(title, player);
            return;
        }


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