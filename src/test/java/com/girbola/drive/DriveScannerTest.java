package com.girbola.drive;

import com.girbola.controllers.folderscanner.SelectedFolder;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import common.utils.OSHI_Utils;
import java.io.File;
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
import static org.mockito.Mockito.when;

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
        File[] roots = DriveScannerUtils.getListOfRoots();

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
        Set<String> discovered = Arrays.stream(DriveScannerUtils.getListOfRoots())
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
        Set<Path> roots = new HashSet<>();
        Path fileParent = Files.createFile(tempDir.resolve("not-a-directory.txt"));

        DriveScannerUtils.addMountedChildren(roots, fileParent);

        assertTrue(roots.isEmpty());
    }

    @Test
    void addMountedChildrenAddsOnlyDirectories(@TempDir Path tempDir) throws Exception {
        Set<Path> roots = new HashSet<>();
        Path mountParent = Files.createDirectory(tempDir.resolve("mount-parent"));
        Path dirChild = Files.createDirectory(mountParent.resolve("usb1"));
        Files.createFile(mountParent.resolve("plain-file.txt"));

        DriveScannerUtils.addMountedChildren(roots, mountParent);

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

        assertTrue(SelectedFolderDriveManager.hasMatchingFileInfoDriveSerial(folderInfo, "volume-123"));
        assertFalse(SelectedFolderDriveManager.hasMatchingFileInfoDriveSerial(folderInfo, "volume-456"));
    }

    @Test
    void doesNotIdentifyDriveWhenSavedFileInfoSerialsAreMissing() {
        FolderInfo folderInfo = new FolderInfo();
        FileInfo fileInfo = new FileInfo();
        folderInfo.setFileInfoList(new ArrayList<>(List.of(fileInfo)));

        assertFalse(SelectedFolderDriveManager.hasMatchingFileInfoDriveSerial(folderInfo, "volume-123"));
    }

    @Test
    void doesNotIdentifyDriveWhenSavedFileInfoSerialsConflict() {
        FolderInfo folderInfo = new FolderInfo();
        FileInfo matchingFile = new FileInfo();
        matchingFile.setOrgPathDriveSerialNumber("volume-123");
        FileInfo conflictingFile = new FileInfo();
        conflictingFile.setOrgPathDriveSerialNumber("volume-456");
        folderInfo.setFileInfoList(new ArrayList<>(List.of(matchingFile, conflictingFile)));

        assertFalse(SelectedFolderDriveManager.hasMatchingFileInfoDriveSerial(folderInfo, "volume-123"));
    }

    @Test
    void createDriveInfoCopiesDiskAndPartitionIdentity(@TempDir Path tempDir) {
        HWDiskStore diskStore = mock(HWDiskStore.class);
        HWPartition partition = mock(HWPartition.class);
        when(diskStore.getSerial()).thenReturn(" disk-serial ");
        when(diskStore.getSize()).thenReturn(1024L);
        when(partition.getUuid()).thenReturn(" partition-uuid ");
        when(partition.getIdentification()).thenReturn(" partition-id ");
        when(partition.getMountPoint()).thenReturn(tempDir.toString());

        DriveInfo driveInfo = DriveInventoryScanner.createDriveInfo(tempDir, diskStore, partition);

        assertEquals(tempDir.toString(), driveInfo.getDrivePath());
        assertEquals(1024L, driveInfo.getDriveTotalSize());
        assertEquals("partition-uuid", driveInfo.getIdentifier());
        assertEquals("disk-serial", driveInfo.getSerial());
        assertEquals("partition-uuid", driveInfo.getPartitionUuid());
        assertEquals("partition-id", driveInfo.getPartitionIdentification());
        assertEquals(tempDir.toString(), driveInfo.getPartitionMountPoint());
        assertTrue(driveInfo.isConnected());
        assertFalse(driveInfo.isSelected());
    }

    @Test
    void registerDriveInfoAddsNewDriveAndUpdatesExistingByIdentity() {
        DriveInfo existingDrive = new DriveInfo("C:\\", 1024, false, true, "partition-uuid");
        List<DriveInfo> registeredDriveInfos = new ArrayList<>(List.of(existingDrive));
        DriveInfo discoveredExisting = new DriveInfo("D:\\", 1024, false, false, "partition-uuid");

        assertFalse(DriveInventoryScanner.registerDriveInfo(discoveredExisting, registeredDriveInfos));

        assertEquals(1, registeredDriveInfos.size());
        assertTrue(existingDrive.isConnected());
        assertTrue(existingDrive.isSelected());

        DriveInfo newDrive = new DriveInfo("E:\\", 2048, false, true, "new-uuid");
        assertTrue(DriveInventoryScanner.registerDriveInfo(newDrive, registeredDriveInfos));

        assertEquals(2, registeredDriveInfos.size());
        assertTrue(newDrive.isConnected());
        assertFalse(newDrive.isSelected());
    }

    @Test
    void scanRootsSkipsMissingAndUnmatchedRootsAndDeduplicatesDiscoveredDrive(@TempDir Path tempDir)
            throws Exception {
        Path unmatchedFile = Files.createFile(tempDir.resolve("ordinary-file.txt"));
        Path missingRoot = tempDir.resolve("missing");
        HWDiskStore diskStore = mock(HWDiskStore.class);
        HWPartition partition = mock(HWPartition.class);
        when(diskStore.getPartitions()).thenReturn(List.of(partition));
        when(diskStore.getSerial()).thenReturn("disk-serial");
        when(diskStore.getSize()).thenReturn(1024L);
        when(diskStore.getName()).thenReturn("disk-name");
        when(partition.getMountPoint()).thenReturn(tempDir.toString());
        when(partition.getUuid()).thenReturn("partition-uuid");
        when(partition.getIdentification()).thenReturn("partition-id");

        List<DriveInfo> registeredDriveInfos = new ArrayList<>();
        DriveInventoryScanner.Result result = new DriveInventoryScanner().scanRoots(
                List.of(tempDir, tempDir, missingRoot, unmatchedFile),
                List.of(diskStore),
                registeredDriveInfos,
                () -> false);

        assertEquals(List.of(diskStore), result.rootDrives());
        assertEquals(1, result.driveKeys().size());
        assertTrue(result.driveInfosChanged());
        assertEquals(1, registeredDriveInfos.size());
        assertEquals(4, result.rootPaths().size());
    }

    @Test
    void scanRootsStopsWhenCancelled(@TempDir Path tempDir) {
        List<DriveInfo> registeredDriveInfos = new ArrayList<>();

        DriveInventoryScanner.Result result = new DriveInventoryScanner().scanRoots(
                List.of(tempDir), List.of(), registeredDriveInfos, () -> true);

        assertTrue(result.rootDrives().isEmpty());
        assertTrue(result.driveKeys().isEmpty());
        assertTrue(registeredDriveInfos.isEmpty());
    }

    @Test
    void snapshotChangeDetectionComparesDriveKeysAndRootPaths() {
        DriveInventoryScanner.Result result = new DriveInventoryScanner.Result(
                List.of(), Set.of(), false, List.of("C:\\", "D:\\"));

        assertFalse(DriveInventoryScanner.hasChanged(result, List.of(), List.of("C:\\", "D:\\")));
        assertTrue(DriveInventoryScanner.hasChanged(result, List.of("new-drive"), List.of("C:\\", "D:\\")));
        assertTrue(DriveInventoryScanner.hasChanged(result, List.of(), List.of("C:\\")));
    }
}
