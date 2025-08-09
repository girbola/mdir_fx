package com.girbola.controllers.main;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.datefixer.DateFixer;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.main.enums.ThemeType;
import com.girbola.controllers.main.options.OptionsComponent;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.DuplicateStatistics;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.SaveTableFileInfos;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import com.girbola.misc.Misc;
import com.girbola.utils.FileInfoUtils;
import common.utils.Conversion;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

public class MenuBarController {

    private final String ERROR = MenuBarController.class.getSimpleName();

    private ModelMain model_main;

    @FXML
    private MenuItem menuItem_tools_options_viewIgnoredList;
    @FXML
    private CheckMenuItem menuItem_tools_themes_dark;
    @FXML
    private CheckMenuItem menuItem_tools_themes_light;
    @FXML
    private CheckMenuItem menuItem_tools_showFullPath;
    @FXML
    private CheckMenuItem menuItem_tools_findImageDuplicates;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem menuItem_file_addFolders;
    @FXML
    private MenuItem menuItem_file_clear;
    @FXML
    private MenuItem menuItem_file_close;
    @FXML
    private MenuItem menuItem_file_import;
    @FXML
    private MenuItem menuItem_file_load;
    @FXML
    private MenuItem menuItem_file_save;
    @FXML
    private MenuItem menuItem_help_about;
    @FXML
    private MenuItem menuItem_help_help;
    @FXML
    private MenuItem menuItem_tools_options;
    @FXML
    private MenuItem menuItem_help_supportUs;
    @FXML
    private MenuItem menuItem_help_update;

    CheckMenuItem[] themeMenuItems = {menuItem_tools_themes_light, menuItem_tools_themes_dark};


    private DuplicateStatistics duplicateStatistics;

    private boolean findDuplicate(DuplicateStatistics duplicateStatistics, FileInfo fileInfoToFind, TableView<FolderInfo> table) {
        boolean needsUpdate = false;
        for (FolderInfo folderInfo : table.getItems()) {
            if (folderInfo.getFolderFiles() > 0) {
                for (FileInfo fileInfoSearching : folderInfo.getFileInfoList()) {
                    if (!fileInfoSearching.isTableDuplicated() && fileInfoSearching.getOrgPath() != fileInfoToFind.getOrgPath() && fileInfoSearching.getSize() == fileInfoToFind.getSize() && fileInfoSearching.getDate() == fileInfoToFind.getDate()) {
                        fileInfoSearching.setTableDuplicated(true);
                        duplicateStatistics.getDuplicateCounter().incrementAndGet();
                        duplicateStatistics.getFolderSavedSize().addAndGet(fileInfoSearching.getSize());
                        if (!needsUpdate) {
                            needsUpdate = true;
                        }
                        Messages.sprintf("FOUND fileInfoToFind: " + fileInfoToFind + "  fileInfoToFind.isTableDuplicated() " + fileInfoToFind.isTableDuplicated() + " DUPLICATED file: " + fileInfoSearching.getOrgPath() + " fileInfoSearch.isTableDuplicated() " + fileInfoSearching.isTableDuplicated() + " needsUpdate? " + needsUpdate);
                    }
                }
            }
        }
        return needsUpdate;
    }

