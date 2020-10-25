/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.messages.Messages.warningText;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.FolderInfo_Utils;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.FolderInfo_SQL;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class FileOperationsController {

	private final String ERROR = FileOperationsController.class.getSimpleName();

	private Model_datefix model_datefix;
	private Model_main model_main;
	private ObservableList<Folder> comboList = FXCollections.observableArrayList();

	@FXML
	private Button mixDates_btn;

	@FXML
	private ComboBox<Folder> move_comboBox;

	public void init(Model_datefix aModel_datefix, Model_main aModel_main) {
		this.model_datefix = aModel_datefix;
		this.model_main = aModel_main;

		for (FolderInfo folderInfo_dest : this.model_main.tables().getSorted_table().getItems()) {
			if (!model_datefix.getCurrentFolderPath().toString().equals(folderInfo_dest.getFolderPath())) {
				comboList.add(new Folder(Paths.get(folderInfo_dest.getFolderPath()), folderInfo_dest));

			}
		}
		move_comboBox.setConverter(new StringConverter<Folder>() {
			@Override
			public Folder fromString(String string) {
				return move_comboBox.getItems().stream().filter(ap -> ap.getName().equals(string)).findFirst()
						.orElse(null);
			}

			@Override
			public String toString(Folder object) {
				return object.getName();

			}
		});
		move_comboBox.valueProperty().addListener(new ChangeListener<Folder>() {
			@Override
			public void changed(ObservableValue<? extends Folder> observable, Folder oldValue, Folder newValue) {
				if (newValue != null) {
					sprintf("newValue: " + newValue.getPath());
				}
			}
		});
		move_comboBox.setItems(comboList);
	}

	@FXML
	private void mixDates_btn_action(ActionEvent event) {
	}

	@FXML
	private void move_comboBox_action(ActionEvent event) {
		sprintf("ComboBox is under constructor");
//        ComboBox cb = (ComboBox) event.getSource();
		Folder folder = (Folder) move_comboBox.getValue();
		Messages.warningText("cbbb: " + folder.getName() + " path: " + folder.getPath());
//		Platform.exit();
		if (model_datefix.getSelectionModel().getSelectionList().isEmpty()) {
			warningText(bundle.getString("noSelectedFiles"));
			return;
		}

		FolderInfo folderInfo = model_datefix.getFolderInfo_full();

		List<FileInfo> toRemove = new ArrayList<>();

		for (Node node : model_datefix.getSelectionModel().getSelectionList()) {
			if (node instanceof VBox) {
				if (node.getId().equals("imageFrame")) {
					FileInfo fileInfo = (FileInfo) node.getUserData();
					toRemove.add(fileInfo);

					FileInfo fileInfo_dest = (FileInfo) fileInfo.clone();

					Path newFilePath = Paths.get(folder.getPath().toString() + File.separator
							+ Paths.get(fileInfo.getOrgPath()).getFileName().toString());
					fileInfo_dest.setOrgPath(newFilePath.toString());

					String destTableType = folder.getFolderInfo().getTableType();

					if (destTableType.equals(TableType.SORTED)) {
						folder.getFolderInfo().getFileInfoList().add(fileInfo_dest);
					} else {

					}
					// Remove fileInfo from source
					FolderInfo_Utils.moveFolderInfoTO(sourceFolderInfo, dest_FolderInfo);

					model_datefix.getFolderInfo_full().getFileInfoList().remove(fileInfo);
					model_datefix.getFolderInfo_full().setChanged(true);
					for (Node vbox : ((VBox) node).getChildren()) {
						if (vbox instanceof TextField) {
//FIXAA TÄMÄ!!!!
//                        Path source = Paths.get(model.getCurrentFilePath() + File.separator + ((TextField) vbox).getText());
//                            Path source = Paths.get(fileInfo.getPath());
//                            Folder folder = (Folder) cb.getValue();
//                            Path destination = Paths.get(folder.getPath() + File.separator + ((TextField) vbox).getText());
//                            FileInfo fi_src = TableUtils.findFileInfo(TableType.SORTED.getType(), Paths.get(fileInfo.getPath()), model_main.getTables());
//                            if (fi_src == null) {
//                                sprintf("fi_src were null;");
//                                return;
//                            }
//                            sprintf("fi_src found!: " + fi_src.toString());
//                            TableValues fi_dest = TableUtils.findTableValues(source, model_main.getTables().get)(TableType.SORTED.getType(), folder.getPath(), model_main.getTables());
//                            if (fi_dest == null) {
//                                sprintf("fi_dest were null;");
//                                return;
//                            }
//
//                            sprintf("fi_src: " + fi_src.toString() + " fi_dest: " + fi_dest.toString());
//
//                            sprintf("NOT MOVING YET Moving from: " + source);
//                            sprintf("->> TO - >: " + destination);
//                        model.getSelectionModel().remove(node);
						}
					}
				}
			}
		}
		folderInfo.getFileInfoList().removeAll(toRemove);
		TableUtils.updateFolderInfos_FileInfo(folderInfo);

		Map<String, List<FileInfo>> map = new HashMap<>();

		String currentFolderInfoFolder = "";
		boolean found = false;
		for (FileInfo fileInfo : toRemove) {
			found = false;
			Path rootPath = Paths.get(fileInfo.getOrgPath()).getParent();
			for (Entry<String, List<FileInfo>> entry : map.entrySet()) {
				if (entry.getKey() == rootPath.toString()) {
					entry.getValue().add(fileInfo);
					found = true;
				}

			}
			if (!found) {
				map.put(rootPath.toString(), new ArrayList<>(Arrays.asList(fileInfo)));
			}

		}

		for (FileInfo fileInfo : toRemove) {
			Path rootPath = Paths.get(fileInfo.getOrgPath()).getParent();
			if (rootPath.toString().length() > 1 && rootPath.toString() != currentFolderInfoFolder) {
				FolderInfo loadFolderInfo = FolderInfo_SQL.loadFolderInfo(rootPath.toString());

			}
		}

	}
}

class Folder {

	private Path path;
	private String name;
	private FolderInfo folderInfo;

	public FolderInfo getFolderInfo() {
		return folderInfo;
	}

	public void setFolderInfo(FolderInfo folderInfo) {
		this.folderInfo = folderInfo;
	}

	public Folder(Path path, FolderInfo folderInfo) {
		this.path = path;
		this.folderInfo = folderInfo;
		name = path.getFileName().toString();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
