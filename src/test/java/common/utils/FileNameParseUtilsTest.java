package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileNameParseUtilsTest {

    @Test
    void testTryParseDateTimeAsLongWithValidDateTime() {
        FileInfo fileInfo = new FileInfo("IMG_20230915_120101.jpg", 1);
        long timestamp = FileNameParseUtils.tryParseDateTimeAsLong(fileInfo);
        Messages.sprintf("timestamp: " + timestamp);
        //assertNotEquals(0L, timestamp);
        assertEquals(1694768461000L, timestamp); // Ensure the date is parsed
    }

    @Test
    void testTryParseDateTimeAsLongWithValidDateOnly() {
        FileInfo fileInfo = new FileInfo("IMG_20230915.jpg", 1);
        long timestamp = FileNameParseUtils.tryParseDateTimeAsLong(fileInfo);
        assertEquals(1694768400000L, timestamp); // Ensure the date is parsed
    }

    @Test
    void testTryParseDateTimeAsLongWithNoDate() {
        FileInfo fileInfo = new FileInfo("IMG_no_date.jpg", 1);
        long timestamp = FileNameParseUtils.tryParseDateTimeAsLong(fileInfo);
        assertEquals(0L, timestamp); // Ensure no date returns 0L
    }

    @Test
    void testGetFileNameRunningNumber() {
        String fileName = "IMG_1234";
        Integer expected = 1234;
        Integer actual = FileNameParseUtils.getFileNameRunningNumber(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void testGetFileNameRunningNumberWithMultipleSeriesOfDigits() {
        String fileName = "IMG_12_34_56_78";
        Integer expected = 12345678;
        Integer actual = FileNameParseUtils.getFileNameRunningNumber(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void testGetFileNameRunningNumberWithNoDigits() {
        String fileName = "IMG";
        Integer expected = 0;
        Integer actual = FileNameParseUtils.getFileNameRunningNumber(fileName);
        assertEquals(expected, actual);
    }

}
