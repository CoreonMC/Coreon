package at.blaulix.coreon.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;

public class QuitListener implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Datei erstellen: /plugins/Coreon/playerdata/UUID.yml
        File folder = new File("plugins/Coreon/playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Inventar in die Config schreiben (Bukkit kann ItemStacks direkt speichern!)
        config.set("inventory.content", player.getInventory().getContents());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
