package com.girbola.controllers.folderscanner;

import common.utils.OSHI_Utils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mockStatic;

public class SelectedFolderUtilsTest {

    @Test
    void getDriveSerialNumberFromPathReturnsSerialNumberForValidPath() {
        String validPath = "/valid/path";
        String correctSerialNumber = "12345";
        String expectedSerialNumber = "12345";

        try (MockedStatic<OSHI_Utils> utilities = mockStatic(OSHI_Utils.class)) {
            utilities.when(() -> OSHI_Utils.getDriveSerialNumber(validPath)).thenReturn(correctSerialNumber);
            String result = SelectedFolderUtils.getDriveSerialNumberFromPath(validPath);
            System.out.println("Result: " + result);
            assertEquals(expectedSerialNumber, result);
        }
    }

    @Test
    void wrongGetDriveSerialNumberFromPathReturnsSerialNumberForDifferentPath() {
        String validPath = "/valid/path";
        String correctSerialNumber = "12345";
        String expectedSerialNumber = "45678";

        try (MockedStatic<OSHI_Utils> utilities = mockStatic(OSHI_Utils.class)) {
            utilities.when(() -> OSHI_Utils.getDriveSerialNumber(validPath)).thenReturn(correctSerialNumber);
            String result = SelectedFolderUtils.getDriveSerialNumberFromPath(validPath);
            System.out.println("Result: " + result);
            assertNotEquals(expectedSerialNumber, result);
        }
    }

    @Test
    void getDriveSerialNumberFromPathReturnsEmptyStringForInvalidPath() {
        String invalidPath = "/invalid/path";

        try (MockedStatic<OSHI_Utils> utilities = mockStatic(OSHI_Utils.class)) {
            utilities.when(() -> OSHI_Utils.getDriveSerialNumber(invalidPath)).thenThrow(new RuntimeException());
            String result = SelectedFolderUtils.getDriveSerialNumberFromPath(invalidPath);
            assertEquals("", result);
        }
    }

    @Test
    void getDriveSerialNumberFromPathHandlesNullPathGracefully() {
        String result = SelectedFolderUtils.getDriveSerialNumberFromPath(null);
        assertEquals("", result);
    }
}
