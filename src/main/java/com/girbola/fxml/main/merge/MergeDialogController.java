package com.girbola.fxml.main.merge;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MergeDialogController {

    private final String ERROR = MergeDialogController.class.getSimpleName();

    private ObservableList<FolderInfo> paths = FXCollections.observableArrayList();

    private ModelMain model_main;
    private Tables tables;
    private TableView<FolderInfo> table;
    private String tableType;

    //@formatter:off
	@FXML private Button apply_and_copy_btn;
	@FXML private Button apply_and_move_btn;
	@FXML private Button apply_btn;
	@FXML private Button cancel_btn;
	@FXML private RadioButton sortitTableSelected;
    @FXML private RadioButton sortedTableSelected;
    @FXML private RadioButton asitisTableSelected;


    @FXML private ComboBox<FolderInfo> selectedDestinationPath_cmb;
	@FXML private ComboBox<String> event_cmb;
	@FXML private ComboBox<String> location_cmb;
	@FXML private ComboBox<String> user_cmb;
    @FXML private Label destinationPath_lbl;
	@FXML private Label event_lbl;
	@FXML private Label location_lbl;

	//@formatter:on
    private void close() {
        Stage stage = (Stage) cancel_btn.getScene().getWindow();
        stage.close();
    }

    private void verifyWorkDirectory() {
        if (Main.conf.getWorkDir() == null) {
            Messages.warningText("copySelectedTableRows Workdir were null");
            return;
        }
        if (Main.conf.getWorkDir().isEmpty()) {
            Messages.warningText("copySelectedTableRows Workdir were empty");
            return;
        }
    }

    private String getSelectedDestinationPath() {
        return selectedDestinationPath_cmb.getEditor().getText().isEmpty() ? "" : selectedDestinationPath_cmb.getEditor().getText();
    }

    private String getEventName() {
        return event_cmb.getEditor().getText().isEmpty() ? "" : event_cmb.getEditor().getText();
    }

    private String getLocationName() {
        return location_cmb.getEditor().getText().isEmpty() ? "" : location_cmb.getEditor().getText();
    }

    private String getUserName() {
        return user_cmb.getEditor().getText().isEmpty() ? "" : user_cmb.getEditor().getText();
    }


    @FXML
    private void apply_and_move_btn_action(ActionEvent event) {
        verifyWorkDirectory();

        String absolutePath = getSelectedDestinationPath();
        String eventName = getEventName();
        String locationName = getLocationName();
        String userName = getUserName();

        Messages.sprintf("absolutePath " + absolutePath + " locationName were= '" + locationName + " eventName were= " + eventName + " tableType: " + " userName: " + userName);

        List<String> list = new ArrayList<>();
        if (absolutePath.isEmpty()) {
            Messages.warningText("Path can't be empty");
            return;
        }

        Path newDestinationPath = definePathByEventLocationUserName(absolutePath, eventName, locationName, userName);


        Messages.sprintf("newDestinationPath will be: " + newDestinationPath);



        if (!FileUtils.createFolders(newDestinationPath)) {
            Messages.warningText(Main.bundle.getString("cannotCreateFolders") + " " + newDestinationPath);
            Main.setProcessCancelled(true);
            return;
        }

        Iterator<FolderInfo> it = table.getSelectionModel().getSelectedItems().iterator();

        while (it.hasNext()) {
            FolderInfo folderInfo = it.next();
/*            if (FolderInfo_Utils.hasBadFiles(folderInfo)) {
                Messages.warningText(Main.bundle.getString("badDatesFound") + " at: " + folderInfo.getFolderPath());
                continue;
            }*/
 /*           if(folderInfo.getFolderPath().equals(absolutePath)) {
                Messages.sprintf("Source and destination are the same: " + folderInfo.getFolderPath());
                continue;
            }*/
            if (Main.getProcessCancelled()) {
                Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null, Misc.getLineNumber(), true);
                break;
            }

            Connection connection = null;
            FolderInfo folderInfoSource = FolderInfo_SQL.loadFolderInfo(newDestinationPath);

            if (folderInfoSource == null) {
                folderInfoSource = new FolderInfo();
                folderInfoSource.setFolderPath(newDestinationPath.toFile().getAbsolutePath());
                folderInfoSource.setTableType(TableUtils.resolvePath(newDestinationPath).getType());

                Messages.sprintf("folderInfo were not found at destination: " + folderInfoSource + " with database name " + Main.conf.getMdir_db_fileName());

                connection = SqliteConnection.connector(newDestinationPath, Main.conf.getMdir_db_fileName());

                SQL_Utils.setAutoCommit(connection, false);

                Messages.sprintf("folderInfoSource were created at: " + folderInfoSource.getFolderPath());
            }

            Iterator<FileInfo> fileInfo_list_it = folderInfo.getFileInfoList().iterator();
            List<FileInfo> fileList = new ArrayList<>();
            while (fileInfo_list_it.hasNext()) {
                FileInfo fileInfo = fileInfo_list_it.next();



                    fileInfo.setEvent(eventName);
                    fileInfo.setLocation(locationName);
                    fileInfo.setUser(userName);

                    Path destinationPath = Paths.get(FileUtils.getFileNameDateWithEventAndLocation(fileInfo, newDestinationPath.toString()).toString());
                    Path finalDest = Paths.get(newDestinationPath.toString(), destinationPath.toString());

                    Messages.sprintf("New dest path? " + finalDest);

                    if (Files.exists(finalDest)) {
                        try {
                            finalDest = FileUtils.renameFile(Paths.get(fileInfo.getOrgPath()), finalDest);
                            Messages.sprintf("and RENAMING to new dest path: " + finalDest);
                        } catch (IOException e) {
                            Messages.sprintfError(Main.bundle.getString("cannotRename"));
                            continue;
                        }
                    } else {
                        Messages.sprintf("+ fileInfo.SRC: " + fileInfo.toString() + "---->22222222new dest path: " + finalDest);
                    }
                    try {
                        if (finalDest != null) {
                            if (!Files.isDirectory(finalDest.getFileName())) {
                                Path parent = Files.createDirectories(finalDest.getParent());
                                boolean exists = Files.exists(parent);
                                if (exists) {
                                    Path movedToDestination = Files.move(Paths.get(fileInfo.getOrgPath()), finalDest);
                                    if (!Files.exists(movedToDestination)) {
                                        Messages.errorSmth(ERROR, Main.bundle.getString("cannotMoveFile" + "\nRadically stopping the app, before proper testing"), null, Misc.getLineNumber(), true);
                                    }

                                } else {
                                    Messages.warningText(Main.bundle.getString("cannotCreateDir"));
                                }
                            }
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        System.exit(0);
                    }

                    fileList.add(fileInfo);


                if (fileList.isEmpty()) {
                    Messages.sprintf("FileList were empty");
                    return;
                }

               /* boolean deleteFileInfoListToDatabase = FileInfo_SQL.deleteFileInfoListToDatabase(connection, fileList);
                if (deleteFileInfoListToDatabase) {
                    Messages.sprintf("Deleting fileInfo_list from database were success");
                } else {
                    Messages.sprintfError("Bug sniffer when deleting files form database");
                }*/


            }
/*
            try {
                connection.commit();
                connection.close();

            } catch (SQLException e) {
                Messages.sprintfError("SQL Exception when deleting from table: " + e.getMessage());
                e.printStackTrace();
            }*/
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
            if (FolderInfo_Utils.hasBadFiles(folderInfo)) {
                continue;
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


    public static Path definePathByEventLocationUserName(String absolutePath, String locationName, String eventName, String userName) {

        String locationStr, eventNameStr, userNameStr = "";

        locationStr = (locationName != null && !locationName.trim().isEmpty()) ? (" - " + locationName.trim()) : "";
        eventNameStr = (eventName != null && !eventName.trim().isEmpty()) ? (" - " + eventName.trim()) : "";
        userNameStr = (userName != null && !userName.trim().isEmpty()) ? (userName.trim()) : "";

        return Paths.get(absolutePath + locationStr + eventNameStr + userNameStr);
    }

    @FXML
    private void cancel_btn_action(ActionEvent event) {
        close();
    }

    public void init(ModelMain model_main, Tables tables, TableView<FolderInfo> table, String tableType) {
        this.model_main = model_main;
        this.tables = tables;
        this.table = table;
        this.tableType = tableType;

        selectedDestinationPath_cmb.setCellFactory(cell -> new AbsolutePathCellFactory());

        selectedDestinationPath_cmb.setItems(paths);

        paths.addAll(table.getSelectionModel().getSelectedItems());

        paths.sort((o1, o2) -> {
            if (o1.getFolderPath().length() < o2.getFolderPath().length()) {
                return -1;
            } else if (o1.getFolderPath().length() > o2.getFolderPath().length()) {
                return 1;
            } else {
                return 0;
            }
        });

        selectedDestinationPath_cmb.setConverter(new StringConverter<>() {
            @Override
            public String toString(FolderInfo folderInfo) {
                return folderInfo.getFolderPath();
            }

            @Override
            public FolderInfo fromString(String s) {
                return selectedDestinationPath_cmb.getSelectionModel().getSelectedItem();
            }
        });

        selectedDestinationPath_cmb.getSelectionModel().select(0);

        ToggleGroup toggleGroup = new ToggleGroup();
        Platform.runLater(() -> {
            sortitTableSelected.setToggleGroup(toggleGroup);
            sortedTableSelected.setToggleGroup(toggleGroup);
            asitisTableSelected.setToggleGroup(toggleGroup);

            sortitTableSelected.setSelected(true);
        });


    }
}
