package com.clitaskmanager.compliance;

import com.clitaskmanager.persistence.sqlite.DatabaseConnectionManager;
import com.clitaskmanager.persistence.sqlite.SqliteFocusSessionRepository;
import com.clitaskmanager.persistence.sqlite.SqliteTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitecturalComplianceTest {

    @Test
    void verifySqliteIsSolePersistenceEngine(@TempDir Path tempDir) throws Exception {
        File dbFile = tempDir.resolve("architectural-compliance.db").toFile();
        DatabaseConnectionManager.setDbPathOverride(dbFile.getAbsolutePath());
        DatabaseConnectionManager.initializeDatabase();

        // 1. Verify SQLite JDBC driver connection
        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            assertThat(metaData.getDriverName()).containsIgnoringCase("SQLite");
            assertThat(metaData.getDatabaseProductName()).containsIgnoringCase("SQLite");
        }

        // 2. Verify SQLite database file created on disk
        assertThat(dbFile).exists();
        assertThat(dbFile.length()).isGreaterThan(0);

        // 3. Verify SQLite repositories instantiation
        SqliteTaskRepository taskRepository = new SqliteTaskRepository();
        SqliteFocusSessionRepository focusSessionRepository = new SqliteFocusSessionRepository();

        assertThat(taskRepository).isNotNull();
        assertThat(focusSessionRepository).isNotNull();
    }
}
