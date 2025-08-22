package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import org.junit.jupiter.api.AfterAll;
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
            SQL_Utils.setAutoCommit(connection_mdirFile,false);
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

    @AfterAll
    public static void closeDBConnection() {
        SQL_Utils.closeConnection(connection_mdirFile);
    }
    @Test
    public void testLoadFolderInfo() throws IOException {

        FolderInfo folderInfo = new FolderInfo();
        folderInfo.setStatus(0);
        folderInfo.setChanged(true);
        folderInfo.setConnected(true);
        folderInfo.setIgnored(false);
        folderInfo.setDateDifferenceRatio(0.1);
        folderInfo.setBadFiles(2);
        folderInfo.setConfirmed(3);
        folderInfo.setCopied(4);
        folderInfo.setFolderFiles(100);
        folderInfo.setFolderImageFiles(20);
        folderInfo.setFolderRawFiles(30);
        folderInfo.setFolderVideoFiles(40);
        folderInfo.setGoodFiles(10);
        folderInfo.setSuggested(5);
        folderInfo.setFolderSize(100000);
        folderInfo.setJustFolderName("test_folder");
        folderInfo.setFolderPath(filePath.getParent().toString());
        folderInfo.setMaxDate("2022-04-25");
        folderInfo.setMinDate("2022-01-01");
        folderInfo.setState("active");
        folderInfo.setTableType("test_table");
        List<FileInfo> list = new ArrayList<FileInfo>();

        FileInfo fileInfo = FileInfoUtils.createFileInfo(filePath);
        list.add(fileInfo);
        folderInfo.setFileInfoList(list);

        FolderInfoUtils.calculateFolderInfoStatus(folderInfo);

//        FolderInfo_SQL.saveFolderInfoToDatabase(connection_mdirFile, folderInfo);

        FolderInfo loadedFi = FolderInfo_SQL.loadFolderInfo(Paths.get(folderInfo.getFolderPath()));
        Assertions.assertNotNull(loadedFi);
        Assertions.assertTrue(folderInfo.getChanged());
        Assertions.assertTrue(folderInfo.isConnected());
        Assertions.assertFalse(folderInfo.getIgnored());
        Assertions.assertEquals(5, loadedFi.getSuggested());
        SQL_Utils.closeConnection(connection_mdirFile);
        Path removeTestFile = Paths.get(folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());
        if(Files.exists(removeTestFile)) {
            System.out.println("Deleting: " + removeTestFile);
            Files.deleteIfExists(removeTestFile);
        }
    }
}