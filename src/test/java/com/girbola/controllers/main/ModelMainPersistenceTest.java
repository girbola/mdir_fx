package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.model.SavedFolderInfos;
import com.girbola.fileinfo.FileInfo;
import com.girbola.persistence.configuration.ConfigurationSavedFoldersDao;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.sql.SQL_Utils;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ModelMainPersistenceTest {

    @Test
    void emptyTableIsSuccessfulAndLeavesConnectionOpen() throws Exception {
        Connection connection = mock(Connection.class);
        ModelMainPersistence persistence = new ModelMainPersistence(null, null);

        assertTrue(persistence.saveTableContent(connection, FXCollections.observableArrayList(), "sorted"));
        verify(connection, never()).close();
    }

    @Test
    void failedFolderSaveIsReportedAndRemainingFoldersAreProcessed() {
        Connection connection = mock(Connection.class);
        FolderInfo missing = folder("missing");
        FolderInfo available = folder("available");
        ModelMainPersistence persistence = new ModelMainPersistence(null, null);

        try (MockedStatic<ConfigurationSavedFoldersDao> savedFolders = mockStatic(ConfigurationSavedFoldersDao.class);
             MockedStatic<FolderInfoDao> folderDao = mockStatic(FolderInfoDao.class);
             MockedStatic<SQL_Utils> sql = mockStatic(SQL_Utils.class)) {
            savedFolders.when(() -> ConfigurationSavedFoldersDao.insertSavedFoldersIntoConfigurationDatabase(
                    eq(connection), any(SavedFolderInfos.class))).thenReturn(true);
            folderDao.when(() -> FolderInfoDao.insertFileInfoListToFileInfoDatabase(missing, false)).thenReturn(false);
            folderDao.when(() -> FolderInfoDao.insertFileInfoListToFileInfoDatabase(available, false)).thenReturn(true);

            assertFalse(persistence.saveTableContent(connection,
                    FXCollections.observableArrayList(missing, available), "sorted"));
            folderDao.verify(() -> FolderInfoDao.insertFileInfoListToFileInfoDatabase(available, false));
            sql.verify(() -> SQL_Utils.closeConnection(connection), never());
        }
    }

    private FolderInfo folder(String name) {
        FolderInfo folder = new FolderInfo(Path.of(name), "sorted", name, true);
        folder.getFileInfoList().add(mock(FileInfo.class));
        return folder;
    }
}
