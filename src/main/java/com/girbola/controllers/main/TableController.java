/*

 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.main.collect.Collect_DialogController;
import com.girbola.fxml.main.collect.Model_CollectDialog;
import com.girbola.fxml.main.merge.MergeDialogController;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.Conversion;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

public class TableController {

    private final String ERROR = TableController.class.getSimpleName();

    private Image show_im;
    private Model_CollectDialog model_CollectDialog;
    private Model_main model_main;
    private ObservableList<FolderInfo> data_obs = FXCollections.observableArrayList();
    private SimpleBooleanProperty showTable = new SimpleBooleanProperty(true);
    private SimpleIntegerProperty allFilesTotal_obs = new SimpleIntegerProperty(0);
    private String tableType;
    private Window owner;

    // @formatter:off
	@FXML private AnchorPane table_RootPane;
	@FXML private AnchorPane tables_rootPane;
	@FXML private Button select_all_btn;
	@FXML private Button select_bad_btn;
	@FXML private Button select_good_btn;
	@FXML private Button select_invert_btn;
	@FXML private Button select_none_btn;
	@FXML private Button updateFolderInfo_btn;
	@FXML private FlowPane tableInformation_flowpane;
	@FXML private FlowPane topMenuButtonFlowPane;
	@FXML private HBox buttons_hbox;
	@FXML private HBox showHideButton_hbox;
	@FXML private HBox tableInformation_hbox;
	@FXML private HBox tables_parent;
	@FXML private ImageView hide_btn_iv;
	@FXML private Label allFilesCopied_lbl;
	@FXML private Label allFilesTotal_lbl;
	@FXML private MenuItem checkChanges_mi;
	@FXML private MenuItem mergeMove_MenuItem;
	@FXML private MenuItem reload_all_mi;
	@FXML private MenuItem select_dateDifference_btn;
	@FXML private TableColumn<FolderInfo, Boolean> connected_col;
	@FXML private TableColumn<FolderInfo, Double> dateDifference_ratio_col;
	@FXML private TableColumn<FolderInfo, Integer> badFiles_col;
	@FXML private TableColumn<FolderInfo, Integer> copied_col;
	@FXML private TableColumn<FolderInfo, Integer> folderFiles_col;
	@FXML private TableColumn<FolderInfo, Integer> image_col;
	@FXML private TableColumn<FolderInfo, Integer> media_col;
	@FXML private TableColumn<FolderInfo, Integer> raw_col;
	@FXML private TableColumn<FolderInfo, Integer> status_col;
	@FXML private TableColumn<FolderInfo, Integer> suggested_col;
	@FXML private TableColumn<FolderInfo, Integer> video_col;
	@FXML private TableColumn<FolderInfo, Long> size_col;
	@FXML private TableColumn<FolderInfo, String> dateFix_col;
	@FXML private TableColumn<FolderInfo, String> fullPath_col;
	@FXML private TableColumn<FolderInfo, String> justFolderName_col;
	@FXML private TableColumn<FolderInfo, String> maxDates_col;
	@FXML private TableColumn<FolderInfo, String> minDate_col;
	@FXML private TableView<FolderInfo> table;
	@FXML private TextField tableDescription_tf;
	@FXML private Tooltip addToBatch_tooltip;
	@FXML private Tooltip collectSimilarDates_btn_tooltip;
	@FXML private Tooltip copySelected_btn_tooltip;
	@FXML private Tooltip resetSelectedFileInfos_btn_tooltip;
	@FXML private Tooltip select_all_btn_tooltip;
	@FXML private Tooltip select_bad_btn_tooltip;
	@FXML private Tooltip select_dateDifference_tooltip;
	@FXML private Tooltip select_good_btn_tooltip;
	@FXML private Tooltip select_invert_btn_tooltip;
	@FXML private Tooltip select_none_btn_tooltip;
	@FXML private Tooltip tableDescription_tf_tooltip;
	@FXML private Tooltip updateFolderInfo_btn_tooltip;
	@FXML private VBox group;
	@FXML private VBox table_Vbox;
	@FXML public Button hide_btn;
    @FXML public Button showHideButtonRoot;
    @FXML private AnchorPane hideablePane;
    @FXML private Label allFilesSize_lbl;
	// @formatter:on

    public Label getAllFilesCopied_lbl() {
        return allFilesCopied_lbl;
    }

    public Label getAllFilesTotal_lbl() {
        return allFilesTotal_lbl;
    }

    public Label getAllFilesSize_lbl() {
        return allFilesSize_lbl;
    }

    @FXML
    private void resetSelectedFileInfos_btn_action(ActionEvent event) {
        AtomicInteger counter = new AtomicInteger(0);

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
                    for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                        fileInfo.setWorkDir("");
                        fileInfo.setWorkDirDriveSerialNumber("");
                        fileInfo.setDestination_Path("");
                        fileInfo.setCopied(false);
                        fileInfo.setConfirmed(false);
                        fileInfo.setEvent("");
                        fileInfo.setLocation("");
                        fileInfo.setIgnored(false);
                        fileInfo.setTableDuplicated(false);
                        fileInfo.setTags("");
                        Main.setChanged(true);
                    }
                    FolderInfo_Utils.calculateFileInfoStatuses(folderInfo);
                    TableUtils.refreshAllTableContent(model_main.tables());
                    // ldt.setMessage("counter; " + counter.get());
                    counter.getAndIncrement();

                }
                return null;
            }

        };
        Thread thread = new Thread(task, "resettingSelecteFileInfos_thread");
        thread.start();
        Messages.sprintf("Resetting selected fileinfo's done!");
    }

    @FXML
    private void mergeMove_btn_action(ActionEvent event) {
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

        if (tableType.equals(TableType.SORTIT.getType())) {
            if (model_main.tables().getSortIt_table().getSelectionModel().getSelectedItems().size() <= 1) {
                warningText(bundle.getString("noSelectedFoldersToMerge"));
                return;
            }
        } else if (tableType.equals(TableType.SORTED.getType())) {
            if (model_main.tables().getSorted_table().getSelectionModel().getSelectedItems().size() <= 1) {
                warningText(bundle.getString("noSelectedFoldersToMerge"));
                return;
            }
        } else if (tableType.equals(TableType.ASITIS.getType())) {
            if (model_main.tables().getAsItIs_table().getSelectionModel().getSelectedItems().size() <= 1) {
                warningText(bundle.getString("noSelectedFoldersToMerge"));
                return;
            }
        }

        FXMLLoader loader = null;
        Parent root = null;

        try {
            loader = new FXMLLoader(Main.class.getResource("fxml/main/merge/MergeDialog.fxml"), Main.bundle);
            root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.initOwner(Main.scene_Switcher.getScene_main().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            Main.centerWindowDialog(stage);

            stage.setMaxWidth(Main.conf.getScreenBounds().getWidth());
            stage.setAlwaysOnTop(true);
            scene.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.DIALOGSSTYLE.getType()).toExternalForm());
            MergeDialogController mergeDialogController = (MergeDialogController) loader.getController();
            mergeDialogController.init(model_main, model_main.tables(), table, tableType);
            stage.setScene(scene);

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void collectSimilarDates_btn_action(ActionEvent event) {
        try {
            Parent parent = null;
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/main/collect/Collect_Dialog.fxml"), bundle);
            parent = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(parent);
            model_CollectDialog = new Model_CollectDialog();

            Collect_DialogController controller = (Collect_DialogController) loader.getController();
            controller.init(model_main, model_CollectDialog, table, tableType);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void copySelected_btn_action(ActionEvent event) {
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
        Dialog<ButtonType> iHaveCheckedEverythingAndAcceptAllChanges_dialog = Dialogs.createDialog_YesNo(Main.scene_Switcher.getWindow(), bundle.getString("iHaveCheckedEverythingAndAcceptAllChanges"));
        Optional<ButtonType> result = iHaveCheckedEverythingAndAcceptAllChanges_dialog.showAndWait();
        if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            Messages.sprintf("Starting moving files from sortit to sorted");
            TableUtils.copySelectedTableRows(model_main, table, tableType);
        }
    }

    @FXML
    private void addToBatch_btn_action(ActionEvent event) {
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
        TableUtils.addToBatchSelectedTableRows(model_main, table, tableType);
    }

    // @formatter:on
    @FXML
    private void checkChanges_mi_action(ActionEvent event) {
        LoadingProcessTask loadingProcess = new LoadingProcessTask(owner);
        Task<Boolean> checkTask = new CheckSelectedRowForChanges(table, model_main, loadingProcess);

        Thread thread = new Thread(checkTask, "Checking changes thread");
        thread.start();
    }

    @FXML
    private void updateFolderInfo_btn_action(ActionEvent event) {
        TableUtils.updateTableContent(table, model_main.tables());
    }

    @FXML
    private void reload_all_mi_action(ActionEvent event) {
        sprintf("Reload All");
        LoadingProcessTask lpt = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        Task<Void> updateTableValuesUsingFileInfo_task = new CreateFileInfoRow(model_main, table, Main.scene_Switcher.getWindow());
        updateTableValuesUsingFileInfo_task.setOnSucceeded(event1 -> {
            sprintf("updateTableValuesFileInfo done successfully");
            lpt.closeStage();
            TableUtils.calculateTableViewsStatistic(model_main.tables());
        });

        updateTableValuesUsingFileInfo_task.setOnCancelled(event12 -> {
            lpt.closeStage();
            Messages.warningText("Creating file info cancelled");
        });

        updateTableValuesUsingFileInfo_task.setOnFailed(event13 -> {
            lpt.closeStage();
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
        });
        lpt.setTask(updateTableValuesUsingFileInfo_task);
        Thread thread = new Thread(updateTableValuesUsingFileInfo_task, "Reloading Thread");
        thread.start();
    }

    @FXML
    private void select_all_btn_action(ActionEvent event) {
        model_main.buttons().select_all_Table(table);
    }

    public void setShowHideTableButtonIcons(Button button, boolean show) {
        ImageView iv = (ImageView) button.getGraphic();
        if (show) {
            Platform.runLater(() -> {
                iv.setRotate(0);
            });
        } else {
            Platform.runLater(() -> {
                iv.setRotate(-180);
            });
        }
    }

    public void setTableIsShown(boolean show) {
        if (tableType.equals(TableType.SORTIT)) {
            model_main.tables().showAndHideTables.setSortit_show_property(!model_main.tables().showAndHideTables.getSortit_show_property().get());
        } else if (tableType.equals(TableType.SORTED)) {
            model_main.tables().showAndHideTables.setSorted_show_property(!model_main.tables().showAndHideTables.getSorted_show_property().get());
        } else if (tableType.equals(TableType.ASITIS)) {
            model_main.tables().showAndHideTables.setAsitis_show_property(!model_main.tables().showAndHideTables.getAsitis_show_property().get());
        }

    }

    public void setTableWidth(Pane pane, double width, TableType tableType) {
        if (tableType.equals(TableType.SORTIT)) {
            pane.setPrefWidth(width);
        } else if (tableType.equals(TableType.SORTED)) {
            pane.setPrefWidth(width);
        } else if (tableType.equals(TableType.ASITIS)) {
            pane.setPrefWidth(width);
        }
    }

    @FXML
    private void hide_btn_action(ActionEvent event) {

        int visibles = TableUtils.getVisibleTables(model_main);
        if (visibles <= 1 && table.isVisible()) {
            return;
        }
        table.setVisible(!table.isVisible());
        tableInformation_flowpane.setVisible(!tableInformation_flowpane.isVisible());
        hideablePane.setVisible(!hideablePane.isVisible());
        tableInformation_flowpane.setVisible(!table.isVisible());
        tableInformation_flowpane.getStyleClass().add("notOk");

        setShowHideTableButtonIcons(hide_btn, table.isVisible());
        if(!table.isVisible()) {
            table_Vbox.setPrefWidth(35);
            table_Vbox.setMinWidth(35);
            table_Vbox.setMaxWidth(35);
            tableInformation_flowpane.setVisible(false);
            Messages.sprintf("hidden: table_Vbox.getWidth(); " + table_Vbox.getWidth() + " pref width: " + table_Vbox.getPrefWidth() + " MIN width: " + table_Vbox.getMinWidth() + " MAX width: " + table_Vbox.getMaxWidth());
        } else {
            table_Vbox.setPrefWidth(-1);
            table_Vbox.setMinWidth(-1);
            table_Vbox.setMaxWidth(-1);
            tableInformation_flowpane.setVisible(true);
            Messages.sprintf("showing: table_Vbox.getWidth(); " + table_Vbox.getWidth() + " pref width: " + table_Vbox.getPrefWidth() + " MIN width: " + table_Vbox.getMinWidth() + " MAX width: " + table_Vbox.getMaxWidth());
        }

        Main.getMain_stage().setWidth(Main.getMain_stage().getWidth() - 1);
        Main.getMain_stage().setWidth(Main.getMain_stage().getWidth() + 1);

    }

    private Pane getPaneFromParent(Parent parent, String id) {
        Messages.sprintf("getPaneFromParent parent is: " + parent);
        VBox pane = (VBox) parent;
        if (pane != null) {
            for (Node table_vbox_node : pane.getChildren()) {
                if (table_vbox_node instanceof HBox && table_vbox_node.getId().equals(id)) {
                    return (HBox) table_vbox_node;
                }
                if (table_vbox_node instanceof AnchorPane && table_vbox_node.getId().equals(id)) {
                    return (AnchorPane) table_vbox_node;
                }
            }
        }
        return null;
    }

    @FXML
    private void reload_btn_action(ActionEvent event) {
        Messages.warningText("No methods for reload btn");
    }

    @FXML
    private void select_bad_btn_action(ActionEvent event) {
        model_main.buttons().select_bad_Table(table);
    }

    @FXML
    private void select_dateDifference_btn_action(ActionEvent event) {
        model_main.buttons().select_dateDifference_Table(table);
    }

    @FXML
    private void openFolderInSystemFileManager(ActionEvent event) {
        Messages.sprintf("openFolderInSystemFileManager Event: " + event.getSource());
        FolderInfo folderInfo = table.getSelectionModel().getSelectedItem();
        Path path = Paths.get(folderInfo.getFolderPath());
        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void select_good_btn_action(ActionEvent event) {
        model_main.buttons().select_good_Table(table);
    }

    @FXML
    private void select_invert_btn_action(ActionEvent event) {
        model_main.buttons().select_invert_Table(table);
    }

    @FXML
    private void select_none_btn_action(ActionEvent event) {
        model_main.buttons().select_none_Table(table);
    }

    @FXML
    private void handleMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Messages.sprintf("Mouse clicked on tableview: " + mouseEvent);
        }
    }

    public TableView<FolderInfo> getTable() {
        return this.table;
    }


    public void init(Model_main aModel_main, String tableName, String tableType, Button showHideButtonRoot) {
        this.model_main = aModel_main;
        this.model_main.tables().setDeleteKeyPressed(table);

        this.tableType = tableType;

        this.showHideButtonRoot = showHideButtonRoot;
        showHideButtonRoot.setOnAction(event -> {
            Messages.sprintf("YEEEEIEEE: " + tableType);
            setShowHideTableButtonIcons(showHideButtonRoot, model_main.tables().getTableByType(tableType).isVisible());
        });

        show_im = GUI_Methods.loadImage("showTable.png", GUIPrefs.BUTTON_WIDTH);
        setShowHideTableButtonIcons(hide_btn, table.isVisible());

        Platform.runLater(() -> {
            setShowHideTableButtonIcons(hide_btn, true);
        });

        tableDescription_tf.setText(tableName);

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setEditable(true);
        table.setPlaceholder(new Label(bundle.getString("tableContentEmpty")));
        table.setId(tableType);
        table.setItems(data_obs);

        table_Vbox.setId(tableType.toLowerCase() + "_table_vbox");
        /*table_Vbox.setMinWidth(40);*/
        table_Vbox.setFillWidth(true);

        model_main.tables().setDrag(table);

        allFilesTotal_lbl.textProperty().bindBidirectional(allFilesTotal_obs, new NumberStringConverter());

        badFiles_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getBadFiles()));
        connected_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Boolean> cellData) -> new SimpleObjectProperty<>(cellData.getValue().isConnected()));
        copied_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getCopied()));
        dateDifference_ratio_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Double> param) -> new SimpleObjectProperty<>(param.getValue().getDateDifferenceRatio()));
        folderFiles_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolderFiles()));
        fullPath_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolderPath()));
        image_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolderImageFiles()));
        justFolderName_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getJustFolderName()));
        maxDates_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getMaxDate()));
        minDate_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, String> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getMinDate()));
        raw_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolderRawFiles()));
        size_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Long> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolderSize()));
        status_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));
        suggested_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getSuggested()));
        video_col.setCellValueFactory((TableColumn.CellDataFeatures<FolderInfo, Integer> cellData) -> new SimpleObjectProperty<>(cellData.getValue().getFolderVideoFiles()));

        connected_col.setCellFactory(model_main.tables().connected_cellFactory);
        copied_col.setCellFactory(model_main.tables().copied_cellFactory);
        dateDifference_ratio_col.setCellFactory(model_main.tables().dateDifference_Status_cellFactory);
        dateFix_col.setCellFactory(model_main.tables().dateFixer_cellFactory);
        justFolderName_col.setCellFactory(param -> new EditingCell(model_main, param));
        status_col.setCellFactory(model_main.tables().cell_Status_cellFactory);

        dateDifference_ratio_col.setSortType(SortType.ASCENDING);
        justFolderName_col.setEditable(true);

        showTable.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> model_main.tables().showAndHideTables.showTable(tableType, newValue));

        justFolderName_col.setOnEditCommit(event -> {
            Messages.sprintf("edit commited event.getNewValue(); " + event.getNewValue());
            FolderInfo folderInfo = (FolderInfo) event.getRowValue();
            if (event.getNewValue().isEmpty()) {
                folderInfo.setJustFolderName(Paths.get(folderInfo.getFolderPath()).getFileName().toString());
            } else if (!folderInfo.getJustFolderName().equals(event.getNewValue()) && !event.getNewValue().isEmpty()) {
                folderInfo.setJustFolderName(event.getNewValue());

                for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                    fileInfo.setEvent(event.getNewValue());
                }

                Main.setChanged(true);
                folderInfo.setChanged(true);
                FolderInfo_Utils.calculateFileInfoStatuses(folderInfo);

                Connection connection = SqliteConnection.connector(folderInfo.getFolderPath(), Main.conf.getMdir_db_fileName());
                SQL_Utils.isDbConnected(connection);
                SQL_Utils.setAutoCommit(connection, false);

                FileInfo_SQL.insertFileInfoListToDatabase(connection, folderInfo.getFileInfoList(), false);

                SQL_Utils.commitChanges(connection);
                SQL_Utils.closeConnection(connection);

            }
            TableUtils.refreshAllTableContent(model_main.tables());
            TableUtils.saveChangesContentsToTables(model_main.tables());
        });
        size_col.setCellFactory(tableColumn -> new TableCell<FolderInfo, Long>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);
                if (value == null || empty) {
                    setText("");
                } else {
                    setText("" + (Conversion.convertToSmallerConversion(value)));
                }
            }
        });

        if (hide_btn == null) {
            Messages.errorSmth(ERROR, "model_main.tables().getHideButtons(). were null", null, Misc.getLineNumber(), true);
        }

        /*
         * Disabling buttons
         */
        Messages.sprintf("table is editable? " + table.isEditable() + " just fold editable?  " + justFolderName_col.isEditable());

        if (tableType.equals(TableType.ASITIS.getType())) {
            tableDescription_tf_tooltip.setText(Main.bundle.getString("asitis_table_desc"));
            tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
            model_main.tables().setAsItIsRootPane(table_Vbox);
        } else if (tableType.equals(TableType.SORTIT.getType())) {
            tableDescription_tf_tooltip.setText(Main.bundle.getString("sortit_table_desc"));
            tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
            model_main.tables().setSortItRootPane(table_Vbox);
        } else if (tableType.equals(TableType.SORTED.getType())) {
            tableDescription_tf_tooltip.setText(Main.bundle.getString("sorted_table_desc"));
            tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
            model_main.tables().setSortedRootPane(table_Vbox);
        }

        Main.conf.showTooltips_property().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    updateFolderInfo_btn.setTooltip(updateFolderInfo_btn_tooltip);
                    select_all_btn.setTooltip(select_all_btn_tooltip);
                    select_bad_btn.setTooltip(select_bad_btn_tooltip);
                    select_good_btn.setTooltip(select_good_btn_tooltip);
                    select_invert_btn.setTooltip(select_invert_btn_tooltip);
                    select_none_btn.setTooltip(select_none_btn_tooltip);

                    if (tableType.equals(TableType.ASITIS.getType())) {
                        tableDescription_tf_tooltip.setText(Main.bundle.getString("asitis_table_desc"));
                        tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
                    } else if (tableType.equals(TableType.SORTIT.getType())) {
                        tableDescription_tf_tooltip.setText(Main.bundle.getString("sortit_table_desc"));
                        tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
                    } else if (tableType.equals(TableType.SORTED.getType())) {
                        tableDescription_tf_tooltip.setText(Main.bundle.getString("sorted_table_desc"));
                        tableDescription_tf.setTooltip(tableDescription_tf_tooltip);
                    }
                } else {
                    TableUtils.hideTooltip(updateFolderInfo_btn);
                    TableUtils.hideTooltip(select_bad_btn);
                    TableUtils.hideTooltip(select_good_btn);
                    TableUtils.hideTooltip(select_invert_btn);
                    TableUtils.hideTooltip(select_none_btn);
                    TableUtils.hideTooltip(select_all_btn);
                }
            }
        });
//        table.setRowFactory(
//                new Callback<TableView<FolderInfo>, TableRow<FolderInfo>>() {
//                    @Override
//                    public TableRow<FolderInfo> call(TableView<FolderInfo> tableView) {
//
//                        return null;
//                    }
//                });
//        table.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
//            if (event.getButton() == MouseButton.SECONDARY) {
//                ContextMenu contextMenu = new ContextMenu();
//                Messages.sprintf("event.getTarget(): " +event.getTarget());
//            }
//        });
    }

}
