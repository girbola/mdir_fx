package com.girbola.migration;

import com.girbola.persistence.migration.FileInfoSqlDatabaseMigrator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link FileInfoSqlDatabaseMigrator} class.
 * These tests validate the behavior of the {@code migrate} method under various use cases.
 */
class FileInfoDaoDatabaseMigratorTest {

    /**
     * Test that when the connection is null, the `migrate` method does nothing.
     */
    @Test
    void testMigrateWithNullConnection() throws SQLException {
        // Act
        assertDoesNotThrow(() -> FileInfoSqlDatabaseMigrator.migrate(null));
        // No assertion needed since null connection should not throw any errors.
    }

    /**
     * Test that when the connection is closed, the `migrate` method does nothing.
     */
    @Test
    void testMigrateWithClosedConnection() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(true);

        // Act
        FileInfoSqlDatabaseMigrator.migrate(mockConnection);

        // Assert
        verify(mockConnection, never()).createStatement();
        verify(mockConnection, never()).setAutoCommit(anyBoolean());
    }

    /**
     * Test that when the schema version is already up-to-date, no migration is performed.
     */
    @Test
    void testMigrateWithUpToDateSchema() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.isClosed()).thenReturn(false);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("PRAGMA user_version")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // CURRENT_SCHEMA_VERSION

        // Act
        FileInfoSqlDatabaseMigrator.migrate(mockConnection);

        // Assert
        verify(mockConnection, never()).setAutoCommit(false);
        verify(mockConnection, never()).commit();
        verify(mockConnection, never()).rollback();
    }

    /**
     * Test that when the schema version is outdated, migration is performed successfully.
     */
    @Test
    void testMigrateWithOutdatedSchema() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.isClosed()).thenReturn(false);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("PRAGMA user_version")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // Outdated version

        // Act
        FileInfoSqlDatabaseMigrator.migrate(mockConnection);

        // Assert
        verify(mockConnection).setAutoCommit(false);
        verify(mockStatement).execute("PRAGMA user_version = 1");
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    /**
     * Test that when an exception occurs during migration, changes are rolled back.
     */
    @Test
    void testMigrateWithExceptionDuringMigration() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.isClosed()).thenReturn(false);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery("PRAGMA user_version")).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // Outdated version
        doThrow(new SQLException("Simulated failure")).when(mockStatement).execute(anyString());

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class, () -> FileInfoSqlDatabaseMigrator.migrate(mockConnection));
        assertEquals("Simulated failure", exception.getMessage());

        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }
}