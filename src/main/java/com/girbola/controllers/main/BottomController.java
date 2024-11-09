/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.main.tables.DuplicateStatistics;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.workdir.WorkDirController;
import com.girbola.fxml.datestreetableview.DatesTreeTableViewController;
import com.girbola.media.collector.Collector;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import com.girbola.misc.Misc;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static common.utils.FileUtils.supportedImage;

public class BottomController {

    private final String ERROR = BottomController.class.getSimpleName();
    private ModelMain model_main;
    private DuplicateStatistics duplicateStatistics;

    @FXML
    private Label debug_pref_width;
    @FXML
    private Button addFolders_btn;
    @FXML
    private Button copy_ok_date_btn;
    @FXML
    private Button copySelected_btn;
    @FXML
    private Button help_btn;
    @FXML
    private Button options_btn;
    @FXML
    private Button start_copyBatch_btn;
    @FXML
    private Button workDir_btn;
    @FXML
    private Button collect;
    @FXML
    private Label drive_name;
    @FXML
    private Label drive_space;
    @FXML
    private Label drive_spaceLeft;
    @FXML
    private Label drive_connected;
    @FXML
    private HBox drive_pane;
    @FXML
    private Button removeDuplicates_btn;
    @FXML
    private Button dates_ttv_btn;
    @FXML
    private Button showWorkdir_btn;

