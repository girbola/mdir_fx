package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.datefixer.table.EXIF_Data_Selector;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.datefixer.utils.DestinationResolver;
import com.girbola.controllers.main.ImportImages;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.controllers.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.utils.FileInfoUtils;
import common.utils.FileUtils;

import java.util.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;
import static com.girbola.misc.Misc.openFileBrowser;

public class DateFixerController {

    private org.slf4j.Logger log = LoggerFactory.getLogger(DateFixerController.class);
    private static final String ERROR = DateFixerController.class.getSimpleName();

    private SimpleBooleanProperty leftInfoTables_visible = new SimpleBooleanProperty(true);
    private QuickPick_Navigator quickPick_Navigator;
    private ModelDatefix modelDatefix;
    private ModelMain model_main;

    //@formatter:off
	@FXML private AnchorPane df_anchorPane;
	@FXML private Button addToBatch_btn;
	@FXML private Button applyChanges_btn;
	@FXML private Button close_btn;
	@FXML private Button dateFix_btn;
	@FXML private Button folderize_btn;
	@FXML private Button hideInfoTables_btn;
	@FXML private Button hideRightInfo_btn;
	@FXML private Button show_all_btn;
	@FXML private Button show_only_selected_btn;
	@FXML private CheckBox copied_chk;
	@FXML private CheckBox ignored_chk;
	@FXML private HBox infoTable_container_root;
	@FXML private Label bad_stat;
	@FXML private Label confirmed_stat;
	@FXML private Label good_stat;
	@FXML private Label images_stat;
	@FXML private Label suggested_stat;
	@FXML private Label videos_stat;
	@FXML private MenuButton move_menuBtn;
	@FXML private ScrollPane df_scrollPane;
	@FXML private TableView<MetaData> metaDataTableView;
	@FXML private TextField filePath_tf;
	@FXML private TilePane df_tilePane;
	@FXML private TilePane quickPick_tilePane;
	@FXML private VBox rightInfoPanel;
	@FXML private VBox vbox_image_test1;
	@FXML private VBox vbox_image_test21;
	@FXML private VBox vbox_image_test2;
	@FXML private VBox vbox_image_test;

	// MISC TAB==========

	@FXML private Button accept_dates_btn;
	@FXML private Button dateFromFileName_btn;
	@FXML private Button lastModified_date_btn;
	@FXML private Button renameFileNameWithDate_btn;
	@FXML private Button restoresSelectedExifDates_btn;
	@FXML private Button updateDate_btn;
	// MISC TAB========== END

	@FXML private Button select_btn;
	@FXML private Button copyToMisc_btn;
	@FXML private Button select_acceptable_btn;
	@FXML private TableColumn<MetaData, String> info_column;
	@FXML private TableColumn<MetaData, String> value_column;
	@FXML private Button remove_btn;
	@FXML private ImageView remove;
	@FXML private Button select_all;
	@FXML private Button select_none_btn;
	@FXML private Label selection_text;
	@FXML private ScrollPane rightInfoPanel_scrollPane;
	@FXML private Label selected;
	// FXML needed! DateTimeAdjusterController
	@FXML DateTimeAdjusterController dateTimeAdjusterController;
	// FXML needed! SelectorController
	@FXML SelectorController selectorController;
	// FXML needed! TimeShiftController
	@FXML TimeShiftController timeShiftController;
	// FXML needed! FileOperationsController
	@FXML FileOperationsController fileOperationsController;

	@FXML private Button addToUnsorted_btn;
	@FXML private Button addToAsItIs_btn;
	@FXML private MenuItem sortByDate_mi;
	@FXML private MenuItem fileName_mi;

	@FXML private Button selectRangeOfNumbers_btn;
	//@formatter:on

    @FXML
    private void fileName_mi_action(ActionEvent event) {
        sortNodes();
        DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
    }

    private void sortNodes() {
        final String imageFrame = "imageFrame";
        Comparator<Node> nodeComparator = Comparator.comparing(node -> {
            VBox vbox = Node_Methods.getImageFrameNode(node, imageFrame);
            FileInfo fileInfo = (FileInfo) vbox.getUserData();
            return fileInfo.getOrgPath();
        });
        modelDatefix.getAllNodes().sort(nodeComparator);
    }

