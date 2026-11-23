package com.girbola.controllers.operate;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.ModelOperate;
import com.girbola.fileinfo.CopyLifecycleState;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.stage.Window;

import static com.girbola.controllers.operate.OperateFilesUtils.renameTmpFileToCorruptedFileExtensions;
import static com.girbola.messages.Messages.sprintf;

public class CopyHelper {


    private ModelMain modelMain;
    private ModelOperate modelOperate;
    private Task<?> copyTask;

    private AtomicInteger counter = new AtomicInteger(0);

    private static List<FileInfo> listCopiedFiles = new ArrayList<>();

    public static final int BUFFER_SIZE = 8192;

    private SimpleStringProperty rememberAnswer = new SimpleStringProperty(CopyAnswerType.ASK);

    public CopyHelper(ModelMain modelMain, ModelOperate modelOperate, Task<?> copyTask) {
        this.modelMain = modelMain;
        this.modelOperate = modelOperate;
        this.copyTask = copyTask;
    }


    public boolean renameTmpFileBackToOriginalExtentension(FileInfo fileInfo, Path destTmp, Path dest, ModelMain model_main) {
        try {
            Messages.sprintf(
                    "Renaming file .tmp back to org extension: " + destTmp + " to dest: " + dest);

            Files.move(destTmp, dest);
            String newName = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
            fileInfo.setDestination_Path(newName);
            fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
            fileInfo.setCopied(true);
            listCopiedFiles.add(fileInfo);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.sprintfError(ex.getMessage());
            return false;
        }

    }


    public void handleWorkDirCheck(Path workDir) {
        if (!Files.exists(workDir)) {
            Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
            handleCancellation(true, "Workdir does not exist");
        } else {
            Messages.sprintf("Workdir exists at " + workDir);
        }
    }


    public void handleListCheck(List<FileInfo> list) {
        if (list.isEmpty()) {
            Messages.warningText("List is empty!");
            handleCancellation(true, "List empty");
        } else {
            modelOperate.getTimeline().play();
        }
    }

    public boolean checkProcessCancellation() {
        if (copyTask.isCancelled() || Main.getProcessCancelled()) {
            Messages.sprintf("Copy process is cancelled");
            handleCancellation(copyTask.isCancelled(), "Process cancelled");
            return true;
        }
        return false;
    }

    public void handleCancellation(boolean condition, String message) {
        if (condition) {
            Messages.sprintf(message);
            Main.setProcessCancelled(true);
            modelOperate.stopTimeLine();
            copyTask.cancel();
        }
    }

