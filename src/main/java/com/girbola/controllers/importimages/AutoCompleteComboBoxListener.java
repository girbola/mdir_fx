/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.importimages;

import com.girbola.messages.Messages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Marko Lokka
 */
public class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {

	private ComboBox comboBox;
	private StringBuilder sb;
	private ObservableList<T> data;
	private boolean moveCaretToPos = false;
	private int caretPos;

	public AutoCompleteComboBoxListener(final ComboBox comboBox) {
		this.comboBox = comboBox;
		sb = new StringBuilder();
		data = comboBox.getItems();

		this.comboBox.setEditable(true);
		TextField tf = this.comboBox.getEditor();

		this.comboBox.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent t) {
				Messages.sprintf("keyevent: " + t.getCharacter());
				comboBox.hide();
			}
		});
		this.comboBox.setOnKeyReleased(AutoCompleteComboBoxListener.this);
	}

	@Override
	public void handle(KeyEvent event) {
		Messages.sprintf("Keyevent?: " + event.getCode());
		
		if (event.getCode() == KeyCode.UP) {
			caretPos = -1;
			moveCaret(comboBox.getEditor().getText().length());
			return;
		} else if (event.getCode() == KeyCode.DOWN) {
			if (!comboBox.isShowing()) {
				comboBox.show();
			}
			caretPos = -1;
			moveCaret(comboBox.getEditor().getText().length());
			return;
		} else if (event.getCode() == KeyCode.BACK_SPACE) {
			moveCaretToPos = true;
			caretPos = comboBox.getEditor().getCaretPosition();
		} else if (event.getCode() == KeyCode.DELETE) {
			moveCaretToPos = true;
			caretPos = comboBox.getEditor().getCaretPosition();
		}

		if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT || event.isControlDown() || event.getCode() == KeyCode.SHIFT
				|| event.isShiftDown() || event.getCode() == KeyCode.HOME || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
			return;
		}
		ObservableList list = FXCollections.observableArrayList();
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).toString().toLowerCase().startsWith(AutoCompleteComboBoxListener.this.comboBox.getEditor().getText().toLowerCase())) {
				list.add(data.get(i));
			}
		}
		String t = comboBox.getEditor().getText();

		comboBox.setItems(list);
		comboBox.getEditor().setText(t);
		if (!moveCaretToPos) {
			caretPos = -1;
		}
		moveCaret(t.length());
		if (!list.isEmpty()) {
			comboBox.show();
		}
	}

	private void moveCaret(int textLength) {
		if (caretPos == -1) {
			comboBox.getEditor().positionCaret(textLength);
		} else {
			comboBox.getEditor().positionCaret(caretPos);
		}
		moveCaretToPos = false;
	}

}
