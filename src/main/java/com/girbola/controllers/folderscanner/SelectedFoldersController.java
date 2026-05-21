
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.dialogs.Dialogs;
import com.girbola.messages.Messages;
import com.girbola.persistence.selectedfolderinfo.SelectedFolderInfoDao;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;

public class SelectedFoldersController {
    private final String ERROR = SelectedFoldersController.class.getName();

    /*private ScheduledService<Void> scanner;*/

    private ModelMain modelMain;
    private ModelFolderScanner model_folderScanner;

    //@formatter:off
    @FXML private TableColumn<SelectedFolder, Boolean> folder_selected_col;
    @FXML private TableColumn<SelectedFolder, String> folder_col;
    @FXML private TableColumn<SelectedFolder, Boolean> folder_connected_col;
    @FXML private TableColumn<SelectedFolder, Boolean> hasMedia_col;
    @FXML private TableColumn<SelectedFolder, Boolean> remove_row_col;

    @FXML private Button selectedFolders_ok_btn;
    @FXML private Button selectedFolders_cancel_btn;
    @FXML private Button selectedFolders_select_folder;
    @FXML private TableView<SelectedFolder> selectedFolder_TableView;
    //@formatter:on
    private List<SelectedFolder> selectedFolderScannerOriginal = new ArrayList<>();

    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(modelMain.getSelectedFolders().getSelectedFolderScannerOriginal(), model_folderScanner);
    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> removeRowCellFactory = p -> new CheckBoxRemoveRowTableCell(modelMain, model_folderScanner);


    @FXML
    private void selectedFolders_ok_action(ActionEvent event) {
        Messages.sprintf("selectedFolders_ok_action pressed");

        modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain

        model_folderScanner.getScanDrives().stop();
        modelMain.getMonitorExternalDriveConnectivity().cancel();

        modelMain.getSelectedFolders().restore();

//        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().setAll(selectedFolderScannerOriginal);
//        SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);

        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().forEach(selectedFolder -> {
            Messages.sprintf("Selected folder to scan: " + selectedFolder.getFolder() + " isSelected: " + selectedFolder.isSelected());
        });

        boolean removeNotSelectedFromTables = TableUtils.removeNotSelectedFromTables(modelMain);

        if (modelMain.tables().getSorted_table().getItems().isEmpty() && modelMain.tables().getSorted_table().getItems().isEmpty()) {
            Messages.warningText(bundle.getString("noFoldersSelected"));
            return;
        }

        List<Path> selectedFolders = new ArrayList<>();
        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(), sf.getFolder()) && sf.isSelected()) {
                if (sf.isConnected() && sf.isSelected()) {
                    boolean selectedFolderExists = SelectedFolderUtils.tableHasFolder(modelMain.tables(), Paths.get(sf.getFolder()));
                    if (!selectedFolderExists) {
                        selectedFolders.add(Paths.get(sf.getFolder()));
                        sprintf("!selectedFolderExists Path is: " + sf.getFolder() + " isConnected: " + sf.isConnected());
                    }
                }
        /*
            Task<Set<Path>> task = }
         */
        for (SelectedFolder sf : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (!hasInIgnoredListMain(Main.conf.getIgnoredFoldersScanList(),
                    sf.getFolder()) &&
                    sf.isSelected() &&
                    sf.isConnected() &&
                    SelectedFolderUtils.tableHasFolder(model_main.tables(), Paths.get(sf.getFolder()))) {
                selectedFolders.add(Paths.get(sf.getFolder()));
                sprintf("!selectedFolderExists Path is: " + sf.getFolder() + " isConnected: " + sf.isConnected());
            } else {
                Messages.sprintf("##### FOLDER IGNORED!!!!: " + sf.getFolder());

                Iterator tableIteratorSortit = modelMain.tables().getSortIt_table().getItems().iterator();
                Iterator tableIteratorSorted = modelMain.tables().getSorted_table().getItems().iterator();

                while (tableIteratorSortit.hasNext()) {
                    FolderInfo folderInfo = (FolderInfo) tableIteratorSortit.next();
                    if (folderInfo.getFolderPath().startsWith(sf.getFolder())) {
                        tableIteratorSortit.remove();
                    }
                }
                while (tableIteratorSorted.hasNext()) {
                    FolderInfo folderInfo = (FolderInfo) tableIteratorSorted.next();
                    if (folderInfo.getFolderPath().startsWith(sf.getFolder())) {
                        tableIteratorSorted.remove();
                    }
                }
            }
        }

//        modelMain.populate().populateTablesFolderScannerList(Main.sceneManager.getWindow());
        SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);


