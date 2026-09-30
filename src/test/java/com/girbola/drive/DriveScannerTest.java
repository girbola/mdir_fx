package com.girbola.drive;

import com.girbola.controllers.folderscanner.ModelFolderScanner;
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
import org.junit.jupiter.api.io.TempDir;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DriveScannerTest {

    @Test
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
    void getHDiskStoreTestG() {
        List<HWDiskStore> hardwareDisks = OSHI_Utils.getHWDiskStore();
        assertFalse(hardwareDisks.isEmpty(), "Expected OSHI to return at least one disk");

        Path firstRoot = Paths.get("G:\\");

        HWDiskStore matchingDisk = OSHI_Utils.findDiskStoreForRoot(firstRoot, hardwareDisks);
        for(HWPartition partition : matchingDisk.getPartitions()) {
            System.out.println("Partition: " + partition.getMountPoint() + " Name: " + partition.getName() + " Type: " + partition.getType());
        }

        assertNotNull(matchingDisk, "Expected to resolve a disk store for the default root");

        List<HWPartition> partitions = matchingDisk.getPartitions();
        assertNotNull(partitions, "Expected partitions list to be available");
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

    @SuppressWarnings("unchecked")
    private void invokeAddMountedChildren(DriveScanner scanner, Set<Path> roots, Path mountParent) throws Exception {
        Method method = DriveScanner.class.getDeclaredMethod("addMountedChildren", Set.class, Path.class);
        method.setAccessible(true);
        method.invoke(scanner, roots, mountParent);
    }
}
