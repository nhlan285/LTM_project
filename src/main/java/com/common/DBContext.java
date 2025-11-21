package com.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * =====================================================
 * DBContext - DATABASE CONNECTION UTILITY
 * =====================================================
 * Purpose: Provides centralized database connection management
 * 
 * DESIGN PATTERN: Singleton Pattern (Optional) / Static Factory
 * 
 * Network Programming Relevance:
 * - Both Web Server and Conversion Server need DB access
 * - Connection pooling can be added here for production
 * 
 * @author Your Name
 * @course Network Programming - Final Project
 */
public class DBContext {

    // Database Configuration (Can be moved to .properties file)
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    // Static block to load configuration
    static {
        Properties props = new Properties();
        try (InputStream input = DBContext.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                DB_URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/file_converter_db");
                DB_USER = props.getProperty("db.user", "root");
                DB_PASSWORD = props.getProperty("db.password", "");
            } else {
                // Fallback to default values if properties file not found
                DB_URL = "jdbc:mysql://localhost:3306/file_converter_db";
                DB_USER = "root";
                DB_PASSWORD = "";
                System.out.println("[DBContext] Warning: database.properties not found, using defaults");
            }

            // Load MySQL JDBC Driver (Important for JDBC 4.0+)
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBContext] MySQL Driver loaded successfully");

        } catch (IOException e) {
            System.err.println("[DBContext] Error loading database.properties: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBContext] MySQL Driver not found: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Creates and returns a new database connection
     * 
     * NETWORKING CONTEXT:
     * - This method is called by BOTH Web Server (Module A) and Conversion Server
     * (Module B)
     * - Each thread in the ThreadPool will get its own connection (Thread-safe)
     * 
     * @return Connection object connected to MySQL
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DBContext] Database connection established: " + DB_URL);
            return conn;
        } catch (SQLException e) {
            System.err.println("[DBContext] Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Safely closes a database connection
     * Best Practice: Always close connections in finally block or
     * try-with-resources
     * 
     * @param conn The connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("[DBContext] Connection closed successfully");
            } catch (SQLException e) {
                System.err.println("[DBContext] Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test the database connection (Utility method for debugging)
     * Can be run as main method to verify DB setup
     */
    public static void main(String[] args) {
        System.out.println("===== Testing Database Connection =====");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ SUCCESS: Database connected!");
                System.out.println("   Database: " + conn.getCatalog());
                System.out.println("   User: " + conn.getMetaData().getUserName());
            }
        } catch (SQLException e) {
            System.err.println("❌ FAILED: " + e.getMessage());
            System.err.println("   Please check:");
            System.err.println("   1. MySQL server is running");
            System.err.println("   2. Database 'file_converter_db' exists");
            System.err.println("   3. Username/password in database.properties is correct");
        }
    }
}
