package com.nftstudio.nftstudio.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:nft_studio.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createLayersTable = """
                CREATE TABLE IF NOT EXISTS layers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    file_path TEXT NOT NULL
                );
            """;

            stmt.execute(createLayersTable);
            System.out.println("SQLite Database connected and layers table verified!");

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
    public static void insertLayer(String name, String category, String filePath) {
        String insertSQL = "INSERT INTO layers(name, category, file_path) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setString(3, filePath);
            pstmt.executeUpdate();

            System.out.println("Successfully logged layer to database: " + name + " [" + category + "]");

        } catch (SQLException e) {
            System.out.println("Database insert failed: " + e.getMessage());
        }
    }

    public static Map<String, List<String>> getLayersByCategory() {
        Map<String, List<String>> categorizedLayers = new HashMap<>();

        String query = "SELECT category, file_path FROM layers";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String category = rs.getString("category");
                String filePath = rs.getString("file_path");

                categorizedLayers.putIfAbsent(category, new ArrayList<>());

                categorizedLayers.get(category).add(filePath);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching layers: " + e.getMessage());
        }

        return categorizedLayers;
    }

    public static void deleteLayer(String filePath) {
        String deleteSQL = "DELETE FROM layers WHERE file_path = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setString(1, filePath);
            pstmt.executeUpdate();
            System.out.println("Deleted layer from database: " + filePath);

        } catch (SQLException e) {
            System.out.println("Error deleting layer: " + e.getMessage());
        }
    }

    public static void resetDatabase() {
        String resetSQL = "DELETE FROM layers";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(resetSQL);
            System.out.println("DATABASE RESET: All layers have been cleared.");

        } catch (SQLException e) {
            System.out.println("Error resetting database: " + e.getMessage());
        }
    }
}