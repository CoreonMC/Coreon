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

import java.io.*;
import java.util.Objects;

/**
 * Hauptklasse des Plugins. Einstiegspunkt für das Bukkit/Spigot-Plugin.
 *
 * <p>Initialisiert Handler, Datenbanken, Module, Listener und Commands beim Aktivieren
 * und führt Cleanup beim Deaktivieren durch.</p>
 */
public final class Coreon extends JavaPlugin {

    private static Coreon instance;

    private final File commandDescriptions = new File(getDataFolder(), "command_descriptions.yml");
    private ModuleManager moduleManager;
    private FileConfiguration homesConfig;
    private FileConfiguration messages;

    /**
     * Liefert die Singleton-Instanz des Plugins.
     *
     * @return aktuelle Plugin-Instanz oder {@code null}, wenn nicht geladen
     */
    public static Coreon getInstance() {
        return instance;
    }

    /**
     * Liefert die geladene messages.yml-Konfiguration.
     *
     * @return FileConfiguration mit den Nachrichentexten
     */
    public FileConfiguration getMessages() {
        return messages;
    }

    /**
     * Wird beim Aktivieren (Enable) des Plugins aufgerufen.
     * Initialisiert Handler, Datenbank, Module, Listener und Commands.
     */
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Load messages.yml
        File messagesFile = new File(getDataFolder(), "message.yml");
        if (!messagesFile.exists()) saveResource("message.yml", false);
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // 1. Handlers initialisieren
        CoreonHandler coreonHandler = new CoreonHandler(this);
        PvPTimerHandler pvpTimerHandler = new PvPTimerHandler(this);
        VanishHandler vanishHandler = new VanishHandler(this);

        // 2. Datenbank
        HomeDatabase homesHomeDatabase = new HomeDatabase(this, "homes.db");
        homesHomeDatabase.enableDatabase();

        // 3. ModuleManager erstellen — lädt Premade-Configs und aktiviert Commands
        moduleManager = new ModuleManager(this, pvpTimerHandler, vanishHandler, homesHomeDatabase);
        moduleManager.applyAll();

        // 4. Listener registrieren
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);
        getServer().getPluginManager().registerEvents(new InvseeListener(this), this);
        getServer().getPluginManager().registerEvents(new PvPTimerListener(pvpTimerHandler), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this, vanishHandler), this);
        getServer().getPluginManager().registerEvents(new EcseeListener(this), this);

        // 5. Coreon-Command registrieren (immer aktiv, kein Modul)
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
    }

    /**
     * Wird beim Deaktivieren (Disable) des Plugins aufgerufen und führt Cleanup aus.
     */
    @Override
    public void onDisable() {
        instance = null;
        HomeDatabase.getAll().forEach(HomeDatabase::disconnect);
    }

    /**
     * Aktualisiert die Commands eines einzelnen Moduls zur Laufzeit nach einem Toggle.
     *
     * @param key Schlüssel des Moduls, das neu angewendet werden soll
     */
    public void applyModule(String key) {
        if (moduleManager != null) {
            moduleManager.apply(key);
        }
    }

    /**
     * Schreibt eine Premade-Config einmalig in den config:-Block der config.yml.
     * Format:
     *   config:
     *     #-----description-----#
     *     name:
     *       <Inhalt der premadePath-Datei>
     *     #-----description-----#
     */
    public void loadPremadeConfig(String premadePath, String name, String description) {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
        }

        // Prüfen ob der Key bereits existiert → nur einmal schreiben
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
        if (yaml.contains("config." + name)) {
            homesConfig = yaml;
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(configFile, true));
             BufferedReader br = new BufferedReader(new InputStreamReader(
                     Objects.requireNonNull(getResource("premade-module-configs/" + premadePath))))) {

            bw.newLine();
            bw.write("  #-----" + description + "-----#");
            bw.newLine();
            bw.write("  " + name + ":");
            bw.newLine();

            String line;
            while ((line = br.readLine()) != null) {
                bw.write("    " + line);
                bw.newLine();
            }

            bw.write("  #-----" + description + "-----#");
            bw.newLine();

        } catch (IOException e) {
            getLogger().warning("Could not write premade config '" + premadePath + "': " + e.getMessage());
        }

        homesConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Gibt den config.homes-Block als ConfigurationSection zurück.
     * HomesHandler kann weiterhin getString("messages.home-set") etc. aufrufen.
     */
    public org.bukkit.configuration.ConfigurationSection getHomesConfig() {
        if (homesConfig == null) return null;
        org.bukkit.configuration.ConfigurationSection section = homesConfig.getConfigurationSection("config.homes");
        return section != null ? section : homesConfig;
    }

    /**
     * Liefert die Datei mit Beschreibungen für Commands im Datenverzeichnis des Plugins.
     *
     * @return File-Objekt zur command_descriptions.yml
     */
    public File getCommandDescriptions() {
        return commandDescriptions;
    }
}