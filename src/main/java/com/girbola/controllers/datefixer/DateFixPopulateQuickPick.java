/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.girbola.Main;
import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.loading.LoadingProcessTask;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.Main.*;
import static com.girbola.controllers.datefixer.ImageUtils.playVideo;
import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

/**
 *
 * @author Marko Lokka
 */
public class DateFixPopulateQuickPick extends Task<Void> {

	private final static String ERROR = DateFixPopulateQuickPick.class.getSimpleName();

	private FolderInfo folderInfo;
	//private GridPane gridPane;
	private TilePane quickPick_tilePane;
	private Model_datefix model_dateFix;
	private ScrollPane scrollPane;
	private Scene scene;
	private DoubleProperty node_height = new SimpleDoubleProperty(0);

	private VBox frame;
	private SimpleIntegerProperty counter = new SimpleIntegerProperty(0);
	private LoadingProcessTask loadingProcess_task;

	private TilePane tilePane;

	public DateFixPopulateQuickPick(Scene scene, Model_datefix aModel_datefix, TilePane aTilePane, LoadingProcessTask loadingProcess_task) {

		this.scene = scene;
		this.model_dateFix = aModel_datefix;
		this.model_dateFix.getSelectionModel().clearAll();
		this.quickPick_tilePane = aModel_datefix.getQuickPick_tilePane();
		this.tilePane = aTilePane;
		this.folderInfo = aModel_datefix.getFolderInfo_full();
		this.scrollPane = aModel_datefix.getScrollPane();
		this.loadingProcess_task = loadingProcess_task;
		sprintf("FolderInfo.getMinDate(): " + this.folderInfo.getMinDate());
		aModel_datefix.setDateTime(this.folderInfo.getMinDate(), true);
		aModel_datefix.setDateTime(this.folderInfo.getMaxDate(), false);

		// RectangleSelection rectangleSelection = new RectangleSelection(scene,
		// pane,
		// gridPane, this.model.getSelectionModel());
		// new RectangleSelection2(scene, pane, this.model.getSelectionModel());
	}

	private VBox createFrame(FileInfo fileInfo, int index) {
		VBox frame_vbox = new VBox();
		frame_vbox.setAlignment(Pos.TOP_CENTER);
		frame_vbox.setId("imageFrame");
		frame_vbox.setPrefSize(GUIPrefs.imageFrame_x, GUIPrefs.imageFrame_y);
		frame_vbox.setMinSize(GUIPrefs.imageFrame_x, GUIPrefs.imageFrame_y);
		frame_vbox.setMaxSize(GUIPrefs.imageFrame_x, GUIPrefs.imageFrame_y);
		frame_vbox.getStyleClass().add("imageFrame");

		StackPane stackPane = createStackPane(index);

		ImageView iv = createImageView(fileInfo, index);

		StackPane.setAlignment(iv, Pos.CENTER);

		// StackPane.setAlignment(checkBox, Pos.TOP_LEFT);
		TextField fileName_ta = createFileName_tf(Paths.get(fileInfo.getOrgPath()), index);
		TextField fileDate_tf = createFileDate_tf(fileInfo, index);

		VBox.setVgrow(stackPane, Priority.ALWAYS);
		VBox.setVgrow(fileName_ta, Priority.NEVER);
		VBox.setVgrow(fileDate_tf, Priority.NEVER);

		HBox bottom = new HBox();
		bottom.setId("bottom");

		HBox.setHgrow(bottom, Priority.ALWAYS);
		HBox.setHgrow(fileDate_tf, Priority.ALWAYS);

		Button accept = createAcceptButton(fileInfo, fileDate_tf, index);
		bottom.setAlignment(Pos.CENTER);

		if (!fileInfo.getLocation().isEmpty() || !fileInfo.getEvent().isEmpty()) {
			frame_vbox.setStyle("-fx-background-color: red;");
		}

		stackPane.getChildren().add(iv);
		bottom.getChildren().addAll(accept, fileDate_tf);
		frame_vbox.getChildren().addAll(stackPane, fileName_ta, bottom);

		return frame_vbox;
	}

