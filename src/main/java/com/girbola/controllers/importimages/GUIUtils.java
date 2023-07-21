package com.girbola.controllers.importimages;

import com.girbola.configuration.GUIPrefs;
import com.girbola.fileinfo.FileInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;

import java.util.Collections;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.rotate.Rotate.rotate;

public class GUIUtils {
	private Model_importImages model_importImages;
	private double width = (GUIPrefs.thumb_x_MAX + 2);
	private double height = (GUIPrefs.thumb_x_MAX + 2);

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public GUIUtils(Model_importImages aModel_importImages) {
		this.model_importImages = aModel_importImages;
	}

	public Group createGroup(FileInfo fi, int i) {
		Group group = new Group();
		group.setId("group");

		// ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(3),
		// group);
		//
		// group.setOnMouseEntered((MouseEvent event) -> {
		//// group.toFront();
		//// scaleTransition.setFromX(0);
		//// scaleTransition.setFromY(0);
		//// scaleTransition.setFromZ(0);
		//
		// scaleTransition.setToX(2);
		// scaleTransition.setToY(2);
		//// scaleTransition.setToZ(2);
		// scaleTransition.setCycleCount(1);
		// scaleTransition.setAutoReverse(true);
		// scaleTransition.play();
		// });
		// group.setOnMouseExited((MouseEvent event) -> {
		//// group.toBack();
		//// scaleTransition.setFromX(2);
		//// scaleTransition.setFromY(2);
		//// scaleTransition.setFromZ(2);
		// scaleTransition.setToX(0);
		// scaleTransition.setToY(0);
		//// scaleTransition.setToZ(0);
		//
		// scaleTransition.setCycleCount(1);
		// scaleTransition.setAutoReverse(true);
		// scaleTransition.play();
		// });
		return group;
	}

	public TitledPane createTitledPane(int index) {
		TitledPane titledPane = new TitledPane();
		titledPane.collapsibleProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == true) {
					sprintf("Timeline about to stop");
					model_importImages.getRenderVisibleNode().stopTimeLine();
				}
			}
		});
		// titledPane.getStyleClass().add("titlePane");
		titledPane.setId("titledPane");
		return titledPane;
	}

	public TilePane createTilePane(int i) {
		TilePane tilePane = new TilePane();
		tilePane.getStyleClass().add("tilePane");
		return tilePane;

	}

	public StackPane createStackPane(FileInfo fi, int index) {
		StackPane stackPane = new StackPane();
//		stackPane.setMouseTransparent(true);
		stackPane.setUserData(fi);
		stackPane.setAlignment(Pos.TOP_CENTER);
		stackPane.setId("imageFrame");
		stackPane.setPrefSize(width, height);
		stackPane.setMinSize(width, height);
		stackPane.setMaxSize(width, height);

		stackPane.getStyleClass().add("imageFrame");
		// Scale scale = new Scale();
		// scale.pivotYProperty().bind(stackPane.heightProperty());
		//
		// stackPane.hoverProperty().addListener((observable, oldValue, newValue) -> {
		// // adjust scale when hover state is changed
		// sprintf("hooover");
		// double scaleFactor = newValue ? 3.5 : 1;
		// scale.setX(scaleFactor);
		// scale.setY(scaleFactor);
		// });

		return stackPane;
	}

	public ImageView createImageView(FileInfo fi, int i) {
		ImageView iv = new ImageView();
		iv.setFitWidth(width - 2);
		iv.setFitHeight(height - 2);
		iv.maxWidth(width - 2);
		iv.maxHeight(height - 2);
		iv.setPreserveRatio(true);
		iv.setMouseTransparent(true);
		iv.setId("imageView");
		iv.setRotate(rotate(fi.getOrientation()));
		// Image image = new Image(Paths.get(path).toUri().toString(), 100, 0, true,
		// true, true);
		// iv.setImage(image);
		return iv;
	}

	public void setListenerComboBox(ComboBox<String> comboBox, ObservableList<String> observableList, BooleanProperty over, IntegerProperty maxLength_event) {

		comboBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String str = comboBox.getEditor().getText();
				sprintf("Event setOnAction : " + str);
				if (!str.isEmpty()) {
					if (!observableList.contains(str)) {
						observableList.add(str);
					}
				}
			}
		});
		comboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue == false) {
					String str = comboBox.getEditor().getText();
					if (!str.isEmpty()) {
						if (!observableList.contains(str)) {
							observableList.add(str);
							Collections.sort(observableList);
						}
					}
				}
			}
		});
		comboBox.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				sprintf("newValue: " + newValue.length() + " maxLength_event.get() " + maxLength_event.get());
				//
				if (over.get() == true) {
					sprintf("Is over! " + maxLength_event);
					comboBox.getEditor().setText(oldValue);
				}
			}
		});

		// comboBox.getEditor().lengthProperty().addListener(new
		// ChangeListener<Number>() {
		// @Override
		// public void changed(ObservableValue<? extends Number> observable, Number
		// oldValue, Number newValue) {
		// maxLength_event.set(maxLength_event.get() + (int) newValue);
		// sprintf("maxLength_event: " + maxLength_event.get());
		//
		// if (maxLength_event.get() <= 20) {
		// comboBox.getEditor().setText(comboBox.getEditor().getText().substring(0,
		// comboBox.getEditor().getText().length()));
		// sprintf("Not over!");
		// } else {
		// sprintf("Is over!");
		// comboBox.getEditor().setText(comboBox.getEditor().getText().substring(0,
		// comboBox.getEditor().getText().length()));
		// }
		// }
		// });
	}

	public Label createLabel(String subString) {
		Label label = new Label(subString);
		return label;
	}

	public CheckBox createCheckBox() {
		CheckBox chk = new CheckBox();
		return chk;
	}

	public ComboBox<String> createComboBox(ObservableList<String> observableList, String id, BooleanProperty over, IntegerProperty maxLength) {
		ComboBox<String> comboBox = new ComboBox<>(observableList);
		comboBox.setId(id);
		model_importImages.getGUIUtils().setListenerComboBox(comboBox, model_importImages.getLocation_obs(), over, maxLength);
		new AutoCompleteComboBoxListener<>(comboBox);

		
		return comboBox;
	}

}
