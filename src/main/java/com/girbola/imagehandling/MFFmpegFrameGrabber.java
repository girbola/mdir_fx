package com.girbola.imagehandling;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.configuration.UIContants;
import common.utils.FileUtils;
import common.utils.ImageUtils;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacpp.Loader;

public class MFFmpegFrameGrabber extends Task<List<BufferedImage>> implements ThumbnailPreview {
    private static final int HEIC_CACHE_LIMIT = 128;
    private static final Map<Path, CachedHeic> HEIC_CACHE = new LinkedHashMap<>(16, 0.75f, true);

    private record CachedHeic(long size, java.nio.file.attribute.FileTime modified, Object fileKey,
                              BufferedImage image) {
        boolean matches(BasicFileAttributes attributes) {
            return size == attributes.size() && modified.equals(attributes.lastModifiedTime())
                    && java.util.Objects.equals(fileKey, attributes.fileKey());
        }
    }

    private final FileInfo fileInfo;
    private final ImageView imageView;
    private final int imageWidth;
    private Timeline timeLine;
    private Node previewRoot;
    private EventHandler<MouseEvent> mouseEntered;
    private EventHandler<MouseEvent> mouseExited;
    private boolean previewReleased;
    private Process heicProcess;

    public MFFmpegFrameGrabber(FileInfo fileInfo, ImageView imageView, double imageWidth) {
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.imageWidth = (int) imageWidth;
    }

    public static List<Long> grabListOfTimeLineMicros(long totalDurationMicros) {
        List<Long> list = new ArrayList<>();
        double durationSec = totalDurationMicros / 1_000_000.0;

        if (durationSec <= 5) {
            list.add(0L);
        } else if (durationSec <= 10) {
            long step = totalDurationMicros / 2;
            list.add(0L);
            list.add(step);
        } else {
            long startPos = totalDurationMicros / 10; // 10% offset to avoid black screens
            long step = (totalDurationMicros - startPos) / 5;
            long current = startPos;
            for (int i = 0; i < 5; i++) {
                list.add(current);
                current += step;
            }
        }
        return list;
    }

