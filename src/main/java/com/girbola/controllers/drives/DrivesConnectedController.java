package com.girbola.controllers.drives;

import com.girbola.messages.Messages;
import java.awt.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DrivesConnectedController {

    @FXML private Button refresh_button;

    @FXML
    public void refresh_button_action(ActionEvent event) {
        Messages.warningText("Refresh button pressed, but not implemented yet.");
    }
}
