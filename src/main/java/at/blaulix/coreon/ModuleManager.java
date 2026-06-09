package at.blaulix.coreon;

import at.blaulix.coreon.command.*;
import at.blaulix.coreon.command.homes.DeleteHomeCommand;
import at.blaulix.coreon.command.homes.ListHomesCommand;
import at.blaulix.coreon.command.homes.SetHomeCommand;
import at.blaulix.coreon.command.homes.TpHomeCommand;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ModuleManager {

    private final Coreon plugin;

    // Stores the real executors per command name
    private final Map<String, CommandExecutor> realExecutors = new TreeMap<>();

    /**
     * Defines per module:
     *   - commands:     which plugin.yml commands to enable/disable
     *   - premadePath:  filename in premade-module-configs/ (null = no config)
     *   - configName:   key written under config: in config.yml
     *   - description:  label shown as comment in config.yml
     */
    private record ModuleDefinition(String[] commands, String premadePath, String configName, String description) {}

    private static final Map<String, ModuleDefinition> MODULES = new HashMap<>();
    static {
        MODULES.put("invsee",   new ModuleDefinition(new String[]{"invsee"},                                   null,         null,    null));
        MODULES.put("vanish",   new ModuleDefinition(new String[]{"vanish"},                                   null,         null,    null));
        MODULES.put("pvptimer", new ModuleDefinition(new String[]{"pvptimer"},                                 null,         null,    null));
        MODULES.put("homes",    new ModuleDefinition(new String[]{"home", "homes", "sethome", "deletehome"},   "homes.yml",  "homes", "Home System"));
        MODULES.put("ec",       new ModuleDefinition(new String[]{"ec"},                                       null,         null,    null));
        MODULES.put("ecsee",    new ModuleDefinition(new String[]{"ecsee"},                                    null,         null,    null));
    }

    private static final CommandExecutor DISABLED_EXECUTOR = new CommandExecutor() {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, @NotNull String[] args) {
            sender.sendMessage("§cThis module is currently disabled.");
            return true;
        }
    };

    public ModuleManager(Coreon plugin,
                         PvPTimerHandler pvpTimerHandler,
                         VanishHandler vanishHandler,
                         HomeDatabase homeDatabase) {
        this.plugin = plugin;

        realExecutors.put("invsee",     new InvseeCommand());
        realExecutors.put("vanish",     new VanishCommand(vanishHandler));
        realExecutors.put("pvptimer",   new PvPTimerCommand(pvpTimerHandler));
        realExecutors.put("home",       new TpHomeCommand(plugin, homeDatabase));
        realExecutors.put("homes",      new ListHomesCommand(plugin, homeDatabase));
        realExecutors.put("sethome",    new SetHomeCommand(plugin, homeDatabase));
        realExecutors.put("deletehome", new DeleteHomeCommand(plugin, homeDatabase));
        realExecutors.put("ec",         new EnderChestCommand());
        realExecutors.put("ecsee",      new EcseeCommand());
    }

    /** Alle Module einmalig beim Start anwenden. */
    public void applyAll() {
        for (String key : MODULES.keySet()) {
            apply(key);
        }
    }

    /** Ein einzelnes Modul anwenden (z.B. nach Live-Toggle). */
    public void apply(String key) {
        ModuleDefinition def = MODULES.get(key.toLowerCase());
        if (def == null) return;

        boolean enabled = plugin.getConfig().getBoolean("modules." + key, false);

        if (enabled) {
            // Premade-Config einmalig in config.yml schreiben, wenn vorhanden
            if (def.premadePath() != null) {
                plugin.loadPremadeConfig(def.premadePath(), def.configName(), def.description());
            }
            for (String cmdName : def.commands()) {
                CommandExecutor real = realExecutors.get(cmdName);
                if (real != null) enableCommand(cmdName, real);
            }
        } else {
            for (String cmdName : def.commands()) {
                disableCommand(cmdName);
            }
        }
    }

    private void enableCommand(String commandName, CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(commandName);
        if (cmd == null) return;
        cmd.setExecutor(executor);
        cmd.setTabCompleter(null);
    }

    private void disableCommand(String commandName) {
        PluginCommand cmd = plugin.getCommand(commandName);
        if (cmd == null) return;
        cmd.setExecutor(DISABLED_EXECUTOR);
        cmd.setTabCompleter((sender, command, alias, args) -> Collections.emptyList());
    }
}