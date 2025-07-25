
package com.girbola.controllers.main.tables;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.SceneNameType;
import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.*;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.filelisting.GetRootFiles;
import com.girbola.controllers.conflicttableview.ConflictTableViewController;
import com.girbola.controllers.operate.OperateFiles;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.FolderInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.Conversion;
import common.utils.FileUtils;
import common.utils.date.DateUtils;
import common.utils.ui.ScreenUtils;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.girbola.Main.*;
import static com.girbola.concurrency.ConcurrencyUtils.exec;
import static com.girbola.concurrency.ConcurrencyUtils.getExecCounter;
import static com.girbola.controllers.main.tables.FolderInfoUtils.calculateFolderInfoStatus;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;


@Log
public class TableUtils {

    private static final String ERROR = TableUtils.class.getSimpleName();

    public static void showConflictTable(ModelMain model_Main, ObservableList<FileInfo> obs) {
        try {
            Parent parent = null;
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/conflicttableview/ConflictTableView.fxml"), bundle);
            try {
                parent = loader.load();
            } catch (IOException ex) {
                Messages.errorSmth(ERROR, bundle.getString("cannotLoadConflictTable"), null,
                        Misc.getLineNumber(), true);
            }

            ConflictTableViewController conflictTableViewController = loader.getController();
            conflictTableViewController.init(model_Main, obs);

            Scene scene_conflictTableView = new Scene(parent);
            scene_conflictTableView.getStylesheets().add(Main.class.getResource(conf.getThemePath() + MDir_Stylesheets_Constants.MAINSTYLE.getType()).toExternalForm());

            Stage window = new Stage();
            window.setScene(scene_conflictTableView);
            window.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //
//	public static void updateCopiedStatus(TableView<FolderInfo> table) {
//		ObservableList<FolderInfo> list = table.getItems();
//		for (FolderInfo folderInfo : list) {
//			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
//				if (fileInfo.getDestinationPath() != null) {
//					if (!fileInfo.getDestinationPath().isEmpty()) {
//						if (!Files.exists(Paths.get(fileInfo.getDestinationPath()))) {
//							fileInfo.setCopied(false);
//						}
//					}
//				}
//			}
//			updateFolderInfos_FileInfo(folderInfo);
//		}
//
//	}
    public static void updateTableContent(TableView<FolderInfo> table, Tables tables) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            UpdateFolderInfoContent up = new UpdateFolderInfoContent(folderInfo);
            up.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    Messages.sprintf("Updating folderinfo cancelled: " + folderInfo.getFolderPath());
                }
            });
            up.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    Messages.sprintf("Updating folderinfo succeeded: " + folderInfo.getFolderPath());
                    TableUtils.calculateTableViewsStatistic(tables);
                }
            });
            up.setOnFailed(event -> Messages.sprintf("Updating folderinfo failed: " + folderInfo.getFolderPath()));
            exec.submit(up);
        }
    }

    public static double calculateDateDifferenceRatio(TreeMap<LocalDate, Integer> map) {
        List<Double> list = new ArrayList<>();

        double tester = 0;
        boolean pass = false;
        LocalDate d1 = null;

        for (Entry<LocalDate, Integer> entry : map.entrySet()) {
            if (!pass) {
                pass = true;
                d1 = entry.getKey();
            } else {
                LocalDate d2 = entry.getKey();
                Period per = Period.between(d1, d2);
                double days = per.getDays();
                list.add(days - tester);
                d1 = d2;
            }
        }
        double sum = 0;
        for (Double db : list) {
            sum += db;
        }
        if (list.isEmpty()) {
            return 0;
        } else {
            return sum;
        }
    }

    public static FileInfo findFileInfo(String tableType, Path path, Tables tables) {
        sprintf("findFileInfo starting... path is: " + path);
        FileInfo fi = null;
        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            if (folderInfo.getFolderPath().equals(path.getParent().toString())) {
                List<FileInfo> fileInfo_list = folderInfo.getFileInfoList();
                for (FileInfo fileInfo : fileInfo_list) {
                    if (fileInfo.getOrgPath().equals(path.toString())) {
                        return fileInfo;
                    }
                }
            }
        }
        return null;
    }

    public static FolderInfo findTableValues(Path path, ObservableList<FolderInfo> tableValues) {
        // TableValues tbv = null;
        for (FolderInfo tv : tableValues) {
            if (tv.getFolderPath().equals(path.toString())) {
                return tv;
            }
        }
        return null;
    }

    public static boolean hasTable(ObservableList<FolderInfo> tableValues_list, FolderInfo folderInfo) {
        for (FolderInfo tv : tableValues_list) {
            if (tv.getFolderPath().equals(folderInfo.getFolderPath())) {
                sprintf("hasTabe found! " + tv.getFolderPath());
                tv.setFileInfoList(folderInfo.getFileInfoList());
                FolderInfoUtils.calculateFolderInfoStatus(tv);
                return true;
            }
        }
        return false;
    }

    public static ChangeListener<Number> progressBarPropertyListener(ProgressBar pbar, Text text) {
        String[] barColorStyleClasses = {"pbar20", "pbar40", "pbar60", "pbar80", "pbar100"};

        return new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pbar.getStyleClass().removeAll(barColorStyleClasses);
                pbar.getStyleClass().add(barColorStyleClasses[0]);
                if ((Double) newValue >= 0.0 && (Double) newValue <= 0.99) {
                    pbar.getStyleClass().removeAll(barColorStyleClasses);
                    pbar.getStyleClass().add(barColorStyleClasses[0]);
                    updateText(text, newValue);
                } else if ((Double) newValue == 1.0) {
                    pbar.getStyleClass().removeAll(barColorStyleClasses);
                    pbar.getStyleClass().add(barColorStyleClasses[4]);
                    text.setText(bundle.getString("done"));
                }
                sprintf("TableCell_ProgressBar_SortIt: " + newValue);
            }

            private void updateText(Text text, Number newValue) {
                int value = ((int) ((Double) newValue * 100));
                // sprintf("sortit Updating text: " + value + " new value was: "
                // + newValue);
                text.setText(value + "%");
            }

        };

    }

    public static void defineMinMaxDate(FolderInfo folderInfo) {
        List<Long> dateCounter = new ArrayList<>();
        for (FileInfo fi : folderInfo.getFileInfoList()) {
            if (fi.getDate() != 0) {
                dateCounter.add(fi.getDate());
            }
        }
        if (!dateCounter.isEmpty()) {
            Collections.sort(dateCounter);
            folderInfo.setMinDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(Collections.min(dateCounter)));
            folderInfo.setMaxDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(Collections.max(dateCounter)));
        }

    }

    public static ChangeListener<Number> folderFiles_ChangeListener(String folderPath) {
        ChangeListener<Number> cl = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sprintf("folderPath " + folderPath + " folderF: " + newValue);
            }
        };
        return cl;
    }

    public static void updateStatus(IntegerProperty status, int folderFiles, int badFiles, int suggested) {
        double status_value = 0;
        try {
            status_value = (((double) folderFiles - ((double) badFiles + (double) suggested)) / (double) folderFiles) * 100;
            status.set((int) Math.floor(status_value));
            status_value = 0;
        } catch (Exception ex) {
            sprintf("updateStatus: " + ex.getMessage());
            status_value = 0;
            status.set((int) status_value);
        }
    }

    public static ChangeListener<Number> updateStatus_listener(IntegerProperty status, int folderFiles, int badFiles, int suggested) {
        ChangeListener<Number> cl = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double status_value = 0;
                try {
                    status_value = (((double) folderFiles - ((double) badFiles + (double) suggested)) / (double) folderFiles) * 100;
                    status.set((int) Math.floor(status_value));
                    status_value = 0;
                } catch (Exception ex) {
                    status_value = 0;
                    status.set((int) status_value);
                }
            }
        };
        return cl;
    }

    public static void updateTableValuesStatus(IntegerProperty status, int folderFiles, int badFiles, int suggested) {
        sprintf("folderFiles: " + folderFiles + " getBadFiles: " + badFiles + " getSuggested " + suggested);

        if (badFiles >= 1 || suggested >= 1) {
            sprintf("there were bad files");
            int a = (folderFiles - (badFiles + suggested));
            if (a == 0) {
                sprintf("a were 0");
                status.set(0);
            } else {
                sprintf("a were greater than 0");
                double result = (((double) a / (double) folderFiles) * 100);
                sprintf("getFolderFiles(): " + folderFiles + " a is: " + a + " result: " + result);
                if (result < 100 && result >= 99) {
                    status.set(99);
                } else {
                    status.set((int) Math.floor(result));
                }
            }
        } else if (badFiles == 0 && suggested == 0) {
            status.set(100);
        } else {
            Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
        }
    }

    public static void setHandleDividingTableWidthEqually(TableView<FolderInfo> table, double divider) {
        Messages.sprintf("Divider is: " + divider);
        Platform.runLater(() -> {
            table.setPrefWidth(divider);
            table.setMinWidth(divider);
            table.setMaxWidth(divider);
        });
    }

    public static void setWidth(HBox hBox, double width) {
        Messages.sprintf("Setting width started");
        Platform.runLater(() -> {
            hBox.setPrefWidth(width);
            hBox.setMinWidth(width);
            hBox.setMaxWidth(width);
        });
    }


    public static TableType resolvePath(Path p) {
        // sprintf("REGULAR EXPRESSIONS STARTED");

        int numberTotal = 0;
        int letterTotal = 0;
        int characterTotal = 0;
        int spaceCount = 0;

        String path = p.getFileName().toString();
        if (path.contains("Pictures") || path.contains("Videos")) {
            return TableType.SORTIT;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (Character.isLetter(c)) {
                letterTotal++;
            } else if (Character.isDigit(c)) {
                numberTotal++;
            } else if (c == ' ') {
                spaceCount++;
            } else {
                characterTotal++;
            }
        }

        /*
         * Lisää 2014-12-11 ja 2012_12_05
         */

        // 100Canon jne
        if (numberTotal == 3 && letterTotal == 5 && characterTotal == 0 && spaceCount == 0) { // The
            // most
            // common
            // format
            // 123Canon
            return TableType.SORTIT;

            // O'layreys pub 2013
        } else if (numberTotal >= 0 && letterTotal >= 1 && characterTotal >= 0 && spaceCount >= 0) { // Just
            // letters
            return TableType.SORTED;
        } else if (numberTotal >= 1 && letterTotal == 0 && characterTotal == 0 && spaceCount == 0) { // 1-9
            // numbers
            return TableType.SORTIT;
        } else {
            return TableType.SORTIT;
        }
    }


    public static List<TableView<FolderInfo>> getAllTables(Tables tables) {
        return Arrays.<TableView<FolderInfo>>asList(tables.getSortIt_table(), tables.getSorted_table(), tables.getAsItIs_table());
    }

    public void check_All_TableView_forChanges(ModelMain model_main) {
        Iterator<FolderInfo> sorted_it = model_main.tables().getSorted_table().getItems().iterator();
        Iterator<FolderInfo> sortit_it = model_main.tables().getSortIt_table().getItems().iterator();

        while (sorted_it.hasNext()) {
            FolderInfo folderInfo = sorted_it.next();
            List<Path> currentPath_root_list = null;
            try {
                currentPath_root_list = GetRootFiles.getRootFiles(Paths.get(folderInfo.getFolderPath()));
            } catch (IOException ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
            if (currentPath_root_list.isEmpty()) {
                Main.setProcessCancelled(true);
                errorSmth(ERROR, "", null, Misc.getLineNumber(), true);

            }
            List<FileInfo> list = validateFileInfoList(currentPath_root_list, folderInfo.getFileInfoList());
            if (!list.isEmpty()) {
                folderInfo.setFileInfoList(list);
                folderInfo.setState("Updated");
                FolderInfoUtils.calculateFolderInfoStatus(folderInfo);
                TableUtils.refreshTableContent(model_main.tables().getSorted_table());
                // model_main.getTables().getSorted_table().getColumns().get(0).setVisible(false);
                // model_main.getTables().getSorted_table().getColumns().get(0).setVisible(true);

                Messages.sprintf("Sorted list had changed content!");
            }
        }
        while (sortit_it.hasNext()) {
            FolderInfo folderInfo = sortit_it.next();
            List<Path> currentPath_root_list = null;
            try {
                currentPath_root_list = GetRootFiles.getRootFiles(Paths.get(folderInfo.getFolderPath()));
            } catch (IOException ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
            if (currentPath_root_list.isEmpty()) {
                Main.setProcessCancelled(true);
                errorSmth(ERROR, "", null, Misc.getLineNumber(), true);

            }
            List<FileInfo> list = validateFileInfoList(currentPath_root_list, folderInfo.getFileInfoList());
            if (!list.isEmpty()) {
                folderInfo.setFileInfoList(list);
                FolderInfoUtils.calculateFolderInfoStatus(folderInfo);

                folderInfo.setState("Updated");
                model_main.tables().getSortIt_table().getColumns().get(0).setVisible(false);
                model_main.tables().getSortIt_table().getColumns().get(0).setVisible(true);

                Messages.sprintf("Sortit list had changed content!");
            }
        }
    }

    public static void refreshTableContent(TableView<?> table) {
        if (table == null) {
            Messages.sprintfError("refreshTableContent - Table were null at TableUtils. Line: " + Misc.getLineNumber());
            return;
        }
        if (table.getColumns().get(0) != null) {
            Platform.runLater(() -> {
                table.getColumns().get(0).setVisible(false);
                table.getColumns().get(0).setVisible(true);
                table.refresh();
            });
        }

    }

    public static boolean checkChangedContent(TableView<FolderInfo> table) {

        for (FolderInfo fo : table.getSelectionModel().getSelectedItems()) {
            Messages.sprintf("FolderInfo check....  " + fo.getFolderPath());
            if (Main.getProcessCancelled()) {
                return false;
            }
            List<FileInfo> fileInfo_list = fo.getFileInfoList();

            List<Path> currentPath_root_list = null;
            try {
                currentPath_root_list = GetRootFiles.getRootFiles(Paths.get(fo.getFolderPath()));
            } catch (IOException ex) {
                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            }
            if (currentPath_root_list.isEmpty()) {
                Main.setProcessCancelled(true);
                errorSmth(ERROR, "", null, Misc.getLineNumber(), true);

            }
            List<FileInfo> list = validateFileInfoList(currentPath_root_list, fileInfo_list);

            if (list.isEmpty()) {
                Messages.sprintf("list were empty!: " + list.size());
                return false;
            }
            fo.setFileInfoList(list);
            Messages.sprintf("list were not empty!: " + list.size());
            return true;
        }
        return false;
    }

    private static List<FileInfo> validateFileInfoList(List<Path> currentPath_root_list, List<FileInfo> fileInfo_list) {
        List<FileInfo> theList = new ArrayList<>();
        Iterator<Path> it = currentPath_root_list.iterator();
        while (it.hasNext()) {
            Path file = it.next();
            Messages.sprintf("File name is: " + file);
            FileInfo fileInfo = hasFileInfo_In_List(file, fileInfo_list);
            if (fileInfo == null) {
                try {
                    fileInfo = FileInfoUtils.createFileInfo(file);
                    Messages.sprintf("fileInfo created: " + fileInfo.toString());
                } catch (IOException ex) {
                    Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
                }
            }
            if (fileInfo != null) {
                theList.add(fileInfo);
            }
        }
        return theList;
    }

    private static FileInfo hasFileInfo_In_List(Path file, List<FileInfo> fileInfo_list) {
        Iterator<FileInfo> it = fileInfo_list.iterator();
        while (it.hasNext()) {
            FileInfo fi = it.next();
            if (fi.getOrgPath().equals(file.toString())) {
                return fi;
            }
        }
        return null;
    }

    public static boolean mergeSameFilesIntoFolderByDateFiles(FolderInfo folderInfoToFind, Tables table) {
        if (table.getSorted_table().getItems().isEmpty() || table.getSortIt_table().getItems().isEmpty() || table.getAsItIs_table().getItems().isEmpty()) {
            Messages.sprintfError("All tables contents are empty");
            return false;
        }
        ListIterator<FolderInfo> folderInfoSortItListIterator = table.getSortIt_table().getItems().listIterator();
        while (folderInfoSortItListIterator.hasNext()) {
            FolderInfo folderInfo = folderInfoSortItListIterator.next();
            ListIterator<FileInfo> fileInfoList_SortItListIterator = folderInfo.getFileInfoList().listIterator();
//			while(fileInfoList_SortItListIterator.hasNext()) {
//				findExistingFolder()
//			}

        }
        return false;
    }

    public static boolean checkTableDuplicates(FolderInfo folderInfoToFind, TableView<FolderInfo> table) {
        if (table.getItems().isEmpty()) {
            return false;
        }
        for (FolderInfo folderInfo : table.getItems()) {
            if (folderInfo.getFolderPath().equals(folderInfoToFind.getFolderPath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkAllTablesForDuplicates(FolderInfo folderInfoToFind, Tables tables) {
        Messages.sprintf("checkAllTablesForDuplicates started");
        if (checkTableDuplicates(folderInfoToFind, tables.getSorted_table())) {
            Messages.sprintf("1checkAllTablesForDuplicates dup found " + folderInfoToFind.getFolderPath());
            return true;
        }

        if (checkTableDuplicates(folderInfoToFind, tables.getSortIt_table())) {
            Messages.sprintf("2checkAllTablesForDuplicates dup found " + folderInfoToFind.getFolderPath());
            return true;
        }

        if (checkTableDuplicates(folderInfoToFind, tables.getAsItIs_table())) {
            Messages.sprintf("3checkAllTablesForDuplicates dup found " + folderInfoToFind.getFolderPath());
            return true;
        }
        Messages.sprintf("checkAllTablesForDuplicates NOT dup found? " + folderInfoToFind.getFolderPath());
        return false;
    }

//    public static void refreshAllTableContent(Tables tables, TableView<FolderInfo> tableView) {
//        ConcurrencyUtils.initNewSingleExecutionService();
//
//        Task<Void> refreshTableViewTask = new RefreshTableContent(tables.getTableByType(tableView.getId()));
//        Thread refreshTableViewThread = new Thread(refreshTableViewTask, "refreshTableViewThread");
//        exec[ConcurrencyUtils.getExecCounter()].submit(refreshTableViewThread);
//
//        calculateTableViewsStatistic(tables);
//        // TODO Folderinfo fileList päivittäminen sekä tablestaticstis päivittäminen. Nyt ne on jtoenkin sekaisin
//
//        updateTableViewStatistic(tables, tableView);
//    }

/*    public static void updateTableViewStatistic(Tables tables, TableView<FolderInfo> tableView) {
        CalculateTableViewsStatistic calculateTableViewsStatisticTask = new CalculateTableViewsStatistic(tables, tableView);
        Thread calculateTableViewsStatisticThread = new Thread(calculateTableViewsStatisticTask, "calculateTableViewsStatisticThread");
        exec[ConcurrencyUtils.getExecCounter()].submit(calculateTableViewsStatisticThread);
    }*/

    public static void refreshAllTableContent(Tables tables) {
        Messages.sprintf("refreshAllTableContent started");
        ConcurrencyUtils.initNewSingleExecutionService();

        RefreshAllTableContent refreshSortItTableTask = new RefreshAllTableContent(tables);

        calculateTableViewsStatistic(tables);

    }

    public static void clearTablesContents(Tables tables) {
        Platform.runLater(() -> {
            tables.getAsItIs_table().getItems().removeAll();
            tables.getSorted_table().getItems().removeAll();
            tables.getSortIt_table().getItems().removeAll();
            tables.getAsItIs_table().getItems().clear();
            tables.getSorted_table().getItems().clear();
            tables.getSortIt_table().getItems().clear();
            refreshAllTableContent(tables);
        });
    }

    public static Path resolveFileDestinationPath(String justFolderName, FileInfo fileInfo, String tableType) {

        String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
        LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();

        if (tableType.equals(TableType.SORTED.getType())) {
            return Paths.get(File.separator + ld.getYear() + File.separator + ld + " - " + justFolderName + File.separator + fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
        } else if (tableType.equals(TableType.SORTIT.getType())) {
            return Paths.get(File.separator + ld.getYear() + File.separator + Conversion.stringTwoDigits(ld.getMonthValue()) + File.separator + fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
        } else if (tableType.equals(TableType.ASITIS.getType())) {
            return Paths.get(File.separator + justFolderName + File.separator + fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
        }
        return null;
    }

    public static void updateAllFolderInfos(Tables tables) {
        for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
            calculateFolderInfoStatus(folderInfo);
        }
        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            calculateFolderInfoStatus(folderInfo);
        }
        for (FolderInfo folderInfo : tables.getAsItIs_table().getItems()) {
            calculateFolderInfoStatus(folderInfo);
        }
    }

    public static void calculateTableViewsStatistic(Tables tables) {

//        CalculateTableViewsStatistic calculateTableViewsStatisticSortit = new CalculateTableViewsStatistic(tables, tables.getSortIt_table());
//
//        CalculateTableViewsStatistic calculateTableViewsStatisticSorted = new CalculateTableViewsStatistic(tables, tables.getSorted_table());
//        CalculateTableViewsStatistic calculateTableViewsStatisticAsItIs = new CalculateTableViewsStatistic(tables, tables.getAsItIs_table());
//
        //ConcurrencyUtils.initNewSingleExecutionService();

//        CalculateTableViewsStatistic calculateTableViewsStatisticSortItTask = new CalculateTableViewsStatistic(tables, tables.getSortIt_table());
//        Thread calculateTableViewsStatisticSortItThread = new Thread(calculateTableViewsStatisticSortItTask, "calculateTableViewsStatisticSortItThread");
//        exec[ConcurrencyUtils.getExecCounter()].submit(calculateTableViewsStatisticSortItThread);
//
//        CalculateTableViewsStatistic calculateTableViewsStatisticSortedTask = new CalculateTableViewsStatistic(tables, tables.getSorted_table());
//        Thread calculateTableViewsStatisticSortedThread = new Thread(calculateTableViewsStatisticSortedTask, "calculateTableViewsStatisticSortedThread");
//        exec[ConcurrencyUtils.getExecCounter()].submit(calculateTableViewsStatisticSortedThread);
//
//        CalculateTableViewsStatistic calculateTableViewsStatisticAsItIsTask = new CalculateTableViewsStatistic(tables, tables.getAsItIs_table());
//        Thread calculateTableViewsStatisticAsItIsItThread = new Thread(calculateTableViewsStatisticAsItIsTask, "calculateTableViewsStatisticSortItThread");
//        exec[ConcurrencyUtils.getExecCounter()].submit(calculateTableViewsStatisticAsItIsItThread);
//
//        refreshAllTableContent(tables);
    }

    public static void removeTableViewEmptyFolderInfos(Tables tables) {
//        removeTableViewsEmptyFolderInfo(tables.getSortIt_table());
//        removeTableViewsEmptyFolderInfo(tables.getSorted_table());
//        removeTableViewsEmptyFolderInfo(tables.getAsItIs_table());
        ConcurrencyUtils.initNewSingleExecutionService();

        Task<Void> removeSortItEmptyFoldersFromTableViews = new CleanTableView(tables.getSortIt_table());
        exec[getExecCounter()].submit(removeSortItEmptyFoldersFromTableViews);

        Task<Void> removeSortedEmptyFoldersFromTableViews = new CleanTableView(tables.getSorted_table());
        exec[getExecCounter()].submit(removeSortedEmptyFoldersFromTableViews);

        Task<Void> removeAsItIsEmptyFoldersFromTableViews = new CleanTableView(tables.getAsItIs_table());
        exec[getExecCounter()].submit(removeAsItIsEmptyFoldersFromTableViews);


//        Platform.runLater(() -> {
//            removeTableViewsEmptyFolderInfo(tables.getSortIt_table());
//            removeTableViewsEmptyFolderInfo(tables.getSorted_table());
//            removeTableViewsEmptyFolderInfo(tables.getAsItIs_table());
//        });
    }

    private static void removeTableViewsEmptyFolderInfo(TableView<FolderInfo> items) {
        Iterator<FolderInfo> iterator = items.getItems().iterator();
        while (iterator.hasNext()) {
            FolderInfo folderInfo = items.getItems().iterator().next();
            if (folderInfo.getFileInfoList().size() == 0) {
                iterator.remove();
            }
        }
    }

    public static void saveChangesContentsToTables(Tables tables) {
        for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
            if (folderInfo.getChanged()) {
                saveFolderInfoToDatabase(folderInfo);
                folderInfo.setChanged(false);
            }
        }

        for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
            if (folderInfo.getChanged()) {
                saveFolderInfoToDatabase(folderInfo);
                folderInfo.setChanged(false);
            }
        }

        for (FolderInfo folderInfo : tables.getAsItIs_table().getItems()) {
            if (folderInfo.getChanged()) {
                saveFolderInfoToDatabase(folderInfo);
                folderInfo.setChanged(false);
            }
        }

        Main.setChanged(false);
    }

    private static void saveFolderInfoToDatabase(FolderInfo folderInfo) {
        try {
            /*
             * Adds FolderInfo into table folderInfo.db. Stores: FolderPath, TableType and
             * Connection status when this was saved Connects to current folder for existing
             * or creates new one called fileinfo.db
             */
            // Inserts all data info fileinfo.db
            FileInfo_SQL.insertFileInfoListToDatabase(folderInfo, false);

            Connection fileList_connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()), Main.conf.getMdir_db_fileName());
            fileList_connection.setAutoCommit(false);

            FolderInfo_SQL.saveFolderInfoToDatabase(fileList_connection, folderInfo);
            SQL_Utils.commitChanges(fileList_connection);
            SQL_Utils.closeConnection(fileList_connection);
        } catch (Exception e) {
            Messages.warningText(Main.bundle.getString("cannotSaveFoldernfoToDatabase"));
        }

    }

    public static void cleanTables(Tables tables) {
        boolean sorted = cleanTable(tables.getSorted_table());
        if (sorted) {
            refreshTableContent(tables.getSorted_table());
        }
        boolean sortit = cleanTable(tables.getSortIt_table());
        if (sortit) {
            refreshTableContent(tables.getSortIt_table());
        }
        boolean asitis = cleanTable(tables.getAsItIs_table());
        if (asitis) {
            refreshTableContent(tables.getAsItIs_table());
        }
    }

    private static boolean cleanTable(TableView<FolderInfo> table) {

        ObservableList<FolderInfo> toRemove = FXCollections.observableArrayList();
        for (FolderInfo folderInfo : table.getItems()) {
            if (folderInfo.getFileInfoList().isEmpty()) {
                toRemove.add(folderInfo);
                Messages.sprintf("TOREMOVE: " + folderInfo.getFolderPath() + " FILES: " + folderInfo.getFolderSize());
            }
        }
        table.getItems().removeAll(toRemove);
        return true;
    }

    private static Pane getPaneFromParent(Parent parent, String id) {
        Pane pane = (Pane) parent;
        if (pane instanceof VBox) {
            Messages.sprintf("pane: " + pane.getId());
            if (pane.getId().contains("table_vbox")) {
                VBox main = (VBox) pane;
                Messages.sprintf("main: " + main.getId());
                for (Node node : main.getChildren()) {
                    if (node instanceof HBox) {
                        if (node.getId().equals(id) && node.getId().equals("showHideButton_hbox")) {
                            return (HBox) node;
                        }
                        if (node.getId().equals(id) && node.getId().equals("buttons_hbox")) {
                            return (HBox) node;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static int getVisibleTables(ModelMain model_main) {
        int visibles = 0;
        if (model_main.tables().getSortIt_table().isVisible()) {
            visibles++;
        }
        if (model_main.tables().getSorted_table().isVisible()) {
            visibles++;
        }
        if (model_main.tables().getAsItIs_table().isVisible()) {
            visibles++;
        }

        return visibles;
    }

    public static void handleTableStates(ModelMain model_main, Button hide_btn) {

        int visibles = getVisibleTables(model_main);

        double tableWidth = Math.floor(ScreenUtils.screenBouds().getWidth() / visibles);
        double buttonWidth = hide_btn.getLayoutBounds().getWidth();
        double divideredWidth = Math.floor(model_main.tables().getTables_rootPane().getWidth() / visibles);
        Messages.sprintf("DividerWidth: " + divideredWidth);

//        HBox showHideButton_hbox_sortit = (HBox) getPaneFromParent(model_main.tables().getSortIt_table().getParent(), "showHideButton_hbox");
//        HBox buttons_hbox_sortit = (HBox) getPaneFromParent(model_main.tables().getSortIt_table().getParent(), "buttons_hbox");
//
//        HBox showHideButton_hbox_sorted = (HBox) getPaneFromParent(model_main.tables().getSorted_table().getParent(), "showHideButton_hbox");
//        HBox buttons_hbox_sorted = (HBox) getPaneFromParent(model_main.tables().getSorted_table().getParent(), "buttons_hbox");
//
//        HBox showHideButton_hbox_asitis = (HBox) getPaneFromParent(model_main.tables().getAsItIs_table().getParent(), "showHideButton_hbox");
//        HBox buttons_hbox_asitis = (HBox) getPaneFromParent(model_main.tables().getAsItIs_table().getParent(), "buttons_hbox");

        if (model_main.tables().getSortIt_table().isVisible()) {

        } else {

        }

        if (model_main.tables().getSorted_table().isVisible()) {

        } else {

        }

        if (model_main.tables().getAsItIs_table().isVisible()) {

        } else {

        }
    }

    public static void setParentWidths(Parent parent, double tableWidth) {
        Platform.runLater(() -> {
            parent.prefWidth(tableWidth);
            parent.minWidth(tableWidth);
            parent.maxWidth(tableWidth);
            Messages.sprintf("Parents parent is: " + parent.toString() + " tableWidth: " + tableWidth);
        });
    }

    public static void hideTooltip(Control control) {
        control.getTooltip().setText("");
        control.getTooltip().hide();
    }

    public static void copySelectedTableRows(ModelMain model_main, TableView<FolderInfo> table, String tableType) {
        if (Main.conf.getWorkDir() == null) {
            Messages.warningText("copySelectedTableRows Workdir were null");
            return;
        }
        if (Main.conf.getWorkDir().isEmpty()) {
            Messages.warningText("copySelectedTableRows Workdir were empty");
            return;
        }
        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            if (FolderInfoUtils.hasBadFiles(folderInfo)) {
                Messages.sprintf("1badBadFiles: " + folderInfo.getFolderPath());
                continue;
            }

            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
                Path destPath = TableUtils.resolveFileDestinationPath(folderInfo.getJustFolderName(), fileInfo, tableType);
                if (destPath != null) {
                    Messages.sprintf("destPath NOT null: " + destPath);
                    if (!destPath.toString().equals(fileInfo.getDestination_Path())) {
                        fileInfo.setWorkDir(Main.conf.getWorkDir());
                        fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                        fileInfo.setDestination_Path(destPath.toString());
                        fileInfo.setCopied(false);
                        Main.setChanged(true);
                        if (!folderInfo.getChanged()) {
                            folderInfo.setChanged(true);
                        }
                    }
                }
                Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
            }
        }

        List<FileInfo> list = new ArrayList<>();
        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            if (FolderInfoUtils.hasBadFiles(folderInfo)) {
                Messages.sprintf("badFiles: " + folderInfo.getFolderPath());
                continue;
            }
            list.addAll(folderInfo.getFileInfoList());
        }

        OperateFiles operateFiles = new OperateFiles(list, true, model_main, SceneNameType.MAIN.getType());
//
//        operate.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent event) {
//                for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
//                    calculateFileInfoStatuses(folderInfo);
//                }
//                TableUtils.refreshAllTableContent(model_main.tables());
//            }
//        });
//        operate.setOnFailed(event -> {
//            Messages.warningText("Copy process failed");
//        });
//        operate.setOnCancelled(event -> {
//            Messages.sprintf("Copy process were cancelled");
//        });
//
//        Thread thread = new Thread(operate, "Operate Thread");
//        ExecutorService exec = Executors.newSingleThreadExecutor();
//        exec.submit(thread);

    }

    public static void addToBatchSelectedTableRows(ModelMain model_main, TableView<FolderInfo> table, String tableType) {
        if (Main.conf.getWorkDir() == null) {
            Messages.warningText("copySelectedTableRows Workdir were null");
            return;
        }
        if (Main.conf.getWorkDir().isEmpty()) {
            Messages.warningText("copySelectedTableRows Workdir were empty");
            return;
        }

        for (FolderInfo folderInfo : table.getSelectionModel().getSelectedItems()) {
            if (FolderInfoUtils.hasBadFiles(folderInfo)) {
                continue;
            }

            for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

                Path destPath = TableUtils.resolveFileDestinationPath(folderInfo.getJustFolderName(), fileInfo, tableType);
                if (destPath != null) {
                    if (!destPath.toString().equals(fileInfo.getDestination_Path())) {
                        fileInfo.setWorkDir(Main.conf.getWorkDir());
                        fileInfo.setWorkDirDriveSerialNumber(Main.conf.getWorkDirSerialNumber());
                        fileInfo.setDestination_Path(destPath.toString());
                        fileInfo.setCopied(false);
                        Main.setChanged(true);
                    }
                }
                Messages.sprintf("Destination path would be: " + fileInfo.getDestination_Path());
            }
        }
    }
}
