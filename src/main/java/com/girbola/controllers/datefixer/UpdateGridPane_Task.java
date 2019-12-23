package com.girbola.controllers.datefixer;

import java.util.concurrent.CountDownLatch;

import com.girbola.Main;
import com.girbola.controllers.loading.LoadingProcess_Task;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UpdateGridPane_Task {

	private static final String ERROR = UpdateGridPane_Task.class.getSimpleName();

	public static void updateGridPaneContent(Model_datefix model_datefix, ObservableList<Node> obs, LoadingProcess_Task loadingProcess_task) {

		CountDownLatch latch = new CountDownLatch(1);

		model_datefix.checkIfDupsNodesExists(latch);

		try {
			latch.await();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Task<ObservableList<Node>> obs_list_task = new Task<ObservableList<Node>>() {
			@Override
			protected ObservableList<Node> call() throws Exception {
				ObservableList<Node> filterlist = model_datefix.filterAllNodesList(obs);
				for (Node node : filterlist) {
					if (node instanceof VBox) {
						if (node.getId().equals("imageFrame")) {
							FileInfo fileInfo = (FileInfo) node.getUserData();
						}
					}
				}
				return filterlist;
			}
		};

		obs_list_task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("Cancelled");
				loadingProcess_task.closeStage();
			}
		});
		obs_list_task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Messages.sprintf("obs_list_task Failed");
				Messages.errorSmth(ERROR, obs_list_task.getException().toString(), null, Misc.getLineNumber(), true);
				loadingProcess_task.closeStage();
			}
		});
		obs_list_task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						model_datefix.getSelectionModel().clearAll();
					}
				});
				ObservableList<Node> filteredList = null;
				try {
					filteredList = obs_list_task.get();
				} catch (Exception e) {
					Messages.errorSmth(ERROR, "", e, Misc.getLineNumber(), true);
				}
				if (filteredList.isEmpty()) {
					Messages.sprintf("FilteredList were empty!!");
					return;
				} else {
					addToGridPane(model_datefix, filteredList, loadingProcess_task, Main.scene_Switcher.getWindow());
					Messages.sprintf("filterList were not empty");
				}
				loadingProcess_task.closeStage();
			}
		});
		Messages.sprintf("DEBUGGING LINE");
		loadingProcess_task.setTask(obs_list_task);
		Thread obs_list_th = new Thread(obs_list_task, "obs_list_th");
		obs_list_th.start();

	}

	public static void addToGridPane(Model_datefix model_datefix, ObservableList<Node> obs, LoadingProcess_Task lpt, Stage stage) {

		Task<Integer> addToGridPane_task = new AddToGridPane2(model_datefix, obs, lpt);
		lpt.setTask(addToGridPane_task);
		Thread addToGridPane_th = new Thread(addToGridPane_task, "addToGridPane_th");
		addToGridPane_th.run();
	}

}
