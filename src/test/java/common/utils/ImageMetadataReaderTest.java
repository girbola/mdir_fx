package common.utils;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.girbola.fileinfo.FileInfo;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.drew.imaging.ImageMetadataReader.readMetadata;
import static com.girbola.utils.FileInfoUtils.createFileInfo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageMetadataReaderTest {

    @Test
    public void testReadBufferedImageHEIC() throws IOException, InterruptedException {
        File input = new File("/Users/girbola/Documents/Kuviloi/IMG_5436.HEIC");

        BufferedImage bufferedImage = ImageHeic.readHeic(input);
        System.out.println("bufferedImage: " + bufferedImage.getWidth() + " " + bufferedImage.getHeight());
        assertNotNull(bufferedImage);
    }

    @Test
    public void testReadMetadataHEIC() throws IOException, InterruptedException, ImageProcessingException {
        File input = new File("/Users/girbola/Documents/Kuviloi/IMG_5436.HEIC");
        Metadata metadata = readMetadata(input);
        FileInfo fileInfo = createFileInfo(input.toPath());
        System.out.println("fileInfo: ::: " + fileInfo.showAllValues());
for(Directory directory : metadata.getDirectories()) {
    System.out.println("-----DIR" + directory.toString());
    for(Tag tag : directory.getTags()) {
        System.out.println("-----####TAG" + tag.toString());
    }

}
        System.out.println("metadata: " + metadata);
    }
}
