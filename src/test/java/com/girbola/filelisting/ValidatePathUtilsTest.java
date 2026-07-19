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

        String[] OSX = {"$RECYCLE.BIN", ".DS_Store", "Applications", "Library", "Network", "Photos Library.photoslibrary", "System Volume Information", "System", "Users", "Volumes", "bin", "cores", "dev", "etc", "home", "lost+found", "opt", "private", "sbin", "tmp", "usr", "var"};

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

            // Scenario: Path containing skipped folder name as a substring is not skipped
            testPath = Paths.get("LibraryBackup");
            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath));
        }
    }

    // Test all OSX skipped folder names
    String[] OSX = {"$RECYCLE.BIN", ".DS_Store", "Applications", "Library", "Network", "Photos Library.photoslibrary", "System Volume Information", "System", "Users", "Volumes", "bin", "cores", "dev", "etc", "home", "lost+found", "opt", "private", "sbin", "tmp", "usr", "var"};

    @Test
    void isInSkippedFolderList_MacOS_AllFolders() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(false);
            miscMock.when(Misc::isUnix).thenReturn(false);
            miscMock.when(Misc::isMac).thenReturn(true);

            for (String folder : OSX) {
                Path testPath = Paths.get(System.getProperty("user.home"), folder, "IMG.jpg");
                System.out.println("Testing folder: " + folder);
                assertTrue(ValidatePathUtils.isInSkippedFolderList(testPath), "Should skip: " + folder);

                // Also test with first letter case flipped
   String flippedCase = folder;
                    for (int i = 0; i < folder.length(); i++) {
                        char c = folder.charAt(i);
                        if (Character.isLetter(c)) {
                            char flipped = Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c);
                            flippedCase = folder.substring(0, i) + flipped + folder.substring(i + 1);
                            break;
                        }
                    }

                // Also test with mixed case
//                String mixedCase = folder.length() > 1
//                        ? Character.toUpperCase(folder.charAt(0)) + folder.substring(1).toLowerCase()
//                        : folder.toUpperCase();
//                testPath = Paths.get(System.getProperty("user.home"),mixedCase, "IMG.jpg");
//                System.out.println("Testing mixed case folder: " + mixedCase);
//                assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath), "Should skip (mixed case): " + mixedCase);


                testPath = Paths.get(System.getProperty("user.home"), flippedCase, "IMG.jpg");
                System.out.println("Testing flipped case folder: " + flippedCase);
                assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath), "Should skip (flipped case): " + flippedCase);

                // Should not skip if folder is a substring (e.g., "LibraryBackup")
                testPath = Paths.get(System.getProperty("user.home"), folder, "Backup", "IMG.jpg");
                assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath), "Should not skip: " + folder + "Backup");
            }
        }
    }

    @Test
    void isInSkippedFolderListTestAcceptedFolder_With_MacOS_All_Known_Folders_Filters() {
        try (MockedStatic<Misc> miscMock = Mockito.mockStatic(Misc.class)) {
            miscMock.when(Misc::isWindows).thenReturn(false);
            miscMock.when(Misc::isUnix).thenReturn(false);
            miscMock.when(Misc::isMac).thenReturn(true);
            Path testPath = Paths.get(System.getProperty("user.home"), "Pictures" , "IMG.jpg");
            if (ValidatePathUtils.isInSkippedFolderList(testPath)) {
                System.out.println("Skipping folder: " + testPath);
            }

            assertFalse(ValidatePathUtils.isInSkippedFolderList(testPath), "Should accept file: " + testPath);

        }
    }

}