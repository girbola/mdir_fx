/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.workdir.WorkDirController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import com.girbola.fxml.datestreetableview.DatesTreeTableViewController;
import com.girbola.media.collector.Collector;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import com.girbola.misc.Misc;
import common.utils.Conversion;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static common.utils.FileUtils.supportedImage;

public class BottomController {

    private final String ERROR = BottomController.class.getSimpleName();
    private Model_main model_main;
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
    private void findImageDuplicates_Btn_action(ActionEvent event) {
        duplicateStatistics = new DuplicateStatistics();

        Task<Void> removeImageDuplicatestask = new Task<>() {

            @Override
            protected Void call() throws Exception {

                Platform.runLater(() -> removeDuplicates_btn.setDisable(true));
                removeImageDuplicates(duplicateStatistics, model_main.tables().getSorted_table(), model_main.tables().getSorted_table(),
                        "Sorted -> Sorted");
                removeImageDuplicates(duplicateStatistics, model_main.tables().getSorted_table(), model_main.tables().getSortIt_table(),
                        "Sorted -> SortIt");
                removeImageDuplicates(duplicateStatistics, model_main.tables().getSortIt_table(), model_main.tables().getSortIt_table(),
                        "SortIt -> SortIt");
                return null;
            }

        };

        removeImageDuplicatestask.setOnSucceeded(event1 -> {
            TableUtils.updateAllFolderInfos(model_main.tables());
            TableUtils.calculateTableViewsStatistic(model_main.tables());
            TableUtils.cleanTables(model_main.tables());

            Messages.warningText("" + "Duplicated files: " + duplicateStatistics.getFileCounter() + "\nScanned folders: " + duplicateStatistics.getFolderCounter()
                    + "\nSaved space: " + Conversion.convertToSmallerConversion(duplicateStatistics.getFolderSavedSize().get()));

            duplicateStatistics.getDuplicateCounter().set(0);
            duplicateStatistics.getFileCounter().set(0);
            duplicateStatistics.getFolderCounter().set(0);
            duplicateStatistics.getFolderSavedSize().set(0);

            Platform.runLater(() -> removeDuplicates_btn.setDisable(false));

        });
        removeImageDuplicatestask.setOnFailed(event12 -> {
            Messages.warningText("Unable to remove duplicates from tables");
            Platform.runLater(() -> removeDuplicates_btn.setDisable(false));
        });

        removeImageDuplicatestask.setOnCancelled(event13 -> Platform.runLater(() -> removeDuplicates_btn.setDisable(false)));

        Thread removeTableDuplicates_th = new Thread(removeImageDuplicatestask, "Removing table duplicates  thread");
        removeTableDuplicates_th.run();
    }

    @FXML
    private void removeDuplicates_btn_action(ActionEvent event) {
        duplicateStatistics = new DuplicateStatistics();

        Task<Void> removeTableDuplicatestask = new Task<>() {

            @Override
            protected Void call() throws Exception {

                Platform.runLater(() -> removeDuplicates_btn.setDisable(true));
                removeTableDuplicates(duplicateStatistics, model_main.tables().getSorted_table(), model_main.tables().getSorted_table(),
                        "Sorted -> Sorted");
                removeTableDuplicates(duplicateStatistics, model_main.tables().getSorted_table(), model_main.tables().getSortIt_table(),
                        "Sorted -> SortIt");
                removeTableDuplicates(duplicateStatistics, model_main.tables().getSortIt_table(), model_main.tables().getSortIt_table(),
                        "SortIt -> SortIt");
                return null;
            }

        };

        removeTableDuplicatestask.setOnSucceeded(event1 -> {
            TableUtils.updateAllFolderInfos(model_main.tables());
            TableUtils.calculateTableViewsStatistic(model_main.tables());
            TableUtils.cleanTables(model_main.tables());

            Messages.warningText("" + "Duplicated files: " + duplicateStatistics.getFileCounter() + "\nScanned folders: " + duplicateStatistics.getFolderCounter()
                    + "\nSaved space: " + Conversion.convertToSmallerConversion(duplicateStatistics.getFolderSavedSize().get()));

            duplicateStatistics.getDuplicateCounter().set(0);
            duplicateStatistics.getFileCounter().set(0);
            duplicateStatistics.getFolderCounter().set(0);
            duplicateStatistics.getFolderSavedSize().set(0);


            Platform.runLater(() -> removeDuplicates_btn.setDisable(false));

        });
        removeTableDuplicatestask.setOnFailed(event12 -> {
            Messages.warningText("Unable to remove duplicates from tables");
            Platform.runLater(() -> removeDuplicates_btn.setDisable(false));
        });

        removeTableDuplicatestask.setOnCancelled(event13 -> Platform.runLater(() -> removeDuplicates_btn.setDisable(false)));

        Thread removeTableDuplicates_th = new Thread(removeTableDuplicatestask, "Removing table duplicates  thread");
        removeTableDuplicates_th.run();
    }

    @FXML
    private void moveFilesToSortedTable_btn_action(ActionEvent event) {
//		TableUtils.checkTableDuplicates(null, null)
    }

