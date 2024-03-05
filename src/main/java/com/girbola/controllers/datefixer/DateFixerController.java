/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.ImportImages;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.DestinationResolver;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

public class DateFixerController {

    private org.slf4j.Logger log = LoggerFactory.getLogger(DateFixerController.class);

    private static final String ERROR = DateFixerController.class.getSimpleName();
    // private boolean infoTables_visible = true;
    private SimpleBooleanProperty leftInfoTables_visible = new SimpleBooleanProperty(true);

    private QuickPick_Navigator quickPick_Navigator;
    private Model_datefix model_datefix;
    private Model_main model_main;

    //@formatter:off
	@FXML private Button cameras_hide_Deselected_btn;
	@FXML private Button cameras_show_all_btn;
	@FXML private MenuButton move_menuBtn;
	@FXML private TableView<MetaData> metaDataTableView;
	@FXML private CheckBox ignored_chk;
	@FXML private CheckBox copied_chk;
	@FXML private HBox infoTable_container_root;
	@FXML private VBox vbox_image_test2;
	@FXML private VBox vbox_image_test21;
	@FXML private VBox rightInfoPanel;
	@FXML private AnchorPane df_anchorPane;
	@FXML private TilePane df_tilePane;
	@FXML private ScrollPane df_scrollPane;
	@FXML private TextField filePath_tf;
	@FXML private TilePane quickPick_tilePane;
	@FXML private VBox vbox_image_test;
	@FXML private VBox vbox_image_test1;
	@FXML private Label bad_stat;
	@FXML private Label confirmed_stat;
	@FXML private Label good_stat;
	@FXML private Label images_stat;
	@FXML private Label suggested_stat;
	@FXML private Label videos_stat;
	@FXML private Button hideRightInfo_btn;
	@FXML private Button hideInfoTables_btn;
	@FXML private Button applyChanges_btn;
	@FXML private Button addToBatch_btn;
	@FXML private Button close_btn;
	@FXML private Button dateFix_btn;
	@FXML private Button folderize_btn;

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
	@FXML private Button listFileInfo_btn;
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
	@FXML private TextField startFromNumber_tf;
	@FXML private TextField endToNumber_tf;

	//@formatter:on
    @FXML
    private void listFileInfo_btn_action(ActionEvent event) {

    }

    @FXML
    private void selectRangeOfNumbers_btn_action(ActionEvent event) {

        int startFrom = Integer.parseInt(startFromNumber_tf.getText().trim());
        int endTo = Integer.parseInt(endToNumber_tf.getText().trim());

        model_datefix.getSelectionModel().clearAll();
        model_datefix.getTilePane().getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(node -> (VBox) node)
                .filter(vbox -> vbox.getId().equals("imageFrame"))
                .forEach(vbox -> vbox.getChildren().stream()
                        .filter(hbox -> hbox instanceof StackPane)
                        .forEach(hbox -> ((StackPane) hbox).getChildren().stream()
                                .filter(stp -> stp instanceof Label)
                                .map(stp -> (Label) stp)
                                .map(Label::getText)
                                .map(this::getInteger)
                                .filter(number -> number >= 0 && startFrom <= number && endTo >= number)
                                .forEach(number -> model_datefix.getSelectionModel().addOnly(vbox)))
                );
//
//        try {
//            sprintf("startFromNumber_tf.getText(): " + startFromNumber_tf.getText());
//            int start = Integer.parseInt(startFromNumber_tf.getText());
//
//            sprintf("Start will be: " + start);
//            startFromNumber_tf.getStyleClass().remove("notValidNumber");
//        } catch (Exception e) {
//            startFromNumber_tf.getStyleClass().add("notValidNumber");
//        }
//
//        try {
//            sprintf("endToNumber_tf.getText(): " + endToNumber_tf.getText());
//            int end = Integer.parseInt(endToNumber_tf.getText());
//            endToNumber_tf.getStyleClass().remove("notValidNumber");
//            sprintf("End will be: " + end);
//        } catch (Exception e) {
//            endToNumber_tf.getStyleClass().add("notValidNumber");
//        }
    }

