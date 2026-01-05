package com.girbola.drive;

import com.girbola.sql.DriveInfoSQL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class DriveInfoUtilsTest {


    @Test
    void testSaveList_whenDriveListIsEmpty() {
        // Arrange
        DriveInfoSQL driveInfoSQLMock = mock(DriveInfoSQL.class);
        DriveInfoUtils driveInfoUtils = new DriveInfoUtils(driveInfoSQLMock);
        ObservableList<DriveInfo> drivesListObs = FXCollections.observableArrayList();
        driveInfoUtils.getDrivesList_obs().addAll(drivesListObs);

        // Act
        driveInfoUtils.saveList();

        // Assert
        verify(driveInfoSQLMock, times(1)).addDriveInfos(drivesListObs);
    }

}