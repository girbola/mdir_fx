package com.girbola.controllers.datefixer;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.datefixer.table.EXIF_Data_Selector;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.controllers.datefixer.utils.DateFixerControllerUtils;
import com.girbola.controllers.datefixer.utils.DestinationResolver;
import com.girbola.controllers.main.ImportImages;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.operate.OperateFiles;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.persistence.fileinfo.FileInfoSqlConnectionFactory;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import com.girbola.persistence.thumbinfo.ThumbInfoDao;
import com.girbola.utils.FileInfoUtils;
import common.utils.date.DateUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

import static com.girbola.Main.bundle;
import static com.girbola.controllers.main.tables.TableUtils.calculateDateDifferenceRatio;
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
    @FXML private Button forceReloadThumbsBtn;
	@FXML private Button hideInfoTables_btn;
	@FXML private Button hideRightInfo_btn;

    @FXML private Button moveFilesInfSourcePath;
	@FXML private Button show_all_btn;
	@FXML private Button show_only_selected_btn;

    @FXML private CheckBox copied_chk;
	@FXML private CheckBox ignored_chk;
    @FXML private CheckBox showHideImageFrameTopChk;
	@FXML private HBox infoTable_container_root;

//    @FXML private Label images_stat;
//    @FXML private Label videos_stat;

    @FXML private Button select_good_btn;
    @FXML private Button select_bad_btn;

    @FXML private Label bad_stat;
    @FXML private Label good_stat;
    @FXML private Label confirmed_stat;
	@FXML private Label suggested_stat;

    @FXML private Label bad_image_stat;
    @FXML private Label good_image_stat;
    @FXML private Label suggested_image_stat;
    @FXML private Label confirmed_image_stat;

    @FXML private Label bad_video_stat;
    @FXML private Label good_video_stat;
    @FXML private Label suggested_video_stat;
    @FXML private Label confirmed_video_stat;

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
	@FXML private Button select_confirmed_btn;
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
	//@formatter:off

    @FXML private void fileName_mi_action(ActionEvent event) {
        sortNodes();
        DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
    }

    private void sortNodes() {
        Comparator<Node> nodeComparator = DateFixerControllerUtils.compareByOrgPath();
        modelDatefix.getAllNodes().sort(nodeComparator);
        df_tilePane.getChildren().sort(nodeComparator);
    }

    @FXML private void sortByDate_mi_action(ActionEvent event) {
        Comparator<Node> nodeComparator = DateFixerControllerUtils.compareByDate();
        modelDatefix.getAllNodes().sort(nodeComparator);
        df_tilePane.getChildren().sort(nodeComparator);
        DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
    }
    @FXML private void addToUnsorted_btn_action(ActionEvent event) {
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
    @FXML private void addToAsItIs_btn_action(ActionEvent event) {
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

        for (Node n : DateFixerControllerUtils.getImageFrameNodes(modelDatefix.getSelectionModel().getSelectionList())) {
            FileInfo fileInfo = (FileInfo) n.getUserData();
            Path source = Paths.get(fileInfo.getOrgPath());
            Path dest = DestinationResolver.getDestinationFileNameAsItIs(source, fileInfo);
            sprintf("Dest: " + dest);
            if (dest != null && Main.conf.getDrive_connected()) {
                DateFixerControllerUtils.applyDestination(fileInfo, dest, Main.conf.getWorkDir(), Main.conf.getWorkDirSerialNumber());
                fileInfo_list.add(fileInfo);
            } else {
                sprintf("1Dest were null. process is about to be cancelled");
                break;
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
    @FXML private void addToBatch_btn_action(ActionEvent event) {
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

        for (Node n : DateFixerControllerUtils.getImageFrameNodes(modelDatefix.getSelectionModel().getSelectionList())) {
            FileInfo fileInfo = (FileInfo) n.getUserData();
            Path source = Paths.get(fileInfo.getOrgPath());
            Path dest = DestinationResolver.getDestinationFileNameMisc(source, fileInfo);
            if (dest != null) {
                DateFixerControllerUtils.applyDestination(fileInfo, dest, Main.conf.getWorkDir(), Main.conf.getWorkDirSerialNumber());
                fileInfo_list.add(fileInfo);
            } else {
                sprintf("2Dest were null. process is about to be cancelled");
                break;
            }
        }

        for (FileInfo fileInfo : DateFixerControllerUtils.collectImageFrameInfos(modelDatefix.getSelectionModel().getSelectionList(), false)) {
            sprintf("destination is: " + fileInfo.getDestination_Path() + " isCopied?: " + fileInfo.isCopied());
        }
        Main.setChanged(true);
    }
    @FXML private void forceReloadThumbsBtnAction(ActionEvent event) {
        // Force reload thumbs
        // Clear images from tableView by dropping thumbnail database for current folder
        boolean deleteSuccess = ThumbInfoDao.deleteThumbInfoListFromDatabase(modelDatefix.getCurrentFolderPath().toString());

        for (Node tilePane : df_tilePane.getChildren()) {
            if (tilePane instanceof VBox && tilePane.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                    for (Node imageFrame_vbox : ((VBox) tilePane).getChildren()) {
                        if (imageFrame_vbox instanceof HBox && imageFrame_vbox.getId().equals("imageViewContainer")) {
                            ImageView iv = (ImageView) imageFrame_vbox.lookup("#imageView");
                            iv.setImage(null);
                        }
                    }
            }
        }

        if (deleteSuccess) {
            sprintf("ThumbInfoDao.deleteThumbInfoListFromDatabase was successful");
            //triggerViewportUpdate(modelDatefix.getScrollPane());

        } else {
            sprintf("ThumbInfoDao.deleteThumbInfoListFromDatabase was NOT successful");
        }
    }

    public void triggerViewportUpdate(ScrollPane scrollPane) {
        double currentV = scrollPane.getVvalue();
        Node content = scrollPane.getContent();

        Messages.sprintf("[DEBUG Viewport] --- Starting Viewport Update ---");
        Messages.sprintf("[DEBUG Viewport] Initial vvalue: " + currentV);

        if (content == null) {
            Messages.sprintf("[DEBUG Viewport] ABORT: ScrollPane content is null.");
            return;
        }

        // Get content and viewport height bounds
        double contentHeight = content.getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = contentHeight - viewportHeight;

        Messages.sprintf("[DEBUG Viewport] Content Height: " + contentHeight + "px");
        Messages.sprintf("[DEBUG Viewport] Viewport Height: " + viewportHeight + "px");
        Messages.sprintf("[DEBUG Viewport] Scrollable Height: " + scrollableHeight + "px");

        if (scrollableHeight <= 0) {
            Messages.sprintf("[DEBUG Viewport] ABORT: Content fits entirely within viewport. No scroll space.");
            return;
        }

        // Calculate delta equivalent to 1 pixel in relative 0.0 - 1.0 range
        double onePixelDelta = 1.0 / scrollableHeight;
        double shift = (currentV <= 0.0) ? onePixelDelta : -onePixelDelta;
        double newV = Math.min(1.0, Math.max(0.0, currentV + shift));

        Messages.sprintf("[DEBUG Viewport] 1-Pixel Relative Delta: " + onePixelDelta);
        Messages.sprintf("[DEBUG Viewport] Calculated Nudge Target: " + newV);

        if (Double.compare(currentV, newV) == 0) {
            Messages.sprintf("[DEBUG Viewport] ABORT: Calculated new position matches current position.");
            return;
        }

        // Step 1: Execute the initial scroll nudge
        scrollPane.setVvalue(newV);
        Messages.sprintf("[DEBUG Viewport] STEP 1 EXECUTED: Set vvalue to " + scrollPane.getVvalue());

        // Step 2: Restore position on the next JavaFX pulse
        Platform.runLater(() -> {
            scrollPane.setVvalue(currentV);
            Messages.sprintf("[DEBUG Viewport] STEP 2 EXECUTED (Platform.runLater): Restored vvalue to " + scrollPane.getVvalue());
            Messages.sprintf("[DEBUG Viewport] --- Viewport Update Complete ---");
        });
    }
    @FXML private void setBadDate_btn_action(ActionEvent event) {
        sprintf("setBadDate_btn_action: ");
        for (Node node : modelDatefix.getSelectionModel().getSelectionList()) {
            if (node instanceof VBox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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
    @FXML private void setModifiedDate_btn_action(ActionEvent event) {
        sprintf("setModifiedDate_btn_action: ");
        for (Node node : modelDatefix.getSelectionModel().getSelectionList()) {
            if (node instanceof VBox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                HBox bottom = DateFixGuiUtils.getBottomHBox(node);
                sprintf("hboxi: " + bottom);
                bottom.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
            }
        }
    }
    @FXML private void show_only_selected_btn_action(ActionEvent event) {
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
//
//        if(modelDatefix.getSelectionModel().getSelectionList().size() > 0) {
//            show_only_selected_btn.setDisable(false);
//        } else {
//            show_only_selected_btn.setDisable(true);
//        }

    }

    @FXML private void show_all_btn_action(ActionEvent event) {
        Messages.sprintf("show_all_btn_action");
        String selected = "-fx-background-color: cyan;";
        String deSelected = "-fx-background-color: -fx-base;";

        if(show_all_btn.getStyle().equals(selected)) {
            show_all_btn.setStyle(deSelected);
        } else {
            show_all_btn.setStyle(selected);
        }
        modelDatefix.getSelectionModel().clearAll();
        modelDatefix.deselectAllExifDataSelectors();
        Platform.runLater(() -> {
            df_tilePane.getChildren().clear();
            df_tilePane.getChildren().addAll(modelDatefix.getAllNodes());
            DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);
        });
    }
    @FXML private void folderize_btn_action(ActionEvent event) {
        sprintf("folderize_btn_action");
        if (modelDatefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        } else {
            ConcurrencyUtils.stopExecThread();
            Parent parent = null;


            TreeMap<LocalDate, Integer> map = new TreeMap<>();

                    calculateDateDifferenceRatio(map);

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
                scene.getStylesheets().add(Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.PALETTE.getType()).toExternalForm());
                scene.getStylesheets().add(Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
                stage.show();
                stage.setOnHiding(event1-> modelDatefix.getSelectionModel().clearAll(df_tilePane));

            } catch (Exception ex) {
                log.error(ERROR, parent, event);
                Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        }

    }

    @FXML private void moveFilesInfSourcePath_action(ActionEvent event) {
        Messages.warningText("moveFilesInfSourcePath_action");

    }

    @FXML private void dateFix_btn_action(ActionEvent event) {
        Path p = Paths.get(modelDatefix.getFolderInfo_full().getFolderPath());
        Scene scene = dateFix_btn.getScene();
        sprintf("ImportImages about to start: " + p);
        new ImportImages(scene, modelDatefix.getFolderInfo_full(), model_main, false);
    }
    @FXML private void dateFromFileName_btn_action(ActionEvent event) {
        modelDatefix.dateFromFileName();
    }
    @FXML private void applyChanges_btn_action(ActionEvent event) {
        modelDatefix.acceptEverything(df_tilePane);
    }
    @FXML private void updateDate_btn_action(ActionEvent event) {
        warningText("updateDate_btn_action Not ready yet");
    }
    @FXML private void accept_dates_btn_action(ActionEvent event) {
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
    @FXML private void restoresSelectedExifDates_btn_action(ActionEvent event) {
        modelDatefix.restoreSelectedExifDateInfos();
    }
    @FXML private void lastModified_date_btn_action(ActionEvent event) {
        modelDatefix.restoreLastModified();
    }
    @FXML private void renameFileNameWithDate_btn_action(ActionEvent event) {
        modelDatefix.renameFileNameWithDate();
    }
    @FXML private void close_btn_action(ActionEvent event) {
        sprintf("Close button pressed");
        Main.sceneManager.getWindow().getOnCloseRequest()
                .handle(new WindowEvent(Main.sceneManager.getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
        model_main.getMonitorExternalDriveConnectivity().restart();
    }

    @FXML private void findSimilars_btn_action(ActionEvent event) {
        for (Node n : modelDatefix.getSelectionModel().getSelectionList()) {
            sprintf("findSimilars_btn_action .getChildren(): " + n);
            if (n instanceof VBox && n.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                FileInfo fileInfo = (FileInfo) n.getUserData();

                LocalDate localDateTimeToSearch = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
                if(localDateTimeToSearch == null) {
                  Messages.sprintfError("localDateTimeToSearch were null");
                    return;
                }

                /*

                 */
                List<String> similarFiles = new ArrayList<>();
                for(FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {

                    LocalDate localDateMin = DateUtils.parseLocalDateFromString(folderInfo.getMinDate());
                    LocalDate localDateMax = DateUtils.parseLocalDateFromString(folderInfo.getMaxDate());

                    if(localDateTimeToSearch.isAfter(localDateMin) && localDateTimeToSearch.isBefore(localDateMax)) {
                        Messages.sprintf("localDateTimeToSearch is between min and max dates");
                        similarFiles.add(folderInfo.getFolderPath());
                    }

                }

                for(FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {

                    LocalDate localDateMin = DateUtils.parseLocalDateFromString(folderInfo.getMinDate());
                    LocalDate localDateMax = DateUtils.parseLocalDateFromString(folderInfo.getMaxDate());

                    if(localDateTimeToSearch.isAfter(localDateMin) && localDateTimeToSearch.isBefore(localDateMax)) {
                        Messages.sprintf("localDateTimeToSearch is between min and max dates");
                        similarFiles.add(folderInfo.getFolderPath());
                    }

                }

                if(similarFiles.size() > 0) {
                    for(String s : similarFiles) {
                        Messages.sprintf(":::::::::::::similarFiles: " + s);
                    }
                }
            }
        }
    }

    private Button getAcceptButton(Node node) {
        if (node instanceof VBox) {
            if (node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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

        showHideImageFrameTopChk.selectedProperty().bindBidirectional(this.modelDatefix.showHideImageFrameTop_property());

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
        select_bad_btn.textProperty().bind(modelDatefix.getFolderInfo_full().badFiles_prop().asString());

        good_stat.textProperty().bind(modelDatefix.getFolderInfo_full().goodFiles_prop().asString());

        select_good_btn.textProperty().bind(modelDatefix.getFolderInfo_full().goodFiles_prop().asString());

//        images_stat.textProperty().bind(modelDatefix.getFolderInfo_full().folderImageFiles_prop().asString());
//        videos_stat.textProperty().bind(modelDatefix.getFolderInfo_full().folderVideoFiles_prop().asString());
        suggested_stat.textProperty().bind(modelDatefix.getFolderInfo_full().suggested_prop().asString());

        bad_image_stat.textProperty().bind(modelDatefix.getFolderInfo_full().badImageFiles_prop().asString());
        good_image_stat.textProperty().bind(modelDatefix.getFolderInfo_full().goodImageFiles_prop().asString());
        suggested_image_stat.textProperty().bind(modelDatefix.getFolderInfo_full().suggestedImageFiles_prop().asString());
        confirmed_image_stat.textProperty().bind(modelDatefix.getFolderInfo_full().confirmedImageFiles_prop().asString());

        bad_video_stat.textProperty().bind(modelDatefix.getFolderInfo_full().badVideoFiles_prop().asString());
        good_video_stat.textProperty().bind(modelDatefix.getFolderInfo_full().goodVideoFiles_prop().asString());
        suggested_video_stat.textProperty().bind(modelDatefix.getFolderInfo_full().suggestedVideoFiles_prop().asString()); 
        confirmed_video_stat.textProperty().bind(modelDatefix.getFolderInfo_full().confirmedVideoFiles_prop().asString());

        int imageGoodFiles = 0;
        int imageBadFiles = 0;
        int imageSuggestedFiles = 0;
        int imageConfirmedFiles = 0;
        int videoGoodFiles = 0;
        int videoBadFiles = 0;
        int videoSuggestedFiles = 0;
        int videoConfirmedFiles = 0;

        for(FileInfo fi : folderInfo.getFileInfoList()) {
            if (fi.isImage()) {
                if (fi.isGood()) {
                    imageGoodFiles++;
                }
                if (fi.isBad()) {
                    imageBadFiles++;
                }
                if (fi.isSuggested()) {
                    imageSuggestedFiles++;
                }
                if (fi.isConfirmed()) {
                    imageConfirmedFiles++;
                }
            }
            if (fi.isVideo()) {
                if (fi.isGood()) {
                    videoGoodFiles++;
                }
                if (fi.isBad()) {
                    videoBadFiles++;
                }
                if (fi.isSuggested()) {
                    videoSuggestedFiles++;
                }
                if (fi.isConfirmed()) {
                    videoConfirmedFiles++;
                }
            }
        }

        modelDatefix.getFolderInfo_full().setGoodImageFiles(imageGoodFiles);
        modelDatefix.getFolderInfo_full().setBadImageFiles(imageBadFiles);
        modelDatefix.getFolderInfo_full().setSuggestedImageFiles(imageSuggestedFiles);
        modelDatefix.getFolderInfo_full().setConfirmedImageFiles(imageConfirmedFiles);
        modelDatefix.getFolderInfo_full().setGoodVideoFiles(videoGoodFiles);
        modelDatefix.getFolderInfo_full().setBadVideoFiles(videoBadFiles);
        modelDatefix.getFolderInfo_full().setSuggestedVideoFiles(videoSuggestedFiles);
        modelDatefix.getFolderInfo_full().setConfirmedVideoFiles(videoConfirmedFiles);

        quickPick_Navigator = new QuickPick_Navigator(modelDatefix, df_scrollPane, df_tilePane, quickPick_tilePane);
        modelDatefix.setQuickPick_Navigator(quickPick_Navigator);

        Main.sceneManager.getWindow().setOnCloseRequest(e -> {
            sprintf("Close request pressed");
            modelDatefix.getSelector_exec().shutdownNow();
            modelDatefix.exitDateFixerWindow(modelDatefix.getTilePane(), Main.sceneManager.getWindow(), e);
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

                MenuItem openItem = createMenuItem(Main.bundle.getString("open"));

                MenuItem selectAll = createMenuItem(Main.bundle.getString("selectAll"));

                SeparatorMenuItem selectionSeparator = new SeparatorMenuItem();

                MenuItem pickDateTime_Start = createMenuItem(Main.bundle.getString("pickdateAndTimeStart"));
                MenuItem pickDateTime_End = createMenuItem(Main.bundle.getString("pickdateAndTimeEnd"));
                MenuItem openFileLocation = createMenuItem(Main.bundle.getString("openFileLocation"));

                Platform.runLater(() -> {
                    contextMenu.getItems().addAll(
                            openItem,
                            selectAll,
                            selectionSeparator,
                            pickDateTime_Start,
                            pickDateTime_End,
                            openFileLocation);
                    modelDatefix.updateAllInfos(modelDatefix.getTilePane());
                });

                if (event.getTarget() instanceof VBox vbox && ((Node) event.getTarget()).getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                    FileInfo fileInfo = (FileInfo) vbox.getUserData();
                    openItem.setOnAction(event2 -> openFileBrowser(Paths.get(fileInfo.getOrgPath())));

                    selectAll.setOnAction(event2 -> modelDatefix.selectAll(df_tilePane, fileInfo));
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
    // All ===============================================================================
    @FXML private void remove_btn_action(ActionEvent event) {
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

        if (result.isPresent() && result.get().getButtonData().equals(ButtonData.YES)) {
            sprintf("yes pressed!");
            boolean update = false;

            List<Node> toRemove = new ArrayList<>();
            List<FileInfo> fileInfo_toRemove = new ArrayList<>();
            for (Node n : DateFixerControllerUtils.getImageFrameNodes(modelDatefix.getSelectionModel().getSelectionList())) {
                FileInfo fileInfo = (FileInfo) n.getUserData();
                fileInfo.setIgnored(true);
                toRemove.add(n);
                fileInfo_toRemove.add(fileInfo);
                Main.setChanged(true);
                modelDatefix.setChanges_made(true);
                update = true;
            }
            if (update) {
                for (Node node : toRemove) {
                    while (modelDatefix.getSelectionModel().contains(node)) {
                        modelDatefix.getSelectionModel().remove(node);
                    }
                }
                modelDatefix.getTilePane().getChildren().removeAll(toRemove);
                modelDatefix.getAllNodes().removeAll(toRemove);
                modelDatefix.getFolderInfo_full().getFileInfoList().removeAll(fileInfo_toRemove);

                DateFixLoadingProcessLoader.reNumberTheFrames(modelDatefix);

            }
        }
    }
    @FXML private void select_all_btn_action(ActionEvent event) {
        sprintf("select_all_btn_action");

        for (Node n : df_tilePane.getChildren()) {
            if ((n instanceof VBox && n.getId().equals(DateFixConstants.IMAGEFRAME.getType()))) {
                modelDatefix.getSelectionModel().addAll(n);
            }
        }
        ObservableList<EXIF_Data_Selector> listi = modelDatefix.getCameras_TableView().getItems();

        for (EXIF_Data_Selector eds : listi) {
            eds.setIsShowing(false);
        }
    }
    @FXML private void select_none_btn_action(ActionEvent event) {
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
    @FXML private void select_invert_btn_action(ActionEvent event) {
        sprintf("select_invert_btn_action");
        modelDatefix.getSelectionModel().invertSelection(df_tilePane);
    }

    // Separtely selecting ===============================================================

    @FXML private void select_good_btn_action(ActionEvent event) {
        sprintf("select_good_btn_action");
        DateFixGuiUtils.selectAnyMediaFrame(modelDatefix, df_tilePane, CssStylesEnum.GOOD_STYLE.getStyle());
    }

    @FXML private void select_bad_btn_action(ActionEvent event) {
        sprintf("select_bad_btn_action");
        DateFixGuiUtils.selectAnyMediaFrame(modelDatefix, df_tilePane, CssStylesEnum.BAD_STYLE.getStyle());
    }
    @FXML private void select_modified_btn_action(ActionEvent event) {
        sprintf("select_modified_btn_action");
        DateFixGuiUtils.selectAnyMediaFrame(modelDatefix, df_tilePane, CssStylesEnum.MODIFIED_STYLE.getStyle());
    }
    @FXML private void select_confirmed_btn_action(ActionEvent event) {
        sprintf("select_confirmed_btn_action");
        DateFixGuiUtils.selectAnyMediaFrame(modelDatefix, df_tilePane, CssStylesEnum.CONFIRMED_STYLE.getStyle());
    }

    // Images ============================================================================
    @FXML private void select_good_image_btn_action(ActionEvent event) {
        sprintf("select_good_image_btn_action");
        DateFixGuiUtils.selectImageFrame(modelDatefix, df_tilePane, CssStylesEnum.GOOD_STYLE.getStyle());
    }
    @FXML private void select_bad_image_btn_action(ActionEvent event) {
        sprintf("select_bad_image_btn_action");
        DateFixGuiUtils.selectImageFrame(modelDatefix, df_tilePane, CssStylesEnum.BAD_STYLE.getStyle());
    }
    @FXML private void select_modified_image_btn_action(ActionEvent event) {
        sprintf("select_modified_image_btn_action");
        DateFixGuiUtils.selectImageFrame(modelDatefix, df_tilePane, CssStylesEnum.MODIFIED_STYLE.getStyle());
    }
    @FXML private void select_confirmed_image_btn_action(ActionEvent event) {
        sprintf("select_confirmed_image_btn_action");
        DateFixGuiUtils.selectImageFrame(modelDatefix, df_tilePane, CssStylesEnum.CONFIRMED_STYLE.getStyle());
    }

    // Videos ===========================================================================
    @FXML private void select_good_video_btn_action(ActionEvent event) {
        sprintf("select_video_good_btn_action");
        DateFixGuiUtils.selectVideoFrame(modelDatefix, df_tilePane, CssStylesEnum.GOOD_STYLE.getStyle());
    }
    @FXML private void select_bad_video_btn_action(ActionEvent event) {
        sprintf("select_bad_video_btn_action");
        DateFixGuiUtils.selectVideoFrame(modelDatefix, df_tilePane, CssStylesEnum.BAD_STYLE.getStyle());
    }
    @FXML private void select_modified_video_btn_action(ActionEvent event) {
        sprintf("select_modified_video_btn_action");
        DateFixGuiUtils.selectVideoFrame(modelDatefix, df_tilePane, CssStylesEnum.MODIFIED_STYLE.getStyle());
    }
    @FXML private void select_confirmed_video_btn_action(ActionEvent event) {
        sprintf("select_confirmed_video_btn_action");
        DateFixGuiUtils.selectVideoFrame(modelDatefix, df_tilePane, CssStylesEnum.CONFIRMED_STYLE.getStyle());
    }

//  ===================================================== Selection buttons============== ENDS

   @FXML private void hideInfoTables_btn_action(ActionEvent event) {
        boolean show = !leftInfoTables_visible.get();
        hideInfoTables_btn.setRotate(DateFixerControllerUtils.arrowRotation(show));
        selectorController.getInfoTables_container().setVisible(show);
        DateFixerControllerUtils.setFixedWidth(selectorController.getInfoTables_container(),
                DateFixerControllerUtils.panelWidth(show, 250, -100));
        leftInfoTables_visible.set(show);
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

		for (Node n : DateFixerControllerUtils.getImageFrameNodes(modelDatefix.getSelectionModel().getSelectionList())) {
			FileInfo fileInfo = (FileInfo) n.getUserData();
			if (fileInfo.isIgnored()) {
				continue;
			}
			Path source = Paths.get(fileInfo.getOrgPath());
			Path dest = DestinationResolver.getDestinationFileNameMisc(source, fileInfo);
			if (dest != null && Main.conf.getDrive_connected()) {
				DateFixerControllerUtils.applyDestination(fileInfo, dest, Main.conf.getWorkDir(), Main.conf.getWorkDirSerialNumber());
				fileInfo_list.add(fileInfo);
			} else {
				sprintf("3Dest were null. process is about to be cancelled");
				break;
			}
		}

		operateFilesTask(fileInfo_list);

	}

    @FXML private void hideRightInfo_btn_action(ActionEvent event) {
        boolean show = !modelDatefix.getRightInfo_visible();
        hideRightInfo_btn.setRotate(DateFixerControllerUtils.arrowRotation(show));
        rightInfoPanel_scrollPane.setVisible(show);
        DateFixerControllerUtils.setFixedWidth(rightInfoPanel_scrollPane,
                DateFixerControllerUtils.panelWidth(show, 250, 0));
        rightInfoPanel.setVisible(show);
        DateFixerControllerUtils.setFixedWidth(rightInfoPanel,
                DateFixerControllerUtils.panelWidth(show, 250, -100));
        modelDatefix.setRightInfo_visible(show);
	}

    private void operateFilesTask(List<FileInfo> fileInfo_list) {
       OperateFiles operateFiles = new OperateFiles(fileInfo_list, true, model_main,
                SceneNameType.DATEFIXER.getType());
        try {
            operateFiles.init();
        } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
// @formatter:on
}
