
package com.girbola.controllers.loading;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;


class ModelLoading {

	private Task<?> task;
	private Label messages_lbl;
	private TextArea messages_txa;
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
	public void setMessages_txa(TextArea messages_txa) { this.messages_txa = messages_txa; }
	public TextArea getMessages_txa() { return messages_txa; }
	public Label getMessages_lbl() {
		return messages_lbl;
	}
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
