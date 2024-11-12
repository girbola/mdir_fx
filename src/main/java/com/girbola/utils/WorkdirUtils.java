package com.girbola.utils;

import com.girbola.*;
import com.girbola.configuration.*;
import com.girbola.messages.*;
import common.utils.*;
import java.io.*;
import java.nio.file.*;
import javafx.stage.*;

public class WorkdirUtils {

    private boolean isAvailable = false;

    public static boolean isAvailable() {
        Main.conf.getWorkDir();
        return isAvailable();
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public static void browseWorkdir() {
        DirectoryChooser dc = new DirectoryChooser();
        File file = dc.showDialog(Main.getMain_stage());
        if (file != null) {
            if (Files.exists(file.toPath())) {

                Messages.sprintf("folder path is now: " + file.toString());
                Main.conf.setWorkDir(file.toString());

                Main.conf.setWorkDirSerialNumber(OSHI_Utils.getDriveSerialNumber((file.toPath().getRoot()).toString()));
                Configuration_SQL_Utils.update_Configuration();

            } else {
                Messages.warningText("Can't find current path. Check you folder access");
            }

            /*
             * TODO DriveInfo drive = new DriveInfo(aDrivePath, aDriveTotalSize, aConnected,
             * aSelected, aIndentifier); workDir_input.setText(file.toString());
             * conf.setWorkDir(file.toString()); testWritingToWorkdir. if it is possible
             * then continue etc. Myös lisää DriveInfo tässä kohtaan ja tallenna FileSystem
             * info jotta voi sitten testata myöhemmin kokoa. Esim jos laittaa uuden aseman
             * niin se täsmää ja jos ei täsmää niin ilmoitus käyttäjälle? Testaa CD aseman
             * HASHcode ja samoin tikun hashcode. Ehkä niitä voi hyödyntää etsimisessä?
             */

        }
    }
}
