package com.girbola.fileinfo;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.filelisting.GetRootFiles;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SqliteConnection;

class FileInfo_List_UtilsTest {

	private Path folder = Paths.get("C:\\Temp\\MarkonTestiMDir");

	/*
	 * @Test void createDatabase() throws IOException { // List<Path> fileList = new
	 * ArrayList<>(); List<FileInfo> fileInfoList = new ArrayList<>();
	 * 
	 * try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) { for
	 * (Path path : stream) { if (!Files.isDirectory(path)) { FileInfo finfo =
	 * FileInfo_Utils.createFileInfo(path); fileInfoList.add(finfo); } } } catch
	 * (IOException e) { e.printStackTrace(); }
	 * 
	 * fileInfoList.remove(0); FolderInfo folderInfo = new FolderInfo(folder);
	 * folderInfo.getFileInfoList().addAll(fileInfoList);
	 * TableUtils.updateFolderInfos_FileInfo(folderInfo);
	 * 
	 * List<Path> rootFileList =
	 * GetRootFiles.getRootFiles(Paths.get(folderInfo.getFolderPath())); boolean
	 * changed = FileInfo_List_Utils.cleanFileInfoList(rootFileList, fileInfoList);
	 * if (changed) { System.out.println("Content were changed"); Iterator<FileInfo>
	 * it = fileInfoList.iterator(); while (it.hasNext()) { FileInfo fileInfo =
	 * it.next(); System.out.println("------------fileInfo: " + fileInfo); } try {
	 * Connection connection = SqliteConnection.connector(folder,
	 * Main.conf.getMdir_db_fileName()); connection.setAutoCommit(false);
	 * FileInfo_SQL.insertFileInfoListToDatabase(connection, fileInfoList, false);
	 * connection.commit(); connection.close(); } catch (Exception e) { // TODO:
	 * handle exception }
	 * 
	 * } else { System.out.println("Content were NOT changed"); } // //
	 * FileInfo_List_Utils.cleanFileInfoList(rootFileList, sourceFileList); }
	 */

	@Test
	void loadDatabase() {

		try {
			Connection connection = SqliteConnection.connector(folder, Main.conf.getMdir_db_fileName());
			connection.setAutoCommit(false);
			List<FileInfo> fileInfoList = FileInfo_SQL.loadFileInfoDatabase(connection);

			List<Path> rootFileList = GetRootFiles.getRootFiles(folder);
			for (Path file : rootFileList) {
				System.out.println("------------TRUE FILEEEE::: " + file + " size: " + Files.size(file) + " hash: " + file.toFile().hashCode());
			}

//			rootFileList.remove(0);
//			rootFileList.remove(1);
//			rootFileList.remove(2);
			Collections.sort(rootFileList);
			Collections.sort(fileInfoList, new Comparator<FileInfo>() {
				@Override
				public int compare(FileInfo o1, FileInfo o2) {
					return o1.getOrgPath().compareTo(o2.getOrgPath());
				}
			});
			for (FileInfo fileInfo : fileInfoList) {
				if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {
					System.out.println("------------fileInfo: " + fileInfo + " size: " + fileInfo.getSize()
							+ " real size: " + Files.size(Paths.get(fileInfo.getOrgPath())));
				} else {
					System.out.println("------------DB fileInfo: " + fileInfo);
				}
			}
			for (Path file : rootFileList) {
				System.out.println("------------>>>>>>2TRUE FILEE::: " + file + " size: " + Files.size(file));
			}

			boolean changed = FileInfo_List_Utils.cleanFileInfoList(connection, rootFileList, fileInfoList);
			if (changed) {
				System.out.println("Content were changed\n");
			} else {
				System.out.println("Content were NOT changed\n");
			}
			Collections.sort(fileInfoList, new Comparator<FileInfo>() {
				@Override
				public int compare(FileInfo o1, FileInfo o2) {
					return o1.getOrgPath().compareTo(o2.getOrgPath());
				}
			});
			for (FileInfo fileInfo : fileInfoList) {
				System.out.println("------------fileInfo. ID: " + fileInfo.getFileInfo_id() + " name: " + fileInfo + " size: " + fileInfo.getSize() + " real size: "
						+ Files.size(Paths.get(fileInfo.getOrgPath())));
			}
			for (Path file : rootFileList) {
				System.out.println("------------FILEE::: " + file + " size: " + Files.size(file));
			}

			connection.commit();
			connection.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
