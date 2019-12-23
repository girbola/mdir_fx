package com.girbola.events;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GUI_Events {

	private static final String ERROR = GUI_Events.class.getSimpleName();
	private static Tooltip tooltip_characterNotValid = null;
	private static Tooltip tooltip_tooLongFileName = null;

	public static void textField_file_listener(TextField textField) {
		if (tooltip_characterNotValid == null) {
			tooltip_characterNotValid = showTooltip(textField, Main.bundle.getString("characterNotValid"));
		}
		if (tooltip_tooLongFileName == null) {
			tooltip_tooLongFileName = showTooltip(textField, Main.bundle.getString("maxLenght"));
		}
		textField.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (tooltip_characterNotValid.isShowing()) {
					tooltip_characterNotValid.hide();
				}
				if (tooltip_tooLongFileName.isShowing()) {
					tooltip_tooLongFileName.hide();
				}
			}
		});
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.contains("\\") || newValue.contains("/") || newValue.contains(":") || newValue.contains("*") || newValue.contains("?")
						|| newValue.contains("<") || newValue.contains(">") || newValue.contains("|")) {
					if (!tooltip_characterNotValid.isShowing()) {
						show(tooltip_characterNotValid, textField);
					}
					textField.setText(oldValue);
				}
				if (newValue.length() > 160) {
					if (!tooltip_tooLongFileName.isShowing()) {
						show(tooltip_tooLongFileName, textField);
					}
					textField.setText(oldValue);
				}
			}
		});
	}

	private static Tooltip showTooltip(TextField textField, String string) {
		Tooltip toolTip = new Tooltip(string);
		if (textField == null) {
			Messages.errorSmth(ERROR, "TextField were null!!!", null, Misc.getLineNumber(), true);
		}
		
		toolTip.setPrefSize(textField.getWidth() + 400, 80);
		toolTip.setMaxSize(textField.getWidth() + 400, 80);
		toolTip.setMinSize(textField.getWidth() + 400, 80);

		return toolTip;
	}

	private static void show(Tooltip toolTip, TextField textField) {
		Point2D p = textField.localToScene(0.0, 0.0);
		double stage_x = p.getX() + textField.getScene().getX() + textField.getScene().getWindow().getX();
		double stage_y = p.getY() + textField.getScene().getY() + textField.getScene().getWindow().getY() + textField.getHeight();
		if (tooltip_characterNotValid.isShowing() || tooltip_tooLongFileName.isShowing()) {
			stage_y += (toolTip.getPrefHeight() + 2);
		}
		textField.setText(textField.getText().substring(0, textField.getText().length() - 1));
		Stage stage = (Stage) textField.getScene().getWindow();
		toolTip.show(stage, stage_x, stage_y);

	}
	public Button createButtonFolder(FileInfo fileInfo) {
		Button button = new Button();
		Path path = Paths.get(fileInfo.getOrgPath());
		
		return button;
	}
}