    public List<BufferedImage> frameGrabber(Path path) {
        if (FileUtils.supportedHeic(path)) {
            try {
                return decodeHeic(path);
            } catch (IOException e) {
                Messages.sprintfError("Cannot decode HEIC image " + path + ": " + e.getMessage());
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Messages.sprintfError("HEIC decoding interrupted for " + path);
                return null;
            }
        }
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path.toFile())) {
            try {
                frameGrabber.start();
            } catch (FFmpegFrameGrabber.Exception e) {
                Messages.sprintfError("Cannot start frameGrabber: " + e.getMessage());
                return null;
            }

            long totalLengthMicros = frameGrabber.getLengthInTime();
            List<Long> timestampsMicros = grabListOfTimeLineMicros(totalLengthMicros);

            List<BufferedImage> list = new ArrayList<>();

            // Re-use a single converter instance to prevent off-heap native memory leaks
            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                for (Long timeStampMicros : timestampsMicros) {
                    if (isCancelled()) {
                        break;
                    }

                    frameGrabber.setTimestamp(timeStampMicros);
                    Frame frame = frameGrabber.grabImage();

                    if (frame != null) {
                        BufferedImage bufferedImage = converter.getBufferedImage(frame);
                        if (bufferedImage != null) {
                            BufferedImage scaledImage = ImageUtils.scaleImageWithAspectRatio(bufferedImage);
                            Messages.sprintf("Grabbed frame for " + path + " at " + timeStampMicros + " µs");
                            list.add(scaledImage);
                        }
                    } else {
                        Messages.sprintfError("Cannot grab frame for " + path + " at " + timeStampMicros + " µs");
                    }
                }
            }

            frameGrabber.stop();
            frameGrabber.release();

            return list;
        } catch (IOException e) {
            Messages.sprintf("Problem grabbing video thumbnails: " + e.getMessage());
            return null;
        }
    }

    private List<BufferedImage> decodeHeic(Path path) throws IOException, InterruptedException {
        Path cacheKey = path.toAbsolutePath().normalize();
        BasicFileAttributes attributes = Files.readAttributes(cacheKey, BasicFileAttributes.class);
        synchronized (HEIC_CACHE) {
            CachedHeic cached = HEIC_CACHE.get(cacheKey);
            if (cached != null && cached.matches(attributes)) {
                return List.of(copyImage(cached.image()));
            }
            HEIC_CACHE.remove(cacheKey);
        }
        String executable = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        Process process;
        synchronized (this) {
            if (isCancelled()) {
                return List.of();
            }
            process = new ProcessBuilder(executable, "-v", "error", "-nostdin", "-i", path.toString(),
                    "-frames:v", "1", "-f", "image2pipe", "-vcodec", "png", "-compression_level", "0", "pipe:1")
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            heicProcess = process;
        }
        try {
            BufferedImage image;
            try (InputStream output = process.getInputStream()) {
                image = ImageIO.read(output);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0 || image == null) {
                throw new IOException("FFmpeg exited with code " + exitCode + " without a valid image");
            }
            BufferedImage scaled = scaleHeicImage(image);
            BasicFileAttributes afterDecode = Files.readAttributes(cacheKey, BasicFileAttributes.class);
            CachedHeic entry = new CachedHeic(attributes.size(), attributes.lastModifiedTime(),
                    attributes.fileKey(), scaled);
            if (!isCancelled() && entry.matches(afterDecode)) {
                synchronized (HEIC_CACHE) {
                    HEIC_CACHE.put(cacheKey, entry);
                    if (HEIC_CACHE.size() > HEIC_CACHE_LIMIT) {
                        HEIC_CACHE.remove(HEIC_CACHE.keySet().iterator().next());
                    }
                }
            }
            return List.of(copyImage(scaled));
        } finally {
            process.destroyForcibly();
            synchronized (this) {
                if (heicProcess == process) {
                    heicProcess = null;
                }
            }
        }
    }

    private static BufferedImage copyImage(BufferedImage image) {
        return new BufferedImage(image.getColorModel(), image.copyData(null),
                image.isAlphaPremultiplied(), null);
    }

    private static BufferedImage scaleHeicImage(BufferedImage image) {
        double scale = Math.min(UIContants.THUMBNAIL_MAX_WIDTH / image.getWidth(),
                UIContants.THUMBNAIL_MAX_HEIGHT / image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }

    @Override
    protected List<BufferedImage> call() throws Exception {
        if (fileInfo == null || fileInfo.getOrgPath() == null) {
            return null;
        }
        return frameGrabber(Paths.get(fileInfo.getOrgPath()));
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        if (previewReleased) {
            return;
        }
        List<BufferedImage> list = getValue();

        if (list == null || list.isEmpty()) {
            Messages.sprintfError("VideoThumbMaker returned null/empty for: " + fileInfo.getOrgPath());
            return;
        }

        VideoPreview videoPreview = new VideoPreview(list, imageView);
        imageView.setImage(videoPreview.getImage(0));
        imageView.setUserData(list);
        if (FileUtils.supportedHeic(Paths.get(fileInfo.getOrgPath()))) {
            return;
        }

        // Safely navigate view hierarchy
        Node parentNode = imageView.getParent();
        HBox pane = null;
        VBox rootPane = null;

        if (parentNode instanceof StackPane) {
            Node grandParent = parentNode.getParent();
            if (grandParent instanceof HBox) {
                pane = (HBox) grandParent;
                if (pane.getParent() instanceof VBox) {
                    rootPane = (VBox) pane.getParent();
                }
            }
        } else if (parentNode instanceof HBox) {
            pane = (HBox) parentNode;
            if (pane.getParent() instanceof VBox) {
                rootPane = (VBox) pane.getParent();
            }
        }

        if (rootPane == null) {
            Messages.sprintfError("Could not resolve VBox root container for ImageView");
            return;
        }

        // Setup clean hover animation handlers
        previewRoot = rootPane;
        mouseEntered = event -> {
            if (timeLine != null) {
                timeLine.stop();
            }
            timeLine = new Timeline(new KeyFrame(Duration.seconds(0.8), e -> {
                Image image = SwingFXUtils.toFXImage(videoPreview.showNextBufferedImage(), null);
                imageView.setImage(image);
            }));
            timeLine.setCycleCount(Timeline.INDEFINITE);
            timeLine.play();
        };

        mouseExited = event -> {
            if (timeLine != null) {
                timeLine.stop();
            }
            // Reset to default static thumbnail on exit
            imageView.setImage(videoPreview.getImage(0));
        };
        previewRoot.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEntered);
        previewRoot.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExited);
    }

    @Override
    public void releasePreview() {
        previewReleased = true;
        if (timeLine != null) {
            timeLine.stop();
            timeLine = null;
        }
        if (previewRoot != null) {
            previewRoot.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEntered);
            previewRoot.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExited);
            previewRoot = null;
            mouseEntered = null;
            mouseExited = null;
        }
    }

    @Override
    protected void cancelled() {
        synchronized (this) {
            if (heicProcess != null) {
                heicProcess.destroyForcibly();
            }
        }
        if (timeLine != null) {
            timeLine.stop();
        }
        super.cancelled();
    }

    @Override
    protected void failed() {
        if (timeLine != null) {
            timeLine.stop();
        }
        super.failed();
    }
}
