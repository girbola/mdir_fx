package com.girbola.drive;

import com.girbola.persistence.drive.DriveInfoDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class DriveInfoUtilsTest {


    @Test
    void testSaveList_whenDriveListIsEmpty() {
        // Arrange
        DriveInfoDao driveInfoDaoMock = mock(DriveInfoDao.class);
        DriveInfoUtils driveInfoUtils = new DriveInfoUtils(driveInfoDaoMock);
        ObservableList<DriveInfo> drivesListObs = FXCollections.observableArrayList();
        driveInfoUtils.getDrivesList_obs().addAll(drivesListObs);

        // Act
        driveInfoUtils.saveList();

        // Assert
        verify(driveInfoDaoMock, times(1)).addDriveInfos(drivesListObs);
    }

}