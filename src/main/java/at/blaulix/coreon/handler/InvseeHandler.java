package at.blaulix.coreon.handler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class InvseeHandler {

    public void invsee(Player viewer, OfflinePlayer target) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8InvSee §7" + target.getName());

        if (target.isOnline()) {
            Player online = target.getPlayer();
            ItemStack[] contents = online.getInventory().getContents();

            for (int i = 0; i < Math.min(contents.length, 54); i++) {
                gui.setItem(i, contents[i]);
            }
        }
        else {
            File file = new File("plugins/Coreon/playerdata/" + target.getUniqueId() + ".yml");

            if (!file.exists()) {
                Inventory guiEmpty = Bukkit.createInventory(null, 54, "§8InvSee §7" + target.getName());
                viewer.openInventory(guiEmpty);
                return;
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<?> list = config.getList("inventory.content");
            if (list != null) {
                for (int i = 0; i < list.size() && i < 54; i++) {
                    gui.setItem(i, (ItemStack) list.get(i));
                }
            }
        }

        viewer.openInventory(gui);
    }
}