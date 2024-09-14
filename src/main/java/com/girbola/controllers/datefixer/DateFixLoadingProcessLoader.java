package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicInteger;

public class DateFixLoadingProcessLoader {

    private static AtomicInteger counter = new AtomicInteger(0);

    public static void reNumberTheFrames(Model_datefix model_datefix, LoadingProcessTask loadingProcessTask) {

        Messages.sprintf("Counter runUpdateTask: " + counter.incrementAndGet());
        if(loadingProcessTask == null) {
            loadingProcessTask = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        }

        LoadingProcessTask finalLoadingProcessTask = loadingProcessTask;
        // Task<Integer> addContentToDateFixContainer = new AddContentToDateFixContainer(model_datefix, model_datefix.getAllNodes(), loadingProcessTask, model_datefix.getTilePane());
        //model_datefix.getTilePane().getChildren().clear();

        Task<Integer> addContentToDateFixContainer = new AddContentToDateFixContainer(model_datefix, model_datefix.getAllNodes(), finalLoadingProcessTask, model_datefix.getTilePane());

        finalLoadingProcessTask.setTask(addContentToDateFixContainer);

        addContentToDateFixContainer.setOnSucceeded(succeed -> {
            Messages.sprintf("addContentToDateFixContainer succeeded!");
            finalLoadingProcessTask.closeStage();
        });

        addContentToDateFixContainer.setOnCancelled(succeed -> {
            Messages.sprintf("addContentToDateFixContainer Cancelled!");
            finalLoadingProcessTask.closeStage();
        });

        addContentToDateFixContainer.setOnFailed(succeed -> {
            Messages.sprintf("addContentToDateFixContainer Failed!");
            finalLoadingProcessTask.closeStage();
        });

        Thread thread = new Thread(addContentToDateFixContainer, "updateGridPane_Task_th");
        thread.setDaemon(true);
        thread.run();
    }

}
