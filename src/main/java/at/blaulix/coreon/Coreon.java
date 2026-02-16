package at.blaulix.coreon;

import at.blaulix.coreon.command.CoreonCommand;
import at.blaulix.coreon.command.InvseeCommand;

import at.blaulix.coreon.listener.QuitListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Coreon extends JavaPlugin {

    @Override
    public void onEnable() {
        //Listener
        getServer().getPluginManager().registerEvents(new QuitListener(), this);

        //Commands
        Objects.requireNonNull(getCommand("coreon")).setExecutor(new CoreonCommand(this));
        Objects.requireNonNull(getCommand("invsee")).setExecutor(new InvseeCommand());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
