package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.messages.Messages;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

public class LoadingProcessLoader {

    private static LoadingProcessTask loadingProcessTask;

    public static void runUpdateTask(Model_datefix model_datefix, LoadingProcessTask loadingProcessTask) {
        if(loadingProcessTask == null) {
            loadingProcessTask = new LoadingProcessTask(Main.scene_Switcher.getWindow());
        }
        LoadingProcessTask finalLoadingProcessTask = loadingProcessTask;

        Task<Integer> addContentToDateFixContainer = new AddContentToDateFixContainer(model_datefix,
                model_datefix.getAllNodes(), loadingProcessTask, model_datefix.getTilePane());

        loadingProcessTask.setTask(addContentToDateFixContainer);


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

    void addToGridPane(Model_datefix model_datefix, ObservableList<Node> obs, LoadingProcessTask lpt) {

        Task<Integer> addToGridPane_task = new AddContentToDateFixContainer(model_datefix, obs, lpt, model_datefix.getTilePane());
        lpt.setTask(addToGridPane_task);
        Thread addToGridPane_th = new Thread(addToGridPane_task, "addToGridPane_th");
        addToGridPane_th.run();
    }

}
