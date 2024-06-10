package common.utils;

import com.girbola.Main;
import common.utils.date.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileNameParseUtilsTest {

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
