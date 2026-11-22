package com.girbola.controllers.datefixer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import com.girbola.fileinfo.FileInfo;
import com.girbola.imagehandling.ThumbnailPreview;
import com.girbola.imagehandling.MFFmpegFrameGrabber;
import com.girbola.imagehandling.ConvertVideo_Byte;
import com.girbola.thumbinfo.ThumbInfo;
import javafx.event.Event;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RenderVisibleNodeTest {
    @TempDir
    Path temporaryDirectory;

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            if (!started.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX did not start");
            }
        } catch (IllegalStateException alreadyStarted) {
            if (!"Toolkit already initialized".equals(alreadyStarted.getMessage())) {
                throw alreadyStarted;
            }
        }
        Platform.setImplicitExit(false);
    }

    @Test
    void repeatedVisibleRendersReusePendingLookupAndEventuallyDisplayImage() throws Exception {
        Path file = temporaryDirectory.resolve("photo.png");
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", file.toFile());
        CountDownLatch lookupStarted = new CountDownLatch(1);
        CountDownLatch releaseLookup = new CountDownLatch(1);
        CountDownLatch imageLoaded = new CountDownLatch(1);
        AtomicInteger lookups = new AtomicInteger();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet results = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            lookups.incrementAndGet();
            lookupStarted.countDown();
            if (!releaseLookup.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Lookup was not released");
            }
            return statement;
        });
        when(statement.executeQuery()).thenReturn(results);
        Fixture fixture = onFx(() -> new Fixture(file, connection, imageLoaded));
        try {
            onFx(() -> {
                fixture.render();
                return null;
            });
            assertTrue(lookupStarted.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                fixture.render();
                fixture.render();
                assertEquals(1, lookups.get());
                return null;
            });
            releaseLookup.countDown();
            assertTrue(imageLoaded.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                fixture.render();
                assertNotNull(fixture.image.getImage());
                assertEquals(1, lookups.get());
                return null;
            });
        } finally {
            releaseLookup.countDown();
            onFx(() -> {
                fixture.renderer.stopTimeLine();
                fixture.stage.close();
                return null;
            });
        }
    }

    @Test
    void repeatedRequestsDoNotDuplicateInFlightOrCompletedWork() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        RenderVisibleNode renderer = onFx(() -> new RenderVisibleNode(new ScrollPane(), null, null));
        ImageView view = onFx(ImageView::new);
        Task<WritableImage> task = new Task<>() {
            @Override
            protected WritableImage call() throws Exception {
                calls.incrementAndGet();
                started.countDown();
                release.await();
                return new WritableImage(1, 1);
            }
        };
        Task<Void> duplicate = new Task<>() {
            @Override
            protected Void call() {
                calls.incrementAndGet();
                return null;
            }
        };
        try {
            onFx(() -> {
                task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> completed.countDown());
                renderer.submitThumbnail(view, task, true);
                renderer.submitThumbnail(view, duplicate, true);
                return null;
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            release.countDown();
            assertTrue(completed.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                assertNotNull(view.getImage());
                renderer.submitThumbnail(view, duplicate, true);
                assertEquals(1, calls.get());
                assertFalse(duplicate.isDone());
                return null;
            });
        } finally {
            release.countDown();
            onFx(() -> {
                renderer.stopTimeLine();
                return null;
            });
        }
    }

    @Test
    void failedOrCancelledWorkCanBeRetriedAfterRestart() throws Exception {
        CountDownLatch failed = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);
        RenderVisibleNode renderer = onFx(() -> new RenderVisibleNode(new ScrollPane(), null, null));
        ImageView view = onFx(ImageView::new);
        Task<Void> failure = new Task<>() {
            @Override
            protected Void call() {
                throw new IllegalStateException("expected test failure");
            }

        };
        Task<Void> blocked = new Task<>() {
            @Override
            protected Void call() throws Exception {
                started.countDown();
                new CountDownLatch(1).await();
                return null;
            }
        };
        Task<WritableImage> retry = new Task<>() {
            @Override
            protected WritableImage call() {
                return new WritableImage(1, 1);
            }
        };
        try {
            onFx(() -> {
                failure.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> failed.countDown());
                renderer.submitThumbnail(view, failure, false);
                return null;
            });
            assertTrue(failed.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                renderer.submitThumbnail(view, blocked, false);
                return null;
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                renderer.stopTimeLine();
                renderer.stopTimeLine();
                assertTrue(blocked.isCancelled());
                renderer.startTimeLine();
                retry.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> completed.countDown());
                renderer.submitThumbnail(view, retry, false);
                return null;
            });
            assertTrue(completed.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                assertNotNull(view.getImage());
                return null;
            });
        } finally {
            onFx(() -> {
                renderer.stopTimeLine();
                return null;
            });
        }
    }

    @Test
    void scrollingReleasesAndReloadsOnlyVisibleThumbnailsInEveryLayout() throws Exception {
        for (String layout : new String[]{"dateFixer", "container", "closerLook"}) {
            ViewportFixture fixture = onFx(() -> new ViewportFixture(layout));
            try {
                onFx(() -> {
                    fixture.check();
                    assertTrue(fixture.visible().containsKey(fixture.first), layout);
                    assertFalse(fixture.visible().containsKey(fixture.last), layout);
                    fixture.first.setImage(new WritableImage(3, 3));
                    fixture.first.setUserData("preview frames");
                    fixture.scroll.setVvalue(1);
                    fixture.scroll.layout();
                    fixture.check();
                    assertNull(fixture.first.getImage(), layout);
                    assertNull(fixture.first.getUserData(), layout);
                    assertTrue(fixture.visible().containsKey(fixture.last), layout);
                    fixture.last.setImage(new WritableImage(3, 3));
                    fixture.scroll.setVvalue(0);
                    fixture.scroll.layout();
                    fixture.check();
                    assertNull(fixture.last.getImage(), layout);
                    assertTrue(fixture.visible().containsKey(fixture.first), layout);
                    return null;
                });
            } finally {
                onFx(() -> {
                    fixture.renderer.stopTimeLine();
                    fixture.stage.close();
                    return null;
                });
            }
        }
    }

    @Test
    void scrollingCancelsPendingWorkAndAllowsItToBeResubmitted() throws Exception {
        ViewportFixture fixture = onFx(() -> new ViewportFixture("dateFixer"));
        CountDownLatch started = new CountDownLatch(1);
        Task<Void> pending = new Task<>() {
            @Override
            protected Void call() throws Exception {
                started.countDown();
                new CountDownLatch(1).await();
                return null;
            }
        };
        try {
            onFx(() -> {
                fixture.check();
                fixture.renderer.submitThumbnail(fixture.first, pending, false);
                return null;
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                fixture.scroll.setVvalue(1);
                fixture.scroll.layout();
                fixture.check();
                assertTrue(pending.isCancelled());
                fixture.scroll.setVvalue(0);
                fixture.scroll.layout();
                fixture.check();
                Task<WritableImage> retry = new Task<>() {
                    @Override
                    protected WritableImage call() {
                        return new WritableImage(2, 2);
                    }
                };
                fixture.renderer.submitThumbnail(fixture.first, retry, true);
                assertFalse(retry.isCancelled());
                return null;
            });
        } finally {
            onFx(() -> {
                fixture.renderer.stopTimeLine();
                fixture.stage.close();
                return null;
            });
        }
    }

    @Test
    void scrollingBackActuallyRendersTheReleasedImageAgain() throws Exception {
        Path file = temporaryDirectory.resolve("photo.png");
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), "png", file.toFile());
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet results = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(results);
        CountDownLatch firstRender = new CountDownLatch(1);
        CountDownLatch secondRender = new CountDownLatch(1);
        AtomicInteger renders = new AtomicInteger();
        ViewportFixture fixture = onFx(() -> new ViewportFixture("dateFixer", connection, file));
        try {
            onFx(() -> {
                fixture.first.imageProperty().addListener((observable, oldValue, image) -> {
                    if (image != null) {
                        if (renders.incrementAndGet() == 1) firstRender.countDown();
                        else secondRender.countDown();
                    }
                });
                fixture.render();
                return null;
            });
            assertTrue(firstRender.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                fixture.scroll.setVvalue(1);
                fixture.scroll.layout();
                fixture.check();
                assertNull(fixture.first.getImage());
                fixture.scroll.setVvalue(0);
                fixture.scroll.layout();
                fixture.render();
                return null;
            });
            assertTrue(secondRender.await(10, TimeUnit.SECONDS));
            assertEquals(2, renders.get());
        } finally {
            onFx(() -> {
                fixture.renderer.stopTimeLine();
                fixture.stage.close();
                return null;
            });
        }
    }

    @Test
    void scrollingReleasesCompletedPreviewResources() throws Exception {
        ViewportFixture fixture = onFx(() -> new ViewportFixture("dateFixer"));
        CountDownLatch completed = new CountDownLatch(1);
        AtomicInteger releases = new AtomicInteger();
        class PreviewTask extends Task<Void> implements ThumbnailPreview {
            @Override
            protected Void call() {
                return null;
            }

            @Test
            void completedVideoCannotReappearAfterItLeavesViewport() throws Exception {
                ViewportFixture fixture = onFx(() -> new ViewportFixture("dateFixer"));
                CountDownLatch finished = new CountDownLatch(1);
                FileInfo video = new FileInfo();
                video.setOrgPath("video.mp4");
                MFFmpegFrameGrabber task = new MFFmpegFrameGrabber(video, fixture.first, 200) {
                    @Override
                    public List<BufferedImage> frameGrabber(Path path) {
                        return List.of(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
                    }

                    @Test
                    void releasedVideoPreviewsStopHandlingHoverWithoutRemovingOtherHandlers() throws Exception {
                        for (boolean cached : new boolean[]{false, true}) {
                            ViewportFixture fixture = onFx(() -> new ViewportFixture("dateFixer"));
                            CountDownLatch completed = new CountDownLatch(1);
                            AtomicInteger otherHandlerCalls = new AtomicInteger();
                            try {
                                onFx(() -> {
                                    fixture.check();
                                    FileInfo video = new FileInfo();
                                    video.setOrgPath("video.mp4");
                                    Task<List<BufferedImage>> task;
                                    javafx.scene.Node previewRoot;
                                    if (cached) {
                                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
                                        ThumbInfo info = new ThumbInfo("video.mp4", 1);
                                        info.getThumbs().add(bytes.toByteArray());
                                        info.getThumbs().add(null);
                                        task = new ConvertVideo_Byte(Path.of("video.mp4"), info, 200, fixture.first);
                                        previewRoot = fixture.first.getParent();
                                    } else {
                                        task = new MFFmpegFrameGrabber(video, fixture.first, 200) {
                                            @Override
                                            public List<BufferedImage> frameGrabber(Path path) {
                                                return List.of(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
                                            }
                                        };
                                        previewRoot = fixture.first.getParent().getParent();
                                    }
                                    previewRoot.setOnMouseExited(event -> otherHandlerCalls.incrementAndGet());
                                    task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> completed.countDown());
                                    fixture.renderer.submitThumbnail(fixture.first, task, true);
                                    return null;
                                });
                                assertTrue(completed.await(10, TimeUnit.SECONDS));
                                onFx(() -> {
                                    assertNotNull(fixture.first.getImage());
                                    javafx.scene.Node previewRoot = cached ? fixture.first.getParent() : fixture.first.getParent().getParent();
                                    fixture.scroll.setVvalue(1);
                                    fixture.scroll.layout();
                                    fixture.check();
                                    Event.fireEvent(previewRoot, new MouseEvent(MouseEvent.MOUSE_EXITED, 0, 0, 0, 0,
                                            MouseButton.NONE, 0, false, false, false, false, false, false, false,
                                            false, false, false, null));
                                    assertNull(fixture.first.getImage());
                                    assertNull(fixture.first.getUserData());
                                    assertEquals(1, otherHandlerCalls.get());
                                    return null;
                                });
                            } finally {
                                onFx(() -> {
                                    fixture.renderer.stopTimeLine();
                                    fixture.stage.close();
                                    return null;
                                });
                            }
                        }
                    }
                };
                try {
                    onFx(() -> {
                        fixture.check();
                        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> finished.countDown());
                        fixture.renderer.submitThumbnail(fixture.first, task, true);
                        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                        while (!task.isDone() && System.nanoTime() < deadline) {
                            Thread.sleep(1);
                        }
                        assertTrue(task.isDone());
                        fixture.scroll.setVvalue(1);
                        fixture.scroll.layout();
                        fixture.check();
                        return null;
                    });
                    assertTrue(finished.await(10, TimeUnit.SECONDS));
                    onFx(() -> {
                        assertNull(fixture.first.getImage());
                        assertNull(fixture.first.getUserData());
                        return null;
                    });
                } finally {
                    onFx(() -> {
                        fixture.renderer.stopTimeLine();
                        fixture.stage.close();
                        return null;
                    });
                }
            }

            @Override
            public void releasePreview() {
                releases.incrementAndGet();
            }
        }
        PreviewTask task = new PreviewTask();
        try {
            onFx(() -> {
                fixture.check();
                task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> completed.countDown());
                fixture.renderer.submitThumbnail(fixture.first, task, true);
                return null;
            });
            assertTrue(completed.await(10, TimeUnit.SECONDS));
            onFx(() -> {
                fixture.scroll.setVvalue(1);
                fixture.scroll.layout();
                fixture.check();
                assertEquals(1, releases.get());
                return null;
            });
        } finally {
            onFx(() -> {
                fixture.renderer.stopTimeLine();
                fixture.stage.close();
                return null;
            });
        }
    }

    private static final class ViewportFixture {
        final ImageView first = new ImageView();
        final ImageView last = new ImageView();
        final Stage stage = new Stage();
        final ScrollPane scroll;
        final RenderVisibleNode renderer;
        final Method check;
        final Method render;
        final java.lang.reflect.Field visible;

        ViewportFixture(String layout) throws Exception {
            this(layout, null, null);
        }

        ViewportFixture(String layout, Connection connection, Path file) throws Exception {
            TilePane tiles = new TilePane();
            tiles.setPrefColumns(1);
            tiles.getChildren().addAll(frame(layout, first, file), frame(layout, last, file));
            if ("dateFixer".equals(layout)) {
                tiles.setId(layout);
                scroll = new ScrollPane(tiles);
            } else {
                VBox root = new VBox();
                root.setId(layout);
                root.getChildren().add("container".equals(layout) ? new TitledPane("Group", tiles) : tiles);
                scroll = new ScrollPane(root);
            }
            scroll.setFitToWidth(true);
            stage.setScene(new Scene(scroll, 200, 150));
            stage.show();
            scroll.applyCss();
            scroll.layout();
            renderer = new RenderVisibleNode(scroll, null, connection);
            check = RenderVisibleNode.class.getDeclaredMethod("checkVisible");
            check.setAccessible(true);
            render = RenderVisibleNode.class.getDeclaredMethod("renderVisibleNodes");
            render.setAccessible(true);
            visible = RenderVisibleNode.class.getDeclaredField("imageViewsMap");
            visible.setAccessible(true);
        }

        private static javafx.scene.Node frame(String layout, ImageView image, Path path) {
            image.setId("imageView");
            FileInfo file = new FileInfo();
            file.setOrgPath(path == null ? "unused.png" : path.toString());
            if ("dateFixer".equals(layout)) {
                HBox container = new HBox(image);
                container.setId("imageViewContainer");
                VBox frame = new VBox(container);
                frame.setId("imageFrame");
                frame.setUserData(file);
                frame.setMinSize(120, 200);
                frame.setPrefSize(120, 200);
                return frame;
            }
            StackPane frame = new StackPane(image);
            frame.setId("imageFrame");
            frame.setUserData(file);
            frame.setMinSize(120, 200);
            frame.setPrefSize(120, 200);
            return frame;
        }

        void check() throws Exception {
            check.invoke(renderer);
        }

        void render() throws Exception {
            render.invoke(renderer);
        }

        @SuppressWarnings("unchecked")
        Map<ImageView, FileInfo> visible() throws IllegalAccessException {
            return (Map<ImageView, FileInfo>) visible.get(renderer);
        }
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }

    private static final class Fixture {
        final ImageView image = new ImageView();
        final Stage stage = new Stage();
        final RenderVisibleNode renderer;
        final Method render;

        Fixture(Path file, Connection connection, CountDownLatch imageLoaded) throws Exception {
            image.setId("imageView");
            image.imageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    imageLoaded.countDown();
                }
            });
            HBox imageContainer = new HBox(image);
            imageContainer.setId("imageViewContainer");
            VBox frame = new VBox(imageContainer);
            frame.setId("imageFrame");
            frame.setMinSize(60, 60);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setOrgPath(file.toString());
            frame.setUserData(fileInfo);
            TilePane tiles = new TilePane(frame);
            tiles.setId("dateFixer");
            ScrollPane scrollPane = new ScrollPane(tiles);
            stage.setScene(new Scene(scrollPane, 300, 300));
            stage.show();
            scrollPane.applyCss();
            scrollPane.layout();
            renderer = new RenderVisibleNode(scrollPane, file.getParent(), connection);
            render = RenderVisibleNode.class.getDeclaredMethod("renderVisibleNodes");
            render.setAccessible(true);
        }

        void render() throws Exception {
            render.invoke(renderer);
        }
    }
}
