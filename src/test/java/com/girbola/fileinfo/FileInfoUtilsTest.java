package com.girbola.fileinfo;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.messages.Messages;
import com.girbola.utils.FileInfoUtils;
import common.utils.FileInfoTestUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.girbola.utils.FileInfoUtils.calculateFileSHA256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FileInfoUtilsTest {

    private final Logger log = LoggerFactory.getLogger(FileInfoUtilsTest.class);

    @TempDir
    Path tempDir;

    @Test
    void createFileInfo_plainJpegWithoutExifThumbnailDoesNotCrash() throws IOException {
        Path imagePath = tempDir.resolve("plain-no-exif.jpg");
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        assertTrue(ImageIO.write(image, "jpg", imagePath.toFile()));
        assertTrue(Files.exists(imagePath));

        FileInfo fileInfo = FileInfoUtils.createFileInfo(imagePath);

        assertNotNull(fileInfo);
        assertEquals(imagePath.toString(), fileInfo.getOrgPath());
        assertTrue(fileInfo.isImage());
    }

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
        List<String> list = new ArrayList<>();
        list.add("2025-09-20T12:04:11.840611300 FileInfo created. PATH=src\\test\\resources\\in\\20220413_160023.jpg");
        fileInfo.setFileHistories(list);
        Messages.sprintf("Fileinfo: " + fileInfo.showAllValues());
        Messages.sprintf("fileInfo1giihgo: " + fileInfo.getImageDifferenceHash());
        Path path = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");

        String expected = "FileInfo{bad=false, camera_model='SM-A515F', confirmed=false, copied=false, date=1649865623000, destination_Path='', event='', fileInfo_id=1, fileInfo_version=1, good=true, ignored=false, image=true, imageDifferenceHash=, localDateTime=null, location='', modified=false, orientation=1, orgPath='src\\test\\resources\\in\\20220413_160023.jpg', raw=false, size=3515984, suggested=false, tableDuplicated=false, tags='', thumb_length=51503, thumb_offset=916, timeShift=0, user='', video=false, workDir='', workDirDriveSerialNumber='', fileInfoHistories='[2025-09-20T12:04:11.840611300 FileInfo created. PATH=src\\test\\resources\\in\\20220413_160023.jpg]'}";
        String expected2 = "FileInfo{fileInfo_version=1, bad=false, confirmed=false, copied=false, good=true, ignored=false, image=true, raw=false," +
                " suggested=false, tableDuplicated=false, video=false, localDateTime=null, camera_model='SM-A515F', destination_Path='', event='', location=''," +
                " orgPath=" + path + "', tags='', user='', workDir='', workDirDriveSerialNumber='', fileInfo_id=2, orientation=1, thumb_length=51503, thumb_offset=916, date=1649865623000, imageDifferenceHash=9024515497931845856, size=3515984, timeShift=0}";

        Messages.sprintf("ACTUAL Fileinfo from file length= " + fileInfo.showAllValues().length() + " Expected length: " + expected.length());
        assertEquals(expected, fileInfo.showAllValues());
    }


    @Test
    void createFileInfo_with_goodDate() throws IOException {
        FileInfo fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "testi-20220413-blaa.jpg"));
        fileInfo.setFileInfo_id(1);
        if (fileInfo.isSuggested()) {
            Messages.sprintf("Suggested date: " + fileInfo.getDate());
        }
        assertEquals(true, fileInfo.isGood());
        assertEquals(false, fileInfo.isBad());
        assertEquals(false, fileInfo.isSuggested());
        assertEquals(false, fileInfo.isConfirmed());
        assertEquals(false, fileInfo.isModified());
    }

    @Test
    void createFileInfo_with_badDate() throws IOException {
        FileInfo fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "IMG.jpg"));
        fileInfo.setFileInfo_id(1);
        if (fileInfo.isSuggested()) {
            Messages.sprintf("Suggested date: " + fileInfo.getDate());
        }
        Messages.sprintf("Fileinfo: " + fileInfo.showAllValues());
        assertEquals(true, fileInfo.isBad());
        assertEquals(false, fileInfo.isGood());
        assertEquals(false, fileInfo.isSuggested());
        assertEquals(false, fileInfo.isConfirmed());
        assertEquals(false, fileInfo.isModified());
    }

    @Test
    void createFileInfo_with_suggestedDate() throws IOException {
        FileInfo fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "testi-20250920111111-tidii.jpg"));
        fileInfo.setFileInfo_id(1);
        if (fileInfo.isSuggested()) {
            Messages.sprintf("Suggested date: " + fileInfo.getDate());
        }
        Messages.sprintf("Fileinfo: " + fileInfo.showAllValues());
        assertEquals(true, fileInfo.isSuggested());
        assertEquals(true, fileInfo.isBad());
        assertEquals(false, fileInfo.isGood());
        assertEquals(false, fileInfo.isConfirmed());
        assertEquals(false, fileInfo.isModified());
        assertEquals(1758358800000L, fileInfo.getDate());
    }

