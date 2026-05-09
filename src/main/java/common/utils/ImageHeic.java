package common.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageHeic {

    public static BufferedImage readHeic(File input) throws IOException {
        Process p = new ProcessBuilder(
                "ffmpeg", "-i", input.getAbsolutePath(),
                "-f", "image2pipe",
                "-vcodec", "png",
                "-"
        ).start();

        try (InputStream is = p.getInputStream()) {
            BufferedImage img = ImageIO.read(is);

            int exit = p.waitFor();
            if (exit != 0 || img == null) {
                throw new IOException("Failed to decode HEIC");
            }

            return img;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

}
