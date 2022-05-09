/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.importimages;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.girbola.Main;
import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.closerlook.CloserLookController;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SqliteConnection;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Marko Lokka
 */
public class DrawImagesToImportImages_Full extends Task<Void> {

	private final String ERROR = DrawImagesToImportImages_Full.class.getSimpleName();

	private Map<String, List<FileInfo>> list;
	// private SelectionModel_Import sel = new SelectionModel_Import();
	private SimpleIntegerProperty index = new SimpleIntegerProperty(0);
	private FolderInfo folderInfo;
	private VBox vbox;
	private Model_importImages model_importImages;
	private Connection connection;

	DrawImagesToImportImages_Full(Map<String, List<FileInfo>> aList, Model_importImages aModel_importImages,
			FolderInfo aFolderInfo, VBox aVbox, Connection connection) {
		this.folderInfo = aFolderInfo;
		this.model_importImages = aModel_importImages;
		this.vbox = aVbox;
		this.list = new TreeMap<>(aList);
		if (connection == null) {
			this.connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
					Main.conf.getMdir_db_fileName());
		} else {
			this.connection = connection;
		}
		sprintf("List size is: " + list.size());
		index.set(0);
		// model_importImages.getEvent_obs().add(folderInfo.getFolderJustPathName());
	}

	@Override
	protected Void call() throws Exception {
		sprintf("DrawImages listing\n===================================");
		for (Map.Entry<String, List<FileInfo>> entry : list.entrySet()) {
			if (Main.getProcessCancelled()) {
				cancel();
				break;
			}
			TitledPane titledPane = model_importImages.getGUIUtils().createTitledPane(index.get());
			TilePane tilePane = model_importImages.getGUIUtils().createTilePane(index.get());
			tilePane.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getTarget() instanceof StackPane) {
						if (event.isShiftDown() && !event.isControlDown()) {
							if (!model_importImages.getSelectionModel_Import().contains((Node) event.getTarget())) {
								model_importImages.getSelectionModel_Import().add((Node) event.getTarget());
							} else {
								model_importImages.getSelectionModel_Import().remove((Node) event.getTarget());
							}
						} else {
							if (!model_importImages.getSelectionModel_Import().contains((Node) event.getTarget())) {
								model_importImages.getSelectionModel_Import().add((Node) event.getTarget());
							} else {
								model_importImages.getSelectionModel_Import().remove((Node) event.getTarget());
							}
						}
					} else if (event.getTarget() instanceof TilePane) {
						model_importImages.getSelectionModel_Import().clearAll();
					}
				}
			});
			titledPane.setContent(tilePane);

			vbox.getChildren().add(titledPane);
			List<FileInfo> current_fileInfo = new ArrayList<>();
			for (FileInfo fi : entry.getValue()) {
				Group group = model_importImages.getGUIUtils().createGroup(fi, index.get());
				StackPane stackPane = model_importImages.getGUIUtils().createStackPane(fi, index.get());
				Label text = new Label(fi.getOrgPath().substring(fi.getOrgPath().lastIndexOf(".") + 1));
				ImageView imageView = model_importImages.getGUIUtils().createImageView(fi, index.get());

				StackPane.setAlignment(imageView, Pos.CENTER);
				StackPane.setAlignment(text, Pos.BOTTOM_RIGHT);

				stackPane.getChildren().addAll(imageView, text);
				group.getChildren().add(stackPane);

				tilePane.getChildren().add(group);

				current_fileInfo.add(fi);
				index.set(index.get() + 1);
			}
			titledPane.setUserData(current_fileInfo);

			CheckBox checkBox = new CheckBox();

			IntegerProperty maxLength = new SimpleIntegerProperty(0);
			BooleanProperty over = new SimpleBooleanProperty(false);
			Label location_lbl = new Label(" " + bundle.getString("location"));
			ComboBox<String> location_cb = new ComboBox<>(model_importImages.getLocation_obs());
			location_cb.setId("location");
			model_importImages.getGUIUtils().setListenerComboBox(location_cb, model_importImages.getLocation_obs(),
					over, maxLength);
			new AutoCompleteComboBoxListener<>(location_cb);
			HBox location_hBox = new HBox(location_lbl, location_cb);
			location_hBox.setAlignment(Pos.CENTER);

			Label event_lbl = new Label(" " + bundle.getString("event"));
			ComboBox<String> event_cb = new ComboBox<>(model_importImages.getEvent_obs());
			if (!model_importImages.getEvent_obs().contains(folderInfo.getJustFolderName())) {
				model_importImages.getEvent_obs().add(folderInfo.getJustFolderName());
			}
			event_cb.getSelectionModel().select(folderInfo.getJustFolderName());
			event_cb.setId("event");
			model_importImages.getGUIUtils().setListenerComboBox(event_cb, model_importImages.getEvent_obs(), over,
					maxLength);
			new AutoCompleteComboBoxListener<>(event_cb);

			HBox event_hBox = new HBox(event_lbl, event_cb);
			event_hBox.setAlignment(Pos.CENTER);

			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					for (Node titled : tilePane.getChildren()) {
						if (titled instanceof Group) {
							sprintf("tpllist iterating: " + titled);
							if (titled.getId().contains("group:")) {
								model_importImages.getSelectionModel_Import().add(titled);
								sprintf(" localtion: " + location_cb.getEditor().getText() + " event: "
										+ event_cb.getEditor().getText());
							}
						} else {
							sprintf("tpList was not tilePane); " + titled);
						}
					}

				}
			});

			Label max = new Label();
			NumberBinding numberBinding = location_cb.getEditor().lengthProperty()
					.add(event_cb.getEditor().lengthProperty());
			maxLength.bind(numberBinding);

			max.textProperty().bind(numberBinding.asString());
			StringProperty stringProperty = new SimpleStringProperty();

			Label folderName = new Label();
			folderName.textProperty().bindBidirectional(stringProperty);
			stringProperty.set(entry.getKey());
			numberBinding.addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if (numberBinding.getValue().intValue() >= 245) {
						sprintf("is over: " + newValue);
						// sp.set(entry.getKey() + " - " +
						// location_cb.getEditor().getText() + " - " +
						// event_cb.getEditor().getText());
						over.set(true);
					} else {
						stringProperty.set(entry.getKey() + " - " + location_cb.getEditor().getText() + " - "
								+ event_cb.getEditor().getText());

						over.set(false);
					}
				}
			});
			Button closerLook = new Button("CloserLook");
			closerLook.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					model_importImages.getRenderVisibleNode().stopTimeLine();
					FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/closerlook/CloserLook.fxml"),
							bundle);

					Parent root = null;
					try {
						root = loader.load();
					} catch (IOException ex) {
						Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
					}
					CloserLookController closerLookController = (CloserLookController) loader.getController();

					Scene close_scene = new Scene(root);
					Stage close_stage = new Stage();

					close_stage.setScene(close_scene);
					closerLookController.init((List<FileInfo>) titledPane.getUserData(),
							model_importImages.getCurrentFolderPath(), connection);

					close_stage.show();
				}
			});
			HBox hbox = new HBox(checkBox, new Label(" "), closerLook, new Label(entry.getKey() + "     "),
					location_hBox, new Label(" "), event_hBox, new Label(" max char: "), max, new Label(" folder= "),
					folderName);
			hbox.setAlignment(Pos.CENTER);
			titledPane.setGraphic(hbox);

			index.set(index.get() + 1);
		}

		return null;
	}

	@Override
	protected void cancelled() {
		super.cancelled();
	}

	@Override
	protected void failed() {
		super.failed();
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		model_importImages.getScrollPane().setVvalue(-1);
	}

}
