package com.girbola.controllers.main;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.events.GUI_Events;
import com.girbola.messages.Messages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class EditingCell extends TableCell<FolderInfo, String> {
	private TextField textField;
	private Model_main model_Main;

	public EditingCell(Model_main aModel_Main) {
		this.model_Main = aModel_Main;
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
					textField.setText(getString());
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
		textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
//					setFocused(false);
					textField.requestFocus();
					Messages.sprintf("Enter pressed: " + e.getCode());
				} else {
					Messages.sprintf("Key pressed: " + e.getCode());
				}
			}
		});
		Messages.sprintf("createTextField: " + textField.getParent());
		textField.getStyleClass().add("text-field");

		GUI_Events.textField_file_listener(textField);
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (!arg2) {
					commitEdit(textField.getText());
				}
			}
		});
	}

	private String getString() {
		return getItem() == null ? "" : getItem().toString();
	}

}
