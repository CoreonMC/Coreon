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

    // Handler für die GUI-Logik
    private final CoreonHandler handler;

    public CoreonSettingsListener(CoreonHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        // Prüfen ob ein Spieler geklickt hat
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Titel des geöffneten Inventars (ohne Farben, klein geschrieben)
        String title = ChatColor.stripColor(event.getView().getTitle()).toLowerCase();

        // "modules" Section aus der Config holen
        ConfigurationSection modulesSection = handler.getPlugin().getConfig().getConfigurationSection("modules");
        if (modulesSection == null) return;

        // Geklicktes Item holen
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Anzeigename des Items (ohne Farben, klein geschrieben)
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();

        // Haupt-Settings GUI
        if (title.equals("coreon settings")) {
            event.setCancelled(true); // Klick blockieren

            // Prüfen ob Modul existiert
            if (!modulesSection.contains(itemName)) return;

            // Untermenü für das Modul öffnen
            handler.partSettings(itemName, player);
            return;
        }

        // Modul-Untermenü
        if (modulesSection.contains(title)) {
            event.setCancelled(true); // Klick blockieren

            // Aktivierungs/Deaktivierungs Menü öffnen
            handler.deActiveSettings(title, player);
            return;
        }

        // Bestätigungsmenü für (De-)Aktivierung
        if (title.startsWith("(de-)activation ")) {
            event.setCancelled(true);

            // Modul-Name aus Titel extrahieren
            String key = title.substring("(de-)activation ".length());

            // Aktivierung bestätigen
            if (itemName.startsWith("confirm")) {
                handler.changeActive(key, player);
            }

            // Zurück zum Modulmenü
            if (itemName.startsWith("cancel")) {
                handler.partSettings(key, player);
            }
        }
    }
}