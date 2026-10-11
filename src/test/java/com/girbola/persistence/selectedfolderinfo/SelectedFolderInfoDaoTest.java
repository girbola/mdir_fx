package com.girbola.persistence.selectedfolderinfo;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.SQLTableEnums;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SelectedFolderInfoDaoTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testCreateTableAndColumns() throws SQLException {
        boolean created = SelectedFolderInfoDao.createSelectedFoldersDBTable(connection);
        assertTrue(created, "Table creation should succeed");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + SQLTableEnums.SELECTEDFOLDERS.getType() + ")")) {
            List<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
            assertTrue(columns.contains(SelectedFolderInfoDao.PATH));
            assertTrue(columns.contains(SelectedFolderInfoDao.CONNECTED));
            assertTrue(columns.contains(SelectedFolderInfoDao.IGNORED));
            assertTrue(columns.contains(SelectedFolderInfoDao.SELECTED));
            assertTrue(columns.contains(SelectedFolderInfoDao.MEDIA));
            assertTrue(columns.contains(SelectedFolderInfoDao.DRIVE_SERIAL_NUMBER));
        }
    }

    @Test
    void testInsertAndLoadSelectedFolders() {
        boolean created = SelectedFolderInfoDao.createSelectedFoldersDBTable(connection);
        assertTrue(created);

        List<SelectedFolder> list = new ArrayList<>();
        SelectedFolder sf1 = new SelectedFolder("C:\\TestFolder1", true, true, true, false, "SERIAL123");
        SelectedFolder sf2 = new SelectedFolder("C:\\TestFolder2", true, false, false, true, "SERIAL456");
        list.add(sf1);
        list.add(sf2);

        boolean inserted = SelectedFolderInfoDao.insertSelectedFoldersToDB(connection, list);
        assertTrue(inserted, "Insert should succeed");

        ModelMain modelMain = new ModelMain();
        boolean loaded = SelectedFolderInfoDao.loadSelectedFoldersFromConfigDb(connection, modelMain);
        assertTrue(loaded, "Load should succeed");

        assertEquals(2, modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
        SelectedFolder loaded1 = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream()
                .filter(sf -> "C:\\TestFolder1".equals(sf.getFolder()))
                .findFirst().orElse(null);
        assertNotNull(loaded1);
        assertTrue(loaded1.isSelected());
        assertTrue(loaded1.isMedia());
        assertEquals("SERIAL123", loaded1.getDriveSerialNumber());

        SelectedFolder loaded2 = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().stream()
                .filter(sf -> "C:\\TestFolder2".equals(sf.getFolder()))
                .findFirst().orElse(null);
        assertNotNull(loaded2);
        assertTrue(loaded2.isSelected());
        assertFalse(loaded2.isMedia());
        assertTrue(loaded2.isIgnored());
        assertEquals("SERIAL456", loaded2.getDriveSerialNumber());
    }
}
