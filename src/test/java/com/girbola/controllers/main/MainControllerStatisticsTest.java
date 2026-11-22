package com.girbola.controllers.main;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainControllerStatisticsTest {
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
    void initializesDistinctTotalAndCopiedLabelsForAllTables() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            MainController controller = new MainController();
            ModelMain model = mock(ModelMain.class);
            Tables tables = new Tables(model);
            when(model.tables()).thenReturn(tables);
            Field modelField = MainController.class.getDeclaredField("model_main");
            modelField.setAccessible(true);
            modelField.set(controller, model);

            LabelPair sortIt = new LabelPair();
            LabelPair sorted = new LabelPair();
            LabelPair asItIs = new LabelPair();
            controller.sortitController = tableController(sortIt);
            controller.sortedController = tableController(sorted);
            controller.asitisController = tableController(asItIs);

            Method initialize = MainController.class.getDeclaredMethod("initStatistics");
            initialize.setAccessible(true);
            initialize.invoke(controller);

            assertLabels(tables.getSortit_TableStatistic(), sortIt);
            assertLabels(tables.getSorted_TableStatistic(), sorted);
            assertLabels(tables.getAsitis_TableStatistic(), asItIs);
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    private static TableController tableController(LabelPair labels) {
        TableController controller = mock(TableController.class);
        when(controller.getAllFilesTotal_lbl()).thenReturn(labels.total);
        when(controller.getAllFilesCopied_lbl()).thenReturn(labels.copied);
        when(controller.getAllFilesSize_lbl()).thenReturn(new Label());
        return controller;
    }

    private static void assertLabels(TableStatistic statistic, LabelPair labels) {
        statistic.setTotalFiles(25);
        statistic.setTotalFilesCopied(0);
        assertEquals("25", labels.total.getText());
        assertEquals("0", labels.copied.getText());

        statistic.setTotalFilesCopied(7);
        assertEquals("25", labels.total.getText());
        assertEquals("7", labels.copied.getText());
    }

    private static final class LabelPair {
        final Label total = new Label();
        final Label copied = new Label();
    }
}
