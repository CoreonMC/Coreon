package at.blaulix.coreon.handler;

import at.blaulix.coreon.Coreon;
import at.blaulix.coreon.util.ConfigGetter;
import at.blaulix.coreon.database.HomeDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class HomesHandler {

    private final JavaPlugin javaPlugin;
    private final HomeDatabase homeDatabase;

    public HomesHandler(Coreon plugin, HomeDatabase homeDatabase) {

        this.javaPlugin = plugin;
        this.homeDatabase = homeDatabase;
    }

    /**
     * Set a home asynchronously
     */
    public void setHome(Player player, String homeName) {

        int homeLimit = ConfigGetter.getInt("config.homes.max-homes");

        if (getHomeCount(player) >= homeLimit) {

            String raw = ConfigGetter.getMessage("messages.max-homes");

            if (raw == null) {
                raw = "&cYou have reached " + "the maximum number " + "of homes (%limit%)!";
            }

            String msg = ChatColor.translateAlternateColorCodes('&', raw.replace("%limit%", String.valueOf(homeLimit)));

            player.sendActionBar(Component.text(msg, NamedTextColor.RED));
            return;
        }

        Location location = player.getLocation();

        String raw = ConfigGetter.getMessage("messages.home-set");

        if (raw == null) {
            raw = "&aHome &b%home%" + "&a has been set!";
        }

        String message = ChatColor.translateAlternateColorCodes('&', raw.replace("%home%", homeName));

        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {

            homeDatabase.saveHome(player.getUniqueId(), homeName, location);

            Bukkit.getScheduler().runTask(javaPlugin, () -> player.sendMessage(message));
        });
    }

    /**
     * Delete a home
     */
    public void deleteHome(Player player, String homeName) {

        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {

            homeDatabase.deleteHome(player.getUniqueId(), homeName);

            String raw = ConfigGetter.getMessage("messages.home-deleted");

            if (raw == null) {
                raw = "&aHome &b%home%" + "&a has been deleted!";
            }

            String message = ChatColor.translateAlternateColorCodes('&', raw.replace("%home%", homeName));

            Bukkit.getScheduler().runTask(javaPlugin, () -> player.sendActionBar(Component.text(message, NamedTextColor.AQUA)));
        });
    }

    /**
     * Get amount of homes
     */
    public int getHomeCount(Player player) {

        return homeDatabase.getHomeCount(player.getUniqueId());
    }

    /**
     * Teleport player to home
     */
    public void teleportToHome(Player player, String homeName) {

        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {

            Location location = homeDatabase.getHome(player.getUniqueId(), homeName);

            String tpRaw = ConfigGetter.getMessage("messages.home-teleported");
            if (tpRaw == null) {
                tpRaw = "&aTeleported to &b%home%&a!";
            }

            String tpMessage = ChatColor.translateAlternateColorCodes('&',
                    tpRaw.replace("%home%", homeName));

            String notExistRaw = ConfigGetter.getMessage("messages.home-not-exist");
            if (notExistRaw == null) {
                notExistRaw = "&cNo home named &b%home%&c found!";
            }

            String notExistMessage = ChatColor.translateAlternateColorCodes('&',
                    notExistRaw.replace("%home%", homeName));

            Bukkit.getScheduler().runTask(javaPlugin, () -> {

                if (location == null) {
                    player.sendMessage(notExistMessage);
                    return;
                }

                int delay = ConfigGetter.getInt("config.homes.teleport.delay-seconds");

                // Bypass delay -> sofort teleportieren
                if (player.hasPermission("coreon.homes.teleport.bypass-delay")) {
                    player.teleport(location);
                    player.sendActionBar(Component.text(tpMessage, NamedTextColor.GREEN));
                    return;
                }

                Location startLocation = player.getLocation().clone();

                new BukkitRunnable() {

                    int timeLeft = delay;

                    @Override
                    public void run() {

                        // Spieler offline?
                        if (!player.isOnline()) {
                            cancel();
                            return;
                        }

                        // Bewegung prüfen
                        if (!player.hasPermission("coreon.homes.teleport.bypass-move")) {

                            Location current = player.getLocation();

                            boolean moved =
                                    current.getBlockX() != startLocation.getBlockX()
                                            || current.getBlockY() != startLocation.getBlockY()
                                            || current.getBlockZ() != startLocation.getBlockZ();

                            if (moved) {
                                player.sendActionBar(Component.text("Teleport cancelled because you moved.", NamedTextColor.DARK_RED));
                                cancel();
                                return;
                            }
                        }

                        // Countdown
                        if (timeLeft > 0) {
                            player.sendActionBar(Component.text("Teleporting in " + timeLeft + " seconds...", NamedTextColor.YELLOW));
                            timeLeft--;
                            return;
                        }

                        // Teleport
                        player.teleport(location);
                        player.sendMessage(tpMessage);
                        cancel();
                    }

                }.runTaskTimer(javaPlugin, 0L, 20L);
            });
        });
    }

    /**
     * List all homes
     */
    public void listHomes(Player player) {

        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(javaPlugin, () -> {

            List<String> homes = homeDatabase.getHomesList(uuid);

            String raw = ConfigGetter.getMessage("messages.home-list");

            if (raw == null) {
                raw = "&aYour homes: " + "&b%homes%";
            }

            String message = ChatColor.translateAlternateColorCodes('&', raw.replace("%homes%", homes.isEmpty() ? "none" : String.join(", ", homes)));

            Bukkit.getScheduler().runTask(javaPlugin, () -> player.sendMessage(message));
        });
    }

    /**
     * Show help
     */
    public void helpHomes(Player player) {

        String raw = ConfigGetter.getMessage("messages.home-help");

        if (raw == null) {

            raw = "&aHome Help:\n" + "&b/home set <name> " + "&7- Set a home\n" +

                    "&b/home delete <name> " + "&7- Delete a home\n" +

                    "&b/home list " + "&7- Show homes\n" +

                    "&b/home <name> " + "&7- Teleport";
        }

        String message = ChatColor.translateAlternateColorCodes('&', raw);

        player.sendMessage(message);
    }
}
