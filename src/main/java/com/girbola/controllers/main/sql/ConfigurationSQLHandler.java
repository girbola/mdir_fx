package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.configuration.Configuration;
import com.girbola.controllers.main.SQL_Enums;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.DriveInfoSQL;
import com.girbola.sql.SQL_Utils;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class ConfigurationSQLHandler extends DriveInfoSQL {

    private static final String ERROR = ConfigurationSQLHandler.class.getName();
    public static final String id = "id";

    private static Connection connection;

    private static final String tablesColumnsInfoInsert = "INSERT OR REPLACE INTO " + SQL_Enums.TABLES_COLS.getType() + " ('tableColumn', " + "'width')" + " VALUES(?, ?)";
    private static int configuration_id = 0;

    public static final String betterQualityThumbs = "betterQualityThumbs";
    public static final String confirmOnExit = "confirmOnExit";
    public static final String id_counter = "id_counter";
    public static final String imageViewXPos = "imageViewXPos";
    public static final String imageViewYPos = "imageViewYPos";
    public static final String saveDataToHD = "saveDataToHD";
    public static final String showFullPath = "showFullPath";
    public static final String showHints = "showHints";
    public static final String showTooltips = "showTooltips";
    public static final String tableShow_asItIs = "tableShow_asItIs";
    public static final String tableShow_sortIt = "tableShow_sortIt";
    public static final String tableShow_sorted = "tableShow_sorted";
    public static final String currentTheme = "currentTheme";
    public static final String vlcPath = "vlcPath";
    public static final String vlcSupport = "vlcSupport";
    public static final String windowStartHeigth = "windowStartHeigth";
    public static final String windowStartPosX = "windowStartPosX";
    public static final String windowStartPosY = "windowStartPosY";
    public static final String windowStartWidth = "windowStartWidth";
    public static final String workDir = "workDir";
    public static final String workDirSerialNumber = "workDirSerialNumber";
    private static final String ignoredPathTable = "path";


    /**
     * Updates the configuration in the database.
     */
    public static synchronized void updateConfiguration() {
        checkConnection();

        if (!SQL_Utils.isDatabaseAccessible(connection, Main.conf.getConfiguration_db_fileName())) {
            createConfigurationDatabase();
        }

        try {
            // Ensure the 'currentTheme' column exists.
            ensureAllColumnExists(connection);

            insert_Configuration(Main.conf);
            SQL_Utils.commitChanges(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //SQL_Utils.closeConnection(connection);
    }

    private static boolean addTableColumn(PreparedStatement pstmt, TableColumn tc, String tableId) {
        try {
            if (tc.getId() != null) {
                pstmt.setString(1, tableId + "_" + tc.getId());
                pstmt.setDouble(2, tc.getWidth());
                pstmt.addBatch();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Creates the configuration table with properties in the database.
     *
     * @return true if the configuration table is successfully created, false otherwise
     */
    public static boolean createConfigurationTableProperties() {
        //checkConnection();
        try {
            if (!SQL_Utils.isDbConnected(connection)) {
                Messages.sprintf("createConfigurationTable connection failed");
                return false;
            }
            String sql = "CREATE TABLE IF NOT EXISTS "
                    + SQL_Enums.TABLES_COLS.getType()
                    + " (tableColumn STRING UNIQUE, "
                    + "	width DOUBLE)";
            try (Statement stmt = connection.createStatement()) {
                stmt.addBatch(sql);
                stmt.executeBatch();
            }
            SQL_Utils.commitChanges(connection);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the configuration table in the database with the specified properties.
     *
     * @return true if the configuration table is successfully created, false otherwise
     */
    public static boolean createConfigurationDatabase() {
        checkConnection();
        Messages.sprintfError("createConfiguration_Table: ");

        boolean isOpen = checkConnection();
        if (!isOpen) {
            connection = getConnection();
            Messages.sprintf("Is connected");
        }

        try {
            //@formatter:off
			String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.CONFIGURATION.getType()+ " ("
					+ id + " INTEGER PRIMARY KEY CHECK (id = 0),"
					+ betterQualityThumbs + " BOOLEAN, "
		        	+ confirmOnExit + " BOOLEAN,"
		    	    + id_counter + " INTEGER UNIQUE,"
		    	    + showFullPath + " BOOLEAN,"
		    	    + showHints + " BOOLEAN,"
		    	    + showTooltips + " BOOLEAN,"
		    	    + currentTheme + " STRING,"
		    	    + vlcPath + " STRING,"
		    	    + vlcSupport + " BOOLEAN,"
		    	    + saveDataToHD + " STRING,"
		    	    + windowStartPosX  + " DOUBLE DEFAULT ( -1),"
					+ windowStartPosY  + " DOUBLE DEFAULT ( -1),"
					+ windowStartWidth  + " DOUBLE DEFAULT ( -1),"
					+ windowStartHeigth + " DOUBLE DEFAULT ( -1),"
					+ imageViewXPos + " DOUBLE,"
					+ imageViewYPos + " DOUBLE,"
					+ workDirSerialNumber + " STRING, "
		    	    + workDir + " STRING, "
		    	    + tableShow_sortIt + " BOOLEAN,"
		    	    + tableShow_sorted + " BOOLEAN,"
		    	    + tableShow_asItIs + " BOOLEAN)";

            if(!SQL_Utils.isDbConnected(connection)) {
                connection = SQL_Utils.createConfigurationConfig();
            }
			//@formatter:on
            Statement stmt = connection.createStatement();
            stmt.execute(sql);

            insert_Configuration(Main.conf);
            createConfigurationTableProperties();
            createIgnoredListTable(Main.conf);

            //SQL_Utils.commitChanges(connection);
//            SQL_Utils.closeConnection(connection);
            return true;
        } catch (Exception e) {
            Messages.sprintfError("Cannot create configuration database");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates the ignored list table in the database.
     *
     * @return true if the ignored list table is successfully created, false otherwise
     */
    public static boolean createIgnoredListTable(Configuration configuration) {

        checkConnection();

        String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.IGNOREDLIST.getType() + " (" + ignoredPathTable + " STRING UNIQUE)";

        //SQL_Utils.setAutoCommit(connection, false);

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            SQL_Utils.commitChanges(connection);
//            SQL_Utils.closeConnection(connection);
            return true;
        } catch (Exception ex) {
            Messages.warningText("Cannot create ignored list table");
            return false;
        }
    }

    /**
     * Verifies the state of the database connection and, if necessary, re-establishes it.
     * If the connection is null or not currently open, a new connection to the SQLite database
     * is created using provided configuration settings.
     * <p>
     * This method also sets the database connection to not auto-commit and logs a message
     * indicating the database file that has been connected.
     */
    public static boolean checkConnection() {
        try {

            if (connection == null || !SQL_Utils.isDbConnected(connection) || getConnection().isClosed()) {
                connection = SQL_Utils.createConfigurationConfig();
                SQL_Utils.setAutoCommit(connection, false);
                Messages.sprintf("Configuration database were opened: " + SQL_Utils.getUrl(connection));
                return true;
            }
        } catch (SQLException e) {

            Messages.sprintfError("Error checking the database connection: " + e.getMessage());
            return false;

        }
        try {
            if (connection.getAutoCommit()) {
                SQL_Utils.setAutoCommit(connection, false);
            }
        } catch (SQLException e) {
            Messages.sprintfError(Main.bundle.getString("cannotOpenConfigurationDatabase"));
        }
        return true;
    }

    /**
     * Ensures that the 'currentTheme' column exists in the configuration table.
     *
     * @param connection the connection to the database.
     * @throws Exception if any SQL error occurs.
     */
    private static void ensureAllColumnExists(Connection connection) throws Exception {

        String configTable = SQL_Enums.CONFIGURATION.getType();
        final String[] columnsSettings = {
                betterQualityThumbs + " BOOLEAN,",
                confirmOnExit + " BOOLEAN,",
                currentTheme + " STRING,",
                id + " INTEGER PRIMARY KEY CHECK (id = 0),",
                id_counter + " INTEGER UNIQUE,",
                imageViewXPos + " DOUBLE,",
                imageViewYPos + " DOUBLE,",
                saveDataToHD + " STRING,",
                showFullPath + " BOOLEAN,",
                showHints + " BOOLEAN,",
                showTooltips + " BOOLEAN,",
                tableShow_asItIs + " BOOLEAN",
                tableShow_sortIt + " BOOLEAN,",
                tableShow_sorted + " BOOLEAN,",
                vlcPath + " STRING,",
                vlcSupport + " BOOLEAN,",
                windowStartHeigth + " DOUBLE DEFAULT ( -1),",
                windowStartPosX + " DOUBLE DEFAULT ( -1),",
                windowStartPosY + " DOUBLE DEFAULT ( -1),",
                windowStartWidth + " DOUBLE DEFAULT ( -1),",
                workDir + " STRING,",
                workDirSerialNumber + " STRING"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String column : columnsSettings) {
                try {
                    String alterTableSQL = "ALTER TABLE " + configTable + " ADD COLUMN "
                            + column + ";";
                    stmt.executeUpdate(alterTableSQL);
                } catch (SQLException e) {
                    // Column already exists, ignore this error
                    if (!e.getMessage().contains("duplicate column name")) {
                        throw e;
                    }
                }
            }
        }

    }


    /**
     * Loads the configuration from the database.
     *
     * @param configuration the Configuration object to load the values into
     * @return true if the configuration is successfully loaded, false otherwise
     */
    public static boolean loadConfiguration(Configuration configuration) {
        Messages.sprintf("loadConfiguration Loading SQL config: " + Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());

        checkConnection();

        if (!SQL_Utils.isDatabaseAccessible(connection, SQL_Enums.CONFIGURATION.getType())) {
            Messages.sprintf("Configuration database were not connected while loading loadConfiguration. Creating configuration database: " + SQL_Enums.CONFIGURATION.getType());
            createConfigurationDatabase();
        }
        Messages.sprintf("loadConfiguration connection were connected: " + SQL_Utils.getUrl(connection));

        String sql = "SELECT id, " + "betterQualityThumbs, " + "confirmOnExit, " + "id_counter, " + "showFullPath, " + "showHints, " + "showTooltips, " + "currentTheme, " + "vlcPath, " + "vlcSupport, " + "saveDataToHD, " + "windowStartPosX, " + "windowStartPosY, " + "windowStartWidth, " + "windowStartHeigth, " + "imageViewXPos, " + "imageViewYPos, " + "workDirSerialNumber, " + "workDir, " + "tableShow_sortIt, " + "tableShow_sorted, " + "tableShow_asItIs " + "FROM " + SQL_Enums.CONFIGURATION.getType();

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.executeQuery();
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                configuration_id = (Integer.parseInt(rs.getString(id)));
                configuration.setBetterQualityThumbs(Boolean.parseBoolean(rs.getString(betterQualityThumbs)));
                configuration.setConfirmOnExit(Boolean.parseBoolean(rs.getString(confirmOnExit)));
                configuration.setId_counter(Integer.parseInt(rs.getString(id_counter)));
                configuration.setShowFullPath(Boolean.parseBoolean(rs.getString(showFullPath)));
                configuration.setShowHints(Boolean.parseBoolean(rs.getString(showHints)));
                configuration.setShowTooltips(Boolean.parseBoolean(rs.getString(showTooltips)));
                configuration.setCurrentTheme(rs.getString(currentTheme)); // e.x. dark
                configuration.setThemePath("/" + configuration.getCurrentTheme() + "/");
                configuration.setVlcPath(rs.getString(vlcPath));
                configuration.setVlcSupport(Boolean.parseBoolean(rs.getString(vlcSupport)));
                configuration.setSaveDataToHD(Boolean.parseBoolean(rs.getString(saveDataToHD)));
                configuration.setWindowStartPosX(Double.parseDouble(rs.getString(windowStartPosX)));
                configuration.setWindowStartPosY(Double.parseDouble(rs.getString(windowStartPosY)));
                configuration.setWindowStartWidth(Double.parseDouble(rs.getString(windowStartWidth)));
                configuration.setWindowStartHeight(Double.parseDouble(rs.getString(windowStartHeigth)));

                String imageViewXPosTemp = (rs.getString(imageViewXPos));
                Messages.sprintf("imageViewXPosTemp: " + imageViewXPosTemp);

                String imageViewYPosTemp = (rs.getString(imageViewYPos));
                Messages.sprintf("imageViewYPosTemp: " + imageViewYPosTemp);
                configuration.setImageViewXProperty(Double.parseDouble(rs.getString(imageViewXPos)));
                configuration.setImageViewYProperty(Double.parseDouble(rs.getString(imageViewYPos)));

                configuration.setWorkDirSerialNumber(rs.getString(workDirSerialNumber));
                String workDirTemp = rs.getString(workDir);
                Messages.sprintfError("workDirTemp= " + workDirTemp);
                if (workDirTemp != null) {
                    configuration.setWorkDir(rs.getString(workDir));
                }
                System.err.println("1conf.workDir_property(): " + configuration.getWorkDir().hashCode());
                Messages.sprintf("Workdir loaded: " + rs.getString(workDir) + " serial number = " + rs.getString(workDirSerialNumber) + " show tooltips " + configuration.isShowTooltips() + " configuration.: " + configuration.getWorkDir());

                configuration.setTableShowSortIt(Boolean.parseBoolean(rs.getString(tableShow_sortIt)));
                configuration.setTableShowSorted(Boolean.parseBoolean(rs.getString(tableShow_sorted)));
                configuration.setTableShowAsItIs(Boolean.parseBoolean(rs.getString(tableShow_asItIs)));

                return true;
            }
            SQL_Utils.commitChanges(connection);
            //SQL_Utils.closeConnection(connection);
            return true;
        } catch (Exception e) {
            System.err.println("RETURNING FALSE 1conf.workDir_property(): " + configuration.getWorkDir() + " ERROR: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads the ignored list from the database and adds the paths to the provided ObservableList.
     *
     * @param connection the connection to the database
     * @param obs        the ObservableList to which the paths will be added
     * @return true if the ignored list is successfully loaded, false otherwise
     */
    public static boolean loadIgnoredList(Connection connection, ObservableList<Path> obs) {
        checkConnection();

        try {
            String sql = "SELECT * FROM " + SQL_Enums.IGNOREDLIST.getType();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Messages.sprintf("loadIgnored_list starting: " + sql);
                String path = rs.getString("path");
                obs.add(Paths.get(path));
            }
            stmt.close();

            Messages.sprintf("loadIgnored_listsize of sel obs= " + obs.size());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loads table columns widths from the database for the provided TableView objects.
     *
     * @param tables The Tables object containing the TableView objects.
     * @return true if the table columns widths are successfully loaded from the database, false otherwise.
     */
    public static boolean loadTables(Tables tables) {
        checkConnection();
        loadTableColumnsWidths(tables.getSorted_table());
        loadTableColumnsWidths(tables.getSortIt_table());
        loadTableColumnsWidths(tables.getAsItIs_table());

        return true;
    }

    /**
     * Loads table columns widths from the database for the provided TableView object.
     *
     * @param table The TableView object to load the columns widths.
     * @return true if the table columns widths are successfully loaded from the database, false otherwise.
     */
    public static boolean loadTableColumnsWidths(TableView<FolderInfo> table) {
        Messages.sprintf("loadTableColumnsWidths started: " + table.getId());
//		final String tableId = table.getId();

        String sql = "SELECT tableColumn, width FROM " + SQL_Enums.TABLES_COLS.getType() + " WHERE tableColumn = ?";
        PreparedStatement pstmt;
        try {
            pstmt = connection.prepareStatement(sql);
            for (TableColumn tc : table.getColumns()) {
                Messages.sprintf("Iterating columns: " + tc.getId());
                pstmt.setString(1, table.getId() + "_" + tc.getId());
                pstmt.executeQuery();
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String column = rs.getString("tableColumn");
                    Messages.sprintf("Loading tablecolumn: " + column + " ID: " + tc.getId());
                    double width = rs.getDouble("width");
                    tc.setPrefWidth(width);
                }
            }
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts or replaces the given Configuration object into the specified database connection.
     *
     * @param configuration the Configuration object to insert or replace
     * @return true if the Configuration object is successfully inserted or replaced, false otherwise
     */
    public static boolean insert_Configuration(Configuration configuration) {
        Messages.sprintf("Inserting insert_Configuration");

        checkConnection();

        if (SQL_Utils.isDbConnected(connection)) {
            try {
                //@formatter:off
                    String sql = "INSERT OR REPLACE INTO " + SQL_Enums.CONFIGURATION.getType()
                            + "('" + id + "', "
                            + "'" + betterQualityThumbs + "',"
                            + "'" + confirmOnExit + "', "
                            + "'" + id_counter + "', "
                            + "'" + showFullPath + "', "
                            + "'" + showHints + "', "
                            + "'" + showTooltips + "', "
                            + "'" + currentTheme + "', "
                            + "'" + vlcPath + "', "
                            + "'" + vlcSupport + "', "
                            + "'" + saveDataToHD + "', "
                            + "'" + windowStartPosX + "', "
                            + "'" + windowStartPosY + "', "
                            + "'" + windowStartWidth + "', "
                            + "'" + windowStartHeigth + "', "
                            + "'" + imageViewXPos+ "', "
                            + "'" + imageViewYPos+ "', "
                            + "'" + workDirSerialNumber + "', "
                            + "'" + workDir + "',"
                            + "'" + tableShow_sortIt + "',"
                            + "'" + tableShow_sorted + "',"
                            + "'" + tableShow_asItIs + "')"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    //@formatter:on
                Messages.sprintf("insert_Configuration: " + sql);
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, configuration_id);
                pstmt.setBoolean(2, configuration.isBetterQualityThumbs());
                pstmt.setBoolean(3, configuration.isConfirmOnExit());
                pstmt.setInt(4, configuration.getId_counter().get());
                pstmt.setBoolean(5, configuration.isShowFullPath());
                pstmt.setBoolean(6, configuration.isShowHints());
                pstmt.setBoolean(7, configuration.isShowTooltips());
                pstmt.setString(8, configuration.getCurrentTheme());
                pstmt.setString(9, configuration.getVlcPath());
                pstmt.setBoolean(10, configuration.isVlcSupport());
                pstmt.setBoolean(11, configuration.isSaveDataToHD());
                pstmt.setDouble(12, configuration.getWindowStartPosX());
                pstmt.setDouble(13, configuration.getWindowStartPosY());
                pstmt.setDouble(14, configuration.getWindowStartWidth());
                pstmt.setDouble(15, configuration.getWindowStartHeight());
                pstmt.setDouble(16, configuration.getImageViewXPosition());
                pstmt.setDouble(17, configuration.getImageViewYPosition());
                pstmt.setString(18, configuration.getWorkDirSerialNumber());
                pstmt.setString(19, configuration.getWorkDir());
                pstmt.setBoolean(20, configuration.getTableShowSortIt());
                pstmt.setBoolean(21, configuration.getTableShowSorted());
                pstmt.setBoolean(22, configuration.getTableShowAsItIs());

                Messages.sprintf(" configuration.getWorkDiREPLACE INTOr()" + configuration.getWorkDir());
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
        return false;

    }

    /**
     * Inserts or replaces the paths of the folders in the ignored list into the database.
     *
     * @param listToRemove the ArrayList of FolderInfo objects representing the folders to insert or replace in the ignored list
     */
    public static void insertIgnoredList(List<FolderInfo> listToRemove) {

        checkConnection();

        try {
            String sql = "INSERT OR REPLACE INTO " + SQL_Enums.IGNOREDLIST.getType() + " ('path') VALUES(?)";
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

        String sql = "DELETE FROM " + SQL_Enums.IGNOREDLIST.getType() + " WHERE path = ?";
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

    /**
     * Saves the widths of the table columns for the specified Tables object into the database.
     *
     * @param table The Tables object containing the TableView objects.
     */
    // @formatter:on
    public static void saveTableWidths(Tables table) {
        checkConnection();

        try {
            PreparedStatement pstmt = null;

            String sql = "DELETE FROM " + SQL_Enums.TABLES_COLS.getType();
            pstmt = connection.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
            pstmt = connection.prepareStatement(tablesColumnsInfoInsert);

            createConfigurationTableProperties();
            saveTableWidths(pstmt, table.getSortIt_table().getColumns(), table.getSortIt_table().getId());
            saveTableWidths(pstmt, table.getSorted_table().getColumns(), table.getSorted_table().getId());
            saveTableWidths(pstmt, table.getAsItIs_table().getColumns(), table.getAsItIs_table().getId());

            pstmt.executeBatch();
            pstmt.close();
            SQL_Utils.commitChanges(connection);
            //SQL_Utils.closeConnection(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the widths of the table columns for the specified table into the database.
     *
     * @param pstmt   The prepared statement for executing SQL queries.
     * @param columns The list of table columns.
     * @param tableId The ID of the table.
     */
    // @formatter:off
	private static void saveTableWidths(PreparedStatement pstmt,
			ObservableList<TableColumn<FolderInfo, ?>> columns, String tableId) {
		try {
			for (TableColumn tc : columns) {
				addTableColumn(pstmt, tc, tableId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/*
    *//*
     * FolderInfos
     *//*
    public static boolean savedFolderInfos() {
        checkConnection();

//@formatter:off
        String sql = "CREATE TABLE IF NOT EXISTS " + SQL_Enums.FOLDERINFOS.getType()
                + " (path STRING NOT NULL PRIMARY KEY UNIQUE, "
                + "justFolderName STRING, "
                + "tableType STRING NOT NULL, "
                + "connected BOOLEAN)";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            SQL_Utils.commitChanges(connection);
            SQL_Utils.closeConnection(connection);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }*/
    //@formatter:on
public static Connection getConnection() {
    checkConnection();
    return connection;
}

    public static void close() {
        SQL_Utils.closeConnection(connection);
    }
}
