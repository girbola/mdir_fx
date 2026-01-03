package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.date.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
    void testTryParseDateTimeAsLongWithValidDateWithSplittedText() {
        FileInfo fileInfo = new FileInfo("Screenshot 2024-02-28 at 13.27.10 (3).png", 1);
        long timestamp = FileNameParseUtils.tryParseDateTimeAsLong(fileInfo);
        LocalDateTime localDateTime = DateUtils.longToLocalDateTime(timestamp);
Messages.sprintf("testTryParseDateTimeAsLongWithValidDateWithSplittedText:::::::: localDateTime: " + localDateTime);
        assertEquals(1709119630000L, timestamp); // Ensure the date is parsed
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
    void testGetFileNameRunningNumberWithNoDigits() {
        String fileName = "IMG";
        Integer expected = 0;
        Integer actual = FileNameParseUtils.getFileNameRunningNumber(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void extractDateFromFileNameWithValidDateAndTime() {
        String fileName = "Screenshot 2024-02-28 at 13.27.10.png";
        String expected = "2024-02-28 13.27.10";
        String actual = FileNameParseUtils.extractDateFromFileName(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void extractDateFromFileNameWithValidDateOnly() {
        String fileName = "Document 2024-02-28.pdf";
        String expected = "2024-02-28"; // No time part, should return null
        String actual = FileNameParseUtils.extractDateFromFileName(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void extractDateFromFileNameWithNoDate() {
        String fileName = "random_file_name.txt";
        String expected = null;
        String actual = FileNameParseUtils.extractDateFromFileName(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void extractDateFromFileNameWithExtraTextAroundDate() {
        String fileName = "Some text before 2024-02-28 at 13.27.10 and after.png";
        String expected = "2024-02-28 13.27.10";
        String actual = FileNameParseUtils.extractDateFromFileName(fileName);
        assertEquals(expected, actual);
    }

    @Test
    void extractDateFromFileNameWithMultipleDates() {
        String fileName = "2024-02-28 at 13.27.10 and 2023-01-01 at 12.00.00.png";
        String expected = "2024-02-28 13.27.10"; // Should match the first occurrence
        String actual = FileNameParseUtils.extractDateFromFileName(fileName);
        assertEquals(expected, actual);
    }


}
