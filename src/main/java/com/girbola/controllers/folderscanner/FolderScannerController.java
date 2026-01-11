
package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.utils.CommonUserFolders;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.girbola.concurrency.ConcurrencyUtils.initNewSingleExecutionService;
import static com.girbola.messages.Messages.sprintf;

public class FolderScannerController {

    private final String ERROR = FolderScannerController.class.getSimpleName();

    //@formatter:off
    /*
     * @FXML needed! SelectedFoldersController
     */
    @FXML SelectedFoldersController selectedFoldersController;
    @FXML private Button addToSelectedFolders_btn;
    @FXML private Button analyzeList_add;
    @FXML private Button analyzeList_remove;
    @FXML private Button list;
    @FXML private ScrollPane analyzeList_scrollPane;
    @FXML private SplitPane splitPane_drives;
    @FXML private SplitPane splitPane_root;

    @FXML private TableView<SelectedFolder> homeDefaultsTableView;
    @FXML private TableColumn<SelectedFolder, Boolean> homeDefaults_select_column;
    @FXML private TableColumn<SelectedFolder, String> homeDefaults_path_column;

    @FXML private TreeView<File> drives_treeView;
    @FXML private VBox analyzeList_vbox;

    private ModelMain model_main;
    private ModelFolderScanner model_folderScanner = new ModelFolderScanner();

    private Scene folderScannerController_scene;
    private Stage folderScannerController_stage;

    private CheckBoxTreeItem<File> drives_rootItem;

    @FXML
    private void addToSelectedFolders_btn_action(ActionEvent event) {
        sprintf("addToSelectedFolders_btn_action...");
        for (Path path : model_folderScanner.getSelectedDrivesFoldersListObs()) {
            sprintf("Path is: " + path);
            if (Files.exists(path)) {
                if (!selectedFolderHasValue(this.model_main.getSelectedFolders().getSelectedFolderScanner_obs(),
                        path)) {
                    if (!hasTableSelectedFolderPath(model_main.tables(), path)) {
                        //TODO Check selectedfolder selected. It might not work correctly?
                        this.model_main.getSelectedFolders().getSelectedFolderScanner_obs()
                                .add(new SelectedFolder(true, true, path.toString(),true));
                    }
                }
            }
        }

        for (TreeItem<File> fil : drives_rootItem.getChildren()) {
            if (!fil.getChildren().isEmpty()) {
                for (TreeItem<File> c_fil : fil.getChildren()) {
                    Messages.sprintf("c_fil.getValue(); " + c_fil);
                }
            }
        }
    }

    private boolean hasTableSelectedFolderPath(Tables tables, Path toSearchPath) {
        return checkFolderInfoHasFolder(tables, toSearchPath.toString(), toSearchPath);
    }

    static boolean checkFolderInfoHasFolder(Tables tables, String string, Path toSearchPath) {
        for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
            if (folderInfo.getFolderPath().equals(string)) {
                return true;
            }
        }

        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            if (folderInfo.getFolderPath().equals(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean selectedFolderHasValue(ObservableList<SelectedFolder> selectedFolderScanner_list, Path path) {
        for (SelectedFolder sf : selectedFolderScanner_list) {
            if (Paths.get(sf.getFolder()).equals(path)) {
                return true;
            }
        }

        return false;
    }

    @FXML
    private void analyzeList_add_action(ActionEvent event) {

    }

    @FXML
    private void analyzeList_remove_action(ActionEvent event) {
    }

    @FXML
    private void list_action(ActionEvent event) {
    }

    public void exit() {
        model_folderScanner.getScanDrives().stop();
        model_folderScanner.drive().saveList();
        folderScannerController_stage.close();
    }

    final EventHandler<KeyEvent> eventFilter = new EventHandler<KeyEvent>() {

        @Override
        public void handle(KeyEvent event) {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                exit();
            }
        }
    };

    public void setScene(Scene folderScannerController_scene) {
        this.folderScannerController_scene = folderScannerController_scene;
    }

    public void setStage(Stage folderScannerController_stage) {
        this.folderScannerController_stage = folderScannerController_stage;
        this.folderScannerController_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                exit();
            }
        });
    }
    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(model_main, model_folderScanner);

    public void init(ModelMain aModel_main) {
        Main.setProcessCancelled(false);

        this.model_main = aModel_main;
        initNewSingleExecutionService();

        drives_rootItem = new CheckBoxTreeItem<>();
        drives_rootItem.setExpanded(true);
        drives_treeView.setCellFactory(CheckBoxTreeCell.forTreeView());

        drives_treeView.setRoot(drives_rootItem);
        drives_treeView.setShowRoot(false);

        model_folderScanner.init(model_main, drives_rootItem);
        selectedFoldersController.init(model_main, model_folderScanner);
        folderScannerController_stage.addEventFilter(KeyEvent.KEY_PRESSED, eventFilter);
        selectedFoldersController.start();

        homeDefaults_select_column.setCellFactory(selectedFoldersCellFactory);
        homeDefaults_select_column.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isSelected()));

        homeDefaults_path_column.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolder()));

        Map<CommonUserFolders.Kind, Path> resolve = CommonUserFolders.resolve();
        for(Path commonPath : resolve.values()) {
//            Messages.sprintf("commonPath: " + commonPath);
            if (Files.exists(commonPath)) {
                Messages.sprintf("Adding commonPath to homeDefaultsTableView: " + commonPath);
//                homeDefaultsTableView.getItems().add(commonPath);
                SelectedFolder selectedHomeFolder = existsInSelectedFolderScannerObs(commonPath);
                if (selectedHomeFolder != null) {
                    Messages.sprintf("Common path exists in selectedFolderScanner_obs: " + commonPath);
                    this.model_main.getSelectedFolders().getHomeDefaultsFolders_obs().add(new SelectedFolder(selectedHomeFolder.isSelected(), true, commonPath.toString(), false));
                } else {
                    selectedHomeFolder = new SelectedFolder(false, true, commonPath.toString(), false);
                    this.model_main.getSelectedFolders().getHomeDefaultsFolders_obs().add(selectedHomeFolder);
                }
            }
        }

        Platform.runLater(() -> {
            homeDefaultsTableView.setItems(this.model_main.getSelectedFolders().getHomeDefaultsFolders_obs());
        });

        homeDefaultsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Messages.sprintf("Selected item: " + newSelection.getFolder());
            }
        });
    }

    SelectedFolder existsInSelectedFolderScannerObs(Path commonPath) {
        for (SelectedFolder sf : this.model_main.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (sf.getFolder().equals(commonPath.toString())) {
                return sf;
            }
        }
        return null;
    }
}
