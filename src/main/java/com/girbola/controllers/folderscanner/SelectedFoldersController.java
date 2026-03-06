
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.dialogs.Dialogs;
import com.girbola.messages.Messages;
import com.girbola.sql.SelectedFolderInfoSQL;
import java.io.File;
import java.sql.Connection;
import java.util.Iterator;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;

public class SelectedFoldersController {
    private final String ERROR = SelectedFoldersController.class.getName();

    /*private ScheduledService<Void> scanner;*/

    private ModelMain model_main;
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

    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(model_main, model_folderScanner);
    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> removeRowCellFactory = p -> new CheckBoxRemoveRowTableCell(model_main, model_folderScanner);

    @FXML
    private void selectedFolders_ok_action(ActionEvent event) {
        Messages.sprintf("selectedFolders_ok_action pressed");
        model_folderScanner.getScanDrives().stop();
        model_main.getMonitorExternalDriveConnectivity().cancel();

        SelectedFolderInfoSQL.saveSelectedFoldersToConfigDb(model_main);
        model_main.getSelectedFolders().getSelectedFolderScanner_obs().forEach(selectedFolder -> {
            Messages.sprintf("Selected folder to scan: " + selectedFolder.getFolder() + " isSelected: " + selectedFolder.isSelected());
        });
        for(SelectedFolder selectedFolder : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if(!selectedFolder.isSelected()) {
                Iterator<FolderInfo> items = model_main.tables().getSorted_table().getItems().iterator();
                while(items.hasNext()) {
                    FolderInfo folderInfo = items.next();
                    if(folderInfo.getSelectedFolderParentPath().equals(selectedFolder.getFolder())) {
                        items.remove();
                    }
                }
            }
        }

        for(SelectedFolder selectedFolder : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if(!selectedFolder.isSelected()) {
                Iterator<FolderInfo> items = model_main.tables().getSortIt_table().getItems().iterator();
                while(items.hasNext()) {
                    FolderInfo folderInfo = items.next();
                    if(folderInfo.getSelectedFolderParentPath().equals(selectedFolder.getFolder())) {
                        items.remove();
                    }
                }
            }
        }


        model_main.populate().populateTablesFolderScannerList(Main.sceneManager.getWindow());
        SelectedFolderInfoSQL.saveSelectedFoldersToConfigDb(model_main);
        Stage stage = (Stage) selectedFolders_ok_btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void selectedFolders_cancel_action(ActionEvent event) {
        sprintf("selectedFolders_cancel_action  pressed");
        Stage stage = (Stage) selectedFolders_cancel_btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void selectedFolders_select_folder_action(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(System.getProperty("user.home")));
        dc.setTitle(Main.bundle.getString("selectFolderForScanning"));
        File folder = dc.showDialog(selectedFolders_select_folder.getScene().getWindow());
        Messages.sprintf("##########Selected folder: " + folder);

//        model_main.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
        if (Main.conf.getWorkDir().contains(folder.toString())) {
            Messages.warningText(Main.bundle.getString("workDirConflict"));
            return;
        }

        if (folder != null) {
            int foldersAdded = model_main.getSelectedFolders().getSelectedFolderScanner_obs().size();

            if (!model_main.getSelectedFolders().getSelectedFolderScanner_obs().isEmpty()) {
                Messages.sprintf("model_main.getSelectedFolders():::::::::: " + model_main.getSelectedFolders().getSelectedFolderScanner_obs().size());
                for (SelectedFolder selectedFolder : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
                    Messages.sprintf("Selected folder in the list: " + selectedFolder.getFolder());
                    if (!selectedFolder.getFolder().equals(folder.getAbsolutePath())) {
                        Messages.sprintf("Adding folder: " + folder.getAbsolutePath());
                    }
                }
                model_main.getSelectedFolders().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
            }
            Messages.sprintf("foldersAdded: " + foldersAdded + "  vs size: " + model_main.getSelectedFolders().getSelectedFolderScanner_obs().size());
            if (foldersAdded != model_main.getSelectedFolders().getSelectedFolderScanner_obs().size() || model_main.getSelectedFolders().getSelectedFolderScanner_obs().size() == 0) {

                model_main.getSelectedFolders().getSelectedFolderScanner_obs().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
//                model_main.getSelectedFolders().add(new SelectedFolder(true, true, folder.getAbsolutePath(), true));
                SelectedFolderInfoSQL.saveSelectedFoldersToConfigDb(model_main);
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

        Iterator<SelectedFolder> selectedFolderScannerObs = model_main.getSelectedFolders().getSelectedFolderScanner_obs().iterator();
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
            SelectedFolderInfoSQL.removeFromTable(selectedItems);
        } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
            SelectedFolderInfoSQL.removeFromTable(selectedItems);
            SelectedFolderInfoSQL.clearSelectedFolders(model_main);
            table.getItems().removeAll(selectedItems);
            table.getSelectionModel().clearSelection();
        } else {
            Messages.sprintf("RemoveFromTable result: " + result.get().getButtonData());
            return;
        }

    }

    public void init(ModelMain aModel_main, ModelFolderScanner aModel_folderScanner) {
        this.model_main = aModel_main;
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

        selectedFolder_TableView.setItems(this.model_main.getSelectedFolders().getSelectedFolderScanner_obs());
        Messages.sprintf("getFolderScanner lldlflfl" + this.model_main.getSelectedFolders().getSelectedFolderScanner_obs().size());


      /*  scanner = new ScheduledService<Void>() {

            @Override
            protected Task createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        for (SelectedFolder sf : model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
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

}