    private int getInteger(String value) {

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }

    }

    @FXML
    private void fileName_mi_action(ActionEvent event) {
        model_datefix.getAllNodes().sort(new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                VBox vbox1 = Node_Methods.getImageFrameNode(o1, "imageFrame");
                VBox vbox2 = Node_Methods.getImageFrameNode(o2, "imageFrame");
                FileInfo fileInfo1 = (FileInfo) vbox1.getUserData();
                FileInfo fileInfo2 = (FileInfo) vbox2.getUserData();

                return fileInfo1.getOrgPath().compareTo(fileInfo2.getOrgPath());
            }
        });

        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        Task<ObservableList<Node>> updateGridPane_Task = new UpdateGridPane_Task(model_datefix,
                model_datefix.getAllNodes(), loadingProcess_task);

        loadingProcess_task.setTask(updateGridPane_Task);

        Thread thread = new Thread(updateGridPane_Task, "updateGridPane_Task_th");
        thread.start();

    }

    @FXML
    private void sortByDate_mi_action(ActionEvent event) {
        model_datefix.getAllNodes().sort(new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                VBox vbox1 = Node_Methods.getImageFrameNode(o1, "imageFrame");
                VBox vbox2 = Node_Methods.getImageFrameNode(o2, "imageFrame");
                FileInfo fileInfo1 = (FileInfo) vbox1.getUserData();
                FileInfo fileInfo2 = (FileInfo) vbox2.getUserData();
                if (fileInfo1.getDate() > (fileInfo2.getDate())) {
                    return 1;
                } else if (fileInfo1.getDate() < (fileInfo2.getDate())) {
                    return -1;
                } else {
                    return 0;
                }

            }
        });

        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        Task<ObservableList<Node>> updateGridPane_Task = new UpdateGridPane_Task(model_datefix,
                model_datefix.getAllNodes(), loadingProcess_task);

        loadingProcess_task.setTask(updateGridPane_Task);

        Thread thread = new Thread(updateGridPane_Task, "updateGridPane_Task_th");
        thread.start();

    }

    @FXML
    private void addToUnsorted_btn_action(ActionEvent event) {
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            warningText(bundle.getString("workDirHasNotConnected"));
            return;
        }
        ConcurrencyUtils.stopExecThread();

        warningText("Not ready yet!");
    }

    @FXML
    private void addToAsItIs_btn_action(ActionEvent event) {
//		addToAsItIs
        // Copy files to workdir root as it is. If folder name is empty files will be
        // under Unsorted
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            warningText(bundle.getString("workDirHasNotConnected"));
            return;
        }
        ConcurrencyUtils.stopExecThread();

