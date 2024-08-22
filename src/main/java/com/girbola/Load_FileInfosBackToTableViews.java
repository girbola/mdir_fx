package com.girbola;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.FolderInfos;
import com.girbola.sql.SQL_Utils;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

public class Load_FileInfosBackToTableViews extends Task<Boolean> {
    private Model_main model_main;
    private Connection connection;

    public Load_FileInfosBackToTableViews(Model_main aModel_Main, Connection aConnection) {
        this.model_main = aModel_Main;
        this.connection = aConnection;
    }

    @Override
    protected Boolean call() throws Exception {
        Messages.sprintf("Load_FileInfosBackToTableViews starts "
                + Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));

        if (!Files.exists(
                Paths.get(Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()))) {
            Messages.sprintf("Can't find "
                    + (Main.conf.getAppDataPath() + File.separator + Main.conf.getConfiguration_db_fileName()));
            return false;
        }
        if(!SQL_Utils.isDbConnected(connection)) {
            return false;
        }

        List<FolderInfos> folderInfos_list = SQL_Utils.loadFolderInfosToTables(connection, model_main);

        if (folderInfos_list == null || folderInfos_list.isEmpty()) {
            Messages.sprintf("folderInfo_list were empty" + Load_FileInfosBackToTableViews.class.getName());
            cancel();
            return false;
        } else {
            for (FolderInfos folderInfos : folderInfos_list) {
                if(Main.getProcessCancelled()) {
                    cancel();
                    return false;
                }
                FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(folderInfos.getFolderPath());
                Messages.sprintf("FolderInfo= " + folderInfo.getFolderPath());
/*                 boolean loadFileInfoIntoFolderInfo = FileInfo_SQL.loadFileInfoDatabase(folderInfo);
               if (!loadFileInfoIntoFolderInfo) {
                    Messages.sprintfError("Something went wrong with loading from FolderInfos: " + folderInfo.getFolderPath());
                    cancel();
                    Main.setProcessCancelled(true);
                }*/
                if(folderInfo == null) {
                    Messages.sprintfError("FolderInfo were null!: " + folderInfos.getFolderPath());
                    continue;
                }
                if(folderInfo.getTableType().equals(TableType.SORTIT.getType())) {
                    model_main.tables().getSortIt_table().getItems().add(folderInfo);
                }
                if(folderInfo.getTableType().equals(TableType.SORTED.getType())) {
                    model_main.tables().getSorted_table().getItems().add(folderInfo);
                }
                if(folderInfo.getTableType().equals(TableType.ASITIS.getType())) {
                    model_main.tables().getAsItIs_table().getItems().add(folderInfo);
                }

            }
/*
            if (!model_main.tables().getSortIt_table().getItems().isEmpty()) {
                populateTable(model_main.tables().getSortIt_table().getItems());
            }
            if (!model_main.tables().getSorted_table().getItems().isEmpty()) {
                populateTable(model_main.tables().getSorted_table().getItems());
            }
            if (!model_main.tables().getAsItIs_table().getItems().isEmpty()) {
                populateTable(model_main.tables().getAsItIs_table().getItems());
            }*/
        }

        return true;
    }

//    private boolean populateTable(List<FolderInfo> folderInfo_list) {
//
//        for (FolderInfo folderInfo : folderInfo_list) {
//            boolean addTable = populateTable(folderInfo);
//            if (!addTable) {
//                Messages.sprintf("Skipping folder scan: " + folderInfo.getFolderPath());
//            }
//        }
//        return true;
//    }

//    private boolean populateTable(FolderInfo folderInfo) {
//        if(Main.getProcessCancelled()) {
//            return false;
//        }
//        Messages.sprintf("populateTable getFolderFiles() is: " + folderInfo.getFolderFiles() + " connected? "
//                + folderInfo.isConnected());
//        if (folderInfo.getJustFolderName().contains("Juhon vanhojen tanssit")) {
//            Messages.sprintf("HMMMMMMMMM");
//        }
//        if (folderInfo.isConnected()) {
//            Connection connection = null;
//            Path path = Paths.get(folderInfo.getFolderPath());
//
//            if (Files.exists(path)) {
//                Messages.sprintf("Populating: " + path);
//                connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
//                        Main.conf.getMdir_db_fileName());
//                List<FileInfo> list = FileInfo_SQL.loadFileInfoDatabase(connection);
//                int counter = 0;
//                List<FileInfo> fileInfoList = new ArrayList<>();
//
//                if (!list.isEmpty()) {
////					Messages.sprintf("FolderInfo loading: " + folderInfo.getFolderPath() + " files == " + list.size());
//                    for (FileInfo fileInfo : list) {
//                        if (fileInfo.isTableDuplicated() || fileInfo.isIgnored()) {
//                            counter++;
//                        } else {
//                            fileInfoList.add(fileInfo);
//                        }
//                    }
//                    if (fileInfoList.size() > 0) {
//                        folderInfo.getFileInfoList().addAll(fileInfoList);
//                        FolderInfo_Utils.calculateFileInfoStatuses(folderInfo);
//                        Messages.sprintf("Counter" + counter + " fileInfoList.size() " + fileInfoList.size()
//                                + " List were empty. Path" + folderInfo.getFolderPath() + " files == "
//                                + folderInfo.getFolderFiles());
//                    } else {
//                        return false;
//                    }
//
//                } else {
//                    Messages.sprintf("Counter" + counter + " fileInfoList.size() " + fileInfoList.size()
//                            + " List were empty. Path" + folderInfo.getFolderPath() + " files == "
//                            + folderInfo.getFolderFiles());
//                }
//            }
//            return SQL_Utils.isDbConnected(connection);
//        }
//        return false;
//    }

    @Override
    protected void cancelled() {
        super.cancelled();
        Messages.sprintfError("Load fileinfo back to table cancelled!");
        Main.setChanged(false);
    }

    @Override
    protected void failed() {
        super.failed();
        Messages.sprintfError("Load fileinfo back to table FAILED!");
        Main.setChanged(false);
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        Messages.sprintf("Load fileinfo back to table SUCCEEDED!");
        TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
        TableUtils.refreshTableContent(model_main.tables().getSorted_table());
        TableUtils.refreshTableContent(model_main.tables().getAsItIs_table());
        Main.setChanged(false);
    }

}
