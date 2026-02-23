package at.blaulix.coreon;

import at.blaulix.coreon.command.CoreonCommand;
import at.blaulix.coreon.command.InvseeCommand;
import at.blaulix.coreon.handler.CoreonHandler;
import at.blaulix.coreon.listener.CoreonListener;
import at.blaulix.coreon.listener.QuitListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class Coreon extends JavaPlugin {
    private final File commandDescriptions = new File(getDataFolder(), "command_descriptions.yml");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        //Handler
        CoreonHandler coreonHandler = new CoreonHandler(this);

        //Listener
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new CoreonListener(coreonHandler), this);

        //Commands
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(coreonHandler));
        Objects.requireNonNull(getCommand("invsee")).setExecutor(new InvseeCommand());
        //Objects.requireNonNull(getCommand("vanish")).setExecutor(new VanishCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public File getCommandDescriptions() {
        return commandDescriptions;
    }
}