    @FXML
    private void sortByDate_mi_action(ActionEvent event) {
        modelDatefix.getAllNodes().sort((o1, o2) -> {
            VBox vbox1 = Node_Methods.getImageFrameNode(o1, "imageFrame");
            VBox vbox2 = Node_Methods.getImageFrameNode(o2, "imageFrame");
            FileInfo fileInfo1 = (FileInfo) vbox1.getUserData();
            FileInfo fileInfo2 = (FileInfo) vbox2.getUserData();
            return Long.compare(fileInfo1.getDate(), fileInfo2.getDate());
        });
        DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
    }

    @FXML
    private void addToUnsorted_btn_action(ActionEvent event) {
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            warningText(bundle.getString("workDirHasNotConnected"));
            return;
        }
        //ConcurrencyUtils.stopExecThread();

        warningText("Not ready yet!");
    }

    @FXML
    private void addToAsItIs_btn_action(ActionEvent event) {
//		addToAsItIs
        // Copy files to workdir root as it is. If folder name is empty files will be
        // under Unsorted
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            warningText(bundle.getString("workDirHasNotConnected"));
            return;
        }
        ConcurrencyUtils.stopExecThread();

        List<FileInfo> fileInfo_list = new ArrayList<>();

        for (Node n : modelDatefix.getSelectionModel().getSelectionList()) {
            if (n instanceof VBox && n.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) n.getUserData();
                Path source = Paths.get(fileInfo.getOrgPath());
                Path dest = DestinationResolver.getDestinationFileNameAsItIs(source, fileInfo);
                sprintf("Dest: " + dest);
                if (dest != null && Main.conf.getDrive_connected()) {
                    sprintf("copyToMisc_btn_action dest is: " + dest);
                    fileInfo.setWorkDir(Main.conf.getWorkDir());
                    fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                    fileInfo.setDestination_Path(dest.toString());
                    fileInfo.setCopied(false);
                    fileInfo_list.add(fileInfo);
                } else {
                    sprintf("1Dest were null. process is about to be cancelled");
                    break;
                }
            }
        }

        operateFilesTask(fileInfo_list);
        OperateFiles operateFiles = new OperateFiles(fileInfo_list, true, model_main,
                SceneNameType.DATEFIXER.getType());
        try {
            operateFiles.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void addToBatch_btn_action(ActionEvent event) {
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            warningText(bundle.getString("workDirHasNotConnected"));
            return;
        }

        ConcurrencyUtils.stopExecThread();

        List<FileInfo> fileInfo_list = new ArrayList<>();

        for (Node n : modelDatefix.getSelectionModel().getSelectionList()) {
            if (n instanceof VBox && n.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) n.getUserData();
                Path source = Paths.get(fileInfo.getOrgPath());
                Path dest = DestinationResolver.getDestinationFileNameMisc(source, fileInfo);
                if (dest != null) {
                    sprintf("blaaadestination is: " + dest);
                    fileInfo.setWorkDir(Main.conf.getWorkDir());
                    fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                    fileInfo.setDestination_Path(dest.toString());
                    fileInfo.setCopied(false);
                    fileInfo_list.add(fileInfo);
                } else {
                    sprintf("2Dest were null. process is about to be cancelled");
                    break;
                }
            }
        }

        for (Node n : modelDatefix.getSelectionModel().getSelectionList()) {
            if (n instanceof VBox && n.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) n.getUserData();
                sprintf("destination is: " + fileInfo.getDestination_Path() + " isCopied?: " + fileInfo.isCopied());
            }
        }
        Main.setChanged(true);
    }

    @FXML
    private void setBadDate_btn_action(ActionEvent event) {
        sprintf("setBadDate_btn_action: ");
        for (Node node : modelDatefix.getSelectionModel().getSelectionList()) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                HBox bottom = DateFixGuiUtils.getBottomHBox(node);
                sprintf("hboxi: " + bottom);
                bottom.setStyle(CssStylesEnum.BAD_STYLE.getStyle());
                Label fileDate = DateFixGuiUtils.getFileDateLabel(node);
                if (fileDate != null) {
                    FileInfo fileInfo = (FileInfo) node.getUserData();
                    FileInfoUtils.setBad(fileInfo);
                }

            }
        }
    }

    @FXML
    private void setModifiedDate_btn_action(ActionEvent event) {
        sprintf("setModifiedDate_btn_action: ");
        for (Node node : modelDatefix.getSelectionModel().getSelectionList()) {
            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                HBox bottom = DateFixGuiUtils.getBottomHBox(node);
                sprintf("hboxi: " + bottom);
                bottom.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
            }
        }
    }

    @FXML
    private void show_only_selected_btn_action(ActionEvent event) {
        sprintf("show_only_selected_btn_action action");
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        sprintf("show_only_selected_btn_action starting: " + df_tilePane.getChildren().size());

        df_tilePane.getChildren().clear();

        df_tilePane.getChildren().addAll(modelDatefix.getSelectionModel().getSelectionList());
        modelDatefix.getSelectionModel().clearAll();

        DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);

    }

    private boolean nodeParentIsSelected(Node nodeParent, ObservableList<Node> allNodes, ObservableList<Node> selectedNodes) {
        for (Node selectedNode : selectedNodes) {
            for (Node node : allNodes) {
                if (selectedNode.equals(node)) {
                    Messages.sprintf("Mathcfdfgdfh found");
                    return true;
                }
            }
        }
        return false;
    }

    @FXML
    private void show_all_btn_action(ActionEvent event) {
        sprintf("show_all_btn_action");
        modelDatefix.getSelectionModel().clearAll();
        modelDatefix.deselectAllExifDataSelectors();
        Platform.runLater(() -> {
            df_tilePane.getChildren().clear();
            df_tilePane.getChildren().addAll(modelDatefix.getAllNodes());
            DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
        });
    }

    @FXML
    private void folderize_btn_action(ActionEvent event) {
        sprintf("folderize_btn_action");
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        } else {
            ConcurrencyUtils.stopExecThread();
            Parent parent = null;

            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/datefixer/AskEventDialog.fxml"),
                        bundle);
                parent = loader.load();
                if (model_main == null) {
                    log.error(bundle.getString("somethingWentWrong"));
                    Messages.errorSmth(log.getName(), bundle.getString("somethingWentWrong"), null,
                            Misc.getLineNumber(), true);
                }
                warningText("model_main is null? " + (model_main == null ? true : false));
                AskEventDialogController askEventDialogController = (AskEventDialogController) loader.getController();
                askEventDialogController.init(model_main, modelDatefix);

                Scene scene = new Scene(parent);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
                stage.setOnHiding(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        modelDatefix.getSelectionModel().clearAll(df_tilePane);
                    }
                });
            } catch (Exception ex) {
                log.error(ERROR, parent, event);
                Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        }

    }

    @FXML
    private void dateFix_btn_action(ActionEvent event) {
        Path p = Paths.get(modelDatefix.getFolderInfo_full().getFolderPath());
        Scene scene = dateFix_btn.getScene();
        sprintf("ImportImages about to start: " + p);
        new ImportImages(scene, modelDatefix.getFolderInfo_full(), model_main, false);
    }

    @FXML
    private void dateFromFileName_btn_action(ActionEvent event) {
        modelDatefix.dateFromFileName();
    }

    @FXML
    private void applyChanges_btn_action(ActionEvent event) {
        modelDatefix.acceptEverything(df_tilePane);
    }

    @FXML
    private void updateDate_btn_action(ActionEvent event) {
        warningText("updateDate_btn_action Not ready yet");
    }

    @FXML
    private void accept_dates_btn_action(ActionEvent event) {
        sprintf("accept_dates_btn_action");
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        for (Node node : modelDatefix.getSelectionModel().getSelectionList()) {
            FileInfo fileInfo = (FileInfo) node.getUserData();
            if (fileInfo != null) {
                Button accept = getAcceptButton(node);
                if (accept != null) {
                    if (!accept.isDisabled()) {
                        accept.fire();
                    }
                }
            }
        }
    }

    /*
     * Gets exifDate for selected files
     */
    @FXML
    private void restoresSelectedExifDates_btn_action(ActionEvent event) {
        modelDatefix.restoreSelectedExifDateInfos();
    }

    @FXML
    private void lastModified_date_btn_action(ActionEvent event) {
        modelDatefix.restoreLastModified();
    }

    @FXML
    private void renameFileNameWithDate_btn_action(ActionEvent event) {
        modelDatefix.renameFileNameWithDate();
    }

    @FXML
    private void close_btn_action(ActionEvent event) {
        sprintf("Close button pressed");
        Main.scene_Switcher.getWindow().getOnCloseRequest()
                .handle(new WindowEvent(Main.scene_Switcher.getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
        model_main.getMonitorExternalDriveConnectivity().restart();
    }

    private Button getAcceptButton(Node node) {
        if (node instanceof VBox) {
            if (node.getId().equals("imageFrame")) {
                for (Node node2 : ((VBox) node).getChildren()) {
                    sprintf("Node2 : " + node2);
                    if (node2 instanceof HBox) {
                        for (Node node3 : ((HBox) node2).getChildren()) {
                            sprintf("TextField: " + node3);
                            if (node3 instanceof Button button) {
                                if (button.getId().equals("accept")) {
                                    return button;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void init(ModelDatefix aModel_datefix, ModelMain aModel_main, Path currentPath, FolderInfo folderInfo,
                     boolean isImported) {
        Main.setProcessCancelled(false);

        this.model_main = aModel_main;
        this.modelDatefix = aModel_datefix;

        this.modelDatefix.setCurrentFolderPath(currentPath);
        Messages.sprintf("currentPath: " + currentPath + " isImported: " + isImported + " folderInfo SIZE:::: " + folderInfo.getFileInfoList().size());
        this.modelDatefix.setFolderInfo_full(folderInfo);
        this.modelDatefix.setTilePane(df_tilePane);

        //this.model_datefix.setGridPane(df_gridPane);
        this.modelDatefix.setScrollPane(df_scrollPane);
        this.modelDatefix.setQuickPick_tilePane(quickPick_tilePane);
        this.modelDatefix.setAnchorPane(df_anchorPane);

        if (isImported) {
            applyChanges_btn.setDefaultButton(true);
        }

        df_tilePane.getChildren().clear();

        ignored_chk.selectedProperty().bindBidirectional(this.modelDatefix.ignored_property());

        copied_chk.selectedProperty().bindBidirectional(this.modelDatefix.copied_property());

        folderize_btn.disableProperty().bind(Main.conf.drive_connected_property().not());
        copied_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
            }
        });

        this.modelDatefix.setRightInfoPanel(rightInfoPanel);

        df_tilePane.setId("dateFixer");
        df_tilePane.setHgap(3);
        df_tilePane.setVgap(3);

        df_tilePane.getChildren().clear();

        filePath_tf.setText(this.modelDatefix.getCurrentFolderPath().toString());

        dateTimeAdjusterController.init(aModel_main, aModel_datefix, df_tilePane, quickPick_tilePane);
        selectorController.init(aModel_datefix, df_tilePane);
        timeShiftController.init(aModel_datefix);

        fileOperationsController.init(aModel_datefix, aModel_main);

        sprintf("quickPick_tilePane= " + quickPick_tilePane);

        df_scrollPane.vmaxProperty().bind(df_tilePane.heightProperty());
        bad_stat.textProperty().bind(modelDatefix.getFolderInfo_full().badFiles_prop().asString());
        good_stat.textProperty().bind(modelDatefix.getFolderInfo_full().goodFiles_prop().asString());
        images_stat.textProperty().bind(modelDatefix.getFolderInfo_full().folderImageFiles_prop().asString());
        videos_stat.textProperty().bind(modelDatefix.getFolderInfo_full().folderVideoFiles_prop().asString());
        suggested_stat.textProperty().bind(modelDatefix.getFolderInfo_full().suggested_prop().asString());

        quickPick_Navigator = new QuickPick_Navigator(modelDatefix, df_scrollPane, df_tilePane, quickPick_tilePane);
        modelDatefix.setQuickPick_Navigator(quickPick_Navigator);


        Main.scene_Switcher.getWindow().setOnCloseRequest(e -> {
            sprintf("Close request pressed");
            modelDatefix.getSelector_exec().shutdownNow();
            modelDatefix.exitDateFixerWindow(modelDatefix.getTilePane(), Main.scene_Switcher.getWindow(), e);
            // TODO KORJAA TÄMÄ EXITDATEFIXERWINDOW!

        });
        modelDatefix.getSelectionModel().getSelectionList().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> c) {
                Platform.runLater(() -> {
                    selected.setText("" + modelDatefix.getSelectionModel().getSelectionList().size());
                });
            }
        });
        rightInfoPanel_scrollPane.setMinWidth(0);
        rightInfoPanel_scrollPane.setPrefWidth(0);
        rightInfoPanel_scrollPane.setMaxWidth(0);
        rightInfoPanel.setVisible(false);
        rightInfoPanel.setMinWidth(-100);
        rightInfoPanel.setMaxWidth(-100);
        rightInfoPanel.setPrefWidth(-100);

        selectorController.getInfoTables_container().setMinWidth(250);
        selectorController.getInfoTables_container().setMaxWidth(250);
        selectorController.getInfoTables_container().setPrefWidth(250);


        df_tilePane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                ContextMenu contextMenu = new ContextMenu();
                MenuItem pickDateTime_Start = createMenuItem(Main.bundle.getString("pickdateAndTimeStart"));
                MenuItem pickDateTime_End = createMenuItem(Main.bundle.getString("pickdateAndTimeEnd"));
                MenuItem openFileLocation = createMenuItem(Main.bundle.getString("openFileLocation"));

                Platform.runLater(() -> {
                    contextMenu.getItems().addAll(pickDateTime_Start, pickDateTime_End, openFileLocation);
                    modelDatefix.updateAllInfos(modelDatefix.getTilePane());
                });

                if (event.getTarget() instanceof VBox vbox && ((Node) event.getTarget()).getId().equals("imageFrame")) {
                    FileInfo fileInfo = (FileInfo) vbox.getUserData();
                    pickDateTime_Start.setOnAction(event2 -> modelDatefix.setDateTime(
                            Main.simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()), true));
                    pickDateTime_End.setOnAction(event2 -> modelDatefix.setDateTime(
                            Main.simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()), false));
                    openFileLocation.setOnAction(event2 -> {
                        Path path = Paths.get(fileInfo.getOrgPath());
                        openFileBrowser(path);
                    });
                    Platform.runLater(() -> {
                        contextMenu.show(vbox, event.getScreenX(), event.getScreenY());
                    });
                }
            }
        });
        modelDatefix.instantiateRenderVisibleNodes();
    }

    private MenuItem createMenuItem(String bundleText) {
        MenuItem menuItem = new MenuItem(bundleText);
        menuItem.getStyleClass().add("dateFixerContextMenu");
        return menuItem;
    }

    // @formatter:off
	/*
	 * ================================== Selection buttons
	 */
	// @formatter:on
    @FXML
    private void select_acceptable_image_btn_action(ActionEvent event) {
        sprintf("select_acceptable_btn_action");
        DateFixGuiUtils.selectImageFrame(modelDatefix, df_tilePane, CssStylesEnum.MODIFIED_STYLE.getStyle());

    }

    @FXML
    private void select_bad_image_btn_action(ActionEvent event) {
        sprintf("select_bad_image_btn_action");
        DateFixGuiUtils.selectImageFrame(modelDatefix, df_tilePane, CssStylesEnum.BAD_STYLE.getStyle());
    }

    @FXML
    private void select_good_video_btn_action(ActionEvent event) {
        sprintf("select_video_good_btn_action");
        DateFixGuiUtils.selectVideoImageFrame(modelDatefix, df_tilePane, CssStylesEnum.GOOD_STYLE.getStyle());
    }

    @FXML
    private void select_modified_video_btn_action(ActionEvent event) {
        sprintf("select_modified_video_btn_action");
        DateFixGuiUtils.selectVideoImageFrame(modelDatefix, df_tilePane, CssStylesEnum.MODIFIED_STYLE.getStyle());
    }


    @FXML
    private void select_acceptable_video_btn_action(ActionEvent event) {
        sprintf("select_acceptable_video_btn_action");
        DateFixGuiUtils.selectVideoImageFrame(modelDatefix, df_tilePane, CssStylesEnum.MODIFIED_STYLE.getStyle());

    }

    @FXML
    private void select_modified_image_btn_action(ActionEvent event) {
        sprintf("select_modified_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                Node hboxi = root.lookup("#fileDate");
                if (hboxi instanceof TextField) {
                    TextField tf = (TextField) hboxi;
                    if (tf != null) {
                        if (tf.getStyle().equals(CssStylesEnum.MODIFIED_STYLE.getStyle())) {
                            modelDatefix.getSelectionModel().addWithToggle(root);
                        }
                    }
                }
            }
        }

    }

    @FXML
    private void select_bad_video_btn_action(ActionEvent event) {
        sprintf("select_bad_video_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                    for (Node hbox : ((VBox) root).getChildren()) {
                        if (hbox instanceof HBox) {
                            for (Node tff : ((HBox) hbox).getChildren()) {
                                if (tff instanceof TextField tf) {
                                    if (tf.getStyle().equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                                        modelDatefix.getSelectionModel().addWithToggle(root);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void select_all_btn_action(ActionEvent event) {
        sprintf("select_all_btn_action");

        for (Node n : df_tilePane.getChildren()) {
            if ((n instanceof VBox && n.getId().equals("imageFrame"))) {
                modelDatefix.getSelectionModel().addAll(n);
            }
        }
        ObservableList<EXIF_Data_Selector> listi = modelDatefix.getCameras_TableView().getItems();

        for (EXIF_Data_Selector eds : listi) {
            eds.setIsShowing(false);
        }
    }

    @FXML
    private void select_bad_btn_action(ActionEvent event) {
        sprintf("select_bad_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            Node hboxi = root.lookup("#fileDate");
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                    if (hboxi instanceof TextField) {
                        if (hboxi.getStyle().equals(CssStylesEnum.BAD_STYLE.getStyle())) {
                            modelDatefix.getSelectionModel().addWithToggle(root);
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void select_good_image_btn_action(ActionEvent event) {
        sprintf("select_good_image_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedImage(Paths.get(fileInfo.getOrgPath()))) {
                    for (Node vbox : ((VBox) root).getChildren()) {
                        if (vbox instanceof HBox) {
                            for (Node hbox : ((HBox) vbox).getChildren()) {
                                if (hbox instanceof TextField) {
                                    if (hbox.getId().contains("fileDate")) {
                                        if (hbox.getStyle().equals(CssStylesEnum.GOOD_STYLE.getStyle())) {
                                            modelDatefix.getSelectionModel().addWithToggle(root);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void select_invert_btn_action(ActionEvent event) {
        sprintf("select_invert_btn_action");
        modelDatefix.getSelectionModel().invertSelection(df_tilePane);
    }

    @FXML
    private void select_none_btn_action(ActionEvent event) {
        Platform.runLater(() -> {
            modelDatefix.getSelectionModel().clearAll(df_tilePane);
        });

        for (EXIF_Data_Selector cam : modelDatefix.getCameras_TableView().getItems()) {
            cam.setIsShowing(false);
        }
        for (EXIF_Data_Selector date : modelDatefix.getDates_TableView().getItems()) {
            date.setIsShowing(false);
        }

        TableUtils.refreshTableContent(modelDatefix.getCameras_TableView());
        TableUtils.refreshTableContent(modelDatefix.getDates_TableView());
        TableUtils.refreshTableContent(modelDatefix.getEvents_TableView());
        TableUtils.refreshTableContent(modelDatefix.getLocations_TableView());
    }

    @FXML
    private void remove_btn_action(ActionEvent event) {
        sprintf("remove_btn_action");
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        ButtonType yes = new ButtonType(bundle.getString("yes"), ButtonData.YES);
        ButtonType no = new ButtonType(bundle.getString("no"), ButtonData.NO);
        dialog.getDialogPane().getButtonTypes().addAll(yes, no);
        dialog.setContentText(bundle.getString("doYouWantToIgnoreTheseFiles"));

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.get().getButtonData().equals(ButtonData.YES)) {
            sprintf("yes pressed!");
            boolean update = false;

            List<Node> toRemove = new ArrayList<>();
            List<FileInfo> fileInfo_toRemove = new ArrayList<>();
            for (Node n : modelDatefix.getSelectionModel().getSelectionList()) {
                sprintf("remove_btn_action setIgnored. df_tilePane.getChildren(): " + n);
                if (n instanceof VBox && n.getId().equals("imageFrame")) {
                    FileInfo fileInfo = (FileInfo) n.getUserData();
                    fileInfo.setIgnored(true);
                    toRemove.add(n);
                    fileInfo_toRemove.add(fileInfo);
                    Main.setChanged(true);
                    modelDatefix.setChanges_made(true);
                    sprintf("remove_btn_action setIgnored. df_tilePane.getChildren(): " + n);
                    update = true;
                }
            }
            if (update) {
                modelDatefix.getTilePane().getChildren().removeAll(toRemove);
                modelDatefix.getFolderInfo_full().getFileInfoList().removeAll(fileInfo_toRemove);

                DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);

            }
        } else if (result.get().getButtonData().equals(ButtonData.NO)) {
            dialog.close();
        }
    }

    // @formatter:off
	/*
	 * ============================================ Selection buttons==============
	 * ENDS
	 */

   @FXML private void hideInfoTables_btn_action(ActionEvent event) {
		if (leftInfoTables_visible.get()) {
			hideInfoTables_btn.setRotate(0);
			selectorController.getInfoTables_container().setVisible(false);
			selectorController.getInfoTables_container().setMinWidth(-100);
			selectorController.getInfoTables_container().setMaxWidth(-100);
			selectorController.getInfoTables_container().setPrefWidth(-100);
			leftInfoTables_visible.set(false);
		} else {
			hideInfoTables_btn.setRotate(180);
			selectorController.getInfoTables_container().setVisible(true);
			selectorController.getInfoTables_container().setMinWidth(250);
			selectorController.getInfoTables_container().setMaxWidth(250);
			selectorController.getInfoTables_container().setPrefWidth(250);
			leftInfoTables_visible.set(true);
		}
	}

   @FXML private void copyToMisc_btn_action(ActionEvent event) {
		if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(bundle.getString("youHaventSelectedMedia"));
			return;
		}
        if (Main.conf.getWorkDir() == null || !Main.conf.getDrive_connected()) {
            Messages.warningText(bundle.getString("cannotFindWorkDir"));
            return;
        }
		ConcurrencyUtils.stopExecThread();

		List<FileInfo> fileInfo_list = new ArrayList<>();

		for (Node n : modelDatefix.getSelectionModel().getSelectionList()) {
			if (n instanceof VBox && n.getId().equals("imageFrame")) {
				FileInfo fileInfo = (FileInfo) n.getUserData();
				Path source = Paths.get(fileInfo.getOrgPath());
				Path dest = DestinationResolver.getDestinationFileNameMisc(source, fileInfo);
				if (dest != null && Main.conf.getDrive_connected()) {
					sprintf("copyToMisc_btn_action dest is: " + dest);
					fileInfo.setWorkDir(Main.conf.getWorkDir());
					fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
					fileInfo.setDestination_Path(dest.toString());
					fileInfo.setCopied(false);
					fileInfo_list.add(fileInfo);
				} else {
					sprintf("3Dest were null. process is about to be cancelled");
					break;
				}
			} else {
                Messages.sprintf("NODE IS: " + n.toString());
            }}

		operateFilesTask(fileInfo_list);

	}

	@FXML private void hideRightInfo_btn_action(ActionEvent event) {
		if (modelDatefix.getRightInfo_visible()) {
			hideRightInfo_btn.setRotate(0);
			rightInfoPanel_scrollPane.setVisible(false);
			rightInfoPanel_scrollPane.setMinWidth(0);
			rightInfoPanel_scrollPane.setPrefWidth(0);
			rightInfoPanel_scrollPane.setMaxWidth(0);
	    	rightInfoPanel.setVisible(false);
			rightInfoPanel.setMinWidth(-100);
			rightInfoPanel.setMaxWidth(-100);
			rightInfoPanel.setPrefWidth(-100);
			modelDatefix.setRightInfo_visible(false);
		} else {
			hideRightInfo_btn.setRotate(180);
			rightInfoPanel_scrollPane.setVisible(true);
			rightInfoPanel_scrollPane.setMinWidth(250);
			rightInfoPanel_scrollPane.setPrefWidth(250);
			rightInfoPanel_scrollPane.setMaxWidth(250);
			rightInfoPanel.setVisible(true);
			rightInfoPanel.setMinWidth(250);
			rightInfoPanel.setMaxWidth(250);
			rightInfoPanel.setPrefWidth(250);
			modelDatefix.setRightInfo_visible(true);
		}
	}

    private void operateFilesTask(List<FileInfo> fileInfo_list) {
       OperateFiles operateFiles = new OperateFiles(fileInfo_list, true, model_main,
                SceneNameType.DATEFIXER.getType());
        try {
operateFiles.init();} catch (Exception e) {
    throw new RuntimeException(e);
}

   }


}
