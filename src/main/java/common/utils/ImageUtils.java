package common.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImageUtils {


    public static long calculateDifferenceHash(Path imagePath) throws IOException {
        BufferedImage image = null;
        try {
            image = ImageIO.read(imagePath.toFile());
            int width = 8; // Width of the resized image
            int height = 8; // Height of the resized image
            image = resizeImage(image, width, height);

            // Convert the resized image to grayscale
            int[][] pixels = new int[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[x][y] = image.getRGB(x, y) & 0xFF;
                }
            }

            // Calculate dHash
            long hash = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width - 1; x++) {
                    hash <<= 1;
                    if (pixels[x][y] > pixels[x + 1][y]) {
                        hash |= 1;
                    }
                }
            }
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.createGraphics().drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        return resizedImage;
    }

}