	private Button createAcceptButton(FileInfo fi, TextField tf, int i) {
		Button button = new Button();
		ImageView imageView = new ImageView(GUI_Methods.loadImage("confirm.png", GUIPrefs.BUTTON_WIDTH));
		button.setGraphic(imageView);

		button.setId("accept");
		if (!fi.isGood()) {
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					String date = tf.getText();
					tf.setText("" + date);
					tf.setStyle(CssStylesController.getModified_style());
					sprintf("Accepted: " + fi.getDate() + " path:  " + fi.getOrgPath() + " time: "
							+ model_dateFix.getStart_time().getTime());
				}
			});
		} else {
			button.setDisable(true);
		}
		return button;
	}

	private ImageView createImageView(FileInfo fi, int i) {
		ImageView iv = new ImageView();
		iv.setFitWidth(GUIPrefs.thumb_x_MAX - 2);
		iv.setFitHeight(GUIPrefs.thumb_y_MAX - 2);
		iv.maxWidth(GUIPrefs.thumb_x_MAX - 2);
		iv.maxHeight(GUIPrefs.thumb_y_MAX - 2);
		iv.setPreserveRatio(true);
		iv.setMouseTransparent(true);
		// iv.setRotate(rotate(fi.getOrientation()));
		iv.setId("imageView");
		return iv;
	}

	private StackPane createStackPane(int i) {
		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.CENTER);
		stackPane.setId("stackPane");
		stackPane.setMouseTransparent(true);
		return stackPane;
	}

	private TextField createFileName_tf(Path path, int index) {
		TextField textField = new TextField();
		textField.getStyleClass().add("fileName_ta");
		textField.setEditable(false);
		textField.setFocusTraversable(false);
		textField.setId("fileName");
		textField.setMaxHeight(25);
		textField.setMinHeight(25);
		textField.setPrefHeight(25);
		// textField.setUserData(path);
		textField.setText(path.getFileName().toString());
		return textField;
	}

	private TextField createFileDate_tf(FileInfo fileInfo, int index) {
		TextField textField = new TextField(simpleDates.getSdf_ymd_hms_minusDots_default().format(fileInfo.getDate()));
		textField.getStyleClass().add("fileDate_tf");
		textField.setEditable(false);
		textField.setFocusTraversable(false);
		textField.setId("fileDate");
		textField.setMaxHeight(25);
		textField.setMinHeight(25);
		textField.setPrefHeight(25);
		if (fileInfo.isBad()) {
			textField.setStyle(CssStylesController.getBad_style());
		} else if (fileInfo.isGood()) {
			textField.setStyle(CssStylesController.getGood_style());
		} else if (fileInfo.isConfirmed()) {
			textField.setStyle(CssStylesController.getConfirmed_style());
		} else if (fileInfo.isVideo()) {
			textField.setStyle(CssStylesController.getVideo_style());
		} else if (fileInfo.isSuggested()) {
			textField.setStyle(CssStylesController.getSuggested_style());
		}

		return textField;
	}

	private void setSelectedImageRoutine(FileInfo fileInfo, VBox frame) {
		frame.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				if (event.getButton().equals(MouseButton.PRIMARY)) {
					long start = System.currentTimeMillis();
					if (event.getClickCount() == 1) {

						Messages.sprintf("Clicked setSelectedImageRoutine PRIMARY mouseclickcount = 1");

						model_dateFix.getRightInfoPanel().getChildren().clear();
						model_dateFix.getMetaDataTableView_obs().clear();
						if (frame instanceof VBox) {
							for (Node node : frame.getChildren()) {
								if (node instanceof StackPane) {
									StackPane sp = (StackPane) node;
									for (Node nodeImv : sp.getChildren()) {
										if (nodeImv instanceof ImageView) {
											ImageView imv = (ImageView) nodeImv;

											if (imv.getImage() != null) {

												boolean deselected = model_dateFix.getSelectionModel().addWithToggle(frame);
												if (model_dateFix.getRightInfo_visible()) {
													File file = new File(fileInfo.getOrgPath());
													model_dateFix.getMetaDataTableView_obs().add(new MetaData(
															Main.bundle.getString("filename"), file.toString()));
													Metadata metaData = null;
													try {
														metaData = ImageMetadataReader.readMetadata(file);
													} catch (Exception e) {
														deselected = false;
													}
													if (!deselected) {
														if (metaData != null) {
															for (Directory dir : metaData.getDirectories()) {
																if (dir != null) {
																	if (!dir.getTags().isEmpty()) {
																		TitledPane titledPane = createTitledPane();
																		model_dateFix.getRightInfoPanel().getChildren()
																				.add(titledPane);
																		titledPane.setText(dir.getName());
																		ObservableList<MetaData> obs = FXCollections
																				.observableArrayList();
																		TableView<MetaData> table = createTableView();
																		table.setItems(obs);
																		titledPane.setContent(table);
																		for (Tag tag : dir.getTags()) {
																			obs.add(new MetaData(tag.getTagName(),
																					tag.getDescription()));
																		}
																		adjustTableHeight(table, obs);
																	}
																}
															}
															Messages.sprintf("Metadata reading took: "
																	+ (System.currentTimeMillis() - start));
														}
													}
												}
											} else {
												Messages.sprintf("Not able to select because imageview were null");
											}
										}
									}
								}
							}
						}

					} else if (event.getClickCount() == 2) {
						Messages.sprintf("Clickcount were 2");
						List<FileInfo> list = getFileList(tilePane.getChildren());
						if (Files.exists(Paths.get(fileInfo.getOrgPath()))) {
							ImageUtils.view(list, fileInfo, Main.scene_Switcher.getScene_dateFixer().getWindow());
						} else {
							Messages.errorSmth(ERROR, bundle.getString("imageNotExists") + " " + fileInfo.getOrgPath(),
									null, getLineNumber(), true);
						}
					} else {
						Messages.sprintf("getselectionmodel. adding frame to selectionmodel");
						model_dateFix.getSelectionModel().addWithToggle(frame);
					}
				}
			}

			private List<FileInfo> getFileList(ObservableList<Node> children) {
				List<FileInfo> list = new ArrayList<>();
				for (Node node : children) {
					FileInfo fileInfo = (FileInfo) node.getUserData();
					Messages.sprintf("getFileList: " + node + " fileInfo is: " + fileInfo.getOrgPath());
					list.add(fileInfo);
				}

				return list;
			}

			private void adjustTableHeight(TableView<MetaData> table, ObservableList<MetaData> obs) {
				table.setPrefHeight(obs.size() * 30);
				if (obs.size() <= 1) {
					table.setMinHeight(80);
				} else {
					table.setMinHeight(obs.size() * 30);
				}
				table.setMaxHeight(obs.size() * 30);
			}

			@SuppressWarnings("unchecked")
			private TableView<MetaData> createTableView() {
				TableView<MetaData> table = new TableView<>();
				table.getStyleClass().add("metadataTable");
				TableColumn<MetaData, String> info_column = new TableColumn<>();
				info_column.setCellValueFactory(
						(TableColumn.CellDataFeatures<MetaData, String> cellData) -> new SimpleObjectProperty<>(
								cellData.getValue().getTag()));

				TableColumn<MetaData, String> value_column = new TableColumn<>();
				value_column.setCellValueFactory(
						(TableColumn.CellDataFeatures<MetaData, String> cellData) -> new SimpleObjectProperty<>(
								cellData.getValue().getValue()));
				table.getColumns().addAll(info_column, value_column);

				return table;
			}

			private TitledPane createTitledPane() {
				return new TitledPane();
			}
		});

	}

	private void setSelectedVideoRoutine(Path path, VBox frame) {
		frame.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					if (event.getClickCount() == 2) {
						if (conf.isVlcSupport()) {
							sprintf(" if (conf.isVlcSupport()) {..");
							if (Files.exists(path)) {
								playVideo(path, frame);
							} else {
								Messages.errorSmth(ERROR, bundle.getString("imageNotExists") + " " + path, null,
										getLineNumber(), true);
							}
						} else {
							if (Files.exists(path)) {
								try {
									Desktop.getDesktop().open(path.toFile());
								} catch (IOException ex) {
									Logger.getLogger(DateFixPopulateQuickPick.class.getName()).log(Level.SEVERE, null,
											ex);
									errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
								}
							} else {
								Messages.errorSmth(ERROR, bundle.getString("imageNotExists") + " " + path, null,
										getLineNumber(), true);
							}
						}
					} else {
						model_dateFix.getSelectionModel().addWithToggle(frame);
					}
				}
			}
		});
	}

	enum DATE_STATUS {

		DATE_BAD("bad"), DATE_GOOD("good"), DATE_SUGGESTED("suggested"), DATE_VIDEO("video");

		private String type;

		DATE_STATUS(String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}
	}

	@Override
	protected Void call() throws Exception {

		for (FileInfo fi : folderInfo.getFileInfoList()) {
			if (Main.getProcessCancelled()) {
				cancel();
				sprintf("Process has been cancelled!");
				return null;
			}
			if (Files.exists(Paths.get(fi.getOrgPath()))) {
				if (fi.isImage() || fi.isVideo() || fi.isRaw()) {
					frame = null;
					if (fi.isImage() || fi.isRaw()) {
						frame = createFrame(fi, counter.get());
						setSelectedImageRoutine(fi, frame);
					} else if (fi.isVideo()) {
						frame = createFrame(fi, counter.get());
						setSelectedVideoRoutine(Paths.get(fi.getOrgPath()), frame);
					}
					if (frame != null) {
						frame.setUserData(fi);
						model_dateFix.getAllNodes().add(frame);

						Button statusButton = model_dateFix.getQuickPick_Navigator()
								.createStatusButton(node_height.get(), frame);
						if (statusButton != null) {
							if (quickPick_tilePane == null) {
								Messages.errorSmth(ERROR, "quickPick_tilePane were null!!!!", null,
										Misc.getLineNumber(), true);
							}
							// TODO Jotain vikaa quickpick navigatorissa. Hmmmmmmmmmmmmmmm
							Messages.sprintf("StatusButton were not null: " + statusButton);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									quickPick_tilePane.getChildren().add(statusButton);
								}
							});
						}
						counter.set(counter.get() + 1);
					}
				}
			}
		}
		return null;
	};

	@Override
	protected void succeeded() {
		super.succeeded();
		loadingProcess_task.closeStage();
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		loadingProcess_task.closeStage();
	}

	@Override
	protected void failed() {
		super.failed();
		loadingProcess_task.closeStage();
	}

}
