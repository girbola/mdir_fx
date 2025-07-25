package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import common.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import static com.girbola.Main.conf;

public class ConfigurationUtils {

    final private static String ERROR = ConfigurationUtils.class.getSimpleName();

    public static void loadConfig() {
        Messages.sprintf("Loading configuration...");
        if (!Files.exists(Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
            boolean sqlDatabaseCreated = createConfiguration_db();
            if (!sqlDatabaseCreated) {
                Messages.errorSmth(ERROR, "Could not be able to create configuration file", null, Misc.getLineNumber(),
                        true);
                Messages.sprintfError("Could not be able to create configuration file failed");
            } else {
                Messages.sprintf("Configuration databases were created successfully");
            }
        } else {
            Messages.sprintf("LOADING CONFIGURATION DATABASE");
            try {
                conf.loadConfig_SQL();
            } catch (SQLException e) {
                Messages.sprintf("Configuration databases cannot be load: " + e.getMessage());
            }
        }
    }

    public static boolean createConfiguration_db() {
        Messages.sprintf("creatingConfiguration_DB at: " + Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName());
        Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(), Main.conf.getConfiguration_db_fileName());

        SQL_Utils.setAutoCommit(connection, false);

        // Create configuration table_cols which keeps tableview's widths
        Configuration_SQL_Utils.createConfigurationTable_properties(connection);
        // Create configuration for programs config like currentTheme, workDir, vlcPath
        // etc.
        Configuration_SQL_Utils.createConfiguration_Table(connection);
        boolean b = SQL_Utils.commitChanges(connection);
        if (!b) {
            SQL_Utils.rollBackConnection(connection);
        }

        // Inserts default params to configuration
        Configuration_SQL_Utils.insert_Configuration(connection, Main.conf);
        Configuration_SQL_Utils.createIgnoredListTable(connection);
        Configuration_SQL_Utils.createFolderInfosDatabase(connection);

        return SQL_Utils.closeConnection(connection);

    }

    public static void loadConfig_GUI(ModelMain modelMain) {
        Messages.sprintf("loadConfig_GUI Started: " + Main.conf.getAppDataPath());
        Connection connection = null;
        boolean createDatabase = false;
        Path configFile = Paths.get(Main.conf.getAppDataPath() + File.separator);

        if (!Files.exists(configFile)) {
            try {
                Path createDirectories = Files.createDirectories(configFile);
                Path createFile = Files.createFile(Paths.get(configFile.toString() + Main.conf.getConfiguration_db_fileName()));
/*
                boolean writable = FileUtils.setWritable(createFile, true);
                boolean readable = FileUtils.setReadable(createFile, true);
*/

                if (!Files.exists(createDirectories)) {
                    Messages.errorSmth(ERROR, Main.bundle.getString("cannotCreateConfigFile"), null,
                            Misc.getLineNumber(), true);
                } else {
                    createDatabase = true;
                }
            } catch (IOException e1) {
                Messages.sprintf("Something went wrong: " + e1.getMessage());
            }
            connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                    Main.conf.getConfiguration_db_fileName());

            if (createDatabase) {
                Configuration_SQL_Utils.createConfiguration_Table(connection);
            } else {
                Configuration_SQL_Utils.loadTables(connection, modelMain.tables());
            }
//			try {
//				connection.close();
//			} catch (Exception e) {
//				Messages.sprintfError("loadConfig_GUI error with closing SQL database");
//			}
        }
    }


}
