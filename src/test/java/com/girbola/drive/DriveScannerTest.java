package com.girbola.drive;

import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import common.utils.OSHI_Utils;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import static com.girbola.drive.DriveInfoUtils.parseDriveLetterFromPath;
import static com.girbola.drive.DriveInfoUtils.parseDrivePathFromPath;
import static com.girbola.drive.DriveInfoUtils.resolveConnectivityForSelectedFolder;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DriveScannerTest {

    @Test
    void testParseDriveLetterFromPath() {
        Path path = Paths.get("C:\\Users\\User\\Documents");
        String driveLetter = parseDriveLetterFromPath(path);
        Path changedPath = DriveInfoUtils.changeDriveLetterWithPath(path, "D:\\Media\\Movies");

        assertEquals("C:\\", driveLetter, "Expected the root path to be C:\\");
        assertEquals(Paths.get("D:\\Users\\User\\Documents"), changedPath,
                "Expected the path to be remapped to the new drive letter");
    }
    @Test
    @EnabledOnOs({OS.WINDOWS})
    void testDriveLetterChange() {
        Path currentDriveLetter = (Paths.get("D:\\"));

        Path selectedFolder = Paths.get("E:\\Temp\\");

        System.out.println("Selected folder: " + selectedFolder);
        String selectedFolderDriveLetter = parseDriveLetterFromPath(selectedFolder);
        String selectedFolderPathWithoutDriveLetter = parseDrivePathFromPath(selectedFolder);

        System.out.println("Selected drive letter: " + selectedFolderDriveLetter);
        System.out.println("Selected folder: " + selectedFolder);

        Path thePath = Paths.get(currentDriveLetter.toString(), selectedFolderPathWithoutDriveLetter);
        System.out.println("The path: " + thePath);

        assertEquals("D:\\Temp", thePath.toString(), "Expected drive letter to be D:\\" + selectedFolderPathWithoutDriveLetter);
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void testDriveLetterChange2(@TempDir Path tempDir) {
        Path currentDriveLetter = tempDir.getRoot();
        System.out.println("Current drive letter: " + currentDriveLetter);
        String selectedFolderPathWithoutDriveLetter = parseDrivePathFromPath(tempDir);
        Path originalFolderPath = Paths.get("Z:\\", selectedFolderPathWithoutDriveLetter);

        SelectedFolder selectedFolder = new SelectedFolder(originalFolderPath.toString(), true, false, true, true, "serial-123");
        DriveInfo currentDriveInfo = new DriveInfo(currentDriveLetter.toString(), 0, true, true, "serial-123");
        currentDriveInfo.setDrive(currentDriveLetter);

        Path resolvedPath = resolveConnectivityForSelectedFolder(selectedFolder, currentDriveInfo);

        assertNotNull(resolvedPath, "Expected the disconnected folder path to be remapped to the current drive");
        assertEquals(tempDir.normalize(), resolvedPath.normalize(),
                "Expected the remapped path to point to the existing directory on the current drive");
    }
    @Test
    @EnabledOnOs({OS.WINDOWS})
    void testDriveLetterChangeWithoutPathJustLetter() {
        Path currentDriveLetter = (Paths.get("C:\\"));

        Path selectedFolder = Paths.get("D:\\");

        System.out.println("Selected folder: " + selectedFolder);
        String selectedFolderDriveLetter = parseDriveLetterFromPath(selectedFolder);
        String selectedFolderPathWithoutDriveLetter = parseDrivePathFromPath(selectedFolder);

        System.out.println("Selected drive letter: " + selectedFolderDriveLetter);
        System.out.println("Selected folder: " + selectedFolder);

        Path thePath = Paths.get(currentDriveLetter.toString(), selectedFolderPathWithoutDriveLetter);
        System.out.println("The path: " + thePath);

        assertEquals("C:\\", thePath.toString(), "Expected drive letter to be C:\\" + selectedFolderPathWithoutDriveLetter);
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    void testDriveLetterChangeWithoutPathJustLetterOnLinux() {
        Path currentDriveLetter = (Paths.get("/mnt/c/"));

        Path selectedFolder = Paths.get("/mnt/d/");

        System.out.println("Selected folder: " + selectedFolder);
        String selectedFolderDriveLetter = parseDriveLetterFromPath(selectedFolder);
        String selectedFolderPathWithoutDriveLetter = parseDrivePathFromPath(selectedFolder);

        System.out.println("Selected drive letter: " + selectedFolderDriveLetter);
        System.out.println("Selected folder: " + selectedFolder);

        Path thePath = Paths.get(currentDriveLetter.toString(), selectedFolderPathWithoutDriveLetter);
        System.out.println("The path: " + thePath);

        assertEquals("/mnt/c/", thePath.toString(), "Expected drive letter to be /mnt/c/" + selectedFolderPathWithoutDriveLetter);
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void getHDiskStoreTest() {
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();
        assertFalse(hardwareDisks.isEmpty(), "Expected OSHI to return at least one disk");

        Path firstRoot = FileSystems.getDefault().getRootDirectories().iterator().next();
        HWDiskStore matchingDisk = OSHI_Utils.findDiskStoreForRoot(firstRoot, hardwareDisks);
        assertNotNull(matchingDisk, "Expected to resolve a disk store for the default root");

        List<HWPartition> partitions = matchingDisk.getPartitions();
        assertNotNull(partitions, "Expected partitions list to be available");
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void getHDiskStoreTestNonExistingDrive() {
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();
        assertFalse(hardwareDisks.isEmpty(), "Expected OSHI to return at least one disk");

        Path firstRoot = Paths.get("thedrivewhichdoesnotexists");
        org.junit.jupiter.api.Assumptions.assumeFalse(Files.exists(firstRoot), "Drive thedrivewhichdoesnotexists: is not available on this machine");

        HWDiskStore matchingDisk = OSHI_Utils.findDiskStoreForRoot(firstRoot, hardwareDisks);
        assertNull(matchingDisk, "Expected to resolve a disk store for the default root");
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    void getHDiskStoreTestNonExistingDriveOnLinux() {
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();
        assertFalse(hardwareDisks.isEmpty(), "Expected OSHI to return at least one disk");

        Path firstRoot = Paths.get("/mnt/thedrivewhichdoesnotexists");
        org.junit.jupiter.api.Assumptions.assumeFalse(Files.exists(firstRoot), "Drive /mnt/thedrivewhichdoesnotexists: is not available on this machine");

        HWDiskStore matchingDisk = OSHI_Utils.findDiskStoreForRoot(firstRoot, hardwareDisks);
        assertNull(matchingDisk, "Expected to resolve a disk store for the default root");
    }

    @Test
    void returnsExistingSortedAndUniqueRoots() throws Exception {
        DriveScanner scanner = createScanner();

        File[] roots = invokeGetListOfRoots(scanner);

        assertNotNull(roots);
        assertTrue(roots.length > 0, "Expected at least one root directory");

        String[] paths = Arrays.stream(roots)
                .map(file -> file.getAbsoluteFile().toPath().normalize().toString())
                .toArray(String[]::new);

        for (File root : roots) {
            assertTrue(root.exists(), () -> "Root does not exist: " + root);
            assertTrue(root.isDirectory(), () -> "Root is not a directory: " + root);
        }

        String[] sortedPaths = Arrays.copyOf(paths, paths.length);
        Arrays.sort(sortedPaths, String::compareToIgnoreCase);
        assertArrayEquals(sortedPaths, paths, "Roots should be sorted case-insensitively by absolute path");

        Set<String> uniquePaths = Arrays.stream(paths)
                .map(path -> path.toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toSet());
        assertEquals(paths.length, uniquePaths.size(), "Roots should not contain duplicates");
    }

    @Test
    void includesAllDefaultFileSystemRootDirectories() throws Exception {
        DriveScanner scanner = createScanner();

        Set<String> discovered = Arrays.stream(invokeGetListOfRoots(scanner))
                .map(file -> file.getAbsoluteFile().toPath().normalize().toString().toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toSet());
        long startTime = System.currentTimeMillis();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            String normalized = root.toAbsolutePath().normalize().toString().toLowerCase(java.util.Locale.ROOT);
            assertTrue(discovered.contains(normalized), () -> "Missing default root: " + normalized);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to check default root directories: " + (endTime - startTime) + " ms");
    }

    @Test
    void addMountedChildrenSkipsNonDirectoryParent(@TempDir Path tempDir) throws Exception {
        DriveScanner scanner = createScanner();
        Set<Path> roots = new HashSet<>();
        Path fileParent = Files.createFile(tempDir.resolve("not-a-directory.txt"));

        invokeAddMountedChildren(scanner, roots, fileParent);

        assertTrue(roots.isEmpty());
    }

    @Test
    void addMountedChildrenAddsOnlyDirectories(@TempDir Path tempDir) throws Exception {
        DriveScanner scanner = createScanner();
        Set<Path> roots = new HashSet<>();
        Path mountParent = Files.createDirectory(tempDir.resolve("mount-parent"));
        Path dirChild = Files.createDirectory(mountParent.resolve("usb1"));
        Files.createFile(mountParent.resolve("plain-file.txt"));

        invokeAddMountedChildren(scanner, roots, mountParent);

        Set<Path> normalizedRoots = roots.stream()
                .map(path -> path.toAbsolutePath().normalize())
                .collect(Collectors.toSet());
        assertEquals(1, normalizedRoots.size());
        assertTrue(normalizedRoots.contains(dirChild.toAbsolutePath().normalize()));
        assertFalse(normalizedRoots.contains(mountParent.resolve("plain-file.txt").toAbsolutePath().normalize()));
    }

    @Test
    void identifiesDriveOnlyWhenSavedFileInfoSerialsMatch() {
        FolderInfo folderInfo = new FolderInfo();
        FileInfo matchingFile = new FileInfo();
        matchingFile.setOrgPathDriveSerialNumber("volume-123");
        FileInfo secondMatchingFile = new FileInfo();
        secondMatchingFile.setOrgPathDriveSerialNumber("VOLUME-123");
        folderInfo.setFileInfoList(new ArrayList<>(List.of(matchingFile, secondMatchingFile)));

        assertTrue(DriveScanner.hasMatchingFileInfoDriveSerial(folderInfo, "volume-123"));
        assertFalse(DriveScanner.hasMatchingFileInfoDriveSerial(folderInfo, "volume-456"));
    }

    @Test
    void doesNotIdentifyDriveWhenSavedFileInfoSerialsAreMissing() {
        FolderInfo folderInfo = new FolderInfo();
        FileInfo fileInfo = new FileInfo();
        folderInfo.setFileInfoList(new ArrayList<>(List.of(fileInfo)));

        assertFalse(DriveScanner.hasMatchingFileInfoDriveSerial(folderInfo, "volume-123"));
    }

    @Test
    void doesNotIdentifyDriveWhenSavedFileInfoSerialsConflict() {
        FolderInfo folderInfo = new FolderInfo();
        FileInfo matchingFile = new FileInfo();
        matchingFile.setOrgPathDriveSerialNumber("volume-123");
        FileInfo conflictingFile = new FileInfo();
        conflictingFile.setOrgPathDriveSerialNumber("volume-456");
        folderInfo.setFileInfoList(new ArrayList<>(List.of(matchingFile, conflictingFile)));

        assertFalse(DriveScanner.hasMatchingFileInfoDriveSerial(folderInfo, "volume-123"));
    }

    private DriveScanner createScanner() {
        ModelMain modelMain = mock(ModelMain.class);
        CheckBoxTreeItem<Path> rootItem = new CheckBoxTreeItem<>(Path.of("."));
        ObservableList<Path> driveListSelectedObs = FXCollections.observableArrayList();
        DriveInfoUtils driveInfoUtils = mock(DriveInfoUtils.class);
        ModelFolderScanner modelFolderScanner = mock(ModelFolderScanner.class);
        return new DriveScanner(modelMain, rootItem, driveListSelectedObs, driveInfoUtils, modelFolderScanner);
    }

    private File[] invokeGetListOfRoots(DriveScanner scanner) throws Exception {
        Method method = DriveScanner.class.getDeclaredMethod("getListOfRoots");
        method.setAccessible(true);
        return (File[]) method.invoke(scanner);
    }

    private void invokeAddMountedChildren(DriveScanner scanner, Set<Path> roots, Path mountParent) throws Exception {
        Method method = DriveScanner.class.getDeclaredMethod("addMountedChildren", Set.class, Path.class);
        method.setAccessible(true);
        method.invoke(scanner, roots, mountParent);
    }
}
