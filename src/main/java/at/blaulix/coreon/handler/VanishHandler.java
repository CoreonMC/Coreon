package at.blaulix.coreon.handler;

import org.bukkit.Color;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class VanishHandler {
    public void vanishPlayer(Player player) {
        Player allPlayer = getServer().getPlayer(player.getUniqueId());
        player.setInvisible(true);
        player.setSilent(true);
        allPlayer.sendMessage(Color.YELLOW + "" + player + " left the game.");
    }
}
