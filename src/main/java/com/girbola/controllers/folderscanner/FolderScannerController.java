package com.girbola.controllers.folderscanner;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.utils.CommonUserFolders;
import common.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import static com.girbola.concurrency.ConcurrencyUtils.initNewSingleExecutionService;
import static com.girbola.controllers.misc.Misc_GUI.autoResizeColumns;
import static com.girbola.messages.Messages.sprintf;

public class FolderScannerController {

    private final String ERROR = FolderScannerController.class.getSimpleName();

    //@formatter:off
    /*
     * @FXML needed! SelectedFoldersController
     */
    @FXML AnchorPane folderScannerMain;
    @FXML SelectedFoldersController selectedFoldersController;
    @FXML private Button addToSelectedFolders_btn;
    @FXML private Button analyzeList_add;
    @FXML private Button analyzeList_remove;
    @FXML private Button list;
    @FXML private ScrollPane analyzeList_scrollPane;
//    @FXML private SplitPane splitPane_drives;
    @FXML private SplitPane splitPane_root;
    @FXML private Label folderSelectorLabel;
    @FXML private TableView<SelectedFolder> homeDefaultsTableView;
    @FXML private TableColumn<SelectedFolder, Boolean> homeDefaults_select_column;
    @FXML private TableColumn<SelectedFolder, String> homeDefaults_path_column;

    @FXML private TreeView<Path> drives_treeView;
    @FXML private VBox analyzeList_vbox;

    //@formatter:on
    private ModelMain modelMain;
    private ModelFolderScanner model_folderScanner = new ModelFolderScanner();
//    private List<SelectedFolder> selectedFolderScanner = new ArrayList<>();¸

    private Scene folderScannerController_scene;
//    private Stage folderScannerController_stage;


    private CheckBoxTreeItem<Path> drives_rootItem;

