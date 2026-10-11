package com.girbola.drive;

import com.girbola.persistence.drive.DriveInfoDao;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @Test
    void testLoadingAllRootPaths() {
        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();

        assertNotNull(rootDirectories, "Root directories iterable should not be null");

        // Ensure at least one root exists (all supported OS environments have at least one root)
        assertTrue(rootDirectories.iterator().hasNext(), "There should be at least one root directory");

        for (Path root : rootDirectories) {
            assertNotNull(root, "Root path should not be null");

            Path normalizedRoot = root.toAbsolutePath().normalize();

            // Assert that the normalized path string is non-empty
            assertFalse(normalizedRoot.toString().isBlank(), "Root path string should not be blank");

            // Optional: Check basic file system properties
            assertTrue(Files.exists(normalizedRoot), () -> "Root directory should exist: " + normalizedRoot);
            assertTrue(Files.isDirectory(normalizedRoot), () -> "Root should be a directory: " + normalizedRoot);

            System.out.println("Root path: " + normalizedRoot);
        }
    }

}
