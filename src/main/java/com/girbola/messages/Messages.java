/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain

 */
package com.girbola.messages;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.messages.html.HTMLClass;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

import static com.girbola.Main.*;
import static com.girbola.controllers.misc.Misc_GUI.fastExit;

/**
 *
 * @author Marko
 */
public class Messages {

	private static Alert alert;
	private static Optional<ButtonType> result;
	private static Stage errorStage;
	private static String previousMessage;
	private static TextArea errorTextField;
	private static TextArea textArea_alert;

//    final private static int dialog_x = 500;
//    final private static int dialog_y = 300;

	public static Dialog<ButtonType> createDialog() {
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogPane dialogPane = new DialogPane();
		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().getStylesheets().add(Main.conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGSSTYLE);
		return dialog;
	}

	/**
	 *
	 * @param text
	 */
	public static void sprintf(String text) {
		if (DEBUG) {
			System.out.println(text);
		}
	}

	public static void sprintfError(String text) {
		if (DEBUG) {
			System.err.println(text);
		}
	}

	/**
	 *
	 * @param text
	 */
	public static void debug(String text) {
		if (DEBUG) {
			sprintf(text);
		}
	}

	/**
	 *
	 * @param text
	 * @param line
	 */
	public static void debug_conf(String text, int line) {
		if (DEBUG_CONF) {
			sprintf(text + " line: " + line);
		}
	}

	public static Alert createAlert(AlertType alertType) {
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGS.getType()).toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		return alert;
	}


	/**
	 * @param className Class name helps to find right class faster.
	 * @param message   Message to describe the error
	 * @param line      calls getLineNumber() of current clas
	 * @param exit      if exit is true the whole program will be closed. And if
	 *                  exit is false it won't quit
	 */
	public static void errorSmth(String className, String message, Exception exception, int line, boolean exit) {
		sprintf("errorSmth triggered. " + " message: " + message + " className= " + className + " at line: " + line);
		if (exit) {
			Main.setProcessCancelled(true);
			ConcurrencyUtils.stopAllExecThreadNow();
			// sit näytetään showAndWait muuten jokin toinen dialigi tyyli
		}
//TODO TÄMÄ TÄYTYY VAIHTAA Dialogiksi mutta stagea käyttäen
//		Node node = alert.getDialogPane().getContent();
		if (alert == null) {
			alert = createAlert(AlertType.ERROR);
			alert.setTitle(Main.bundle.getString("error"));
			textArea_alert = new TextArea(message);
			alert.getDialogPane().setContent(textArea_alert);
			alert.getDialogPane().setHeaderText(className + " at line " + line);
			if (exception != null) {
				textArea_alert.appendText("\n\n==============" + exception.getMessage());
			}
			alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
				@Override
				public void handle(DialogEvent event) {
					alert.hide();
				}
			});
			result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				if (exit) {
					sprintf("Program will be exited");
					fastExit();
				} else {
				sprintf("Ok pressed at errortext");
				alert.hide();
				}
			}
		} else {

			if (alert.isShowing()) {
				if (previousMessage != message) {
					Platform.runLater(() -> {
						alert.getDialogPane().setHeaderText(className + " at line " + line);
						textArea_alert.appendText("\n============================ NEXT ERROR: " + message);
						previousMessage = message;
					});
				} else {
					return;
				}

			} else {
				if (!previousMessage.contentEquals(message)) {
					Platform.runLater(() -> {
						alert.getDialogPane().setHeaderText(className + " at line " + line);
						textArea_alert.setText(message + " new message");
						previousMessage = message;
					});
				} else {
					return;
				}
				if (exception != null) {
					textArea_alert.appendText("\n\n==============" + exception.getMessage());
				}

				result = alert.showAndWait();
			}
		}

	}

	/**
	 *
	 * @param message
	 */
	public static void warningText(String message) {
		sprintf("warningText: " + message);
		Alert alert = new Alert(AlertType.WARNING);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setContentText(message);
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGSSTYLE.getType()).toExternalForm());
		dialogPane.setHeaderText("");

		dialogPane.getStyleClass().add("alertDiag");

		alert.showAndWait();
	}

	/**
	 *
	 * @param message
	 * @param helpURL
	 */
	public static void warningTextHelp(String message, String helpURL) {
		sprintf("message: " + message + " helpURL: " + helpURL);
		Dialog<ButtonType> dialog = createDialog();
		DialogPane dialogPane = new DialogPane();
		// dialogPane.setHeaderText(text);
		Image helpImg = new Image(Main.class.getResourceAsStream("/img/helpSign.png"), 30, 0, true, true);
		ImageView helpIcon = new ImageView(helpImg);

		Button helpButton = new Button();
		helpButton.setGraphic(helpIcon);
		helpButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sprintf("Help pressed: " + HTMLClass.programHomePage + "/" + helpURL);
			}
		});

		TextFlow lbl = new TextFlow(new Text("\nClick to get help"), helpButton);

		BorderPane bpane = new BorderPane();

		HBox hbox = new HBox();
		hbox.setStyle("-fx-border-color: cyan;");
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.getChildren().addAll(lbl);

		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().setContent(bpane);

		ImageView warningImage = new ImageView(
				new Image(Main.class.getResourceAsStream("/img/warningSign.png")));
		bpane.setLeft(warningImage);
		bpane.setCenter(new Label(message));
		bpane.setBottom(lbl);
		// dialog.setGraphic(warningImage);
		ButtonType ok_TypeButton = new ButtonType("OK", ButtonData.OK_DONE);

		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			sprintf("Ok pressed warningtext");
			dialog.close();
		}
	}

	@SuppressWarnings("exports")
	public static Dialog<ButtonType> ask(String titleText, String headerText, String contentText, int lineNumber) {
		sprintf("titledText: " + titleText + " headerText: " + headerText + " contentText: " + contentText + " line: " + lineNumber);
		Dialog<ButtonType> dialog = createDialog();
		if (!titleText.isEmpty()) {
			dialog.setTitle(titleText);
		}
		if (!headerText.isEmpty()) {
			dialog.setHeaderText(headerText);
		}
		if (!contentText.isEmpty()) {
			dialog.setContentText(contentText);
		} else {
			dialog.setContentText("contentText were empty at line: " + lineNumber);
		}

		return dialog;
	}

	/**
	 *
	 * @param message
	 * @param headerText
	 */
	public static void warningText_title(String message, String headerText) {
		sprintf("warningText_title: " + message + " headerText: " + headerText);
		Dialog<ButtonType> dialog = createDialog();
		dialog.setTitle("Error");
		dialog.setHeaderText(headerText);
		dialog.setContentText(message);
		ImageView warningSign = new ImageView(
				new Image(Main.class.getResourceAsStream("/img/errorSign.png")));
		dialog.setGraphic(warningSign);
		ButtonType ok_TypeButton = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			sprintf("Ok pressed warningText");
			dialog.close();
		}

	}

	public static void showAlert(String message, @SuppressWarnings("exports") AlertType alertType) {
		sprintf("warningText_title: " + message + " alertType: " + alertType.toString());
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGSSTYLE.getType()).toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		dialogPane.setHeaderText(null);
		dialogPane.setContentText(message);
		alert.showAndWait();
	}

	public static void sprintf(String string, Object object) {
		System.out.printf(string, object);

	}

}
