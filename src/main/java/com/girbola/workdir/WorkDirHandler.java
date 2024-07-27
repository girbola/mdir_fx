package com.girbola.workdir;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.filelisting.GetAllMediaFiles;
import com.girbola.messages.Messages;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import common.utils.Conversion;
import common.utils.date.DateUtils;
import javafx.scene.control.TableView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WorkDirHandler {

	private final String ERROR = WorkDirHandler.class.getSimpleName();
	private boolean workDir;
	private List<FileInfo> workDir_List = new ArrayList<>();

	public boolean isWorkDir_connected() {
		return this.workDir;
	}

//	public void addAllTables(Tables tables) {
//		addTable(tables.getSorted_table());
//		addTable(tables.getSortIt_table());
//	}

	private void addTable(TableView<FolderInfo> table) {
		List<Long> dateList = new ArrayList<>();

		for (FolderInfo folderInfo : table.getItems()) {
			dateList = new ArrayList<>();
			for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
				dateList.add(fileInfo.getDate());
			}
			Collections.sort(dateList);
			long min = Collections.min(dateList);
			long max = Collections.max(dateList);
			if (min != 0) {
				LocalDateTime ld_min = DateUtils.longToLocalDateTime(min);
				LocalDateTime ld_max = DateUtils.longToLocalDateTime(max);

				for (int i = ld_min.getYear(); i < ld_max.getYear(); i++) {
					if (Files.exists(Paths.get(Main.conf.getWorkDir() + File.separator + i))) {
						loadListFromWorkDir_To_List(Main.conf.getWorkDir() + File.separator + i);
					}
				}
			}
			dateList.clear();
			dateList = null;
		}
	}
	/**
	 * Loading all workdir paths to WorkDir_Handler's workDir_List
	 *
	 * @param workDirPath
	 * @return
	 */
	public boolean loadAllLists(Path workDirPath) {
		Messages.sprintf("2WorkDir Handler loadAllLists:'" + workDirPath.toString().length() + "'");
		if (workDirPath.toString().length() == 0) {
			Messages.sprintf("workDirPath were empty!");
			return false;
		}
		if (!Files.exists(workDirPath) && !workDirPath.toString().isBlank()) {

			return false;
		}

		Connection connection = SqliteConnection.connector(workDirPath, Main.conf.getMdir_db_fileName());
		boolean autoCommited = SQL_Utils.setAutoCommit(connection, false);
		if (!autoCommited) {
			Messages.warningText("Can't set autoCommit to false");
			return false;
		}








		if (SQL_Utils.isDbConnected(connection)) {
			List<FileInfo> fileInfo_list = FileInfo_SQL.loadFileInfoDatabase(connection);
			if (!fileInfo_list.isEmpty()) {
				workDir_List.addAll(fileInfo_list);
				Messages.sprintf("fileInfo added at: " + workDirPath + " list size were: " + fileInfo_list.size());
			}
		} else {
			return false;
		}
		SQL_Utils.commitChanges(connection);
		SQL_Utils.closeConnection(connection);





		return true;
	}

	private void loadListFromWorkDir_To_List(String path) {
		Connection connection = SqliteConnection.connector(Paths.get(path), Main.conf.getMdir_db_fileName());
		if (SQL_Utils.isDbConnected(connection)) {
			List<FileInfo> list = FileInfo_SQL.loadFileInfoDatabase(connection);
			if (!list.isEmpty()) {
				workDir_List.addAll(list);
			}
		}
	}

	public FileInfo exists(FileInfo fileInfo_toFind) {
		Iterator<FileInfo> it = workDir_List.iterator();
		final LocalDate ld_toFind = DateUtils.longToLocalDateTime(fileInfo_toFind.getDate()).toLocalDate();
		final int year = ld_toFind.getYear();
		final int month = ld_toFind.getMonthValue();

		while (it.hasNext()) {
			FileInfo fileInfo = it.next();
			if (DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate().getMonthValue() == year
					&& DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate().getMonthValue() == month) {
				if (new File(fileInfo.getOrgPath()).length() == new File(fileInfo.getOrgPath()).length()) {
					Messages.sprintf(
							"FileInfo exists at workdir: " + fileInfo.getWorkDir() + fileInfo.getDestination_Path());
					return fileInfo;
				}
			}
		}
		return null;
	}

	public List<FileInfo> getWorkDir_List() {
		return workDir_List;
	}

	public boolean add(FileInfo fileInfo) {
		for (FileInfo fileInfo_Workdir : workDir_List) {
			if (fileInfo_Workdir.getDate() == fileInfo.getDate()) {
				if (fileInfo_Workdir.getSize() == fileInfo.getSize()) {
					if (fileInfo_Workdir.getOrgPath().equals(fileInfo.getOrgPath())) {
						return false;
					}
				}
			}

		}
		return this.workDir_List.add(fileInfo);

	}

	public boolean saveWorkDirListToDatabase() {
		Messages.sprintf("Before sorting the list the size is: " + workDir_List.size());

		FileInfoUtils.sortByDate_Ascending(workDir_List);

		Messages.sprintf("AFter soring the list the size is: " + workDir_List.size());
		Path destionationPath = null;
		Connection connection = null;
		try {
			destionationPath = Paths.get(Main.conf.getWorkDir() + File.separator);
			if (Files.exists(destionationPath)) {
				connection = SqliteConnection.connector(destionationPath, Main.conf.getMdir_db_fileName());
				connection.setAutoCommit(false);
			}
			boolean dbConnected = SQL_Utils.isDbConnected(connection);
			if (dbConnected) {
				if (SQL_Utils.isDbConnected(connection)) {
					boolean inserting = FileInfo_SQL.insertFileInfoListToDatabase(connection, workDir_List, true);
					if (inserting) {
						Messages.sprintf("Insert worked!");
						connection.commit();
					} else {
						Messages.sprintfError("inserting not working!");
						connection.commit();
					}
				}
			} else {
				Messages.sprintfError(Main.bundle.getString("cannotSaveWorkdirDatabase"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			Messages.warningText(Main.bundle.getString("couldNotSaveDestinationDatabase") + ": " + destionationPath);
		} finally {
			boolean dbConnected = SQL_Utils.isDbConnected(connection);
			if (dbConnected) {
				SQL_Utils.closeConnection(connection);
			}
		}

		return true;
	}

	public void createWorkDirContentForMissingDatabase() {
		Main.conf.getWorkDir();
		List<Path> listOfWorkDirFiles = GetAllMediaFiles.getAllMediaFiles(Paths.get(Main.conf.getWorkDir()));
		if (!listOfWorkDirFiles.isEmpty()) {
			for (Path p : listOfWorkDirFiles) {
				if (Main.getProcessCancelled()) {
					try {
						FileInfo fileInfo = FileInfoUtils.createFileInfo(p);
						add(fileInfo);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public List<FileInfo> findPossibleExistsFoldersInWorkdir(FileInfo fileInfoToSearch) {
		List<FileInfo> list = new ArrayList<>();

		LocalDate yearAndMonthToSearch = DateUtils.longToLocalDateTime(fileInfoToSearch.getDate()).toLocalDate();

		String year = Conversion.stringWithDigits(yearAndMonthToSearch.getYear(), 4);
		String month = Conversion.stringWithDigits(yearAndMonthToSearch.getMonthValue(), 2);
		String day = Conversion.stringWithDigits(yearAndMonthToSearch.getDayOfMonth(), 2);

		Messages.sprintf("year: " + year + " month " + month + " day" + day);

//		Path workDirToSearch = Paths.get(Main.conf.getWorkDir() + File.separator + year + File.separator + month);
		for (FileInfo fileInfo : workDir_List) {
			if (fileInfo.getDate() == fileInfoToSearch.getDate()) {
				if (fileInfo.getSize() == fileInfoToSearch.getSize()) {
					list.add(fileInfo);
					Messages.sprintfError("DUPLICATE FOUND: " + fileInfo.getOrgPath());
				}
			}
		}
		return list;
	}

}
