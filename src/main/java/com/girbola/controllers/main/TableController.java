
package com.girbola.controllers.main;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.collect.Collect_DialogController;
import com.girbola.controllers.main.collect.Model_CollectDialog;
import com.girbola.controllers.main.merge.MergeDialogController;
import com.girbola.controllers.main.sql.TablesSQL;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.utils.WorkdirUtils;
import common.utils.Conversion;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.NumberStringConverter;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

public class TableController {

    private final double buttonHideSize = 26;
    private final String ERROR = TableController.class.getSimpleName();

    private Model_CollectDialog model_CollectDialog;
    private ModelMain model_main;
    private final ObservableList<FolderInfo> data_obs = FXCollections.observableArrayList();
    private final SimpleBooleanProperty showTable = new SimpleBooleanProperty(true);
    private final SimpleIntegerProperty allFilesTotal_obs = new SimpleIntegerProperty(0);
    private String tableType;
    private Window owner;
    private ArrayList<HBox> savedButtonsHBoxList;

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
//	@FXML private HBox tableInformation_hbox;
//	@FXML private HBox tables_parent;
//	@FXML private ImageView hide_btn_iv;
    @FXML private Label selectedLbl;
	@FXML private Label allFilesCopied_lbl;
	@FXML private Label allFilesTotal_lbl;
    @FXML private MenuButton menuReload;
    @FXML private MenuButton menuAction;
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
	@FXML private Label tableDescription_tf;
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
	@FXML private VBox table_Vbox;
	@FXML public Button hide_btn;
    @FXML private Label allFilesSize_lbl;
    @FXML HBox tableLabelNameHBox;
    @FXML private Separator buttonsSeparator1;
    @FXML private Separator buttonsSeparator2;
    @FXML private Separator buttonsSeparator3;
    @FXML HBox descriptionHBox;
    @FXML VBox table_rootVBox;
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
                    FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
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
        boolean verifyWorkDirectory = WorkdirUtils.verifyWorkDirectory();
        if (!verifyWorkDirectory) {
            return;
        }

