package at.blaulix.coreon;

import at.blaulix.coreon.command.*;
import at.blaulix.coreon.handler.CoreonHandler;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.PvPTimerHandler;
import at.blaulix.coreon.listener.CoreonListener;
import at.blaulix.coreon.listener.InvseeListener;
import at.blaulix.coreon.listener.PvPTimerListener;
import at.blaulix.coreon.listener.QuitListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class Coreon extends JavaPlugin {
    private final File commandDescriptions = new File(getDataFolder(), "command_descriptions.yml");

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Handlers for GUI, PvP timer, etc.
        CoreonHandler coreonHandler = new CoreonHandler(this);
        PvPTimerHandler pvpTimerHandler = new PvPTimerHandler(this); // Handler for PvP timer

        // Initialize home database
        HomeDatabase homesHomeDatabase = new HomeDatabase(this, "homes.db");
        homesHomeDatabase.enableDatabase();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);
        getServer().getPluginManager().registerEvents(new InvseeListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPTimerListener(pvpTimerHandler), this); // Listener for PvP timer

        // Register commands and their executors
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
        Objects.requireNonNull(getCommand("invsee")).setExecutor(new InvseeCommand());
        Objects.requireNonNull(getCommand("vanish")).setExecutor(new VanishCommand(this));
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomesCommand(this, homesHomeDatabase));
        Objects.requireNonNull(getCommand("pvptimer")).setExecutor(new PvPTimerCommand(pvpTimerHandler)); // Command for PvP timer

    }

    @Override
    public void onDisable() {
        HomeDatabase.getAll().forEach(HomeDatabase::disconnect);
    }

    public File getCommandDescriptions() {
        return commandDescriptions;
    }

    public File getHomesConfig() {
        return new File(getDataFolder(), "homes.yml");
    }
}