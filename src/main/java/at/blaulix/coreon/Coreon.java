package at.blaulix.coreon;

import at.blaulix.coreon.command.CoreonCommand;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.CoreonHandler;
import at.blaulix.coreon.handler.PvPTimerHandler;
import at.blaulix.coreon.handler.VanishHandler;
import at.blaulix.coreon.listener.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Objects;

public final class Coreon extends JavaPlugin {

    private static Coreon instance;

    private final File commandDescriptions = new File(getDataFolder(), "command_descriptions.yml");

    private ModuleManager moduleManager;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // messages.yml erstellen/laden
        saveResource("messages.yml", false);

        File messagesFile = new File(getDataFolder(), "messages.yml");

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Handler
        CoreonHandler coreonHandler = new CoreonHandler(this);

        PvPTimerHandler pvpTimerHandler = new PvPTimerHandler(this);

        VanishHandler vanishHandler = new VanishHandler(this);

        // Datenbank
        HomeDatabase homesDatabase = new HomeDatabase(this, "homes.db");

        homesDatabase.enableDatabase();

        // Module laden
        moduleManager = new ModuleManager(this, pvpTimerHandler, vanishHandler, homesDatabase);

        moduleManager.applyAll();

        // Listener
        getServer().getPluginManager().registerEvents(new QuitListener(), this);

        getServer().getPluginManager().registerEvents(new JoinListener(instance), this);

        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);

        getServer().getPluginManager().registerEvents(new InvseeListener(this), this);

        getServer().getPluginManager().registerEvents(new PvPTimerListener(pvpTimerHandler), this);

        getServer().getPluginManager().registerEvents(new VanishListener(this, vanishHandler), this);

        getServer().getPluginManager().registerEvents(new EcseeListener(this), this);

        // Command
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
    }

    @Override
    public void onDisable() {
        HomeDatabase.getAll().forEach(HomeDatabase::disconnect);
    }

    public static Coreon getInstance() {
        return instance;
    }

    public FileConfiguration getMessages() {
        return messagesConfig;
    }

    public void applyModule(String key) {
        if (moduleManager != null) {
            moduleManager.apply(key);
        }
    }

    public void loadPremadeConfig(String premadePath, String name, String description) {

        File configFile = new File(getDataFolder(), "config.yml");

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);

        if (yaml.contains("config." + name)) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getResource("premade-module-configs/" + premadePath))))) {

            StringBuilder content = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {

                content.append(line).append("\n");
            }

            YamlConfiguration premade = new YamlConfiguration();

            premade.loadFromString(content.toString());

            yaml.set("config." + name, premade.getValues(false));

            yaml.save(configFile);

            getLogger().info("Loaded premade config: " + name);

        } catch (Exception e) {

            getLogger().warning("Could not write premade config '" + premadePath + "': " + e.getMessage());
        }

        reloadConfig();
    }

    public File getCommandDescriptions() {
        return commandDescriptions;
    }
}