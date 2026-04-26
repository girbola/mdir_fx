package com.girbola.sql;

import com.girbola.fileinfo.FileInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileInfo_SqlTest {

    /**
     * Test class for the FileInfoSql class.
     * Focused on testing the addToFileInfoDB method, particularly its functionality to correctly bind parameters
     * from a FileInfo object to a PreparedStatement.
     */

    @Test
    void testAddToFileInfoDB_success() throws SQLException {
        // Arrange
        PreparedStatement pstmt = Mockito.mock(PreparedStatement.class);
        FileInfo fileInfo = createSampleFileInfo();

        System.out.println("fileInfo.showAllValues(): " + fileInfo.showAllValues());

        // Act
        boolean result = FileInfoSql.addToFileInfoDB(pstmt, fileInfo);

        // Assert
        assertTrue(result);

        // Verify PreparedStatement bindings
        Mockito.verify(pstmt).setBoolean(1, fileInfo.isBad());
        Mockito.verify(pstmt).setString(2, fileInfo.getCamera_model());
        Mockito.verify(pstmt).setBoolean(3, fileInfo.isConfirmed());
        Mockito.verify(pstmt).setString(4, fileInfo.getDestination_Path());
        Mockito.verify(pstmt).setLong(5, fileInfo.getDate());
        Mockito.verify(pstmt).setString(6, fileInfo.getEvent());
        Mockito.verify(pstmt).setInt(7, fileInfo.getFileInfo_id());
        Mockito.verify(pstmt).setString(8, String.join(",", fileInfo.getFileHistories()));
        Mockito.verify(pstmt).setBoolean(9, fileInfo.isGood());
        Mockito.verify(pstmt).setBoolean(10, fileInfo.isCopied());
        Mockito.verify(pstmt).setBoolean(11, fileInfo.isIgnored());
        Mockito.verify(pstmt).setBoolean(12, fileInfo.isImage());
        Mockito.verify(pstmt).setString(13, fileInfo.getImageDifferenceHash());
        Mockito.verify(pstmt).setString(14, fileInfo.getLocation());
        Mockito.verify(pstmt).setBoolean(15, fileInfo.isModified());
        Mockito.verify(pstmt).setString(16, fileInfo.getOrgPath());
        Mockito.verify(pstmt).setString(17, fileInfo.getWorkDirDriveSerialNumber());
        Mockito.verify(pstmt).setInt(18, fileInfo.getOrientation());
        Mockito.verify(pstmt).setBoolean(19, fileInfo.isRaw());
        Mockito.verify(pstmt).setLong(20, fileInfo.getSize());
        Mockito.verify(pstmt).setBoolean(21, fileInfo.isSuggested());
        Mockito.verify(pstmt).setBoolean(22, fileInfo.isTableDuplicated());
        Mockito.verify(pstmt).setString(23, fileInfo.getTags());
        Mockito.verify(pstmt).setInt(24, fileInfo.getThumb_length());
        Mockito.verify(pstmt).setInt(25, fileInfo.getThumb_offset());
        Mockito.verify(pstmt).setLong(26, fileInfo.getTimeShift());
        Mockito.verify(pstmt).setBoolean(27, fileInfo.isVideo());
        Mockito.verify(pstmt).setString(28, fileInfo.getWorkDir());
        Mockito.verify(pstmt).addBatch();
    }

    @Test
    void testAddToFileInfoDB_failure() throws SQLException {
        // Arrange
        PreparedStatement pstmt = Mockito.mock(PreparedStatement.class);
        FileInfo fileInfo = createSampleFileInfo();

        Mockito.doThrow(new SQLException("Mocked SQL exception")).when(pstmt).setBoolean(Mockito.anyInt(), Mockito.anyBoolean());

        // Act
        boolean result = FileInfoSql.addToFileInfoDB(pstmt, fileInfo);

        // Assert
        assertFalse(result);
    }

    /**
     * Creates a sample FileInfo object with populated fields for testing purposes.
     *
     * @return A FileInfo object with sample data.
     */
    private FileInfo createSampleFileInfo() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setBad(false);
        fileInfo.setCamera_model("SampleModel");
        fileInfo.setConfirmed(true);
        fileInfo.setDestination_Path("/sample/destination");
        fileInfo.setDate(1649865623000L); // Sample date
        fileInfo.setEvent("SampleEvent");
        fileInfo.setFileInfo_id(1);
        List<String> fileHistories = new ArrayList<>();
        fileHistories.add("History1");
        fileHistories.add("History2");
        fileInfo.setFileHistories(fileHistories);
        fileInfo.setGood(true);
        fileInfo.setCopied(false);
        fileInfo.setIgnored(false);
        fileInfo.setImage(true);
        fileInfo.setImageDifferenceHash("123456");
        fileInfo.setLocation("SampleLocation");
        fileInfo.setModified(false);
        fileInfo.setOrgPath("/sample/path");
        fileInfo.setWorkDirDriveSerialNumber("Drive123");
        fileInfo.setOrientation(90);
        fileInfo.setRaw(false);
        fileInfo.setSize(1024L);
        fileInfo.setSuggested(true);
        fileInfo.setTableDuplicated(false);
        fileInfo.setTags("Tag1, Tag2");
        fileInfo.setThumb_length(512);
        fileInfo.setThumb_offset(128);
        fileInfo.setTimeShift(60L);
        fileInfo.setVideo(false);
        fileInfo.setWorkDir("/work/directory");
        return fileInfo;
    }
}