package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTest {


    Logger log = Logger.getLogger(FileUtilsTest.class.getName());
    final private Path srcTest = Paths.get("src", "test", "resources", "in", "IMG.jpg");

    @Test
    public void testHash() {

        String checkSumFromFile = FileUtils.getCheckSumFromFile(srcTest);

        assertEquals("67685d74c22ebf3083d4a2f5d4b7c809", checkSumFromFile);
    }

    @Test
    public void renameFileToDate() throws IOException {

        FileInfo fileInfo = FileInfoUtils.createFileInfo(srcTest);
        Path renamedFile = FileInfoUtils.renameFileToDate(srcTest, fileInfo);
        Path expectedFile = Paths.get(srcTest.getParent().toString(), "1970-01-01 00.00.00.jpg");
        assertEquals(expectedFile.toString(), renamedFile.toString());
    }

    @Test
    public void testGetHashTiming() {

        String expected = "67685d74c22ebf3083d4a2f5d4b7c809";
        String actual = FileUtils.getCheckSumFromFile(srcTest);

        assertEquals(expected, actual);
    }

    @Test
    public void testRenameFile() throws IOException {

        Path destFile = Paths.get("src", "test", "resources", "out", "IMG1.jpg");

        Messages.sprintf("src: " + srcTest + " dest: " + destFile);

        Path path = FileUtils.renameFile(srcTest, destFile);
        log.info("PATH: " + path);
        Path destExpected = Paths.get("src", "test", "resources", "out", "IMG1_1.jpg");

        assertNotNull(path);
        assertEquals(destExpected.toString(), path.toString());
    }
    @Test
    public void testRenameFileWithDifferentSize() throws IOException {
        Path sourceFile = Paths.get("src", "test", "resources", "test-material", "IMG.jpg");
// "src", "test", "resources", "in", "20220413_160023.jpg"


        Path destinationFile = Paths.get("src", "test", "resources", "test-material", "IMG1.jpg");

        log.info("sourceFile: " + sourceFile + " destinationFile: " + destinationFile);

        // Perform a file rename
        Path actualResult = FileUtils.renameFile(sourceFile, destinationFile);

        // Expected Result
        Path expectedFile = Paths.get("src", "test", "resources", "test-material", "IMG1_1.jpg");

        // Check the Assert
        assertEquals(expectedFile, actualResult);
    }

    @Test
    public void testRenameFileWithSameSize() throws IOException {
        Path sourceFile = Paths.get("src", "test", "resources", "test-material", "IMG1.jpg");
        Path destinationFile = Paths.get("src", "test", "resources", "test-material", "IMG1.jpg");

        // Perform a file rename
        Path actualResult = FileUtils.renameFile(sourceFile, destinationFile);

        // As both files have the same size, no renaming should be done and null should be returned.
        assertNull(actualResult);
    }
}
