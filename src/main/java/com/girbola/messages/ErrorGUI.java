package com.girbola.messages;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.*;

import static com.girbola.Main.conf;
import static com.girbola.controllers.misc.Misc_GUI.fastExit;

public class ErrorGUI {

    private static ObservableMap<String, List<String>> errorsMap = FXCollections.observableHashMap();
    //  private static DialogPane dialogPane;
    private static Alert alert = null;

    public static void errorGUI(String message, boolean exit) {

        if (Main.getProcessCancelled()) {
            Messages.sprintfError("errorGUI process were cancelled");
            return;
        }
        if (alert == null) {
            alert = new Alert(Alert.AlertType.ERROR);
//            dialogPane = alert.getDialogPane();
//            dialogPane.setHeaderText("");
//            dialogPane.setContentText(message);
            alert.getDialogPane().getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGSSTYLE.getType()).toExternalForm());

            alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
                @Override
                public void handle(DialogEvent event) {
                    alert.hide();
                }
            });

//            alert.getButtonTypes().clear();
//            alert.getButtonTypes().add(ButtonType.OK);
//            alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
            //        dialogPane.getStyleClass().add("alertDiag");
        }

        ErrorMessage errorMessage = createErrorMessage(message);

        if (!alert.isShowing()) {
            Messages.sprintf("ErrorMessage is showing and group is: " + errorMessage.getGroup());
            Platform.runLater(() -> {
                alert.getDialogPane().setHeaderText("Group of: " + errorMessage.getGroup());
                alert.getDialogPane().setContentText(message);
            });

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (exit) {
                    Main.setProcessCancelled(true);
                    fastExit();
                    alert.hide();
                } else {
                    alert.hide();
                }
            }
        }
    }

    private static ErrorMessage createErrorMessage(String message) {
        String group = "";

        if (message.length() >= 15) {
            group = message.substring(0, 15);
            if (!errorMessageGroupExists(group)) {
                Messages.sprintf("errormessage group did exixts");
                return new ErrorMessage(group, message);
            }
        }
        return null;
    }

    private static boolean errorMessageGroupExists(String group) {
        for (Map.Entry<String, List<String>> entry : errorsMap.entrySet()) {
            String groupEntry = entry.getKey();
            List<String> messageEntry = entry.getValue();
            if (groupEntry.equals(group) && !groupEntry.isEmpty() || !group.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
