package com.girbola.messages;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.misc.Misc_GUI;
import com.girbola.fxml.alertdialog.AlertDialogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import static com.girbola.Main.bundle;

public class AlertDialog {

    final private static String ERROR = AlertDialog.class.getName();
    private org.slf4j.Logger log = LoggerFactory.getLogger(AlertDialog.class);

    public static void alertDialog(String headerText, String contentText, Stage window, boolean exit) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/alertdialog/alert-dialog.fxml"), bundle);
            Parent parent = loader.load();

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.initOwner(window.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setAlwaysOnTop(true);
            scene.getStylesheets().add(Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());

            AlertDialogController alertDialogController = (AlertDialogController) loader.getController();
            alertDialogController.initialize(headerText, contentText, exit);

            stage.setScene(scene);

            stage.setOnCloseRequest(windowEvent -> {
                handleExitAlert(stage, exit);
            });

            Platform.runLater(stage::showAndWait);

        } catch (Exception ex) {
            ex.printStackTrace();
            //Messages.errorSmth(ERROR, "Problem with loading FXML", ex, Misc.getLineNumber(), true);
        }
    }

    public static void handleExitAlert(Stage stage, boolean exit) {
        if (exit) {
            Misc_GUI.fastExit();
        } else {
            stage.hide();
        }
    }
}
