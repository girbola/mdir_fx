package com.girbola.fxml.main.merge.copy;

import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fxml.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SqliteConnection;
import common.utils.FileUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MergeCopyDialogController {

    private final String ERROR = MergeCopyDialogController.class.getSimpleName();

    private Model_main model_main;
    private Tables tables;
    private TableView<FolderInfo> table;
    private String tableType;

    //@formatter:off
	@FXML private Button apply_and_copy_btn;
	@FXML private Button apply_and_move_btn;
	@FXML private Button apply_btn;
	@FXML private Button cancel_btn;
	@FXML private CheckBox addEverythingInsameDir_chb;

	@FXML private ComboBox<String> absolutePath_cmb;
	@FXML private ComboBox<String> event_cmb;
	@FXML private ComboBox<String> location_cmb;
	@FXML private ComboBox<String> user_cmb;
    @FXML private Label absolutePath_lbl;
	@FXML private Label event_lbl;
	@FXML private Label location_lbl;

	//@formatter:on
    private void close() {
        Stage stage = (Stage) cancel_btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void apply_and_move_btn_action(ActionEvent event) {
        if (Main.conf.getWorkDir() == null) {
            Messages.warningText("copySelectedTableRows Workdir were null");
            return;
        }
        if (Main.conf.getWorkDir().isEmpty()) {
            Messages.warningText("copySelectedTableRows Workdir were empty");
            return;
        }

        String absolutePath = "";
        String eventName = "";
        String locationName = "";
        String userName = "";


        absolutePath = absolutePath_cmb.getEditor().getText().isEmpty() ? absolutePath : absolutePath_cmb.getEditor().getText();
        eventName = event_cmb.getEditor().getText().isEmpty() ? eventName : event_cmb.getEditor().getText();
        locationName = location_cmb.getEditor().getText().isEmpty() ? locationName : location_cmb.getEditor().getText();
        userName = user_cmb.getEditor().getText().isEmpty() ? userName : user_cmb.getEditor().getText();

        Messages.sprintf("absolutePath "+ absolutePath + " locationName were= '" + locationName + " eventName were= " + eventName + " userName: " + userName);

        List<String> list = new ArrayList<>();
        if (absolutePath.isEmpty()) {
            Messages.warningText("Path can't be empty");
            return;
        }

        Path newDestinationPath = definePathByEventLocation(absolutePath, eventName, locationName, userName);

        Iterator<FolderInfo> it = table.getSelectionModel().getSelectedItems().iterator();

        while (it.hasNext()) {
            FolderInfo folderInfo = it.next();
            if (folderInfo.getBadFiles() > 0) {
                Messages.warningText(Main.bundle.getString("badDatesFound"));
                return;
            }
            if (Main.getProcessCancelled()) {
                Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
                        Misc.getLineNumber(), true);
                break;
            }
            Connection connection = SqliteConnection.connector(folderInfo.getFolderPath(),
                    Main.conf.getMdir_db_fileName());
            try {
                connection.setAutoCommit(false);
            } catch (Exception e) {
                // TODO: handle exception
            }
            Iterator<FileInfo> fileInfo_list_it = folderInfo.getFileInfoList().iterator();
            List<FileInfo> fileList = new ArrayList<>();
            while (fileInfo_list_it.hasNext()) {
                FileInfo fileInfo = fileInfo_list_it.next();
                if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {

                    fileInfo.setEvent(eventName);
                    fileInfo.setLocation(locationName);
                    fileInfo.setUser(userName);
                    Path destinationPath = Paths.get(FileUtils
                            .getFileNameDateWithEventAndLocation(fileInfo, newDestinationPath.toString()).toString());
                    Path finalDest = Paths.get(newDestinationPath + destinationPath.toString());

                    if (Files.exists(finalDest)) {
                        try {

                            finalDest = FileUtils.renameFile(Paths.get(fileInfo.getOrgPath()), finalDest);
                            Messages.sprintfError("RENAMING! new dest path: " + finalDest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Messages.sprintf(
                                "+ fileInfo.SRC: " + fileInfo.toString() + "---->22222222new dest path: " + finalDest);
                    }

                    try {
                        Files.createDirectories(finalDest.getParent());
                        Files.move(Paths.get(fileInfo.getOrgPath()), finalDest);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.exit(0);
                    }

                    fileList.add(fileInfo);
                }
                if(fileList.isEmpty()) {
                    Messages.sprintf("FileList were empty");
                    return;
                }

                boolean deleteFileInfoListToDatabase = FileInfo_SQL.deleteFileInfoListToDatabase(connection, fileList);
                if (deleteFileInfoListToDatabase) {
                    Messages.sprintf("Deleting fileInfo_list from database were success");
                } else {
                    Messages.sprintfError("Bug sniffer when deleting files form database");
                }


            }

            try {
                connection.commit();
                connection.close();

            } catch (SQLException e) {
                Messages.sprintfError("SQL Exception when deleting from table: " + e.getMessage());
                e.printStackTrace();
            }
        }

        TableUtils.refreshAllTableContent(tables);
        close();

    }

    @FXML
    private void apply_btn_action(ActionEvent event) {
        if (Main.conf.getWorkDir() == null) {
            Messages.warningText("copySelectedTableRows Workdir were null");
            return;
        }
        if (Main.conf.getWorkDir().isEmpty()) {
            Messages.warningText("copySelectedTableRows Workdir were empty");
            return;
        }
        String eventName = "";
        String locationName = "";
        String userName = "";

        if (!event_cmb.getEditor().getText().isEmpty()) {
            eventName = event_cmb.getEditor().getText().trim();
        }
        if (!location_cmb.getEditor().getText().isEmpty()) {
            locationName = location_cmb.getEditor().getText().trim();
        }
        if (!user_cmb.getEditor().getText().isEmpty()) {
            userName = user_cmb.getEditor().getText().trim();
        }
        Messages.sprintf(
                "locationName were= '" + locationName + " eventName were= " + eventName + " userName: " + userName);

        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            if (folderInfo.getBadFiles() >= 1) {
                Messages.warningText(Main.bundle.getString("badDatesFound"));
                return;
            }
            if (Main.getProcessCancelled()) {
                Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
                        Misc.getLineNumber(), true);
                break;
            }
            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

                if (Main.getProcessCancelled()) {
                    Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null,
                            Misc.getLineNumber(), true);
                    break;
                }
                fileInfo.setEvent(eventName);
                fileInfo.setLocation(locationName);
                fileInfo.setUser(userName);

                // I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
                // I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
                Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());
                if (!Files.exists(destinationPath)) {
                    Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: "
                            + destinationPath);
                    Main.setProcessCancelled(true);
                    break;
                }
                fileInfo.setCopied(false);
                folderInfo.setChanged(true);
                Main.setChanged(true);
                Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
            }
        }
        TableUtils.refreshAllTableContent(tables);
        close();
    }



    public static Path definePathByEventLocation(String absolutePath, String locationName, String eventName, String userName) {

        String locationStr, eventNameStr, userNameStr = "";

        locationStr = (locationName != null && !locationName.trim().isEmpty()) ? (" - " + locationName.trim()) : "";
        eventNameStr = (eventName != null && !eventName.trim().isEmpty()) ? ( " - " + eventName.trim()) : "";
        userNameStr = (userName != null && !userName.trim().isEmpty()) ? (userName.trim()) : "";

        return Paths.get(absolutePath + locationStr + eventNameStr + userNameStr);
    }

    @FXML
    private void apply_and_copy_btn_action(ActionEvent event) {
        if (Main.conf.getWorkDir() == null) {
            Messages.warningText("copySelectedTableRows Workdir were null");
            return;
        }
        if (Main.conf.getWorkDir().isEmpty()) {
            Messages.warningText("copySelectedTableRows Workdir were empty");
            return;
        }

        String absolutePath = "";
        String eventName = "";
        String locationName = "";
        String userName = "";

        absolutePath = absolutePath_cmb.getEditor().getText().isEmpty() ? absolutePath : absolutePath_cmb.getEditor().getText();
        eventName = event_cmb.getEditor().getText().isEmpty() ? eventName : event_cmb.getEditor().getText();
        locationName = location_cmb.getEditor().getText().isEmpty() ? locationName : location_cmb.getEditor().getText();
        userName = user_cmb.getEditor().getText().isEmpty() ? userName : user_cmb.getEditor().getText();

        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            if (folderInfo.getBadFiles() >= 1) {
                Messages.warningText(Main.bundle.getString("badDatesFound"));
                return;
            }
            if (Main.getProcessCancelled()) {
                Messages.sprintfError("Merging were cancelled");
                break;
            }

            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                if (Main.getProcessCancelled()) {
                    Messages.sprintfError("Merging were cancelled");
                    break;
                }
                if (!fileInfo.getEvent().isEmpty()) {
                    if (addEverythingInsameDir_chb.isSelected()) {
                        if (folderInfo.getJustFolderName() != eventName) {
                            folderInfo.setJustFolderName(eventName);
                        }
                    }
                }

                fileInfo.setEvent(eventName);
                fileInfo.setLocation(locationName);
                fileInfo.setUser(userName);

                Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());