//		Messages.warningText("Not ready yet!");
        List<FileInfo> fileInfo_list = new ArrayList<>();

        for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
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
                    sprintf("Dest were null. process is about to be cancelled");
                    break;
                }
            }
        }

        Task<Boolean> operateFiles = new OperateFiles(fileInfo_list, true, model_main,
                SceneNameType.DATEFIXER.getType());
        operateFiles.setOnSucceeded((workerStateEvent) -> {
//			operateFiles.get
            sprintf("operateFiles Succeeded");
        });
        operateFiles.setOnCancelled((workerStateEvent) -> {
            sprintf("operateFiles CANCELLED");
        });
        operateFiles.setOnFailed((workerStateEvent) -> {
            sprintf("operateFiles FAILED");
            Main.setProcessCancelled(true);
        });
        Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
        operateFiles_th.setDaemon(true);
        operateFiles_th.start();
    }

    @FXML
    private void addToBatch_btn_action(ActionEvent event) {
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        if (!Files.exists(Paths.get(Main.conf.getWorkDir()))) {
            warningText(bundle.getString("workDirHasNotConnected"));
            return;
        }
        ConcurrencyUtils.stopExecThread();

        List<FileInfo> fileInfo_list = new ArrayList<>();

        for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
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
                    sprintf("Dest were null. process is about to be cancelled");
                    break;
                }
            }
        }

        for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
            if (n instanceof VBox && n.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) n.getUserData();
                sprintf(
                        "destination is: " + fileInfo.getDestination_Path() + " isCopied?: " + fileInfo.isCopied());
            }
        }
        Main.setChanged(true);
    }

    @FXML
    private void setBadDate_btn_action(ActionEvent event) {
        sprintf("setBadDate_btn_action: ");
        for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
            sprintf("video_bad_btn_action: " + node.getId());

            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                Node hboxi = node.lookup("#fileDate");
                sprintf("hboxi: " + hboxi);
                if (hboxi instanceof TextField) {
                    FileInfo fileInfo = (FileInfo) node.getUserData();
                    // model_datefix.getSelectionModel().add(node);
                    FileInfoUtils.setBad(fileInfo);
                    hboxi.setStyle(CssStylesController.getBad_style());
                }
            }
        }

    }

    @FXML
    private void setModifiedDate_btn_action(ActionEvent event) {
        sprintf("setBadDate_btn_action: ");
        for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
            sprintf("video_bad_btn_action: " + node.getId());

            if (node instanceof VBox && node.getId().equals("imageFrame")) {
                Node hboxi = node.lookup("#fileDate");
                sprintf("hboxi: " + hboxi);
                if (hboxi instanceof TextField) {
                    FileInfo fileInfo = (FileInfo) node.getUserData();
                    // model_datefix.getSelectionModel().add(node);
                    TextField tf = (TextField) hboxi;
                    // FileInfoUtils.setBad(fileInfo);(fileInfo);
                    tf.setStyle(CssStylesController.getModified_style());
                }
            }
        }
    }

    @FXML
    private void cameras_hide_Deselected_btn_action(ActionEvent event) {
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        sprintf("cameras_hide_Deselected_btn_action starting");
//		LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
//		loadingProcess_task.setTask(null);
//		UpdateGridPane_Task.updateGridPaneContent(model_datefix, model_datefix.getSelectionModel().getSelectionList(),
//				loadingProcess_task);

        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        Task<ObservableList<Node>> updateGridPane_Task = new UpdateGridPane_Task(model_datefix,
                model_datefix.getSelectionModel().getSelectionList(), loadingProcess_task);

        loadingProcess_task.setTask(updateGridPane_Task);

        Thread thread = new Thread(updateGridPane_Task, "updateGridPane_Task_th");
        thread.start();

    }

    @FXML
    private void cameras_show_all_btn_action(ActionEvent event) {
        sprintf("cameras_show_all_btn_action starting");
        model_datefix.getSelectionModel().clearAll();
        model_datefix.deselectAllExifDataSelectors();
//		LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
//		UpdateGridPane_Task.updateGridPaneContent(model_datefix, model_datefix.getAllNodes(), loadingProcess_task);

        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        Task<ObservableList<Node>> updateGridPane_Task = new UpdateGridPane_Task(model_datefix,
                model_datefix.getAllNodes(), loadingProcess_task);

        loadingProcess_task.setTask(updateGridPane_Task);

        Thread thread = new Thread(updateGridPane_Task, "updateGridPane_Task_th");
        thread.start();

        //
        // AddToGridPane2 apg2 = new AddToGridPane2(model_datefix,
        // model_datefix.getAllNodes(), lp);
        // new Thread(apg2).run();

    }

    @FXML
    private void folderize_btn_action(ActionEvent event) {
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        } else {
            ConcurrencyUtils.stopExecThread();
            Parent parent = null;
            // rgwerg;
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
                askEventDialogController.init(model_main, model_datefix);

                Scene scene = new Scene(parent);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
                stage.setOnHiding(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        model_datefix.getSelectionModel().clearAll();
                    }
                });