    private void removeTableDuplicates(DuplicateStatistics duplicateStatistics, TableView<FolderInfo> table, TableView<FolderInfo> tableToSearch, String phase) {

        duplicateStatistics.getChangesMadeInFolderInfo().set(false);
        boolean folderNeedsToUpdate = false;
        for (FolderInfo folderInfo : table.getItems()) {
            duplicateStatistics.getFolderCounter().incrementAndGet();
            for (FileInfo fileInfoToFind : folderInfo.getFileInfoList()) {
                if (!fileInfoToFind.isIgnored() && !fileInfoToFind.isTableDuplicated()) {
                    Messages.sprintf("fileInfoToFind " + fileInfoToFind + " dup? " + fileInfoToFind.isTableDuplicated());
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
                FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
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

    @FXML
    private void menuItem_file_addFolders_action(ActionEvent event) {
        Messages.sprintf("menuItem_file_addFolders_action pressed");
        model_main.getMonitorExternalDriveConnectivity().cancel();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/folderscanner/FolderScanner.fxml"), Main.bundle);

        Parent parent = null;
        FolderScannerController folderScannerController = null;
        try {
            parent = loader.load();
            folderScannerController = (FolderScannerController) loader.getController();
        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.errorSmth(ERROR, "Country= " + Main.bundle.getLocale().getCountry() + " location?\n: " + Main.bundle.getLocale(), ex, Misc.getLineNumber(), true);
        }
        Stage fc_stage = new Stage();
//		fc_stage.setWidth(conf.getScreenBounds().getWidth());
//		fc_stage.setHeight(conf.getScreenBounds().getHeight() / 1.3);
        fc_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                model_main.getMonitorExternalDriveConnectivity().restart();
                fc_stage.close();
            }
        });

        Scene fc_scene = new Scene(parent, 800, 400);
        fc_scene.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
        folderScannerController.setStage(fc_stage);
        folderScannerController.setScene(fc_scene);
        folderScannerController.init(model_main);
        fc_stage.setTitle("Select folder to scan for images");
        fc_stage.initModality(Modality.WINDOW_MODAL);
        fc_stage.initOwner(menuBar.getScene().getWindow());
        fc_stage.setScene(fc_scene);

        fc_stage.show();

    }

    private List<FileInfo> removeImageDuplicates(DuplicateStatistics duplicateStatistics, TableView<FolderInfo> tableToSearch, FolderInfo folderInfo) {
        List<FileInfo> filesToRemove = new ArrayList<>();

        Iterator<FolderInfo> items = tableToSearch.getItems().iterator();
        while (items.hasNext()) {
            FolderInfo fo = items.next();
            Iterator<FileInfo> list = folderInfo.getFileInfoList().iterator();
            while (list.hasNext()) {
                FileInfo fileInfoToFind = list.next();
                Messages.sprintf("HMmmmm: " + fileInfoToFind.getOrgPath());
                if (!fileInfoToFind.isIgnored() && !fileInfoToFind.isTableDuplicated()) {

                }

            }
        }
        return filesToRemove;
    }

    private void removeImageDuplicates(DuplicateStatistics duplicateStatistics, TableView<FolderInfo> tableSource, TableView<FolderInfo> tableToSearch, String phase) {
        boolean filesRemoved = false;
        Iterator<FolderInfo> folderInfoIT = tableSource.getItems().iterator();
        while (folderInfoIT.hasNext()) {
            FolderInfo folderInfo = folderInfoIT.next();
            if (folderInfo == null) {
                continue;
            }
            duplicateStatistics.getFolderCounter().incrementAndGet();
            List<FileInfo> listToRemove = removeImageDuplicates(duplicateStatistics, tableToSearch, folderInfo);
            if (!listToRemove.isEmpty()) {
                Messages.sprintf("listToRemove were not empty");
                folderInfo.getFileInfoList().removeAll(listToRemove);
                FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
            }
            if (folderInfo.getFileInfoList().isEmpty()) {
                tableSource.getItems().remove(folderInfo);
            }
        }
        TableUtils.refreshTableContent(model_main.tables().getSorted_table());
        TableUtils.refreshTableContent(model_main.tables().getSortIt_table());

    }

    @FXML
    private void menuItem_tools_findImageDuplicatesAction(ActionEvent event) {
        duplicateStatistics = new DuplicateStatistics();

        Task<Void> removeImageDuplicatestask = new Task<>() {

            @Override
            protected Void call() throws Exception {


                removeImageDuplicates(duplicateStatistics, model_main.tables().getSorted_table(), model_main.tables().getSorted_table(), "Sorted -> Sorted");
                removeImageDuplicates(duplicateStatistics, model_main.tables().getSorted_table(), model_main.tables().getSortIt_table(), "Sorted -> SortIt");
                removeImageDuplicates(duplicateStatistics, model_main.tables().getSortIt_table(), model_main.tables().getSortIt_table(), "SortIt -> SortIt");
                return null;
            }

        };

        removeImageDuplicatestask.setOnSucceeded(event1 -> {
            TableUtils.updateAllFolderInfos(model_main.tables());
            TableUtils.cleanTables(model_main.tables());

            Messages.warningText("" + "Duplicated files: " + duplicateStatistics.getFileCounter() + "\nScanned folders: " + duplicateStatistics.getFolderCounter() + "\nSaved space: " + Conversion.convertToSmallerConversion(duplicateStatistics.getFolderSavedSize().get()));

            duplicateStatistics.getDuplicateCounter().set(0);
            duplicateStatistics.getFileCounter().set(0);
            duplicateStatistics.getFolderCounter().set(0);
            duplicateStatistics.getFolderSavedSize().set(0);


        });
        removeImageDuplicatestask.setOnFailed(event12 -> {
            Messages.warningText("Unable to remove duplicates from tables");
        });

        removeImageDuplicatestask.setOnCancelled(event13 -> {
            Messages.sprintf("remove image duplicates were cancelled");
        });

        Thread removeTableDuplicates_th = new Thread(removeImageDuplicatestask, "Removing table duplicates  thread");
        removeTableDuplicates_th.run();
    }

    @FXML
    private void menuItem_file_clear_action(ActionEvent event) {
        TableUtils.clearTablesContents(model_main.tables());
        Main.setChanged(false);
    }

    @FXML
    private void menuItem_file_close_action(ActionEvent event) {
        model_main.exitProgram();
    }

    @FXML
    private void menuItem_file_import_action(ActionEvent event) {
        // thn tarvitaan folderinfo creator
        // srth;
        Stage stage = (Stage) menuBar.getScene().getWindow();

        DirectoryChooser dc = new DirectoryChooser();
        File file = dc.showDialog(stage);
        if (file == null) {
            return;
        } else {
            FolderInfo folderInfo = new FolderInfo(file.toPath());
            List<FileInfo> list = FileInfoUtils.createFileInfo_list(folderInfo);

            if (list != null) {
                folderInfo.setFileInfoList(list);
                FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
            }

            if (folderInfo.getFileInfoList().isEmpty()) {
                Messages.warningText("noMediaFilesFoundInCurrentDir");
                return;
            } else {
                Task<Void> dateFixer = new DateFixer(Paths.get(folderInfo.getFolderPath()), folderInfo, model_main, true);
                Thread dateFixer_th = new Thread(dateFixer, "dateFixer_th");
                dateFixer_th.start();
                // new ImportImages(model_main.getScene(), folderInfo, model_main, true);
            }
            //
            // ObservableList<Path> obs = FXCollections.observableArrayList();
            // obs.add(file.toPath());
            //
            // Task<List<Path>> createFileList = new SubList(obs);
            // sdv;
            // Thread createFileList_th = new Thread(createFileList, "createFileList_th");
            // sprintf("createFileList_th.getName(): " + createFileList_th.getName());
            // createFileList_th.start();

        }
        Messages.sprintf("menuItem_file_import_action");
        // ImportImages importImages = new ImportImages(model_main.getScene(), null,
        // model_main, true);

    }

    @FXML
    private void menuItem_file_load_action(ActionEvent event) {
        Messages.sprintf("menuItem_file_load_action");
//		boolean load = true;
        if (Main.getChanged()) {
            Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(Main.sceneManager.getWindow(), bundle.getString("changesMadeDataLost"));
            dialog.getDialogPane().getButtonTypes().remove(1);
            Messages.sprintf("dialog changesDialog width: " + dialog.getWidth());
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
                Main.setChanged(false);
                loadTablesContents();
                Main.setChanged(false);
            } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                Messages.sprintf("Cancel_Close pressed. Doing nothing than closing the window");
            }
        } else {
            loadTablesContents();
        }
    }

    private void loadTablesContents() {
        if (Files.exists(Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
            Messages.sprintf("Folderinfo database foudn at appdata path: " + (Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));
            model_main.load();
        }
    }

    @FXML
    private void menuItem_file_save_action(ActionEvent event) {
        sprintf("menuItem_file_save_action");
        SaveTableFileInfos saveFileInfos = new SaveTableFileInfos(model_main, Main.sceneManager.getWindow(), null, true);
        saveFileInfos.readTables();

        Task<Integer> saveTablesToDatabases = new SaveTablesToFolderInfoDatabases(model_main, Main.sceneManager.getWindow(), null, true);

        saveTablesToDatabases.setOnSucceeded(event2 -> {
            Messages.sprintfError("saveTablesToDatabases succeeded");
        });
        saveTablesToDatabases.setOnFailed(event2 -> {
            Messages.sprintfError("saveTablesToDatabases failed");
        });
        saveTablesToDatabases.setOnCancelled(event2 -> {
            Messages.sprintfError("saveTablesToDatabases cancelled");
        });
        Thread thread = new Thread(saveTablesToDatabases, "Saving data MenuBarConctroller Thread");
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    private void menuItem_help_about_action(ActionEvent event) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        Label programName = new Label("Organizer tool for backing up image & video files");
        Label programVersion = new Label(conf.getProgramVersion());
        Label programUserInfo = new Label("girbola9@gmail.com");
        Label programMoreInfo = new Label("NOT FOR PUBLIC DISTRIBUTION");
        Hyperlink programHomePage = new Hyperlink(HTMLClass.programHomePage);

        programHomePage.setOnAction(event1 -> viewWebPage(programHomePage.getText()));

        root.getChildren().addAll(programName, programVersion, programUserInfo, programMoreInfo, programHomePage);

        Dialog dialog = new Dialog();
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(root);
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("About");
        dialog.setResizable(true);

        dialogPane.setHeaderText("About");

        ButtonType buttonTypeYes = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeYes);
        Optional<ButtonType> result = dialog.showAndWait();
        if ((result.isPresent()) && (result.get().getText().equals("OK"))) {
            dialog.close();
        }
    }

    @FXML
    private void menuItem_help_help_action(ActionEvent event) {
        viewWebPage("http://girbola.com/index.html");
    }

    @FXML
    private void menuItem_help_supportUs_action(ActionEvent event) {
        viewWebPage("http://girbola.com/supportUs.html");
    }

    @FXML
    private void menuItem_help_update_action(ActionEvent event) {
        viewWebPage("http://girbola.com/downloads.html");
    }

    @FXML
    private void menuItem_tools_options_action(ActionEvent event) {
        OptionsComponent.openOptions();
    }


    public void init(ModelMain aModel_main) {
        this.model_main = aModel_main;
        sprintf("menuBarController....");
        sprintf("menuItem_tools_showFullPath: " + conf.isShowFullPath());

        menuItem_tools_showFullPath.selectedProperty().bindBidirectional(conf.showFullPathProperty());
        if (conf.getThemePath().endsWith("light/")) {
            switchThemeItemOn("light");
        } else if (conf.getThemePath().endsWith("dark/")) {
            menuItem_tools_themes_dark.setSelected(true);
            menuItem_tools_themes_light.setSelected(false);
        } else {
            sprintf("Cannot find theme:" + conf.getThemePath());
            Messages.errorSmth(ERROR, "Problem by find theme path", null, Misc.getLineNumber(), false);
        }
    }

    private void switchThemeItemOn(String dark) {
        Platform.runLater(() -> {
            for (CheckMenuItem item : themeMenuItems) {
                Messages.sprintf("item.getText(): " + item.getId());
                if (item.getId().contains(ThemeType.DARK.getValue())) {
                    item.setSelected(true);
                } else {
                    item.setSelected(false);
                }
                Messages.sprintf("item.getText(): " + item.getText());
            }
        });
    }

    @FXML
    private void menuItem_tools_options_viewIgnoredList_action(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(Main.class.getResource("fxml/misc/ViewIgnoredList.fxml"), bundle);

            Scene viewIgnored_scene = new Scene(parent);
            // Main.class.getResource(conf.getThemePath() +
            // "dateFixer.css").toExternalForm());

            viewIgnored_scene.getStylesheets().add(Main.class.getResource(Main.conf.getThemePath() + "viewignoredlist.css").toExternalForm());
            Stage viewIgnored_stage = new Stage();
            viewIgnored_stage.setScene(viewIgnored_scene);
            viewIgnored_stage.show();
        } catch (IOException ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
        }

    }

    @FXML
    private void menuItem_tools_themes_dark_action(ActionEvent event) {
        Messages.sprintf("menuItem_tools_themes_dark_action pressed: " + conf.getThemePath());
        Platform.runLater(() -> {
            Main.sceneManager.getScene_main().getStylesheets().clear();
            conf.setCurrentTheme(ThemeType.DARK.getValue());
            switchThemeItemOn(ThemeType.DARK.getValue());
            Main.sceneManager.getScene_main().getStylesheets().add(getClass().getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
            ConfigurationSQLHandler.updateConfiguration();
        });
    }

    @FXML
    private void menuItem_tools_themes_light_action(ActionEvent event) {
//        Main.sceneManager.getScene_main().getStylesheets().clear();
//        conf.setCurrentTheme(light);
//        switchThemeItemOn(light);
//        Main.sceneManager.getScene_main().getStylesheets().add(getClass().getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
//        ConfigurationSQLHandler.updateConfiguration();
    }

    private void viewWebPage(String string) {
        try {
            Desktop.getDesktop().browse(new URL(string).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}