/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils.ui;

import com.girbola.configuration.GUIPrefs;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.Set;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.misc.Misc.getLineNumber;

/**
 *
 * @author Marko Lokka
 */
public class UI_Tools {

	final private static String ERROR = UI_Tools.class.getSimpleName();

	public static Bounds getNodeLayoutBounds(Node node) {
		// log.error(arg0);
		Bounds bound = node.getLayoutBounds();
		return bound;
	}

	public static double getScrollBarWidth(ScrollPane scrollPane) {
		Set<Node> listOfNodes = scrollPane.lookupAll(".scroll-bar");
		for (Node node : listOfNodes) {
			if (node instanceof ScrollBar) {
				ScrollBar scrollBar = (ScrollBar) node;
				return scrollBar.getWidth();
			}
		}
		return 0;
	}

	public static void setGridRowConstraints(GridPane gridPane, int mRows) {
		if (mRows == 0) {
			errorSmth(ERROR, "", null, getLineNumber(), true);
		} else {
			for (int i = 0; i < mRows; i++) {
				RowConstraints grid_row1 = new RowConstraints(GUIPrefs.imageFrame_y, GUIPrefs.imageFrame_y, GUIPrefs.imageFrame_y, Priority.NEVER,
						VPos.CENTER, false);
				grid_row1.setValignment(VPos.CENTER);
				gridPane.getRowConstraints().add(grid_row1);
			}
		}
	}

	public static void setGridColumnConstraints(GridPane gridPane, int imagesPerLine) {
		// sprintf("setGridColumnConstraints started imagesPerLine: " + imagesPerLine);
		ColumnConstraints[] col = new ColumnConstraints[imagesPerLine];
		for (int i = 0; i < imagesPerLine; i++) {
			col[i] = new ColumnConstraints(GUIPrefs.imageFrame_x, GUIPrefs.imageFrame_x, GUIPrefs.imageFrame_x, Priority.NEVER, HPos.CENTER, false);
			gridPane.getColumnConstraints().add(col[i]);
		}
	}
}
