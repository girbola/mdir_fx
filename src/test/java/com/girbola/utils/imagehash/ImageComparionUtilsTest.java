package com.girbola.utils.imagehash;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pHash method in the ImageComparionUtils class.
 * The pHash method generates a perceptual hash of an image by resizing it,
 * computing the Discrete Cosine Transform (DCT), and extracting features from the low frequencies.
 * This test class ensures that the pHash method works as expected for various input scenarios.
 */
public class ImageComparionUtilsTest {


    @Test
    public void testPHash() {

        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG1.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);

        Path resourcePath2 = Paths.get("src", "test", "resources", "in", "IMG1_dot_difference.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);

        long startTime = System.currentTimeMillis();

        String h1 = ImageComparionUtils.computePHash(resourcePath.toAbsolutePath().toString());
        String h2 = ImageComparionUtils.computePHash(resourcePath2.toAbsolutePath().toString());

        long endTime = System.currentTimeMillis();

        System.out.println("PHash computation time: " + (endTime - startTime) + " ms");

        assertEquals(1, ImageComparionUtils.hammingDistance(h1, h2), "Hashes should be different");
    }
}