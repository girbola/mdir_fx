package com.girbola.controllers.datefixer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.workdir.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WorkDirSQLTest {

    private FileInfo fileInfo;
    private Path path;
    private Connection connectionMock;
    private PreparedStatement statementMock;

    @BeforeEach
    public void setup() {
        fileInfo = new FileInfo();
        fileInfo.setFileInfo_id(1);
        path = Path.of("tmp");
        connectionMock = mock(Connection.class);
        statementMock = mock(PreparedStatement.class);
    }

    @Test
    public void insertFileInfo_validInfo() throws SQLException {

        when(connectionMock.prepareStatement(anyString())).thenReturn(statementMock);
        when(statementMock.executeUpdate()).thenAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                return count++;
            }
        });
        WorkDirSQL.insertFileInfo(fileInfo);
    }

    @Test
    public void insertFileInfo_throwsException() throws SQLException {
        when(connectionMock.prepareStatement(any())).thenThrow(SQLException.class);
        WorkDirSQL.insertFileInfo(fileInfo);
    }

}