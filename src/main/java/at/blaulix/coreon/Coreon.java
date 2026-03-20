package at.blaulix.coreon;

import at.blaulix.coreon.command.*;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.CoreonHandler;
import at.blaulix.coreon.handler.PvPTimerHandler;
import at.blaulix.coreon.handler.VanishHandler;
import at.blaulix.coreon.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class Coreon extends JavaPlugin {
    private final File commandDescriptions = new File(getDataFolder(), "command_descriptions.yml");

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Handlers initialisieren
        CoreonHandler coreonHandler = new CoreonHandler(this);
        PvPTimerHandler pvpTimerHandler = new PvPTimerHandler(this);
        VanishHandler vanishHandler = new VanishHandler(this); // Zentraler Handler

        // 2. Datenbank
        HomeDatabase homesHomeDatabase = new HomeDatabase(this, "homes.db");
        homesHomeDatabase.enableDatabase();

        // 3. Listener registrieren (Wichtig: vanishHandler übergeben!)
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);
        getServer().getPluginManager().registerEvents(new InvseeListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPTimerListener(pvpTimerHandler), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this, vanishHandler), this);

        // 4. Commands registrieren
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
        Objects.requireNonNull(getCommand("invsee")).setExecutor(new InvseeCommand());
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomesCommand(this, homesHomeDatabase));
        Objects.requireNonNull(getCommand("pvptimer")).setExecutor(new PvPTimerCommand(pvpTimerHandler));

        // WICHTIG: Hier muss der zentrale vanishHandler rein, kein "new VanishHandler(this)"!
        Objects.requireNonNull(getCommand("vanish")).setExecutor(new VanishCommand(vanishHandler));
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