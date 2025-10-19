package com.girbola.controllers.datefixer;


import com.girbola.controllers.main.sql.WorkDirSQL;
import com.girbola.fileinfo.FileInfo;
import com.girbola.sql.SQL_Utils;
import com.girbola.utils.FileInfoUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkDirSQL2Test {


    private FileInfo fileInfo;
    private WorkDirSQL workDirSQL;
    private static Path databaseTemp = Paths.get("src", "test", "resources");

    @BeforeEach
    public void setup() throws IOException {
        fileInfo = new FileInfo();
        fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "test-material", "IMG.jpg"));
        fileInfo.setFileInfo_id(0);
        fileInfo.setImageDifferenceHash("stringiiiiii");
        assertTrue(fileInfo.getImageDifferenceHash() instanceof String);
        //assertEquals(fileInfo.getImageDifferenceHash(), "STRING");
        assertNotNull(fileInfo);

        workDirSQL = new WorkDirSQL(databaseTemp);
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("teariiiing Down");
        Path file = Paths.get(databaseTemp.toString(), "workdir.db");
//        try {
//            Files.deleteIfExists(file);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Test
    public void createWorkdirDatabase() {
        Connection workDirConnection = workDirSQL.getConnection();
        SQL_Utils.setAutoCommit(workDirConnection, false);
        SQL_Utils.commitChanges(workDirConnection);
        assertEquals(true, SQL_Utils.isDbConnected(workDirConnection));
        SQL_Utils.closeConnection(workDirConnection);
        assertEquals(false, SQL_Utils.isDbConnected(workDirSQL.getConnection()));
        //Connect again to workdir database
        workDirSQL.checkConnection();
        workDirConnection = workDirSQL.getConnection();
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


//
//        SQL_Utils.commitChanges(workDirConnection);
//        SQL_Utils.closeConnection(workDirConnection);

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
}
