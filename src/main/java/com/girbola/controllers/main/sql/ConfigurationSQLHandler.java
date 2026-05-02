package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.configuration.Configuration;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.persistence.drive.DriveInfoDao;
import com.girbola.sql.SQL_Utils;
import java.util.List;
import java.nio.file.Path;
import java.sql.*;

import static com.girbola.controllers.main.sql.ConfigurationEnum.*;

public class ConfigurationSQLHandler extends DriveInfoDao {

    private static final String ERROR = ConfigurationSQLHandler.class.getName();
//    public static final String ID = "id";

    private static Connection connection;

    private static int configuration_id = 0;

//    public static final String BETTER_THUMBNAIL_QUALITY = "betterQualityThumbs";
//    public static final String CONFIRM_ON_EXIT = "confirmOnExit";
//    public static final String ID_COUNTER = "id_counter";
//    public static final String IMAGE_VIEW_X_POSITION = "imageViewXPos";
//    public static final String IMAGE_VIEW_Y_POSITION = "imageViewYPos";
//    public static final String SAVE_DATA_AS_HD = "saveDataToHD";
//    public static final String SHOW_FULL_PATH = "showFullPath";
//    public static final String SHOW_HINTS = "showHints";
//    public static final String SHOW_TOOLTIPS = "showTooltips";
//
//    public static final String CURRENTTHEME = "currentTheme";
//    public static final String VLC_PATH = "vlcPath";
//    public static final String VLC_SUPPORT = "vlcSupport";
//    public static final String WINDOW_START_HEIGTH = "windowStartHeight";
//    public static final String WINDOW_START_POSITION_X = "windowStartPosX";
//    public static final String WINDOW_START_POSITION_Y = "windowStartPosY";
//    public static final String WINDOW_START_WIDTH = "windowStartWidth";
//    public static final String WORK_DIR = "workDir";
//    public static final String WORK_DIR_SERIAL_NUMBER = "workDirSerialNumber";

