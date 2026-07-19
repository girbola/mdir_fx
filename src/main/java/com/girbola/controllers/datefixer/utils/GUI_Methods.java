package com.girbola.controllers.datefixer.utils;

import com.girbola.Main;
import com.girbola.controllers.datefixer.DateFixConstants;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;

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
            file = Main.class.getResource("/img/" + string);
            if (file != null) {
                return new Image(file.toString(), buttonWidth, 0, true, true, false);
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
        if (children instanceof VBox && children.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
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
        if (children instanceof VBox && children.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
            FileInfo fi = (FileInfo) children.getUserData();
            return fi.getEvent();
        }
        return null;
    }

//	/**
//	 * getCameraModel will get cameraModel from Node getUserDatas
//	 *
//	 * @param children
//	 * @return
//	 */
//	public static String getLocations(Node children) {
//		if (children instanceof VBox && children.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
//			FileInfo fi = (FileInfo) children.getUserData();
//			return fi.getLocation();
//		}
//		return null;
//	}

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

    public static void scrollNodeTopIntoView(ScrollPane scrollPane, Node node, double paddingTop) {
        Messages.sprintf("############scrollNodeTopIntoView: " + node.toString());
        if (scrollPane == null || scrollPane.getContent() == null || node == null) return;

        final Bounds viewportBounds = scrollPane.getViewportBounds();
        final double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        final double viewportHeight = viewportBounds.getHeight();

        if (contentHeight <= viewportHeight || contentHeight <= 0) {
            scrollPane.setVvalue(scrollPane.getVmin());
            return;
        }

        // Calculate the scrollable range in pixels
        final double scrollablePixelRange = contentHeight - viewportHeight;

        // Determine the target Y position in pixels (Node top - padding)
        final double nodeTopPixelY = node.getBoundsInParent().getMinY();
        final double effectivePadding = Math.max(0.0, Math.min(paddingTop, viewportHeight * 0.5));
        final double targetPixelY = nodeTopPixelY - effectivePadding;

        // Convert pixel position to normalized 0.0-1.0 value
        final double vmaxValue = scrollPane.getVmax();
        final double vminValue = scrollPane.getVmin();
        final double normalizedValue = (targetPixelY / scrollablePixelRange) * (vmaxValue - vminValue);

        // Clamp the result to valid ScrollPane bounds
        final double finalVValue = Math.min(vmaxValue, Math.max(vminValue, normalizedValue));

        Messages.sprintf("scrollNodeTopIntoView - Target Y: " + targetPixelY + " Final VValue: " + finalVValue);
        Platform.runLater(() -> scrollPane.setVvalue(finalVValue));
    }

    public static void centerNodeInScrollPane(ScrollPane scrollPane, Node node) {
        double scrollPane_bounds_height = scrollPane.getContent().getBoundsInLocal().getHeight();
        double node_height_tmp = (node.getBoundsInParent().getMaxY() + node.getBoundsInParent().getMinY()) / 2.0;
        double scrollPane_viewport_height = scrollPane.getViewportBounds().getHeight();
        Messages.sprintf("scrollPane_bounds_height: " + scrollPane_bounds_height);
        Messages.sprintf("node_height_tmp: " + node_height_tmp);
        Messages.sprintf("scrollPane_viewport_height: " + scrollPane_viewport_height);
        Messages.sprintf("node.getBoundsInParent(): " + node.getBoundsInParent().toString());
        //        sprintf("scrollPane_bounds_height: " + scrollPane_bounds_height
        //                + "\nscrollPane_viewport_height " + scrollPane_viewport_height
        //                + "\nnode.getBoundsInParent().getMinY() " + node.getBoundsInParent().getMinY()
        //                + "\nnode.getBoundsInParent().getMaxY() " + node.getBoundsInParent().getMaxY()
        //                + "\n node_height_tmp: " + node_height_tmp
        //                + "\nscrollPane.getVmax() " + scrollPane.getVmax());
        double vmaxValueToSet = scrollPane.getVmax()
                * ((node_height_tmp - 0.5 * scrollPane_viewport_height) / (scrollPane_bounds_height - scrollPane_viewport_height));
        Messages.sprintf("=======vmaxValueToSet: " + vmaxValueToSet);
        scrollPane.setVvalue(vmaxValueToSet);
    }

}
