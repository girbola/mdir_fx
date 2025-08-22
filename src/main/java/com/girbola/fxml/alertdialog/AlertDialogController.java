package com.girbola.fxml.alertdialog;

import com.girbola.messages.Messages;
import com.girbola.messages.AlertDialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class AlertDialogController {

    private boolean exit;

    //@formatter:off
    @FXML private AnchorPane anchorPane;
    @FXML private Button alertOkButton;
    @FXML private Label headerText;
    @FXML private TextArea textContent;
    //@formatter:on

    @FXML
    private void alertOkButtonAction(ActionEvent event) {
        Messages.sprintf("Should be closed");

        Stage window = (Stage) alertOkButton.getScene().getWindow();
        window.hide();

        AlertDialog.handleExitAlert(window, exit);

//        Main.sceneManager.getWindow().getOnCloseRequest()
//                .handle(new WindowEvent(Main.sceneManager.getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));

    }

    public void initialize(String headerText, String contentText, boolean exit) {
        this.exit = exit;

        Platform.runLater(() -> {
            this.headerText.setText(headerText);
            this.textContent.setText(contentText);
        });
    }

}