    public void updateFilesLeftCounter() {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().setFilesLeft(counter.decrementAndGet());
        });
    }

    /**
     * Check if there are possibilities for a conflict existence with copy process
     *
     * @param fileInfo
     * @param destination
     * @return
     */
    public boolean checkCancellation(FileInfo fileInfo, Path destination) {
        if (copyTask.isCancelled()) {
            Main.setProcessCancelled(true);
            copyTask.cancel();
            return true;
        }
        if (Main.getProcessCancelled()) {
            Main.setProcessCancelled(true);
            copyTask.cancel();
            return true;
        }
        if (fileInfo.getDestination_Path() == null) {
            Messages.warningText("getDestination_Path were null: " + fileInfo.getOrgPath());
            Main.setProcessCancelled(true);
            copyTask.cancel();
            return true;
        }

        if (fileInfo.getDestination_Path().isEmpty()) {
            Messages.warningText("getDestination_Path were empty: " + fileInfo.getOrgPath());
            Main.setProcessCancelled(true);
            copyTask.cancel();
            return true;
        }
        if (Paths.get(fileInfo.getOrgPath()).getParent().toString().contains(Main.conf.getWorkDir())) {
            Messages.warningText(Main.bundle.getString("conflictWithWorkDir"));
            Main.setProcessCancelled(true);
            copyTask.cancel();
            return true;
        }
        if (Objects.equals(fileInfo.getOrgPath(), destination.toString())) {
            Main.setProcessCancelled(true);
            copyTask.cancel();
            return true;
        }
        return false;
    }

    private synchronized void resetAndUpdateFileCopiedProcessValues() {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().setLastSecondFileSize_tmp(0);
            modelOperate.getCopyProcess_values().decreaseFilesLeft_tmp();
        });
    }

    private synchronized void updateIncreaseLastSecondFileSizeProcessValues(int byteRead) {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().increaseLastSecondFileSize_tmp(byteRead);
        });
    }

    public synchronized void updateIncreaseRenamedProcessValues() {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().increaseRenamed_tmp();
        });
    }

    public synchronized void updateIncreaseCopyingProcessValues() {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().increaseCopied_tmp();
        });
    }

    public synchronized void updateIncreaseDuplicatesProcessValues() {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().increaseDuplicated_tmp();
            Messages.sprintf("Increader dups is now: " + modelOperate.getCopyProcess_values().getDuplicated_tmp());
        });
    }

    public synchronized void updateSourceAndDestProcessValues(Path source, Path dest) {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().setCopyFrom(source.toString());
            modelOperate.getCopyProcess_values().setCopyTo(dest.toString());
        });
    }

    public Path resolveDestinationPath(Path sourcePath, Path requestedDestPath) throws IOException {
        Path candidate = requestedDestPath;
        int suffix = 1;

        while (Files.exists(candidate)) {
            if (Files.isRegularFile(candidate) && Files.mismatch(sourcePath, candidate) == -1) {
                return null;
            }
            candidate = addSuffix(requestedDestPath, suffix++);
        }

        return candidate;
    }

    private Path addSuffix(Path path, int suffix) {
        String fileName = path.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex <= 0) {
            extensionIndex = fileName.length();
        }

        String name = fileName.substring(0, extensionIndex);
        String extension = fileName.substring(extensionIndex);
        return path.resolveSibling(name + "_" + suffix + extension);
    }

    private synchronized void resetAndupdateSourceAndDestProcessValues(Path source, Path dest) {
        Platform.runLater(() -> {
            modelOperate.getCopyProcess_values().setCopyFrom_tmp(source.toString());
            modelOperate.getCopyProcess_values().setCopyTo_tmp(dest.toString());
            modelOperate.getCopyProcess_values().setCopyProgress(0);
            modelOperate.getCopyProcess_values().setFilesCopyProgress_MAX_tmp(source.toFile().length());
        });
    }

    public boolean copyFile(FileInfo fileInfo, Path sourcePath, Path destPath, String state,
                            SimpleIntegerProperty answer, ModelOperate modelOperate) throws IOException, ExecutionException, InterruptedException {

        fileInfo.setCopyLifecycleState(CopyLifecycleState.NEW);
        fileInfo.setCopied(false);

        if (!Files.isRegularFile(sourcePath)) {
            Messages.sprintfError("Source file does not exist or is not a regular file: " + sourcePath);
            return false;
        }

        Path destParent = destPath.getParent();
        if (destParent == null) {
            Messages.sprintfError("Destination must have a parent directory: " + destPath);
            return false;
        }

        if (!OperateFilesUtils.createDirectories(destPath)) {
            Messages.sprintfError("Couldn't not be able to create folder");
            Main.setProcessCancelled(true);
            return false;
        }

        fileInfo.setCopyLifecycleState(CopyLifecycleState.PROCESS);
        Path destTmpPath = null;

        try {
            long expectedSize = Files.size(sourcePath);
            destTmpPath = Files.createTempFile(destParent, "mdir-copy-", ".tmp");
            try (InputStream from = Files.newInputStream(sourcePath);
                 OutputStream to = Files.newOutputStream(destTmpPath, StandardOpenOption.WRITE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {
                Messages.sprintf("Source: " + sourcePath + " dest: " + destPath);
                resetAndupdateSourceAndDestProcessValues(sourcePath, destPath);
                copyData(from, to, sourcePath);
            }

            if (!Files.isRegularFile(sourcePath) || Files.size(sourcePath) != expectedSize) {
                throw new IOException("Source file changed or disappeared during copy: " + sourcePath);
            }
            if (!Files.isRegularFile(destTmpPath) || Files.size(destTmpPath) != expectedSize) {
                throw new IOException("Incomplete copy to temporary destination: " + destTmpPath
                        + " (expected " + expectedSize + " bytes)");
            }
            if (Files.mismatch(sourcePath, destTmpPath) != -1) {
                throw new IOException("Copied data differs from source: " + sourcePath);
            }
            if (!finalizeCopy(destTmpPath, destPath, fileInfo, answer)) {
                throw new IOException("Could not move temporary copy to destination: " + destPath);
            }
            if (!Files.isRegularFile(destPath) || Files.size(destPath) != expectedSize) {
                Files.deleteIfExists(destPath);
                throw new IOException("Destination copy failed size verification: " + destPath);
            }

            fileInfo.setCopied(true);
            fileInfo.setCopyLifecycleState(CopyLifecycleState.DONE);
            resetAndUpdateFileCopiedProcessValues();
            return true;
        } catch (Exception e) {
            if (destTmpPath != null) {
                try {
                    Files.deleteIfExists(destTmpPath);
                } catch (IOException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
            }
            fileInfo.setCopied(false);
            fileInfo.setCopyLifecycleState(CopyLifecycleState.NEW);
            Messages.sprintfError("Copy failed for " + sourcePath + " to " + destPath + ": " + e.getMessage());
            return false;
        }
    }

    private void copyData(InputStream from, OutputStream to, Path sourcePath) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        sprintf("----] Starting copying: " + sourcePath);
        while ((bytesRead = from.read(buffer)) > 0) {
            if (Main.getProcessCancelled()) {
                throw new IOException("Copy was cancelled: " + sourcePath);
            }
            to.write(buffer, 0, bytesRead);
            updateIncreaseLastSecondFileSizeProcessValues(bytesRead);
        }

        to.flush();
    }

    /**
     * Finalizes the file copy process by handling potential conflicts and renaming
     * the temporary file back to its original extension if no conflicts are detected.
     *
     * @param destTmpPath  the path of the temporary destination file
     * @param destPath     the final destination path of the file
     * @param fileInfo     metadata about the file being copied
     * @param answer       a property that holds the user's decision in case of a conflict
     * @throws IOException              if an I/O error occurs during file operations
     * @throws ExecutionException       if an error occurs during concurrent execution
     * @throws InterruptedException     if the thread is interrupted during execution
     */
    private boolean finalizeCopy(Path destTmpPath, Path destPath, FileInfo fileInfo, SimpleIntegerProperty answer) throws IOException, ExecutionException, InterruptedException {
        boolean finalized = renameTmpFileBackToOriginalExtentension(fileInfo, destTmpPath, destPath, modelMain);
        if (finalized) {
            Messages.sprintf("File renamed to original extension: " + destTmpPath + " dest: " + destPath);
        }
        return finalized;
    }

    private void handleCopyConflict(SimpleIntegerProperty answer, Path destTmpPath, Path destPath, FileInfo fileInfo) throws IOException, ExecutionException, InterruptedException {
        switch (rememberAnswer.get()) {
            case CopyAnswerType.COPY:
                answer.set(0);
                Messages.sprintf("Copy will be done. User answer is: " + answer.get());
                break;
            case CopyAnswerType.DONTCOPY:
                answer.set(1);
                Messages.sprintf("Copy won't be done. User answer is: " + answer.get());
                break;
            case CopyAnswerType.ASK:
                promptUserForDecision(fileInfo, answer);
                break;
        }

        executeUserDecision(answer.get(), destTmpPath, destPath, fileInfo);
    }

    private void promptUserForDecision(FileInfo fileInfo, SimpleIntegerProperty answer) throws ExecutionException, InterruptedException {
        Messages.sprintf("Prompting dialogue");
        FutureTask<SimpleIntegerProperty> task = new FutureTask<>(
                new Dialogue((Window) modelOperate.getStart_btn().getScene().getWindow(), fileInfo,
                        modelOperate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp(), answer, rememberAnswer));
        Platform.runLater(task);
        answer = task.get();
        Messages.sprintf("And the answer is: " + answer.get());
    }

    private void executeUserDecision(int decision, Path destTmpPath, Path destPath, FileInfo fileInfo) throws IOException {
        switch (decision) {
            case 0:
                renameTmpFileToCorruptedFileExtensions(fileInfo, destTmpPath, destPath, modelMain, listCopiedFiles);
                Messages.sprintf("File renamed to corrupted extension: " + destTmpPath);
                break;
            case 1:
                Files.deleteIfExists(destTmpPath);
                Messages.sprintf("Tmp file deleted: " + destTmpPath);
                break;
            case 2:
                Messages.sprintf("Cancel pressed. This is not finished!");
                copyTask.cancel();
                Main.setProcessCancelled(true);
                break;
            default:
                Messages.sprintfError("Unknown decision made. Cancelling process");
                copyTask.cancel();
                Main.setProcessCancelled(true);
                break;
        }
    }
}
