package com.girbola.controllers.datefixer;

import java.net.URL;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GUI_Methods {

	/**
	 * loadImage loads image from /resources/img/ Image will be keep it's ratio
	 * 
	 * @param string
	 * @param buttonWidth
	 * @return
	 */
	public static Image loadImage(String string, int buttonWidth) {
		URL file = null;
		try {
			file = Main.class.getResource("/resources/img/" + string);
			if (file != null) {
				Image image = new Image(file.toString(), buttonWidth, 0, true, true, false);
				return image;
			}
		} catch (Exception e) {
			Messages.sprintfError(
					"file name: " + file + "\nException with loading image from resource: " + e.getMessage());
			com.girbola.controllers.misc.Misc_GUI.fastExit();
		}
		return null;

	}

	/**
	 * getDate will get TextField with date from Node
	 * 
	 * @param children
	 * @return
	 */
	public static String getDate(Node children) {
		if (children instanceof VBox) {
			for (Node node : ((VBox) children).getChildren()) {
				if (node instanceof HBox) {
					for (Node nnn : ((HBox) node).getChildren()) {
						if (nnn instanceof TextField) {
							TextField tf = (TextField) nnn;
							Messages.sprintf("nnn-------> " + tf.getText());
							return tf.getText();
						}
					}
				}
			}
		}
		return null;
		// TextField date = (TextField) node.lookupAll("fileDate");
		// if (date == null) {
		// return null;
		// }
		// return date.getText();
	}

	/**
	 * getCameraModel will get cameraModel from Node getUserDatas
	 * 
	 * @param children
	 * @return
	 */
	public static String getCameraModel(Node children) {
		if (children instanceof VBox && children.getId().equals("imageFrame")) {
			FileInfo fi = (FileInfo) children.getUserData();
			return fi.getCamera_model();
		}
		return null;
	}

	/**
	 * getEvents will get cameraModel from Node getUserDatas
	 * 
	 * @param children
	 * @return
	 */
	public static String getEvents(Node children) {
		if (children instanceof VBox && children.getId().equals("imageFrame")) {
			FileInfo fi = (FileInfo) children.getUserData();
			return fi.getEvent();
		}
		return null;
	}

	/**
	 * getCameraModel will get cameraModel from Node getUserDatas
	 * 
	 * @param children
	 * @return
	 */
	public static String getLocations(Node children) {
		if (children instanceof VBox && children.getId().equals("imageFrame")) {
			FileInfo fi = (FileInfo) children.getUserData();
			return fi.getLocation();
		}
		return null;
	}

	public static Button getShowHideButtonFromTableView(TableView<FolderInfo> table) {
		Node parent = table.getParent();
		if (parent instanceof HBox) {

			if (parent.getId().equals("showHideButton_hbox")) {
				HBox hbox_tools = (HBox) parent;

				for (Node hbox_tools_parent : hbox_tools.getChildren()) {
					if (hbox_tools_parent instanceof Button) {
						return (Button) hbox_tools_parent;
					}
				}
			}
		}
		return null;
	}
}
