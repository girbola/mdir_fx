package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Pair;

public class TablesSQL {

    private static Connection connection;

    private static String tableSortIt = "tablesortit";
    private static String tableSorted = "tablesorted";
    private static String tableAsItIs = "tableasitis";

    public static void saveColumnOrderToRow(TableView<FolderInfo> table) {
        connection = ConfigurationSQLHandler.getConnection();
        String tableName = getCorrectTableById(table.getId());

        if (tableName == null) {
            Messages.sprintfError("Invalid table ID: " + table.getId());
            return;
        }

        // Build CREATE TABLE statement dynamically based on actual columns
        StringBuilder createTableSQL = new StringBuilder();
        createTableSQL.append("CREATE TABLE ").append(tableName).append(" (");

        // Add standard metadata columns first
        createTableSQL.append("id INTEGER PRIMARY KEY AUTOINCREMENT, ");

        // Add columns based on TableView structure
        for (TableColumn<FolderInfo, ?> column : table.getColumns()) {
            String columnId = column.getId();
            if (columnId != null && !columnId.isEmpty()) {
                // Convert JavaFX types to SQL types
                String sqlType = getSQLTypeForColumn(column);
                createTableSQL.append(columnId).append(" ").append(sqlType).append(", ");
                // Add column for storing the order
                createTableSQL.append(columnId).append("_order INTEGER, ");
                // Add column for storing the width
                createTableSQL.append(columnId).append("_width DOUBLE, ");
            }
        }

        // Remove the trailing comma and space
        if (createTableSQL.toString().endsWith(", ")) {
            createTableSQL.setLength(createTableSQL.length() - 2);
        }
        createTableSQL.append(")");

        try {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                // Drop existing table
                stmt.execute("DROP TABLE IF EXISTS " + tableName);

                // Create new table
                stmt.execute(createTableSQL.toString());

                // Insert the current column configuration
                insertColumnConfiguration(table, tableName, connection);

                SQL_Utils.commitChanges(connection);
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error saving column configuration: " + e.getMessage());
            SQL_Utils.rollBackConnection(connection);
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    private static String getSQLTypeForColumn(TableColumn<FolderInfo, ?> column) {
        // Determine SQL type based on the column's value type
        if (column.getCellData(0) instanceof Number) {
            if (column.getCellData(0) instanceof Integer) {
                return "INTEGER";
            } else if (column.getCellData(0) instanceof Double) {
                return "DOUBLE";
            } else if (column.getCellData(0) instanceof Long) {
                return "BIGINT";
            }
        } else if (column.getCellData(0) instanceof Boolean) {
            return "BOOLEAN";
        }
        // Default to TEXT for String and other types
        return "TEXT";
    }

    private static void insertColumnConfiguration(TableView<FolderInfo> table, String tableName, Connection connection)
            throws SQLException {
        StringBuilder insertSQL = new StringBuilder();
        insertSQL.append("INSERT INTO ").append(tableName).append(" (");

        // Build column names part
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < table.getColumns().size(); i++) {
            TableColumn<FolderInfo, ?> column = table.getColumns().get(i);
            String columnId = column.getId();
            if (columnId != null && !columnId.isEmpty()) {
                columnNames.add(columnId);
                columnNames.add(columnId + "_order");
                columnNames.add(columnId + "_width");

                values.add(column.getText());  // or any other relevant column data
                values.add(i);  // order
                values.add(column.getWidth());  // width
            }
        }

        insertSQL.append(String.join(", ", columnNames));
        insertSQL.append(") VALUES (");
        insertSQL.append("?, ".repeat(values.size()));

        // Remove trailing comma and space
        if (insertSQL.toString().endsWith(", ")) {
            insertSQL.setLength(insertSQL.length() - 2);
        }
        insertSQL.append(")");

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL.toString())) {
            // Set all values
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    public static void restoreColumnOrderFromRow(TableView<FolderInfo> table) {
        connection = ConfigurationSQLHandler.getConnection();
        String tableName = getCorrectTableById(table.getId());

        if (tableName == null || !SQL_Utils.isDbAccessible(connection, tableName)) {
            return;
        }

        try {
            // Create a map of current columns
            Map<String, TableColumn<FolderInfo, ?>> columnMap = new HashMap<>();
            for (TableColumn<FolderInfo, ?> col : table.getColumns()) {
                columnMap.put(col.getId(), col);
            }

            // Query the saved configuration
            String sql = "SELECT * FROM " + tableName + " LIMIT 1";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    // Clear existing columns
                    table.getColumns().clear();

                    // Create a sorted list of columns based on their order
                    List<Pair<TableColumn<FolderInfo, ?>, Integer>> orderedColumns = new ArrayList<>();

                    for (String columnId : columnMap.keySet()) {
                        TableColumn<FolderInfo, ?> column = columnMap.get(columnId);
                        if (column != null) {
                            // Get the order and width for this column
                            int order = rs.getInt(columnId + "_order");
                            double width = rs.getDouble(columnId + "_width");

                            // Set the width
                            column.setPrefWidth(width);

                            // Add to ordered list
                            orderedColumns.add(new Pair<>(column, order));
                        }
                    }

                    // Sort columns by their order
                    orderedColumns.sort(Comparator.comparing(Pair::getValue));

                    // Add columns in the correct order
                    for (Pair<TableColumn<FolderInfo, ?>, Integer> pair : orderedColumns) {
                        table.getColumns().add(pair.getKey());
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error restoring column configuration: " + e.getMessage());
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    private static String getCorrectTableById(String tableId) {
        if (tableId.equalsIgnoreCase(TableType.SORTIT.getType().toLowerCase())) {
            Messages.sprintf("1Creating table: " + tableSortIt);
            return tableSortIt;
        } else if (tableId.equalsIgnoreCase(TableType.SORTED.getType().toLowerCase())) {
            Messages.sprintf("2Creating table: " + tableSorted);
            return tableSorted;
        } else if (tableId.equalsIgnoreCase(TableType.ASITIS.getType().toLowerCase())) {
            Messages.sprintf("3Creating table: " + tableAsItIs);
            return tableAsItIs;
        } else {
            Main.setProcessCancelled(true);
            throw new IllegalArgumentException("Unknown tableId: " + tableId);
        }
    }
}