        Main.setProcessCancelled(false);
/*
        try {
            if (!Files.exists(Paths.get(conf.getWorkDir()).toRealPath())) {
                warningText(bundle.getString("cannotFindWorkDir"));
                return;
            }
        } catch (IOException ex) {
            warningText(bundle.getString("cannotFindWorkDir"));
            return;
        }
*/

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
            stage.initOwner(Main.sceneManager.getScene_main().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            Main.centerWindowDialog(stage);

            stage.setMaxWidth(Misc.getScreenBounds().getWidth());
            stage.setAlwaysOnTop(true);
            scene.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
            MergeDialogController mergeDialogController = loader.getController();
            mergeDialogController.init(model_main, model_main.tables(), table, tableType);
            stage.setScene(scene);

            Platform.runLater(() -> {
                stage.showAndWait();
            });

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

            Collect_DialogController controller = loader.getController();
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
        Dialog<ButtonType> iHaveCheckedEverythingAndAcceptAllChanges_dialog = Dialogs.createDialog_YesNo(Main.sceneManager.getWindow(), bundle.getString("iHaveCheckedEverythingAndAcceptAllChanges"));
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
        LoadingProcessTask lpt = new LoadingProcessTask(Main.sceneManager.getWindow());
        Task<Void> updateTableValuesUsingFileInfo_task = new CreateFileInfoRow(model_main, table, Main.sceneManager.getWindow());
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

        if (show) {
            Platform.runLater(() -> {
                button.setRotate(0);
            });
        } else {
            Platform.runLater(() -> {
                button.setRotate(-180);
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
        Messages.sprintf("hide_btn_action pressed");
        int visibles = TableUtils.getVisibleTables(model_main);
        if (visibles <= 1 && table.isVisible()) {
            return;
        }

        Bounds btopMenuButtonFlowPane = topMenuButtonFlowPane.getBoundsInLocal();
        Messages.sprintf("topMenuButtonFlowPane: " + topMenuButtonFlowPane + " btopMenuButtonFlowPane; " + btopMenuButtonFlowPane);

        Messages.sprintf("model_main.tables().tablesParentWidthProperty();: " + model_main.tables().getTablesParentWidth());

        table.setVisible(!table.isVisible());

//        tableInformation_flowpane.setVisible(!tableInformation_flowpane.isVisible());

        //hideablePane.setVisible(!hideablePane.isVisible());

//        tableInformation_flowpane.setVisible(!table.isVisible());
        tableInformation_flowpane.getStyleClass().add("notOk");
        Bounds b = hide_btn.getBoundsInLocal();

        setShowHideTableButtonIcons(hide_btn, table.isVisible());
        hideSetTableVisible(!table.isVisible());
//        if (!table.isVisible()) {
//            Messages.sprintf("Showing the content? " + table.isVisible());
//            hideSetTableVisible(true);
//            Messages.sprintf("hidden: table_Vbox.getWidth(); "
//                    + table_Vbox.getWidth() + " pref width: "
//                    + table_Vbox.getPrefWidth() + " MIN width: "
//                    + table_Vbox.getMinWidth() + " MAX width: "
//                    + table_Vbox.getMaxWidth());
//        } else {
//            Messages.sprintf("Hiding the content? " + table.isVisible());
//            hideSetTableVisible(false);
//            Messages.sprintf("showing: table_Vbox.getWidth(); "
//                    + table_Vbox.getWidth() + " pref width: "
//                    + table_Vbox.getPrefWidth() + " MIN width: "
//                    + table_Vbox.getMinWidth() + " MAX width: "
//                    + table_Vbox.getMaxWidth());
//        }

        Main.getMain_stage().setWidth(Main.getMain_stage().getWidth() - 1);
        Main.getMain_stage().setWidth(Main.getMain_stage().getWidth() + 1);

        Main.getMain_stage().getScene().getRoot().applyCss();
        Main.getMain_stage().getScene().getRoot().layout();

    }

    private void hideSetTableVisible(boolean hide) {
        Messages.sprintf("VISIBLE TABLE IS NOW:::::::: " + hide);

        Platform.runLater(() -> {
            // Hide or show all child elements
//            toggleVisibilityAndManaged(buttonsSeparator1, hide);
            if(table.isVisible()) {
                Messages.sprintf("TABLE IS VISIBLE!!!!!!!!");
                hide_btn.setManaged(true);
                table.setManaged(true);

                table_rootVBox.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                table_rootVBox.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                table_rootVBox.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                tableLabelNameHBox.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                tableLabelNameHBox.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                tableLabelNameHBox.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                topMenuButtonFlowPane.setMinSize(420, buttonHideSize);
                topMenuButtonFlowPane.setPrefSize(420, buttonHideSize);
                topMenuButtonFlowPane.setMaxSize(420, buttonHideSize);

                buttonsSeparator1.setMinSize(Region.USE_COMPUTED_SIZE, 0);
                buttonsSeparator1.setPrefSize(4, 17);
                buttonsSeparator1.setMaxSize(Region.USE_COMPUTED_SIZE, 0);

                buttonsSeparator2.setMinSize(Region.USE_COMPUTED_SIZE, 0);
                buttonsSeparator2.setPrefSize(4, 17);
                buttonsSeparator2.setMaxSize(Region.USE_COMPUTED_SIZE, 0);

                buttonsSeparator3.setMinSize(Region.USE_COMPUTED_SIZE, 0);
                buttonsSeparator3.setPrefSize(4, 17);
                buttonsSeparator3.setMaxSize(Region.USE_COMPUTED_SIZE, 0);

                updateFolderInfo_btn.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                updateFolderInfo_btn.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                updateFolderInfo_btn.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                menuReload.setMinSize(100, buttonHideSize);
                menuReload.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                menuReload.setMaxSize(100, buttonHideSize);

                menuAction.setMinSize(100, buttonHideSize);
                menuAction.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                menuAction.setMaxSize(100, buttonHideSize);

                select_all_btn.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_all_btn.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_all_btn.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                select_good_btn.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_good_btn.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_good_btn.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                select_bad_btn.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_bad_btn.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_bad_btn.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                select_none_btn.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_none_btn.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_none_btn.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                select_invert_btn.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_invert_btn.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                select_invert_btn.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                descriptionHBox.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                descriptionHBox.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                descriptionHBox.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

                buttons_hbox.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                buttons_hbox.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                buttons_hbox.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                tableInformation_flowpane.setMinSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                tableInformation_flowpane.setPrefSize(Region.USE_COMPUTED_SIZE, buttonHideSize);
                tableInformation_flowpane.setMaxSize(Region.USE_COMPUTED_SIZE, buttonHideSize);

            } else {
                Messages.sprintf("TABLE  NOOOOT VISIBLE!!!!!!!!");
                hide_btn.setManaged(false);

                table_rootVBox.setMinSize(buttonHideSize,  Region.USE_COMPUTED_SIZE);
                table_rootVBox.setPrefSize(buttonHideSize,  Region.USE_COMPUTED_SIZE);
                table_rootVBox.setMaxSize(buttonHideSize,  Region.USE_COMPUTED_SIZE);

                tableLabelNameHBox.setMinSize(0,0);
                tableLabelNameHBox.setPrefSize(0,0);
                tableLabelNameHBox.setMaxSize(0, 0);

                topMenuButtonFlowPane.setMinSize(buttonHideSize,  buttonHideSize);
                topMenuButtonFlowPane.setPrefSize(buttonHideSize,  buttonHideSize);
                topMenuButtonFlowPane.setMaxSize(buttonHideSize,  buttonHideSize);

                buttonsSeparator1.setMinSize(0, 0);
                buttonsSeparator1.setPrefSize(0, 0);
                buttonsSeparator1.setMaxSize(0, 0);

                buttonsSeparator2.setMinSize(0, 0);
                buttonsSeparator2.setPrefSize(0, 0);
                buttonsSeparator2.setMaxSize(0, 0);

                buttonsSeparator3.setMinSize(0, 0);
                buttonsSeparator3.setPrefSize(0, 0);
                buttonsSeparator3.setMaxSize(0, 0);

                updateFolderInfo_btn.setMinSize(0, 0);
                updateFolderInfo_btn.setPrefSize(0, 0);
                updateFolderInfo_btn.setMaxSize(0, 0);

                menuReload.setMinSize(0, 0);
                menuReload.setPrefSize(0, 0);
                menuReload.setMaxSize(0, 0);

                menuAction.setMinSize(0, 0);
                menuAction.setPrefSize(0, 0);
                menuAction.setMaxSize(0, 0);

                select_all_btn.setMinSize(0, 0);
                select_all_btn.setPrefSize(0, 0);
                select_all_btn.setMaxSize(0, 0);

                select_good_btn.setMinSize(0, 0);
                select_good_btn.setPrefSize(0, 0);
                select_good_btn.setMaxSize(0, 0);

                select_bad_btn.setMinSize(0, 0);
                select_bad_btn.setPrefSize(0, 0);
                select_bad_btn.setMaxSize(0, 0);

                select_none_btn.setMinSize(0, 0);
                select_none_btn.setPrefSize(0, 0);
                select_none_btn.setMaxSize(0, 0);

                select_invert_btn.setMinSize(0, 0);
                select_invert_btn.setPrefSize(0, 0);
                select_invert_btn.setMaxSize(0, 0);

                descriptionHBox.setMinSize(0, 0);
                descriptionHBox.setPrefSize(0, 0);
                descriptionHBox.setMaxSize(0, 0);

                buttons_hbox.setMinSize(0, 0);
                buttons_hbox.setPrefSize(0, 0);
                buttons_hbox.setMaxSize(0, 0);
            }

            toggleVisibilityAndManaged(buttonsSeparator1, hide);
            toggleVisibilityAndManaged(buttonsSeparator2, hide);
            toggleVisibilityAndManaged(buttonsSeparator3, hide);
            toggleVisibilityAndManaged(descriptionHBox, hide);
            toggleVisibilityAndManaged(menuAction, hide);
            toggleVisibilityAndManaged(menuReload, hide);
            toggleVisibilityAndManaged(select_all_btn, hide);
            toggleVisibilityAndManaged(select_bad_btn, hide);
            toggleVisibilityAndManaged(select_good_btn, hide);
            toggleVisibilityAndManaged(select_invert_btn, hide);
            toggleVisibilityAndManaged(select_none_btn, hide);
            toggleVisibilityAndManaged(updateFolderInfo_btn, hide);
            toggleVisibilityAndManaged(tableInformation_flowpane, hide);
            toggleVisibilityAndManaged(tableLabelNameHBox, hide);

//            topMenuButtonFlowPane.setManaged(hide);
            // Dynamically adjust the size of the parent HBox
//            adjustParentContainerSize(descriptionHBox, hide);
//            if (!buttons_hbox.isVisible()) {
//                Platform.runLater(() -> {
//                    buttons_hbox.setMinWidth(0);
//                    buttons_hbox.setMaxWidth(0);
//                    buttons_hbox.setPrefWidth(0);
//
//                    topMenuButtonFlowPane.setMinWidth(0);
//                    topMenuButtonFlowPane.setMaxWidth(0);
//
//                    descriptionHBox.setMinWidth(0);
//                    descriptionHBox.setMaxWidth(0);
//                });
//
//            } else {
//                Platform.runLater(() -> {
//                    Platform.runLater(() -> {
//                        buttons_hbox.setMinWidth(50);
//                        buttons_hbox.setMaxWidth(50);
//                        buttons_hbox.setPrefWidth(50);
//
//                        topMenuButtonFlowPane.setMinWidth(50);
//                        topMenuButtonFlowPane.setMaxWidth(50);
//
//                        descriptionHBox.setMinWidth(50);
//                        descriptionHBox.setMaxWidth(50);
//                    });
//                });
//
//            }
        });
    }

    /**
     * Helper method to safely toggle visibility and managed state for nodes.
     *
     * @param node The JavaFX Node (UI element) to toggle.
     * @param hide Boolean flag to determine visibility state.
     */
    private void toggleVisibilityAndManaged(Node node, boolean hide) {
        if (node != null) {
            node.setVisible(!hide);  // Hide or show the element
            node.setManaged(!hide);  // Include or exclude it from layout calculations
        }
    }

    /**
     * Adjust the parent container's size dynamically based on the visibility state.
     * If all child elements are hidden, shrink the container to minimum dimensions.
     *
     * @param parent The parent container (e.g., HBox).
     * @param hide   Boolean flag for whether to shrink or expand the container.
     */
    private void adjustParentContainerSize(Region parent, boolean hide) {
        if (parent != null) {
            if (hide) {
                // Shrink the parent container to minimal size when hiding all children
                parent.setMinWidth(0);
                parent.setMinHeight(0);
                parent.setPrefWidth(0);
                parent.setPrefHeight(0);
            } else {
                // Restore the parent container's default size rules
                parent.setMinWidth(Region.USE_COMPUTED_SIZE);
                parent.setMinHeight(Region.USE_COMPUTED_SIZE);
                parent.setPrefWidth(Region.USE_COMPUTED_SIZE);
                parent.setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            // Force the layout to update dynamically
            parent.applyCss();
            parent.layout();
        }
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

    public void init(ModelMain aModel_main, String tableName, String tableType) {
        this.model_main = aModel_main;
        this.model_main.tables().setDeleteKeyPressed(table);

        this.tableType = tableType;

        setShowHideTableButtonIcons(hide_btn, table.isVisible());

        Platform.runLater(() -> {
            setShowHideTableButtonIcons(hide_btn, true);
        });

        tableDescription_tf.setText(tableName);

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Listener to keep track of selected items
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int selectedCount = table.getSelectionModel().getSelectedItems().size();
            Messages.sprintf("selectedCount: " + selectedCount);
            updateSelectedLbl(selectedCount);
        });

        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                table.getSelectionModel().selectAll(); // Select all items
                int selectedCount = table.getSelectionModel().getSelectedItems().size();
                Messages.sprintf("selectedCount: " + selectedCount);
                updateSelectedLbl(selectedCount);

                event.consume(); // Consume the event to prevent further processing
            }
        });

        table.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isControlDown()) {
                // Handle Ctrl + Click for selecting/deselecting rows
                int selectedCount = table.getSelectionModel().getSelectedItems().size();
                Messages.sprintf("selectedCount: " + selectedCount);
                updateSelectedLbl(selectedCount);
                event.consume(); // Consume the event
            }
        });

        table.focusedProperty().addListener((observable, oldValue, newValue) -> {
            Messages.sprintf("table.focusedProperty(): " + newValue);
        });
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
            FolderInfo folderInfo = event.getRowValue();
            if (event.getNewValue().isEmpty()) {
                folderInfo.setJustFolderName(Paths.get(folderInfo.getFolderPath()).getFileName().toString());
            } else if (!folderInfo.getJustFolderName().equals(event.getNewValue()) && !event.getNewValue().isEmpty()) {
                folderInfo.setJustFolderName(event.getNewValue());

                for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                    fileInfo.setEvent(event.getNewValue());
                }

                Main.setChanged(true);
                folderInfo.setChanged(true);
                FolderInfoUtils.calculateFolderInfoStatus(folderInfo);


                FileInfo_SQL.insertFileInfoListToDatabase(folderInfo, false);

