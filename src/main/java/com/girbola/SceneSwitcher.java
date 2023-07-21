package com.girbola;

import com.girbola.messages.Messages;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

	private Stage window;
	private Stage window_loadingprogress;

	private Scene scene_main;
	private Scene scene_dateFixer;
	private Scene scene_operate;
	private Scene scene_loading;

	private Scene currentScene;

	public Scene getScene_main() {
		Messages.sprintf("Getting MAIN SCENE...");
		return scene_main;
	}

	public Scene getScene_dateFixer() {
		Messages.sprintf("Getting DATEFIXER SCENE...");
		return scene_dateFixer;
	}

	public Scene getScene_operate() {
		Messages.sprintf("Getting OPERATE SCENE...");
		return scene_operate;
	}

	public void setScene_main(Scene scene_main) {
		this.scene_main = scene_main;
	}

	public void setScene_dateFixer(Scene scene_dateFixer) {
		this.scene_dateFixer = scene_dateFixer;
	}

	public void setScene_operate(Scene scene_operate) {
		this.scene_operate = scene_operate;
	}

	public Scene getScene_loading() {
		Messages.sprintf("Getting LOADING SCENE...");
		return scene_loading;
	}

	public void setScene_loading(Scene scene_loading) {
		this.scene_loading = scene_loading;
	}

	public Stage getWindow() {
		Messages.sprintf("Getting MAIN STAGE....");
		return window;
	}

	public void setWindow(Stage window) {
		this.window = window;
	}

	public Stage getWindow_loadingprogress() {
		Messages.sprintf("Getting LOADINGINGPROCESS STAGE...");
		return window_loadingprogress;
	}

	public void setWindow_loadingprogress(Stage window_loadingprogress) {
		this.window_loadingprogress = window_loadingprogress;
	}

	public Scene getCurrentScene() {
		return currentScene;
	}

	public void setCurrentScene(Scene currentScene) {
		this.currentScene = currentScene;
	}

}
