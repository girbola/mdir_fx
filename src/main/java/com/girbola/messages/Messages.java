/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain

 */
package com.girbola.messages;

import static com.girbola.Main.DEBUG;
import static com.girbola.Main.DEBUG_CONF;
import static com.girbola.Main.conf;
import static com.girbola.Main.country;
import static com.girbola.controllers.misc.Misc_GUI.fastExit;
import static com.girbola.messages.html.HTMLClass.urlExt;

import java.util.ArrayList;
import java.util.Optional;

import com.girbola.Main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Marko
 */
public class Messages {

//    final private static int dialog_x = 500;
//    final private static int dialog_y = 300;

	public static Dialog<ButtonType> createDialog() {
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogPane dialogPane = new DialogPane();
		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().getStylesheets().add(Main.conf.getThemePath() + "dialogsStyle.css");
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

	private static Alert createAlert(AlertType alertType) {
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + "dialog.css").toExternalForm());

		dialogPane.getStyleClass().add("alertDiag");

		return alert;
	}

	/**
	 * @param className Class name helps to find right class faster.
	 * @param message   Something went wrong + ERROR code
	 * @param line      calls getLineNumber() method
	 * @param exit      if exiting or not after pressing OK
	 */
	public static void errorSmth(String className, String message, Exception exception, int line, boolean exit) {
		Messages.sprintf("errorSmth triggered. " + " className= " + className + " Line is: " + line);
		if (exit) {
			Main.setProcessCancelled(true);
		}
		Alert alert = createAlert(AlertType.ERROR);
		alert.setTitle(Main.bundle.getString("error"));

		TextArea textArea = new TextArea(message);
		alert.getDialogPane().setContent(textArea);
		alert.getDialogPane().setHeaderText(className + " at line " + line);
		if (exception != null) {
			textArea.appendText("\n\n==============" + exception.getMessage());
		}
		alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				alert.close();
			}
		});
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			if (exit) {
				sprintf("Program will be exited");
				fastExit();
			}
			sprintf("Ok pressed at errortext");
			alert.close();
		}
	}

	/**
	 *
	 * @param text
	 * @param exit
	 */
	public static void isAlreadyRunning(String text, boolean exit) {
		
		Dialog<ButtonType> dialog = createDialog();
		dialog.setTitle("Error");
		dialog.setContentText(text);
		ImageView errorSign = new ImageView(new Image(Main.class.getResourceAsStream("/resources/img/errorSign.png")));
		dialog.setGraphic(errorSign);

		ButtonType ok_TypeButton = new ButtonType("CLOSE", ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		sprintf("Buttpntype is = " + result);
		if ((result.isPresent()) && (result.get().getText().equals("CLOSE"))) {

			sprintf("Ok pressed at errortext");
			if (exit) {
				sprintf("Exiting programm...");
				fastExit();
			} else {
				sprintf("Closing dialog...");
				dialog.close();
			}
		}
	}
	public static void errorText_(String text, boolean exit) {
		Dialog<ButtonType> dialog = createDialog();
		// if(dialog != null || !dialog.isShowing()) {
		//
		// }
		dialog.setTitle("Error");

		dialog.setContentText(text);
		ImageView errorSign = new ImageView(new Image(Main.class.getResourceAsStream("/resources/img/errorSign.png")));
		dialog.setGraphic(errorSign);

		ButtonType ok_TypeButton = new ButtonType("CLOSE", ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		sprintf("Buttpntype is = " + result);
		if ((result.isPresent()) && (result.get().getText().equals("CLOSE"))) {

			// if (result.isPresent() && result.get() == ButtonType.CLOSE) {
			sprintf("Ok pressed at errortext");
			if (exit) {
				sprintf("Exiting program...");
				fastExit();
			} else {
				sprintf("Closing dialog...");
				dialog.close();
			}
		}
	}

	public static void errorText_(String text, String headerText, boolean exit) {
		Dialog<ButtonType> dialog = createDialog();

		dialog.setTitle("Error");
		if (!headerText.isEmpty() || headerText != null) {
			dialog.setHeaderText(headerText);
		}
		dialog.setContentText(text);
		ImageView errorSign = new ImageView(new Image(Main.class.getResourceAsStream("/resources/img/errorSign.png")));
		dialog.setGraphic(errorSign);
		ButtonType ok_TypeButton = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			sprintf("Ok pressed at errortext");
			dialog.close();
			if (exit) {
				fastExit();
			}
		}
	}

	public static void okText(String text) {
		Dialog<ButtonType> dialog  = createDialog();
		dialog.setTitle("");
		dialog.setContentText(text);
		ImageView warningImage = new ImageView(new Image(Main.class.getResourceAsStream("/resources/img/okSign.png")));

		dialog.setGraphic(warningImage);
		ButtonType ok_TypeButton = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			sprintf("Ok pressed warningtext");
			dialog.close();
		}
	}

	/**
	 *
	 * @param text
	 */
	public static void warningText(String text) {
		Alert alert = new Alert(AlertType.WARNING);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setContentText(text);
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + "dialog.css").toExternalForm());
		dialogPane.setHeaderText("");

		dialogPane.getStyleClass().add("alertDiag");

		alert.showAndWait();
	}

	/**
	 *
	 * @param text
	 * @param helpURL
	 */
	public static void warningTextHelp(String text, String helpURL) {
		Dialog<ButtonType> dialog  = createDialog();
		DialogPane dialogPane = new DialogPane();
		// dialogPane.setHeaderText(text);
		Image helpImg = new Image(Main.class.getResourceAsStream("/resources/img/helpSign.png"), 30, 0, true, true);
		ImageView helpIcon = new ImageView(helpImg);

		Button hl = new Button();
		hl.setGraphic(helpIcon);
		hl.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sprintf("Help pressed: " + conf.getProgramHomePage() + "/" + country + "/" + helpURL + urlExt);
			}
		});

		TextFlow lbl = new TextFlow(new Text("\nClick to get help"), hl);

		BorderPane bpane = new BorderPane();

		HBox hbox = new HBox();
		hbox.setStyle("-fx-border-color: cyan;");
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.getChildren().addAll(lbl);

		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().setContent(bpane);

		ImageView warningImage = new ImageView(
				new Image(Main.class.getResourceAsStream("/resources/img/warningSign.png")));
		bpane.setLeft(warningImage);
		bpane.setCenter(new Label(text));
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

	/**
	 *
	 * @param text
	 * @param list
	 */
	public static void warningTextList(String text, ArrayList<String> list) {
		Dialog<ButtonType> dialog  = createDialog();
		DialogPane dialogPane = new DialogPane();
		dialogPane.setMaxSize(800, 500);
		dialogPane.setMinSize(800, 500);
		dialogPane.setPrefSize(800, 500);

		// dialogPane.setHeaderText(text);
		VBox vbox = new VBox(1);
		// vbox.setMinSize(800, 500);
		// vbox.setMaxSize(800, 500);
		// vbox.setPrefSize(800, 500);

		for (String s : list) {
			Label label = new Label(s);
			vbox.getChildren().add(label);
		}

		BorderPane bpane = new BorderPane();
		// bpane.setMinSize(800, 500);
		// bpane.setMaxSize(800, 500);
		// bpane.setPrefSize(800, 500);

		ScrollPane scr = new ScrollPane();
		// scr.setMinSize(800, 500);
		// scr.setMaxSize(800, 500);
		// scr.setPrefSize(800, 500);

		scr.setContent(vbox);
		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().setContent(bpane);

		ImageView warningImage = new ImageView(
				new Image(Main.class.getResourceAsStream("/resources/img/warningSign.png")));
		warningImage.setStyle("-fx-background-padding: 5;");
		bpane.setLeft(warningImage);
		bpane.setTop(new Label(text));
		bpane.setCenter(scr);

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
	 * @param text
	 * @param headerText
	 */
	public static void warningText_title(String text, String headerText) {
		Dialog<ButtonType> dialog  = createDialog();
		dialog.setTitle("Error");
		dialog.setHeaderText(headerText);
		dialog.setContentText(text);
		ImageView warningSign = new ImageView(
				new Image(Main.class.getResourceAsStream("/resources/img/errorSign.png")));
		dialog.setGraphic(warningSign);
		ButtonType ok_TypeButton = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);

		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			sprintf("Ok pressed warningText");
			dialog.close();
		}

	}

	public static void printf(String string, Object object) {
		System.out.printf(string, object);
	}

	public static void showAlert(String message, @SuppressWarnings("exports") AlertType alertType) {
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + "dialogs.css").toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		dialogPane.setHeaderText(null);
		dialogPane.setContentText(message);
		alert.showAndWait();
	}

	public static void sprintf(String string, Object object) {
		System.out.printf(string, object);

	}

}
