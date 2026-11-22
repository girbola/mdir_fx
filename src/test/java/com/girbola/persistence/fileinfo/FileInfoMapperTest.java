package com.girbola.persistence.fileinfo;

import com.girbola.fileinfo.CopyLifecycleState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoEnum;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileInfoMapperTest {

    @Test
    void bindsCopyLifecycleState() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.getFileHistories()).thenReturn(List.of());
        when(fileInfo.getCopyLifecycleState()).thenReturn(CopyLifecycleState.PROCESS);

        assertTrue(FileInfoMapper.bindToFileInfoStatement(statement, fileInfo));
        verify(statement).setString(
                FileInfoEnum.COPY_LIFECYCLE_STATE.getBindIndex(),
                CopyLifecycleState.PROCESS.name());
        assertTrue(FileInfoEnum.getCreateTableSQL("fileinfo").contains("copyLifecycleState TEXT"));
    }
}
