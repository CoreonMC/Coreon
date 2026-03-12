package at.blaulix.coreon.handler;

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
import java.util.logging.Level;

public class Database {
    private final JavaPlugin plugin;
    private Connection connection;
    private final String databaseFileName;
    private static final List<Database> all = new ArrayList<>();

    // Track all Database instances
    public Database(JavaPlugin plugin, String databaseFileName) {
        this.plugin = plugin;
        this.databaseFileName = databaseFileName;
        all.add(this);
    }

    public static List<Database> getAll(){
        return all;
    }

    // Connect and create table
    public void connect() throws SQLException, IOException {
        // Create plugins/Coreon/databases/ if missing
        File databaseFolder = new File(plugin.getDataFolder(), "databases");
        if (!databaseFolder.exists()) {
            boolean created = databaseFolder.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Could not create database folder: " + databaseFolder.getPath());
            }
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

    // Called from main to enable the database connection
    public void enableDatabase() {
        try {
            connect();
            plugin.getLogger().info("Database connected successfully!");
        } catch (SQLException | IOException e) {
            plugin.getLogger().severe("Could not load database!");
            plugin.getLogger().log(Level.SEVERE, "Failed to enable database", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    // Save home (REPLACE overwrites homes with same name)
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
            plugin.getLogger().log(Level.SEVERE, "Failed to save home", e);
        }
    }

    // Load a named home for a player
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
            plugin.getLogger().log(Level.SEVERE, "Failed to load home", e);
        }
        return null;
    }

    // Return a list of a player's home names
    public List<String> getHomes(UUID uuid) {
        String sql = "SELECT name FROM player_homes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            List<String> homes = new ArrayList<>();
            while (rs.next()) {
                homes.add(rs.getString("name"));
            }
            return homes; // return the list of homes
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to list homes", e);
        }
        return new ArrayList<>();
    }

    // Count how many homes a player has
    public int getHomeCount(UUID uuid) {
        String sql = "SELECT COUNT(*) AS count FROM player_homes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to count homes", e);
        }
        return 0;
    }

    // Close DB connection
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
}