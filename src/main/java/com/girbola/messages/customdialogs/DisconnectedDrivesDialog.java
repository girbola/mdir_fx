package com.girbola.messages.customdialogs;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.folderscanner.ModelFolderScanner;
import com.girbola.drive.DriveInfo;
import com.girbola.drive.DriveScanner;
import com.girbola.messages.Messages;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DisconnectedDrivesDialog extends Dialog<ButtonType> {

    //private DriveScanner driveScanner;

    public DisconnectedDrivesDialog(List<Path> missingPaths, DriveScanner driveScanner) {
      //  this.driveScanner = driveScanner;
        setTitle("Storage Check");
        setHeaderText(null);
        setResizable(true);

        // --- Main Layout ---
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setPrefWidth(500);
        root.getStyleClass().add("disconnected-drives-root");

        // --- Header Section ---
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("disconnected-drives-header");

        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("disconnected-drives-icon-pane");
        Circle iconBg = new Circle(18, Color.web("#FEF3C7"));
        iconBg.getStyleClass().add("disconnected-drives-icon-bg");
        Label iconText = new Label("!");
        iconText.getStyleClass().add("disconnected-drives-icon-text");
        iconPane.getChildren().addAll(iconBg, iconText);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Disconnected Drives Detected");
        titleLabel.getStyleClass().add("disconnected-drives-title");

        Label subtitleLabel = new Label("The scanner is checking for saved locations.");
        subtitleLabel.getStyleClass().add("disconnected-drives-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        header.getChildren().addAll(iconPane, titleBox);

        // --- Path List Container ---
        VBox pathContainer = new VBox(8);
        pathContainer.setPadding(new Insets(12));
        pathContainer.getStyleClass().add("disconnected-drives-path-container");

        Label listHeader = new Label("UNAVAILABLE LOCATIONS (" + missingPaths.size() + ")");
        listHeader.getStyleClass().add("disconnected-drives-list-header");

        VBox pathList = new VBox(8);
        pathList.getStyleClass().add("disconnected-drives-path-list");

        if (missingPaths.isEmpty()) {
            Label emptyLabel = new Label("No disconnected locations were found.");
            emptyLabel.getStyleClass().add("disconnected-drives-empty");
            pathList.getChildren().add(emptyLabel);
        } else {
            for (Path path : missingPaths) {
                HBox item = new HBox(8);
                item.setAlignment(Pos.TOP_LEFT);
                item.getStyleClass().add("disconnected-drives-item");

                Circle dot = new Circle(4, Color.web("#EF4444"));
                dot.getStyleClass().add("disconnected-drives-dot");

                Label pathLabel = new Label(path.toString());
                pathLabel.setWrapText(true);
                pathLabel.setMaxWidth(Double.MAX_VALUE);
                pathLabel.getStyleClass().add("disconnected-drives-path-label");
                pathLabel.setTooltip(new Tooltip(path.toString()));

                HBox.setHgrow(pathLabel, Priority.ALWAYS);
                item.getChildren().addAll(dot, pathLabel);
                pathList.getChildren().add(item);
            }
        }

        ScrollPane pathScrollPane = new ScrollPane(pathList);
        pathScrollPane.getStyleClass().add("disconnected-drives-scroll");
        pathScrollPane.setFitToWidth(true);
        pathScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pathScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pathScrollPane.setPannable(true);
        pathScrollPane.setPrefViewportHeight((int) Math.clamp((long) missingPaths.size() * 30L, 80L, 220L));

        pathContainer.getChildren().addAll(listHeader, pathScrollPane);

        // --- Progress/Status Bar ---
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.getStyleClass().add("disconnected-drives-progress");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(16, 16);

        Label statusText = new Label("Scanning for reconnected USB devices...");
        statusText.getStyleClass().add("disconnected-drives-status");

        if (missingPaths.isEmpty()) {
            spinner.setVisible(false);
            spinner.setManaged(false);
            statusText.setText("No unavailable locations at the moment.");
        }

        progressBox.getChildren().addAll(spinner, statusText);

        // --- Assemble Root ---
        root.getChildren().addAll(header, pathContainer, progressBox);

        // --- Setup Dialog ---
        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(root);
        dialogPane.getStyleClass().add("disconnected-drives-dialog");

        URL mainStyleUrl = Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType());
        if (mainStyleUrl != null) {
            dialogPane.getStylesheets().add(mainStyleUrl.toExternalForm());
        }

        URL dialogsStyleUrl = Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGSSTYLE.getType());
        if (dialogsStyleUrl != null) {
            dialogPane.getStylesheets().add(dialogsStyleUrl.toExternalForm());
        }

        ButtonType retryButtonType = new ButtonType("Retry", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, retryButtonType);

        Button retryButton = (Button) dialogPane.lookupButton(retryButtonType);

        retryButton.setDefaultButton(true);
retryButton.setOnAction(event -> {
            // Trigger the drive scanner to recheck the drives
//    List<DriveInfo> rootDrivesSnapshot = driveScanner.getRootDrivesSnapshot();
//    for(DriveInfo driveInfo : rootDrivesSnapshot) {
//        Messages.sprintf("DriveScanner root drive: " + driveInfo.getDrivePath() + " serial: " + driveInfo.getSerial() + " isConnected: " + driveInfo.isConnected());
//    }
    //Messages.warningText("NOT READY YET!");
            // Close the dialog after retrying
//            this.close();
        });

        // Clean up native dialog padding
        dialogPane.setPadding(Insets.EMPTY);
    }
}
