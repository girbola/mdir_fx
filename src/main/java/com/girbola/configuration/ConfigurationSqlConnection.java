package com.girbola.configuration;

import com.girbola.messages.Messages;
import com.girbola.sql.migrate.ConfigurationSqlDatabaseMigrator;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConfigurationSqlConnection {

    public static Connection connectToDatabase(Path path, String tableName) {
        return connectToDatabase(path.toFile().getAbsolutePath(), tableName);
    }

    public static Connection connectToDatabase(String path, String tableName) {
        Messages.sprintf("Configuration SQL connection to path: " + path + " tableName: " + tableName);
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + path + File.separator + tableName);
            if(conn != null && !conn.isClosed()) {
                ConfigurationSqlDatabaseMigrator.migrate(conn);
            }
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            Messages.sprintf("Something went wrong while connecting SQLITE database.\n" + e.getMessage());
            return conn;
        }
    }

}
