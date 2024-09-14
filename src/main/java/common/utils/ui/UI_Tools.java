/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils.ui;

import com.girbola.configuration.GUIPrefs;
import com.girbola.messages.Messages;
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
		Messages.sprintf("getNodeLayoutBounds started");
		return node.getLayoutBounds();
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

}
