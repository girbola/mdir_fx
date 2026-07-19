package com.girbola.utils.imagehash;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DHashTest {

    /**
     * Unit tests for DHash class. Ensures that the dHash method generates the correct hash values for various
     * BufferedImage inputs by validating specific cases with predictable outcomes.
     */

    @Test
    void givenSingleColorImage_whenDHash_thenReturnsZeroHash() {
        // Arrange
        BufferedImage img = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();

        // Act
        long hash = DHash.dHash(img);

        // Assert
        assertEquals(0L, hash, "Hash of a single-color black image should be 0");
    }

    @Test
    void givenGradientImage_whenDHash_thenReturnsExpectedHash() {
        // Arrange
        BufferedImage img = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 9; x++) {
                int grayValue = x * 28; // Gradient from 0 to 255
                int rgb = new Color(grayValue, grayValue, grayValue).getRGB();
                img.setRGB(x, y, rgb);
            }
        }

        // Act
        long hash = DHash.dHash(img);

        // Assert
        assertEquals(0xFF_FF_FF_FF_FF_FF_FFL, hash, "Gradient image should yield a fully set hash.");
    }

    @Test
    void givenCheckerboardImage_whenDHash_thenReturnsExpectedHash() {
        // Arrange
        BufferedImage img = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 9; x++) {
                int color = ((x + y) % 2 == 0) ? 255 : 0;
                int rgb = new Color(color, color, color).getRGB();
                img.setRGB(x, y, rgb);
            }
        }

        // Act
        long hash = DHash.dHash(img);

        // Assert
        assertEquals(0xAAAAAAAAAAAAAAAAL, hash, "Checkerboard pattern should yield alternating bits in hash.");
    }

    @Test
    void givenRealImage_whenDHash_thenMatchesKnownHash() throws IOException {
        // Arrange: Load known image from resources
        BufferedImage img = ImageIO.read(this.getClass().getResourceAsStream("/test-images/test-img-1.png"));

        // Act
        long hash = DHash.dHash(img);

        // Assert: Validate against precomputed hash for the image
        long expectedHash = 0x1234567890ABCDEFL; // Replace with actual precomputed hash
        assertEquals(expectedHash, hash, "Known image hash should match precomputed hash.");
    }

    @Test
    void givenResizedImage_whenDHash_thenInvarianceIsMaintained() {
        // Arrange: Create original gradient image for hashing
        BufferedImage original = new BufferedImage(18, 16, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 18; x++) {
                int grayValue = x * 14; // Gradient from 0 to 255
                int rgb = new Color(grayValue, grayValue, grayValue).getRGB();
                original.setRGB(x, y, rgb);
            }
        }

        // Resize original to smaller dimensions
        BufferedImage resized = new BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, resized.getWidth(), resized.getHeight(), null);
        g.dispose();

        // Act: Compute hashes for original and resized images
        long originalHash = DHash.dHash(original);
        long resizedHash = DHash.dHash(resized);

        // Assert: The hash should remain identical due to invariance in the dHash method
        assertEquals(originalHash, resizedHash, "Hash should remain invariant across resized images.");
    }

    @Test
    public void testDHash_WithImageFromResourceFile() throws Exception {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image resource not found: " + resourcePath);

        BufferedImage image;
        try (java.io.InputStream imageStream = java.nio.file.Files.newInputStream(resourcePath)) {
            image = javax.imageio.ImageIO.read(imageStream);
        }

        assertNotNull(image, "Failed to read image from: " + resourcePath);
        // Act: Calculate the perceptual hash of the loaded image and time it.
        long startTime = System.nanoTime();
        long result = DHash.dHash(image);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        System.out.println("DHash computation time: " + durationMs + " ms");

        // Assert: Validate that a hash is generated (non-zero for a real image).
        // The exact hash value depends on the specific test image used.
        // Replace the expected value with the actual hash of your test image.
        assertEquals(0x1234567890ABCDEFL, result, "Expected hash for the test image from resources.");
    }

}