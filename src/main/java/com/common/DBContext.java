package com.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBContext {

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    // load config
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
                // Fallback to default 
                DB_URL = "jdbc:mysql://localhost:3306/file_converter_db";
                DB_USER = "root";
                DB_PASSWORD = "";
                System.out.println("[DBContext] Warning: database.properties not found, using defaults");
            }

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