    public static boolean checkConnection() {
        try {
            // Check if the connection is valid
            if (!SQL_Utils.isDbConnected(connection)) {
                connection = SQL_Utils.createConfigurationConfig();
                if (connection == null) {
                    Messages.sprintfError("Failed to create new configuration connection");
                    return false;
                }
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
            }

            // Ensure AutoCommit is disabled
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }

            return true;
        } catch (SQLException e) {
            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void updateConfiguration() {
        Connection localConnection = null;
        try {
            localConnection = getConnection();
            if (localConnection == null) {
                Messages.sprintfError("Could not establish database connection");
                return;
            }

            // Set busy timeout
            try (Statement stmt = localConnection.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 30000");
            }

            localConnection.setAutoCommit(false);

            // Ensure all columns exist
            ensureAllColumnExists(localConnection);

            // Insert configuration
            if (insertConfiguration(localConnection, Main.conf)) {
                localConnection.commit();
            } else {
                Messages.sprintfError("Rolling back configuration");
                localConnection.rollback();
            }
        } catch (Exception e) {
            if (localConnection != null) {
                try {
                    Messages.sprintfError("No connection to configuration. Rolling back configuration");
                    localConnection.rollback();
                } catch (SQLException ex) {
                    Messages.sprintfError("Error rolling back configuration stacktrace: " + ex);
                    ex.printStackTrace();
                }
            }
        } finally {
            if (localConnection != null) {
                try {
                    localConnection.close();
                } catch (SQLException e) {

                    if(!SQL_Utils.isDbConnected(localConnection)){
                        Messages.sprintfError("Cannot close connection. " +  e.getMessage());
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean insertConfiguration(Connection conn, Configuration configuration) {
        Messages.sprintf("Inserting configuration");

        boolean dbConnected = SQL_Utils.isDbConnected(conn);

        if (!dbConnected) {
            boolean configurationDatabase = createConfigurationDatabase();
            if(!configurationDatabase) {
                return false;
            }
        }

        StringBuilder questionMarks = new StringBuilder();

        for (String col : ConfigurationEnum.getAllColumnNames().split(",")) {
            Messages.sprintf("col: " + col);
            if (questionMarks.toString().length() == 0) {
                questionMarks.append("?");
            } else {
                questionMarks.append(",?");
            }
        }
        String sql = "INSERT OR REPLACE INTO " + SQLTableEnums.CONFIGURATION.getType() +
                " (" + String.join(", ", getAllColumnNames()) + ") " +
                "VALUES(" + questionMarks + ")";

        Messages.sprintf("insertConfiguration: " + sql);
//        int index = 1;
//        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setInt(index++, 0);
//            Messages.sprintf("index++ ID: " + 0);
//            pstmt.setBoolean(index++, configuration.isBetterQualityThumbs());
//            Messages.sprintf("index++ BETTER_THUMBNAIL_QUALITY: " + configuration.isBetterQualityThumbs() + " " + index++);
//            pstmt.setBoolean(index++, configuration.isConfirmOnExit());
//            Messages.sprintf("index++ CONFIRM_ON_EXIT: " + configuration.isConfirmOnExit() + " :" + index++);
//            pstmt.setInt(index++, configuration.getId_counter().get());
//            Messages.sprintf("index++ ID_COUNTER: " + configuration.getId_counter().get() + " :" + index++);
//            pstmt.setBoolean(index++, configuration.isShowFullPath());
//            Messages.sprintf("index++ SHOW_FULL_PATH: " + configuration.isShowFullPath());
//            pstmt.setBoolean(index++, configuration.isShowHints());
//            Messages.sprintf("index++ SHOW_HINTS: " + configuration.isShowHints());
//            pstmt.setBoolean(index++, configuration.isShowTooltips());
//            Messages.sprintf("index++ SHOW_TOOLTIPS: " + configuration.isShowTooltips());
//            pstmt.setString(index++, configuration.getCurrentTheme());
//            Messages.sprintf("index++ CURRENTTHEME: " + configuration.getCurrentTheme());
//            pstmt.setString(index++, configuration.getVlcPath());
//            Messages.sprintf("index++ VLC_PATH: " + configuration.getVlcPath());
//            pstmt.setBoolean(index++, configuration.isVlcSupport());
//            Messages.sprintf("index++ VLC_SUPPORT: " + configuration.isVlcSupport());
//            pstmt.setBoolean(index++, configuration.isSaveDataToHD());
//            Messages.sprintf("index++ SAVE_DATA_AS_HD: " + configuration.isSaveDataToHD());
//            pstmt.setDouble(index++, configuration.getWindowStartPosX());
//            Messages.sprintf("index++ WINDOW_START_POSITION_X: " + configuration.getWindowStartPosX());
//            pstmt.setDouble(index++, configuration.getWindowStartPosY());
//            Messages.sprintf("index++ WINDOW_START_POSITION_Y: " + configuration.getWindowStartPosY());
//            pstmt.setDouble(index++, configuration.getWindowStartWidth());
//            Messages.sprintf("index++ WINDOW_START_WIDTH: " + configuration.getWindowStartWidth());
//            pstmt.setDouble(index++, configuration.getWindowStartHeight());
//            Messages.sprintf("index++ WINDOW_START_HEIGHT: " + configuration.getWindowStartHeight());
//            pstmt.setDouble(index++, configuration.getImageViewXPosition());
//            if(configuration.getImageViewXPosition() == null) {
//                configuration.setImageViewXProperty(0);
//            }
//            Messages.sprintf("index++ IMAGE_VIEW_X_POSITION: " + configuration.getImageViewXPosition());
//            pstmt.setDouble(index++, configuration.getImageViewYPosition());
//            Messages.sprintf("index++ IMAGE_VIEW_Y_POSITION: " + configuration.getImageViewYPosition());
//            pstmt.setString(index++, configuration.getWorkDirSerialNumber());
//            Messages.sprintf("index++ WORK_DIR_SERIAL_NUMBER: " + configuration.getWorkDirSerialNumber());
//            pstmt.setString(index++, configuration.getWorkDir());
//
//            int result = pstmt.executeUpdate();
//            conn.commit();
//            return result > 0;
//        } catch (SQLException e) {
//            Messages.sprintfError("Error inserting configuration: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
        Messages.sprintf("insertConfiguration: " + sql);
        int index = 1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int p;

            p = index; pstmt.setInt(index++, 0);
            Messages.sprintf("index " + p + " ID: 0");

            p = index; pstmt.setBoolean(index++, configuration.isBetterQualityThumbs());
            Messages.sprintf("index " + p + " BETTER_THUMBNAIL_QUALITY: " + configuration.isBetterQualityThumbs());

            p = index; pstmt.setBoolean(index++, configuration.isConfirmOnExit());
            Messages.sprintf("index " + p + " CONFIRM_ON_EXIT: " + configuration.isConfirmOnExit());

            p = index; pstmt.setInt(index++, configuration.getId_counter().get());
            Messages.sprintf("index " + p + " ID_COUNTER: " + configuration.getId_counter().get());

            p = index; pstmt.setBoolean(index++, configuration.isShowFullPath());
            Messages.sprintf("index " + p + " SHOW_FULL_PATH: " + configuration.isShowFullPath());

            p = index; pstmt.setBoolean(index++, configuration.isShowHints());
            Messages.sprintf("index " + p + " SHOW_HINTS: " + configuration.isShowHints());

            p = index; pstmt.setBoolean(index++, configuration.isShowTooltips());
            Messages.sprintf("index " + p + " SHOW_TOOLTIPS: " + configuration.isShowTooltips());

            p = index; pstmt.setString(index++, configuration.getCurrentTheme());
            Messages.sprintf("index " + p + " CURRENTTHEME: " + configuration.getCurrentTheme());

            p = index; pstmt.setString(index++, configuration.getVlcPath());
            Messages.sprintf("index " + p + " VLC_PATH: " + configuration.getVlcPath());

            p = index; pstmt.setBoolean(index++, configuration.isVlcSupport());
            Messages.sprintf("index " + p + " VLC_SUPPORT: " + configuration.isVlcSupport());

            p = index; pstmt.setBoolean(index++, configuration.isSaveDataToHD());
            Messages.sprintf("index " + p + " SAVE_DATA_AS_HD: " + configuration.isSaveDataToHD());

            p = index; pstmt.setDouble(index++, configuration.getWindowStartPosX());
            Messages.sprintf("index " + p + " WINDOW_START_POSITION_X: " + configuration.getWindowStartPosX());

            p = index; pstmt.setDouble(index++, configuration.getWindowStartPosY());
            Messages.sprintf("index " + p + " WINDOW_START_POSITION_Y: " + configuration.getWindowStartPosY());

            p = index; pstmt.setDouble(index++, configuration.getWindowStartWidth());
            Messages.sprintf("index " + p + " WINDOW_START_WIDTH: " + configuration.getWindowStartWidth());

            p = index; pstmt.setDouble(index++, configuration.getWindowStartHeight());
            Messages.sprintf("index " + p + " WINDOW_START_HEIGHT: " + configuration.getWindowStartHeight());

            p = index; pstmt.setDouble(index++, configuration.getImageViewXPosition());
            Messages.sprintf("index " + p + " IMAGE_VIEW_X_POSITION: " + configuration.getImageViewXPosition());

            p = index; pstmt.setDouble(index++, configuration.getImageViewYPosition());
            Messages.sprintf("index " + p + " IMAGE_VIEW_Y_POSITION: " + configuration.getImageViewYPosition());

            p = index; pstmt.setString(index++, configuration.getWorkDirSerialNumber());
            Messages.sprintf("index " + p + " WORK_DIR_SERIAL_NUMBER: " + configuration.getWorkDirSerialNumber());

            p = index; pstmt.setString(index++, configuration.getWorkDir());
            Messages.sprintf("index " + p + " WORK_DIR: " + configuration.getWorkDir());

            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
        } catch (SQLException e) {
            Messages.sprintfError("Error inserting configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createConfigurationDatabase() {
        Connection localConnection = null;
        try {
            localConnection = getConnection();
            if (localConnection == null) {
                Messages.sprintfError("Could not establish database connection");
                return false;
            }

            SQL_Utils.setAutoCommit(localConnection, false);
            String sql = "CREATE TABLE IF NOT EXISTS "
                    + SQLTableEnums.CONFIGURATION.getType() + " ("
                    + ID.getColumnDefinition() + ", "
                    + BETTER_THUMBNAIL_QUALITY.getColumnDefinition() + ", "
                    + CONFIRM_ON_EXIT.getColumnDefinition() + ", "
                    + ID_COUNTER.getColumnDefinition() + ", "
                    + SHOW_FULL_PATH.getColumnDefinition() + ", "
                    + SHOW_HINTS.getColumnDefinition() + ", "
                    + SHOW_TOOLTIPS.getColumnDefinition() + ", "
                    + CURRENTTHEME.getColumnDefinition() + ", "
                    + VLC_PATH.getColumnDefinition() + ", "
                    + VLC_SUPPORT.getColumnDefinition() + ", "
                    + SAVE_DATA_AS_HD.getColumnDefinition() + ", "
                    + WINDOW_START_POSITION_X.getColumnDefinition() + ", "
                    + WINDOW_START_POSITION_Y.getColumnDefinition() + ", "
                    + WINDOW_START_WIDTH.getColumnDefinition() + ", "
                    + WINDOW_START_HEIGHT.getColumnDefinition() + ", "
                    + IMAGE_VIEW_X_POSITION.getColumnDefinition() + ", "
                    + IMAGE_VIEW_Y_POSITION.getColumnDefinition() + ", "
                    + WORK_DIR_SERIAL_NUMBER.getColumnDefinition() + ", "
                    + WORK_DIR.getColumnDefinition() + ")";

            Messages.sprintf("CreateConfiguration database: " + sql);

            try (Statement stmt = localConnection.createStatement()) {
                stmt.execute(sql);

                // IMPORTANT: commit on the same connection you executed on
                localConnection.commit();

                if (insertConfiguration(localConnection, Main.conf)) {
                    createIgnoredListTable(Main.conf);
                    localConnection.commit();
                    return true;
                }
                localConnection.rollback();
            } catch (SQLException e) {
                Messages.sprintfError("Error creating table: " + e.getMessage());
            }
        } catch (Exception e) {
            Messages.sprintfError("Database error: " + e.getMessage());
        }
        // Don't close localConnection here if it's the shared/static connection.
        return false;
    }

    /**
     * Creates the ignored list table in the database.
     *
     * @return true if the ignored list table is successfully created, false otherwise
     */
    public static boolean createIgnoredListTable(Configuration configuration) {
        Messages.sprintf("createIgnoredListTable:::: ");
        if (!validateConnectionWithRetry()) {
            Messages.warningText("Database connection could not be established.");
            return false;
        }
        String sql = "CREATE TABLE IF NOT EXISTS " + SQLTableEnums.IGNOREDLIST.getType() + " (" +
                "path STRING UNIQUE)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException ex) {
            Messages.warningText("Cannot create ignored list table: " + ex.getMessage());
            return false;
        }

    }

    public static boolean validateConnectionWithRetry() {
        int maxRetries = 3;
        int delaySeconds = 5;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Check if the connection is null, disconnected, or closed
                if (connection == null || !SQL_Utils.isDbConnected(connection) || connection.isClosed()) {
                    connection = SQL_Utils.createConfigurationConfig();
                    SQL_Utils.setAutoCommit(connection, false);
                    Messages.sprintf("Configuration database opened: " + SQL_Utils.getUrl(connection));
                    return true; // Connection successfully re-established
                }

                // Ensure AutoCommit is disabled
                if (connection.getAutoCommit()) {
                    SQL_Utils.setAutoCommit(connection, false);
                }

                return true; // Existing connection is valid

            } catch (SQLException e) {
                Messages.sprintfError("Error checking the database connection (attempt " + attempt + "/" + maxRetries + "): " + e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delaySeconds * 1000); // Convert seconds to milliseconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }

        // If we get here, all attempts failed
        Messages.sprintfError("Failed to establish database connection after " + maxRetries + " attempts");
        return false;
    }

    /**
     * Updates the configuration in the database.
     */
//    public static synchronized void updateConfiguration() {
//        checkConnection();
//
//        if (!SQL_Utils.isDbAccessible(connection, Main.conf.getConfiguration_db_fileName())) {
//            createConfigurationDatabase();
//        }
//
//        try {
//            // Ensure the 'currentTheme' column exists.
//            ensureAllColumnExists(connection);
//
//            insertConfiguration(Main.conf);
//            SQL_Utils.commitChanges(connection);
//            SQL_Utils.closeConnection(connection);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //SQL_Utils.closeConnection(connection);
//    }


    /**
     * Ensures that the 'currentTheme' column exists in the configuration table.
     *
     * @param connection the connection to the database.
     * @throws Exception if any SQL error occurs.
     */
    private static void ensureAllColumnExists(Connection connection) throws SQLException {
        String configTable = SQLTableEnums.CONFIGURATION.getType();
//        final String[] columnsSettings = {
//                BETTER_THUMBNAIL_QUALITY + " BOOLEAN",
//                CONFIRM_ON_EXIT + " BOOLEAN",
//                CURRENTTHEME + " STRING",
//                ID + " INTEGER PRIMARY KEY CHECK (id = 0)",
//                ID_COUNTER + " INTEGER UNIQUE",
//                IMAGE_VIEW_X_POSITION + " DOUBLE",
//                IMAGE_VIEW_Y_POSITION + " DOUBLE",
//                SAVE_DATA_AS_HD + " STRING",
//                SHOW_FULL_PATH + " BOOLEAN",
//                SHOW_HINTS + " BOOLEAN",
//                SHOW_TOOLTIPS + " BOOLEAN",
//                VLC_PATH + " STRING",
//                VLC_SUPPORT + " BOOLEAN",
//                WINDOW_START_HEIGTH + " DOUBLE DEFAULT (-1)",
//                WINDOW_START_POSITION_X + " DOUBLE DEFAULT (-1)",
//                WINDOW_START_POSITION_Y + " DOUBLE DEFAULT (-1)",
//                WINDOW_START_WIDTH + " DOUBLE DEFAULT (-1)",
//                WORK_DIR + " STRING",
//                WORK_DIR_SERIAL_NUMBER + " STRING"
//        };

        try (Statement stmt = connection.createStatement()) {
            for (String column : ConfigurationEnum.getAllColumnNames().split(",")) {
                try {
                    String alterTableSQL = "ALTER TABLE " + configTable + " ADD COLUMN " + column;
                    stmt.executeUpdate(alterTableSQL);
                } catch (SQLException e) {
                    // Column already exists, ignore this error
                    if (!e.getMessage().contains("duplicate column name")) {
                        Messages.sprintfError("Error adding column to table: " + e.getMessage());
                        throw e;
                    }
                }
            }
        } catch (SQLException e) {
            Messages.sprintfError("Error adding column to table: " + e.getMessage());
        }
        Messages.sprintf("ensureAllColumnExists: " + configTable);
    }


    private static boolean loadTableSQL(Configuration configuration) {
        final String tableName = SQLTableEnums.CONFIGURATION.getType();
        final String tableSQL = "SELECT " + ConfigurationEnum.getAllColumnNames() + " FROM " + tableName;
        Messages.sprintf("-----------loadConfiguration: " + tableSQL);

        if (!SQL_Utils.isDbConnected(connection) || !SQL_Utils.isDbAccessible(connection, tableName)) {
            if (!ConfigurationSQLHandler.checkConnection()) {
                Messages.sprintf("loadConfiguration database not accessible: " + tableName);
            }
            return false;
        }

        try {
            if (!tableExists(connection, tableName)) {
                Messages.sprintf("loadConfiguration table not exists: " + tableName);
                ConfigurationSQLHandler.createConfigurationDatabase();
                SQL_Utils.commitChanges(connection);
                return true;
            }

            try (PreparedStatement pstmt = connection.prepareStatement(tableSQL);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    configuration_id = rs.getInt(ID.getColumnName());

                    configuration.setBetterQualityThumbs(rs.getBoolean(BETTER_THUMBNAIL_QUALITY.getColumnName()));
                    configuration.setConfirmOnExit(rs.getBoolean(CONFIRM_ON_EXIT.getColumnName()));
                    configuration.setId_counter(rs.getInt(ID_COUNTER.getColumnName()));

                    configuration.setShowFullPath(rs.getBoolean(SHOW_FULL_PATH.getColumnName()));
                    configuration.setShowHints(rs.getBoolean(SHOW_HINTS.getColumnName()));
                    configuration.setShowTooltips(rs.getBoolean(SHOW_TOOLTIPS.getColumnName()));

                    configuration.setCurrentTheme(rs.getString(CURRENTTHEME.getColumnName())); // e.g. "dark"
                    configuration.setThemePath("/themes/" + configuration.getCurrentTheme() + "/");

                    configuration.setVlcPath(rs.getString(VLC_PATH.getColumnName()));
                    configuration.setVlcSupport(rs.getBoolean(VLC_SUPPORT.getColumnName()));
                    configuration.setSaveDataToHD(rs.getBoolean(SAVE_DATA_AS_HD.getColumnName()));

                    configuration.setWindowStartPosX(rs.getDouble(WINDOW_START_POSITION_X.getColumnName()));
                    configuration.setWindowStartPosY(rs.getDouble(WINDOW_START_POSITION_Y.getColumnName()));
                    configuration.setWindowStartWidth(rs.getDouble(WINDOW_START_WIDTH.getColumnName()));
                    configuration.setWindowStartHeight(rs.getDouble(WINDOW_START_HEIGHT.getColumnName()));

                    configuration.setImageViewXProperty(rs.getDouble(IMAGE_VIEW_X_POSITION.getColumnName()));
                    configuration.setImageViewYProperty(rs.getDouble(IMAGE_VIEW_Y_POSITION.getColumnName()));

                    configuration.setWorkDirSerialNumber(rs.getString(WORK_DIR_SERIAL_NUMBER.getColumnName()));

                    String workDir = rs.getString(WORK_DIR.getColumnName());
                    if (workDir != null && !workDir.isBlank()) {
                        configuration.setWorkDir(workDir);
                    }

                    Messages.sprintf(
                            "Workdir loaded: " + workDir +
                                    " serial number = " + configuration.getWorkDirSerialNumber() +
                                    " show tooltips " + configuration.isShowTooltips() +
                                    " configuration.: " + configuration.getWorkDir()
                    );
                }

                SQL_Utils.commitChanges(connection);
                return true;
            } catch (SQLException e) {
                Messages.sprintfError("SQL Exception: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("ConfigurationSQLHandler RETURNING FALSE conf.workDir_property(): "
                    + configuration.getWorkDir() + " ERROR: " + e.getMessage());
            return false;
        } finally {
            SQL_Utils.closeConnection(connection);
        }
    }


    private static boolean tableExists(Connection conn, String tableName) {
        if (conn == null) return false;
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            Messages.sprintfError("tableExists() error: " + e.getMessage());
            return false;
        }
    }


    /**
     * Loads the configuration from the database.
     *
     * @param configuration the Configuration object to load the values into
     * @return true if the configuration is successfully loaded, false otherwise
     */

    public static boolean loadConfiguration(Configuration configuration) {
        Messages.sprintf("loadConfiguration Loading SQL config: " + Main.conf.getAppDataPath() + " - " + Main.conf.getConfiguration_db_fileName());

        ConfigurationSQLHandler.checkConnection();
        if (!SQL_Utils.isDbAccessible(connection, SQLTableEnums.CONFIGURATION.getType()) || !SQL_Utils.isDbAccessible(connection, SQLTableEnums.CONFIGURATION.getType())) {
            Messages.sprintf("loadConfiguration database not accessible: " + SQLTableEnums.CONFIGURATION.getType());

            ConfigurationSQLHandler.createConfigurationDatabase();

            // Re-acquire an open connection before inserting (createConfigurationDatabase used it)
            Connection conn = getConnection();
            ConfigurationSQLHandler.insertConfiguration(conn, Main.conf);

            boolean b = ConfigurationSQLHandler.closeConnection();
            if (b) {
                SQL_Utils.closeConnection(connection);
            }
            return true;
        }

        Messages.sprintf("loadConfiguration connection were connected: " + SQL_Utils.getUrl(connection));
        loadTableSQL(configuration);
        return true;
    }

    /**
     * Loads the ignored list from the database and adds the paths to the provided ObservableList.
     *
     * @param connection the connection to the database
     * @param obs        the ObservableList to which the paths will be added
     * @return true if the ignored list is successfully loaded, false otherwise
     */
//    public static boolean loadIgnoredList(Connection connection, ObservableList<Path> obs) {
//        checkConnection();
//
//        try {
//            String sql = "SELECT * FROM " + SQLTableEnums.IGNOREDLIST.getType();
//            Statement stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                Messages.sprintf("loadIgnored_list starting: " + sql);
//                String path = rs.getString("path");
//                obs.add(Paths.get(path));
//            }
//            stmt.close();
//
//            Messages.sprintf("loadIgnored_listsize of sel obs= " + obs.size());
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

//    public static boolean insertConfiguration(Configuration configuration) {
//        Messages.sprintf("Inserting insert_Configuration");
//        Connection localConnection = null;
//
//        try {
//            localConnection = getConnection();
//            if (!SQL_Utils.isDbConnected(localConnection)) {
//                return false;
//            }
//
//            //@formatter:off
//            String sql = "INSERT OR REPLACE INTO " + SQLTableEnums.CONFIGURATION.getType() +
//                    " (" +
//                    ID + ", " +
//                    BETTER_THUMBNAIL_QUALITY + ", " +
//                    CONFIRM_ON_EXIT + ", " +
//                    ID_COUNTER + ", " +
//                    SHOW_FULL_PATH + ", " +
//                    SHOW_HINTS + ", " +
//                    SHOW_TOOLTIPS + ", " +
//                    CURRENTTHEME + ", " +
//                    VLC_PATH + ", " +
//                    VLC_SUPPORT + ", " +
//                    SAVE_DATA_AS_HD + ", " +
//                    WINDOW_START_POSITION_X + ", " +
//                    WINDOW_START_POSITION_Y + ", " +
//                    WINDOW_START_WIDTH + ", " +
//                    WINDOW_START_HEIGTH + ", " +
//                    IMAGE_VIEW_X_POSITION + ", " +
//                    IMAGE_VIEW_Y_POSITION + ", " +
//                    WORK_DIR_SERIAL_NUMBER + ", " +
//                    WORK_DIR + ") " +
//                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//            //@formatter:on
//
//            try (PreparedStatement pstmt = localConnection.prepareStatement(sql)) {
//                pstmt.setInt(1, configuration_id);
//                pstmt.setBoolean(2, configuration.isBetterQualityThumbs());
//                pstmt.setBoolean(3, configuration.isConfirmOnExit());
//                pstmt.setInt(4, configuration.getId_counter().get());
//                pstmt.setBoolean(5, configuration.isShowFullPath());
//                pstmt.setBoolean(6, configuration.isShowHints());
//                pstmt.setBoolean(7, configuration.isShowTooltips());
//                pstmt.setString(8, configuration.getCurrentTheme());
//                pstmt.setString(9, configuration.getVlcPath());
//                pstmt.setBoolean(10, configuration.isVlcSupport());
//                pstmt.setBoolean(11, configuration.isSaveDataToHD());
//                pstmt.setDouble(12, configuration.getWindowStartPosX());
//                pstmt.setDouble(13, configuration.getWindowStartPosY());
//                pstmt.setDouble(14, configuration.getWindowStartWidth());
//                pstmt.setDouble(15, configuration.getWindowStartHeight());
//                pstmt.setDouble(16, configuration.getImageViewXPosition());
//                pstmt.setDouble(17, configuration.getImageViewYPosition());
//                pstmt.setString(18, configuration.getWorkDirSerialNumber());
//                pstmt.setString(19, configuration.getWorkDir());
//
//                int result = pstmt.executeUpdate();
//
//                if (result > 0) {
//                    localConnection.commit();
//                    return true;
//                }
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (localConnection != null) {
//                try {
//                    localConnection.rollback();
//                } catch (SQLException ex) {
//                    ex.printStackTrace();
//                }
//            }
//            return false;
//        }
//    }

    /**
     * Inserts or replaces the paths of the folders in the ignored list into the database.
     *
     * @param listToRemove the ArrayList of FolderInfo objects representing the folders to insert or replace in the ignored list
     */
    public static void insertIgnoredList(List<FolderInfo> listToRemove) {

        checkConnection();

        try {
            String sql = "INSERT OR REPLACE INTO " + SQLTableEnums.IGNOREDLIST.getType() + " ('path') VALUES(?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            for (FolderInfo folderInfo : listToRemove) {
                pstmt.setString(1, folderInfo.getFolderPath());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            pstmt.close();
            SQL_Utils.commitChanges(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param connection_open The open Connection object to the database. If null, a new connection will be created.
     * @param removePath      The Path object representing the path to be removed from the ignored list.
     * @return true if the path is successfully removed from the ignored list, false otherwise.
     */
    public static boolean removeFromIgnoredList(Connection connection_open, Path removePath) {
        checkConnection();

        String sql = "DELETE FROM " + SQLTableEnums.IGNOREDLIST.getType() + " WHERE path = ?";
        Messages.sprintf("removeFromIgnoredList SQL= " + sql);
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, removePath.toString());
            pstmt.executeUpdate();
            pstmt.close();

            SQL_Utils.commitChanges(connection);
            //SQL_Utils.closeConnection(connection);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //@formatter:on
    public static Connection getConnection() {
        if (checkConnection()) {
            return connection;
        }
        createConfigurationDatabase();
        return connection;
    }

    public static void close() {
        SQL_Utils.closeConnection(connection);
    }
}