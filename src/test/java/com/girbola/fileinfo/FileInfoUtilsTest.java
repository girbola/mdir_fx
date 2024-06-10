package com.girbola.fileinfo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileInfoUtilsTest {

    @Test
    public void createFileInfoTest_imageType() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "milky-way-559641_640.jpg");

        try {
            FileInfo fileInfo = FileInfoUtils.createFileInfo(fileName);
            assertNotNull(fileInfo, "File info is null");
            assertEquals(fileName.toString(), fileInfo.getOrgPath(), "Original path didn't match");
            assertTrue(fileInfo.isImage(), "File should be of image type");
        } catch (IOException e) {
            fail("IOException thrown on createFileInfo: " + e.getMessage());
        }
    }

    @Test
    public void createFileInfoTest_videoType() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "153976-817104245_tiny.mp4");

        try {
            FileInfo fileInfo = FileInfoUtils.createFileInfo(fileName);
            assertNotNull(fileInfo, "File info is null");
            assertEquals(fileName.toString(), fileInfo.getOrgPath(), "Original path didn't match");
            assertTrue(fileInfo.isVideo(), "File should be of video type");
        } catch (IOException e) {
            fail("IOException thrown on createFileInfo: " + e.getMessage());
        }
    }

    @Test
    public void createFileInfoTest_rawType() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "IMG_4312.CR2");

        try {
            FileInfo fileInfo = FileInfoUtils.createFileInfo(fileName);
            assertNotNull(fileInfo, "File info is null");
            assertEquals(fileName.toString(), fileInfo.getOrgPath(), "Original path didn't match");
            assertTrue(fileInfo.isRaw(), "File should be of raw type");
        } catch (IOException e) {
            fail("IOException thrown on createFileInfo: " + e.getMessage());
        }
    }

    @Test
    public void createFileInfoTest_unsupportedType() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "unsupportedfile.txt");

        try {
            FileInfo fileInfo = FileInfoUtils.createFileInfo(fileName);
            assertNull(fileInfo, "File info should be null for unsupported types");
        } catch (IOException e) {
            fail("IOException thrown on createFileInfo: " + e.getMessage());
        }
    }
}