/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main.tables;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.MDir_Constants;
import com.girbola.Main;
import com.girbola.controllers.main.CopyBatch;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.UpdateFolderInfoContent;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfo_Utils;
import com.girbola.filelisting.GetRootFiles;
import com.girbola.fxml.conflicttableview.ConflictTableViewController;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.Conversion;
import common.utils.FileUtils;
import common.utils.date.DateUtils;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Marko Lokka
 */
public class TableUtils {

	private static final String ERROR = TableUtils.class.getSimpleName();

	public static void showConflictTable(Model_main model_Main, ObservableList<FileInfo> obs) {
		try {
			Parent parent = null;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/conflicttableview/ConflictTableView.fxml"),
					bundle);
			try {
				parent = loader.load();
			} catch (IOException ex) {
				Logger.getLogger(CopyBatch.class.getName()).log(Level.SEVERE, null, ex);
			}

			ConflictTableViewController conflictTableViewController = (ConflictTableViewController) loader
					.getController();
			conflictTableViewController.init(model_Main, obs);
			Scene scene_conflictTableView = new Scene(parent);
			scene_conflictTableView.getStylesheets().add(
					Main.class.getResource(conf.getThemePath() + MDir_Constants.MAINSTYLE.getType()).toExternalForm());

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
	public static void updateTableContent(TableView<FolderInfo> table) {
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
				}
			});
			up.setOnFailed(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					Messages.sprintf("Updating folderinfo failed: " + folderInfo.getFolderPath());
				}
			});
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
			return Collections.max(list);
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
		return fi;
	}

	// public static TableValues findTableValues(String type, Path path, Tables
	// tables) {
	// TableValues tbv = null;
	// for (TableValues tableValues : tables.getSorted_table().getItems()) {
	// if (tableValues.getFolderPath().equals(path.toString())) {
	// return tableValues;
	// }
	// }
	//
	// return tbv;
	// }
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
				TableUtils.updateFolderInfos_FileInfo(tv);
				return true;
			}
		}
		return false;
	}

	public static ChangeListener<Number> progressBarPropertyListener(ProgressBar pbar, Text text) {
		String[] barColorStyleClasses = { "pbar20", "pbar40", "pbar60", "pbar80", "pbar100" };

		ChangeListener<Number> pbarProp = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pbar.getStyleClass().removeAll(barColorStyleClasses);
				pbar.getStyleClass().add("pbar20");
				if ((Double) newValue >= 0.0 && (Double) newValue <= 0.99) {
					pbar.getStyleClass().removeAll(barColorStyleClasses);
					pbar.getStyleClass().add("pbar20");
					updateText(text, newValue);
				} else if ((Double) newValue == 1.0) {
					pbar.getStyleClass().removeAll(barColorStyleClasses);
					pbar.getStyleClass().add("pbar100");
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
		return pbarProp;

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

	@Deprecated
	public static void updateFolderInfos_FileInfo(FolderInfo folderInfo) {
		Messages.sprintf("tableutils updateFolderInfos_FileInfo: " + folderInfo.getFolderPath());

		int bad = 0;
		int good = 0;
		int image = 0;
		int raw = 0;
		int video = 0;
		int confirmed = 0;
		long size = 0;
		int copied = 0;
		int ignored = 0;
		TreeMap<LocalDate, Integer> map = new TreeMap<>();

		List<Long> dateCounter_list = new ArrayList<>();
		if (folderInfo.getFileInfoList() == null) {
			Messages.sprintf("Somehow fileInfo list were null!!!");
			Main.setProcessCancelled(true);
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
			return;
		}

		for (FileInfo fi : folderInfo.getFileInfoList()) {
			if (Main.getProcessCancelled()) {
				return;
			}
			if (fi.isIgnored() || fi.isTableDuplicated()) {
				Messages.sprintf("FileInfo were ignore or duplicated: " + fi.getOrgPath());
				ignored++;
			} else {
				size += fi.getSize();
				if (fi.isCopied()) {
					copied++;
				}
				if (fi.isBad()) {
					bad++;
				}
				if (fi.isConfirmed()) {
					confirmed++;
				}
				if (fi.isGood()) {
					good++;
				}
				if (fi.isIgnored()) {
					Messages.sprintfError("isignored!");
				}
				if (fi.isTableDuplicated()) {
					Messages.sprintfError("isTABLRignored!");
				}
				if (fi.isRaw()) {
					raw++;
				}
				if (fi.isImage()) {
					image++;
				}
				if (fi.isVideo()) {
					video++;
				}
				if (fi.getDate() != 0) {
					dateCounter_list.add(fi.getDate());
				} else {
					fi.getDate();
				}

				LocalDate localDate = null;
				try {
					localDate = LocalDate.of(Integer.parseInt(simpleDates.getSdf_Year().format(fi.getDate())),
							Integer.parseInt(simpleDates.getSdf_Month().format(fi.getDate())),
							Integer.parseInt(simpleDates.getSdf_Day().format(fi.getDate())));

				} catch (Exception ex) {
					Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
				}

				map.put(localDate, 0);
			}
		}
		Messages.sprintf("Copied: " + copied + " files: " + (image + raw + video) + " ignored: " + ignored);
		folderInfo.setConfirmed(confirmed);
		folderInfo.setFolderFiles(image + raw + video);

		folderInfo.setBadFiles(bad);

		folderInfo.setFolderRawFiles(raw);

		folderInfo.setFolderVideoFiles(video);

		folderInfo.setGoodFiles(good);

		folderInfo.setCopied(copied);

		folderInfo.setFolderSize(size);
		folderInfo.setFolderImageFiles(image);
		folderInfo.setFolderVideoFiles(video);

		long min = 0;
		long max = 0;

		if (Files.exists(Paths.get(folderInfo.getFolderPath()))) {
			folderInfo.setConnected(true);
		} else {
			folderInfo.setConnected(false);
		}
		if (!dateCounter_list.isEmpty()) {
			Collections.sort(dateCounter_list);
			try {
				min = Collections.min(dateCounter_list);
				max = Collections.max(dateCounter_list);

			} catch (Exception ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}

		folderInfo.setMinDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(min));
		folderInfo.setMaxDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(max));

		double dateDifferenceRatio = calculateDateDifferenceRatio(map);
		folderInfo.setDateDifferenceRatio(dateDifferenceRatio);
		// sprintf("Datedifference ratio completed");
		// folderInfo.setDateDifferenceRatio(0);
		dateCounter_list.clear();
		bad = 0;
		good = 0;
		image = 0;
		raw = 0;
		video = 0;
		confirmed = 0;

		// sdv;
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
			status_value = (((double) folderFiles - ((double) badFiles + (double) suggested)) / (double) folderFiles)
					* 100;
			status.set((int) Math.floor(status_value));
			status_value = 0;
		} catch (Exception ex) {
			sprintf("updateStatus: " + ex.getMessage());
			status_value = 0;
			status.set((int) status_value);
		}
	}

	public static ChangeListener<Number> updateStatus_listener(IntegerProperty status, int folderFiles, int badFiles,
			int suggested) {
		ChangeListener<Number> cl = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				double status_value = 0;
				try {
					status_value = (((double) folderFiles - ((double) badFiles + (double) suggested))
							/ (double) folderFiles) * 100;
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

	public void check_All_TableView_forChanges(Model_main model_main) {
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
				TableUtils.updateFolderInfos_FileInfo(folderInfo);
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
				TableUtils.updateFolderInfos_FileInfo(folderInfo);

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
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					table.getColumns().get(0).setVisible(false);
					table.getColumns().get(0).setVisible(true);
					table.refresh();
				}
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
					fileInfo = FileInfo_Utils.createFileInfo(file);
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

	public static void refreshAllTableContent(Tables tables) {
		Platform.runLater(() -> {
			refreshTableContent(tables.getAsItIs_table());
			refreshTableContent(tables.getSorted_table());
			refreshTableContent(tables.getSortIt_table());
		});
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

	public static void findPossibleNewDestinationByDate(FileInfo fileInfoToFind, Tables tables) {
		for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
			LocalDate start = DateUtils.parseLocalDateFromString(folderInfo.getMinDate());
			LocalDate end = DateUtils.parseLocalDateFromString(folderInfo.getMaxDate());
			LocalDate toFind = DateUtils.longToLocalDateTime(fileInfoToFind.getDate()).toLocalDate();

//			if(folderInfo.getMinDate())
//			for(FileInfo fileInfo : folderInfo.getFileInfoList()) {
//				FileInfo possibleDuplicate = model_main.getWorkDir_Handler().exists(fileInfo);
//				if(possibleDuplicate!=null) {
//					Messages.sprintf("File were already copied to destination: " + possibleDuplicate.getDestination_Path());
////					fileInfo.setDestination_Path(possibleDuplicate.getDestination_Path());
//				} else {
//					TableUtils.findPossibleNewDestinationByDate(path, tableValues)
//				}
//			}

		}

	}

	public static Path resolveFileDestinationPath(String justFolderName, FileInfo fileInfo, String tableType) {

		String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate())
				.format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
		LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();

		if (tableType.equals(TableType.SORTED.getType())) {
			Path destPath = Paths.get(File.separator + ld.getYear() + File.separator + ld + " - " + justFolderName
					+ File.separator + fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
			return destPath;
		} else if (tableType.equals(TableType.SORTIT.getType())) {
			Path destPath = Paths.get(File.separator + ld.getYear() + File.separator
					+ Conversion.stringTwoDigits(ld.getMonthValue()) + File.separator + fileName + "."
					+ FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
			return destPath;
		} else if (tableType.equals(TableType.ASITIS.getType())) {
			Path destPath = Paths.get(File.separator + justFolderName + File.separator + fileName + "."
					+ FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));
			return destPath;
		}
		return null;
	}

	public static void updateAllFolderInfos(Tables tables) {
		for (FolderInfo folderInfo : tables.getSortIt_table().getItems()) {
			updateFolderInfos_FileInfo(folderInfo);
		}
		for (FolderInfo folderInfo : tables.getSorted_table().getItems()) {
			updateFolderInfos_FileInfo(folderInfo);
		}
		for (FolderInfo folderInfo : tables.getAsItIs_table().getItems()) {
			updateFolderInfos_FileInfo(folderInfo);
		}
	}

	public static void sumUp(Model_main model_main) {

//		
//		int sortitTotalFiles = getTableTotalFiles(model_main.tables().getSortIt_table());
//		int sortitTotalFiles = getTableTotalFiles(model_main.tables().getSortIt_table());
//		int sortitTotalFiles = getTableTotalFiles(model_main.tables().getSortIt_table());
//		
//		int sortedTotalFiles = getTableTotalFiles(model_main.tables().getSorted_table());
//		int sortedTotalFiles = getTableTotalFiles(model_main.tables().getSorted_table());
//		int sortedTotalFiles = getTableTotalFiles(model_main.tables().getSorted_table());
//		
//		int asitisTotalFiles = getTableTotalFiles(model_main.tables().getAsItIs_table());
//		int asitisTotalFiles = getTableTotalFiles(model_main.tables().getAsItIs_table());
//		int asitisTotalFiles = getTableTotalFiles(model_main.tables().getAsItIs_table());
//		
//		int sumAllSorted = model_main.tables().sorted_TableStatistic();
//		int sumAllSortIt = model_main.tables().sortit_TableStatistic();
//		int sumAllAsItIs = model_main.tables().asitis_TableStatistic();

		for (FolderInfo folderInfo : model_main.tables().getSortIt_table().getItems()) {
			model_main.tables().getSortit_TableStatistic().setTotalFiles(
					model_main.tables().getSortit_TableStatistic().getTotalFiles().get() + folderInfo.getFolderFiles());
			model_main.tables().getSortit_TableStatistic()
					.setTotalFilesCopied(model_main.tables().getSortit_TableStatistic().getTotalFilesCopied().get()
							+ folderInfo.getCopied());

			model_main.tables().getSortit_TableStatistic()
					.setTotalFilesSize(model_main.tables().getSortit_TableStatistic().getTotalFilesSize().get()
							+ folderInfo.getFolderSize());
		}
		for (FolderInfo folderInfo : model_main.tables().getSorted_table().getItems()) {
			model_main.tables().getSorted_TableStatistic().setTotalFiles(
					model_main.tables().getSorted_TableStatistic().getTotalFiles().get() + folderInfo.getFolderFiles());
			model_main.tables().getSorted_TableStatistic()
					.setTotalFilesCopied(model_main.tables().getSorted_TableStatistic().getTotalFilesCopied().get()
							+ folderInfo.getCopied());

			model_main.tables().getSorted_TableStatistic()
					.setTotalFilesSize(model_main.tables().getSorted_TableStatistic().getTotalFilesSize().get()
							+ folderInfo.getFolderSize());
		}
		for (FolderInfo folderInfo : model_main.tables().getAsItIs_table().getItems()) {
			model_main.tables().getAsItIs_TableStatistic().setTotalFiles(
					model_main.tables().getAsItIs_TableStatistic().getTotalFiles().get() + folderInfo.getFolderFiles());
			model_main.tables().getAsItIs_TableStatistic()
					.setTotalFilesCopied(model_main.tables().getAsItIs_TableStatistic().getTotalFilesCopied().get()
							+ folderInfo.getCopied());

			model_main.tables().getAsItIs_TableStatistic()
					.setTotalFilesSize(model_main.tables().getAsItIs_TableStatistic().getTotalFilesSize().get()
							+ folderInfo.getFolderSize());
		}

	}

	private static int getTableTotalFiles(TableView<FolderInfo> sortIt_table) {

		return 0;
	}
}
