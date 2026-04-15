package com.girbola.utils.imagehash;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AHashTest {

    /**
     * Unit tests for the aHash method in the AHash class.
     * The aHash method calculates an average hash for a given BufferedImage.
     * This hash represents an 8x8 grayscale image converted into a binary pattern
     * based on an average pixel intensity comparison.
     */

    @Test
    public void testAHash_WithSimpleImage() {
        // Arrange: Create a simple 8x8 grayscale image where all pixels are the same value.
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, 8, 8);
        g.dispose();

        // Act: Calculate the average hash of the image.
        long result = AHash.aHash(img);

        // Assert: All pixels being equal results in a hash with all bits set (64 ones).
        assertEquals(-1L, result); // -1 is the binary representation of all bits set in 64-bit.
    }

    @Test
    public void testAHash_WithCheckerboardImage() {
        // Arrange: Create an 8x8 checkerboard pattern image.
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int color = (x + y) % 2 == 0 ? 0xFFFFFF : 0x000000; // White and Black
                img.setRGB(x, y, color);
            }
        }

        // Act: Calculate the average hash of the checkerboard image.
        long result = AHash.aHash(img);

        // Assert: A checkerboard pattern has an alternating hash.
        assertEquals(0xAAAAAAAAAAAAAAAAL, result); // Alternating bits for 8x8.
    }

    @Test
    public void testAHash_WithGradientImage() {
        // Arrange: Create an 8x8 gradient image where pixel intensity increases left to right.
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int gray = (x * 255) / 7; // Linear gradient from black to white.
                int rgb = new Color(gray, gray, gray).getRGB();
                img.setRGB(x, y, rgb);
            }
        }

        // Act: Calculate the average hash of the gradient image.
        long result = AHash.aHash(img);

        // Assert: The result should represent the pixels above average as 1.
        assertEquals(0x000000FFFFE00000L, result); // Expected hash based on gradient values.
    }

    @Test
    public void testAHash_WithSingleBrightPixel() {
        // Arrange: Create an 8x8 image with all dark pixels except one bright pixel.
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                img.setRGB(x, y, 0x000000); // Black
            }
        }
        img.setRGB(4, 4, 0xFFFFFF); // One bright white pixel.

        // Act: Calculate the average hash of the image.
        long result = AHash.aHash(img);

        // Assert: The hash reflects the single bright pixel's influence.
        assertEquals(0x0000000000000800L, result); // Expected position for a single pixel above the average.
    }

    @Test
    public void testAHash_WithImageFromResourceFile() throws Exception {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image resource not found: " + resourcePath);

        BufferedImage image;
        try (java.io.InputStream imageStream = java.nio.file.Files.newInputStream(resourcePath)) {
            image = javax.imageio.ImageIO.read(imageStream);
        }

        assertNotNull(image, "Failed to read image from: " + resourcePath);
        // Act: Calculate the perceptual hash of the loaded image and time it.
        long startTime = System.nanoTime();
        long result = AHash.aHash(image);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("AHash computation time: " + durationMs + " ms");

        // Assert: Validate that a hash is generated (non-zero for a real image).
        // The exact hash value depends on the specific test image used.
        // Replace the expected value with the actual hash of your test image.
        assertEquals(0x1234567890ABCDEFL, result, "Expected hash for the test image from resources.");
    }

}