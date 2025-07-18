package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoEnum;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TablesSQL {

    final private static String createTableColumnsWidths = " (" + TableColumnEnum.DATE_DIFFERENCE_RATIO_WIDTH.getColumnName() + " " + TableColumnEnum.DATE_DIFFERENCE_RATIO_WIDTH.getSqlType() + "," + TableColumnEnum.BAD_FILES_WIDTH.getColumnName() + " " + TableColumnEnum.BAD_FILES_WIDTH.getSqlType() + "," + TableColumnEnum.COPIED_WIDTH.getColumnName() + " " + TableColumnEnum.COPIED_WIDTH.getSqlType() + "," + TableColumnEnum.FOLDER_FILES_WIDTH.getColumnName() + " " + TableColumnEnum.FOLDER_FILES_WIDTH.getSqlType() + "," + TableColumnEnum.IMAGE_WIDTH.getColumnName() + " " + TableColumnEnum.IMAGE_WIDTH.getSqlType() + "," + TableColumnEnum.MEDIA_WIDTH.getColumnName() + " " + TableColumnEnum.MEDIA_WIDTH.getSqlType() + "," + TableColumnEnum.RAW_WIDTH.getColumnName() + " " + TableColumnEnum.RAW_WIDTH.getSqlType() + "," + TableColumnEnum.STATUS_WIDTH.getColumnName() + " " + TableColumnEnum.STATUS_WIDTH.getSqlType() + "," + TableColumnEnum.SUGGESTED_WIDTH.getColumnName() + " " + TableColumnEnum.SUGGESTED_WIDTH.getSqlType() + "," + TableColumnEnum.VIDEO_WIDTH.getColumnName() + " " + TableColumnEnum.VIDEO_WIDTH.getSqlType() + "," + TableColumnEnum.SIZE_WIDTH.getColumnName() + " " + TableColumnEnum.SIZE_WIDTH.getSqlType() + "," + TableColumnEnum.DATE_FIX_WIDTH.getColumnName() + " " + TableColumnEnum.DATE_FIX_WIDTH.getSqlType() + "," + TableColumnEnum.FULL_PATH_WIDTH.getColumnName() + " " + TableColumnEnum.FULL_PATH_WIDTH.getSqlType() + "," + TableColumnEnum.JUST_FOLDER_NAME_WIDTH.getColumnName() + " " + TableColumnEnum.JUST_FOLDER_NAME_WIDTH.getSqlType() + "," + TableColumnEnum.MAX_DATES_WIDTH.getColumnName() + " " + TableColumnEnum.MAX_DATES_WIDTH.getSqlType() + "," + TableColumnEnum.MIN_DATE_WIDTH.getColumnName() + " " + TableColumnEnum.MIN_DATE_WIDTH.getSqlType();

    final private static String createTableColumns = " (" + TableColumnEnum.DATE_DIFFERENCE_RATIO.getColumnName() + " " + TableColumnEnum.DATE_DIFFERENCE_RATIO.getSqlType() + "," + TableColumnEnum.BAD_FILES.getColumnName() + " " + TableColumnEnum.BAD_FILES.getSqlType() + "," + TableColumnEnum.COPIED.getColumnName() + " " + TableColumnEnum.COPIED.getSqlType() + "," + TableColumnEnum.FOLDER_FILES.getColumnName() + " " + TableColumnEnum.FOLDER_FILES.getSqlType() + "," + TableColumnEnum.IMAGE.getColumnName() + " " + TableColumnEnum.IMAGE.getSqlType() + "," + TableColumnEnum.MEDIA.getColumnName() + " " + TableColumnEnum.MEDIA.getSqlType() + "," + TableColumnEnum.RAW.getColumnName() + " " + TableColumnEnum.RAW.getSqlType() + "," + TableColumnEnum.STATUS.getColumnName() + " " + TableColumnEnum.STATUS.getSqlType() + "," + TableColumnEnum.SUGGESTED.getColumnName() + " " + TableColumnEnum.SUGGESTED.getSqlType() + "," + TableColumnEnum.VIDEO.getColumnName() + " " + TableColumnEnum.VIDEO.getSqlType() + "," + TableColumnEnum.SIZE.getColumnName() + " " + TableColumnEnum.SIZE.getSqlType() + "," + TableColumnEnum.DATE_FIX.getColumnName() + " " + TableColumnEnum.DATE_FIX.getSqlType() + "," + TableColumnEnum.JUST_FOLDER_NAME.getColumnName() + " " + TableColumnEnum.JUST_FOLDER_NAME.getSqlType() + "," + TableColumnEnum.MAX_DATES.getColumnName() + " " + TableColumnEnum.MAX_DATES.getSqlType() + "," + TableColumnEnum.MIN_DATE.getColumnName() + " " + TableColumnEnum.MIN_DATE.getSqlType() + "," + TableColumnEnum.FULL_PATH.getColumnName() + " " + TableColumnEnum.FULL_PATH.getSqlType() + " PRIMARY KEY)";

    final private static String insertTableColumns = " (" + TableColumnEnum.DATE_DIFFERENCE_RATIO.getColumnName() + "," + TableColumnEnum.BAD_FILES.getColumnName() + "," + TableColumnEnum.COPIED.getColumnName() + "," + TableColumnEnum.FOLDER_FILES.getColumnName() + "," + TableColumnEnum.IMAGE.getColumnName() + "," + TableColumnEnum.MEDIA.getColumnName() + "," + TableColumnEnum.RAW.getColumnName() + "," + TableColumnEnum.STATUS.getColumnName() + "," + TableColumnEnum.SUGGESTED.getColumnName() + "," + TableColumnEnum.VIDEO.getColumnName() + "," + TableColumnEnum.SIZE.getColumnName() + "," + TableColumnEnum.DATE_FIX.getColumnName() + "," + TableColumnEnum.FULL_PATH.getColumnName();


    private static final String insertToTable = "INSERT OR REPLACE INTO " + SQLTableEnums.TABLES_COLS.getType() + " ('tableColumn', " + "'width')" + " VALUES(?, ?)";

    private static String buildInsertColumns(List<TableColumnEnum> columnOrder) {
        StringBuilder sql = new StringBuilder(" ( ");
        for (int i = 0; i < columnOrder.size(); i++) {
            sql.append(columnOrder.get(i).getColumnName());
            if (i < columnOrder.size() - 1) {
                sql.append(", ");
            }
        }
        return sql.toString();
    }

    private static final String insertAllColumns = " ( " + TableColumnEnum.DATE_DIFFERENCE_RATIO.getColumnName() + ", " + TableColumnEnum.BAD_FILES.getColumnName() + ", " + TableColumnEnum.COPIED.getColumnName() + ", " + TableColumnEnum.FOLDER_FILES.getColumnName() + ", " + TableColumnEnum.IMAGE.getColumnName() + ", " + TableColumnEnum.MEDIA.getColumnName() + ", " + TableColumnEnum.RAW.getColumnName() + ", " + TableColumnEnum.STATUS.getColumnName() + ", " + TableColumnEnum.SUGGESTED.getColumnName() + ", " + TableColumnEnum.VIDEO.getColumnName() + ", " + TableColumnEnum.SIZE.getColumnName() + ", " + TableColumnEnum.DATE_FIX.getColumnName() + ", " + TableColumnEnum.FULL_PATH.getColumnName() + ", " + TableColumnEnum.JUST_FOLDER_NAME.getColumnName() + ", " + TableColumnEnum.MAX_DATES.getColumnName() + ", " + TableColumnEnum.MIN_DATE.getColumnName();

    private static final String insertAllColumnsWidths = " , " + TableColumnEnum.DATE_DIFFERENCE_RATIO_WIDTH.getColumnName() + ", " + TableColumnEnum.BAD_FILES_WIDTH.getColumnName() + ", " + TableColumnEnum.COPIED_WIDTH.getColumnName() + ", " + TableColumnEnum.FOLDER_FILES_WIDTH.getColumnName() + ", " + TableColumnEnum.IMAGE_WIDTH.getColumnName() + ", " + TableColumnEnum.MEDIA_WIDTH.getColumnName() + ", " + TableColumnEnum.RAW_WIDTH.getColumnName() + ", " + TableColumnEnum.STATUS_WIDTH.getColumnName() + ", " + TableColumnEnum.SUGGESTED_WIDTH.getColumnName() + ", " + TableColumnEnum.VIDEO_WIDTH.getColumnName() + ", " + TableColumnEnum.SIZE_WIDTH.getColumnName() + ", " + TableColumnEnum.DATE_FIX_WIDTH.getColumnName() + ", " + TableColumnEnum.FULL_PATH_WIDTH.getColumnName() + ", " + TableColumnEnum.JUST_FOLDER_NAME_WIDTH.getColumnName() + ", " + TableColumnEnum.MAX_DATES_WIDTH.getColumnName() + ", " + TableColumnEnum.MIN_DATE_WIDTH.getColumnName();

    private static final String insertAllColumnValues = "0, "  // DATE_DIFFERENCE_RATIO
            + "0, "   // BAD_FILES
            + "0, "   // COPIED
            + "0, "   // FOLDER_FILES
            + "0, "   // IMAGE
            + "0, "   // MEDIA
            + "0, "   // RAW
            + "0, "   // STATUS
            + "0, "   // SUGGESTED
            + "0, "   // VIDEO
            + "0, "   // SIZE
            + "'', "  // DATE_FIX
            + "'', "  // FULL_PATH
            + "'', "  // JUST_FOLDER_NAME
            + "'', "  // MAX_DATES
            + "''";   // MIN_DATE

    private static final String insertAllColumnWidthValues = ", 0, "  // DATE_DIFFERENCE_RATIO_WIDTH
            + "0, "   // BAD_FILES_WIDTH
            + "0, "   // COPIED_WIDTH
            + "0, "   // FOLDER_FILES_WIDTH
            + "0, "   // IMAGE_WIDTH
            + "0, "   // MEDIA_WIDTH
            + "0, "   // RAW_WIDTH
            + "0, "   // STATUS_WIDTH
            + "0, "   // SUGGESTED_WIDTH
            + "0, "   // VIDEO_WIDTH
            + "0, "   // SIZE_WIDTH
            + "0, "   // DATE_FIX_WIDTH
            + "0, "   // FULL_PATH_WIDTH
            + "0, "   // JUST_FOLDER_NAME_WIDTH
            + "0, "   // MAX_DATES_WIDTH
            + "0";    // MIN_DATE_WIDTH


    private static Connection connection;

    private static String tableSortIt = "tablesortit";
    private static String tableSorted = "tablesorted";
    private static String tableAsItIs = "tableasitis";

    final private static String createTableSortIt = "CREATE TABLE IF NOT EXISTS " + tableSortIt + " " + createTableColumns;
    final private static String createTableSorted = "CREATE TABLE IF NOT EXISTS " + tableSorted + " " + createTableColumns;
    final private static String createTableAsItIs = "CREATE TABLE IF NOT EXISTS " + tableAsItIs + " " + createTableColumns;

    private static String insertTableSortIt = "INSERT OR REPLACE INTO " + tableSortIt;
    private static String insertTableSorted = "INSERT OR REPLACE INTO " + tableSorted;
    private static String insertTableAsItIs = "INSERT OR REPLACE INTO " + tableAsItIs;

    /**
     * Creates the configuration table with properties in the database.
     *
     * @return true if the configuration table is successfully created, false otherwise
     */
    public static boolean createTableColumns(String tableId) {
        connection = ConfigurationSQLHandler.getConnection();
        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("createConfigurationTable connection failed");
                return false;
            }

            Messages.sprintf("TableColumnnnnsnnssnns table ID.:: " + tableId);
            String sql = "CREATE TABLE IF NOT EXISTS " + getCorrectTableById(tableId) + " " + createTableColumns;

            Messages.sprintf("TableColumnnnnsnnssnns.:: " + sql);
            Messages.sprintf("Correct table id: " + getCorrectTableById(tableId));
            Messages.sprintf("Table columns: " + createTableColumns);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);  // Using execute() instead of batch for single statement
                SQL_Utils.commitChanges(connection);
                return true;
            }
        } catch (Exception e) {
            Messages.sprintfError("Failed to create configuration table: " + e.getMessage());
            return false;
        }
    }

    public static boolean loadTableColumns(TableView<FolderInfo> table, String tableId) {
        if (table == null) {
            Messages.sprintfError("Invalid table ID: " + tableId);
            return false;
        }

        try (Connection connection = ConfigurationSQLHandler.getConnection()) {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("loadTableColumns: Database connection failed");
                return false;
            }

            String tableName = getCorrectTableById(tableId);
            if (!SQL_Utils.isDatabaseAccessible(connection, tableName)) {
                Messages.sprintfError("Table not accessible: " + tableName);
                return false;
            }

            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM ?")) {
                stmt.setString(1, tableName);

                List<TableColumn<FolderInfo, ?>> columnsToAdd = new ArrayList<>();

                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        for (TableColumnEnum columnEnum : TableColumnEnum.values()) {
                            if (!columnEnum.getColumnName().endsWith("_width")) {
                                String columnName = columnEnum.getColumnName();
                                Object value = rs.getObject(columnName);

                                if (value != null) {
                                    TableColumn<FolderInfo, ?> column = createColumnForType(columnEnum);
                                    if (column != null) {
                                        column.setId(columnName);

                                        // Set width if width column exists
                                        String widthColumnName = columnName + "_width";
                                        try {
                                            double width = rs.getDouble(widthColumnName);
                                            if (width > 0) {
                                                column.setPrefWidth(width);
                                            }
                                        } catch (SQLException e) {
                                            column.setPrefWidth(100);
                                        }

                                        columnsToAdd.add(column);
                                    }
                                }
                            }
                        }
                    }
                }

                // Update UI in a single operation
                if (!columnsToAdd.isEmpty()) {
                    Platform.runLater(() -> table.getColumns().addAll(columnsToAdd));
                }
            }

            Messages.sprintf("Successfully loaded columns for table: " + tableId);
            return true;

        } catch (SQLException e) {
            Messages.sprintfError("SQL error loading table columns: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Messages.sprintfError("Failed to load table columns: " + e.getMessage());
            return false;
        }
    }

    public static boolean loadTableColumns_(TableView<FolderInfo> table, String tableId) {
        if (table == null) {
            Messages.sprintfError("Invalid table ID: " + tableId);
            return false;
        }

        connection = ConfigurationSQLHandler.getConnection();
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("loadTableColumns: Database connection failed");
            return false;
        }

        String tableName = getCorrectTableById(tableId);
        if (!SQL_Utils.isDatabaseAccessible(connection, tableName)) {
            Messages.sprintfError("Table not accessible: " + tableName);
            return false;
        }

        try {
            String sql = "SELECT * FROM " + tableName;
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

//                // Clear existing columns
//                Platform.runLater(()-> {
//                    table.getColumns().clear();
//                });
                // Create columns based on stored values
                // Using the FolderInfoEnum as shown in your code
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnType = metaData.getColumnTypeName(i);
                    Object value = rs.getObject(i);
                    Messages.sprintf(i + ". Column: " + columnName +
                            " (Type: " + columnType +
                            ") = " + value);
                }
                while (rs.next()) {
                    Messages.sprintf("Loading table column: " + rs.getString("folderFiles"));
                    for (TableColumnEnum columnEnum : TableColumnEnum.values()) {
                        Messages.sprintf("Loading column: " + columnEnum.getColumnName());
                        if (!columnEnum.getColumnName().endsWith("_width")) {
                            String columnName = columnEnum.getColumnName();
                            Object value = rs.getObject(columnName);
                            if (value != null) {
                                TableColumn<FolderInfo, ?> column = createColumnForType(columnEnum);
                                if (column != null) {
                                    column.setId(columnName);

                                    // Set width if width column exists
                                    String widthColumnName = columnName + "_width";
                                    try {
                                        double width = rs.getDouble(widthColumnName);
                                        if (width > 0) {
                                            column.setPrefWidth(width);
                                        }
                                    } catch (SQLException e) {
                                        // Width column might not exist, use default width
                                        column.setPrefWidth(100);
                                    }
                                    Platform.runLater(()-> {
                                        table.getColumns().add(column);
                                    });
                                }
                            }
                        }
                    }
                }

                Messages.sprintf("Successfully loaded columns for table: " + tableId);
                return true;

            }
        } catch (Exception e) {
            Messages.sprintfError("Failed to load table columns: " + e.getMessage());
            return false;
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }

    private static TableColumn<FolderInfo, ?> createColumnForType(TableColumnEnum columnEnum) {
        TableColumn<FolderInfo, ?> column = new TableColumn<>(columnEnum.getColumnName());

        switch (columnEnum.getSqlType()) {
            case "DOUBLE":
                TableColumn<FolderInfo, Double> doubleColumn = new TableColumn<>(columnEnum.getColumnName());
                doubleColumn.setCellValueFactory(new PropertyValueFactory<>(columnEnum.getColumnName()));
                return doubleColumn;

            case "INTEGER":
                TableColumn<FolderInfo, Integer> intColumn = new TableColumn<>(columnEnum.getColumnName());
                intColumn.setCellValueFactory(new PropertyValueFactory<>(columnEnum.getColumnName()));
                return intColumn;

            case "BOOLEAN":
                TableColumn<FolderInfo, Boolean> boolColumn = new TableColumn<>(columnEnum.getColumnName());
                boolColumn.setCellValueFactory(new PropertyValueFactory<>(columnEnum.getColumnName()));
                return boolColumn;

            case "TEXT":
                TableColumn<FolderInfo, String> textColumn = new TableColumn<>(columnEnum.getColumnName());
                textColumn.setCellValueFactory(new PropertyValueFactory<>(columnEnum.getColumnName()));
                return textColumn;

            default:
                Messages.sprintfError("Unknown column type: " + columnEnum.getSqlType());
                return null;
        }
    }

    public static boolean createTableWidthColumns() {
        connection = ConfigurationSQLHandler.getConnection();
        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintfError("createConfigurationTable connection failed");
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

//    public static boolean loadTableColumns(Tables tables, String tableId) {
//        connection = ConfigurationSQLHandler.getConnection();
//
//
//        if (tableId.equals(TableType.SORTIT.getType())) {
//            String sql = "SELECT * FROM " + "tablesortit";
//            PreparedStatement pstmt;
//            try {
//                pstmt = connection.prepareStatement(sql);
//                ResultSet rs = pstmt.executeQuery();
//                for (TableColumn<?, ?> column : tables.getSortIt_table().getColumns()) {
//                    while (rs.next()) {
//                        String tc = rs.getString("tableColumn");
//                        Messages.sprintf("Loading tablecolumn: " + column + " ID: " + column.getId());
//                        double width = rs.getDouble("width");
//                        column.setPrefWidth(width);
//                    }
//                }
//            } catch (Exception e) {
//                Messages.sprintfError("Failed to load table columns: " + e.getMessage());
//            }
//        } else if (tableId.equals(TableType.SORTED.getType())) {
//
//        } else if (tableId.equals(TableType.ASITIS.getType())) {
//
//        }
//        return false;
//    }

    private static String getInsertTableNameById(String tableId) {
        if (tableId.equalsIgnoreCase(TableType.SORTED.getType())) {
            Messages.sprintf("1Inserting table to id: " + tableId + " " + insertTableSorted);
            return insertTableSorted;
        } else if (tableId.equalsIgnoreCase(TableType.SORTIT.getType())) {
            Messages.sprintf("2Inserting table to id: " + tableId + " " + insertTableSortIt);
            return insertTableSortIt;
        } else if (tableId.equalsIgnoreCase(TableType.ASITIS.getType())) {
            Messages.sprintf("3Inserting table to id: " + tableId + " " + insertTableAsItIs);
            return insertTableAsItIs;
        } else {
            Messages.sprintfError("Unknown tableId: " + tableId);
            return "";
        }
    }

    public static boolean insertTableColumns(TableView<FolderInfo> table) {
        connection = ConfigurationSQLHandler.getConnection();
        Messages.sprintf("insertTableColumns Inserting table columns for: " + table.getId());
        boolean tableSortItDatabaseAccessible = SQL_Utils.isDatabaseAccessible(connection, tableSortIt);
        boolean tableSortedDatabaseAccessible = SQL_Utils.isDatabaseAccessible(connection, tableSorted);
        boolean tableAsItIsDatabaseAccessible = SQL_Utils.isDatabaseAccessible(connection, tableAsItIs);
        if (!tableSortItDatabaseAccessible) {
            Messages.sprintfError("tableSortItDatabaseAccessible Could not access database: " + tableSortIt);
            createTableColumns(table.getId());
        } else if (!tableSortedDatabaseAccessible) {
            Messages.sprintfError("tableSortedDatabaseAccessible Could not access database: " + tableSorted);
            createTableColumns(table.getId());
        } else if (!tableAsItIsDatabaseAccessible) {
            Messages.sprintfError("tableAsItIsDatabaseAccessible Could not access database: " + tableAsItIs);
            createTableColumns(table.getId());
        }

//        if (!tableSortItDatabaseAccessible && !tableSortedDatabaseAccessible && !tableAsItIsDatabaseAccessible) {
//            Messages.sprintfError("Could not access any database: " + tableSortIt + ", " + tableSorted + ", " + tableAsItIs);
//        }

        try {
            // Build the insert statement directly
            StringBuilder sql = new StringBuilder("INSERT OR REPLACE INTO " + getCorrectTableById(table.getId()) + " " + insertTableColumns + " VALUES ");
            //INSERT INTO tablesortit
            //(dateDifference_ratio, badFiles, copied, folderFiles, image, media, raw, status, suggested, video, "size", dateFix, justFolderName, maxDates, minDate, fullPath)
            //VALUES(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '', '', '', '', '');
            Messages.sprintf("Table id is: " + table.getId());
            List<String> values = new ArrayList<>();
            int order = 0;
            for (TableColumn<FolderInfo, ?> column : table.getColumns()) {
                values.add(String.format("('%s', %d)", column.getId().toLowerCase(), order++));
                Messages.sprintf("Processing column: " + column.getText() + " ID: " + column.getId() + " width: " + column.getWidth() + " order: " + order);
            }

            sql.append(String.join(",", values));
            Messages.sprintf("FULL sql string: " + sql.toString());

            // Create table if it doesn't exist first
            String createTableSql = "CREATE TABLE IF NOT EXISTS " + getCorrectTableById(table.getId()) + " (column_name TEXT, column_order INTEGER)";
            Messages.sprintf("===========Creating table: " + createTableSql);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSql);

                try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                    pstmt.executeUpdate();
                    Messages.sprintf("Successfully inserted columns for table: " + table.getId());
                    SQL_Utils.commitChanges(connection);
                    return true;
                }
            }
        } catch (Exception e) {
            Messages.sprintfError("Could not insert table columns: " + e.getMessage());
            return false;
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }
//    public static boolean insertTableColumns(TableView<FolderInfo> table) {
//        connection = ConfigurationSQLHandler.getConnection();
//        createTableById(connection, table.getId());
//        try {
//            try (PreparedStatement pstmt = connection.prepareStatement(insertAll)) {
//                insertToTableView(pstmt, table);
//                SQL_Utils.commitChanges(connection);
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            SQL_Utils.closeConnection(connection);
//        }
//    }

    private static boolean createTableById(String tableId) {
        connection = ConfigurationSQLHandler.getConnection();
        String schema = getCorrectTableById(tableId);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(schema);
            stmt.close();
            SQL_Utils.commitChanges(ConfigurationSQLHandler.getConnection());
            //  SQL_Utils.closeConnection(ConfigurationSQLHandler.getConnection());
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Could not create TableById: " + e.getMessage());
            return false;
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

//
//    public static boolean insertAllTableColumns(Tables tables) {
//        connection = ConfigurationSQLHandler.getConnection();
//        try {
//            try (PreparedStatement pstmt = connection.prepareStatement(insertAll)) {
//                insertColumnForSortingToTableView(pstmt, tables.getSortIt_table());
//                insertColumnForSortingToTableView(pstmt, tables.getSorted_table());
//                insertColumnForSortingToTableView(pstmt, tables.getAsItIs_table());
//                SQL_Utils.commitChanges(connection);
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            SQL_Utils.closeConnection(connection);
//        }
//    }


    private static void insertColumnForSortingToTableView(PreparedStatement pstmt, TableView<FolderInfo> table) throws SQLException {
        for (TableColumn<?, ?> column : table.getColumns()) {
            int paramIndex = 1;

            // Date Difference Ratio
            pstmt.setDouble(paramIndex++, Double.parseDouble(column.getText()));

            // Bad Files
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Copied
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Folder Files
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Image
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Media
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Raw
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Status
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Suggested
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Video
            pstmt.setInt(paramIndex++, Integer.parseInt(column.getText()));

            // Size
            pstmt.setLong(paramIndex++, Long.parseLong(column.getText()));

            // DateFix
            pstmt.setString(paramIndex++, column.getText());

            // FullPath
            pstmt.setString(paramIndex++, column.getText());

            // JustFolderName
            pstmt.setString(paramIndex++, column.getText());

            // MaxDates
            pstmt.setString(paramIndex++, column.getText());

            // MinDate
            pstmt.setString(paramIndex++, column.getText());

            pstmt.executeUpdate();

            Messages.sprintf("COLUMN NAME: " + column.getText());
            Messages.sprintf("WIDTH: " + column.getWidth());
        }
    }

    private static void insertWidthsToTableView(PreparedStatement pstmt, TableView<FolderInfo> table) throws SQLException {
        for (TableColumn<?, ?> column : table.getColumns()) {
            int paramIndex = 1;

            // Date Difference Ratio
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Bad Files
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Copied
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Folder Files
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Image
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Media
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Raw
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Status
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Suggested
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Video
            pstmt.setDouble(paramIndex++, column.getWidth());

            // Size
            pstmt.setDouble(paramIndex++, column.getWidth());

            // DateFix
            pstmt.setDouble(paramIndex++, column.getWidth());

            // FullPath
            pstmt.setDouble(paramIndex++, column.getWidth());

            // JustFolderName
            pstmt.setDouble(paramIndex++, column.getWidth());

            // MaxDates
            pstmt.setDouble(paramIndex++, column.getWidth());

            // MinDate
            pstmt.setDouble(paramIndex++, column.getWidth());

            pstmt.executeUpdate();

            Messages.sprintf("COLUMN NAME: " + column.getText());
            Messages.sprintf("WIDTH: " + column.getWidth());
        }
    }


    public enum TableColumnEnum {
        CONNECTED("connected", "BOOLEAN"),

        DATE_DIFFERENCE_RATIO("dateDifference_ratio", "DOUBLE"), DATE_DIFFERENCE_RATIO_WIDTH("dateDifference_ratio_width", "DOUBLE"),

        BAD_FILES("badFiles", "INTEGER"), BAD_FILES_WIDTH("badFiles_width", "DOUBLE"),

        COPIED("copied", "INTEGER"), COPIED_WIDTH("copied_width", "DOUBLE"),

        FOLDER_FILES("folderFiles", "INTEGER"), FOLDER_FILES_WIDTH("folderFiles_width", "DOUBLE"),

        IMAGE("image", "INTEGER"), IMAGE_WIDTH("image_width", "DOUBLE"),

        MEDIA("media", "INTEGER"), MEDIA_WIDTH("media_width", "DOUBLE"),

        RAW("raw", "INTEGER"), RAW_WIDTH("raw_width", "DOUBLE"),

        STATUS("status", "INTEGER"), STATUS_WIDTH("status_width", "DOUBLE"),

        SUGGESTED("suggested", "INTEGER"), SUGGESTED_WIDTH("suggested_width", "DOUBLE"),

        VIDEO("video", "INTEGER"), VIDEO_WIDTH("video_width", "DOUBLE"),

        SIZE("size", "BIGINT"), SIZE_WIDTH("size_width", "DOUBLE"),

        DATE_FIX("dateFix", "TEXT"), DATE_FIX_WIDTH("dateFix_width", "DOUBLE"),

        FULL_PATH("fullPath", "TEXT"), FULL_PATH_WIDTH("fullPath_width", "DOUBLE"),

        JUST_FOLDER_NAME("justFolderName", "TEXT"), JUST_FOLDER_NAME_WIDTH("justFolderName_width", "DOUBLE"),

        MAX_DATES("maxDates", "TEXT"), MAX_DATES_WIDTH("maxDates_width", "DOUBLE"),

        MIN_DATE("minDate", "TEXT"), MIN_DATE_WIDTH("minDate_width", "DOUBLE");

        private final String columnName;
        private final String sqlType;

        TableColumnEnum(String columnName, String sqlType) {
            this.columnName = columnName;
            this.sqlType = sqlType;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getSqlType() {
            return sqlType;
        }

        // Updated utility method to rebuild the table creation SQL including width columns
        public static String getTableColumns() {
            Messages.sprintf("Creating table columns SQL...");
            StringBuilder sb = new StringBuilder(" ( ");
            for (TableColumnEnum column : values()) {
                Messages.sprintf("Adding column: " + column.getColumnName());
                // Skip the width columns as they are added next to their main columns
                if (!column.getColumnName().endsWith("_width")) {
                    sb.append(column.getColumnName()).append(" ").append(column.getSqlType()).append(",");

                    // Find and add the corresponding width column
                    String widthColumnName = column.getColumnName() + "_width";
                    for (TableColumnEnum widthColumn : values()) {
                        if (widthColumn.getColumnName().equals(widthColumnName)) {
                            sb.append(widthColumn.getColumnName()).append(" ").append(widthColumn.getSqlType()).append(",");
                            break;
                        }
                    }
                }
            }
            // Add primary key
            sb.append("PRIMARY KEY (").append(FULL_PATH.getColumnName()).append("))");
            Messages.sprintf("Creating table columns SQL DONE!!!!!!!!!!!!!!!");
            return sb.toString();
        }
    }
}