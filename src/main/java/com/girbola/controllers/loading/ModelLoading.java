
package com.girbola.controllers.loading;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;


class ModelLoading {

	private Task<?> task;
	private Label messages_lbl;
	private ProgressBar progressBar;
	private Stage stage_loading;

	public ModelLoading() {
		// Initialize components in constructor
		this.messages_lbl = new Label();
		this.progressBar = new ProgressBar();
	}


	@Override
	public String toString() {
		return ("Model_loading{ task= " + task);
	}

	void setTask(Task<?> task) {
		this.task = task;
	}

	Task<?> getTask() {
		return this.task;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void setMessages_lbl(Label messages_lbl) {
		this.messages_lbl = messages_lbl;
	}

	/**
	 * @return the messages_lbl
	 */
	public Label getMessages_lbl() {
		return messages_lbl;
	}

	/**
	 * @return the progressBar
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Stage getStage_loading() {
		return stage_loading;
	}

	public void setStage_loading(Stage stage_loading) {
		this.stage_loading = stage_loading;
	}
}
