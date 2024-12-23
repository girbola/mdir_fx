package common.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.girbola.configuration.GuiImageFrame;
import com.girbola.controllers.datefixer.DateFixerController;
import com.girbola.messages.Messages;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.messages.Messages.sprintf;

public class ImageUtils {

    public static String calculateRAWImagePHash(Path imagePath) {
        int offset = -1;
        int length = -1;
        try {
            // Read the metadata from the CR2 file
            Metadata metaData = ImageMetadataReader.readMetadata(imagePath.toFile());

            ExifThumbnailDirectory directory = metaData.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
            if (directory != null) {


                if (directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET) != null) {
                    offset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
                }
                if (directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH) != null) {
                    length = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
                }


                byte[] data = null;
                try {
                    data = Files.readAllBytes(imagePath);
                } catch (Exception ex) {
                    Messages.sprintfError(
                            "Can't Files.readAllBytes: " + imagePath + "\nError message: " + ex.getMessage());
                    return null;
                }

                if (data == null) {
                    sprintf("Cannot readAllByte to data. returning->");
                    return null;
                }
                sprintf("data size is: " + data.length + " length: " + length + " offset: " + offset);
                byte[] slice = null;
                try {
                    slice = Arrays.copyOfRange(data, offset, (offset + length));
                } catch (Exception ex) {
                    Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }

                ByteArrayInputStream in = new ByteArrayInputStream(slice);
                BufferedImage bufferedImage = null;
                try {
                    bufferedImage = javax.imageio.ImageIO.read(in);
                } catch (IOException ex) {
                    Logger.getLogger(DateFixerController.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
                return getString(bufferedImage);
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    public static String calculateImagePHash(Path imagePath) throws IOException {

        // Create an ImageInputStream
        ImageInputStream input = ImageIO.createImageInputStream(imagePath.toFile());

        // Read the image
        BufferedImage image = ImageIO.read(input);

        return getString(image);
    }

    private static String getString(BufferedImage image) {
        // Resize the image to 8x8
        BufferedImage resizedImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(image.getScaledInstance(8, 8, Image.SCALE_SMOOTH), 0, 0, null);

        // Calculate the average color
        long sum = 0;
        for (int y = 0; y < resizedImage.getHeight(); y++) {
            for (int x = 0; x < resizedImage.getWidth(); x++) {
                Color color = new Color(resizedImage.getRGB(x, y));
                sum += color.getRed(); // Use the red component for grayscale
            }
        }

        long avg = sum / ((long) resizedImage.getWidth() * resizedImage.getHeight());

        // Generate the hash
        long hash = 0;
        for (int y = 0; y < resizedImage.getHeight(); y++) {
            for (int x = 0; x < resizedImage.getWidth(); x++) {
                Color color = new Color(resizedImage.getRGB(x, y));
                hash <<= 1; // Shift hash left
                if (color.getRed() > avg) {
                    hash |= 1; // Set the last bit to 1 if the pixel is above average
                }
            }
        }

        return "" + Math.abs(hash);
    }

    public static boolean compareImages(BufferedImage image1, BufferedImage image2) {
        // Check if the images have the same dimensions
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            return false;
        }

        // Compare the pixel values of the images
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static String getAverageHash(BufferedImage img) {
        int width = 8;
        int height = 8;
        BufferedImage resizedImg = resize(img, width, height);
        int[] grayscaleValues = new int[width * height];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = resizedImg.getRGB(x, y);
                int gray = (rgb >> 16 & 0xff) * 30 + (rgb >> 8 & 0xff) * 59 + (rgb & 0xff) * 11;
                grayscaleValues[index++] = gray;
            }
        }

        int average = Arrays.stream(grayscaleValues).sum() / grayscaleValues.length;
        StringBuilder hash = new StringBuilder();
        for (int gray : grayscaleValues) {
            hash.append(gray >= average ? '1' : '0');
        }

        return hash.toString();
    }

    private static BufferedImage resize(BufferedImage img, int width, int height) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private static BufferedImage scaleBufferedImage(BufferedImage image, int desiredWidth, int desiredHeight) {
        BufferedImage scaledImage = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_RGB);
        scaledImage.getGraphics().drawImage(image.getScaledInstance(desiredWidth, desiredHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        return scaledImage;
    }


    public static BufferedImage scaleImageWithAspectRatio(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double ratio = 0;

        // Calculate the new dimensions while maintaining the aspect ratio
        double newWidth = GuiImageFrame.thumb_x_MAX;
        double newHeight = GuiImageFrame.thumb_y_MAX;

        // Horizontal image
        if (originalWidth > originalHeight) {
            newWidth = GuiImageFrame.thumb_x_MAX;
            ratio = (double) originalHeight / originalWidth;
            newHeight = (ratio * GuiImageFrame.thumb_y_MAX);
        }
        //Vertical image
        else if (originalWidth < originalHeight) {
            newHeight = GuiImageFrame.thumb_x_MAX;
            ratio = (double) originalWidth / originalHeight;
            newWidth = (ratio *  GuiImageFrame.thumb_y_MAX);
        }

        // Create a new BufferedImage with the calculated dimensions
        BufferedImage scaledImage = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_INT_RGB);

        // Scale the original image to the new dimensions
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(originalImage.getScaledInstance((int) newWidth, (int) newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        return scaledImage;
    }
}
