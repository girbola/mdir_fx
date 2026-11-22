package common.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.girbola.fileinfo.FileInfo;
import com.girbola.utils.FileInfoUtils;
import com.girbola.messages.Messages;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static com.girbola.utils.FileInfoUtils.createFileInfo;

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

        System.out.println("SRCPATH: " + srcTest.toFile().getAbsolutePath());
        File absoluteFile = srcTest.toFile().getAbsoluteFile();
        FileInfo fileInfo = createFileInfo(absoluteFile.toPath());
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
    public void testRenameFileSourceDoesNotExist() {
        Path nonExistentSource = Paths.get("src", "test", "resources", "non-existent.jpg");
        Path destFile = Paths.get("src", "test", "resources", "out", "IMG1.jpg");
        Path pathToTest = null;
        // Attempt to rename and expect an exception
        try {
            pathToTest = FileUtils.renameFile(nonExistentSource, destFile);
            if(pathToTest != null) {
                System.out.println("path: " + pathToTest);
            }
        } catch (IOException e) {
            return;
        };

        assertNull(pathToTest);
    }

    @Test
    public void testRenameFileDestinationDirectoryDoesNotExist() throws IOException {
        Path destFile = Paths.get("src", "test", "resources", "non-existent-directory", "IMG1.jpg");

        Path path = FileUtils.renameFile(srcTest, destFile);
        assertNull(path);
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

        // Ensure the test image exists
        if (!java.nio.file.Files.exists(sourceFile)) {
            createTestImage(sourceFile);
        }

        // Remove destination file if it exists (redundant since source == destination, but kept for safety)
        if (java.nio.file.Files.exists(destinationFile)) {
            java.nio.file.Files.delete(destinationFile);
            createTestImage(destinationFile);
        }

        // Perform a file rename
        Path actualResult = FileUtils.renameFile(sourceFile, destinationFile);

        // As both files have the same size, no renaming should be done and null should be returned.
        assertNull(actualResult);
    }

    @Test
    public void testsDng() {

        try {
            FileInfo fileInfo = createFileInfo(Paths.get("src", "test", "resources", "test-material", "IMG_4246.DNG"));
            assertNotNull(fileInfo);
            Messages.sprintf("fileInfo: " + fileInfo);
            long start = System.currentTimeMillis();
            createThumbnail(Paths.get(fileInfo.getOrgPath()), Paths.get("src", "test", "resources", "test-material", "Blaa-IMG_4246_thumb.DNG"), 100);
            long end = System.currentTimeMillis();
            Messages.sprintf("Thumbnail creation took: " + (end - start) + " ms");
//            Metadata metaData = DateTaken.readMetaData(Paths.get(fileInfo.getOrgPath()));
//            for(Directory directory : metaData.getDirectories()) {
//                Messages.sprintf("-directory: " + directory);
//                directory.getTags().forEach(tag -> {
//                    Messages.sprintf("--->tag: " + tag);
//                });
//            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testHeic() {
        try {
            FileInfo fileInfo = createFileInfo(Paths.get("src", "test", "resources", "test-material", "IMG_5436.HEIC"));
            assertNotNull(fileInfo);

            Metadata metadata = ImageMetadataReader.readMetadata(Paths.get(fileInfo.getOrgPath()).toFile());

            ArrayList<String> lines = new ArrayList<>();

            for (Directory dir : metadata.getDirectories()) {
                Messages.sprintf("Directory: " + dir.getName());
                for (Tag tag : dir.getTags()) {
                    Messages.sprintf("---TAG::: " + tag);
                }
            }
//
//            for(String line : lines) {
//
//            }

//            boolean processed = fileInfoGetCreationDates(Paths.get("src", "test", "resources", "test-material", "IMG_4246.DNG"), fileInfo);
//
//            boolean processed = handleMetadataInformation(Paths.get(fileInfo.getOrgPath()), fileInfo);
//            assertNotNull(processed);
//            if(processed) {
//                Messages.sprintf("Metadata information handled successfully.");
//            } else {
//                Messages.sprintf("Failed to handle metadata information.");
//            }

        } catch (IOException | ImageProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createThumbnail(Path sourcePath, Path outputPath, int targetWidth) throws IOException {
        if (targetWidth <= 0) {
            throw new IllegalArgumentException("targetWidth must be positive");
        }

        try (ImageInputStream iis = ImageIO.createImageInputStream(sourcePath.toFile())) {
            if (iis == null) {
                throw new IOException("Could not open ImageInputStream for: " + sourcePath);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new IOException("No ImageReader found for: " + sourcePath);
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                int fullWidth = reader.getWidth(0);
                int fullHeight = reader.getHeight(0);

                int step = Math.max(1, fullWidth / (targetWidth * 2));

                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(step, step, 0, 0);

                BufferedImage subsampled = reader.read(0, param);

                int targetHeight = Math.max(1, (int) Math.round(
                        (double) fullHeight * targetWidth / fullWidth
                ));

                BufferedImage thumb = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = thumb.createGraphics();
                try {
                    g2d.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR
                    );
                    g2d.drawImage(subsampled, 0, 0, targetWidth, targetHeight, null);
                } finally {
                    g2d.dispose();
                }

                if (outputPath.getParent() != null) {
                    java.nio.file.Files.createDirectories(outputPath.getParent());
                }
                ImageIO.write(thumb, "jpg", outputPath.toFile());
            } finally {
                reader.dispose();
            }
        }
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
