package com.girbola.persistence.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConfigurationSqlDatabaseMigrator {

    private static final int CURRENT_SCHEMA_VERSION = 1;

    private ConfigurationSqlDatabaseMigrator() {
    }

    public static void migrate(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return;
        }

        int currentVersion = getUserVersion(connection);

        if (currentVersion >= CURRENT_SCHEMA_VERSION) {
            return;
        }

        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            if (currentVersion < 1) {
                migrateToVersion1(connection);
                setUserVersion(connection, 1);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static int getUserVersion(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA user_version")) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private static void setUserVersion(Connection connection, int version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA user_version = " + version);
        }
    }

    private static void migrateToVersion1(Connection connection) throws SQLException {
        // Add configuration schema migrations here.
        // Example:
        // addMissingColumns(connection);
        // createIndexes(connection);
    }
}
