package common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.girbola.utils.imagehash.PHash.computePHash;
import static com.girbola.utils.imagehash.PHash.hammingDistance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ImageUtilsTest {

    /**
     * Tests for calculateRAWImagePHash method in the ImageUtils class.
     * The calculateRAWImagePHash method calculates a perceptual hash (pHash) for RAW images
     * by extracting their embedded thumbnails and analyzing their content.
     */

    @Test
    public void testCalculateRAWImagePHash_WithValidPath() {
        // Arrange: Create a mock Path and a valid thumbnail for testing
        Path imagePath = Paths.get("src", "test", "resources", "test-material", "IMG_4312.CR2");
        assertTrue(Files.exists(imagePath), "Test image resource not found: " + imagePath);

        // Act: Call the method
        String pHash = ImageUtils.calculateRAWImagePHash(imagePath);
        assertEquals("ffffffff38000000", pHash);

        System.out.println("pHash: " + pHash);
        // Assert: Ensure the pHash is a non-empty string
        assertNotNull(pHash, "The pHash should not be null.");
        assertFalse(pHash.isEmpty(), "The pHash should not be an empty string.");
    }

    @Test
    public void testCalculateRAWImagePHash_WithInvalidPath() {
        // Arrange: Pass a non-existing image path
        Path invalidPath = Paths.get("src", "test", "resources", "in", "non_existent_file.raw");
        assertFalse(Files.exists(invalidPath), "Test image resource should not exist");

        // Act: Call the method
        String pHash = ImageUtils.calculateRAWImagePHash(invalidPath);

        // Assert: Method should return an empty string on invalid input
        assertNotNull(pHash, "The pHash should not be null.");
        assertTrue(pHash.isEmpty(), "The pHash should be an empty string for invalid input.");
    }

//
//    @Test
//    public void testCalculateRAWImagePHash_WithDecodingFailure() throws IOException {
//        // Arrange: Mock the readFileData and decodeToImage methods to simulate a decoding failure
//        Path pathMock = mock(Path.class);
//
//        byte[] mockFileData = {0, 1, 2, 3, 4, 5}; // Simulated file bytes
//
//        // Stub internal helper methods using Mockito
//        ImageUtils imageUtilsSpy = Mockito.spy(ImageUtils.class);
//        doReturn(mockFileData).when(imageUtilsSpy).readFileData(pathMock);
//        doReturn(null).when(imageUtilsSpy).decodeToImage(any(byte[].class));
//
//        // Act: Call the method with mocked behavior
//        String result = imageUtilsSpy.calculateRAWImagePHash(pathMock);
//
//        // Assert: Ensure the pHash is empty when decoding fails
//        assertNotNull(result, "The pHash should not be null.");
//        assertTrue(result.isEmpty(), "The pHash should be an empty string when thumbnail decoding fails.");
//    }

    @Test
    public void testCalculateRAWImagePHash_WithNullPath() {
        // Act: Directly pass a null path to the method
        String result = ImageUtils.calculateRAWImagePHash(null);

        // Assert: Expect an empty string as output
        assertNotNull(result, "The pHash should not be null.");
        assertTrue(result.isEmpty(), "The pHash should be an empty string for null input.");
    }

    @Test
    public void testPhash() {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);

        Path resourcePath2 = Paths.get("src", "test", "resources", "in", "20220413_160023_edited.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);

        long startTime = System.currentTimeMillis();
        String h1 = computePHash(resourcePath.toAbsolutePath().toString());

        long endTime = System.currentTimeMillis();
        System.out.println("PHash1 computation time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        String h2 = computePHash(resourcePath2.toAbsolutePath().toString());
        endTime = System.currentTimeMillis();

        System.out.println("PHash2 computation time: " + (endTime - startTime) + " ms");

        System.out.println("Hash1: " + h1);
        System.out.println("Hash2: " + h2);
        System.out.println("Hamming Distance: " + hammingDistance(h1, h2));
        assertEquals(1, hammingDistance(h1, h2), "Hamming distance between similar images should be 1");
    }

    @Test
    public void testSameImagePhashMatching() {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);

        Path resourcePath2 = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");
        //Path resourcePath2 = Paths.get("src", "test", "resources", "in", "20220413_160023_edited.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);

        long startTime = System.currentTimeMillis();
        String h1 = computePHash(resourcePath.toAbsolutePath().toString());

        long endTime = System.currentTimeMillis();
        System.out.println("PHash1 computation time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        String h2 = computePHash(resourcePath2.toAbsolutePath().toString());
        endTime = System.currentTimeMillis();

        System.out.println("PHash2 computation time: " + (endTime - startTime) + " ms");

        System.out.println("Hash1: " + h1);
        System.out.println("Hash2: " + h2);
        System.out.println("Hamming Distance: " + hammingDistance(h1, h2));
        assertEquals(0, hammingDistance(h1, h2), "Hamming distance between similar images should be 0");
    }
}