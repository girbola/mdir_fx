package com.girbola.controllers.main.folderinfoscan;

import com.girbola.Main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.filelisting.GetRootFiles;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import common.utils.FileUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import static com.girbola.utils.FileInfoUtils.createFileInfo;
import static common.utils.FileUtils.supportedMediaFormat;

public class FolderInfoScanner extends Task<Integer> {

    private final Tables tables;
    private final List<Path> selectedFolders;

    private final Map<String, FolderInfo> existingFoldersByPath = new HashMap<>();

    private final List<FolderInfo> sortItFoldersToAdd = new ArrayList<>();
    private final List<FolderInfo> sortedFoldersToAdd = new ArrayList<>();
    private final List<FolderInfo> asItIsFoldersToAdd = new ArrayList<>();
    private final List<FolderInfo> changedExistingFolders = new ArrayList<>();

    public FolderInfoScanner(Tables tables, List<Path> selectedFolders) {
        this.tables = tables;
        this.selectedFolders = selectedFolders == null ? List.of() : List.copyOf(selectedFolders);

        /*
         * TableView items must be read on the JavaFX Application Thread.
         * This constructor is created from JavaFX event handlers, so taking the snapshot here
         * avoids reading TableView content from call().
         */
        snapshotExistingFolders();
    }

    private void snapshotExistingFolders() {
        existingFoldersByPath.clear();
        snapshotTable(tables.getSortIt_table());
        snapshotTable(tables.getSorted_table());
        snapshotTable(tables.getAsItIs_table());
    }

    private void snapshotTable(TableView<FolderInfo> table) {
        for (FolderInfo folderInfo : table.getItems()) {
            if (folderInfo.getFolderPath() == null || folderInfo.getFolderPath().isBlank()) {
                continue;
            }

            existingFoldersByPath.put(
                    normalizePathString(Path.of(folderInfo.getFolderPath())),
                    folderInfo
            );
        }
    }

    @Override
    protected Integer call() throws Exception {
        if (selectedFolders.isEmpty()) {
            Messages.sprintf("FolderInfoScanner selectedFolders was empty");
            return 0;
        }

        for (Path selectedFolder : selectedFolders) {
            if (isCancelled() || Main.getProcessCancelled()) {
                cancel();
                break;
            }

            if (selectedFolder == null || !Files.isDirectory(selectedFolder)) {
                Messages.sprintfError("FolderInfoScanner selectedFolder was invalid: " + selectedFolder);
                continue;
            }

            scanSelectedFolder(selectedFolder);
        }

        return sortItFoldersToAdd.size()
                + sortedFoldersToAdd.size()
                + asItIsFoldersToAdd.size()
                + changedExistingFolders.size();
    }

