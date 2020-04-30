package com.girbola.dialogs;

import static com.girbola.Main.conf;

import com.girbola.Main;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class Dialogs {
	/*
	 * Return Dialog<ButtonType> Yes, No and Cancel ButtonTypes
	 */
	public static Dialog<ButtonType> createDialog_YesNoCancel(final String contentText) {
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogPane dialogPane = new DialogPane();
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + "dialogsStyle.css").toExternalForm());
		dialog.setDialogPane(dialogPane);
		//dialogPane.getStyleClass().add("dialogPane");
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
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + "dialogsStyle.css").toExternalForm());
		dialog.setDialogPane(dialogPane);
		//dialogPane.getStyleClass().add("dialogPane");
		dialogPane.setContentText(contentText);

		ButtonType yes = new ButtonType(Main.bundle.getString("yes"), ButtonBar.ButtonData.YES);
		ButtonType no = new ButtonType(Main.bundle.getString("no"), ButtonBar.ButtonData.NO);
		dialog.getDialogPane().getButtonTypes().addAll(yes, no);

		return dialog;

	}

}
