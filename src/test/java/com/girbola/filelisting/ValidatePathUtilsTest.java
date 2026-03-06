package com.girbola.filelisting;

import com.girbola.misc.Misc;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatePathUtilsTest {

    @Test
    void isInSkippedFolderList_Windows() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(true);
            miscMock.when(Misc::isUnix).thenReturn(false);
            miscMock.when(Misc::isMac).thenReturn(false);

            // Scenario: Path is not in the skipped folder list
            Path testPath = Paths.get("test/path/file");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Path "app" should be skipped
            testPath = Paths.get("app");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Skipped folder "AppData" (case insensitive)
            testPath = Paths.get("APPDATA");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Hidden file should be skipped
            testPath = Paths.get(".hiddenFile");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Mixed casing in skipped folder name
            testPath = Paths.get("aPpDaTa");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Path containing skipped folder name as a substring is not skipped
            testPath = Paths.get("AppDataBackup");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }

    @Test
    void isInSkippedFolderList_Unix() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(false);
            miscMock.when(Misc::isUnix).thenReturn(true);
            miscMock.when(Misc::isMac).thenReturn(false);

            // Scenario: Path is not in the skipped folder list
            Path testPath = Paths.get("test");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Path "lib" should be skipped
            testPath = Paths.get("lib");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Hidden file starting with "." is skipped
            testPath = Paths.get(".hiddenUnixFile");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Path containing skipped folder name as a substring is not skipped
            testPath = Paths.get("libBackup");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }

    @Test
    void isInSkippedFolderList_MacOS() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(false);
            miscMock.when(Misc::isUnix).thenReturn(false);
            miscMock.when(Misc::isMac).thenReturn(true);

            // Scenario: Path is not in the skipped folder list
            Path testPath = Paths.get("test/path/file");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Path "Library" should be skipped
            testPath = Paths.get("Library");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Mixed case "LiBrArY" should be skipped
            testPath = Paths.get("LiBrArY");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            // Scenario: Path containing skipped folder name as a substring is not skipped
            testPath = Paths.get("LibraryBackup");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }
}