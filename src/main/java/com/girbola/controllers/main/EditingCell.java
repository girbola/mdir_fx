package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.events.GUI_Events;
import com.girbola.messages.Messages;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EditingCell extends TableCell<FolderInfo, String> {
	private TextField textField;
	private ModelMain model_Main;
	private TableColumn<FolderInfo, String> tableColumn;
	private FolderInfo folderInfo;

	public EditingCell(ModelMain aModel_Main, TableColumn<FolderInfo, String> aTableColumn) {
		this.model_Main = aModel_Main;
		this.tableColumn = aTableColumn;
	}

	@Override
	public void startEdit() {
		if (!isEmpty()) {
			super.startEdit();
			createTextField();
			setText(null);
			setGraphic(textField);
			textField.selectAll();
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();

		setText((String) getItem());
		setGraphic(null);
	}

	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					FolderInfo folderInfo = tableColumn.getTableView().getSelectionModel().getSelectedItem();
					if (!textField.getText().isBlank()) {
						if (folderInfo.getFolderPath().equals(textField.getText())) {
							Path checkFolderPath = Paths.get(folderInfo.getFolderPath());
							if (Files.exists(checkFolderPath)) {
								textField.getStyleClass().add("tableTextField_bad");
							} else {
								textField.getStyleClass().add("tableTextField");
							}
						}
					} else {
						textField.getStyleClass().add("tableTextField_bad");
					}
//					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(!Text_Utils.isValidFileOrFolderName(newValue)) {
					textField.setText(oldValue);
				}
			}
		});
		textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
//					setFocused(false);
					textField.getParent().requestFocus();
					Messages.sprintf("Enter pressed: " + e.getCode());
				}
			}
		});
		Messages.sprintf("createTextField: " + textField.getParent());
		textField.getStyleClass().add("tableTextField");

		GUI_Events.textField_file_listener(textField);
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> value, Boolean old, Boolean newValue) {
				if (!newValue) {
					commitEdit(textField.getText());
				}
			}
		});
	}

	private String getString() {
		if (getItem() != null) {

		} else {
			return "";
		}
		return getItem() == null ? "" : getItem().toString();
	}

}
