package com.girbola.messages;

import com.girbola.Main;
import com.girbola.utils.WorkdirUtils;
import common.utils.OSHI_Utils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.errorSmth;

public class WorkdirGUI {

    private static Timeline timeline;

    private static WorkdirGUI instance;

    private static Dialog<Boolean> dialog;

    private static Stage owner;
    private static Path workDir;

    private WorkdirGUI(Path workDir, Stage owner) {
        WorkdirGUI.owner = owner;
        WorkdirGUI.workDir = workDir;
        Messages.sprintf("CoNSTRuctor DIALOG: " + dialog);
    }

    public static synchronized WorkdirGUI getInstance(Path workDir, Stage owner) {
        if (instance == null) {
            instance = new WorkdirGUI(workDir, owner);
            Messages.sprintf("cancel DIALOG: " + owner);
            workdirConnection();
        }
        return instance;
    }

    private static void workdirConnection() {
        dialog = new Dialog<>();
        Messages.sprintf("workdirConnection DIALOG: " + dialog);
        dialog.setTitle("Workdir is not connected");
        //dialog.setContentText(bundle.getString("workDirHasNotConnected"));
        //dialog.initOwner(owner);
//        dialog.initModality(Modality.APPLICATION_MODAL);
//        dialog.initStyle(StageStyle.UTILITY);
        dialog.setResizable(false);
        //dialog.setOnCloseRequest(Event::consume);

        Button retryButton = new Button("Retry");
        Button cancelButton = new Button("Cancel");
        Button browseButton = new Button("Browse");

        retryButton.setOnAction(e -> {
            Messages.sprintf("Retry dialog");

            // You can perform your retry logic here
            Platform.runLater(WorkdirGUI::retryConnectingWorkDir);
        });

        cancelButton.setOnAction(e -> {
            Messages.sprintf("Closing dialog");
            // Optional: Change button text or perform other actions before closing
            Platform.runLater(() -> {
                dialog.setResult(Boolean.TRUE);
                Messages.sprintf("cancel DIALOG: " + dialog);
                dialog.close(); // Close the dialog directly

                if (timeline != null) {
                    timeline.stop();
                }
            });
        });

        browseButton.setOnAction(e -> {
            Platform.runLater(WorkdirUtils::browseWorkdir);
        });

        HBox buttonPane = new HBox(retryButton, cancelButton, browseButton);

        AtomicReference<Double> xOffset = new AtomicReference<>((double) 0);
        AtomicReference<Double> yOffset = new AtomicReference<>((double) 0);

        Label topLabel = new Label(bundle.getString("workDirHasNotConnected"));
        topLabel.setOnMousePressed(event -> {
            Messages.sprintf("Drargging: yay");
            Platform.runLater(() -> {
                xOffset.set(event.getSceneX());
                yOffset.set(event.getScreenY());
            });
        });

        topLabel.setOnMouseDragged(event -> {
            Platform.runLater(() -> {
                owner.setX(event.getScreenX() - xOffset.get());
                owner.setY(event.getScreenY() - yOffset.get());
            });
        });

        VBox dialogPane = new VBox(topLabel, buttonPane);

        dialog.getDialogPane().setContent(dialogPane);

        Messages.sprintf("Showing dialog...");
        // Show the dialog and wait for it to close
        Platform.runLater(() -> {
            dialog.showAndWait();
        });
    }

    private static void retryConnectingWorkDir() {
        try {
            if (Files.exists(workDir)) {
                String driveSerialNumber = OSHI_Utils.getDriveSerialNumber(String.valueOf(workDir));

                String workDirSerialNumber = Main.conf.getWorkDirSerialNumber();
                if (!driveSerialNumber.isEmpty() || !workDirSerialNumber.isEmpty()) {
                    if (driveSerialNumber.equals(workDirSerialNumber)) {
                        Messages.sprintf("Driver letter is same: " + workDir + " current workDirSerialNumber: " + workDirSerialNumber);
                        fadeOutDialog(dialog);
                    } else {
                        Messages.sprintf("Driver letter has changed: " + workDir + " current workDirSerialNumber: " + workDirSerialNumber);
                    }
                }
                Messages.sprintf("workdir were found at: " + workDir);
                Platform.runLater(() -> {

                    dialog.setContentText(bundle.getString("workDirIsNowConnected"));
                    dialog.setResult(Boolean.TRUE);
                    fadeOutDialog(dialog);

                });
                //timeline.stop(); // Stop the timeline
            } else {
                Messages.sprintf("Workdir is not connected at: " + workDir);
                //timeline.play();
            }
        } catch (Exception e) {
            errorSmth("Messages", "Error checking work directory connection", e, Thread.currentThread().getStackTrace()[1].getLineNumber(), false);
        }
    }

    private static void fadeOutDialog(Dialog<Boolean> dialog) {
        // Create a FadeTransition for the dialog
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), dialog.getDialogPane());
        fadeTransition.setFromValue(1.0); // Start fully visible
        fadeTransition.setToValue(0.0); // End fully transparent
        fadeTransition.setOnFinished(event -> dialog.close()); // Close the dialog after fading out
        fadeTransition.play(); // Start the fade-out effect
    }

}
