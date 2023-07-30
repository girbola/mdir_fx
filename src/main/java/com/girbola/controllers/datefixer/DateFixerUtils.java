package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.controllers.main.Model_main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.List;

public class DateFixerUtils {
    public static boolean checkDriveConnectivity(List<FileInfo> fileInfo_list, FileInfo fileInfo, Path dest) {
        if (dest != null && Main.conf.getDrive_connected()) {
            Messages.sprintf("copyToMisc_btn_action dest is: " + dest);
            fileInfo.setWorkDir(Main.conf.getWorkDir());
            fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
            fileInfo.setDestination_Path(dest.toString());
            fileInfo.setCopied(false);
            fileInfo_list.add(fileInfo);
        } else {
            Messages.sprintf("Dest were null. process is about to be cancelled");
            return true;
        }
        return false;
    }

    public static void operateFiles(Model_main model_main, List<FileInfo> fileInfo_list) {
        Task<Boolean> operateFiles = new OperateFiles(fileInfo_list, true, model_main,
                SceneNameType.DATEFIXER.getType());
        operateFiles.setOnSucceeded((workerStateEvent) -> {
//			operateFiles.get
            Messages.sprintf("operateFiles Succeeded");
        });
        operateFiles.setOnCancelled((workerStateEvent) -> {
            Messages.sprintf("operateFiles CANCELLED");
        });
        operateFiles.setOnFailed((workerStateEvent) -> {
            Messages.sprintf("operateFiles FAILED");
            Main.setProcessCancelled(true);
        });
        Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
        operateFiles_th.setDaemon(true);
        operateFiles_th.start();
    }


}