//                SQL_Utils.commitChanges(connection);
//                SQL_Utils.closeConnection(connection);

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
                    setText(Conversion.convertToSmallerConversion(value));
                }
            }
        });


        TablesSQL.restoreColumnOrderFromRow(table);


        // Listen for column reorder events
        table.getColumns().addListener((ListChangeListener<TableColumn<FolderInfo, ?>>) change -> {
            Platform.runLater(() -> {
                TablesSQL.saveColumnOrderToRow(table);
            });
//            while (change.next()) {
//                System.out.println("Column order changed:");
//                for (TableColumn<?, ?> column : table.getColumns()) {
//                    System.out.println(column.getText());
//                }
//            }
        });
//        table.setRowFactory(new Callback<TableView<FolderInfo>, TableRow<FolderInfo>>() {
//            @Override
//            public TableRow<FolderInfo> call(TableView<FolderInfo> folderInfoTableView) {
//                final TableRow<FolderInfo> row = new TableRow<>() {
//                    @Override
//                    protected void updateItem(FolderInfo folderInfo, boolean empty) {
//                        super.updateItem(folderInfo, empty);
//
//                    }
//                };
//                return row;
//            }
//        });

        if (hide_btn == null) {
            Messages.errorSmth(ERROR, "model_main.tables().getHideButtons(). were null", null, Misc.getLineNumber(), true);
        }
        savedButtonsHBoxList = buttons_hbox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .collect(Collectors.toCollection(ArrayList::new));

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

        Main.conf.showTooltipsProperty().addListener(new ChangeListener<Boolean>() {
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
    }

    private void updateSelectedLbl(int selectedCount) {
        if (selectedCount > 0) {
            Platform.runLater(() -> {
                selectedLbl.setText(selectedCount + " " + bundle.getString("selected"));
            });
        } else {
            Platform.runLater(() -> {
                selectedLbl.setText("");
            });
        }
    }

    private ScrollBar getHorizontalScrollBarFromTableView(TableView<?> tableView) {
        if (tableView.getSkin() instanceof TableViewSkin<?> skin) {
            VirtualFlow<?> virtualFlow = (VirtualFlow<?>) skin.getChildren().get(1); // VirtualFlow is usually the second child
            ScrollBar scrollBar = (ScrollBar) virtualFlow.lookup(".scroll-bar:horizontal");
            if (scrollBar != null) {
                return scrollBar;
            }

        }
        return null;
    }

    private ScrollPane getScrollPaneFromTableView() {
        if (table.getSkin() instanceof TableViewSkin<?> skin) {
            return (ScrollPane) skin.getChildren().stream()
                    .filter(node -> node instanceof ScrollPane)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private ScrollPane findScrollPaneInTableView(TableView<?> tableView) {
        if (tableView.getSkin() instanceof TableViewSkin<?> skin) {
            for (Node child : skin.getChildren()) {
                if (child instanceof ScrollPane) {
                    return (ScrollPane) child;
                }
            }
        }
        return null;
    }

    private void checkScrollBarVisibility() {
        ScrollPane scrollPane = getScrollPaneFromTableView();
        if (scrollPane != null) {
            // Check horizontal scrollbar
            ScrollBar horizontalScrollBar = (ScrollBar) scrollPane.lookup(".scroll-bar:horizontal");
            if (horizontalScrollBar != null) {
                System.out.println("Horizontal ScrollBar visible: " + horizontalScrollBar.isVisible());
            }
        } else {
            Messages.sprintf("scrollPane is null");
        }
//        Platform.runLater(() -> {
//            ScrollBar horizontalBar = null;
//            ScrollPane tableViewScrollPane = (javafx.scene.control.ScrollPane) table.lookup(".scroll-pane");
//            if (tableViewScrollPane != null) {
//                Messages.sprintf("This worokrokaeorkgoaekthoaeth? " + tableViewScrollPane);
//                // Check horizontal scrollbar
//                var horizontalScrollBar = (javafx.scene.control.ScrollBar) tableViewScrollPane.lookup(".scroll-bar:horizontal");
//                if (horizontalScrollBar != null) {
//                    System.out.println("Horizontal ScrollBar visible: " + horizontalScrollBar.isVisible());
//                }
//            }
//            for (Node node : table.lookupAll(".scroll-bar:horizontal")) {
//                if (node instanceof ScrollBar bar) {
//                    Messages.sprintf("WHAT IS THIS: " + node);
//                    if (bar.getOrientation().equals(Orientation.HORIZONTAL)) {
//                        horizontalBar = bar;
//                    }
//
//                }
//            }
//            if (horizontalBar != null) {
//                horizontalBar.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//                            Messages.sprintf("horizontalBar is visible? " + newValue);
//                        }
//                );
//            }
//        });
    }

}
