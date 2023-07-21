package com.girbola.controllers.main.tables;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestBinding extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = new BorderPane();
		Label num1 = new Label();
		Label num2 = new Label();
		num2.setStyle("-fx-background-color: orange;");
		SimpleIntegerProperty num1_value = new SimpleIntegerProperty();
		SimpleIntegerProperty num2_value = new SimpleIntegerProperty();
		num2.textProperty().bind(Bindings.subtract(num1_value, num2_value).asString());
		
		num1.textProperty().bind(num1_value.asString());
//		num2.textProperty().bind(num2_value.asString());

		TextField tf1 = new TextField();
		tf1.setPromptText("Files");
		tf1.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.matches("\\d*")) {
					int value = Integer.parseInt(newValue);
					num1_value.set(value);
				} else {
					tf1.setText(oldValue);
				}
			}
		});

		TextField tf2 = new TextField();
		tf2.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.matches("\\d*")) {
					int value = Integer.parseInt(newValue);
					num2_value.set(value);
				} else {
					tf2.setText(oldValue);
				}
			}
		});
		//		tf2.textProperty().bind(num2_value.asString());

		VBox nums = new VBox(num1, num2);

		VBox tf_vbox = new VBox(tf1, tf2);

		root.setTop(nums);
		root.setCenter(tf_vbox);

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public static void main(String args[]) {
		Application.launch(args);
	}

}