//				for (FileInfo fileInfo : model_main.getWorkDir_Handler().getWorkDir_List()) {
//					Messages.sprintf("===========WORKDIR::::: FileInfo: " + fileInfo.getOrgPath());
//				}
            } catch (Exception ex) {
                log.error(ERROR, parent, event);
                Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
        }

    }

    @FXML
    private void dateFix_btn_action(ActionEvent event) {
        Path p = Paths.get(model_datefix.getFolderInfo_full().getFolderPath());
        Scene scene = dateFix_btn.getScene();
        sprintf("ImportImages about to start: " + p);
        ImportImages importImages = new ImportImages(scene, model_datefix.getFolderInfo_full(), model_main, false);
        // asc;
        // DateFixer dateFixer = new DateFixer(scene,
        // Paths.get(model_datefix.getFolderInfo_full().getFolderPath()),
        // model_datefix.getFolderInfo_full(), model_main);
        // Thread dateFixer_th = new Thread(dateFixer, "dateFixer_th");
        // sprintf("dateFixer_th.getName(): " + dateFixer_th.getName());
        // dateFixer_th.run();
    }

    @FXML
    private void dateFromFileName_btn_action(ActionEvent event) {
        model_datefix.dateFromFileName();
    }

    @FXML
    private void applyChanges_btn_action(ActionEvent event) {
        model_datefix.acceptEverything(df_tilePane);
    }

    @FXML
    private void updateDate_btn_action(ActionEvent event) {
        warningText("updateDate_btn_action Not ready yet");
    }

    @FXML
    private void accept_dates_btn_action(ActionEvent event) {
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
            warningText(bundle.getString("youHaventSelectedMedia"));
            return;
        }
        for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
            sprintf("Node is: " + node.getId() + " NODE ALL INFO: " + node.toString());
            FileInfo fileInfo = (FileInfo) node.getUserData();
            if (fileInfo != null) {
                File file = new File(fileInfo.getOrgPath());
                // long date = DateTaken.getMetadataDateTaken(file.toPath());
                Button accept = getAcceptButton(node);
                if (accept != null) {
                    if (!accept.isDisabled()) {
                        accept.fire();
                    }
                }

                sprintf("Node name is: " + node + " LastMod was: " + DateUtils.longToLocalDateTime(file.lastModified())
                        .format(Main.simpleDates.getDtf_ymd_hms_minusDots_default()));
            }
        }
    }

    /*
     * Gets exifDate for selected files
     */
    @FXML
    private void restoresSelectedExifDates_btn_action(ActionEvent event) {
        model_datefix.restoreSelectedExifDateInfos();
    }

    @FXML
    private void lastModified_date_btn_action(ActionEvent event) {
        model_datefix.restoreLastModified();
    }

    @FXML
    private void renameFileNameWithDate_btn_action(ActionEvent event) {
        model_datefix.renameFileNameWithDate();
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
                            if (node3 instanceof Button) {
                                Button button = (Button) node3;
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

    public void init(Model_datefix aModel_datefix, Model_main aModel_main, Path currentPath, FolderInfo folderInfo,
                     boolean isImported) {
        Main.setProcessCancelled(false);

        this.model_main = aModel_main;
        this.model_datefix = aModel_datefix;

        this.model_datefix.setCurrentFolderPath(currentPath);
        this.model_datefix.setFolderInfo_full(folderInfo);
        this.model_datefix.setTilePane(df_tilePane);
        sprintf(model_datefix.getTilePane() == null ? "NUUUUU" : model_datefix.getTilePane().getId());

        //this.model_datefix.setGridPane(df_gridPane);
        this.model_datefix.setScrollPane(df_scrollPane);
        this.model_datefix.setQuickPick_tilePane(quickPick_tilePane);
        this.model_datefix.setAnchorPane(df_anchorPane);
        if (isImported) {
            applyChanges_btn.setDefaultButton(true);
        }
//		df_scrollPane.onScrollStartedProperty().addListener((InvalidationListener) new ChangeListener<Number>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				
//			}
//		});

        ignored_chk.selectedProperty().bindBidirectional(this.model_datefix.ignored_property());

        copied_chk.selectedProperty().bindBidirectional(this.model_datefix.copied_property());

        folderize_btn.disableProperty().bind(Main.conf.drive_connected_property().not());
        copied_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    LoadingProcess();

                } else {
                    LoadingProcess();
                }
            }
        });
