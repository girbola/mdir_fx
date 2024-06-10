package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.fileinfo.FileInfoUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FileUtilsTest {


    Logger log = Logger.getLogger(FileUtilsTest.class.getName());

    @Test
    public void testHash() {
        Path destTest = Paths.get("src", "main", "resources", "output", "IMG.jpg");

        String checkSumFromFile = FileUtils.getCheckSumFromFile(destTest);

        assertEquals("7b262121e97031af48756b501faa05ac", checkSumFromFile);
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
        Path destTest = Paths.get("src", "main", "resources", "output", "IMG.jpg");

        String expected = "7b262121e97031af48756b501faa05ac";
        String actual = FileUtils.getCheckSumFromFile(destTest);

        assertEquals(expected, actual);
    }

    @Test
    public void testRenameFile() throws IOException {
        Path srcFile = Paths.get("./src/test/resources/in/IMG.jpg");
        Path destFile = Paths.get("./src/test/resources/out/IMG1.jpg");

        Path path = FileUtils.renameFile(srcFile, destFile);
        log.info("PATH: " + path);

     assertEquals(".\\src\\test\\resources\\out\\IMG1_1.jpg", path.toString());
    }
}
