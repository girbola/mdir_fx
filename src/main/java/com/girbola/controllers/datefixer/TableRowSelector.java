package com.girbola.controllers.datefixer;

import com.girbola.controllers.datefixer.table.EXIF_Data_Selector;
import com.girbola.controllers.datefixer.utils.GUI_Methods;
import com.girbola.messages.Messages;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class TableRowSelector implements Callback<TableView<EXIF_Data_Selector>, TableRow<EXIF_Data_Selector>> {

	private final String ERROR = TableRowSelector.class.getName();
	private TableView<EXIF_Data_Selector> table;
	private ScrollPane scrollPane;

	public TableRowSelector(TableView<EXIF_Data_Selector> table, ScrollPane scrollPanne) {
		this.table = table;
		this.scrollPane = scrollPanne;
	}

	@Override
	public TableRow<EXIF_Data_Selector> call(TableView<EXIF_Data_Selector> arg0) {
		TableRow<EXIF_Data_Selector> row = new TableRow<EXIF_Data_Selector>();
		row.setOnMouseClicked((MouseEvent e) -> {
			Messages.sprintf("Mouse clicked: " + e.getTarget() + "\nsource: " + e.getSource());
			if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
				// Messages.sprintf("Button double pressed: " +
				// table.getSelectionModel().getSelectedItem().getInfo());
				String value = "";
				GridPane gridPane = (GridPane) scrollPane.getContent();
				for (Node node : gridPane.getChildren()) {
					if (table.getId().equals("dates_tableView")) {
						value = GUI_Methods.getDate(node);
						if (table.getSelectionModel().getSelectedItem().getInfo().contains(value)) {
							VBox vbox = (VBox) node;
							double vvalue = GridPane.getRowIndex(vbox) * vbox.getHeight()
									+ (gridPane.getVgap() * (GridPane.getRowIndex(vbox) - 2));
							Messages.sprintf("RowConstraits size: " + gridPane.getRowConstraints().get(0));
							scrollPane.setVvalue(vvalue);
							Messages.sprintf("dates_tableView vvalue: " + vvalue);
							value = "";
							break;
						}
					} else if (table.getId().equals("cameras_tableView")) {
						value = GUI_Methods.getCameraModel(node);
						if (table.getSelectionModel().getSelectedItem().getInfo().equals(value)
								&& table.getSelectionModel().getSelectedItem().getInfo() != null) {
							VBox vbox = (VBox) node;
							double vvalue = GridPane.getRowIndex(vbox) * vbox.getHeight()
									+ (gridPane.getVgap() * (GridPane.getRowIndex(vbox) - 2));
							Messages.sprintf("RowConstraits size: " + gridPane.getRowConstraints().get(0));
							scrollPane.setVvalue(vvalue);
							value = "";
							break;
						}
					} else if (table.getId().equals("events_tableView")) {
						value = GUI_Methods.getEvents(node);
						if (table.getSelectionModel().getSelectedItem().getInfo().equals(value)
								&& table.getSelectionModel().getSelectedItem().getInfo() != null) {
							VBox vbox = (VBox) node;
							double vvalue = GridPane.getRowIndex(vbox) * vbox.getHeight()
									+ (gridPane.getVgap() * (GridPane.getRowIndex(vbox) - 2));
							Messages.sprintf("RowConstraits size: " + gridPane.getRowConstraints().get(0));
							scrollPane.setVvalue(vvalue);
							value = "";
							break;
						}
					} else if (table.getId().equals("locations_tableView")) {
						value = GUI_Methods.getEvents(node);
						if (table.getSelectionModel().getSelectedItem().getInfo().equals(value)
								&& table.getSelectionModel().getSelectedItem().getInfo() != null) {
							VBox vbox = (VBox) node;
							double vvalue = GridPane.getRowIndex(vbox) * vbox.getHeight()
									+ (gridPane.getVgap() * (GridPane.getRowIndex(vbox) - 2));
							Messages.sprintf("RowConstraits size: " + gridPane.getRowConstraints().get(0));
							scrollPane.setVvalue(vvalue);
							value = "";
							break;
						}
					} else {
						Messages.sprintfError("This should not come using TableRow<EXIF_Data_Selector>: in class: "
								+ ERROR + " tableId is: " + table.getId());
					}
				}
			} else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1 && row.isEmpty()) {
				Messages.sprintf("Button were instanceof Text");
				table.getSelectionModel().clearSelection();
				e.consume();
			}
		});
		return row;

	}

}
