package at.blaulix.coreon;

import at.blaulix.coreon.command.VanishCommand;

import at.blaulix.coreon.handler.VanishHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Coreon extends JavaPlugin {
    private static Coreon instance;

    @Override
    public void onEnable() {
        instance = this;

        this.getConfig();

        Objects.requireNonNull(getCommand("vanish")).setExecutor(new VanishCommand(new VanishHandler()));

        getLogger().info("Coreon enabled on (v" + getPluginMeta().getVersion() + ")");
    }

    @Override
    public void onDisable() {
        this.saveConfig();
        getLogger().info("Coreon disabled");
    }

    public static Coreon getInstance() {
        return instance;
    }
}
