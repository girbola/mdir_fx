package common.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.girbola.configuration.UIContants;
import com.girbola.controllers.datefixer.DateFixerController;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import common.media.DateTaken;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
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

    public static BufferedImage getMetadataThumbImage(Path file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file.toFile());
        } catch (IOException ex) {
            Messages.sprintfError("Error reading image: " + ex.getMessage());
        }
        return image;
    }

    public static byte[] getMetadataThumbImageAsByteArray(FileInfo fileinfo) {
    if (fileinfo == null) {
        Messages.sprintfError("fileinfo is null");
        return null;
    }
    Metadata metadata = DateTaken.readMetaData(Paths.get(fileinfo.getOrgPath()));
    Path file = Paths.get(fileinfo.getOrgPath());
    
    if (metadata == null) {
        Messages.sprintfError("No metadata found for file: " + fileinfo.getOrgPath());
        return null;
    }
    
    ExifThumbnailDirectory directory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
    if (directory == null) {
        Messages.sprintfError("No thumbnail directory found in metadata for file: " + fileinfo.getOrgPath());
        return null;
    }

    // Get both raw and adjusted offsets for debugging
    Integer rawOffset = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
    Integer rawLength = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
    Integer thumbOffset = directory.getAdjustedThumbnailOffset();
    Integer thumbLength = directory.getInteger(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
    
    Messages.sprintf("Raw thumbnail data: offset=" + rawOffset + " length=" + rawLength);
    Messages.sprintf("Adjusted thumbnail data: offset=" + thumbOffset + " length=" + thumbLength);
    
    if (thumbOffset != null && thumbLength != null && thumbOffset > 0 && thumbLength > 0) {
        // Try to extract thumbnail directly from file using RandomAccessFile
        try (RandomAccessFile fileRaf = new RandomAccessFile(file.toFile(), "r")) {
            fileRaf.seek(thumbOffset);
            byte[] thumbnailData = new byte[thumbLength];
            fileRaf.readFully(thumbnailData);
            
            Messages.sprintf("Attempting to decode thumbnail using ImageIO from offset: " + thumbOffset);
            
            // Try to decode as image for validation but don't fail if it doesn't decode
            try (ByteArrayInputStream bais = new ByteArrayInputStream(thumbnailData)) {
                BufferedImage thumbnailImage = ImageIO.read(bais);
                if (thumbnailImage != null) {
                    Messages.sprintf("Thumbnail successfully decoded: " + thumbnailImage.getWidth() + "x" + thumbnailImage.getHeight());
                    // Successfully decoded - return the data
                    return thumbnailData;
                } else {
                    Messages.sprintf("Failed to decode thumbnail with adjusted offset, trying raw offset...");
                    // Try with the raw offset instead if adjusted offset failed
                    try (RandomAccessFile fileRaf2 = new RandomAccessFile(file.toFile(), "r")) {
                        fileRaf2.seek(rawOffset);
                        byte[] rawThumbnailData = new byte[rawLength];
                        fileRaf2.readFully(rawThumbnailData);
                        
                        try (ByteArrayInputStream bais2 = new ByteArrayInputStream(rawThumbnailData)) {
                            BufferedImage rawThumbnailImage = ImageIO.read(bais2);
                            if (rawThumbnailImage != null) {
                                Messages.sprintf("Thumbnail successfully decoded using raw offset: " + 
                                                 rawThumbnailImage.getWidth() + "x" + rawThumbnailImage.getHeight());
                                return rawThumbnailData;
                            } else {
                                Messages.sprintf("Failed to decode thumbnail even with raw offset");
                                // Just return the data even if we can't decode it
                                return thumbnailData;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        System.err.println("Invalid thumbnail metadata: offset=" + thumbOffset + ", length=" + thumbLength);
    }
    
    // Fall back to the other method if direct extraction fails
    return getMetadataThumbImageAsByteArray(file, rawOffset != null ? rawOffset : 0, 
                                           rawLength != null ? rawLength : 0);
}

    private static byte[] getMetadataThumbImageAsByteArray(Path file, int thumbOffset, int thumbLength) {
        if (file == null) {
            Messages.sprintfError("File path is null");
            return null;
        }

        if (thumbOffset <= 0 || thumbLength <= 0) {
            Messages.sprintfError("Invalid thumbnail metadata: offset=" + thumbOffset + ", length=" + thumbLength);
            return null;
        }

        // Read the file into a byte array
        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(file);
            if (fileData == null) {
                Messages.sprintfError("Failed to read file data: " + file);
                return null;
            }
            if (thumbOffset + thumbLength > fileData.length) {
                Messages.sprintfError("Thumbnail metadata exceeds file size: offset=" + thumbOffset +
                        ", length=" + thumbLength + ", fileSize=" + fileData.length);
                return null;
            }

            Messages.sprintf("Extracting thumnail by slicing");

            // Extract the thumbnail slice
            return extractThumbnailSlice(fileData, thumbOffset, thumbLength, file);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
            byte[] thumbnailData = extractThumbnailSlice(fileData, offset, length, imagePath);
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
            System.err.println("Error calculating RAW image PHash: " + e.getMessage());
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

    private static byte[] extractThumbnailSlice(byte[] data, int offset, int length, Path filePath) {
        return extractThumbnailSlice(data, offset, length, filePath.toFile());
    }

    /**
     * Extracts a slice of bytes from the given data.
     *
     * @param data   The source byte array
     * @param offset The starting position in the source array
     * @param length The number of bytes to extract
     * @return The extracted byte slice or null if extraction fails
     */
    private static byte[] extractThumbnailSlice(byte[] data, int offset, int length, File file) {
        Messages.sprintf("Extracting thumbnail from file: " + file + " slice from data of length: " + data.length + " offset: " + offset + " length: " + length);
        try {
            if (data == null) {
                Messages.sprintfError("Error extracting slice: data is null");
                return null;
            }

            if (offset < 0 || length <= 0) {
                Messages.sprintfError("Error extracting slice: invalid offset or length - offset=" + offset + ", length=" + length);
                return null;
            }

            // Check for array bounds
            if (offset + length > data.length) {
                Messages.sprintfError("Error extracting slice: requested range exceeds array bounds - offset=" + offset +
                        ", length=" + length + ", data length=" + data.length);
                return null;
            }

            sprintf("data size is: " + data.length + " length: " + length + " offset: " + offset);
            byte[] slice = null;
            try {

                int actualOffset = ImageOffsetUtils.resolveImageOffset(data, offset);
                
                slice = Arrays.copyOfRange(data, actualOffset, (offset + length));
                Messages.sprintf("slice size is: " + slice.length);
                saveByteArrayToFile(slice, Paths.get("C:\\Temp\\thumbnail_slice_for_examination.bin"));
                ByteArrayInputStream bais = new ByteArrayInputStream(slice);
                BufferedImage image = ImageIO.read(bais);
                if(image != null) {
                    Messages.sprintf("Successfully decoded thumbnail: " + image.getWidth() + "x" + image.getHeight());
                } else {
                    Messages.sprintf("Warning: Could not decode image. First bytes: " + bytesToHexPreview(slice, 16));
                }
            } catch (Exception ex) {
                Messages.sprintf("exxxx:::" + ex.getMessage());
                ex.printStackTrace();
                return null;
            }

            // Try to decode the image but don't fail if we can't
            try (ByteArrayInputStream in = new ByteArrayInputStream(slice)) {
                BufferedImage bufferedImage = ImageIO.read(in);
                if (bufferedImage != null) {
                    Messages.sprintf("Successfully decoded thumbnail: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
                } else {
                    // Try to detect what format this might be
                    Messages.sprintf("Warning: Could not decode image. First bytes: " + 
                                      bytesToHexPreview(slice, 16));
                    
                    // Try with different image readers to see if any can handle it
                    tryAlternativeImageReaders(slice);
                }
            } catch (IOException ex) {
                Messages.sprintf("IOException during image decoding: " + ex.getMessage());
                // Continue anyway - we'll return the raw data
            }

            // Return the slice data regardless of whether we could decode it
            return slice;
        } catch (Exception e) {
            Messages.sprintfError("Unexpected error extracting slice: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to convert beginning of byte array to hex for debugging
     */
    private static String bytesToHexPreview(byte[] bytes, int maxBytes) {
        if (bytes == null || bytes.length == 0) {
            return "empty";
        }
        
        int len = Math.min(maxBytes, bytes.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    /**
     * Tries different image readers to see if any can decode the image data
     */
    private static void tryAlternativeImageReaders(byte[] imageData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(bais));
            if (!readers.hasNext()) {
                Messages.sprintf("No image readers found for this format");
            } else {
                while (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    Messages.sprintf("Found image reader: " + reader.getFormatName());
                    
                    // Reset stream for next reader
                    bais.reset();
                }
            }
        } catch (IOException e) {
            Messages.sprintf("Error inspecting image format: " + e.getMessage());
        }
    }

    /**
     * Decodes byte array into a BufferedImage.
     */
    private static BufferedImage decodeToImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            Messages.sprintfError("Error decoding image: input data is null or empty");
            return null;
        }

        try (ByteArrayInputStream in = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                Messages.sprintfError("Error decoding image: unsupported or invalid image data");
            }
            return image;
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

    private static void saveByteArrayToFile(byte[] data, Path filePath) {
        if (data == null || data.length == 0 || filePath == null) {
            return;
        }
        try {
            Files.write(filePath, data);
            Messages.sprintf("Saved slice to file: " + filePath);
        } catch (IOException e) {
            Messages.sprintfError("Failed to save slice to file: " + e.getMessage());
        }
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

    public static byte[] resizeImage(byte[] src, int width, int height) {
        if (src == null || src.length == 0) {
            Messages.sprintfError("Cannot resize null or empty image data");
            return null;
        }

        ByteArrayOutputStream outputStream = null;
        try {
            Messages.sprintf("Resizing image to " + width + "x" + height + " src.length: " + src.length);
            
            // Convert byte array to BufferedImage
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(src));
            if (originalImage == null) {
                Messages.sprintfError("Failed to read image data");
                return null;
            }
            
            Messages.sprintf("img IWDTH:::: " + originalImage.getWidth() + " IHGT:::: " + originalImage.getHeight());
            
            // Convert to TYPE_INT_RGB to ensure compatibility
            BufferedImage convertedImage = new BufferedImage(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                BufferedImage.TYPE_INT_RGB);
            convertedImage.getGraphics().drawImage(originalImage, 0, 0, null);
            
            // Resize the image
            BufferedImage resizedImage = resize(convertedImage, width, height);
            
            // Try to detect the original format
            String formatName = getImageFormat(src);
            if (formatName == null || formatName.isEmpty()) {
                formatName = "jpeg"; // Default format
            }
            
            Messages.sprintf("formatName:::: " + formatName);
            
            // Convert BufferedImage back to byte array
            outputStream = new ByteArrayOutputStream();
            
            // Try to write with the detected format
            boolean success = ImageIO.write(resizedImage, formatName, outputStream);
            
            // If writing with the detected format fails, try JPEG
            if (!success) {
                Messages.sprintf("Failed to write with format: " + formatName + ", trying JPEG");
                outputStream.reset();
                success = ImageIO.write(resizedImage, "jpeg", outputStream);
            }
            
            // If that also fails, try PNG
            if (!success) {
                Messages.sprintf("Failed to write with JPEG, trying PNG");
                outputStream.reset();
                success = ImageIO.write(resizedImage, "png", outputStream);
            }
            
            if (success) {
                Messages.sprintf("Successfully wrote image with size: " + outputStream.size());
                return outputStream.toByteArray();
            } else {
                Messages.sprintfError("Failed to write resized image data with any format");
                return null;
            }
            
        } catch (IOException e) {
            Messages.sprintfError("Error resizing image: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
        }
    }

    // Helper method to detect image format from byte array
    private static String getImageFormat(byte[] imageData) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            ImageInputStream iis = ImageIO.createImageInputStream(bis);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                String format = reader.getFormatName().toLowerCase();
                Messages.sprintf("Detected image format: " + format);
                return format;
            }
        } catch (IOException e) {
            // Log but continue with default format
            Messages.sprintfError("Error detecting image format: " + e.getMessage());
        }
        return null;
    }

    private static BufferedImage resize(BufferedImage img, int width, int height) {
        // Create a new BufferedImage with RGB color model for maximum compatibility
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Get graphics context and enable better quality
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw the scaled image
        g2d.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        
        return resizedImage;
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

        Messages.sprintf("Processing file: " + file);
        try {
            // Read image from file
            BufferedImage imagee = ImageIO.read(file.toFile());
            if (imagee == null) {
                throw new IOException("Unsupported or corrupted file: " + file);
            }
            // Generate hash for the image
            hash = getString(imagee);


            // Output the result for the current file
            Messages.sprintf("File: " + file + " -> Hash: " + hash);
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
