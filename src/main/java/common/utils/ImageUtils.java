package common.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.girbola.configuration.GuiImageFrame;
import com.girbola.controllers.datefixer.DateFixerController;
import com.girbola.messages.Messages;

import java.awt.geom.AffineTransform;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
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
                    bufferedImage = ImageIO.read(in);
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

    private static String getString2(BufferedImage image) {
        // Resize the image to 8x8
        BufferedImage resizedImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(image.getScaledInstance(8, 8, Image.SCALE_SMOOTH), 0, 0, null);

        // Calculate the average color
        long sum = IntStream.range(0, resizedImage.getHeight()) // Iterate over rows
                .parallel()
                .mapToLong(y -> {
                    long rowSum = 0;
                    for (int x = 0; x < resizedImage.getWidth(); x++) {
                        Color color = new Color(resizedImage.getRGB(x, y));
                        rowSum += color.getRed();
                    }
                    return rowSum;
                })
                .sum(); // Sum all rows

        long avg = sum / ((long) resizedImage.getWidth() * resizedImage.getHeight());

        // Generate the hash
        long hash = calculateHash(resizedImage, (int) avg);
//        long hash = 0;
//        for (int y = 0; y < resizedImage.getHeight(); y++) {
//            for (int x = 0; x < resizedImage.getWidth(); x++) {
//                Color color = new Color(resizedImage.getRGB(x, y));
//                hash <<= 1; // Shift hash left
//                if (color.getRed() > avg) {
//                    hash |= 1; // Set the last bit to 1 if the pixel is above average
//                }
//            }
//        }

        return "" + Math.abs(hash);
    }

    public static long calculateHash(BufferedImage resizedImage, int avg) {
        int height = resizedImage.getHeight();
        int width = resizedImage.getWidth();

        // AtomicLong to manage concurrent updates
        AtomicLong hash = new AtomicLong(0);

        // Parallelize over rows (or can split over pixels too if needed)
        IntStream.range(0, height).parallel().forEach(y -> {
            long rowHash = 0L; // Each thread computes row-wise hash independently
            for (int x = 0; x < width; x++) {
                Color color = new Color(resizedImage.getRGB(x, y));
                rowHash <<= 1;
                if (color.getRed() > avg) {
                    rowHash |= 1;
                }
            }
            synchronized (hash) {
                long finalRowHash = rowHash;
                hash.updateAndGet(h -> (h << width) | finalRowHash);
            }
        });

        return hash.get();
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
        scaledImage.getGraphics().drawImage(image.getScaledInstance(desiredWidth, desiredHeight, Image.SCALE_SMOOTH), 0, 0, null);
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


    public static String processImagesOneAtATime(Path file) {
        String hash = "";

            System.out.println("Processing file: " + file);
            try {
                // Read image from file
                BufferedImage imagee = ImageIO.read(file.toFile());
                if (imagee == null) {
                    throw new IOException("Unsupported or corrupted file: " + file);
                }
                // Generate hash for the image
                hash = getString(imagee);


                // Output the result for the current file
                System.out.println("File: " + file + " -> Hash: " + hash);
            } catch (IOException e) {
                // Log error and skip invalid file
                System.err.println("Error processing file: " + file + ". Skipping.");
            }
            return hash;
    }

    private static String getString(BufferedImage image) {
        // Resize image to 8x8 grayscale
        BufferedImage resizedImage = resizeImage(image, 8, 8);

        // Directly access raw pixel values for fast processing
        byte[] pixels = ((DataBufferByte) resizedImage.getRaster().getDataBuffer()).getData();

        // Calculate the average brightness
        long sum = 0;
        for (byte pixel : pixels) {
            sum += (pixel & 0xFF); // Convert byte (signed) to unsigned int
        }
        long avg = sum / pixels.length;

        // Generate the hash
        long hash = 0;
        for (byte pixel : pixels) {
            hash <<= 1; // Shift left by 1 bit
            if ((pixel & 0xFF) > avg) {
                hash |= 1; // Set the least significant bit if above average
            }
        }

        return Long.toHexString(hash); // Convert hash to hexadecimal string
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        // Create a new BufferedImage for resizing (grayscale)
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Perform scaling using AffineTransform
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform at = AffineTransform.getScaleInstance(
                (double) width / originalImage.getWidth(),
                (double) height / originalImage.getHeight()
        );
        g2d.drawRenderedImage(originalImage, at);
        g2d.dispose();

        return resized;
    }


}
