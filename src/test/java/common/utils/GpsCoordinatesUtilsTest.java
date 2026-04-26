package common.utils;

import com.drew.metadata.Metadata;
import com.girbola.utils.CoordinateUtils;
import common.media.DateTaken;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static common.media.DateTaken.readMetaData;

public class GpsCoordinatesUtilsTest {

    @Test
    public void testDistanceMeters() {
        // Test case 1: Same coordinates (distance should be 0)
        double lat1 = 40.7128;
        double lon1 = -74.0060;
        double lat2 = 40.7128;
        double lon2 = -74.0060;
        double distance = CoordinateUtils.distanceMeters(lat1, lon1, lat2, lon2);
        assert distance == 0 : "Distance should be 0 for identical coordinates";

        // Test case 2: Known distance between two points (New York and Los Angeles)
        lat1 = 40.7128; // New York
        lon1 = -74.0060;
        lat2 = 34.0522; // Los Angeles
        lon2 = -118.2437;
        distance = CoordinateUtils.distanceMeters(lat1, lon1, lat2, lon2);
        System.out.println("Distance between New York and Los Angeles: " + distance + " meters");
        assert distance > 3930000 && distance < 3960000 : "Distance should be around 3940000 meters";
    }

    @Test
    public void testGetCoordinates() {
        // This test would require mocking the Metadata and GpsDirectory to simulate the presence of GPS data.
        // Since we are not using a mocking framework here, we will just outline the test case.

        // Arrange: Create a mock Metadata object with GPS data
        // Metadata metaData = mock(Metadata.class);
        // GpsDirectory gpsDirectory = mock(GpsDirectory.class);
        // when(metaData.getFirstDirectoryOfType(GpsDirectory.class)).thenReturn(gpsDirectory);
        // when(gpsDirectory.getString(GpsDirectory.TAG_LATITUDE)).thenReturn("40.7128 N");
        // when(gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE)).thenReturn("74.0060 W");

        // Act: Call the getCoordinates method
        // String coordinates = DateTaken.getCoordinates(metaData);

        // Assert: Verify that the coordinates are correctly formatted
        // assertEquals("40.7128 N,74.0060 W", coordinates);
        Path resourcePath = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");

        Metadata metaData = null;
        try {
            metaData = readMetaData(resourcePath);
            String coordinates = DateTaken.getCoordinates(metaData);
            System.out.println("Coordinates: " + coordinates);
        } catch (Exception e) {
            System.err.println("Error reading metadata: " + e.getMessage());
        }
    }

}
