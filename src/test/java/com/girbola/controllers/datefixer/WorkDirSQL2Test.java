package com.girbola.controllers.datefixer;


import com.girbola.Main;
import com.girbola.configuration.Configuration;
import com.girbola.controllers.main.sql.WorkDirSQL;
import com.girbola.fileinfo.FileInfo;
import com.girbola.sql.SQL_Utils;
import com.girbola.utils.FileInfoUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkDirSQL2Test {


    private static FileInfo fileInfo;
    private static WorkDirSQL workDirSQL;
    private static Path databaseTemp = Paths.get("src", "test", "resources");
    private static Path srcFile = Paths.get("src", "test", "resources", "test-material", "IMG.jpg");

    @BeforeAll
    static void setup() throws IOException {
        System.out.println("setup WorkDirSQL2Test");

        deleteDatabase();

        fileInfo = new FileInfo();
        fileInfo = FileInfoUtils.createFileInfo(srcFile.toAbsolutePath());
        fileInfo.setFileInfo_id(0);
        fileInfo.setImageDifferenceHash("stringiiiiii");
        assertTrue(fileInfo.getImageDifferenceHash() instanceof String);
        //assertEquals(fileInfo.getImageDifferenceHash(), "STRING");
        assertNotNull(fileInfo);
    }

    @AfterAll
    static void tearDown() {
        deleteDatabase();
    }

    @Test
    public void createWorkdirDatabase() {
        Connection workDirConnection = workDirSQL.getConnection();
        if (!SQL_Utils.isDbConnected(workDirConnection)) {
            workDirSQL = new WorkDirSQL(databaseTemp);
        }
        SQL_Utils.setAutoCommit(workDirConnection, false);

//        SQL_Utils.closeConnection(workDirConnection);
//        assertEquals(false, SQL_Utils.isDbConnected(workDirConnection));
//

        //Connect again to workdir database
//        workDirSQL = new WorkDirSQL(databaseTemp);
        workDirConnection = workDirSQL.getConnection();
        SQL_Utils.setAutoCommit(workDirConnection, false);
        String url = SQL_Utils.getUrl(workDirConnection);
        System.out.println("url: " + url);
        assertTrue(url.contains("workDir.db"));
        assertEquals(true, SQL_Utils.isDbConnected(workDirConnection));
        assertNotNull(fileInfo);

//        workDirSQL.createFileInfoTable(workDirConnection);

        System.out.println("fileInfo::::::::::: " + fileInfo.showAllValues());
        workDirSQL.insertFileInfoToWorkDir(fileInfo);
        SQL_Utils.commitChanges(workDirConnection);
        SQL_Utils.closeConnection(workDirConnection);

    }

    private static final ObjectMapper PRETTY =
            JsonMapper.builder()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .build();

    public static String pretty(String json) {
        try {
            Object obj = PRETTY.readValue(json, Object.class);
            return PRETTY.writeValueAsString(obj);
        } catch (Exception e) {
            return json;
        }
    }

    private static boolean deleteDatabase() {
        System.out.println("teariiiing Down");
        Path file = Paths.get(databaseTemp.toString(), "workdir.db");

        if (workDirSQL != null) {
            SQL_Utils.closeConnection(workDirSQL.getConnection());
        }

        try {
            Files.deleteIfExists(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
