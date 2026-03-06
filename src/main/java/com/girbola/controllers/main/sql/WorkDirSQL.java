package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.operate.CopyState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.utils.FileInfoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.girbola.Main.simpleDates;

public class WorkDirSQL {

    private Connection connection;

    public Connection getConnection() {
        return this.connection;
    }

    public WorkDirSQL(Path workDirPath) {
        if (Files.exists(workDirPath)) {
            System.out.println("WorkDirSQL: " + workDirPath.toString() + " exists");
            this.connection = createWorkDirConnection(workDirPath);
        } else {
            loadWorkDir();
            System.err.println("Loading the content: " + workDirPath.toString());
        }
    }

    private Connection createWorkDirConnection(Path workDirPath) {
        try {
            connection = SqliteConnection.connectToDatabase(workDirPath, Main.conf.getWorkDir_db_fileName());
            SQL_Utils.setAutoCommit(connection, false);
            if (SQL_Utils.isDbConnected(connection)) {
                createFileInfoTable(connection);
                return connection;
            }
        } catch (Exception ex) {
            Messages.sprintfError("Error connecting to database: " + Main.conf.getWorkDir_db_fileName() + " ex: " + ex.getMessage());
            return null;
        }
        return null;
    }

    public boolean createFileInfoTable(Connection connection) {
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("Database connection is not active. Aborting the operation. Path is?" + SQL_Utils.getUrl(connection));
            return false;
        }

