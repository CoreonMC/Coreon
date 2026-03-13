package at.blaulix.coreon;

import at.blaulix.coreon.command.CoreonCommand;
import at.blaulix.coreon.command.InvseeCommand;
import at.blaulix.coreon.command.PvPTimerCommand;
import at.blaulix.coreon.command.VanishCommand;
import at.blaulix.coreon.handler.CoreonHandler;
import at.blaulix.coreon.handler.Database;
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

        // Handlers
        CoreonHandler coreonHandler = new CoreonHandler(this);
        PvPTimerHandler pvpTimerHandler = new PvPTimerHandler(this); // New Handler for PvP Timer

        // Databases
        Database homesDatabase = new Database(this, "homes.db");
        homesDatabase.enableDatabase();

        // Listeners
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);
        getServer().getPluginManager().registerEvents(new InvseeListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPTimerListener(pvpTimerHandler), this); // New Listener for PvP Timer

        // Commands
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
        Objects.requireNonNull(getCommand("invsee")).setExecutor(new InvseeCommand());
        Objects.requireNonNull(getCommand("vanish")).setExecutor(new VanishCommand());
        Objects.requireNonNull(getCommand("pvptimer")).setExecutor(new PvPTimerCommand(pvpTimerHandler)); // New Command for PvP Timer

    }

    @Override
    public void onDisable() {
        Database.getAll().forEach(Database::disconnect);
    }

    public File getCommandDescriptions() {
        return commandDescriptions;
    }

    public File getHomesConfig() {
        return new File(getDataFolder(), "homes.yml");
    }
}