    @FXML
    private void showWorkdir_btn_action(ActionEvent event) {
        Platform.runLater(() -> {

            try {
                Parent parent = null;
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/workdir/Workdir.fxml"), Main.bundle);
                parent = loader.load();
                WorkDirController workdirController = (WorkDirController) loader.getController();
                workdirController.init(model_main);
                Scene scene = new Scene(parent);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
                workdirController.createFileBrowserTreeTableView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void findImageDuplicatesMenuItemAction(ActionEvent event) {

    }


    @FXML
    private void moveFilesToSortedTable_btn_action(ActionEvent event) {
//		TableUtils.checkTableDuplicates(null, null)
    }





    @FXML
    private void dates_ttv_btn_action(ActionEvent event) {
        try {
            Parent parent = null;
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("fxml/datestreetableview/DatesTreeTableView.fxml"), Main.bundle);
            parent = loader.load();
            DatesTreeTableViewController datesTreeTableViewController = (DatesTreeTableViewController) loader
                    .getController();
            datesTreeTableViewController.init(model_main);
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void collect_action(ActionEvent event) {
        Collector collector = new Collector();
        collector.collectAll(model_main.tables());
        collector.listMap(model_main.tables());

//		drive_pane.visibleProperty().model_main

    }

    @FXML
    private void workDir_btn_action(ActionEvent event) {
        Messages.sprintf("Not ready yet!");
        Parent parent = null;
        FXMLLoader workDirLoader = new FXMLLoader(Main.class.getResource("fxml/main/Main.fxml"), bundle);
        sprintf("main_loader location: " + workDirLoader.getLocation());
        // TODO Tarkista tämä
        WorkDirController workDirController = (WorkDirController) workDirLoader.getController();
        // try {
        // parent = workDirLoader.load();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        Scene workDir_scene = new Scene(parent);
        Stage workDir_stage = new Stage();
        workDir_stage.setScene(workDir_scene);
        workDir_stage.show();

    }

    @FXML
    private void addFolders_btn_action(ActionEvent action) {

        Messages.sprintf("locale is: " + Main.bundle.getLocale().toString());
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/folderscanner/FolderScanner.fxml"),
                Main.bundle);

        Parent parent = null;
        FolderScannerController folderScannerController = null;
        try {
            parent = loader.load();
            folderScannerController = (FolderScannerController) loader.getController();
        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.errorSmth(ERROR,
                    "Country= " + Main.bundle.getLocale().getCountry() + " location?\n: " + Main.bundle.getLocale(), ex,
                    Misc.getLineNumber(), true);
        }
        Stage fc_stage = new Stage();
//		fc_stage.setWidth(conf.getScreenBounds().getWidth());
//		fc_stage.setHeight(conf.getScreenBounds().getHeight() / 1.3);
        fc_stage.setOnCloseRequest(event -> fc_stage.close());

        Scene fc_scene = new Scene(parent, 800, 400);
        fc_scene.getStylesheets()
                .add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.FOLDERCHOOSER.getType()).toExternalForm());
        folderScannerController.setStage(fc_stage);
        folderScannerController.setScene(fc_scene);
        folderScannerController.init(model_main);
        fc_stage.setScene(fc_scene);

        fc_stage.show();

    }

    @FXML
    private void copy_ok_date_btn_action(ActionEvent event) {
        Main.setProcessCancelled(false);
        try {
            if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
                warningText(bundle.getString("cannotFindWorkDir"));
                return;
            }
        } catch (IOException ex) {
            warningText(bundle.getString("cannotFindWorkDir"));
            return;
        }
    }

    /*
     *
     * Check if file exists: - Workdir - Check if file exists already in different
     * time - Copy to destination
     */

    /**
     * Copy selected files
     *
     * @param event
     */
    @FXML
    private void copySelected_btn_action(ActionEvent event) {
        Messages.warningText("Under construction");
    }

    @FXML
    private void help_btn_action(ActionEvent event) {
        Messages.warningTextHelp(
                "Drag and drop folders to left \"SortIt\" which are not created by you or you want them to be sorted manualy",
                HTMLClass.help_html + "#sorter");
    }

    @FXML
    private void options_btn_action(ActionEvent event) {

    }

    @FXML
    private void start_copyBatch_btn_action(ActionEvent event) {
        Main.setProcessCancelled(false);
        model_main.getMonitorExternalDriveConnectivity().cancel();

        if (Main.conf.getWorkDir().equals("null")) {
            Messages.warningText(Main.bundle.getString("workDirHasNotBeenSet"));
            return;
        }
        if (!Main.conf.getDrive_connected() || !Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            Messages.warningText(Main.bundle.getString("workDirHasNotConnected"));
            return;
        }
        Messages.sprintf("workDir: " + Main.conf.getWorkDir());
        /*
         * List files and handle actions with lists. For example ok files, conflict
         * files(Handle this before ok files), bad files(Handle this before okfiles)
         * When everything are good will be operateFiles starts. Notice! Everything are
         * in memory already so concurrency can be used to prevent the lagging.
         */

        CopyBatch copyBatch = new CopyBatch(model_main);
        copyBatch.start();

        // check Destinatiin duplicates/existens
        /*
         * Main.conf.work if(!copyBatch.getConflictList().isEmpty()) {
         * copyBatch.getConflictList(); }
         */
//TODO Testaan ensin workdir konfliktien varalta ennen kopiointia. Täytyy pystyy korjaavaaman ne ennen kopintia. cantcopyt tulee errori 

    }

    public static boolean hasCheckWorkDirConflict(ObservableList<FolderInfo> obs) {
        for (FolderInfo fi : obs) {
            if (Paths.get(fi.getFolderPath()).getParent().toString().equals(conf.getWorkDir())) {
                return true;
            }
        }
        return false;
    }

    @FXML
    public void initialize() {
        sprintf("bottom controller...");
    }

    public void initBottomWorkdirMonitors() {
        if (drive_name == null || drive_space == null || drive_spaceLeft == null || drive_connected == null) {
            return;
        }

        Messages.sprintf("initBottomWorkdirMonitors started");
        drive_name.textProperty().bindBidirectional(Main.conf.drive_name_property());
        drive_space.textProperty().bindBidirectional(Main.conf.drive_space_property());
        drive_spaceLeft.textProperty().bindBidirectional(Main.conf.drive_spaceLeft_property());
        drive_pane.visibleProperty().bindBidirectional(Main.conf.drive_connected_property());
        Main.conf.drive_connected_property().addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {
                drive_connected.setStyle("-fx-background-color: green;");
                start_copyBatch_btn.setDisable(false);
            } else {
                drive_connected.setStyle("-fx-background-color: red;");
                start_copyBatch_btn.setDisable(true);
            }
            Messages.sprintf("drive connected? " + newValue);
        });
        Main.conf.setDrive_connected(true);
        Main.conf.setDrive_connected(false);
    }

    public void init(ModelMain aModel_main) {
        this.model_main = aModel_main;
        sprintf("bottom controller...");
        debug_pref_width.textProperty().bind(model_main.getTable_root_hbox_width());
    }

}
