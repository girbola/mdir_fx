package com.girbola.controllers.main;

import com.girbola.app.ApplicationContext;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.dialogs.Dialogs;
import com.girbola.messages.Messages;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

final class ModelMainExitHandler {

    private final ApplicationContext applicationContext;
    private final ScheduledService<Void> monitorExternalDriveConnectivity;
    private final BooleanSupplier saveAllTableContents;

    ModelMainExitHandler(ApplicationContext applicationContext,
                         ScheduledService<Void> monitorExternalDriveConnectivity,
                         BooleanSupplier saveAllTableContents) {
        this.applicationContext = applicationContext;
        this.monitorExternalDriveConnectivity = monitorExternalDriveConnectivity;
        this.saveAllTableContents = saveAllTableContents;
    }

    void exitWithoutSaving() {
        ConcurrencyUtils.stopExecThreadNow();
        Platform.exit();
    }

    void exit() {
        Messages.sprintf("exitProgram()");
        Messages.sprintf("Exiting program");
        ConfigurationSQLHandler.updateConfiguration();

        if (applicationContext.isChanged() && !confirmSaveOrExit()) {
            return;
        }

        boolean confirmOnExit = applicationContext.getConfiguration().isConfirmOnExit();
        Messages.sprintf(confirmOnExit ? "isShowOnExit was true" : "isShowOnExit was false");
        ConcurrencyUtils.stopExecThreadNow();
        monitorExternalDriveConnectivity.cancel();
        Platform.exit();
    }

    private boolean confirmSaveOrExit() {
        Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(
                applicationContext.getSceneManager().getWindow(),
                applicationContext.getBundle().getString("saveBeforeExit"));
        Messages.sprintf("dialog changesDialog width: " + dialog.getWidth());
        Iterator<ButtonType> iterator = dialog.getDialogPane().getButtonTypes().iterator();
        while (iterator.hasNext()) {
            ButtonType button = iterator.next();
            Messages.sprintf("BTNNTTNNT: " + button.getText());
        }

        ExitDecision decision = resolveDecision(dialog.showAndWait());
        if (decision == ExitDecision.SAVE) {
            saveAllTableContents.getAsBoolean();
            monitorExternalDriveConnectivity.cancel();
        } else if (decision == ExitDecision.EXIT_WITHOUT_SAVE) {
            Messages.sprintf("No answered. This is not finished!");
        } else {
            Messages.sprintf("Cancel pressed. This is not finished!");
            return false;
        }
        return true;
    }

    static ExitDecision resolveDecision(Optional<ButtonType> result) {
        if (result.isEmpty()) {
            return ExitDecision.CANCEL;
        }
        ButtonBar.ButtonData buttonData = result.get().getButtonData();
        if (buttonData == ButtonBar.ButtonData.YES) {
            return ExitDecision.SAVE;
        }
        if (buttonData == ButtonBar.ButtonData.CANCEL_CLOSE) {
            return ExitDecision.CANCEL;
        }
        return ExitDecision.EXIT_WITHOUT_SAVE;
    }

    enum ExitDecision {
        SAVE,
        EXIT_WITHOUT_SAVE,
        CANCEL
    }
}
