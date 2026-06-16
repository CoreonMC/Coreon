package at.blaulix.coreon.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Wrapper für eine SQLite-Datenbank zur Speicherung von Spieler-Homes.
 * Verwaltet Connection, Erstellung der Tabelle und CRUD-Operationen.
 */
public class HomeDatabase {
    private final JavaPlugin plugin;
    private Connection connection;
    private final String databaseFileName;
    private static final List<HomeDatabase> all = new ArrayList<>();

    /**
     * Legt eine neue HomeDatabase-Instanz an.
     *
     * @param plugin           Plugin-Instanz (zur Pfad- und Logger-Nutzung)
     * @param databaseFileName Dateiname der SQLite-DB innerhalb plugins/Coreon/databases
     */
    public HomeDatabase(JavaPlugin plugin, String databaseFileName) {
        this.plugin = plugin;
        this.databaseFileName = databaseFileName;
        all.add(this);
    }

    /**
     * Liefert alle registrierten HomeDatabase-Instanzen.
     *
     * @return Liste aller HomeDatabase-Instanzen
     */
    public static List<HomeDatabase> getAll(){
        return all;
    }

    /**
     * Öffnet die DB-Verbindung und erstellt die Tabelle falls notwendig.
     *
     * @throws SQLException bei SQL-Fehlern
     * @throws IOException  bei Dateisystemfehlern
     */
    public void connect() throws SQLException, IOException {
        // Ensure plugins/Coreon/databases/ exists
        File databaseFolder = new File(plugin.getDataFolder(), "databases");
        if (!databaseFolder.exists()) {
            databaseFolder.mkdirs();
        }

        File file = new File(databaseFolder, databaseFileName);
        String url = "jdbc:sqlite:" + file.getPath();

        this.connection = DriverManager.getConnection(url);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS player_homes (" +
                    "uuid TEXT, name TEXT, world TEXT, x REAL, y REAL, z REAL, yaw REAL, pitch REAL, " +
                    "PRIMARY KEY (uuid, name))");
        }
    }

    /**
     * Hilfsmethode zum initialen Aktivieren der Datenbank beim Plugin-Start.
     * Verbindet und deaktiviert das Plugin bei fatalen Fehlern.
     */
    public void enableDatabase() {
        try {
            connect();
            plugin.getLogger().info("Database connected successfully!");
        } catch (SQLException | IOException e) {
            plugin.getLogger().severe("Failed to load database!");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Speichert oder überschreibt ein Home eines Spielers.
     *
     * @param uuid UUID des Spielers
     * @param name Name des Homes
     * @param loc  Ziel-Location
     */
    public void saveHome(UUID uuid, String name, Location loc) {
        String sql = "REPLACE INTO player_homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name.toLowerCase());
            pstmt.setString(3, loc.getWorld().getName());
            pstmt.setDouble(4, loc.getX());
            pstmt.setDouble(5, loc.getY());
            pstmt.setDouble(6, loc.getZ());
            pstmt.setFloat(7, loc.getYaw());
            pstmt.setFloat(8, loc.getPitch());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Löscht ein Home eines Spielers.
     *
     * @param uuid UUID des Spielers
     * @param name Name des Homes
     */
    public void deleteHome(UUID uuid, String name){
        String sql = "DELETE FROM player_homes WHERE uuid = ? AND name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lädt ein Home aus der Datenbank.
     *
     * @param uuid UUID des Spielers
     * @param name Name des Homes
     * @return Location oder {@code null}, wenn nicht gefunden
     */
    public Location getHome(UUID uuid, String name) {
        String sql = "SELECT * FROM player_homes WHERE uuid = ? AND name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("world"));
                if (world == null) return null;
                return new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Liefert eine Liste der Home-Namen eines Spielers.
     *
     * @param uuid UUID des Spielers
     * @return Liste von Home-Namen
     */
    public List<String> getHomesList(UUID uuid) {
        String sql = "SELECT name FROM player_homes WHERE uuid = ?";
        List<String> homes = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            homes = new ArrayList<>();
            while (rs.next()) {
                homes.add(rs.getString("name"));
            }
            // list collected
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }

    /**
     * Liefert die Anzahl der Homes eines Spielers.
     *
     * @param uuid UUID des Spielers
     * @return Anzahl an Homes
     */
    public int getHomeCount(UUID uuid) {
        String sql = "SELECT COUNT(*) AS count FROM player_homes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Schließt die Datenbankverbindung sauber.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}