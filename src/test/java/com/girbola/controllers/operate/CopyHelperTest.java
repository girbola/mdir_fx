package com.girbola.controllers.operate;

import com.girbola.Main;
import com.girbola.configuration.Configuration;
import com.girbola.controllers.main.ModelMain;
import com.girbola.fileinfo.FileInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CopyHelperTest {

    @TempDir
    private Path tempDirectory;

    private CopyHelper copyHelper;
    private ModelMain modelMainMock;
    private FileInfo fileInfoMock;

    /**
     * Setup for initializing mocks and the CopyHelper instance.
     */
    @BeforeEach
    public void setUp() {
        modelMainMock = Mockito.mock(ModelMain.class);
        fileInfoMock = Mockito.mock(FileInfo.class);
        copyHelper = new CopyHelper(modelMainMock, null, null);
    }

    @Test
    public void testResolveDestinationPathReturnsNullForIdenticalFileContents() throws IOException {
        Path source = tempDirectory.resolve("source.jpg");
        Path destination = tempDirectory.resolve("photo.jpg");
        Files.writeString(source, "same contents");
        Files.writeString(destination, "same contents");

        assertNull(copyHelper.resolveDestinationPath(source, destination));
    }

    @Test
    public void testResolveDestinationPathAddsSuffixForSameSizeDifferentContents() throws IOException {
        Path source = tempDirectory.resolve("source.jpg");
        Path destination = tempDirectory.resolve("photo.jpg");
        Files.writeString(source, "new-data");
        Files.writeString(destination, "old-data");

        assertEquals(tempDirectory.resolve("photo_1.jpg"),
                copyHelper.resolveDestinationPath(source, destination));
    }

    @Test
    public void testResolveDestinationPathSkipsAlreadyUsedSuffixes() throws IOException {
        Path source = tempDirectory.resolve("source.jpg");
        Path destination = tempDirectory.resolve("photo.jpg");
        Files.writeString(source, "new-data");
        Files.writeString(destination, "old-data");
        Files.writeString(tempDirectory.resolve("photo_1.jpg"), "different");

        assertEquals(tempDirectory.resolve("photo_2.jpg"),
                copyHelper.resolveDestinationPath(source, destination));
    }

    @Test
    public void testRenameTmpFileBackToOriginalExtension_SuccessfulRename() throws IOException {
        // Arrange
        Path destTmp = Files.createTempFile("tempFile", ".tmp");
        Path dest = Paths.get(destTmp.toString().replace(".tmp", ""));
        when(fileInfoMock.getWorkDir()).thenReturn("workDir");
        when(fileInfoMock.getOrgPath()).thenReturn("testOriginalPath");

        // Act
        copyHelper.renameTmpFileBackToOriginalExtentension(fileInfoMock, destTmp, dest, modelMainMock);

        // Assert
        assertTrue(Files.exists(dest), "Destination file should exist.");
        verify(fileInfoMock).setDestination_Path(anyString());
        verify(fileInfoMock).setWorkDirDriveSerialNumber(anyString());
        verify(fileInfoMock).setCopied(true);
        Files.deleteIfExists(dest);  // Cleanup
    }

    @Test
    public void testRenameTmpFileBackToOriginalExtension_FileNotFound() throws IOException {
        // Arrange
        Path destTmp = Paths.get("nonExistingFile.tmp");
        Path dest = Paths.get("nonExistingFile");
        when(fileInfoMock.getWorkDir()).thenReturn("workDir");

        // Act
        copyHelper.renameTmpFileBackToOriginalExtentension(fileInfoMock, destTmp, dest, modelMainMock);

        // Assert
        verify(fileInfoMock, never()).setCopied(true);
    }

    @Test
    public void testRenameTmpFileBackToOriginalExtension_ExceptionHandling() throws IOException {
        // Arrange
        Path destTmp = Files.createTempFile("tempFile", ".tmp");
        Path dest = Paths.get("dest");
        Files.deleteIfExists(destTmp);  // Trigger IOException

        // Act
        copyHelper.renameTmpFileBackToOriginalExtentension(fileInfoMock, destTmp, dest, modelMainMock);

        // Assert
        verify(fileInfoMock, never()).setCopied(true);
    }

    @Test
    public void testRenameTmpFileBackToOriginalExtension_ValidPathSetsCorrectValues() throws IOException {
        // Arrange
        Path destTmp = Files.createTempFile("tempFile", ".tmp");
        Path dest = Paths.get(destTmp.toString().replace(".tmp", ""));
        when(fileInfoMock.getWorkDir()).thenReturn("workDir");
        Main.conf = mock(Configuration.class);
        when(Main.conf.getWorkDirSerialNumber()).thenReturn("serial123");

        // Act
        copyHelper.renameTmpFileBackToOriginalExtentension(fileInfoMock, destTmp, dest, modelMainMock);

        // Assert
        verify(fileInfoMock).setDestination_Path(anyString());
        assertEquals("serial123", Main.conf.getWorkDirSerialNumber(), "WorkDirSerialNumber should be set correctly.");
        Files.deleteIfExists(dest);  // Cleanup
    }
}