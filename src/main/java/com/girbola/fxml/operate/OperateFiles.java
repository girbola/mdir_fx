package com.girbola.fxml.operate;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Model_operate;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.dialogs.YesNoCancelDialogController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SqliteConnection;
import common.utils.FileUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.fxml.operate.OperateFilesUtils.copyFile;
import static com.girbola.messages.Messages.sprintf;

public class OperateFiles {

    private final String ERROR = OperateFiles.class.getSimpleName();
    private boolean close;
    private Model_operate model_operate = new Model_operate();
    private List<FileInfo> list = new ArrayList<>();
    private Model_main model_main;
    private String scene_NameType;
    private List<FileInfo> listCopiedFiles = new ArrayList<>();

    public OperateFiles(List<FileInfo> list, boolean close, Model_main aModel_main, String scene_NameType) {
        Messages.sprintf("OperateFiles starting LIST");
        this.list = list;
        this.close = close;
        this.model_main = aModel_main;
        this.scene_NameType = scene_NameType;
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * MOVE
     * dest exists
     * files does not exists
     * rename if files not exists with same name
     *
     */

    public void init() throws Exception {
        Main.setProcessCancelled(false);
        try {
            if (!Files.exists(Paths.get(Main.conf.getWorkDir()).toRealPath())) {
                Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
                Messages.sprintfError(Main.bundle.getString("cannotFindWorkDir"));
                return;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            Messages.warningText_title(ex.getMessage(), Main.bundle.getString("cannotFindWorkDir"));
        }

        Parent parent = null;
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/operate/OperateDialog.fxml"), Main.bundle);
        parent = loader.load();

        OperateDialogController operateDialogController = (OperateDialogController) loader.getController();
        operateDialogController.init(model_operate);

        Scene operate_scene = new Scene(parent);
        operate_scene.getStylesheets()
                .add(Main.class.getResource(Main.conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());
        Platform.runLater(() -> {
            Main.scene_Switcher.getWindow().setScene(operate_scene);
        });

        Main.scene_Switcher.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Model_main model_Main = (Model_main) Main.getMain_stage().getUserData();
                Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
                Main.getMain_stage().setOnCloseRequest(model_Main.exitProgram);
                event.consume();
            }
        });
        operate();

    }

