package com.girbola.filelisting;

import common.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CheckMediaExistenceInFolderTest {
    final private Path srcValid = Paths.get("src", "test", "resources", "in");
    final private Path srcNotValid = Paths.get("src", "test", "java");

    /**
     * Test to verify that the method returns true when a valid media file is found in the folder.
     */
    @Test
    void testGetAllMediaFiles_FindsValidFile() throws IOException {

        boolean hasMedia = FileUtils.getHasMedia(srcValid.toFile().getAbsoluteFile());
        assertTrue(hasMedia);

    }

    @Test
    void testGetAllMediaFiles_FindsNotValidFile() throws IOException {

        boolean hasMedia = FileUtils.getHasMedia(srcNotValid.toFile().getAbsoluteFile());
        assertFalse(hasMedia);

    }
}