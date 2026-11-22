package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RenderVisibleNodeDragTest {
    @TempDir
    Path temporaryDirectory;
    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            assertTrue(started.await(10, TimeUnit.SECONDS));
        } catch (IllegalStateException alreadyStarted) {
            if (!"Toolkit already initialized".equals(alreadyStarted.getMessage())) {
                throw alreadyStarted;
            }
        }
        Platform.setImplicitExit(false);
    }

    @Test
    void repeatedDragsReleaseImagesAndRemoveCancelledQueuedTasks() throws Exception {
        Fixture fixture = onFx(Fixture::new);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Task<Void> blocker = new Task<>() {
            @Override
            protected Void call() throws InterruptedException {
                started.countDown();
                release.await();
                return null;
            }
        };
        try {
            onFx(() -> null);
            onFx(() -> {
                fixture.renderer.submitThumbnail(new ImageView(), blocker, false);
                return null;
            });
            assertTrue(started.await(10, TimeUnit.SECONDS));
            for (int i = 0; i < 20; i++) {
                ImageView current = i % 2 == 0 ? fixture.first : fixture.last;
                double next = i % 2 == 0 ? 1 : 0;
                Task<Void> queued = new Task<>() {
                    @Override
                    protected Void call() {
                        return null;
                    }
                };
                onFx(() -> {
                    fixture.renderer.submitThumbnail(current, queued, false);
                    assertEquals(1, fixture.executor().getQueue().size());
                    current.setImage(new WritableImage(3, 3));
                    current.setUserData("preview");
                    fixture.scroll.setVvalue(next);
                    return null;
                });
                onFx(() -> {
                    assertNull(current.getImage());
                    assertNull(current.getUserData());
                    assertTrue(queued.isCancelled());
                    assertTrue(fixture.executor().getQueue().isEmpty());
                    assertFalse(fixture.visible().containsKey(current));
                    assertEquals(1, fixture.inFlight().size());
                    return null;
                });
            }
        } finally {
            release.countDown();
            onFx(() -> {
                fixture.renderer.stopTimeLine();
                fixture.stage.close();
                return null;
            });
        }
    }

    @Test
    void viewportCleanupPrecedesThe120MillisecondRenderDelay() throws Exception {
        Fixture fixture = onFx(Fixture::new);
        try {
            onFx(() -> null);
            onFx(() -> {
                assertEquals(120, fixture.timeline().getKeyFrames().getFirst().getTime().toMillis());
                fixture.first.setImage(new WritableImage(2, 2));
                fixture.scroll.setVvalue(1);
                return null;
            });
            onFx(() -> {
                assertNull(fixture.first.getImage());
                assertTrue(fixture.timeline().getCurrentTime().toMillis() < 120);
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
    void renderingWaitsUntil120MillisecondsAfterTheLastScroll() throws Exception {
        Path file = Files.createFile(temporaryDirectory.resolve("photo.png"));
        CountDownLatch lookup = new CountDownLatch(1);
        AtomicLong lookupTime = new AtomicLong();
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet results = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            lookupTime.set(System.nanoTime());
            lookup.countDown();
            return statement;
        });
        when(statement.executeQuery()).thenReturn(results);
        Fixture fixture = onFx(() -> new Fixture(connection, file));
        try {
            onFx(() -> {
                fixture.scroll.setVvalue(1);
                return null;
            });
            Thread.sleep(50);
            long lastScroll = onFx(() -> {
                long now = System.nanoTime();
                fixture.scroll.setVvalue(0);
                return now;
            });
            assertFalse(lookup.await(80, TimeUnit.MILLISECONDS));
            assertTrue(lookup.await(5, TimeUnit.SECONDS));
            assertTrue(TimeUnit.NANOSECONDS.toMillis(lookupTime.get() - lastScroll) >= 120);
        } finally {
            onFx(() -> {
                fixture.renderer.stopTimeLine();
                fixture.stage.close();
                return null;
            });
        }
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private static final class Fixture {
        final ImageView first = new ImageView();
        final ImageView last = new ImageView();
        final ScrollPane scroll;
        final Stage stage = new Stage();
        final RenderVisibleNode renderer;

        Fixture() {
            this(null, null);
        }

        Fixture(Connection connection, Path file) {
            TilePane tiles = new TilePane(frame(first, file), frame(last, file));
            tiles.setId("dateFixer");
            tiles.setPrefColumns(1);
            scroll = new ScrollPane(tiles);
            scroll.setFitToWidth(true);
            stage.setScene(new Scene(scroll, 200, 150));
            stage.show();
            scroll.applyCss();
            scroll.layout();
            renderer = new RenderVisibleNode(scroll, null, connection);
        }

        private static VBox frame(ImageView image, Path path) {
            image.setId("imageView");
            HBox container = new HBox(image);
            container.setId("imageViewContainer");
            VBox frame = new VBox(container);
            frame.setId("imageFrame");
            frame.setMinSize(120, 200);
            frame.setPrefSize(120, 200);
            FileInfo file = new FileInfo();
            file.setOrgPath(path == null ? "unused.png" : path.toString());
            frame.setUserData(file);
            return frame;
        }

        private Object field(String name) throws ReflectiveOperationException {
            Field field = RenderVisibleNode.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(renderer);
        }

        ThreadPoolExecutor executor() throws ReflectiveOperationException {
            return (ThreadPoolExecutor) field("exec_single");
        }

        Timeline timeline() throws ReflectiveOperationException {
            return (Timeline) field("timeline");
        }

        Map<?, ?> visible() throws ReflectiveOperationException {
            return (Map<?, ?>) field("imageViewsMap");
        }

        Map<?, ?> inFlight() throws ReflectiveOperationException {
            return (Map<?, ?>) field("inFlight");
        }
    }
}
