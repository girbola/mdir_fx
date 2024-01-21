package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilsTest {


	Logger log = Logger.getLogger(FileUtilsTest.class.getName());

	@Test
	public void testFileRename() throws IOException {
		Path copyFileForSourceTest = Paths.get("src","main", "resources","input", "IMG.jpg");
        Path copyFileForDestTest = Paths.get("src","main", "resources","input", "IMG_123.jpg");

		Path srcTest = Files.copy(copyFileForSourceTest,copyFileForDestTest);
		Path destTest = Paths.get("src/main/resources/output/IMG_123.jpg");

        System.out.println(
                "srcTest: " + srcTest.toFile().getAbsolutePath() + " dest: " + destTest.toFile().getAbsolutePath());

        Path renamedFile = FileUtils.renameFile(srcTest, destTest);
		Path expectedFile = Paths.get("src/main/resources/output/IMG_1.jpg");

        log.info("copyFileForSourceTest: " + copyFileForSourceTest + " copied to " + copyFileForDestTest + " srcTest is now: " + srcTest.toString() + " destTest is now: " + destTest.toString() + " renamed file is now: " + renamedFile.toString());

		assert renamedFile != null;
		assertEquals(expectedFile.toString(), renamedFile.toString());

    }

    @Test
    public void testHash() {
        Path destTest = Paths.get("src/main/resources/output/IMG.jpg");

        String checkSumFromFile = FileUtils.getCheckSumFromFile(destTest);

        assertTrue(checkSumFromFile.equals("ef7237ce469ab8040761a5be5d5478f8"));
    }

    @Test
    public void renameFileToDate() throws IOException {
        Path srcTest = Paths.get("src/main/resources/input/IMG.jpg");


		FileInfo fileInfo = FileInfoUtils.createFileInfo(srcTest);
		Path renamedFile = FileInfoUtils.renameFileToDate(srcTest, fileInfo);
		Path expectedFile = Paths.get("src/main/resources/input/1970-01-01 00.00.00.jpg");
		assertEquals(expectedFile.toString(), renamedFile.toString());
    }

    @Test
    public void testGetHashTiming() {
        Path destTest = Paths.get("src/main/resources/output/IMG.jpg");

        String expected = "ef7237ce469ab8040761a5be5d5478f8";
        String actual = FileUtils.getCheckSumFromFile(destTest);

        assertEquals(expected, actual);
    }
}
