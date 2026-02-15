package at.blaulix.coreon.handler;

import org.bukkit.entity.Player;

public class VanishHandler {
    public void vanishPlayer(Player player) {
        player.setInvisible(true);
        player.setSilent(true);
    }
}
