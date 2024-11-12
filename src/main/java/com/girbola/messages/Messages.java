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
import com.girbola.utils.*;
import common.utils.*;
import java.nio.file.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
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
import javafx.stage.*;

import java.util.Optional;
import javafx.util.*;

import static com.girbola.Main.*;
import static com.girbola.controllers.misc.Misc_GUI.fastExit;

/**
 * @author Marko
 */
public class Messages {

    private static Alert alert;
    private static Optional<ButtonType> result;
    private static Stage errorStage;
    private static String previousMessage;
    private static TextArea errorTextField;
    private static TextArea textArea_alert;
    private static Timeline timeline;

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
     * @param text
     */
    public static void debug(String text) {
        if (DEBUG) {
            sprintf(text);
        }
    }

    /**
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

    public static void workdirConnection(Path workDir) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Workdir is not connected");
        dialog.setContentText(bundle.getString("workDirHasNotConnected"));

        Button retryButton = new Button("Retry");
        Button cancelButton = new Button("Cancel");
        Button browseButton = new Button("Browse");

        retryButton.setOnAction(e -> {
            Messages.sprintf("Retry dialog");
            // You can perform your retry logic here
            retryConnectingWorkDir(Paths.get(Main.conf.getWorkDir()), dialog);
        });

        cancelButton.setOnAction(e -> {
            Messages.sprintf("Closing dialog");
            // Optional: Change button text or perform other actions before closing
            dialog.setResult(Boolean.TRUE);
            dialog.close(); // Close the dialog directly
        });

        browseButton.setOnAction(e -> {
            WorkdirUtils.browseWorkdir();
        });

        HBox buttonPane = new HBox(retryButton, cancelButton, browseButton);
        VBox dialogPane = new VBox(buttonPane);
        dialog.getDialogPane().setContent(dialogPane);
        System.out.println("Showing dialog...");
        // Show the dialog and wait for it to close
        dialog.showAndWait();
    }
//
//    public static void workdirConnection_(Path workDir) {
//
//        sprintf("workdirConnection at: " + workDir);
//        Dialog<ButtonType> dialog = createDialog();
//        dialog.setTitle("Workdir is not connected");
//        dialog.setContentText(bundle.getString("workDirHasNotConnected"));
//
//        ImageView warningSign = new ImageView(new Image(Main.class.getResourceAsStream("/img/errorSign.png")));
//        dialog.setGraphic(warningSign);
//
//        ButtonType ok_TypeButton = new ButtonType("Retry", ButtonData.OK_DONE);
//        ButtonType cancel_TypeButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
//        dialog.getDialogPane().getButtonTypes().addAll(ok_TypeButton, cancel_TypeButton);
//
//        Optional<ButtonType> result = dialog.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            sprintf("Retrying and waiting for connect 10 seconds");
//            // Create a Timeline to check for the file's existence
//            //retryConnectingWorkDir(workDir, dialog);
//        } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
//            sprintf("Cancel pressed warningText");
//            fadeOutDialog(dialog);
//        }
//
//    }

    private static void retryConnectingWorkDir(Path workDir, Dialog<Boolean> dialog) {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            try {
                if (Files.exists(workDir)) {
                    String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(String.valueOf(workDir));


                    String workDirSerialNumber = conf.getWorkDirSerialNumber();
                    if(!driveSerialNumber.isEmpty() || !workDirSerialNumber.isEmpty()) {
                        if(driveSerialNumber.equals(workDirSerialNumber)) {
                            Messages.sprintf("Driver letter has changed: " + workDir + " current workDirSerialNumber: " + workDirSerialNumber);
                        }
                    }
                    Messages.sprintf("workdir were found at: " + workDir);
                    Platform.runLater(() -> {

                    dialog.setContentText(bundle.getString("workDirIsNowConnected"));
                    dialog.setResult(Boolean.TRUE);
                    fadeOutDialog(dialog);

                    });
                    timeline.stop(); // Stop the timeline
                } else {
                    Messages.sprintf("Workdir is not connected at: " + workDir);
                    timeline.play();
                }
            } catch (Exception e) {
                errorSmth("Messages", "Error checking work directory connection", e, Thread.currentThread().getStackTrace()[1].getLineNumber(), false);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // Run indefinitely for periodic checks
        timeline.play(); // Start the periodic checks
    }

    public static void fadeOutDialog(Dialog<?> dialog) {
        // Create a FadeTransition for the dialog
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), dialog.getDialogPane());
        fadeTransition.setFromValue(1.0); // Start fully visible
        fadeTransition.setToValue(0.0); // End fully transparent
        fadeTransition.setOnFinished(event -> dialog.close()); // Close the dialog after fading out
        fadeTransition.play(); // Start the fade-out effect
    }
}
