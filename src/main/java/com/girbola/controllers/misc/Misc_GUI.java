
package com.girbola.controllers.misc;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import javafx.application.Platform;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;


public class Misc_GUI {

    public static void fastExit() {
        sprintf("exitProgram " + getLineNumber());
        ConfigurationSQLHandler.close();
        sprintf("DELETING RUNNING DAT FILE DEMOOOOOOOOOOOOOOOOOOO FIX THIS BEFORE RELEASE" + getLineNumber());
        ConcurrencyUtils.stopAllExecThreadNow();
        Platform.exit();
    }
}
