package com.girbola.controllers.folderscanner;

import com.girbola.controllers.datefixer.utils.GUI_Methods;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javax.swing.text.html.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;

public class TableCell_Media extends TableCell<SelectedFolder, Boolean> {

    private Button mediaButton = new Button();
    FontIcon fontIcon = new FontIcon();

    public TableCell_Media() {
        fontIcon.setIconLiteral("bi-check2");
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.WHITESMOKE);
        mediaButton.setGraphic(fontIcon);
    }

    @Override
    protected void updateItem(Boolean aBoolean, boolean empty) {
        super.updateItem(aBoolean, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(mediaButton);
            setText(null);
            mediaButton.getStyleClass().add("transparent_btn");
            SelectedFolder selectedFolded = (SelectedFolder) getTableView().getItems().get(getIndex());
            if (selectedFolded.isMedia()) {
                mediaButton.setText("");
                fontIcon.setIconLiteral("bi-check");
            } else {
                mediaButton.setText("");
                mediaButton.getStyleClass().add("transparent_btn");
                fontIcon.setIconLiteral("bi-x");
            }
        }
    }
}
