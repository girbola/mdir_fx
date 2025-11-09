package com.girbola.controllers.folderscanner;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class TableCell_HeaderConnectedIcon<S> extends TableCell<S, Boolean> {
    private Label label = new Label();

    private final FontIcon fontIcon = new FontIcon();

    public TableCell_HeaderConnectedIcon(String iconLiteral, int iconSize) {
        fontIcon.setIconLiteral(iconLiteral);
        fontIcon.setIconSize(iconSize);
        fontIcon.setIconColor(Color.WHITESMOKE);
        label.setGraphic(fontIcon);
    }

    @Override
    protected void updateItem(Boolean connected, boolean empty) {
        super.updateItem(connected, empty);
        setGraphic(label);
    }
}
