package com.girbola.controllers.datefixer;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class Node_Methods {

	public static VBox getImageFrameNode(Node node, String id) {
		if (node instanceof VBox) {
			if (node.getId().equals(id)) {
				return (VBox) node;
			}
		}
		return null;
	}

}
