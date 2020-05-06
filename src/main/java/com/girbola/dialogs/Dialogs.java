package com.girbola.dialogs;

import static com.girbola.Main.conf;

import com.girbola.Main;
import com.girbola.messages.Messages;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

public class Dialogs {
	/*
	 * Return Dialog<ButtonType> Yes, No and Cancel ButtonTypes
	 */
	public static Dialog<ButtonType> createDialog_YesNoCancel(Window owner, final String contentText) {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.widthProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Messages.sprintf("Widthhhh: " + newValue);

			}
		});
		dialog.heightProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Messages.sprintf("heighttttt: " + newValue);

			}
		});
		dialog.setHeight(150);
		dialog.setWidth(400);
//		double main_width = Main.conf.getWindowStartWidth();
//		double main_height = Main.conf.getWindowStartHeight();
//		double x = Main.conf.getWindowStartPosX();
//		double y = Main.conf.getWindowStartPosY();
		dialog.initOwner(owner);
		
		Messages.sprintf("createDialog_YesNoCancel width: " + dialog.getWidth());
		DialogPane dialogPane = new DialogPane();
		dialogPane.getStylesheets()
				.add(Main.class.getResource(conf.getThemePath() + "dialogsStyle.css").toExternalForm());
		dialog.setDialogPane(dialogPane);
		// dialogPane.getStyleClass().add("dialogPane");
		dialogPane.setContentText(contentText);

		ButtonType yes = new ButtonType(Main.bundle.getString("yes"), ButtonBar.ButtonData.YES);
		ButtonType no = new ButtonType(Main.bundle.getString("no"), ButtonBar.ButtonData.NO);
		ButtonType cancel = new ButtonType(Main.bundle.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(yes, no, cancel);

		return dialog;

	}

	public static Dialog<ButtonType> createDialog_YesNo(final String contentText) {
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogPane dialogPane = new DialogPane();
		Messages.sprintf("width: " + dialog.getWidth());
		dialogPane.getStylesheets()
				.add(Main.class.getResource(conf.getThemePath() + "dialogsStyle.css").toExternalForm());
		dialog.setDialogPane(dialogPane);
		// dialogPane.getStyleClass().add("dialogPane");
		dialogPane.setContentText(contentText);

		ButtonType yes = new ButtonType(Main.bundle.getString("yes"), ButtonBar.ButtonData.YES);
		ButtonType no = new ButtonType(Main.bundle.getString("no"), ButtonBar.ButtonData.NO);
		dialog.getDialogPane().getButtonTypes().addAll(yes, no);

		return dialog;

	}

}
