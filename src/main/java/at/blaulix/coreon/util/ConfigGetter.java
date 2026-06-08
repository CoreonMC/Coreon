package at.blaulix.coreon.util;

import at.blaulix.coreon.Coreon;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigGetter {

    // config.yml

    public static String getString(String path) {
        return Coreon.getInstance().getConfig().getString(path);
    }

    public static int getInt(String path) {
        return Coreon.getInstance().getConfig().getInt(path);
    }

    public static boolean getBoolean(String path) {
        return Coreon.getInstance().getConfig().getBoolean(path);
    }

    public static ConfigurationSection getSection(String path) {
        return Coreon.getInstance().getConfig().getConfigurationSection(path);
    }

    // messages.yml

    public static String getMessage(String path) {
        return Coreon.getInstance().getMessages().getString(path);
    }
}
