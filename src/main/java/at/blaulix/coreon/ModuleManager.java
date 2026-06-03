package at.blaulix.coreon;

import at.blaulix.coreon.command.*;
import at.blaulix.coreon.database.HomeDatabase;
import at.blaulix.coreon.handler.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages module-based command registration and deregistration at runtime.
 * One module = one or more commands + optional listeners.
 *
 * Call applyAll() on startup, and apply(key) after toggling a module in config.
 */
public class ModuleManager {

    private final Coreon plugin;

    // Stores the real executors so we can restore them on re-enable
    private final Map<String, CommandExecutor> realExecutors = new HashMap<>();

    // Stores listeners that need to be registered/unregistered per module
    private final Map<String, Listener> moduleListeners = new HashMap<>();

    // Executor used for disabled commands - gives feedback + hides from tab
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

        // Register real executors per module key (must match config key & plugin.yml command name)
        realExecutors.put("invsee",    new InvseeCommand());
        realExecutors.put("vanish",    new VanishCommand(vanishHandler));
        realExecutors.put("pvptimer",  new PvPTimerCommand(pvpTimerHandler));
        realExecutors.put("homes",      new HomesCommand(plugin, homeDatabase));
    }

    /**
     * Apply the current module state from config for all known modules.
     * Call this once during onEnable().
     */
    public void applyAll() {
        ConfigurationSection modules = plugin.getConfig().getConfigurationSection("modules");
        if (modules == null) return;

        for (String key : modules.getKeys(false)) {
            apply(key);
        }
    }

    /**
     * Apply the current config state for a single module key.
     * Call this after changeActive() to update commands live.
     */
    public void apply(String key) {
        boolean enabled = plugin.getConfig().getBoolean("modules." + key, false);
        CommandExecutor real = realExecutors.get(key.toLowerCase());

        // If we have no real executor registered, this module has no commands - skip
        if (real == null) return;

        if (enabled) {
            enableCommand(key.toLowerCase(), real);
        } else {
            disableCommand(key.toLowerCase());
        }
    }

    // --- Private helpers ---

    private void enableCommand(String commandName, CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(commandName);
        if (cmd == null) return;

        cmd.setExecutor(executor);
        // Restore normal tab completion (null = default Bukkit behavior)
        cmd.setTabCompleter(null);
    }

    private void disableCommand(String commandName) {
        PluginCommand cmd = plugin.getCommand(commandName);
        if (cmd == null) return;

        cmd.setExecutor(DISABLED_EXECUTOR);
        // Empty tab completer = command disappears from suggestions
        cmd.setTabCompleter((sender, command, alias, args) -> Collections.emptyList());
    }
}
