package com.admanagement.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility with retry mechanism
 */
public class DatabaseUtil {
    private static String url;
    private static String username;
    private static String password;
    private static String driver;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

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
     * Get a database connection with retry mechanism
     */
    public static Connection getConnection() throws SQLException {
        SQLException lastException = null;
        System.out.println("Attempting to connect to: " + url);
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                System.out.println("Connection attempt " + attempt + " of " + MAX_RETRY_ATTEMPTS);
                Connection conn = DriverManager.getConnection(url, username, password);
                if (conn != null && !conn.isClosed()) {
                    // Connection successful
                    System.out.println("✓ Database connection established successfully" + 
                                     (attempt > 1 ? " on attempt " + attempt : ""));
                    return conn;
                }
            } catch (SQLException e) {
                lastException = e;
                System.err.println("✗ Connection attempt " + attempt + " failed: " + e.getMessage());
                System.err.println("  Error code: " + e.getErrorCode() + ", SQL state: " + e.getSQLState());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    System.out.println("  Retrying in " + RETRY_DELAY_MS + "ms...");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Connection retry interrupted", ie);
                    }
                }
            }
        }
        
        // All attempts failed
        System.err.println("✗ Failed to establish database connection after " + MAX_RETRY_ATTEMPTS + " attempts");
        throw new SQLException("Failed to connect to database after " + MAX_RETRY_ATTEMPTS + 
                             " attempts. Last error: " + (lastException != null ? lastException.getMessage() : "unknown"), 
                             lastException);
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