    @FXML
    private void addToSelectedFolders_btn_action(ActionEvent event) {
        sprintf("addToSelectedFolders_btn_action...");

//        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().addAll(selectedFolderScanner);

        for (Path path : model_folderScanner.getSelectedDrivesFoldersListObs()) {
            sprintf("Path is: " + path);
            if (Files.exists(path)) {
                if (!selectedFolderHasValue(modelMain.getSelectedFolders().getSelectedFolderScanner_obs(), path)) {
                    if (!hasTableSelectedFolderPath(modelMain.tables(), path)) {
                        //TODO Check selectedfolder selected. It might not work correctly?
                        modelMain.getSelectedFolders().getSelectedFolderScanner_obs().add(SelectedFolder.create(path.toString(), true, true, FileUtils.getHasMedia(path.toString()), false));
                    }
                }
            }
        }

        for (TreeItem<Path> fil : drives_rootItem.getChildren()) {
            if (!fil.getChildren().isEmpty()) {
                for (TreeItem<Path> c_fil : fil.getChildren()) {
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
    private void list_action(ActionEvent event) {
        Messages.warningText("list_action NOT READY YET!");
    }

    public void exit() {
        model_folderScanner.getScanDrives().stop();
        model_folderScanner.drive().saveList();
//        folderScannerController_stage.close();
    }

//
//    final EventHandler<KeyEvent> eventFilter = new EventHandler<KeyEvent>() {
//
//        @Override
//        public void handle(KeyEvent event) {
//            if (event.getCode().equals(KeyCode.ESCAPE)) {
//                exit();
//            }
//        }
//    };

//    public void setScene(Scene folderScannerController_scene) {
//        this.folderScannerController_scene = folderScannerController_scene;
//    }

//    public void setStage(Stage folderScannerController_stage) {
//        this.folderScannerController_stage = folderScannerController_stage;
//        this.folderScannerController_stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                exit();
//            }
//        });
//    }


    Callback<TableColumn<SelectedFolder, Boolean>, TableCell<SelectedFolder, Boolean>> selectedFoldersCellFactory = p -> new CheckBoxSelectFolderTableCell(modelMain, model_folderScanner);

    public void initFolderScanner() {
        Main.setProcessCancelled(false);

        initNewSingleExecutionService();
//        CustomFolderTreeCellFactory customFolderTreeCellFactory = new CustomFolderTreeCellFactory(modelMain, model_folderScanner);
        drives_rootItem = new CheckBoxTreeItem<>();
        drives_rootItem.setIndependent(true);
        drives_rootItem.setExpanded(true);
        drives_treeView.setCellFactory(tv -> new CustomFolderTreeCell(modelMain, model_folderScanner));
//        drives_treeView.setCellFactory(tv -> new CheckBoxTreeCell<Path>() {
//            @Override
//            public void updateItem(Path item, boolean empty) {
//                super.updateItem(item, empty);
//
//                if (empty || item == null) {
//                    Messages.sprintf("-----drives_treeView setCellFactory null: " + item + " boolean is: " + empty);
//                    setText(null);
//                } else {
//                    Messages.sprintf("-----drives_treeView setCellFactory: " + item + " boolean is: " + empty);
//                    String name = item.getFileName() == null ? item.toString() : item.getFileName().toString();
//                    setText(name);
//                }
//            }
//        });

//        drives_treeView.setCellFactory(CheckBoxTreeCell.forTreeView());

        drives_treeView.setRoot(drives_rootItem);
        drives_treeView.setShowRoot(false);

        // Selection propagation logic (parent <-> children)
        //SelectionPropagation.installSelectionPropagation(modelMain);

        modelMain.getFolderSelectionService().installSelectionPropagation();

        model_folderScanner.init(modelMain, drives_rootItem);
        selectedFoldersController.init(modelMain, model_folderScanner);

        //folderScannerController_stage.addEventFilter(KeyEvent.KEY_PRESSED, eventFilter);
        selectedFoldersController.start();

        homeDefaults_select_column.setCellFactory(selectedFoldersCellFactory);
        homeDefaults_select_column.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isSelected()));

        homeDefaults_path_column.setCellValueFactory((TableColumn.CellDataFeatures<SelectedFolder, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolder()));

        Platform.runLater(() -> {
            homeDefaultsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
            homeDefaultsTableView.setTableMenuButtonVisible(true);
        });

        Map<CommonUserFolders.Kind, Path> resolve = CommonUserFolders.resolve();
        for (Path commonPath : resolve.values()) {
            Messages.sprintf("commonPath: " + commonPath);
            if (Files.exists(commonPath)) {
                Messages.sprintf("Adding commonPath to homeDefaultsTableView: " + commonPath);
//                homeDefaultsTableView.getItems().add(commonPath);
                SelectedFolder selectedHomeFolder = existsInSelectedFolderScannerObs(commonPath);
                if (selectedHomeFolder != null) {
                    Messages.sprintf("Common path exists in selectedFolderScanner_obs: " + commonPath);
                    modelMain.getSelectedFolders().getHomeDefaultsFolders_obs().add(SelectedFolder.create(commonPath.toString(), selectedHomeFolder.isSelected(), true, FileUtils.getHasMedia(commonPath.toString()), false));
                } else {
                    selectedHomeFolder = SelectedFolder.create(commonPath.toString(), false, true, FileUtils.getHasMedia(commonPath.toString()), false);
                    modelMain.getSelectedFolders().getHomeDefaultsFolders_obs().add(selectedHomeFolder);
                }
            }
        }
//        homeDefaultsTableView.setColumnResizePolicy(tableView -> true);

        homeDefaultsTableView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            Messages.sprintf("homeDefaultsTableView.itemsProperty::: " + newValue.size());
            //autoResizeColumns(homeDefaultsTableView, 30);
            Platform.runLater(() -> {
                homeDefaultsTableView.getColumns().forEach(column -> {

                    column.setPrefWidth(Region.USE_COMPUTED_SIZE);
                    autoResizeColumns(homeDefaultsTableView, 30);
                });
            });
        });

        Platform.runLater(() -> {
            homeDefaultsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
            homeDefaultsTableView.setItems(this.modelMain.getSelectedFolders().getHomeDefaultsFolders_obs());
            homeDefaultsTableView.applyCss();
            homeDefaultsTableView.layout();
            homeDefaultsTableView.refresh();
            homeDefaultsTableView.requestLayout();

        });
        Messages.sprintf("homeDefaultsTableView.getItems().size(); " + homeDefaultsTableView.getColumnResizePolicy() + " " + homeDefaultsTableView.getItems().size());
        homeDefaultsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Messages.sprintf("Selected item: " + newSelection.getFolder());
                modelMain.getFolderSelectionService().focusFolder(newSelection);
            }
        });
        homeDefaultsTableView.itemsProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (homeDefaultsTableView != null) {

                    Messages.sprintf("homeDefaultsTableView.WIDTH:: " + homeDefaultsTableView.getItems().size());
                    Platform.runLater(() -> {
                        homeDefaultsTableView.applyCss();
                        homeDefaultsTableView.layout();
                        homeDefaultsTableView.refresh();
                        homeDefaultsTableView.requestLayout();
                    });
                }
            }
        });

        folderScannerMain.widthProperty().addListener((obs, oldVal, newVal) -> {
            Messages.sprintf("folderScannerMain.getWidth(); " + folderScannerMain.getWidth());
            if (homeDefaultsTableView == null) {
                Messages.sprintfError("homeDefaultsTableView is null!");
                return;
            }
            Platform.runLater(() -> {
                updatePathColumnWidth(homeDefaultsTableView);
            });
        });

    }

    public TreeView<Path> getDrivesTreeView() {
        if (drives_treeView == null) {
            Messages.sprintfError("drivesTreeView is null!");
        }
        return drives_treeView;
    }

    private void updatePathColumnWidth(TableView<?> table) {
        //Set the right policy
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
        table.getColumns().stream().forEach((column) -> {
            //Minimal width = columnheader
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                //cell must not be empty
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    //remember new max-width
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            //set the new max-widht with some extra space
            column.setPrefWidth(max + 10.0d);
        });
    }

    SelectedFolder existsInSelectedFolderScannerObs(Path commonPath) {
        for (SelectedFolder sf : this.modelMain.getSelectedFolders().getSelectedFolderScanner_obs()) {
            if (sf.getFolder().equals(commonPath.toString())) {
                return sf;
            }
        }
        return null;
    }

    public CheckBoxTreeItem<Path> getDrives_rootItem() {
        return drives_rootItem;
    }

    public void init(ModelMain aModel_main) {
        this.modelMain = aModel_main;
        model_folderScanner = new ModelFolderScanner();
    }
}
