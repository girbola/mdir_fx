package com.girbola.controllers.main.tables;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.TableStatistic;
import com.girbola.controllers.main.Tables;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculateTableViewsStatisticTest {
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
    }

    @Test
    void sumsFoldersAndResetsEmptyTables() throws Exception {
        onFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.sortIt.getItems().addAll(folder(4, 1, 120), folder(6, 2, 300), folder(0, 0, 0));
            fixture.sorted.getItems().add(folder(3, 0, 80));
            fixture.asItIs.getItems().add(folder(0, 0, 0));
            fixture.asItIsStatistic.setTotalFiles(99);
            fixture.asItIsStatistic.setTotalFilesCopied(99);
            fixture.asItIsStatistic.setTotalFilesSize(99);

            TableUtils.calculateTableViewsStatistic(fixture.tables);

            assertEquals(2, fixture.sortIt.getItems().size());
            assertTotals(fixture.sortItStatistic, 10, 3, 420);
            assertTotals(fixture.sortedStatistic, 3, 0, 80);
            assertTotals(fixture.asItIsStatistic, 0, 0, 0);
            assertTrue(fixture.asItIs.getItems().isEmpty());

            fixture.sortIt.getItems().clear();
            TableUtils.calculateTableViewsStatistic(fixture.tables);
            assertTotals(fixture.sortItStatistic, 0, 0, 0);
            return null;
        });
    }

    @Test
    void refreshFromBackgroundUpdatesOnFxThreadWithoutCreatingExecutors() throws Exception {
        AtomicBoolean updatesOnFxThread = new AtomicBoolean(true);
        Fixture fixture = onFxThread(() -> {
            Fixture created = new Fixture();
            created.sortIt.getItems().add(folder(5, 2, 900));
            created.sortItStatistic.totalFiles_property().addListener((observable, oldValue, newValue) ->
                    updatesOnFxThread.set(updatesOnFxThread.get() && Platform.isFxApplicationThread()));
            return created;
        });
        int executorCount = ConcurrencyUtils.getExecCounter();

        TableUtils.refreshAllTableContent(fixture.tables);

        onFxThread(() -> {
            assertTotals(fixture.sortItStatistic, 5, 2, 900);
            assertEquals("5", fixture.totalFilesLabel.getText());
            assertEquals("2", fixture.copiedLabel.getText());
            assertTrue(updatesOnFxThread.get());
            assertEquals(executorCount, ConcurrencyUtils.getExecCounter());
            return null;
        });
    }

    private static FolderInfo folder(int files, int copied, long size) {
        FolderInfo folder = new FolderInfo();
        folder.setFileInfoList(new ArrayList<>(files == 0 ? List.of() : List.of(new FileInfo())));
        folder.setFolderFiles(files);
        folder.setCopied(copied);
        folder.setFolderSize(size);
        return folder;
    }

    private static void assertTotals(TableStatistic statistic, int files, int copied, long size) {
        assertEquals(files, statistic.totalFiles_property().get());
        assertEquals(copied, statistic.totalFilesCopied_property().get());
        assertEquals(size, statistic.totalFilesSize_property().get());
    }

    private static <T> T onFxThread(java.util.concurrent.Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }

    private static final class Fixture {
        final Tables tables = mock(Tables.class);
        final TableView<FolderInfo> sortIt = table("sortIt");
        final TableView<FolderInfo> sorted = table("sorted");
        final TableView<FolderInfo> asItIs = table("asItIs");
        final Label totalFilesLabel = new Label();
        final Label copiedLabel = new Label();
        final TableStatistic sortItStatistic = new TableStatistic(copiedLabel, new Label(), totalFilesLabel);
        final TableStatistic sortedStatistic = new TableStatistic(new Label(), new Label(), new Label());
        final TableStatistic asItIsStatistic = new TableStatistic(new Label(), new Label(), new Label());

        Fixture() {
            when(tables.getSortIt_table()).thenReturn(sortIt);
            when(tables.getSorted_table()).thenReturn(sorted);
            when(tables.getAsItIs_table()).thenReturn(asItIs);
            when(tables.getTableStatisticByType("sortIt")).thenReturn(sortItStatistic);
            when(tables.getTableStatisticByType("sorted")).thenReturn(sortedStatistic);
            when(tables.getTableStatisticByType("asItIs")).thenReturn(asItIsStatistic);
            new Scene(new VBox(sortIt, sorted, asItIs, totalFilesLabel, copiedLabel));
        }

        private static TableView<FolderInfo> table(String id) {
            TableView<FolderInfo> table = new TableView<>();
            table.setId(id);
            if (!id.equals("asItIs")) {
                table.getColumns().add(new TableColumn<>());
            }
            return table;
        }
    }
}
