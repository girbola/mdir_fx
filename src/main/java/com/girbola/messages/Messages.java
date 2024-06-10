/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain

 */
package com.girbola.messages;

import com.girbola.MDir_Constants;
import com.girbola.Main;
import com.girbola.messages.html.HTMLClass;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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

//    final private static int dialog_x = 500;
//    final private static int dialog_y = 300;

	public static Dialog<ButtonType> createDialog() {
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogPane dialogPane = new DialogPane();
		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().getStylesheets().add(Main.conf.getThemePath() + MDir_Constants.DIALOGSSTYLE);
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
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Constants.DIALOGS.getType()).toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		return alert;
	}

	private static Stage errorStage;
	private static TextArea errorTextField;

	public static void errorSmth_stage(String className, String message, Exception exception, int line, boolean exit) {
		if (exit) {
			Main.setProcessCancelled(true);
		}
		if (errorStage == null) {
			errorStage = new Stage();
			BorderPane bp = new BorderPane();

			errorTextField = new TextArea();
			errorTextField.setEditable(false);

			bp.setCenter(errorTextField);

			Button button = new Button("Close");
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					errorStage.hide();
				}
			});

			bp.setBottom(button);
			Scene scene = new Scene(bp);
			errorStage.setScene(scene);
			errorStage.show();

//			errorStage
		}
		if (errorStage.isShowing()) {
			if (exit) {
				errorStage.showAndWait();
			}
			errorTextField.appendText("isShowing\n" + message);
		} else {
			errorTextField.appendText("\nnot showing" + message);
			if (exit) {
				errorStage.showAndWait();
			} else {
				errorStage.show();
			}
		}
	}

	private static Alert alert;
	private static TextArea textArea_alert;
	private static Optional<ButtonType> result;
	private static String previousMessage;

	/**
	 * @param className Class name helps to find right class faster.
	 * @param message   Message to describe the error
	 * @param line      calls getLineNumber() of current clas
	 * @param exit      if exit is true the whole program will be closed. And if
	 *                  exit is false it won't quit
	 */
	public static void errorSmth(String className, String message, Exception exception, int line, boolean exit) {
		Messages.sprintf("errorSmth triggered. " + " message: " + message + " className= " + className + " at line: " + line);
		if (exit) {
			Main.setProcessCancelled(true);
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
//	public static void errorText_(String text, boolean exit) {
//		Dialog<ButtonType> dialog = createDialog();
//		// if(dialog != null || !dialog.isShowing()) {
//		//
//		// }
//		dialog.setTitle("Error");
//
//		dialog.setContentText(text);
//		ImageView errorSign = new ImageView(new Image(Main.class.getResourceAsStream("/resources/img/errorSign.png")));
//		dialog.setGraphic(errorSign);
//
//		ButtonType ok_TypeButton = new ButtonType("CLOSE", ButtonData.CANCEL_CLOSE);
//		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);
//
//		Optional<ButtonType> result = dialog.showAndWait();
//		sprintf("Buttpntype is = " + result);
//		if ((result.isPresent()) && (result.get().getText().equals("CLOSE"))) {
//
//			// if (result.isPresent() && result.get() == ButtonType.CLOSE) {
//			sprintf("Ok pressed at errortext");
//			if (exit) {
//				sprintf("Exiting program...");
//				fastExit();
//			} else {
//				sprintf("Closing dialog...");
//				dialog.close();
//			}
//		}
//	}

//	public static void errorText_(String text, String headerText, boolean exit) {
//		Dialog<ButtonType> dialog = createDialog();
//
//		dialog.setTitle("Error");
//		if (!headerText.isEmpty() || headerText != null) {
//			dialog.setHeaderText(headerText);
//		}
//		dialog.setContentText(text);
//		ImageView errorSign = new ImageView(new Image(Main.class.getResourceAsStream("/resources/img/errorSign.png")));
//		dialog.setGraphic(errorSign);
//		ButtonType ok_TypeButton = new ButtonType("OK", ButtonData.OK_DONE);
//		dialog.getDialogPane().getButtonTypes().add(ok_TypeButton);
//
//		Optional<ButtonType> result = dialog.showAndWait();
//		if (result.isPresent() && result.get() == ButtonType.OK) {
//			sprintf("Ok pressed at errortext");
//			dialog.close();
//			if (exit) {
//				fastExit();
//			}
//		}
//	}

	public static void okText(String text) {
		Dialog<ButtonType> dialog = createDialog();
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
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Constants.DIALOGSSTYLE.getType()).toExternalForm());
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
		Dialog<ButtonType> dialog = createDialog();
		DialogPane dialogPane = new DialogPane();
		// dialogPane.setHeaderText(text);
		Image helpImg = new Image(Main.class.getResourceAsStream("/resources/img/helpSign.png"), 30, 0, true, true);
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
	public static void warningTextList(String text, List<String> list) {
		Dialog<ButtonType> dialog = createDialog();
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
		Dialog<ButtonType> dialog = createDialog();
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

	public static void showAlert(String message, @SuppressWarnings("exports") AlertType alertType) {
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Constants.DIALOGSSTYLE.getType()).toExternalForm());
		dialogPane.getStyleClass().add("alertDiag");
		dialogPane.setHeaderText(null);
		dialogPane.setContentText(message);
		alert.showAndWait();
	}

	public static void sprintf(String string, Object object) {
		System.out.printf(string, object);

	}

}
