package com.girbola.fileinfo;

import com.girbola.messages.Messages;
import common.utils.FileInfoTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
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
    void testMoveFileInfoToAnotherLocation() {
        FileInfo fileInfo = FileInfoTestUtil.createFileInfoForTesting();
        Messages.sprintf("FileInfo values are:\n" + fileInfo.showAllValues());

    }

    @Test
    void createFileInfo() throws IOException {
        FileInfo fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "20220413_160023.jpg"));
        Messages.sprintf("Fileinfo: " + fileInfo.showAllValues());
        Messages.sprintf("fileInfo1giihgo: " + fileInfo.getImageDifferenceHash());
        String expected = "FileInfo{fileInfo_version=2, bad=false, confirmed=false, copied=false, good=true, ignored=false, image=true, raw=false, suggested=false, tableDuplicated=false, video=false, localDateTime=null, camera_model='SM-A515F', destination_Path='', event='', location='', orgPath='src" + File.separator +
                "test" + File.separator + "resources" + File.separator + "in" + File.separator + "20220413_160023.jpg', tags='', user='', workDir='', workDirDriveSerialNumber='', fileInfo_id=2, orientation=1, thumb_length=51503, thumb_offset=916, date=1649865623000, imageDifferenceHash=39770222055762649, size=3515984, timeShift=0}";

        Messages.sprintf("ACTUAL Fileinfo from file length= " + fileInfo.showAllValues().length() + " Expected length: " + expected.length());
        assertEquals(expected, fileInfo.showAllValues());
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