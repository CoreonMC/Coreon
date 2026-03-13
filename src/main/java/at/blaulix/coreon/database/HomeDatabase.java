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

public class HomeDatabase {
    private final JavaPlugin plugin;
    private Connection connection;
    private final String databaseFileName;
    private static final List<HomeDatabase> all = new ArrayList<>();

    public HomeDatabase(JavaPlugin plugin, String databaseFileName) {
        this.plugin = plugin;
        this.databaseFileName = databaseFileName;
        all.add(this);
    }

    public static List<HomeDatabase> getAll(){
        return all;
    }

    // Connect and create table if missing
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

    // Helper to enable DB at plugin startup
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

    // Save home (REPLACE will overwrite existing home with same name)
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

    // Load a home by name
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

    // Return list of home names for player
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

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}