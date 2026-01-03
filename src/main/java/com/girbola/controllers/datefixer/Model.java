package com.girbola.controllers.datefixer;

import javafx.scene.Scene;
import javafx.stage.Stage;

public interface Model {

	public void setInheritedScene(Scene scene);
	public void setInheritedStage(Stage stage);
	public Scene getInheritedScene();
	public Stage getInheritedStage();
	
}
