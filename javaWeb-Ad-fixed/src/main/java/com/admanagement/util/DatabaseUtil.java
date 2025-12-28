package com.admanagement.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility
 */
public class DatabaseUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    static {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseUtil.class.getClassLoader()
                    .getResourceAsStream("config.properties");
            
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            
            props.load(input);
            
            url = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driver = props.getProperty("db.driver");
            
            // Load MySQL driver
            Class.forName(driver);
            
            input.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize database configuration", e);
        }
    }

    /**
     * Get a database connection
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Close database connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