    private void removeImageDuplicates(DuplicateStatistics duplicateStatistics, TableView<FolderInfo> tableSource, TableView<FolderInfo> tableToSearch, String phase) {
        boolean filesRemoved = false;
        Iterator<FolderInfo> folderInfoIT = tableSource.getItems().iterator();
        while (folderInfoIT.hasNext()) {
            FolderInfo folderInfo = folderInfoIT.next();
            duplicateStatistics.getFolderCounter().incrementAndGet();
            List<FileInfo> listToRemove = removeImageDuplicates(duplicateStatistics, tableToSearch, folderInfo);
            if (!listToRemove.isEmpty()) {
                Messages.sprintf("Successfully checked");
                folderInfo.getFileInfoList().removeAll(listToRemove);
                TableUtils.updateFolderInfo(folderInfo);
            }
            if(folderInfo.getFileInfoList().isEmpty()) {
                tableSource.getItems().remove(folderInfo);
            }
        }
        TableUtils.refreshTableContent(model_main.tables().getSorted_table());
        TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
//
//        if (filesRemoved) {
//            Messages.sprintf("Gonna remove some files: ");
//            List<FolderInfo> folderInfoToRemove = new ArrayList<>();
//            Iterator<FolderInfo> foi = tableToSearch.getItems().iterator();
//            while (foi.hasNext()) {
//                FolderInfo folderInfo = foi.next();
//
//                TableUtils.updateFolderInfo(folderInfo);
//
//                if (folderInfo.getFolderFiles() <= 0) {
//                    folderInfoToRemove.add(folderInfo);
//                }
//                TableUtils.refreshTableContent(model_main.tables().getSorted_table());
//                TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
//            }
//            Main.setChanged(true);
//            if (!folderInfoToRemove.isEmpty()) {
//                model_main.tables().getSortIt_table().getItems().removeAll(folderInfoToRemove);
//            }
//        }
    }

    private List<FileInfo> removeImageDuplicates(DuplicateStatistics duplicateStatistics, TableView<FolderInfo> tableToSearch, FolderInfo folderInfo) {
        List<FileInfo> filesToRemove = new ArrayList<>();

        Iterator<FileInfo> list = folderInfo.getFileInfoList().iterator();
        while (list.hasNext()) {
            FileInfo fileInfoToFind = list.next();
            Messages.sprintf("HMmmmm: " + fileInfoToFind.getOrgPath());
            if (!fileInfoToFind.isIgnored() && !fileInfoToFind.isTableDuplicated()) {
                if (fileInfoToFind.getImageDifferenceHash() != 0 && supportedImage(Paths.get(fileInfoToFind.getOrgPath()))) {
                    Messages.sprintf("We found a match!: " + fileInfoToFind.getOrgPath());
                }
            }

        }

        return filesToRemove;
    }

    private void removeTableDuplicates(DuplicateStatistics duplicateStatistics, TableView<FolderInfo> table, TableView<FolderInfo> tableToSearch, String phase) {

        duplicateStatistics.getChangesMadeInFolderInfo().set(false);
        boolean folderNeedsToUpdate = false;
        for (FolderInfo folderInfo : table.getItems()) {
            duplicateStatistics.getFolderCounter().incrementAndGet();
            for (FileInfo fileInfoToFind : folderInfo.getFileInfoList()) {
                if (!fileInfoToFind.isIgnored() && !fileInfoToFind.isTableDuplicated()) {
                    Messages.sprintf(
                            "fileInfoToFind " + fileInfoToFind + " dup? " + fileInfoToFind.isTableDuplicated());
                    boolean duplicated = findDuplicate(duplicateStatistics, fileInfoToFind, tableToSearch);
                    if (duplicated) {
                        folderNeedsToUpdate = true;
                    }
                }
            }
        }
        if (folderNeedsToUpdate) {
            List<FolderInfo> toRemove = new ArrayList<>();
            Iterator<FolderInfo> foi = model_main.tables().getSortIt_table().getItems().iterator();
            while (foi.hasNext()) {
                FolderInfo folderInfo = foi.next();
                TableUtils.updateFolderInfo(folderInfo);
                if (folderInfo.getFolderFiles() <= 0) {
                    toRemove.add(folderInfo);
                }
                TableUtils.refreshTableContent(model_main.tables().getSorted_table());
                TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
            }
            Main.setChanged(true);
            if (!toRemove.isEmpty()) {
                model_main.tables().getSortIt_table().getItems().removeAll(toRemove);
            }
        }
    }

    private boolean findDuplicate(DuplicateStatistics duplicateStatistics, FileInfo fileInfoToFind, TableView<FolderInfo> table) {
        boolean needsUpdate = false;
        for (FolderInfo folderInfo : table.getItems()) {
            if (folderInfo.getFolderFiles() > 0) {
                for (FileInfo fileInfoSearching : folderInfo.getFileInfoList()) {
                    if (!fileInfoSearching.isTableDuplicated() &&
                            fileInfoSearching.getOrgPath() != fileInfoToFind.getOrgPath() &&
                            fileInfoSearching.getSize() == fileInfoToFind.getSize() &&
                            fileInfoSearching.getDate() == fileInfoToFind.getDate()) {
                        fileInfoSearching.setTableDuplicated(true);
                        duplicateStatistics.getDuplicateCounter().incrementAndGet();
                        duplicateStatistics.getFolderSavedSize().addAndGet(fileInfoSearching.getSize());
                        if (!needsUpdate) {
                            needsUpdate = true;
                        }
                        Messages.sprintf("FOUND fileInfoToFind: " + fileInfoToFind
                                + "  fileInfoToFind.isTableDuplicated() "
                                + fileInfoToFind.isTableDuplicated() + " DUPLICATED file: "
                                + fileInfoSearching.getOrgPath() + " fileInfoSearch.isTableDuplicated() "
                                + fileInfoSearching.isTableDuplicated() + " needsUpdate? " + needsUpdate);
                    }
                }
            }
        }
        return needsUpdate;
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
                .add(Main.class.getResource(conf.getThemePath() + "folderChooser.css").toExternalForm());
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

    public void init(Model_main aModel_main) {
        this.model_main = aModel_main;
        sprintf("bottom controller...");
        debug_pref_width.textProperty().bind(model_main.getTable_root_hbox_width());
    }

}