// TODO KORJAA TÄMÄ TESTII!!!!!!!!IIII!!!!III!!!
    @Test
    public void testRenameFile() throws IOException {
        FileInfo fileInfo = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in", "20220413_160023.jpg"));
        fileInfo.setFileInfo_id(1);
        FileInfo fileInfo2 = FileInfoUtils.createFileInfo(Paths.get("src", "test", "resources", "in2", "20220413_160023.jpg"));
        fileInfo2.setFileInfo_id(2);

        FolderInfo folderInfo = new FolderInfo(Paths.get("src", "test", "resources", "in2"));
        folderInfo.getFileInfoList().add(fileInfo2);

        Path newPath = FileInfoUtils.renameFile(fileInfo, folderInfo);
        System.out.println("fileInfo.getOrgPath(): " + fileInfo.getOrgPath() + " newPATH: " + newPath);
        assertNotNull(newPath, "Renamed file path is null");
        assertNotEquals(fileInfo.getOrgPath(), newPath.toString(), "Original path and renamed path should not be same");
        assertTrue(newPath.toString().contains("_2"), "Renamed path should contain '_2' ");
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

        assertEquals("src\\test\\resources\\in\\IMG.jpg", newPath.toString());
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

    @Test
    public void testCalculateFileSHA256_Should_Differ() throws IOException {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG1.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);

        Path resourcePath2 = Paths.get("src", "test", "resources", "in", "IMG1_dot_difference.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);

        long startTime = System.currentTimeMillis();

        String h1 = calculateFileSHA256(resourcePath.toAbsolutePath());
        String h2 = calculateFileSHA256(resourcePath2.toAbsolutePath());

        long endTime = System.currentTimeMillis();

        System.out.println("h1: " + h1 + " h2: " + h2 + " sha256Checksum computation time: " + (endTime - startTime) + " ms");

        assertNotEquals(h1, h2, "Hashes should be different");
    }

    @Test
    public void testCalculateFileSHA256ShouldBeTheSame() throws IOException {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "20220413_160023.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);

        Path resourcePath2 = Paths.get("src", "test", "resources", "test-material", "20220413_160023.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath2), "Test image2 resource not found: " + resourcePath2);

        long startTime = System.currentTimeMillis();

        String h1 = calculateFileSHA256(resourcePath.toAbsolutePath());
        String h2 = calculateFileSHA256(resourcePath2.toAbsolutePath());

        long endTime = System.currentTimeMillis();

        System.out.println("h1: " + h1 + " h2: " + h2 + " sha256Checksum computation time: " + (endTime - startTime) + " ms");

        assertEquals(h1, h2, "Hashes should be different");
    }

    @Test
    public void testCalculateFileSHA256CalculationSpeed() throws IOException {
        Path resourcePath = Paths.get("src", "test", "resources", "in", "IMG1.jpg");
        assertTrue(java.nio.file.Files.exists(resourcePath), "Test image1 resource not found: " + resourcePath);
        File[] folder = new File("src/test/resources/in").listFiles();

        for (File file : folder) {
            long startTime = System.currentTimeMillis();
            String sha256 = calculateFileSHA256(file.toPath().toAbsolutePath());
            long endTime = System.currentTimeMillis();
            System.out.println("File: " + file.getAbsolutePath() + " sha256\n" + sha256 + " sha256Checksum computation time: " + (endTime - startTime) + " ms");
        }
    }

    @Test
    public void containsEssentialDCFEntries() {
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("DCIM"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("MISC"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("VIDEO"));
    }

    @Test
    public void containsCommonCameraFolderNames() {
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Camera"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Pictures"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Photos"));
    }

    @Test
    public void containsRawAndVideoFolderNames() {
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("RAW"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("DNG"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Movies"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Videos"));
    }

    @Test
    public void noNullOrEmptyEntriesInKnownCameraFolderNames() {
        for (String name : com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES) {
            assertNotNull(name, "Found null entry in KNOWN_CAMERA_FOLDER_NAMES");
            assertFalse(name.trim().isEmpty(), "Found empty or blank entry in KNOWN_CAMERA_FOLDER_NAMES");
            assertEquals(name, name.trim(), "Entry has leading or trailing whitespace: '" + name + "'");
        }
    }

    @Test
    public void containsNameWhenComparedCaseInsensitively() {
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.stream().anyMatch(n -> n.equalsIgnoreCase("dcim")));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.stream().anyMatch(n -> n.equalsIgnoreCase("camera")));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.stream().anyMatch(n -> n.equalsIgnoreCase("gopro")));
    }

    @Test
    public void containsManufacturerAndVendorNames() {
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Canon"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Nikon"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("Sony"));
        assertTrue(com.girbola.media.KnownCameraFolderNames.KNOWN_CAMERA_FOLDER_NAMES.contains("DJI"));
    }
}
