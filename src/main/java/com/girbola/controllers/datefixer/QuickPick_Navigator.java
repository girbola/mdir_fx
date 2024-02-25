/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved.  
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class QuickPick_Navigator {

	private final static String ERROR = QuickPick_Navigator.class.getSimpleName();
	private ScrollPane scrollPane;
	//private GridPane gridPane;
	private TilePane tilePane;
	private TilePane quickPick_tilePane;
	private Model_datefix model_dateFix;

	QuickPick_Navigator(Model_datefix aModel_dateFix, ScrollPane aScrollPane, TilePane aTilePane, TilePane aQuickPick_TilePane) {
		this.model_dateFix = aModel_dateFix;
		this.scrollPane = aScrollPane;
		this.tilePane = aTilePane;
		this.quickPick_tilePane = aQuickPick_TilePane;

		//        update();
	}

	private String getStyle(Node n) {
		for (Node vbox : ((VBox) n).getChildren()) {
			sprintf("checkIfRedDates vbox-- " + vbox);
			if (vbox instanceof HBox) {
				sprintf("vbox is: " + vbox);
				for (Node hbox : ((HBox) vbox).getChildren()) {
					sprintf("hbox is: " + hbox);
					if (hbox instanceof TextField) {
						sprintf("--TextField is: " + hbox);
						return hbox.getStyle();
					}
				}
			}
		}
		return null;
	}

	public void update() {
		int x = 0;
		int y = 0;
		List<Node> list = new ArrayList<>();

		for (Node n : tilePane.getChildren()) {
			sprintf("checkIfRedDates node: " + n);
			if (n instanceof VBox) {
				if (n.getId().contains("imageFrame")) {
					if (y != GridPane.getColumnIndex(n)) {
						y = GridPane.getColumnIndex(n);
						list.add(n);
					} else {
						list.clear();
						String style = getStyle(n);
						if (style != null) {
							//                            list.add();
							sprintf("style is: " + style);
						}
					}

				}
			}
		}
	}

	public Button createStatusButton(final double currentHeight, Node node) {
		Button button = new Button();
		button.setPrefSize(15, 6);
		button.setMaxSize(15, 6);
		button.setMinSize(15, 6);

		FileInfo fi = null;
		if (node instanceof VBox) {
			fi = (FileInfo) node.getUserData();
		}
		if (fi != null) {
			button.setTooltip(new Tooltip("node is: " + node + " id is: " + node.getId() + "\n" + fi.getOrgPath()));
		} else {
			Messages.errorSmth(ERROR, "FileInfo were null!", null, Misc.getLineNumber(), true);
//			button.setTooltip(new Tooltip("node is: " + node + " id is: " + node.getId()));
			return null;
		}
		if (fi.isBad()) {
			button.getStyleClass().add("jump_Button_bad");
		} else if (fi.isSuggested()) {
			button.getStyleClass().add("jump_Button_suggested");
		} else if (fi.isGood()) {
			return null;
		} else if (fi.isVideo() && !fi.isSuggested()) {
			button.getStyleClass().add("jump_Button_video");
		} else {
			button.getStyleClass().add("jump_Button_something");
		}
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//                scrollPane.setVvalue(currentHeight);
				//                createNavigationMap();
				centerNodeInScrollPane(scrollPane, node);

			}
		});
		return button;
	}

	public void centerNodeInScrollPane(ScrollPane scrollPane, Node node) {
		double scrollPane_bounds_height = scrollPane.getContent().getBoundsInLocal().getHeight();
		double node_height_tmp = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
		double scrollPane_viewport_height = scrollPane.getViewportBounds().getHeight();
		//        sprintf("scrollPane_bounds_height: " + scrollPane_bounds_height
		//                + "\nscrollPane_viewport_height " + scrollPane_viewport_height
		//                + "\nnode.getBoundsInParent().getMinY() " + node.getBoundsInParent().getMinY()
		//                + "\nnode.getBoundsInParent().getMaxY() " + node.getBoundsInParent().getMaxY()
		//                + "\n node_height_tmp: " + node_height_tmp
		//                + "\nscrollPane.getVmax() " + scrollPane.getVmax());
		scrollPane.setVvalue(scrollPane.getVmax()
				* ((node_height_tmp - 0.5 * scrollPane_viewport_height) / (scrollPane_bounds_height - scrollPane_viewport_height)));
	}

	private String getJumpButtonState(List<String> list) {
		//        sprintf("getJumpButtonState started");
		for (String s : list) {
			sprintf("List of jumpButtonState: " + s);
			if (s.equals(DateFixPopulateQuickPick.DATE_STATUS.DATE_BAD.getType())) {
				return DateFixPopulateQuickPick.DATE_STATUS.DATE_BAD.getType();
			} else {
				if (s.equals(DateFixPopulateQuickPick.DATE_STATUS.DATE_SUGGESTED.getType())) {
					return DateFixPopulateQuickPick.DATE_STATUS.DATE_SUGGESTED.getType();
				} else {
					if (s.equals(DateFixPopulateQuickPick.DATE_STATUS.DATE_VIDEO.getType())) {
						return DateFixPopulateQuickPick.DATE_STATUS.DATE_VIDEO.getType();
					}
				}
			}
		}
		return DateFixPopulateQuickPick.DATE_STATUS.DATE_GOOD.getType();
	}

	/**
	 *
	 * @param fileInfo
	 * @return
	 */
	public String getStatus(FileInfo fileInfo) {
		if (fileInfo.isBad()) {
			return DateFixPopulateQuickPick.DATE_STATUS.DATE_BAD.getType();
		} else if (fileInfo.isGood()) {
			return DateFixPopulateQuickPick.DATE_STATUS.DATE_GOOD.getType();
		} else if (fileInfo.isSuggested()) {
			return DateFixPopulateQuickPick.DATE_STATUS.DATE_SUGGESTED.getType();
		} else if (fileInfo.isVideo()) {
			return DateFixPopulateQuickPick.DATE_STATUS.DATE_VIDEO.getType();
		}
		return null;
	}

}
