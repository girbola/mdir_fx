package com.girbola.fxml.operate;

import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Model_operate;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.FutureTask;

import static com.girbola.messages.Messages.sprintf;

public class OperateFilesUtils {

    final static private String ERROR = OperateFilesUtils.class.getSimpleName();

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


    public static boolean cleanCancelledFile(InputStream from, OutputStream to, Path source, Path dest) {
        sprintf("cleanCancelledFile cancelled");
        try {
            from.close();
            to.flush();
            to.close();
            if (Files.size(source) != Files.size(dest)) {
                Files.deleteIfExists(Paths.get(dest.toString() + ".tmp"));
                sprintf("2file is gonna be deleted! " + dest.toString());
            }
            return true;
        } catch (Exception ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            Main.setProcessCancelled(true);
            return false;
        }
    }

    public static void renameTmpFileToCorruptedFileExtensions(FileInfo fileInfo, Path destTmp, Path dest, Model_main model_main, List<FileInfo> listCopiedFiles) {
        try {
            Messages.sprintf("Renaming corrupted file to: " + dest);

            String fileName = FileUtils.parseExtension(dest);
            Path destPath = Paths.get(dest.getParent().toString() + File.separator + fileName + "_crp."
                    + FileUtils.getExtension(dest));
            if (Files.exists(destPath)) {
                destPath = FileUtils.renameFile(dest, destPath);
            }
            Files.move(Paths.get(dest.toString() + ".tmp"), destPath);

            String newName = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
            fileInfo.setDestination_Path(newName);
            fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
            fileInfo.setCopied(true);
            listCopiedFiles.add(fileInfo);
            boolean added = model_main.getWorkDir_Handler().add(fileInfo);
            if (added) {
                Messages.sprintf("FileInfo - corrupted added to destination succesfully");
            } else {
                Messages.sprintf("FileInfo - corrupted were not added because it did exists "
                        + fileInfo.getDestination_Path());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.sprintfError(ex.getMessage());
            Main.setProcessCancelled(true);
        }
    }




}
