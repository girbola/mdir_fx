package com.girbola.controllers.main.merge;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.FolderInfoUtils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FolderInfo_SQL;
import common.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MergeDialogController {

    private final String ERROR = MergeDialogController.class.getSimpleName();

    private ObservableList<FolderInfo> paths = FXCollections.observableArrayList();

    private ModelMain model_main;
    private Tables tables;
    private TableView<FolderInfo> table;
    private String tableType;

    private ToggleGroup selectedTableType = new ToggleGroup();

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
        String absolutePath = getSelectedDestinationPath();
        String eventName = getEventName();
        String locationName = getLocationName();
        String userName = getUserName();

        Messages.sprintf("absolutePath " + absolutePath + " locationName were= '" + locationName + " eventName were= " + eventName + " tableType: " + " userName: " + userName);

        if (absolutePath.isEmpty()) {
            Messages.warningText("Path can't be empty");
            return;
        }
        FolderInfo folderInfoDestination;

        Path newDestinationPath = definePathByLocationEventUserName(absolutePath, locationName, eventName, userName);
        Messages.sprintf("newDestinationPath will be: " + newDestinationPath);
        if (!Files.exists(newDestinationPath)) {
            if (!FileUtils.createFolders(newDestinationPath)) {
                Messages.warningText(Main.bundle.getString("cannotCreateFolders") + " " + newDestinationPath);
                Main.setProcessCancelled(true);
                return;
            } else {
                folderInfoDestination = new FolderInfo(newDestinationPath);
            }

        } else {
            folderInfoDestination = FolderInfo_SQL.loadFolderInfo(newDestinationPath);

            if (folderInfoDestination == null) {
                folderInfoDestination = new FolderInfo();
                folderInfoDestination.setFolderPath(newDestinationPath.toFile().getAbsolutePath());
                folderInfoDestination.setTableType(TableUtils.resolvePath(newDestinationPath).getType());

                Messages.sprintf("folderInfo were not found at destination: " + folderInfoDestination + " with database name " + Main.conf.getMdir_db_fileName());
            }

        }


        Iterator<FolderInfo> it = table.getSelectionModel().getSelectedItems().iterator();

        while (it.hasNext()) {
            FolderInfo folderInfo = it.next();
/*            if (FolderInfoUtils.hasBadFiles(folderInfo)) {
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

//            Connection connection = null;


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
                        Messages.sprintfError(Main.bundle.getString("cannotRename") + "File from: " + fileInfo.getOrgPath() + " Filename: " + finalDest);
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

                                if (Files.exists(movedToDestination) && Files.isDirectory(movedToDestination.getFileName())) {
                                    Messages.sprintf("MovedToDestionation path is now: " + movedToDestination);
                                    fileInfo.setOrgPath(movedToDestination.toString());
                                    fileList.add(fileInfo);

                                    fileInfo_list_it.remove();
                                } else {
                                    Messages.sprintfError("MovedToDestionation path is now: " + movedToDestination + " and is directory? " + Files.isDirectory(movedToDestination.getFileName()));
                                    Messages.errorSmth(ERROR, Main.bundle.getString("cannotMoveFile") + "\nRadically stopping the app, before proper testing", null, Misc.getLineNumber(), true);
                                    Main.setProcessCancelled(true);
                                    break;
                                }

                            } else {
                                Messages.warningText(Main.bundle.getString("cannotCreateDir"));
                            }
                        }
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Messages.sprintfError(Main.bundle.getString("cannotMoveFile"));
                    continue;
                }

                fileList.add(fileInfo);


                if (fileList.isEmpty()) {
                    Messages.sprintf("FileList were empty");
                    return;
                }


            }
            folderInfoDestination.setFileInfoList(fileList);
            folderInfoDestination.setChanged(true);
            TableView<FolderInfo> tableByType = model_main.tables().getTableByType(folderInfo.getTableType());
            if (tableByType != null) {
                tableByType.getItems().add(folderInfoDestination);
            }

        }
        FolderInfoUtils.cleanTables(model_main.tables());

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
            if (FolderInfoUtils.hasBadFiles(folderInfo)) {
                continue;
            }
            if (Main.getProcessCancelled()) {
                Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null, Misc.getLineNumber(), true);
                break;
            }
            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

                if (Main.getProcessCancelled()) {
                    Messages.errorSmth(ERROR, Main.bundle.getString("creatingDestinationDirFailed"), null, Misc.getLineNumber(), true);
                    break;
                }
                fileInfo.setEvent(eventName);
                fileInfo.setLocation(locationName);
                fileInfo.setUser(userName);

                // I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
                // I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
                Path destinationPath = FileUtils.getFileNameDateWithEventAndLocation(fileInfo, Main.conf.getWorkDir());
                if (!Files.exists(destinationPath)) {
                    Messages.sprintfError(Main.bundle.getString("creatingDestinationDirFailed") + " File destination: " + destinationPath);
                    Main.setProcessCancelled(true);
                    break;
                }
                fileInfo.setCopied(false);
                folderInfo.setChanged(true);
                folderInfo.setTableType(selectedTableType.getSelectedToggle().getUserData().toString());
                Main.setChanged(true);
                Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
            }
        }
        TableUtils.refreshAllTableContent(tables);
        close();
    }


    public static Path definePathByLocationEventUserName(String absolutePath, String locationName, String eventName, String userName) {

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

        Platform.runLater(() -> {
            sortitTableSelected.setUserData(TableType.SORTIT.getType());
            sortedTableSelected.setUserData(TableType.SORTED.getType());
            asitisTableSelected.setUserData(TableType.ASITIS.getType());

            sortitTableSelected.setToggleGroup(selectedTableType);
            sortedTableSelected.setToggleGroup(selectedTableType);
            asitisTableSelected.setToggleGroup(selectedTableType);

            sortitTableSelected.setSelected(true);
        });
        selectedTableType.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            Messages.sprintf("fromString: " + selectedTableType.getSelectedToggle().getUserData().toString() + " NEWNWNWN: " + new_toggle.getUserData().toString());
        });

        selectedDestinationPath_cmb.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            selectedDestinationPath_cmb.getEditor().positionCaret(newText.length()); // Set caret to the end
        });
        event_cmb.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            event_cmb.getEditor().positionCaret(newText.length()); // Set caret to the end
        });
        location_cmb.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            location_cmb.getEditor().positionCaret(newText.length()); // Set caret to the end
        });
        user_cmb.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            user_cmb.getEditor().positionCaret(newText.length()); // Set caret to the end
        });

    }
}
