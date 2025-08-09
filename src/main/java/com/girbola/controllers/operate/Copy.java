package com.girbola.controllers.operate;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.ModelOperate;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.workdir.WorkDirSQL;
import common.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import static com.girbola.messages.Messages.sprintf;

public class Copy extends Task<Integer> {
    private CopyHelper copyHelper;

    private final String ERROR = Copy.class.getSimpleName();

    List<FileInfo> list = new ArrayList<>();
    ModelOperate modelOperate;
    ModelMain modelMain;
    String sceneNameType;
    boolean close;

    public Copy(List<FileInfo> list, ModelOperate modelOperate, ModelMain modelMain, String sceneNameType, boolean close) {
        this.list = list;
        this.modelOperate = modelOperate;
        this.modelMain = modelMain;
        this.sceneNameType = sceneNameType;
        this.close = close;
        copyHelper = new CopyHelper(modelMain, modelOperate, this);
    }
    private AtomicInteger counter;
    private int byteRead;
    private long currentSize;
    private String STATE = "";
    private Path source = null;
    private Path dest = null;
    private SimpleStringProperty rememberAnswer = new SimpleStringProperty(CopyAnswerType.ASK);

//    @Override
//    protected Integer call() throws Exception {
//        if (!initializeWorkDirectory()) {
//            return null;
//        }
//
//        removeDuplicates();
//        processRemainingFiles();
//        modelOperate.getCopyProcess_values().update();
//        return null;
//    }

    @Override
    protected Integer call() throws Exception {

        Path workDir = Paths.get(Main.conf.getWorkDir()).toRealPath();

        copyHelper.handleWorkDirCheck(workDir);

        if (copyHelper.checkProcessCancellation()) {
            return null;
        }

        copyHelper.handleListCheck(list);

        List<FileInfo> duplicatedFiles = new ArrayList<>();

        Iterator<FileInfo> fileInfoIterator = list.iterator();
        //Removing duplicates before copying
        while (fileInfoIterator.hasNext()) {
            FileInfo fileInfo = fileInfoIterator.next();
            Messages.sprintf(fileInfo.getOrgPath() + " getWorkDir file: " + fileInfo.getWorkDir());
            List<FileInfo> duplicateByExactDate = WorkDirSQL.findDuplicateByExactDate(fileInfo);
//                if(duplicateByExactDate == null) {
//                    break;
//                }
            if (!duplicateByExactDate.isEmpty()) {
                duplicatedFiles.add(fileInfo);
                copyHelper.updateIncreaseDuplicatesProcessValues();
                fileInfoIterator.remove();
            }
        }

        for (FileInfo fileInfo : list) {

            Messages.sprintf("Copying file: " + fileInfo.getOrgPath() + " dest: " + fileInfo.getWorkDir() + fileInfo.getDestination_Path());
            if (fileInfo.getWorkDir() == null || fileInfo.getWorkDir().isEmpty() && Files.exists(Paths.get(fileInfo.getWorkDir()))) {
                Messages.warningText(Main.bundle.getString("workDirDoesNotExist"));
                cancel();
                break;
            }

            source = Paths.get(fileInfo.getOrgPath());

            if (Files.exists(source)) {

                dest = Paths.get(fileInfo.getWorkDir() + fileInfo.getDestination_Path());
                boolean cancellation = copyHelper.checkCancellation(fileInfo, dest);
                if (cancellation) {
                    Main.setProcessCancelled(true);
                    break;
                }
//			TODO	duplicates ei toimi oikein. Korjaa!!!!

                Messages.sprintf("source is: " + source + " dest: " + dest);
                copyHelper.updateSourceAndDestProcessValues(source, dest);

//                    List<FileInfo> findPossibleExistsFoldersInWorkdir = modelMain.getWorkDir_Handler().findPossibleExistsFoldersInWorkdir(fileInfo);

                Path dest_test = FileUtils.renameFile(Paths.get(fileInfo.getOrgPath()), dest);
                if (dest_test != null) {
                    dest = dest_test;
                    STATE = CopyState.RENAME.getType();
                } else {
                    STATE = CopyState.COPY.getType();
                }

                SimpleIntegerProperty answer = new SimpleIntegerProperty(-1);

                CopyState copyState = CopyState.valueOf(STATE);
                switch (copyState) {
                    case COPY:
                        if (Files.exists(source)) {
                            if (copyHelper.copyFile(fileInfo, source, dest, STATE, answer, modelOperate)) {
                                copyHelper.updateSourceAndDestProcessValues(source, dest);
                                copyHelper.updateIncreaseCopyingProcessValues();
                                if (!fileInfo.isCopied()) {
                                    fileInfo.setCopied(true);
                                    WorkDirSQL.insertFileInfo(fileInfo);
                                }
                            }
                        } else {
                            Messages.sprintfError("Source file did not exists!: " + source);

                        }

                        break;
                    case RENAME:
                        if (copyHelper.copyFile(fileInfo, source, dest, STATE, answer, modelOperate)) {
                            copyHelper.updateIncreaseRenamedProcessValues();
                            if (!fileInfo.isCopied()) {
                                fileInfo.setCopied(true);
                            }
                            WorkDirSQL.insertFileInfo(fileInfo);
//                                modelMain.getWorkDirSQL().insertFileInfo(fileInfo);
                        }
                        break;
                    case BROKENFILE:
                        Messages.sprintfError("Broken file were found. Filename: " + fileInfo.getOrgPath());
                        break;
                    default:
                        Messages.sprintfError("Unknown copy state. " + copyState + " Filename: " + fileInfo.getOrgPath());
                        break;
                }
                Messages.sprintf("Dest: " + dest + " STATE: " + STATE);

                copyHelper.updateFilesLeftCounter();
                source = null;
                dest = null;
                STATE = "";
            }
        }
        modelOperate.getCopyProcess_values().update();
        return null;
    }

    @Override
    protected void failed() {
        modelOperate.stopTimeLine();

        TableUtils.refreshAllTableContent(modelMain.tables());
//			modelOperate.doneButton(sceneNameType, close);
        Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), false);
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("OperateFiles COPY were cancelled");
        modelOperate.stopTimeLine();
        modelOperate.doneButton(sceneNameType, close);
        TableUtils.refreshAllTableContent(modelMain.tables());
    }

    @Override
    protected void running() {
        if (!isRunning()) {
            Messages.sprintf("OperateFiles running were stopped");
        } else {
//                Platform.runLater(() -> {
//                    modelOperate.getStart_btn().setDisable(true);
//                    modelOperate.getCancel_btn().setDisable(false);
//                });
        }
    }

    @Override
    protected void succeeded() {
        Messages.sprintf("OperateFiles COPY were succeeded");
        modelOperate.stopTimeLine();
        modelOperate.doneButton(sceneNameType, close);
        modelOperate.stopTimeLine();
        Task<Void> saveWorkDirToDatabase = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Messages.sprintf("Step1");
                boolean saveWorkDirList = WorkDirSQL.saveWorkDirDatabase();
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

}