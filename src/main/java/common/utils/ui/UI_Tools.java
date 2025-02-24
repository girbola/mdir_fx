/*
 @(#)Copyright:  Copyright (c) 2012-2025 All right reserved.
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils.ui;

import com.girbola.messages.Messages;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;

import java.util.Set;

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
