package com.girbola.controllers.datefixer;


import com.girbola.controllers.main.sql.WorkDirSQL;
import com.girbola.fileinfo.FileInfo;
import com.girbola.sql.SQL_Utils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class WorkDirSQL2Test {


    private FileInfo fileInfo;
    private Path path;
    private Connection connectionMock;
    private PreparedStatement statementMock;
    private WorkDirSQL workDirSQL;

    @BeforeEach
    public void setup() {
        fileInfo = new FileInfo();
        fileInfo.setFileInfo_id(1);
        path = Path.of("tmp");
        workDirSQL = new WorkDirSQL(Paths.get("."));
        connectionMock = mock(Connection.class);
        statementMock = mock(PreparedStatement.class);
    }

    @Test
    public void createWorkdirDatabase() {
        Connection workDirConnection = workDirSQL.getConnection();
        assertEquals(true, SQL_Utils.isDbConnected(workDirConnection));
        SQL_Utils.closeConnection(workDirConnection);
    }
}
