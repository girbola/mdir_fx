package com.girbola.fileinfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.sql.FolderInfo_SQL;

class FileInfo_UtilsTest {

	@Test
	void testMoveFile() {
		Path path = Paths.get("C:\\Users\\marko_000\\Pictures\\2017\\Juhon vanhojen tanssit\\");
		FolderInfo folderInfo = FolderInfo_SQL.loadFolderInfo(path.toString());

		for (FileInfo fileInfo : folderInfo.getFileInfoList()) {
			if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {
				boolean success = FileInfo_Utils.moveFile(fileInfo);
				if (!success) {
					System.err.println("NOT COPIED!! FileInfo: " + fileInfo + " fileInfo dest: "
							+ fileInfo.getDestination_Path() + " workdir: " + fileInfo.getWorkDir());
//				break;
				} else {
					System.out.println("FileInfo: " + fileInfo + " fileInfo dest: " + fileInfo.getDestination_Path()
							+ " workdir: " + fileInfo.getWorkDir());
				}
			}
		}

	}

}
