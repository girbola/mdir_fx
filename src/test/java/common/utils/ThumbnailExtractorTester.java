package common.utils;

import com.drew.imaging.ImageProcessingException;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.utils.FileInfoUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class ThumbnailExtractorTester {

    Path file = Paths.get("src", "test", "resources", "test-material", "IMG_4312.CR2");

    @Test
    public void testGetMetadataThumbImage() throws IOException {
        System.out.println("testGetMetadataThumbImage");
        double startTime = System.currentTimeMillis();
        BufferedImage image = ImageUtils.getMetadataThumbImage(file);
        if (image == null) {
            System.err.println("Failed to get metadata thumbnail image from file: " + file);
            return;
        }
        System.out.println("getMetadataThumbImage::: " + image.getWidth() + " took: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Test
    public void testGetThumbnailByOffsetAndLenght() {
        System.out.println("testGetThumbnailByOffsetAndLenght: " + file);
        FileInfo fileInfo = null;
        try {
            fileInfo = FileInfoUtils.createFileInfo(file.toAbsolutePath());

            double startTime = System.currentTimeMillis();
            byte[] thumbnailData = ImageUtils.getMetadataThumbImageAsByteArray(fileInfo);
            if (thumbnailData == null) {
                System.err.println("Failed to get thumbnail data from file: " + file);
                return;
            }
            System.out.println("getMetadataThumbImageAsByteArray::: " + thumbnailData.length + " took: " + (System.currentTimeMillis() - startTime) + " ms");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetThumbnailByOffsetAndLenghtAndResize() {
        System.out.println("testGetThumbnailByOffsetAndLenght: " + file);
        FileInfo fileInfo = null;
        try {
            fileInfo = FileInfoUtils.createFileInfo(file.toAbsolutePath());

            double startTime = System.currentTimeMillis();
            byte[] data = ImageUtils.getMetadataThumbImageAsByteArray(fileInfo);
            if (data == null) {
                System.err.println("Failed to get thumbnail data from file: " + file);
                return;
            }
            Messages.sprintf("thumbnailData full size in bytes: " + data.length);

            BufferedImage teset = ImageIO.read(new ByteArrayInputStream(data));
            System.out.println("teset:::: " + teset.getWidth());
            if (data.length > 0) {
                byte[] bytes = ImageUtils.resizeImage(data, 100, 100);
                Messages.sprintf("SCALED in bytes: " + bytes.length);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
