package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
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
    public void testRenameFileSourceDoesNotExist() throws IOException {
        Path nonExistentSource = Paths.get("src", "test", "resources", "non-existent.jpg");
        Path destFile = Paths.get("src", "test", "resources", "out", "IMG1.jpg");

        // Attempt to rename and expect an exception
        IOException exception = assertThrows(IOException.class, () -> FileUtils.renameFile(nonExistentSource, destFile));
        assertTrue(exception.getMessage().contains("non-existent.jpg"));
    }

    @Test
    public void testRenameFileDestinationDirectoryDoesNotExist() throws IOException {
        Path destFile = Paths.get("src", "test", "resources", "non-existent-directory", "IMG1.jpg");

        // Attempt to rename and expect an exception
        IOException exception = assertThrows(IOException.class, () -> FileUtils.renameFile(srcTest, destFile));
        assertTrue(exception.getMessage().contains("non-existent-directory"));
    }

    @Test
    public void testRenameFileWhenPathsAreSameButFileDoesNotExist() throws IOException {

        Path sourceAndDest = Paths.get("src", "test", "resources", "out", "IMG_NON_EXISTENT.jpg");
        createTestImage(sourceAndDest);

        // Perform the rename operation
        Path result = FileUtils.renameFile(sourceAndDest, sourceAndDest);

        // Expect no actual renaming as the file does not exist
        assertNull(result);
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

    private static Path createTestImage(Path filePath) throws IOException {
        int width = 10; // Width of the image
        int height = 10; // Height of the image

        // Create a buffered image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Fill the image with a solid color
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }

        // Write the image to the specified path
        ImageIO.write(image, "jpg", filePath.toFile().getAbsoluteFile());

        System.out.println("Test image created at: " + filePath);
        return filePath;
    }


}
