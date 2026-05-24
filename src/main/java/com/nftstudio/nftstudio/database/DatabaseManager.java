package com.nftstudio.nftstudio.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DatabaseManager {

    // This tells SQLite to create a file named 'nft_studio.db' right next to your app
    private static final String DB_URL = "jdbc:sqlite:nft_studio.db";

    public static void initializeDatabase() {
        // The try-with-resources block automatically opens and closes the connection
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // SQL command to create a table for our layers if it doesn't exist yet
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
}