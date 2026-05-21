package common.utils;

import com.girbola.imagehandling.HeicThumbnailService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FfmpegAppDetector {

    @Test
    public void testFfmpegAppDetector() {
        HeicThumbnailService heicThumbnailService = new HeicThumbnailService();
        boolean ffmpegAvailable = heicThumbnailService.isFfmpegAvailable();
        assertTrue(ffmpegAvailable);

    }
}
