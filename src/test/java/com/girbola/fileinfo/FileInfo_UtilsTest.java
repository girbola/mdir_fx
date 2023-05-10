package com.girbola.fileinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.girbola.messages.Messages;

class FileInfo_UtilsTest {

	// @Test
	void testMoveFile() {
		/*
		 * Path path =
		 * Paths.get("C:\\Users\\marko_000\\Pictures\\2017\\Juhon vanhojen tanssit\\");
		 * FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(path.toString()); File
		 * file = new File("\\"); Messages.sprintf("Absolute path is: " +
		 * file.getAbsolutePath()); try { Messages.sprintf("Absolute path is: " +
		 * file.getCanonicalPath()); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 * 
		 * for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
		 * 
		 * // if (Files.exists(Paths.get(fileInfo.getOrgPath()))) { boolean success =
		 * FileInfo_Utils.moveFile(fileInfo); if (!success) {
		 * System.err.println("NOT COPIED!! FileInfo: " + fileInfo + " fileInfo dest: "
		 * + fileInfo.getDestination_Path() + " workdir: " + fileInfo.getWorkDir()); //
		 * break; } else { System.out.println("FileInfo: " + fileInfo +
		 * " fileInfo dest: " + fileInfo.getDestination_Path() + " workdir: " +
		 * fileInfo.getWorkDir()); } // } else { //
		 * Messages.sprintfError("There are no files to move"); // }
		 * 
		 * }
		 */
	}

	@Test
	void testMoveFileInfoToAnotherLocation() {
		Path file = Paths.get("/home/gerbiloi/Pictures/Riston kuvat/Risto1/Ristiäiset");
		System.out.println(
				"File " + file + " exists? " + Files.exists(file) + " fullPath: " + file.toFile().getAbsolutePath());

	}

	@Test
	void testCreateFileInfo() throws IOException {
		FileInfo fileInfo = FileInfo_Utils.createFileInfo(Paths.get("src/main/resources/input/20220413_160023.jpg"));
		Messages.sprintf("Fileinfo: " + fileInfo.showAllValues());
		String expected = "FileInfo [orgPath=src/main/resources/input/20220413_160023.jpg, workdir=, workDirDriveSerialNumber=, destination_Path=, fileInfo_version=2, event=, location=, tags=, + fileInfo_id=2, camera_model=SM-A515F, bad=false, confirmed=false, copied=false, good=true, ignored=false, image=true, raw=false, suggested=false, video=false, orientation=1, thumb_length=51503, timeShift=0, date=1649865623000, size=3515984, tableDuplicated=false, thumb_offset=916 user ]";
		Messages.sprintf("ACTUAL Fileinfo from file length= " + fileInfo.showAllValues().length() + " Expected length: " + expected.length());
		assertEquals(fileInfo.showAllValues(), expected);
	}
}
