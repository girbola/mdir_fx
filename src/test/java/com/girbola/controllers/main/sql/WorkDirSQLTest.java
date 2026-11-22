package com.girbola.controllers.main.sql;

import com.girbola.Main;
import com.girbola.configuration.Configuration;
import com.girbola.fileinfo.CopyLifecycleState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkDirSQLTest {

    @TempDir
    Path workDirectory;

    @Test
    void persistsCopyLifecycleTransitions() throws Exception {
        Configuration originalConfiguration = Main.conf;
        Configuration configuration = mock(Configuration.class);
        when(configuration.getWorkDir_db_fileName()).thenReturn("workdir.db");
        Main.conf = configuration;

        WorkDirSQL workDirSQL = new WorkDirSQL(workDirectory);
        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileInfo_id(1);
            fileInfo.setOrgPath(workDirectory.resolve("source.jpg").toString());
            fileInfo.setFileHistories(List.of());
            fileInfo.setCopyLifecycleState(CopyLifecycleState.NEW);
            assertTrue(workDirSQL.insertFileInfo(fileInfo));

            fileInfo.setCopyLifecycleState(CopyLifecycleState.PROCESS);
            assertTrue(workDirSQL.insertFileInfo(fileInfo));

            fileInfo.setCopyLifecycleState(CopyLifecycleState.DONE);
            fileInfo.setCopied(true);
            assertTrue(workDirSQL.insertFileInfo(fileInfo));

            String sql = "SELECT " + FileInfoEnum.COPY_LIFECYCLE_STATE.getColumnName()
                    + " FROM fileinfo WHERE " + FileInfoEnum.ORGPATH.getColumnName() + " = ?";
            try (PreparedStatement statement = workDirSQL.getConnection().prepareStatement(sql)) {
                statement.setString(1, fileInfo.getOrgPath());
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertEquals(CopyLifecycleState.DONE.name(),
                            resultSet.getString(FileInfoEnum.COPY_LIFECYCLE_STATE.getColumnName()));
                }
            }
        } finally {
            workDirSQL.getConnection().close();
            Main.conf = originalConfiguration;
        }
    }
}
