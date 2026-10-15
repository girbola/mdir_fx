package com.girbola.persistence.configuration;

import com.girbola.controllers.main.SQLTableEnums;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationSavedFoldersDaoTest {

    @Test
    void upgradesExistingSavedFoldersTableWithDriveIdentityColumns() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE %s (
                        path TEXT PRIMARY KEY,
                        justFolderName TEXT,
                        tableType TEXT NOT NULL,
                        connected BOOLEAN
                    )
                    """.formatted(SQLTableEnums.SAVED_FOLDERS.getType()));

            assertTrue(ConfigurationSavedFoldersDao.createSavedFoldersIntoConfigurationDatabase(connection));

            Set<String> columns = new HashSet<>();
            try (ResultSet resultSet = statement.executeQuery(
                    "PRAGMA table_info(" + SQLTableEnums.SAVED_FOLDERS.getType() + ")")) {
                while (resultSet.next()) {
                    columns.add(resultSet.getString("name"));
                }
            }

            assertTrue(columns.containsAll(Set.of(
                    "driveIdentifier", "driveSerial", "driveSize", "partitionUuid", "partitionIdentification")));
        }
    }
}
