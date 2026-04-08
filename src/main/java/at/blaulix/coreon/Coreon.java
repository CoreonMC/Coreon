package at.blaulix.coreon;

import at.blaulix.coreon.command.CoreonCommand;
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

    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Handlers initialisieren
        CoreonHandler coreonHandler = new CoreonHandler(this);
        PvPTimerHandler pvpTimerHandler = new PvPTimerHandler(this);
        VanishHandler vanishHandler = new VanishHandler(this);

        // 2. Datenbank
        HomeDatabase homesHomeDatabase = new HomeDatabase(this, "homes.db");
        homesHomeDatabase.enableDatabase();

        // 3. ModuleManager erstellen und initial anwenden (setzt Commands auf aktiv/deaktiviert)
        moduleManager = new ModuleManager(this, pvpTimerHandler, vanishHandler, homesHomeDatabase);
        moduleManager.applyAll();

        // 4. Listener registrieren
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);
        getServer().getPluginManager().registerEvents(new InvseeListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPTimerListener(pvpTimerHandler), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this, vanishHandler), this);

        // 5. Coreon-Command registrieren (immer aktiv, kein Modul)
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
    }

    @Override
    public void onDisable() {
        HomeDatabase.getAll().forEach(HomeDatabase::disconnect);
    }

    /** Wird von CoreonHandler nach einem Modul-Toggle aufgerufen, um Commands live zu updaten. */
    public void applyModule(String key) {
        if (moduleManager != null) {
            moduleManager.apply(key);
        }
    }

    public File getCommandDescriptions() {
        return commandDescriptions;
    }

    public File getHomesConfig() {
        return new File(getDataFolder(), "homes.yml");
    }
}