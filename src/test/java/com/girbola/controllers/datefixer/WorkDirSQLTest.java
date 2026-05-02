package com.girbola.controllers.datefixer;

import com.girbola.controllers.main.sql.WorkDirSQL;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.persistence.fileinfo.FileInfoDao;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkDirSQLTest {

    private FileInfo fileInfo;
    private Path path;
    private Connection connectionMock;
    private PreparedStatement statementMock;

    //@BeforeEach
    public void setup() {
        Messages.sprintf("SETUPPR!#PRPIEOG¤%WPOJTEB");
        fileInfo = new FileInfo();
        fileInfo.setFileInfo_id(1);
        path = Path.of("tmp");
        connectionMock = mock(Connection.class);
        statementMock = mock(PreparedStatement.class);
    }

//  //  @Test
//    public void insertFileInfo_validInfo() throws SQLException {
//
//        when(connectionMock.prepareStatement(anyString())).thenReturn(statementMock);
//        when(statementMock.executeUpdate()).thenAnswer(new Answer() {
//            private int count = 0;
//
//            public Object answer(InvocationOnMock invocation) {
//                return count++;
//            }
//        });
//        FileInfoDao.insertFileInfo(fileInfo);
//    }
//
//    //@Test
//    public void insertFileInfo_throwsException() throws SQLException {
//        when(connectionMock.prepareStatement(any())).thenThrow(SQLException.class);
//        FileInfoDao.insertFileInfo(fileInfo);
//    }

}