//        Stage stage = (Stage) selectedFolders_ok_btn.getScene().getWindow();
//        stage.close();
    }

    @FXML
    private void selectedFolders_cancel_action(ActionEvent event) {
        sprintf("selectedFolders_cancel_action  pressed");

//        SelectionPropagation.syncTreeFromModel(modelMain);

        modelMain.getSelectedFolders().restore();

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScannerOriginal()) {
            if (sf.isSelected()) {
                Messages.sprintf("111##########getSelectedFolderScannerOriginal folder: " + sf.getFolder() + " is selected ");
            }
        }

        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (sf.isSelected()) {
                Messages.sprintf("222##########getSelectedFolderScanner_obs folder: " + sf.getFolder() + " is selected ");
            }
        }

        //modelMain.getSelectedFolders().getSelectedFolderScanner_obs().setAll(selectedFolderScannerOriginal);

//        boolean loadSelectedFolders = SelectedFolderInfoDao.loadSelectedFolders(modelMain);
//        if (!loadSelectedFolders) {
//            Messages.warningText(bundle.getString("errorLoadingSelectedFolders"));
//        }
//
//        selectedFolder_TableView.setItems(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
//        SelectionPropagation.syncTreeFromModel(modelMain);
      //  modelMain.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
//        selectedFolder_TableView.setItems(model_main.getSelectedFolders().getSelectedFolderScanner_obs());
//        SelectionPropagation.syncTreeFromModel(model_main);
        //  model_main.getTabPaneMain().getSelectionModel().select(0); // Selecting tabMain
    }

    @FXML
    private void selectedFolders_select_folder_action(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        dc.setTitle(Main.bundle.getString("selectFolderForScanning"));
        File folder = dc.showDialog(selectedFolders_select_folder.getScene().getWindow());
        Messages.sprintf("##########Selected folder: " + folder);

//        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
        if (Main.conf.getWorkDir().contains(folder.toString())) {
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            return;
        }

        if (folder != null) {
            int foldersAdded = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size();

            if (!modelMain.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
                Messages.sprintf("modelMain.getSelectedFolders():::::::::: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
                for (SelectedFolder selectedFolder : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Messages.sprintf("Selected folder in the list: " + selectedFolder.getFolder());
                    if (!selectedFolder.getFolder().equals(folder.getAbsolutePath())) {
                        Messages.sprintf("Adding folder: " + folder.getAbsolutePath());
                    }
                }
                modelMain.getSelectedFolders().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
            }
            Messages.sprintf("foldersAdded: " + foldersAdded + "  vs size: " + modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());
            if (foldersAdded != modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() || modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size() == 0) {

                modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
//                modelMain.getSelectedFolders().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
                SelectedFolderInfoDao.saveSelectedFoldersToConfigDb(modelMain);
            }

        } else {
            Messages.sprintfError("Folder is null");
            Messages.warningText(Main.bundle.getString("folderNotFound"));
        }
    }

    public void setDeleteKeyPressed() {
        selectedFolder_TableView.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                removeFromTable(selectedFolder_TableView);
            }
        });
    }

    private void removeFromTable(TableView<SelectedFolder> table) {
        //TODO Finish removeFromTable
        Messages.warningText("RemoveFromTable option is NOT READY");

        Iterator<SelectedFolder> selectedFolderScannerObs = modelMain.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
        while (selectedFolderScannerObs.hasNext()) {
            SelectedFolder selectedFolder = selectedFolderScannerObs.next();
            Messages.sprintf("RemoveFromTable selectedFolder: " + selectedFolder.getFolder());
        }
        Connection connection = null;

        ObservableList<SelectedFolder> selectedItems = table.getSelectionModel().getSelectedItems();

        for (SelectedFolder selectedItem : selectedItems) {
            Messages.sprintf("RemoveFromTable selectedItem: " + selectedItem.getFolder());
        }

        Dialog<ButtonType> changesDialog = Dialogs.createDialog_YesNo(Main.sceneManager.getScene_dateFixer().getWindow(), bundle.getString("removeAsWellFromTables"));

        Optional<ButtonType> result = changesDialog.showAndWait();
        if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            SelectedFolderInfoDao.removeFromTable(selectedItems);
        } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
            SelectedFolderInfoDao.removeFromTable(selectedItems);
            SelectedFolderInfoDao.clearSelectedFolders(modelMain);
            table.getItems().removeAll(selectedItems);
            table.getSelectionModel().clearSelection();
        } else {
            Messages.sprintf("RemoveFromTable result: " + result.get().getButtonData());
            return;
        }

    }

    public void init(ModelMain aModel_main, ModelFolderScanner aModel_folderScanner) {
        this.modelMain = aModel_main;
        this.model_folderScanner = aModel_folderScanner;

        selectedFolder_TableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //model_folderScanner.setDeleteKeyPressed(selectedFolder_TableView);
        selectedFolder_TableView.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                removeFromTable(selectedFolder_TableView);
            }
        });

        folder_selected_col.setCellFactory(selectedFoldersCellFactory);
        folder_selected_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isSelected()));

        remove_row_col.setCellFactory(removeRowCellFactory);
        remove_row_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(false));


        folder_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolder()));

        folder_connected_col.setCellFactory(connected);
        folder_connected_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isConnected()));

        hasMedia_col.setCellFactory(hasMediaFiles);
        hasMedia_col.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isMedia()));

        selectedFolder_TableView.setItems(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());
        Messages.sprintf("getFolderScanner lldlflfl" + this.modelMain.getSelectedFolders().getSelectedFolderScanner_obs().size());

        selectedFolderScannerOriginal.addAll(modelMain.getSelectedFolders().getSelectedFolderScanner_obs());

      /*  scanner = new ScheduledService<Void>() {

            @Override
            protected Task createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        for (SelectedFolder sf : modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
                            sf.setConnected(Files.exists(Paths.get(sf.getFolder())));
                            Messages.sprintf("SelectedFolder: " + sf.getFolder() + " isConnected?: " + sf.isConnected());
                        }
                        return null;
                    }
                };
            }
        };

        scanner.setPeriod(Duration.seconds(10));*/
    }

    public Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> hasMediaFiles = new Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>>() {
        @Override
        public TableCell<SelectedFolder, Boolean> call(TableColumn<SelectedFolder, Boolean> selectedFolderBooleanTableColumn) {
            return new TableCell_Media();
        }
    };

    public Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> connected = new Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>>() {
        @Override
        public TableCell<SelectedFolder, Boolean> call(TableColumn<SelectedFolder, Boolean> selectedFolderBooleanTableColumn) {
            return new com.girbola.controllers.folderscanner.TableCell_Connected();
        }
    };


//    public Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> hasMedia_tableCell =
//            new Callback<SelectedFolder, TableCell<SelectedFolder, Boolean>>() {
//                @Override
//                public TableCell<SelectedFolder, Boolean> call(Object o) {
//                    return new TableCell_Media();
//                }
//            };


    public void start() {
//        this.scanner.start();
    }

    public void restart() {
//        this.scanner.restart();
    }

    public void stop() {
//        this.scanner.cancel();
    }

    private boolean hasInIgnoredListMain(ObservableList<Path> ignoredList, String path) {
        for (Path ignored : ignoredList) {
            if (ignored.toString().equals(path)) {
                return true;
            }
        }
        return false;
    }
}
