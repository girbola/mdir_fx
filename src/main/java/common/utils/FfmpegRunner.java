package common.utils;

import javafx.concurrent.Task;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

public final class FfmpegRunner {

    private final Process process;
    private final InputStream videoStream;
    private final BufferedReader progressReader;

    public FfmpegRunner(String ffmpegPath, String inputPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-progress", "pipe:2",
                "-nostats",
                "-frames:v", "1",
                "-f", "rawvideo",
                "-pix_fmt", "rgb24",
                "pipe:1"
        );

        process = pb.start();
        videoStream = process.getInputStream();
        progressReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));
    }

    /**
     * Reads exactly one raw RGB frame.
     */
    public byte[] readRawFrame(int width, int height) throws IOException {
        int frameSize = width * height * 3;
        byte[] data = new byte[frameSize];

        int read = 0;
        while (read < frameSize) {
            int r = videoStream.read(data, read, frameSize - read);
            if (r < 0) {
                throw new EOFException("Unexpected end of rawvideo stream");
            }
            read += r;
        }
        return data;
    }

    /**
     * Reads progress updates until end or cancel.
     */
    public void readProgress(DoubleConsumer progressCallback,
                             long totalDurationMs,
                             CancelFlag cancel) throws IOException {

        String line;
        while ((line = progressReader.readLine()) != null) {

            if (cancel.cancelled) {
                process.destroyForcibly();
                return;
            }

            if (line.startsWith("out_time_ms=")) {
                long outMs = Long.parseLong(line.substring(12));
                double p = Math.min(1.0,
                        (double) outMs / totalDurationMs);
                progressCallback.accept(p);
            }

            if ("progress=end".equals(line)) {
                progressCallback.accept(1.0);
                return;
            }
        }
    }

    public void waitFor(long timeoutMs) throws Exception {
        if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new RuntimeException("FFmpeg timeout");
        }
    }

    public static final class CancelFlag {
        public volatile boolean cancelled = false;
    }
}