    private void scanSelectedFolder(Path selectedFolder) throws IOException {
        Files.walkFileTree(selectedFolder, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
                if (isCancelled() || Main.getProcessCancelled()) {
                    cancel();
                    return FileVisitResult.TERMINATE;
                }

                try {
                    scanDirectory(directory);
                } catch (IOException exception) {
                    Messages.sprintfError("scanSelectedFolder FolderInfoScanner failed to scan directory: "
                            + directory + " error: " + exception.getMessage());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exception) {
                Messages.sprintfError("FolderInfoScanner failed to visit: "
                        + file + " error: " + exception.getMessage());
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }

    private void scanDirectory(Path directory) throws IOException {
        FolderInfo folderInfo = null;
        Path mdirDatabaseFilePath = directory.resolve(Main.conf.getMdir_db_fileName());
        if(Files.exists(mdirDatabaseFilePath)) {
            folderInfo = FolderInfoDao.loadFolderInfo(directory);
        }

        List<FileInfo> currentFileInfos = null;

        if (folderInfo != null && folderInfo.getFileInfoList() != null && !folderInfo.getFileInfoList().isEmpty()) {
            currentFileInfos = folderInfo.getFileInfoList();

            List<Path> rootFiles = GetRootFiles.getRootFiles(directory);
            FileUtils.checkFolderForChanges(folderInfo.getFileInfoList(), rootFiles);

            boolean hasMediaFilesInFolder = ValidatePathUtils.hasMediaFilesInFolder(directory);
            if (!hasMediaFilesInFolder) {

            }


            Messages.sprintf("Found existing folder info for directory: " + directory);
        } else {
            currentFileInfos = createFileInfosFromDirectory(directory);
        }
        if (currentFileInfos.isEmpty()) {
            removeExistingFolderIfDirectoryNoLongerHasMedia(directory);
            return;
        }

        FolderInfo existingFolderInfo = findExistingFolderInfo(directory);

        if (existingFolderInfo != null) {
            checkExistingFolderForChanges(existingFolderInfo, currentFileInfos);
            return;
        }

        createNewFolderInfo(directory, currentFileInfos);
    }

    private List<FileInfo> createFileInfosFromDirectory(Path directory) throws IOException {
        List<FileInfo> fileInfos = new ArrayList<>();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
            for (Path path : paths) {
                if (isCancelled() || Main.getProcessCancelled()) {
                    cancel();
                    break;
                }

                if (!Files.isRegularFile(path)) {
                    continue;
                }

                if (!supportedMediaFormat(path)) {
                    continue;
                }
                long startTime = System.currentTimeMillis();
                FileInfo fileInfo = createFileInfo(path);

                if (fileInfo != null) {
                    Messages.sprintf("FileInfo were created! " + fileInfo.getOrgPath());
                    fileInfos.add(fileInfo);
                    long endTime = System.currentTimeMillis();
                    Messages.sprintf("########createFileInfosFromDirectory created in " + (endTime - startTime) + " ms ################");
                } else {
                    Messages.sprintfError("FolderInfoScanner createFileInfo returned null: " + path);
                }
            }
        }

        return fileInfos;
    }

    private void checkExistingFolderForChanges(FolderInfo existingFolderInfo, List<FileInfo> currentFileInfos) {
        boolean changed = false;

        if (existingFolderInfo.getFileInfoList() == null) {
            existingFolderInfo.setFileInfoList(new ArrayList<>());
            changed = true;
        }

        boolean addedNewFiles = FolderInfoUtils.addFileInfoList(existingFolderInfo, currentFileInfos);

        if (addedNewFiles) {
            changed = true;
        }

        boolean removedMissingFiles = removeFileInfosThatNoLongerExist(existingFolderInfo, currentFileInfos);

        if (removedMissingFiles) {
            changed = true;
        }

        existingFolderInfo.setConnected(Files.exists(Path.of(existingFolderInfo.getFolderPath())));

        /*
         * Always recalculate existing folders that currently have media files.
         * Even if no file was added/removed, metadata/state may have changed elsewhere.
         */
        FolderInfoUtils.calculateFolderInfoStatus(existingFolderInfo);

        if (changed) {
            existingFolderInfo.setChanged(true);

            if (!changedExistingFolders.contains(existingFolderInfo)) {
                changedExistingFolders.add(existingFolderInfo);
            }
        }

        Messages.sprintf("FolderInfoScanner checked existing folder: "
                + existingFolderInfo.getFolderPath()
                + " changed: "
                + changed);
    }

    private boolean removeFileInfosThatNoLongerExist(FolderInfo existingFolderInfo, List<FileInfo> currentFileInfos) {
        Set<String> currentPaths = new HashSet<>();

        for (FileInfo currentFileInfo : currentFileInfos) {
            if (currentFileInfo.getOrgPath() != null) {
                currentPaths.add(normalizePathString(Path.of(currentFileInfo.getOrgPath())));
            }
        }

        return existingFolderInfo.getFileInfoList().removeIf(existingFileInfo -> {
            if (existingFileInfo.getOrgPath() == null) {
                return true;
            }

            Path existingPath = Path.of(existingFileInfo.getOrgPath());

            if (!isSameParentFolder(existingFolderInfo, existingPath)) {
                return false;
            }

            return !currentPaths.contains(normalizePathString(existingPath));
        });
    }

    private boolean isSameParentFolder(FolderInfo folderInfo, Path filePath) {
        Path parent = filePath.getParent();

        if (parent == null || folderInfo.getFolderPath() == null) {
            return false;
        }

        String folderPath = normalizePathString(Path.of(folderInfo.getFolderPath()));
        String fileParentPath = normalizePathString(parent);

        return folderPath.equals(fileParentPath);
    }

    private void removeExistingFolderIfDirectoryNoLongerHasMedia(Path directory) {
        FolderInfo existingFolderInfo = findExistingFolderInfo(directory);

        if (existingFolderInfo == null) {
            return;
        }

        if (existingFolderInfo.getFileInfoList() != null && !existingFolderInfo.getFileInfoList().isEmpty()) {
            existingFolderInfo.getFileInfoList().clear();
            existingFolderInfo.setChanged(true);
            existingFolderInfo.setConnected(Files.exists(directory));
            FolderInfoUtils.calculateFolderInfoStatus(existingFolderInfo);

            if (!changedExistingFolders.contains(existingFolderInfo)) {
                changedExistingFolders.add(existingFolderInfo);
            }

            Messages.sprintf("FolderInfoScanner existing folder no longer has media files: " + directory);
        }
    }

    private void createNewFolderInfo(Path directory, List<FileInfo> currentFileInfos) {
        TableType tableType = TableUtils.resolveTableTypeByPath(directory);

        FolderInfo folderInfo = new FolderInfo(directory);
        folderInfo.setConnected(Files.exists(directory));
        folderInfo.setTableType(tableType.getType());
        folderInfo.setFileInfoList(currentFileInfos);

        FolderInfoUtils.calculateFolderInfoStatus(folderInfo);

        switch (tableType) {
            case SORTED -> sortedFoldersToAdd.add(folderInfo);
            case ASITIS -> asItIsFoldersToAdd.add(folderInfo);
            default -> sortItFoldersToAdd.add(folderInfo);
        }

        Messages.sprintf("FolderInfoScanner created new FolderInfo: "
                + directory
                + " tableType: "
                + tableType);
    }

    private FolderInfo findExistingFolderInfo(Path folderPath) {
        return existingFoldersByPath.get(normalizePathString(folderPath));
    }

    private String normalizePathString(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }

    @Override
    protected void succeeded() {
        super.succeeded();

        /*
         * Task.succeeded() already runs on the JavaFX Application Thread.
         * Do not wrap this in Platform.runLater().
         */
        tables.getSortIt_table().getItems().addAll(sortItFoldersToAdd);
        tables.getSorted_table().getItems().addAll(sortedFoldersToAdd);
        tables.getAsItIs_table().getItems().addAll(asItIsFoldersToAdd);

        TableUtils.refreshTableContent(tables.getSortIt_table());
        TableUtils.refreshTableContent(tables.getSorted_table());
        TableUtils.refreshTableContent(tables.getAsItIs_table());
        TableUtils.calculateTableViewsStatistic(tables);

        if (!sortItFoldersToAdd.isEmpty()
                || !sortedFoldersToAdd.isEmpty()
                || !asItIsFoldersToAdd.isEmpty()
                || !changedExistingFolders.isEmpty()) {
            Main.setChanged(true);
        }

        Messages.sprintf("FolderInfoScanner completed. Added SORTIT: "
                + sortItFoldersToAdd.size()
                + " Added SORTED: "
                + sortedFoldersToAdd.size()
                + " Added ASITIS: "
                + asItIsFoldersToAdd.size()
                + " Changed existing: "
                + changedExistingFolders.size());
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("FolderInfoScanner cancelled");
    }

    @Override
    protected void failed() {
        Throwable exception = getException();

        if (exception != null) {
            Messages.sprintfError("FolderInfoScanner failed: " + exception.getMessage());
        } else {
            Messages.sprintfError("FolderInfoScanner failed");
        }
    }
}
