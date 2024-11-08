package com.girbola.fileinfo;

import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.utils.FileInfoUtils;
import common.utils.FileInfoTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.*;

import static org.junit.jupiter.api.Assertions.*;

public class FileInfoUtilsTest {

    private final Logger log = LoggerFactory.getLogger(FileInfoUtilsTest.class);

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
        fileInfo.setFileInfo_id(1);
        Messages.sprintf("Fileinfo: " + fileInfo.showAllValues());
        Messages.sprintf("fileInfo1giihgo: " + fileInfo.getImageDifferenceHash());
        Path path = Paths.get("src","test","resources", "in","20220413_160023.jpg");

        String expected = "FileInfo{bad=false, camera_model='SM-A515F', confirmed=false, copied=false, date=1649865623000, destination_Path='', event='', fileInfo_id=9, fileInfo_version=1, good=true, ignored=false, image=true, imageDifferenceHash=9024515497931845856, localDateTime=null, location='', orientation=1, orgPath='src\\test\\resources\\in\\20220413_160023.jpg', raw=false, size=0, suggested=false, tableDuplicated=false, tags='', thumb_length=51503, thumb_offset=916, timeShift=0, user='', video=false, workDir='', workDirDriveSerialNumber=''}";
        String expected2 = "FileInfo{fileInfo_version=1, bad=false, confirmed=false, copied=false, good=true, ignored=false, image=true, raw=false," +
                " suggested=false, tableDuplicated=false, video=false, localDateTime=null, camera_model='SM-A515F', destination_Path='', event='', location=''," +
                " orgPath=" + path + "', tags='', user='', workDir='', workDirDriveSerialNumber='', fileInfo_id=2, orientation=1, thumb_length=51503, thumb_offset=916, date=1649865623000, imageDifferenceHash=9024515497931845856, size=3515984, timeShift=0}";

        Messages.sprintf("ACTUAL Fileinfo from file length= " + fileInfo.showAllValues().length() + " Expected length: " + expected.length());
        assertEquals(expected, fileInfo.showAllValues());
    }

    @Test
    public void testRenameFile() throws IOException {
        FileInfo fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "20220413_160023.jpg"));
        fileInfo.setFileInfo_id(1);
        FileInfo fileInfo2 = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "out", "20220413_160023.jpg"));
        fileInfo2.setFileInfo_id(2);

        FolderInfo folderInfo = new FolderInfo(Paths.get("src", "test", "resources", "in"));
        folderInfo.getFileInfoList().add(fileInfo2);

        Path newPath = FileInfoUtils.renameFile(fileInfo, folderInfo);

        assertNotNull(newPath, "Renamed file path is null");
        assertNotEquals(fileInfo.getOrgPath(), newPath.toString(), "Original path and renamed path should not be same");
        assertTrue(newPath.toString().contains("_"), "Renamed path should contain '_' ");
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
    public void testRenameFile_FileAlreadyExistsInDestination() throws IOException {
        FileInfo fileInfoSrc = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "IMG.jpg"));
        fileInfoSrc.setFileInfo_id(1);
        FolderInfo folderInfoDest = new FolderInfo(Paths.get("src", "test", "resources", "in"));

        FileInfo fileInfoSrc2 = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "out", "IMG1.jpg"));
        fileInfoSrc2.setFileInfo_id(2);

        // Add fileInfoSrc in folderInfoDest's list to simulate a file with same name already exists
        folderInfoDest.getFileInfoList().add(fileInfoSrc2);
        Path newPath = FileInfoUtils.renameFile(fileInfoSrc, folderInfoDest);

        assertEquals("src\\test\\resources\\in\\IMG.jpg",newPath.toString());
    }


    @Test
    public void createFileInfoTest_rawType() {
        Path fileName = Paths.get("src", "test", "resources", "test-material", "IMG_4312.CR2");

        try {
            FileInfo fileInfo = FileInfoUtils.createFileInfo(fileName);
            fileInfo.setFileInfo_id(1);
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
    @Test
    public void testCompareImagesMetadata_SameFileInfoMetadata() {
        Path sourcePath = Paths.get("src", "test", "resources", "test-material", "milky-way-559641_640.jpg");
        try {
            FileInfo fileInfo = FileInfoUtils.createFileInfo(sourcePath);
            fileInfo.setFileInfo_id(1);

            FileInfo fileInfo2 = FileInfoUtils.createFileInfo(sourcePath);
            fileInfo2.setFileInfo_id(2);

            assertTrue(FileInfoUtils.compareImagesMetadata(fileInfo, fileInfo2), "Comparison of identical files failed");
        } catch (IOException e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    @Test
    public void testCompareImagesMetadata_DifferentFileInfoMetadataFalse() {
        Path sourcePath = Paths.get("src", "test", "resources", "test-material", "milky-way-559641_640.jpg");
        Path differentPath = Paths.get("src", "test", "resources", "test-material", "another-image2.jpg");
        try {
            FileInfo fileInfo1 = FileInfoUtils.createFileInfo(sourcePath);
            fileInfo1.setFileInfo_id(1);

            FileInfo fileInfo2 = FileInfoUtils.createFileInfo(differentPath);
            fileInfo2.setFileInfo_id(2);

            log.info("Fileinfo1: " + fileInfo1.getImageDifferenceHash() + " fileInfo2: " + fileInfo2.getImageDifferenceHash());

            assertFalse(FileInfoUtils.compareImagesMetadata(fileInfo1, fileInfo2), "Comparison of different files incorrectly returned false");
        } catch (IOException e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    @Test
    public void testCompareImagesMetadata_DifferentFileInfoMetadataTrue() {
        Path sourcePath = Paths.get("src", "test", "resources", "test-material", "milky-way-559641_640.jpg");
        Path differentPath = Paths.get("src", "test", "resources", "test-material", "another-image.jpg");
        try {
            FileInfo fileInfo1 = FileInfoUtils.createFileInfo(sourcePath);
            fileInfo1.setFileInfo_id(1);

            FileInfo fileInfo2 = FileInfoUtils.createFileInfo(differentPath);
            fileInfo2.setFileInfo_id(2);

            log.info("Fileinfo1: " + fileInfo1.getImageDifferenceHash() + " fileInfo2: " + fileInfo2.getImageDifferenceHash());

            assertTrue(FileInfoUtils.compareImagesMetadata(fileInfo1, fileInfo2), "Comparison of different files incorrectly returned true");
        } catch (IOException e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }
}