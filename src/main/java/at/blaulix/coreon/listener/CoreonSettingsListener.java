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

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        ConfigurationSection modulesSection = handler.getPlugin().getConfig().getConfigurationSection("modules");
        if (modulesSection == null) {
            return;
        }


        if (title.equals("Coreon Settings")) {

            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String key = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

            if (!modulesSection.contains(key)) {
                return;
            }

            handler.partSettings(key, player);
            return;
        }


        String settingsTitle = ChatColor.stripColor(title).toLowerCase();

        if (modulesSection.contains(settingsTitle)) {

            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            handler.deActiveSettings(settingsTitle, player);
        }

        String toggleTitle = ChatColor.stripColor(title).toLowerCase();

        if (toggleTitle.startsWith("(de-)activation")) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

            if (itemName.startsWith("confirm")) {
                String key = toggleTitle.replace("de-activation ", "");
                handler.changeActive(key, player);
            }
            if(itemName.startsWith("cancel")) {
                String key = toggleTitle.replace("de-activation ", "");
                handler.partSettings(key, player);
            }



        }
    }
}
