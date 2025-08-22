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
        DriveInfoUtils driveInfoUtils = new DriveInfoUtils();
        ObservableList<DriveInfo> drivesListObs = FXCollections.observableArrayList();
        driveInfoUtils.getDrivesList_obs().addAll(drivesListObs);

        // Mock DriveInfoSQL
        DriveInfoSQL driveInfoSQLMock = mock(DriveInfoSQL.class);
        Mockito.doNothing().when(driveInfoSQLMock).addDriveInfos(drivesListObs);

        // Act
        driveInfoUtils.saveList();

        // Assert
        verify(driveInfoSQLMock, times(1)).addDriveInfos(drivesListObs);
    }

    @Test
    void testSaveList_whenDriveListHasItems() {
        // Arrange
        DriveInfoUtils driveInfoUtils = new DriveInfoUtils();
        ObservableList<DriveInfo> drivesListObs = FXCollections.observableArrayList();
        drivesListObs.add(new DriveInfo("C:\\", 500000L, true, true, "12345"));
        drivesListObs.add(new DriveInfo("D:\\", 1000000L, true, false, "67890"));
        driveInfoUtils.getDrivesList_obs().addAll(drivesListObs);

        // Mock DriveInfoSQL
        DriveInfoSQL driveInfoSQLMock = mock(DriveInfoSQL.class);
        Mockito.doNothing().when(driveInfoSQLMock).addDriveInfos(drivesListObs);

        // Act
        driveInfoUtils.saveList();

        // Assert
        verify(driveInfoSQLMock, times(1)).addDriveInfos(drivesListObs);
    }
}