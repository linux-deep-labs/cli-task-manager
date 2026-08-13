package com.clitaskmanager.persistence.sqlite;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionManager {

    private static String dbPathOverride = null;

    public static void setDbPathOverride(String path) {
        dbPathOverride = path;
    }

    public static String getDbPath() {
        if (dbPathOverride != null) {
            return dbPathOverride;
        }
        String customEnvPath = System.getenv("TASK_DB_PATH");
        if (customEnvPath != null && !customEnvPath.isBlank()) {
            return customEnvPath;
        }
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, ".cli-task-manager");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "tasks.db").getAbsolutePath();
    }

    public static Connection getConnection() throws SQLException {
        String dbPath = getDbPath();
        String url = "jdbc:sqlite:" + dbPath;
        Connection conn = DriverManager.getConnection(url);
        // Enable foreign key constraints in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            InputStream is = DatabaseConnectionManager.class.getResourceAsStream("/db/migration/V1__initial_schema.sql");
            if (is == null) {
                throw new IllegalStateException("Migration script /db/migration/V1__initial_schema.sql not found");
            }
            String ddl = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : ddl.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException sqle) {
                throw sqle;
            }
            throw new SQLException("Failed to initialize database schema", e);
        }
    }
}