//				if (!Files.exists(destinationPath)) {
//					Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: "
//							+ destinationPath);
//					Main.setProcessCancelled(true);
//					break;
//				}
            }
        }
//		FolderInfo_Utils.moveToAnotherTable(tables, table, tableType);

        List<FileInfo> list = new ArrayList<>();
        ExecutorService exec = Executors.newSingleThreadExecutor();
        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            if (folderInfo.getBadFiles() >= 1) {
                Messages.warningText(Main.bundle.getString("badDatesFound"));
                return;
            }
            list.addAll(folderInfo.getFileInfoList());
        }

        Task<Boolean> operate = new OperateFiles(list, true, model_main, SceneNameType.MAIN.getType());

        operate.setOnSucceeded(succeeded -> {
                for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
                    TableUtils.updateFolderInfo(folderInfo);
                }
                TableUtils.refreshAllTableContent(tables);
                TableUtils.saveChangesContentsToTables(model_main.tables());
                Main.setChanged(false);
                close();
        });

        operate.setOnFailed(failed -> {
                Messages.warningText("Copy process failed");
                close();
        });

        operate.setOnCancelled(cancelled -> {
                Messages.sprintf("Copy process were cancelled");
                close();
        });

        Thread thread = new Thread(operate, "Operate Thread");
        exec.submit(thread);

    }

    @FXML
    private void cancel_btn_action(ActionEvent event) {
        close();
    }

    public void init(Model_main model_main, Tables tables, TableView<FolderInfo> table, String tableType) {
        this.model_main = model_main;
        this.tables = tables;
        this.table = table;
        this.tableType = tableType;

        ObservableList<String> paths = FXCollections.observableArrayList();
        for (FolderInfo selectedItems : table.getSelectionModel().getSelectedItems()) {
            paths.add(selectedItems.getFolderPath());
        }
        absolutePath_cmb.setItems(paths);

    }
}
