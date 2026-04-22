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

    public static long computePHash(String imagePath) {
        // Load image in color (or change to IMREAD_GRAYSCALE)
        Mat img = opencv_imgcodecs.imread(imagePath);
        if (img.empty()) {
            throw new IllegalArgumentException("Cannot load image: " + imagePath);
        }

        // Convert to grayscale
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(img, gray, opencv_imgproc.COLOR_BGR2GRAY);

        // Resize to 32x32
        int size = 32;
        Mat resized = new Mat();
        opencv_imgproc.resize(gray, resized, new Size(size, size));

        // Convert to float32 for DCT
        Mat f = new Mat();
        resized.convertTo(f, opencv_core.CV_32F);

        // Apply DCT
        Mat dct = new Mat();
        opencv_core.dct(f, dct);

        // Extract top-left 8x8 block of DCT coefficients
        int smallSize = 8;
        double[] vals = new double[smallSize * smallSize];
        int idx = 0;
        for (int y = 0; y < smallSize; y++) {
            for (int x = 0; x < smallSize; x++) {
                // Optionally skip the DC coefficient at (0,0) by continuing when x==0 && y==0
                float v = dct.ptr(y, x).getFloat(); // read float value
                vals[idx++] = v;
            }
        }

        // Optionally ignore the first coefficient (DC) when computing median.
        // Here we include all 64 coefficients; if you prefer, skip index 0 when computing median.
        double median = median(vals);

        // Build 64-bit hash: bit i = 1 if coeff > median
        long hash = 0L;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] > median) {
                hash |= (1L << i);
            }
        }

        // Release Mats
        img.release();
        gray.release();
        resized.release();
        f.release();
        dct.release();

        return hash;
    }

    // Utility: compute median of double array
    private static double median(double[] arr) {
        double[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        int mid = copy.length / 2;
        if (copy.length % 2 == 0) {
            return (copy[mid - 1] + copy[mid]) / 2.0;
        } else {
            return copy[mid];
        }
    }

    // Helper to format hash as 16-hex string and binary string
    public static String hashToHex(long hash) {
        return String.format("%016x", hash);
    }

    public static String hashToBinaryString(long hash) {
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < 64; i++) {
            sb.append(((hash >> i) & 1L) == 1L ? '1' : '0');
        }
        // bits are in coefficient order (LSB = first coefficient). Reverse if you want MSB-first:
        return sb.reverse().toString();
    }

    // Hamming distance between two 64-bit pHashes
    public static int hammingDistance(long h1, long h2) {
        long x = h1 ^ h2;
        return Long.bitCount(x);
    }


//    @BeforeAll
//    public static void init() {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }


//    @Test
//    public void testPHash_WithImageFromResourceFile() throws Exception {
//
//
//        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG_2.jpg");
//        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);
//
//        Path resourcePath2 = Paths.get("src", "test", "resources", "in", "IMG_2.JPG");
//        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);
//
//        Mat img1 = Imgcodecs.imread(resourcePath.toAbsolutePath().toString());
//        Mat img2 = Imgcodecs.imread(resourcePath2.toAbsolutePath().toString());
//
//        if (img1.empty() || img2.empty()) {
//            System.out.println("Error loading images!");
//            return;
//        }
//
//        assertNotNull(img1, "Failed to read image from: " + resourcePath);
//        assertNotNull(img2, "Failed to read image2 from: " + resourcePath2);
//
//        // Act: Calculate the perceptual hash of the loaded image and time it.
//        long startTime = System.nanoTime();
//
//        // Create pHash instance
//        ImgHashBase phash = org.opencv.img_hash.ImageComparionUtils.create();
//
//        // Compute hashes
//        Mat hash1 = new Mat();
//        Mat hash2 = new Mat();
//
//        phash.compute(img1, hash1);
//        phash.compute(img2, hash2);
//
//        // Compare hashes
//        double distance = phash.compare(hash1, hash2);
//
//
//        long endTime = System.nanoTime();
//        long durationMs = (endTime - startTime) / 1_000_000;
//
//        System.out.println("2 files PHashed computation time: " + durationMs + " ms");
//
//        System.out.println("Similarity: " + distance);
//
//        // Assert: Validate similarity score is within expected range (0.0 = identical, higher = more different)
//        assertTrue(distance >= 0.0, "Similarity score should be non-negative");
//        assertTrue(distance <= 64.0, "Similarity score should be within expected range for ImageComparionUtils");
//    }

    @Test
    public void testPHash() {

        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG1.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);

        Path resourcePath2 = Paths.get("src", "test", "resources", "in", "IMG1_dot_difference.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);

        long startTime = System.currentTimeMillis();

        long h1 = computePHash(resourcePath.toAbsolutePath().toString());
        long h2 = computePHash(resourcePath2.toAbsolutePath().toString());

        long endTime = System.currentTimeMillis();
        System.out.println("ImageComparionUtils computation time: " + (endTime - startTime) + " ms");

        System.out.println("Hash1 (hex): " + hashToHex(h1));
//        System.out.println("Hash1 (bin): " + hashToBinaryString(h1));
        System.out.println("Hash2 (hex): " + hashToHex(h2));
//        System.out.println("Hash2 (bin): " + hashToBinaryString(h2));
        System.out.println("Hamming distance: " + hammingDistance(h1, h2));
        assertEquals(31, hammingDistance(h1, h2), "Hashes should be different");
    }
}