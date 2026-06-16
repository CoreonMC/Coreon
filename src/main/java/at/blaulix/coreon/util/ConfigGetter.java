package at.blaulix.coreon.util;

import at.blaulix.coreon.Coreon;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Utility-Klasse zum einfachen Zugriff auf Werte aus config.yml und messages.yml.
 */
public class ConfigGetter {

    // config.yml

    /**
     * Liest einen String-Wert aus der config.yml.
     *
     * @param path Pfad in der Konfiguration
     * @return gespeicherter String oder {@code null}
     */
    public static String getString(String path) {
        return Coreon.getInstance().getConfig().getString(path);
    }

    /**
     * Liest einen int-Wert aus der config.yml.
     *
     * @param path Pfad in der Konfiguration
     * @return gespeicherter int-Wert
     */
    public static int getInt(String path) {
        return Coreon.getInstance().getConfig().getInt(path);
    }

    /**
     * Liest einen boolean-Wert aus der config.yml.
     *
     * @param path Pfad in der Konfiguration
     * @return gespeicherter boolean-Wert
     */
    public static boolean getBoolean(String path) {
        return Coreon.getInstance().getConfig().getBoolean(path);
    }

    /**
     * Liefert einen ConfigurationSection aus der config.yml.
     *
     * @param path Pfad zur Section
     * @return ConfigurationSection oder {@code null}
     */
    public static ConfigurationSection getSection(String path) {
        return Coreon.getInstance().getConfig().getConfigurationSection(path);
    }

    // messages.yml

    /**
     * Liest einen Eintrag aus messages.yml.
     *
     * @param path Pfad in messages.yml
     * @return gespeicherte Nachricht oder {@code null}
     */
    public static String getMessage(String path) {
        return Coreon.getInstance().getMessages().getString(path);
    }
}
