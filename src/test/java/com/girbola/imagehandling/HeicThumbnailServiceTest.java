package com.girbola.imagehandling;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static common.utils.FileUtils.findExecutableFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class HeicThumbnailServiceTest {

    /**
     * Tests the case where createThumbnail succeeds with a valid ffmpeg path.
     */
    /**
     * Tests the case where createThumbnail succeeds with a valid ffmpeg path.
     */
    @Test
    void createThumbnail_successfulExecution() throws Exception {
        HeicThumbnailService spyService = Mockito.spy(new HeicThumbnailService());

        doReturn(List.of("/usr/local/bin/ffmpeg")).when(spyService).findAllFfmpegPaths();
        doReturn(new HeicThumbnailService.FfmpegInfo() {{
            path = "/usr/bin/ffmpeg";
            version = "ffmpeg version 4.3";
            supportsHevc = true;
            sha256 = "testhash";
            score = 10;
        }}).when(spyService).inspect(anyString());
        doNothing().when(spyService).runFfmpeg(anyString(), anyString(), anyString());

        spyService.createThumbnail("input.heic", "output.jpg");

        verify(spyService, times(1)).runFfmpeg("/usr/bin/ffmpeg", "input.heic", "output.jpg");
    }

    /**
     * Tests the case where no usable ffmpeg binary is found.
     */
    @Test
    void createThumbnail_noUsableFfmpegFound() throws Exception {
        HeicThumbnailService spyService = Mockito.spy(new HeicThumbnailService());

        doReturn(List.of()).when(spyService).findAllFfmpegPaths();

        Exception exception = assertThrows(RuntimeException.class, () ->
                spyService.createThumbnail("input.heic", "output.jpg")
        );
        assertEquals("No usable ffmpeg found", exception.getMessage());
    }

    /**
     * Tests the case where all ffmpeg candidates fail during execution.
     */
    @Test
    void createThumbnail_allCandidatesFail() throws Exception {
        HeicThumbnailService spyService = Mockito.spy(new HeicThumbnailService());

        doReturn(List.of("/usr/bin/ffmpeg1", "/usr/bin/ffmpeg2", "/usr/local/bin/ffmpeg")).when(spyService).findAllFfmpegPaths();
        doReturn(new HeicThumbnailService.FfmpegInfo() {{
            path = "/usr/bin/ffmpeg1";
            version = "ffmpeg version 4.3";
            sha256 = "testhash";
            score = 10;
        }}).when(spyService).inspect(anyString());
        doThrow(new RuntimeException("ffmpeg failed")).when(spyService).runFfmpeg(anyString(), anyString(), anyString());

        Exception exception = assertThrows(RuntimeException.class, () ->

                spyService.createThumbnail("input.heic", "output.jpg")
        );
        assertEquals("All ffmpeg candidates failed", exception.getMessage());
    }

    /**
     * Tests the case where an untrusted ffmpeg binary is skipped.
     */
    @Test
    void createThumbnail_skipsUntrustedBinary() throws Exception {
        HeicThumbnailService spyService = Mockito.spy(new HeicThumbnailService());

        doReturn(List.of("/untrusted/path/ffmpeg")).when(spyService).findAllFfmpegPaths();
        doReturn(new HeicThumbnailService.FfmpegInfo() {{
            path = "/untrusted/path/ffmpeg";
            version = "ffmpeg version 4.3";
            supportsHevc = true;
            sha256 = "untrustedhash";
            score = 0;
        }}).when(spyService).inspect(anyString());
        
        doNothing().when(spyService).validateHash(any());

        Exception exception = assertThrows(RuntimeException.class, () ->
                spyService.createThumbnail("input.heic", "output.jpg")
        );
        assertEquals("No usable ffmpeg found", exception.getMessage());
    }

}