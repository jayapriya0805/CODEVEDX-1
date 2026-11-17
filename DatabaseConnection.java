package com.library;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String CONFIG_FILE = "config.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            throw new SQLException(
                    "Cannot read config.properties. Make sure it exists in the project root.", e);
        }

        String url = properties.getProperty("db.url",
                "jdbc:mysql://localhost:3306/library_management");
        String username = properties.getProperty("db.username", "root");
        String password = properties.getProperty("db.password", "");

        if ("YOUR_MYSQL_PASSWORD".equals(password) || "CHANGE_ME".equals(password)) {
            throw new SQLException(
                    "Please edit config.properties and replace the placeholder with your MySQL password.");
        }

        return DriverManager.getConnection(url, username, password);
    }
}