//		ignored_chk.selectedProperty().addListener(new ChangeListener<Boolean>() {
//			@Override
//			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
//				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
//						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);
//			}
//		});

        this.model_datefix.setRightInfoPanel(rightInfoPanel);

        df_tilePane.setId("dateFixer");
        df_tilePane.getChildren().clear();

//        df_gridPane.setId("dateFixer");
//        df_gridPane.getChildren().clear();
//        df_gridPane.getRowConstraints().removeAll();
//        df_gridPane.getColumnConstraints().removeAll();
        filePath_tf.setText(this.model_datefix.getCurrentFolderPath().toString());

        dateTimeAdjusterController.init(aModel_main, aModel_datefix, df_tilePane, quickPick_tilePane);
        selectorController.init(aModel_datefix, df_tilePane);
        timeShiftController.init(aModel_datefix);

        fileOperationsController.init(aModel_datefix, aModel_main);

        sprintf("quickPick_tilePane= " + quickPick_tilePane);
//        df_gridPane.heightProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                sprintf("grid height: " + newValue);
//            }
//        });
        df_scrollPane.vmaxProperty().bind(df_tilePane.heightProperty());
        bad_stat.textProperty().bind(model_datefix.getFolderInfo_full().badFiles_prop().asString());
        good_stat.textProperty().bind(model_datefix.getFolderInfo_full().goodFiles_prop().asString());
        images_stat.textProperty().bind(model_datefix.getFolderInfo_full().folderImageFiles_prop().asString());
        videos_stat.textProperty().bind(model_datefix.getFolderInfo_full().folderVideoFiles_prop().asString());
        suggested_stat.textProperty().bind(model_datefix.getFolderInfo_full().suggested_prop().asString());

        quickPick_Navigator = new QuickPick_Navigator(model_datefix, df_scrollPane, df_tilePane, quickPick_tilePane);
        model_datefix.setQuickPick_Navigator(quickPick_Navigator);
        model_datefix.instantiateRenderVisibleNodes();

        Main.scene_Switcher.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                sprintf("Close request pressed");
                model_datefix.getSelector_exec().shutdownNow();
                model_datefix.exitDateFixerWindow(model_datefix.getTilePane(), Main.scene_Switcher.getWindow(), event);
                // TODO KORJAA TÄMÄ EXITDATEFIXERWINDOW!

                // event.consume();
            }
        });
        model_datefix.getSelectionModel().getSelectionList().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> c) {
                selected.setText("" + model_datefix.getSelectionModel().getSelectionList().size());
            }
        });
        /*
         * info_column.setCellValueFactory((TableColumn.CellDataFeatures< MetaData,
         * String> cellData) -> new
         * SimpleObjectProperty<>(cellData.getValue().getTag()));
         * value_column.setCellValueFactory( (TableColumn.CellDataFeatures<MetaData,
         * String> cellData) -> new
         * SimpleObjectProperty<>(cellData.getValue().getValue()));
         */
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

        startFromNumber_tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    startFromNumber_tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        endToNumber_tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    endToNumber_tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    public void LoadingProcess() {

        LoadingProcessTask loadingProcess_task = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        Task<ObservableList<Node>> updateGridPane_Task = new UpdateGridPane_Task(model_datefix,
                model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);

        loadingProcess_task.setTask(updateGridPane_Task);

        Thread thread = new Thread(updateGridPane_Task, "updateGridPane_Task_th");
        thread.start();
    }

    // @formatter:off
	/*
	 * ========================================================= Selection buttons
	 */
	// @formatter:on
    @FXML
    private void select_acceptable_btn_action(ActionEvent event) {
        sprintf("select_acceptable_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            Node hboxi = root.lookup("#fileDate");
            if (hboxi instanceof TextField) {
                if (hboxi.getStyle().equals(CssStylesController.getSuggested_style())) {
                    model_datefix.getSelectionModel().addWithToggle(root);
                }
            }
        }

    }

    @FXML
    private void select_video_bad_btn_action(ActionEvent event) {
        sprintf("select_video_bad_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            sprintf("video_good_btn_action: " + root);
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                    if (fileInfo.isBad()) {
                        model_datefix.getSelectionModel().addWithToggle(root);
                    }
                }
            }
        }
    }

    @FXML
    private void select_video_good_btn_action(ActionEvent event) {
        sprintf("select_video_good_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                    if (fileInfo.isGood()) {
                        model_datefix.getSelectionModel().addWithToggle(root);
                    }
                }
            }
        }
    }

    @FXML
    private void select_modified_video_btn_action(ActionEvent event) {
        sprintf("select_modified_video_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                    Node hboxi = root.lookup("#fileDate");
                    if (hboxi instanceof TextField) {
                        TextField tf = (TextField) hboxi;
                        if (tf != null) {
                            if (tf.getStyle().equals(CssStylesController.getModified_style())) {
                                model_datefix.getSelectionModel().addWithToggle(root);
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void select_modified_btn_action(ActionEvent event) {
        sprintf("select_modified_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
//				FileInfo fileInfo = (FileInfo) root.getUserData();
                Node hboxi = root.lookup("#fileDate");
                if (hboxi instanceof TextField) {
                    TextField tf = (TextField) hboxi;
                    if (tf != null) {
                        if (tf.getStyle().equals(CssStylesController.getModified_style())) {
                            model_datefix.getSelectionModel().addWithToggle(root);
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
            sprintf("video_bad_btn_action: " + root);
            if (root instanceof VBox && root.getId().equals("imageFrame")) {
                FileInfo fileInfo = (FileInfo) root.getUserData();
                if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                    for (Node hbox : ((VBox) root).getChildren()) {
                        if (hbox instanceof HBox) {
                            for (Node tff : ((HBox) hbox).getChildren()) {
                                if (tff instanceof TextField) {
                                    TextField tf = (TextField) tff;
                                    if (tf != null) {
                                        if (tf.getStyle().equals(CssStylesController.getBad_style())) {
                                            model_datefix.getSelectionModel().addWithToggle(root);
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
    private void select_all_btn_action(ActionEvent event) {
        sprintf("select_all_btn_action");

        df_tilePane.getChildren().stream().filter((n) -> (n instanceof VBox && n.getId().equals("imageFrame")))
                .forEachOrdered((n) -> {
                    model_datefix.getSelectionModel().addAll(n);
                });
//		FolderInfo fo = model_datefix.getFolderInfo_full();
//		for (FileInfo fileInfo : fo.getFileInfoList()) {
//			fileInfo.toString();
//		}
        ObservableList<EXIF_Data_Selector> listi = model_datefix.getCameras_TableView().getItems();

        for (EXIF_Data_Selector eds : listi) {
            eds.setIsShowing(false);
        }
    }

    @FXML
    private void select_bad_btn_action(ActionEvent event) {
        sprintf("select_bad_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            Node hboxi = root.lookup("#fileDate");
            if (hboxi instanceof TextField) {
                if (hboxi.getStyle().equals(CssStylesController.getBad_style())) {
                    model_datefix.getSelectionModel().addWithToggle(root);
                }
            }
        }
    }

    @FXML
    private void select_good_btn_action(ActionEvent event) {
        sprintf("select_good_btn_action");
        for (Node root : df_tilePane.getChildren()) {
            if (root instanceof VBox) {
                for (Node vbox : ((VBox) root).getChildren()) {
                    if (vbox instanceof HBox) {
                        for (Node hbox : ((HBox) vbox).getChildren()) {
                            if (hbox instanceof TextField) {
                                if (hbox.getId().contains("fileDate")) {
                                    if (hbox.getStyle().equals(CssStylesController.getGood_style())) {
                                        model_datefix.getSelectionModel().addWithToggle(root);
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
        model_datefix.getSelectionModel().invertSelection(df_tilePane);
    }

    @FXML
    private void select_none_btn_action(ActionEvent event) {
        model_datefix.getSelectionModel().clearAll();
        for (EXIF_Data_Selector cam : model_datefix.getCameras_TableView().getItems()) {
            cam.setIsShowing(false);
        }
        for (EXIF_Data_Selector date : model_datefix.getDates_TableView().getItems()) {
            date.setIsShowing(false);
        }
        TableUtils.refreshTableContent(model_datefix.getCameras_TableView());
        TableUtils.refreshTableContent(model_datefix.getDates_TableView());
        TableUtils.refreshTableContent(model_datefix.getEvents_TableView());
        TableUtils.refreshTableContent(model_datefix.getLocations_TableView());
    }

    @FXML
    private void remove_btn_action(ActionEvent event) {
        sprintf("remove_btn_action");
        if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
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
            for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
                sprintf("remove_btn_action setIgnored. df_tilePane.getChildren(): " + n);
                if (n instanceof VBox && n.getId().equals("imageFrame")) {
                    FileInfo fileInfo = (FileInfo) n.getUserData();
                    fileInfo.setIgnored(true);
                    toRemove.add(n);
                    fileInfo_toRemove.add(fileInfo);
                    Main.setChanged(true);
                    model_datefix.setChanges_made(true);
                    sprintf("remove_btn_action setIgnored. df_tilePane.getChildren(): " + n);
                    update = true;
                }
            }
            if (update) {
                model_datefix.getTilePane().getChildren().removeAll(toRemove);
                model_datefix.getFolderInfo_full().getFileInfoList().removeAll(fileInfo_toRemove);

//				LoadingProcess_Task loadingProcess_task = new LoadingProcess_Task(Main.scene_Switcher.getWindow());
//				UpdateGridPane_Task.updateGridPaneContent(model_datefix,
//						model_datefix.filterAllNodesList(model_datefix.getAllNodes()), loadingProcess_task);

                LoadingProcess();

            } else {
                sprintf("Nothing to update");
            }

        } else if (result.get().getButtonData().equals(ButtonData.NO)) {
            sprintf("no pressed!");
            dialog.close();
        }
    }

    // @formatter:off
	/*
	 * ============================================ Selection buttons==============
	 * ENDS
	 */

	@FXML
	private void hideInfoTables_btn_action(ActionEvent event) {
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

	@FXML
	private void copyToMisc_btn_action(ActionEvent event) {
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(bundle.getString("youHaventSelectedMedia"));
			return;
		}
		ConcurrencyUtils.stopExecThread();

		List<FileInfo> fileInfo_list = new ArrayList<>();

		for (Node n : model_datefix.getSelectionModel().getSelectionList()) {
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
					sprintf("Dest were null. process is about to be cancelled");
					break;
				}
			}
		}

		Task<Boolean> operateFiles = new OperateFiles(fileInfo_list, true, model_main,
				SceneNameType.DATEFIXER.getType());
		operateFiles.setOnSucceeded((workerStateEvent) -> {
//			operateFiles.get
			sprintf("operateFiles Succeeded");
		});
		operateFiles.setOnCancelled((workerStateEvent) -> {
			sprintf("operateFiles CANCELLED");
		});
		operateFiles.setOnFailed((workerStateEvent) -> {
			sprintf("operateFiles FAILED");
			Main.setProcessCancelled(true);
		});
		Thread operateFiles_th = new Thread(operateFiles, "operateFiles_th");
		operateFiles_th.setDaemon(true);
		operateFiles_th.start();

	}

	@FXML private void hideRightInfo_btn_action(ActionEvent event) {
		if (model_datefix.getRightInfo_visible()) {
			hideRightInfo_btn.setRotate(0);
			rightInfoPanel_scrollPane.setVisible(false);
			rightInfoPanel_scrollPane.setMinWidth(0);
			rightInfoPanel_scrollPane.setPrefWidth(0);
			rightInfoPanel_scrollPane.setMaxWidth(0);
			rightInfoPanel.setVisible(false);
			rightInfoPanel.setMinWidth(-100);
			rightInfoPanel.setMaxWidth(-100);
			rightInfoPanel.setPrefWidth(-100);
			model_datefix.setRightInfo_visible(false);
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
			model_datefix.setRightInfo_visible(true);
		}
	}

}
