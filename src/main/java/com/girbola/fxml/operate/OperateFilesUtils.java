package com.girbola.fxml.operate;

import com.girbola.Main;
import com.girbola.controllers.main.Model_operate;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Window;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.FutureTask;

import static com.girbola.messages.Messages.sprintf;

public class OperateFilesUtils {

    public static boolean createDirectories(Path destinationDirectories) {
        Messages.sprintf("About to create folder if does not exists: " + destinationDirectories.getParent());
        try {
            if (!Files.exists(destinationDirectories.getParent())) {
                Messages.sprintf(
                        "destinationDirectories: " + destinationDirectories.getParent() + " did NOT exists");
                Files.createDirectories(destinationDirectories.getParent());
                return true;
            } else {
                return true;
            }
        } catch (Exception ex) {
            Main.setProcessCancelled(true);
            return false;
        }
    }

    public static boolean copyFile(FileInfo fileInfo, Path source2, Path dest2, String STATE2,
                                   SimpleIntegerProperty answer) {

        if (!Files.exists(source2)) {

            return false;
  }
        boolean destinationDirectoriesCreated = OperateFilesUtils.createDirectories(dest2);
        if (!destinationDirectoriesCreated) {
            Messages.sprintfError("Couldn't not be able to create folder");
            Main.setProcessCancelled(true);
            return false;
        }
        try {
            Path destTmpFile = Paths.get(dest2.toFile() + ".tmp");

            Files.deleteIfExists(destTmpFile);
            Messages.sprintf("Source: " + source2 + " dest: " + dest2);

            InputStream from = new FileInputStream(source2.toFile());
            OutputStream to = new FileOutputStream(destTmpFile.toFile());
            resetAndupdateSourceAndDestProcessValues(source2, dest2);

            long nread = 0L;
            int byteRead = 0;
            byte[] buf = new byte[8192];
            sprintf("----] Starting copying: " + source2);
            while ((byteRead = from.read(buf)) > 0) {
                if (Main.getProcessCancelled()) {
                    boolean cancelledSucceeded = cleanCancelledFile(from, to, source2, dest2);
                    if (cancelledSucceeded) {
                        Messages.sprintf("Cleanup is done ");
                    } else {
                        Messages.sprintf("Cleanup has FAILED. There will be tmp file will remain: " + destTmpFile);
                    }

                    break;
                }
                to.write(buf, 0, byteRead);
                nread += byteRead;
                updateIncreaseLastSecondFileSizeProcessValues(byteRead);
            }
            from.close();
            to.flush();
            to.close();
            resetAndUpdateFileCopiedProcessValues();

            if (nread != source2.toFile().length()) {

                switch (rememberAnswer.get()) {
                    case CopyAnswerType.COPY:
                        answer.set(0);
                        Messages.sprintf("Copy will be done. Value is: " + answer.get());
                        break;
                    case CopyAnswerType.DONTCOPY:
                        answer.set(1);
                        Messages.sprintf("Copy won't be done. Value is: " + answer.get());
                        break;
                    case CopyAnswerType.ASK:
                        Messages.sprintf("Prompting dialogue");
                        FutureTask<SimpleIntegerProperty> task = new FutureTask<>(
                                new OperateFiles.Dialogue((Window) model_operate.getStart_btn().getScene().getWindow(), fileInfo,
                                        model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp(), answer,
                                        rememberAnswer));
                        Platform.runLater(task);
                        answer = task.get();
                        Messages.sprintf("And the answer is: " + answer.get());
                        break;
                }

                if (answer.get() == 0) {
                    renameTmpFileToCorruptedFileExtensions(fileInfo, destTmpFile, dest2);
                    Messages.sprintf("renameTmpFileToCorruptedFile: " + destTmpFile);
                } else if (answer.get() == 1) {
                    Messages.sprintf("Don't keep the file. Tmp file will be deleted: " + destTmpFile);
                    Files.deleteIfExists(destTmpFile);
                } else if (answer.get() == 2) {
                    Messages.sprintf("Cancel pressed. This is not finished!");
                    cancel();
                    Main.setProcessCancelled(true);
                    return false;

                }
            } else {
                renameTmpFileBackToOriginalExtentension(fileInfo, destTmpFile, dest2);
                Messages.sprintf("renameTmpFileBackToOriginalExtentension: " + destTmpFile + " dest: " + dest2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    private static synchronized void resetAndupdateSourceAndDestProcessValues(Path source, Path dest, Model_operate model_operate) {
        Platform.runLater(() -> {
            model_operate.getCopyProcess_values().setCopyFrom_tmp(source.toString());
            model_operate.getCopyProcess_values().setCopyTo_tmp(dest.toString());
            model_operate.getCopyProcess_values().setCopyProgress(0);
            model_operate.getCopyProcess_values().setFilesCopyProgress_MAX_tmp(source.toFile().length());
        });
    }
}
