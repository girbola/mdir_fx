package com.girbola.controllers.main.folderinfoscan;

import com.girbola.Main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.girbola.utils.FileInfoUtils.createFileInfo;
import static common.utils.FileUtils.supportedMediaFormat;

public class FolderInfoScanner extends Task<Integer> {

    private final Tables tables;
    private final Path selectedFolder;

    private final List<FolderInfo> sortItFoldersToAdd = new ArrayList<>();
    private final List<FolderInfo> sortedFoldersToAdd = new ArrayList<>();
    private final List<FolderInfo> asItIsFoldersToAdd = new ArrayList<>();
    private final List<FolderInfo> changedExistingFolders = new ArrayList<>();

    public FolderInfoScanner(Tables tables, Path selectedFolder) {
        this.tables = tables;
        this.selectedFolder = selectedFolder;
    }

    @Override
    protected Integer call() throws Exception {
        if (selectedFolder == null || !Files.isDirectory(selectedFolder)) {
            Messages.sprintfError("FolderInfoScanner selectedFolder was invalid: " + selectedFolder);
            return 0;
        }

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
                    Messages.sprintfError("FolderInfoScanner failed to scan directory: "
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

        return sortItFoldersToAdd.size()
                + sortedFoldersToAdd.size()
                + asItIsFoldersToAdd.size()
                + changedExistingFolders.size();
    }

    private void scanDirectory(Path directory) throws IOException {
        List<FileInfo> currentFileInfos = createFileInfosFromDirectory(directory);

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

                FileInfo fileInfo = createFileInfo(path);

                if (fileInfo != null) {
                    fileInfos.add(fileInfo);
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
            case SORTIT -> sortItFoldersToAdd.add(folderInfo);
            default -> sortItFoldersToAdd.add(folderInfo);
        }

        Messages.sprintf("FolderInfoScanner created new FolderInfo: "
                + directory
                + " tableType: "
                + tableType);
    }

    private FolderInfo findExistingFolderInfo(Path folderPath) {
        FolderInfo folderInfo = findExistingFolderInfoFromTable(tables.getSortIt_table(), folderPath);

        if (folderInfo != null) {
            return folderInfo;
        }

        folderInfo = findExistingFolderInfoFromTable(tables.getSorted_table(), folderPath);

        if (folderInfo != null) {
            return folderInfo;
        }

        return findExistingFolderInfoFromTable(tables.getAsItIs_table(), folderPath);
    }

    private FolderInfo findExistingFolderInfoFromTable(TableView<FolderInfo> table, Path folderPath) {
        String normalizedFolderPath = normalizePathString(folderPath);

        for (FolderInfo folderInfo : table.getItems()) {
            if (folderInfo.getFolderPath() == null) {
                continue;
            }

            String existingFolderPath = normalizePathString(Path.of(folderInfo.getFolderPath()));

            if (existingFolderPath.equals(normalizedFolderPath)) {
                return folderInfo;
            }
        }

        return null;
    }

    private String normalizePathString(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }

    @Override
    protected void succeeded() {
        super.succeeded();

        Platform.runLater(() -> {
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
        });
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("FolderInfoScanner cancelled: " + selectedFolder);
    }

    @Override
    protected void failed() {
        Throwable exception = getException();

        if (exception != null) {
            Messages.sprintfError("FolderInfoScanner failed: "
                    + selectedFolder + " error: " + exception.getMessage());
        } else {
            Messages.sprintfError("FolderInfoScanner failed: " + selectedFolder);
        }
    }
}
