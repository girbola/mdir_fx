package com.girbola.fxml.operate;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.controllers.datefixer.*;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.Model_operate;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.dialogs.YesNoCancelDialogController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SqliteConnection;
import common.utils.FileUtils;
import java.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.fxml.operate.OperateFilesUtils.*;
import static com.girbola.messages.Messages.sprintf;

public class OperateFiles {

    private final String ERROR = OperateFiles.class.getSimpleName();

    private WorkDirSQL workDirSQL;
    private boolean close;
    private Model_operate model_operate = new Model_operate();
    private List<FileInfo> list = new ArrayList<>();
    private ModelMain modelMain;
    private String sceneNameType;
    private static List<FileInfo> listCopiedFiles = new ArrayList<>();

    public OperateFiles(List<FileInfo> list, boolean close, ModelMain aModel_main, String sceneNameType) {
        Messages.sprintf("OperateFiles starting...");
        this.list = list;
        this.close = close;
        this.modelMain = aModel_main;
        this.sceneNameType = sceneNameType;
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void init() throws Exception {
        Main.setProcessCancelled(false);
        Path workDir = Paths.get(Main.conf.getWorkDir());
        try {
            if (!Files.exists(workDir.toRealPath())) {
                Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
                return;
            }
        } catch (IOException ex) {
            Messages.warningText_title(ex.getMessage(), Main.bundle.getString("cannotFindWorkDir"));
        }

        workDirSQL = new WorkDirSQL(workDir);

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

        Main.scene_Switcher.getWindow().setOnCloseRequest(event -> {
            ModelMain model_Main = (ModelMain) Main.getMain_stage().getUserData();
            Main.scene_Switcher.getWindow().setScene(Main.scene_Switcher.getScene_main());
            Main.getMain_stage().setOnCloseRequest(model_Main.exitProgram);
            event.consume();
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

        Platform.runLater(() -> {

            model_operate.getStart_btn().setOnAction(event -> {
                if (modelMain == null) {
                    Messages.sprintfError("model main is null");
                }
                if (modelMain.getMonitorExternalDriveConnectivity() == null) {
                    Messages.sprintfError("model main getMonitorExternalDriveConnectivity is null");
                }
                modelMain.getMonitorExternalDriveConnectivity().cancel();

                try {
                    Path workDir = Paths.get(Main.conf.getWorkDir()).toRealPath();

                    Task<Integer> copy = new Copy();
                    copy.setOnSucceeded((WorkerStateEvent eventWorker) -> Messages.sprintf("copy succeeded"));
                    copy.setOnFailed((WorkerStateEvent eventWorker) -> Messages.sprintf("copy failed"));
                    copy.setOnCancelled((WorkerStateEvent eventWorker) -> {
                        model_operate.getCancel_btn().setText(Main.bundle.getString("close"));
                        model_operate.doneButton(sceneNameType, close);
                        Messages.sprintf("copy cancelled");
                    });

                    Thread copy_thread = new Thread(copy, "Copy Thread");
                    copy_thread.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
            model_operate.getCancel_btn().setOnAction(event -> {
                Main.setProcessCancelled(true);
                Messages.sprintf("Current file cancelled is: " + model_operate.getCopyProcess_values().getCopyTo());
                model_operate.stopTimeLine();
                Main.setProcessCancelled(true);
            });
        });

        Platform.runLater(() -> {
            model_operate.getStart_btn().setDisable(false);
        });

    }

    private class Copy extends Task<Integer> {
        private AtomicInteger counter = new AtomicInteger(list.size());
        private int byteRead;
        private long currentSize;
        private String STATE = "";
        private Path source = null;
        private Path dest = null;
        private SimpleStringProperty rememberAnswer = new SimpleStringProperty(CopyAnswerType.ASK);

        @Override
        protected Integer call() throws Exception {

            Path workDir = Paths.get(Main.conf.getWorkDir()).toRealPath();

            handleWorkDirCheck(workDir);

            if (checkProcessCancellation()) {
                return null;
            }

            handleListCheck(list);


            // boolean copy = false;
            /*
            Destination folder to memory
            See if there are duplicates to avoid to copy
            Iterate fileinfos
            Update fileInfo and update tables etc

             */

            List<FileInfo> duplicatedFiles = new ArrayList<>();

            Iterator<FileInfo> fileInfoIterator = list.iterator();
            while (fileInfoIterator.hasNext()) {
                FileInfo fileInfo = fileInfoIterator.next();
                Messages.sprintf(fileInfo.getOrgPath() + " getWorkDir file: " + fileInfo.getWorkDir());
                List<FileInfo> duplicateByExactDate = workDirSQL.findDuplicateByExactDate(fileInfo);
                if (!duplicateByExactDate.isEmpty()) {
                    duplicatedFiles.add(fileInfo);
                    updateIncreaseDuplicatesProcessValues();
                    fileInfoIterator.remove();
                }
            }

            for (FileInfo fileInfo : list) {

                Messages.sprintf("Copying file: " + fileInfo.getOrgPath() + " dest: " + fileInfo.getWorkDir()
                        + fileInfo.getDestination_Path());
                if (fileInfo.getWorkDir() == null || fileInfo.getWorkDir().isEmpty() && Files.exists(Paths.get(fileInfo.getWorkDir()))) {
                    Messages.warningText(Main.bundle.getString("workDirDoesNotExist"));
                    cancel();
                    break;
                }

                source = Paths.get(fileInfo.getOrgPath());

                if (Files.exists(source)) {

                    dest = Paths.get(fileInfo.getWorkDir() + fileInfo.getDestination_Path());
                    boolean cancellation = checkCancellation(fileInfo, dest);
                    if (cancellation) {
                        Main.setProcessCancelled(true);
                        break;
                    }
//			TODO	duplicates ei toimi oikein. Korjaa!!!!

                    Messages.sprintf("source is: " + source + " dest: " + dest);
                    updateSourceAndDestProcessValues(source, dest);

//                    List<FileInfo> findPossibleExistsFoldersInWorkdir = modelMain.getWorkDir_Handler().findPossibleExistsFoldersInWorkdir(fileInfo);

                    Path dest_test = FileUtils.renameFile(Paths.get(fileInfo.getOrgPath()), dest);
                    if (dest_test != null) {
                        dest = dest_test;
                        STATE = Copy_State.RENAME.getType();
                    } else {
                        STATE = Copy_State.COPY.getType();
                    }

                    SimpleIntegerProperty answer = new SimpleIntegerProperty(-1);

                    Copy_State copy_State = Copy_State.valueOf(STATE);
                    switch (copy_State) {
                        case COPY:
                            if (Files.exists(source)) {
                                if (copyFile(fileInfo, source, dest, STATE, answer, model_operate)) {
                                    updateSourceAndDestProcessValues(source, dest);
                                    updateIncreaseCopyingProcessValues();
                                    if (!fileInfo.isCopied()) {
                                        fileInfo.setCopied(true);
                                        modelMain.getWorkDir_Handler().add(fileInfo);
                                    }
                                }
                            } else {
                                Messages.sprintfError("Source file did not exists!: " + source);

                            }

                            break;
                        case RENAME:
                            if (copyFile(fileInfo, source, dest, STATE, answer, model_operate)) {
                                updateIncreaseRenamedProcessValues();
                                if (!fileInfo.isCopied()) {
                                    fileInfo.setCopied(true);
                                }
                                modelMain.getWorkDir_Handler().add(fileInfo);

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

        private void handleWorkDirCheck(Path workDir) throws Exception {
            if (!Files.exists(workDir)) {
                Messages.sprintfError("Cannot find workdir!");
                Messages.warningText(Main.bundle.getString("cannotFindWorkDir"));
                handleCancellation(true, "Workdir does not exist");
            } else {
                Messages.sprintf("Workdir exists at " + workDir);
            }
        }

        private boolean checkProcessCancellation() {
            if (isCancelled() || Main.getProcessCancelled()) {
                Messages.sprintf("Copy process is cancelled");
                handleCancellation(isCancelled(), "Process cancelled");
                return true;
            }
            return false;
        }

        private void handleListCheck(List<FileInfo> list) {
            if (list.isEmpty()) {
                Messages.warningText("List is empty!");
                handleCancellation(true, "List empty");
            } else {
                model_operate.getTimeline().play();
            }
        }

        private void handleCancellation(boolean condition, String message) {
            if (condition) {
                Messages.sprintf(message);
                Main.setProcessCancelled(true);
                model_operate.stopTimeLine();
                cancel();
            }
        }

        private void updateFilesLeftCounter() {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().setFilesLeft(counter.decrementAndGet());
            });
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
            if (Paths.get(fileInfo.getOrgPath()).getParent().toString().contains(Main.conf.getWorkDir().toString())) {
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

        public boolean copyFile(FileInfo fileInfo, Path source2, Path dest2, String STATE2,
                                SimpleIntegerProperty answer, Model_operate model_operate) {

            if (!Files.exists(source2)) {
                return false;
            }
            boolean destinationDirectoriesCreated = OperateFilesUtils.createDirectories(dest2);
            if (!destinationDirectoriesCreated) {
                Messages.sprintfError("Couldn't not be able to create folder");
                Main.setProcessCancelled(true);
                return false;
            }
            try {
                Path destTmpFile = Paths.get(dest2.toFile() + ".tmp");

                Files.deleteIfExists(destTmpFile);
                Messages.sprintf("Source: " + source2 + " dest: " + dest2);

                InputStream from = new FileInputStream(source2.toFile());
                OutputStream to = new FileOutputStream(destTmpFile.toFile());
                resetAndupdateSourceAndDestProcessValues(source2, dest2);

                long nread = 0L;
                int byteRead = 0;
                byte[] buf = new byte[8192];
                sprintf("----] Starting copying: " + source2);
                while ((byteRead = from.read(buf)) > 0) {
                    if (Main.getProcessCancelled()) {
                        boolean cancelledSucceeded = cleanCancelledFile(from, to, source2, dest2);
                        if (cancelledSucceeded) {
                            Messages.sprintf("Cleanup is done ");
                        } else {
                            Messages.sprintf("Cleanup has FAILED. There will be tmp file will remain: " + destTmpFile);
                        }

                        break;
                    }
                    to.write(buf, 0, byteRead);
                    nread += byteRead;
                    updateIncreaseLastSecondFileSizeProcessValues(byteRead);
                }
                from.close();
                to.flush();
                to.close();
                resetAndUpdateFileCopiedProcessValues();

                if (nread != source2.toFile().length()) {

                    switch (rememberAnswer.get()) {
                        case CopyAnswerType.COPY:
                            answer.set(0);
                            Messages.sprintf("Copy will be done. Value is: " + answer.get());
                            break;
                        case CopyAnswerType.DONTCOPY:
                            answer.set(1);
                            Messages.sprintf("Copy won't be done. Value is: " + answer.get());
                            break;
                        case CopyAnswerType.ASK:
                            Messages.sprintf("Prompting dialogue");
                            FutureTask<SimpleIntegerProperty> task = new FutureTask<>(
                                    new OperateFiles.Dialogue((Window) model_operate.getStart_btn().getScene().getWindow(), fileInfo,
                                            model_operate.getCopyProcess_values().getFilesCopyProgress_MAX_tmp(), answer,
                                            rememberAnswer));
                            Platform.runLater(task);
                            answer = task.get();
                            Messages.sprintf("And the answer is: " + answer.get());
                            break;
                    }

                    if (answer.get() == 0) {
                        renameTmpFileToCorruptedFileExtensions(fileInfo, destTmpFile, dest2, modelMain, listCopiedFiles);
                        Messages.sprintf("renameTmpFileToCorruptedFile: " + destTmpFile);
                    } else if (answer.get() == 1) {
                        Messages.sprintf("Don't keep the file. Tmp file will be deleted: " + destTmpFile);
                        Files.deleteIfExists(destTmpFile);
                    } else if (answer.get() == 2) {
                        Messages.sprintf("Cancel pressed. This is not finished!");
                        cancel();
                        Main.setProcessCancelled(true);
                        return false;

                    }
                } else {
                    renameTmpFileBackToOriginalExtentension(fileInfo, destTmpFile, dest2, modelMain);
                    Messages.sprintf("renameTmpFileBackToOriginalExtentension: " + destTmpFile + " dest: " + dest2);
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
            return true;
        }


        @Override
        protected void failed() {
            model_operate.stopTimeLine();

            TableUtils.refreshAllTableContent(modelMain.tables());
//			model_operate.doneButton(sceneNameType, close);
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
        }

        @Override
        protected void cancelled() {
            Messages.sprintf("OperateFiles COPY were cancelled");
            model_operate.stopTimeLine();
            model_operate.doneButton(sceneNameType, close);
            TableUtils.refreshAllTableContent(modelMain.tables());
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
            model_operate.doneButton(sceneNameType, close);
            model_operate.stopTimeLine();
            Task<Void> saveWorkDirToDatabase = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Messages.sprintf("Step1");
                    if (modelMain.getWorkDir_Handler() == null) {
                        Messages.sprintf("Workdir_handler null");
                    }
                    boolean saveWorkDirList = modelMain.getWorkDir_Handler().saveWorkDirListToDatabase();
                    Messages.sprintf("Step2");
                    TableUtils.refreshAllTableContent(modelMain.tables());
                    if (saveWorkDirList) {
                        Messages.sprintf("saveWorkDirList DONE!!!");
                    } else {
                        Messages.sprintf("saveWorkDirList FAILED");
                    }
                    TableUtils.updateAllFolderInfos(modelMain.tables());
                    TableUtils.refreshAllTableContent(modelMain.tables());
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

        private void renameTmpFileBackToOriginalExtentension(FileInfo fileInfo, Path destTmp, Path dest, ModelMain model_main) {
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

        private synchronized void resetAndupdateSourceAndDestProcessValues(Path source, Path dest) {
            Platform.runLater(() -> {
                model_operate.getCopyProcess_values().setCopyFrom_tmp(source.toString());
                model_operate.getCopyProcess_values().setCopyTo_tmp(dest.toString());
                model_operate.getCopyProcess_values().setCopyProgress(0);
                model_operate.getCopyProcess_values().setFilesCopyProgress_MAX_tmp(source.toFile().length());
            });
        }

        private void writeToDatabase() {
            Messages.sprintf("Insert worked!");
            if (Main.conf.getDrive_connected()) {
                try {

                    Connection connection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir().toString()),
                            Main.conf.getMdir_db_fileName());
                    connection.setAutoCommit(false);
                    boolean inserted = FileInfo_SQL.insertFileInfoListToDatabase(connection, listCopiedFiles, true);
                    if (!inserted) {
                        connection.close();
                        connection = SqliteConnection.connector(Paths.get(Main.conf.getWorkDir().toString()),
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
