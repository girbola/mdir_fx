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
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CopyHelperTest {

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