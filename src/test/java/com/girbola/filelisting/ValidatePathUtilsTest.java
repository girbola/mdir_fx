package com.girbola.filelisting;

import com.girbola.misc.Misc;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatePathUtilsTest {

    @Test
    void isInSkippedFolderList_Windows() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(true);
            miscMock.when(Misc::isUnix).thenReturn(false);
            miscMock.when(Misc::isMac).thenReturn(false);

            Path testPath = Paths.get("test/path/file");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            testPath = Paths.get("app");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));

            testPath = Paths.get("APPDATA");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }

    @Test
    void isInSkippedFolderList_Unix() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(false);
            miscMock.when(Misc::isUnix).thenReturn(true);
            miscMock.when(Misc::isMac).thenReturn(false);

            Path testPath = Paths.get("test");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            testPath = Paths.get("lib");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }

    @Test
    void isInSkippedFolderList_MacOS() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(false);
            miscMock.when(Misc::isUnix).thenReturn(false);
            miscMock.when(Misc::isMac).thenReturn(true);

            Path testPath = Paths.get("test/path/file");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));

            testPath = Paths.get("Library");
            assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }
}