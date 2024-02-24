package common.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ThumbnailVideoTest {

    @Test
    public void testThumbnails() throws Exception {

        Thumbnails.of(new File("C:\\Users\\marko\\OneDrive\\Kuvat\\Samsung Gallery\\DCIM\\Camera\\20230423_152124.mp4")).size(640,480).outputFormat("jpg").toFiles(Rename.PREFIX_DOT_THUMBNAIL);

    }

}
