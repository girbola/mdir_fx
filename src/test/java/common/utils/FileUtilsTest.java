package common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.girbola.fileinfo.FileInfoUtils;
import org.junit.jupiter.api.Test;

import com.girbola.fileinfo.FileInfo;

public class FileUtilsTest {

	@Test
	public void testFileRename() throws IOException {
		Path srcTest = Paths.get("src/main/resources/input/IMG.jpg");
		Path destTest = Paths.get("src/main/resources/output/IMG.jpg");

		System.out.println(
				"srcTest: " + srcTest.toFile().getAbsolutePath() + " dest: " + destTest.toFile().getAbsolutePath());

		Path renamedFile = FileUtils.renameFile(srcTest, destTest);

		assertEquals("src/main/resources/output/IMG_1.jpg", renamedFile.toString());

	}

	@Test
	public void renameFileToDate() throws IOException {
		Path srcTest = Paths.get("src/main/resources/input/IMG.jpg");
		Path destTest = Paths.get("src/main/resources/output/IMG.jpg");

		System.out.println(
				"srcTest: " + srcTest.toFile().getAbsolutePath() + " dest: " + destTest.toFile().getAbsolutePath());
		FileInfo fileInfo = FileInfoUtils.createFileInfo(srcTest);
		Path renamedFile = FileInfoUtils.renameFileToDate(srcTest, fileInfo);
		
		assertEquals("src/main/resources/input/1970-01-01 00.00.00.jpg", renamedFile.toString());
	}

}