        final String sql = FileInfoEnum.getCreateTableSQL(SQLTableEnums.FILEINFO.getType());
        System.out.println("SQLLLRLERLGELR::::::: " + sql);
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);

            // Ensure all columns exist (migration for existing databases)
            ensureAllFileInfoColumnsExist(connection);

            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Cannot create table: " + sql);
            return false;
        }
    }

    private void ensureAllFileInfoColumnsExist(Connection connection) {
        String fileInfoTable = SQLTableEnums.FILEINFO.getType();

        try (Statement stmt = connection.createStatement()) {
            String[] allColumns = FileInfoEnum.getAllFileInfoColumnNames().split(",");
            for (String columnDef : allColumns) {
                try {
                    String alterTableSQL = "ALTER TABLE " + fileInfoTable + " ADD COLUMN " + columnDef;
                    stmt.executeUpdate(alterTableSQL);
                } catch (SQLException e) {
                    // Column already exists, which is fine
                    if (!e.getMessage().toLowerCase().contains("duplicate column") &&
                            !e.getMessage().toLowerCase().contains("already exists")) {
                        Messages.sprintf("Column migration note: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error ensuring FileInfo columns exist: " + e.getMessage());
        }
    }

    public void checkConnection() {
        try {
            // Check if the connection is valid
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("Configuration database closed. Creating new connection");
                connection = createWorkDirConnection(Main.conf.getAppDataPath());
                if (connection == null) {
                    Messages.sprintfError("Failed to create new configuration connection");
                    return;
                }
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
            }

            // Ensure AutoCommit is disabled
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }

        } catch (SQLException e) {
            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
        }
    }

    public void closeConnection() {
        SQL_Utils.closeConnection(connection);
    }

    public List<FileInfo> findDuplicatesByDateRange(String startDate, String endDate) {
        List<FileInfo> duplicates = new ArrayList<>();
        String sql = "SELECT * FROM " + SQLTableEnums.WORKDIR.getType() + " WHERE date_range BETWEEN ? AND ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Map ResultSet to FileInfo objects and add to duplicates list
                    // Implementation depends on your FileInfo class structure
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error finding duplicates: " + e.getMessage());
            // Consider throwing a custom exception or handling the error appropriately
        }

        return duplicates;
    }


    public void loadWorkDir() {
        getConnection();
    }

    public void ensureFileInfoTable() {
        FileInfo_SQL.createFileInfoTable(connection);
    }


    public void insertFileInfoToWorkDir(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return;
        }

        try {
            // Try to get or create connection if needed
            if (SQL_Utils.isDbConnected(connection) && !connection.isClosed()) {

                boolean fileInfoTable = FileInfo_SQL.createFileInfoTable(connection);
                if (!fileInfoTable) {
                    Messages.sprintfError("Could not create fileinfo workdir table");
                    return;
                }
                // Verify connection is valid
                if (!SQL_Utils.isDbConnected(connection)) {
                    throw new SQLException("Database connection validation failed");
                }
            }

            int columnCount = 0;
            // Get the length of the column names
            for (FileInfoEnum e : FileInfoEnum.values()) {
                if (e != FileInfoEnum.ORGPATH) { // Adjust based on your needs
                    columnCount++;
                }
            }

            List<String> allValuesSplitted = Arrays.asList(FileInfoEnum.getAllFileInfoColumnNames().split(","));
            StringBuilder valuePlaceholders = new StringBuilder();
            for (String placeHolder : allValuesSplitted) {
                if (valuePlaceholders.length() > 0) {
                    valuePlaceholders.append(',');
                }
                valuePlaceholders.append("?");
            }

            final String sql = "INSERT INTO " + SQLTableEnums.FILEINFO.getType()
                    + " (" + FileInfoEnum.getAllFileInfoColumnNames() + ") "
                    + "VALUES (" + valuePlaceholders + ")";

            SQL_Utils.setAutoCommit(connection, false);

            System.out.println("sql:::::::: " + sql);
            System.out.println(" fileInfo: " + fileInfo.showAllValues());
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int index = 1;

            // Update values
            setFileInfoParameters(pstmt, fileInfo, index);

            pstmt.executeUpdate();
            Messages.sprintf("FileInfo inserted/updated successfully");
        } catch (SQLException e) {
            String orgPath = fileInfo.getOrgPath();

            Messages.sprintfError("1111Error inserting/updating FileInfo: " + e.getMessage() + " orgPath::: " + orgPath);
        }
    }

    public CopyState findDuplicates(FileInfo fileInfo) {

        findDuplicatesByDateRange(simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()), simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()));

        return CopyState.COPY; // Default state, should be replaced with actual logic to determine the state
    }

    public List<FileInfo> findDuplicateByExactDate(FileInfo fileInfo) throws SQLException {
        if (fileInfo == null) {
            return new ArrayList<>();
        }

        // If connection failed or database doesn't exist, return empty list
        if (connection == null || !SQL_Utils.isDbConnected(connection)) {
            Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
            return new ArrayList<>();
        }

        // If table creation fails, return empty list
        if (!createFileInfoTable(connection)) {
            Messages.sprintf("Failed to create FileInfo table in: " + Main.conf.getWorkDir_db_fileName());
            return new ArrayList<>();
        }

        List<FileInfo> list = new ArrayList<>();

        boolean empty = isTableEmpty(connection, SQLTableEnums.WORKDIR.getType());
        if (!empty) {
            Messages.sprintf("findDuplicateByExactDate: " + fileInfo.getOrgPath() + " empty? ");

            String sql = "SELECT " + FileInfoEnum.getAllFileInfoColumnNames() +
                    " FROM " + SQLTableEnums.WORKDIR.getType() +
                    " WHERE orgPath = ? AND size = ? AND localDateTime = ? AND imageDifferenceHash = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, fileInfo.getOrgPath());
                pstmt.setLong(2, fileInfo.getSize());
//            pstmt.setObject(3, fileInfo.getLocalDateTime());
                pstmt.setString(3, fileInfo.getImageDifferenceHash());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        FileInfo duplicateFileInfo = FileInfo_SQL.loadFileInfo(rs);
                        if (duplicateFileInfo != null && FileInfoUtils.compareImagesMetadata(fileInfo, duplicateFileInfo)) {
                            list.add(duplicateFileInfo);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                Messages.sprintfError("Error finding duplicate FileInfo: " + e.getMessage() + " line nubmer: " + Misc.getLineNumber());
                return new ArrayList<>();
            }

            return list;
        }
        Messages.sprintf("findDuplicateByExactDate: " + fileInfo.getOrgPath() + " empty? ");
        return new ArrayList<>();
    }


    public static boolean isTableEmpty(Connection conn, String tableName) {
        if (conn == null || tableName == null || tableName.trim().isEmpty()) {
            return true;
        }
        if (SQL_Utils.isDbConnected(conn)) {
            String sql = "SELECT 1 FROM " + tableName + " LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            } catch (SQLException e) {
                // If the table doesn't exist or connection is dead,
                // we treat it as having no data.
                return true;
            }
        }
        return true;
    }
    //@formatter:on


    public boolean insertFileInfo(FileInfo fileInfo) {
        Messages.sprintf("insertFileInfo starting: " + fileInfo);
        if (fileInfo == null) {
            Messages.warningText("Cannot insert null FileInfo");
            return false;
        }

        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("Database connection failed for: " + Main.conf.getWorkDir());
                return false;
            }
            String sql = FileInfo_SQL.createFileInfoTable();
            PreparedStatement pstmt = connection.prepareStatement(sql);

            int index = 1;

            /*
fileInfo.getDestination_Path() (for DESTINATIONPATH)
fileInfo.getFileHistories() (for FILEHISTORIES - needs to be converted to String)
             */
            // Update values
            // Update values
            setFileInfoParameters(pstmt, fileInfo, index);

            pstmt.addBatch();
            Messages.sprintf("FileInfo added to batch");
            return true;
        } catch (SQLException e) {
            Messages.sprintfError("4444Error inserting/updating FileInfo: " + e.getMessage());
            return false;
        }
    }


    private int setFileInfoParameters(PreparedStatement pstmt, FileInfo fileInfo, int index) throws SQLException {
        // Must match the order in FileInfoEnum.values()
        // BAD, CAMERA_MODEL, CONFIRMED, DATE, DESTINATION_PATH, EVENT, FILEINFO_ID, FILEHISTORIES,
        // GOOD, COPIED, IGNORED, IMAGE, IMAGE_DIFFERENCE_HASH, LOCATION, MODIFIED, ORGPATH,
        // ORIENTATION, RAW, SIZE, SUGGESTED, TABLE_DUPLICATED, TAGS, THUMB_LENGTH, THUMB_OFFSET,
        // TIME_SHIFT, USER, VIDEO, WORK_DIR, WORK_DIR_DRIVE_SERIAL_NUMBER

        pstmt.setBoolean(index++, fileInfo.isBad());                        // 1 - BAD
        pstmt.setString(index++, fileInfo.getCamera_model());               // 2 - CAMERA_MODEL
        pstmt.setBoolean(index++, fileInfo.isConfirmed());                  // 3 - CONFIRMED
        pstmt.setString(index++, fileInfo.getDestination_Path());           // 4 - DESTINATION_PATH
        pstmt.setString(index++, fileInfo.getEvent());                      // 5 - EVENT
        pstmt.setInt(index++, fileInfo.getFileInfo_id());                   // 6 - FILEINFO_ID
        String historiesStr = String.join("|", fileInfo.getFileHistories());
        pstmt.setString(index++, historiesStr);                             // 7 - FILEHISTORIES
        pstmt.setBoolean(index++, fileInfo.isGood());                       // 8 - GOOD
        pstmt.setBoolean(index++, fileInfo.isCopied());                     // 9 - COPIED
        pstmt.setBoolean(index++, fileInfo.isIgnored());                    // 10 - IGNORED
        pstmt.setBoolean(index++, fileInfo.isImage());                      // 11 - IMAGE
        pstmt.setString(index++, fileInfo.getImageDifferenceHash());        // 11 - IMAGE_DIFFERENCE_HASH
        pstmt.setString(index++, fileInfo.getLocation());                   // 12 - LOCATION
        pstmt.setBoolean(index++, fileInfo.isModified());                   // 13 - MODIFIED
        pstmt.setString(index++, fileInfo.getOrgPath());                    // 14 - ORGPATH
        pstmt.setInt(index++, fileInfo.getOrientation());                   // 15 - ORIENTATION
        pstmt.setBoolean(index++, fileInfo.isRaw());                        // 16 - RAW
        pstmt.setLong(index++, fileInfo.getSize());                         // 17 - SIZE
        pstmt.setBoolean(index++, fileInfo.isSuggested());                  // 18 - SUGGESTED
        pstmt.setBoolean(index++, fileInfo.isTableDuplicated());            // 19 - TABLE_DUPLICATED
        pstmt.setString(index++, fileInfo.getTags());                       // 20 - TAGS
        pstmt.setInt(index++, fileInfo.getThumb_length());                  // 21 - THUMB_LENGTH
        pstmt.setInt(index++, fileInfo.getThumb_offset());                  // 22 - THUMB_OFFSET
        pstmt.setLong(index++, fileInfo.getTimeShift());                    // 23 - TIME_SHIFT
        pstmt.setString(index++, fileInfo.getUser());                       // 24 - USER
        pstmt.setBoolean(index++, fileInfo.isVideo());                      // 25 - VIDEO
        pstmt.setString(index++, fileInfo.getWorkDir());                    // 26 - WORK_DIR
        pstmt.setString(index++, fileInfo.getWorkDirDriveSerialNumber());   // 27 - WORK_DIR_DRIVE_SERIAL_NUMBER

        return index;
    }
}