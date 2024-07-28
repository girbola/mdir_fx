package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FolderInfo_SQLTest {

    private static Connection connection_mdirFile;
    final static Path filePath = Paths.get("src", "test", "resources", "test-material", "IMG_4312.CR2");

    @BeforeAll
    static public void setupDBConnection() {
        try {
            connection_mdirFile = DriverManager.getConnection("jdbc:sqlite:" + filePath.getParent() + File.separator + Main.conf.getMdir_db_fileName());
            boolean dbConnected = SQL_Utils.isDbConnected(connection_mdirFile);
            if (!dbConnected) {

                Statement stmt = connection_mdirFile.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON");
                SQL_Utils.setAutoCommit(connection_mdirFile, false);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadFolderInfo() throws IOException {

        FolderInfo fi = new FolderInfo();
        fi.setStatus(0);
        fi.setChanged(true);
        fi.setConnected(true);
        fi.setIgnored(false);
        fi.setDateDifferenceRatio(0.1);
        fi.setBadFiles(2);
        fi.setConfirmed(3);
        fi.setCopied(4);
        fi.setFolderFiles(100);
        fi.setFolderImageFiles(20);
        fi.setFolderRawFiles(30);
        fi.setFolderVideoFiles(40);
        fi.setGoodFiles(10);
        fi.setSuggested(5);
        fi.setFolderSize(100000);
        fi.setJustFolderName("test_folder");
        fi.setFolderPath(filePath.getParent().toString());
        fi.setMaxDate("2022-04-25");
        fi.setMinDate("2022-01-01");
        fi.setState("active");
        fi.setTableType("test_table");
        List<FileInfo> list = new ArrayList<FileInfo>();

        FileInfo fileInfo = FileInfoUtils.createFileInfo(filePath);
        list.add(fileInfo);
        fi.setFileInfoList(list);

        FolderInfo_Utils.calculateFileInfoStatuses(fi);

        FolderInfo_SQL.saveFolderInfoToTable(connection_mdirFile, fi);
        FolderInfo loadedFi = FolderInfo_SQL.loadFolderInfo(Paths.get(fi.getFolderPath()));
        Assertions.assertNotNull(loadedFi);
        Assertions.assertTrue(fi.getChanged());
        Assertions.assertTrue(fi.isConnected());
        Assertions.assertFalse(fi.getIgnored());
        Assertions.assertEquals(5, loadedFi.getSuggested());
        SQL_Utils.closeConnection(connection_mdirFile);
        Path removeTestFile = Paths.get(fi.getFolderPath(), Main.conf.getMdir_db_fileName());
        Files.deleteIfExists(removeTestFile);
    }
}