    private void operate() {
        Messages.sprintf("stage is showing");
        if (!list.isEmpty()) {
            Platform.runLater(() -> {
                model_operate.getStart_btn().setDisable(false);
            });

        } else {
            Messages.warningText("List were empty!");
            return;
        }
        model_operate.getCopyProcess_values().setTotalFiles(String.valueOf(list.size()));
        long totalSize = 0;
        for (FileInfo fileInfo : list) {
            totalSize += fileInfo.getSize();
        }

        Messages.sprintf("totalSize: " + totalSize + " workdir: " + new File(Main.conf.getWorkDir()).getFreeSpace());
        Platform.runLater(() -> {

            model_operate.getStart_btn().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (model_main == null) {
                        Messages.sprintfError("model main is null");
                    }
                    if (model_main.getMonitorExternalDriveConnectivity() == null) {
                        Messages.sprintfError("model main getMonitorExternalDriveConnectivity is null");
                    }
                    model_main.getMonitorExternalDriveConnectivity().cancel();
                    Task<Integer> copy = new Copy();


                    copy.setOnSucceeded((WorkerStateEvent eventWorker) -> {
                        Messages.sprintf("copy succeeded");
                    });
                    copy.setOnFailed((WorkerStateEvent
                                              eventWorker) -> {
                        Messages.sprintf("copy failed");
                    });
                    copy.setOnCancelled((WorkerStateEvent eventWorker) -> {
                        model_operate.getCancel_btn().setText(Main.bundle.getString("close"));
                        model_operate.doneButton(scene_NameType, close);
                        Messages.sprintf("copy cancelled");
                    });

                    Thread copy_thread = new Thread(copy, "Copy Thread");
                    copy_thread.start();
                }
            });
            model_operate.getCancel_btn().setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    Main.setProcessCancelled(true);
                    Messages.sprintf("Current file cancelled is: " + model_operate.getCopyProcess_values().getCopyTo());
                    model_operate.stopTimeLine();
                    Main.setProcessCancelled(true);
                }
            });
        });

    }

    private class Copy extends Task<Integer> {
        private AtomicInteger counter = new AtomicInteger(list.size());
        private int byteRead;
        // private long currentFileByte;
        private long currentSize;
        private String STATE = "";
        private Path source = null;
        private Path dest = null;
        private SimpleStringProperty rememberAnswer = new SimpleStringProperty(CopyAnswerType.ASK);

        @Override
        protected Integer call() throws Exception {

            Path workDir = Paths.get(Main.conf.getWorkDir()).toRealPath();

            if (!Files.exists(workDir)) {
                Messages.sprintfError("Cannot find workdir!");
                Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
                cancel();
                model_operate.stopTimeLine();
                Main.setProcessCancelled(true);
                return null;
            } else {
                Messages.sprintf("Workdir exists at " + workDir);
            }
            if (isCancelled()) {
                Messages.sprintf("Copy process is cancelled");
                Main.setProcessCancelled(true);
                model_operate.stopTimeLine();
                return null;
            }
            if (Main.getProcessCancelled()) {
                Messages.sprintf("Copy process getProcessCancelled is cancelled");
                cancel();
                model_operate.stopTimeLine();
                return null;
            }
            if (!list.isEmpty()) {
                Messages.warningText("List were NOOOOOOOOOT empty!!!!!!!");
//				model_operate.getStart_btn().setDisable(false);
                model_operate.getTimeline().play();
            } else {
                Messages.warningText("List were empty!!!!!!!");
                cancel();
                Main.setProcessCancelled(true);
                return null;
            }

            // boolean copy = false;
            for (FileInfo fileInfo : list) {

                Messages.sprintf("Copying file: " + fileInfo.getOrgPath() + " dest: " + fileInfo.getWorkDir()
                        + fileInfo.getDestination_Path());

                source = Paths.get(fileInfo.getOrgPath());
                if (Files.exists(source)) {

                    dest = Paths.get(fileInfo.getWorkDir() + fileInfo.getDestination_Path());
                    boolean cancellation = checkCancellation(fileInfo, dest);
                    if (cancellation) {
                        break;
                    }
//			TODO	duplicates ei toimi oikein. Korjaa!!!!

                    Messages.sprintf("source is: " + source + " dest: " + dest);
                    updateSourceAndDestProcessValues(source, dest);
                    List<FileInfo> findPossibleExistsFoldersInWorkdir = model_main.getWorkDir_Handler()
                            .findPossibleExistsFoldersInWorkdir(fileInfo);
                    if (!findPossibleExistsFoldersInWorkdir.isEmpty()) {
                        Messages.sprintf("Duplicates found: " + source);
//						fileInfo.setCopied(true);
//				boolean defineDuplicate = FileInfoUtils.defineDuplicateFile(fileInfo, dest);
//				if (defineDuplicate) {
                        STATE = Copy_State.DUPLICATE.getType();
                    } else {

                        Path dest_test = FileUtils.renameFile(Paths.get(fileInfo.getOrgPath()), dest);
                        if (dest_test != null) {
                            dest = dest_test;
                            STATE = Copy_State.RENAME.getType();
                        } else {
                            STATE = Copy_State.COPY.getType();
                        }
                    }
                    SimpleIntegerProperty answer = new SimpleIntegerProperty(-1);

                    Copy_State copy_State = Copy_State.valueOf(STATE);
                    switch (copy_State) {
                        case COPY:
                            if (Files.exists(source)) {
                                if (copyFile(fileInfo, source, dest, STATE, answer)) {
                                    updateSourceAndDestProcessValues(source, dest);
                                    updateIncreaseCopyingProcessValues();
                                    if (!fileInfo.isCopied()) {
                                        fileInfo.setCopied(true);
                                        model_main.getWorkDir_Handler().add(fileInfo);
                                    }
                                }
                            } else {
                                Messages.sprintfError("Source file did not exists!: " + source);
                            }

                            break;
                        case DUPLICATE:
                            updateIncreaseDuplicatesProcessValues();
                            if (!fileInfo.isCopied()) {
                                fileInfo.setCopied(true);
                                model_main.getWorkDir_Handler().add(fileInfo);
                            }

                            break;
                        case RENAME:
                            if (copyFile(fileInfo, source, dest, STATE, answer)) {
                                updateIncreaseRenamedProcessValues();
                                if (!fileInfo.isCopied()) {
                                    fileInfo.setCopied(true);
                                }
                                model_main.getWorkDir_Handler().add(fileInfo);

                            }
                            break;
                        case BROKENFILE:
                            Messages.sprintfError("Broken file were found. Filename: " + fileInfo.getOrgPath());
                            break;
                    }
                    Messages.sprintf("Dest: " + dest + " STATE: " + STATE);

                    updateFilesLeftCounter();
                    source = null;
                    dest = null;
                    STATE = "";
                }
            }
            model_operate.getCopyProcess_values().update();
            return null;
        }



        /**
         * Check if there are possibilities for a conflict existence with copy process
         *
         * @param fileInfo
         * @param destination
         * @return
         */
        private boolean checkCancellation(FileInfo fileInfo, Path destination) {
            if (isCancelled()) {
                Main.setProcessCancelled(true);
                cancel();
                return true;
            }
            if (Main.getProcessCancelled()) {
                Main.setProcessCancelled(true);
                cancel();
                return true;
            }
            if (fileInfo.getDestination_Path() == null) {
                Messages.warningText("getDestination_Path were null: " + fileInfo.getOrgPath());
                Main.setProcessCancelled(true);
                cancel();
                return true;
            }

            if (fileInfo.getDestination_Path().isEmpty()) {
                Messages.warningText("getDestination_Path were empty: " + fileInfo.getOrgPath());
                Main.setProcessCancelled(true);
                cancel();
                return true;
            }
            if (Paths.get(fileInfo.getOrgPath()).getParent().toString().contains(Main.conf.getWorkDir())) {
                Messages.warningText(Main.bundle.getString("conflictWithWorkDir"));
                Main.setProcessCancelled(true);
                cancel();
                return true;
            }
            if (fileInfo.getOrgPath() == destination.toString()) {
                Main.setProcessCancelled(true);
                cancel();
                return true;
            }
            return false;
        }

        private void updateFilesLeftCounter() {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().setFilesLeft(counter.decrementAndGet());
            });
        }




        private void renameTmpFileToCorruptedFileExtensions(FileInfo fileInfo, Path destTmp, Path dest) {
            try {
                Messages.sprintf("Renaming corrupted file to: " + dest);

                String fileName = FileUtils.parseExtension(dest);
                Path destPath = Paths.get(dest.getParent().toString() + File.separator + fileName + "_crp."
                        + FileUtils.getExtension(dest));
                if (Files.exists(destPath)) {
                    destPath = FileUtils.renameFile(dest, destPath);
                }
                Files.move(Paths.get(dest.toString() + ".tmp"), destPath);

//				listCopiedFiles.add(fileInfo);
//
//				String newName = FileUtils.parseWorkDir(destPath.toString(), fileInfo.getWorkDir());
//				updateFileInfoWorkDirPathAndSerial(fileInfo, newName);
//				fileInfo.setCopied(true);
                String newName = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
                fileInfo.setDestination_Path(newName);
                fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                fileInfo.setCopied(true);
                listCopiedFiles.add(fileInfo);
                boolean added = model_main.getWorkDir_Handler().add(fileInfo);
                if (added) {
                    Messages.sprintf("FileInfo - corrupted added to destination succesfully");
                } else {
                    Messages.sprintf("FileInfo - corrupted were not added because it did exists "
                            + fileInfo.getDestination_Path());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.sprintfError(ex.getMessage());
                Main.setProcessCancelled(true);
            }
        }

        private void renameTmpFileBackToOriginalExtentension(FileInfo fileInfo, Path destTmp, Path dest) {
            try {
                Messages.sprintf(
                        "Renaming file .tmp back to org extension: " + dest.toString() + ".tmp" + " to dest: " + dest);
                Files.move(Paths.get(dest.toString() + ".tmp"), dest);
                String newName = FileUtils.parseWorkDir(dest.toString(), fileInfo.getWorkDir());
                fileInfo.setDestination_Path(newName);
                fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                fileInfo.setCopied(true);
                listCopiedFiles.add(fileInfo);
                boolean added = model_main.getWorkDir_Handler().add(fileInfo);
                if (added) {
                    Messages.sprintf("FileInfo added to destination succesfully");
                } else {
                    Messages.sprintf("FileInfo were not added because it did exists " + fileInfo.getDestination_Path());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.sprintfError(ex.getMessage());
            }

        }

        private synchronized void resetAndUpdateFileCopiedProcessValues() {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().setLastSecondFileSize_tmp(0);
                model_operate.getCopyProcess_values().decreaseFilesLeft_tmp();
            });
        }

        private synchronized void updateIncreaseLastSecondFileSizeProcessValues(int byteRead) {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().increaseLastSecondFileSize_tmp(byteRead);
            });
        }

        private synchronized void updateIncreaseRenamedProcessValues() {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().increaseRenamed_tmp();
            });
        }

        private synchronized void updateIncreaseCopyingProcessValues() {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().increaseCopied_tmp();
            });
        }

        private synchronized void updateIncreaseDuplicatesProcessValues() {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().increaseDuplicated_tmp();
                Messages.sprintf("Increader dups is now: " + model_operate.getCopyProcess_values().getDuplicated_tmp());
            });
        }

        private synchronized void updateSourceAndDestProcessValues(Path source, Path dest) {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().setCopyFrom(source.toString());
                model_operate.getCopyProcess_values().setCopyTo(dest.toString());
            });

        }

        private boolean cleanCancelledFile(InputStream from, OutputStream to, Path source, Path dest) {
            sprintf("cleanCancelledFile cancelled");
            try {
                from.close();
                to.flush();
                to.close();
                if (Files.size(source) != Files.size(dest)) {
                    Files.deleteIfExists(Paths.get(dest.toString() + ".tmp"));
                    sprintf("2file is gonna be deleted! " + dest.toString());
                }
                return true;
            } catch (Exception ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
                Main.setProcessCancelled(true);
                return false;
            }
        }

        @Override
        protected void failed() {
            model_operate.stopTimeLine();

            TableUtils.refreshAllTableContent(model_main.tables());
//			model_operate.doneButton(scene_NameType, close);
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
        }

        @Override
        protected void cancelled() {
            Messages.sprintf("OperateFiles COPY were cancelled");
            model_operate.stopTimeLine();
            model_operate.doneButton(scene_NameType, close);
            TableUtils.refreshAllTableContent(model_main.tables());
        }

        @Override
        protected void running() {
            if (!isRunning()) {
                Messages.sprintf("OperateFiles running were stopped");
            } else {
                Platform.runLater(() -> {
                    model_operate.getStart_btn().setDisable(true);
                    model_operate.getCancel_btn().setDisable(false);

                });
            }
        }

        @Override
        protected void succeeded() {
            Messages.sprintf("OperateFiles COPY were succeeded");
            model_operate.stopTimeLine();
            model_operate.doneButton(scene_NameType, close);
            model_operate.stopTimeLine();
            Task<Void> saveWorkDirToDatabase = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Messages.sprintf("Step1");
                    if (model_main.getWorkDir_Handler() == null) {
                        Messages.sprintf("Workdir_handler null");
                    }
                    boolean saveWorkDirList = model_main.getWorkDir_Handler().saveWorkDirListToDatabase();
                    Messages.sprintf("Step2");
                    TableUtils.refreshAllTableContent(model_main.tables());
                    if (saveWorkDirList) {
                        Messages.sprintf("saveWorkDirList DONE!!!");
                    } else {
                        Messages.sprintf("saveWorkDirList FAILED");
                    }
                    TableUtils.updateAllFolderInfos(model_main.tables());
                    TableUtils.refreshAllTableContent(model_main.tables());
                    return null;
                }
            };
            // @formatter:on
            saveWorkDirToDatabase.setOnSucceeded((eventti) -> {
                Messages.sprintf("saveWorkDirListToDatabase finished success!");
//				writeToDatabase();
            });
            saveWorkDirToDatabase.setOnCancelled((eventti) -> {
                Messages.sprintfError("saveWorkDirListToDatabase finished success!");
//				writeToDatabase();

            });
            saveWorkDirToDatabase.setOnFailed((eventti) -> {
                Messages.sprintf("saveWorkDirToDatabase Task failed!");
//				writeToDatabase();

            });

            Thread savingWorkDirContent = new Thread(saveWorkDirToDatabase, "Saving workDir content");
            savingWorkDirContent.setDaemon(true);
            savingWorkDirContent.start();

            sprintf("OperateFiles succeeded");
        }

        private void writeToDatabase() {
            Messages.sprintf("Insert worked!");
            if (Main.conf.getDrive_connected()) {
                try {

                    Connection connection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()),
                            Main.conf.getMdir_db_fileName());
                    connection.setAutoCommit(false);
                    boolean inserted = FileInfo_SQL.insertFileInfoListToDatabase(connection, listCopiedFiles, true);
                    if (!inserted) {
                        connection.close();
                        connection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir()),
                                Main.conf.getMdir_db_fileName() + new Date().toString());
                        inserted = FileInfo_SQL.insertFileInfoListToDatabase(connection, listCopiedFiles, true);
                        if (!inserted) {
                            Messages.errorSmth(ERROR, "Can't save to destination dir", null, Misc.getLineNumber(),
                                    true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    class Dialogue implements Callable<SimpleIntegerProperty> {
        private Window owner;
        private FileInfo fileInfo;
        private long copyedFileCurrentSize;
        private SimpleIntegerProperty answer;
        private SimpleStringProperty rememberAnswer;

        Dialogue(Window owner, FileInfo fileInfo, long copyedFileCurrentSize, SimpleIntegerProperty answer,
                 SimpleStringProperty rememberAnswer) {
            this.owner = owner;
            this.fileInfo = fileInfo;
            this.copyedFileCurrentSize = copyedFileCurrentSize;
            this.answer = answer;
            this.rememberAnswer = rememberAnswer;
        }

        @Override
        public SimpleIntegerProperty call() throws Exception {
            FXMLLoader loader = null;
            Parent parent = null;
            YesNoCancelDialogController yesNoCancelDialogController = null;
            try {
                loader = new FXMLLoader(Main.class.getResource("dialogs/YesNoCancelDialog.fxml"), Main.bundle);
                parent = loader.load();

                yesNoCancelDialogController = (YesNoCancelDialogController) loader.getController();

                final Stage stage = new Stage();
                stage.setTitle(Main.bundle.getString("corruptedFile"));
                stage.initOwner(owner);
                stage.initStyle(StageStyle.UTILITY);
                stage.initModality(Modality.WINDOW_MODAL);
                yesNoCancelDialogController.init(stage, answer, fileInfo, rememberAnswer, "Corrupted file",
                        "Corrupted file found at " + fileInfo.getOrgPath() + " size should be: " + fileInfo.getSize()
                                + " but it is now " + copyedFileCurrentSize + "\n"
                                + Main.bundle.getString("doYouWantToKeepTheFile") + "",
                        Main.bundle.getString("yes"), Main.bundle.getString("no"), Main.bundle.getString("abort"));

                stage.setScene(new Scene(parent));
                stage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return answer;
        }
    }
}
