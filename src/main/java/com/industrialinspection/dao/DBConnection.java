package com.industrialinspection.dao;

import com.industrialinspection.exception.DatabaseException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Database Connection Manager using JDBC.
 * Reads credentials dynamically from db.properties.
 */
public class DBConnection {

    private static volatile DBConnection instance;
    private String url;
    private String username;
    private String password;
    private String driver;
    private boolean isDriverLoaded = false;
    private Boolean isConnected = null;

    private DBConnection() {
        loadProperties();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception ignored) {}

        this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/industrial_inspection?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        this.username = props.getProperty("db.user", "root");
        this.password = props.getProperty("db.password", "root");

        try {
            Class.forName(driver);
            isDriverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[WARN] MySQL JDBC Driver not found on classpath: " + driver);
            isDriverLoaded = false;
        }
    }

    /**
     * Obtains a live JDBC Connection to the MySQL database.
     */
    public Connection getConnection() throws DatabaseException {
        if (!isDriverLoaded) {
            try {
                Class.forName(driver);
                isDriverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new DatabaseException("MySQL JDBC Driver could not be loaded: " + driver, e);
            }
        }
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            isConnected = true;
            return conn;
        } catch (SQLException e) {
            isConnected = false;
            throw new DatabaseException("Unable to connect to MySQL database at " + url + ": " + e.getMessage(), e);
        }
    }

    /**
     * Non-throwing connection test for UI status indicators.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUrl() { return url; }
    public String getUsername() { return username; }
}
