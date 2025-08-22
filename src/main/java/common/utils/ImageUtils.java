package common.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.DateFixerController;
import com.girbola.messages.Messages;

import java.awt.geom.AffineTransform;
import java.awt.image.DataBufferByte;
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
import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import static com.girbola.messages.Messages.sprintf;

public class ImageUtils {

    public static BufferedImage convertFrameToBufferedImageWithScalingThumbnail(Frame frame, int height) throws IOException {
        if (frame == null) {
            return null;
        }
        if (height <= 0) {
            return null;
        }

        BufferedImage converted = convertToBufferedImage(frame);
        if (frame.imageHeight > height) {
            return Thumbnails.of(converted).height(height).keepAspectRatio(true).asBufferedImage();
        } else {
            return Thumbnails.of(converted).height(frame.imageHeight).keepAspectRatio(true).asBufferedImage();
        }
    }

    private static BufferedImage convertToBufferedImage(Frame frame) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.convert(frame);
    }

    public static String calculateRAWImagePHash(Path imagePath) {
        try {
            // Read the metadata from the RAW (CR2) file
            Metadata metaData = ImageMetadataReader.readMetadata(imagePath.toFile());
            ExifThumbnailDirectory directory = metaData.getFirstDirectoryOfType(ExifThumbnailDirectory.class);

            if (directory == null) {
                Messages.sprintfError("No ExifThumbnailDirectory found in metadata.");
                return "";
            }

            // Get offset and length from the thumbnail metadata
            Integer offset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
            Integer length = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);

            if (offset == null || length == null) {
                Messages.sprintfError("Offset or length not found in ExifThumbnailDirectory.");
                return "";
            }

            // Read the file into a byte array
            byte[] fileData = readFileData(imagePath);
            if (fileData == null) {
                Messages.sprintfError("Failed to read file data.");
                return "";
            }

            // Extract the thumbnail slice
            byte[] thumbnailData = extractThumbnailSlice(fileData, offset, length);
            if (thumbnailData == null) {
                Messages.sprintfError("Failed to extract thumbnail slice.");
                return "";
            }

            // Decode the thumbnail into a BufferedImage
            BufferedImage thumbnailImage = decodeToImage(thumbnailData);
            if (thumbnailImage == null) {
                Messages.sprintfError("Failed to decode thumbnail to image.");
                return "";
            }

            // Convert the image to a hash string
            return getString(thumbnailImage);

        } catch (Exception e) {
            Messages.sprintfError("Error calculating RAW image PHash: " + e.getMessage());
            return "";
        }
    }

    /**
     * Reads all bytes from the image file.
     */
    private static byte[] readFileData(Path imagePath) {
        try {
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            Messages.sprintfError("Error reading file: " + imagePath + ". Message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a slice of bytes from the given data.
     */
    private static byte[] extractThumbnailSlice(byte[] data, int offset, int length) {
        try {
            return Arrays.copyOfRange(data, offset, offset + length);
        } catch (IndexOutOfBoundsException e) {
            Messages.sprintfError("Error extracting slice: offset=" + offset + ", length=" + length);
            return null;
        }
    }

    /**
     * Decodes byte array into a BufferedImage.
     */
    private static BufferedImage decodeToImage(byte[] imageData) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(imageData)) {
            return ImageIO.read(in);
        } catch (IOException e) {
            Messages.sprintfError("Error decoding image from byte array: " + e.getMessage());
            return null;
        }
    }

    public static String calculateRAWImagePHash_org(Path imagePath) {
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
        double newWidth = UIContants.THUMBNAIL_MAX_WIDTH;
        double newHeight = UIContants.THUMBNAIL_MAX_HEIGHT;

        // Horizontal image
        if (originalWidth > originalHeight) {
            newWidth = UIContants.THUMBNAIL_MAX_WIDTH;
            ratio = (double) originalHeight / originalWidth;
            newHeight = (ratio * UIContants.THUMBNAIL_MAX_HEIGHT);
        }
        //Vertical image
        else if (originalWidth < originalHeight) {
            newHeight = UIContants.THUMBNAIL_MAX_WIDTH;
            ratio = (double) originalWidth / originalHeight;
            newWidth = (ratio * UIContants.THUMBNAIL_MAX_HEIGHT);
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

        // Get pixel data
        byte[] pixels = ((DataBufferByte) resizedImage.getRaster().getDataBuffer()).getData();

        // Calculate average brightness using bit operations and unrolled loop
        int sum = 0;
        int length = pixels.length;
        int i = 0;

        // Unroll the loop for better performance (process 4 pixels at a time)
        for (; i < length - 3; i += 4) {
            sum += pixels[i] & 0xFF;
            sum += pixels[i + 1] & 0xFF;
            sum += pixels[i + 2] & 0xFF;
            sum += pixels[i + 3] & 0xFF;
        }

        // Handle remaining pixels
        for (; i < length; i++) {
            sum += pixels[i] & 0xFF;
        }

        // Use bit shift for division by 64 (since pixels.length is always 64)
        int avg = sum >> 6;

        // Generate hash using bit operations
        long hash = 0;
        for (byte pixel : pixels) {
            // Combine operations to reduce steps
            hash = (hash << 1) | (((pixel & 0xFF) > avg) ? 1 : 0);
        }

        // Use lookup table for faster hex conversion of small chunks
        return Long.toHexString(hash);
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
