/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.ui.UI_Tools;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Marko Lokka
 */
public class HideButtons {

	final private String ERROR = HideButtons.class.getSimpleName();

	private int test = 100;

	private SimpleDoubleProperty maxWidth = new SimpleDoubleProperty(300);

	private int visible;
	private HBox sortit_buttons_hbox;
	private HBox sorted_buttons_hbox;
	private HBox asitis_buttons_hbox;

	private SimpleBooleanProperty asitis_show_property = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty sortit_show_property = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty sorted_show_property = new SimpleBooleanProperty(true);

	private Model_main model_Main;

	private AnchorPane tables_rootPane;

	private Bounds tables_rootPaneNodeLayoutBounds;

	private Bounds button_Bounds;

	private Image show_im;

	private Image hide_im;

	public HideButtons(Model_main model) {
		this.model_Main = model;
		visible = 3;
		show_im = GUI_Methods.loadImage("showTable.png", GUIPrefs.BUTTON_WIDTH);
		hide_im = GUI_Methods.loadImage("hideTable.png", GUIPrefs.BUTTON_WIDTH);

//		tables_rootPane = model_Main.tables().getTables_rootPane();
//
//		tables_rootPaneNodeLayoutBounds = UI_Tools.getNodeLayoutBounds(tables_rootPane);

		sprintf("HideButtons instantiated...");
	}

	public void setSortItButtons_hbox(HBox hbox_sortIt) {
		this.sortit_buttons_hbox = hbox_sortIt;
	}

	public void setAsItIsButtons_hbox(HBox hbox_asItIs) {
		this.asitis_buttons_hbox = hbox_asItIs;
	}

	public void setSortedButtons_hbox(HBox hbox_sorted) {
		this.sorted_buttons_hbox = hbox_sorted;
	}

	public HBox getSortedButtons_hbox() {
		return this.sorted_buttons_hbox;
	}

	public HBox getSortItButtons_hbox() {
		return this.sortit_buttons_hbox;
	}

	public HBox getAsItIsButtons_hbox() {
		return this.asitis_buttons_hbox;
	}

	public void setShowTableButton(Button button, boolean value) {
		ImageView iv = (ImageView) button.getGraphic();
		iv.setImage(show_im);
		button.setGraphic(iv);
		setButtonsState(button, value);
	}

	public void setHideTableButton(Button button, boolean value) {
		ImageView iv = (ImageView) button.getGraphic();
		iv.setImage(hide_im);
		button.setGraphic(iv);
		setButtonsState(button, value);
	}

	public Image getShow_im() {
		return show_im;
	}

	public Image getHide_im() {
		return hide_im;
	}

	private void updateTableWidths() {
		model_Main.tables();
	}

	private void setButtonsState(Button button, boolean showButton) {

//		Node tables_rootPaneNode = button.getParent().getParent().getParent();
		Messages.sprintf("tables_rootPaneNode: " + model_Main.tables().getTables_rootPane().getId());

		tables_rootPaneNodeLayoutBounds = UI_Tools.getNodeLayoutBounds(model_Main.tables().getTables_rootPane());

		button_Bounds = UI_Tools.getNodeLayoutBounds(button);

		Node node = button.getParent().getParent(); // VBox - Buttons -
		Messages.sprintf("Main for Table is : " + node.getId());

		// TextField - Tables
//		if (node instanceof VBox) {
//			VBox table_VBox = (VBox) node;
		int counter = 0;
		if (node instanceof VBox) {
			Messages.sprintf("NOD??????????: " + node);
			for (Node main_tableVBox_node : ((VBox) node).getChildren()) {
				Messages.sprintf("COUNTER: " + counter + " show? " + showButton + " main_tableVBox_node:=========== "
						+ main_tableVBox_node);
				if (main_tableVBox_node instanceof HBox && main_tableVBox_node.getId().equals("buttons_hbox")) {
					HBox hbox = (HBox) main_tableVBox_node;
					handleShowHide(hbox, showButton);
				} else if (main_tableVBox_node instanceof TableView<?>) {
					Messages.sprintf("table" + main_tableVBox_node.getId());
					TableView<?> tableView = (TableView<?>) main_tableVBox_node;
					handleShowHide(tableView, showButton);
				} else if (main_tableVBox_node instanceof FlowPane
						&& main_tableVBox_node.getId().equals("tableInformation_flowpane")) {
					FlowPane main_tableVBox_node_flowPane = (FlowPane) main_tableVBox_node;
					handleShowHide(main_tableVBox_node_flowPane, showButton);
				} else {
					Messages.sprintfError("NODE NOT FOUND: " + main_tableVBox_node);
				}
				counter++;
			}
		}
//			for (Node table_Node : table_VBox.getChildren()) {
//				Messages.sprintf("table_Node VBOX Found:" + table_Node);
//				if (table_Node instanceof HBox) {
//					// HBox
//					HBox topButtons_HBox = (HBox) table_Node;
//					if (showButton) {
//						topButtons_HBox.setVisible(false);
//					} else {
//						topButtons_HBox.setVisible(true);
//					}
////					setRegionSize(topButtons_HBox, button_Bounds.getWidth(), button_Bounds.getHeight());
//					for (Node buttons_Node : topButtons_HBox.getChildren()) {
//						Messages.sprintf("buttons: " + buttons_Node);
//						// Show/hide Button
//						if (buttons_Node instanceof FlowPane) {
//							FlowPane controlButtons_FlowPane = (FlowPane) buttons_Node;
//							if (controlButtons_FlowPane.isVisible()) {
//								controlButtons_FlowPane.setVisible(false);
//							} else {
//								controlButtons_FlowPane.setVisible(true);
//							}
//						}
//						if (buttons_Node instanceof Button) {
//							if (buttons_Node.getId().equals(button.getId())) {
//								if (showButton) {
//									setRegionSize(table_VBox, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
////									setRegionSize(table_Node, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
////									Messages.sprintf("showButton setting regionsize: " + button_Bounds.getWidth()
////											+ " height: " + button_Bounds.getHeight());
//									buttons_Node.setVisible(false);
//								} else {
//									setRegionSize(table_VBox, button_Bounds.getWidth(), button_Bounds.getHeight());
////									setRegionSize(table_Node, button_Bounds.getWidth(), button_Bounds.getHeight());
//									Messages.sprintf("!showButton setting regionsize: " + button_Bounds.getWidth()
//											+ " height: " + button_Bounds.getHeight());
//									buttons_Node.setVisible(true);
//								}
//							}
//						}
//
//						if (buttons_Node instanceof TextField) {
//							TextField tf = (TextField) buttons_Node;
//							if (tf.isVisible()) {
//								tf.setVisible(false);
//							} else {
//								tf.setVisible(true);
//							}
//						}
//
//						if (table_Node instanceof FlowPane) {
//							FlowPane fp = (FlowPane) table_Node;
//							fp.setVisible(!fp.isVisible());
//						}
//						if (table_Node instanceof TableView) {
//							TableView<?> table = (TableView<?>) table_Node;
//							table.setVisible(!table.isVisible());
//						}
//					}
//				}
//			}
//		}
	}

	private void handleShowHide(Pane pane, boolean showButton) {
		if (showButton) {
			pane.setVisible(false);
		} else {
			pane.setVisible(true);
		}

		Messages.sprintf("1Pane setVisible: " + pane.getId() + " isVisible? " + !showButton);
	}

	private void handleShowHide(TableView<?> tableView, boolean showButton) {
		tableView.setVisible(!showButton);
		Messages.sprintf("tableView setVisible: " + !showButton);
	}

	private void setRegionSize(Region node, double width, double height) {
		sprintf("node name is: " + node.getId() + " visible node hbox = " + visible);
		node.setPrefWidth(width);
		node.setMinWidth(width);
		// node.setMaxWidth(width);

	}

	public void setAccelerator(Button button, TableType tableType, int number) {
		KeyCode numberKey = null;
		if (number == 1) {
			numberKey = KeyCode.DIGIT1;
		} else if (number == 2) {
			numberKey = KeyCode.DIGIT2;
		} else if (number == 3) {
			numberKey = KeyCode.DIGIT3;
		}
		button.getScene().getAccelerators().put(new KeyCodeCombination(numberKey, KeyCombination.CONTROL_DOWN),
				(Runnable) () -> {
					if (tableType.equals(TableType.ASITIS)) {
						button.fire();
						hide_show_table(button, TableType.ASITIS.getType());
					} else if (tableType.equals(TableType.SORTED.getType())) {
						button.fire();
						hide_show_table(button, TableType.SORTED.getType());
					} else if (tableType.equals(TableType.SORTIT.getType())) {
						button.fire();
						hide_show_table(button, TableType.SORTIT.getType());
					}
				});
	}

//	void hide_show_table2(Button button, String tableType) {
//		if (tableType.equals(TableType.ASITIS.getType())) {
////			setAsItIs(show);
//		} else if (tableType.equals(TableType.SORTED.getType())) {
//
//		} else if (tableType.equals(TableType.SORTIT.getType())) {
//
//		}
//
//	}

	void hide_show_table(Button button, String tableType) {
		if (visible == 1) {
			return;
		}
		if (tableType.equals(TableType.ASITIS.getType())) {
			Messages.sprintf("Asitis button pressed");
			if (asitis_show_property.get()) {
				asitis_show_property.set(false);
				sprintf("asitis_show? " + asitis_show_property.get());
				setHideTableButton(button, asitis_show_property.get());
				model_Main.tables().getAsItIs_table().setVisible(asitis_show_property.get());
				updateTableVisible();
			} else {
				asitis_show_property.set(true);
				sprintf("asitis_show? " + asitis_show_property.get());
				setShowTableButton(button, asitis_show_property.get());
				model_Main.tables().getAsItIs_table().setVisible(asitis_show_property.get());
				updateTableVisible();
			}
		} else if (tableType.equals(TableType.SORTED.getType())) {
			Messages.sprintf("SORTED button pressed");
			if (sorted_show_property.get()) {
				sorted_show_property.set(false);
				updateTableVisible();
				sprintf("sorted_show? " + sorted_show_property.get());
				setHideTableButton(button, sorted_show_property.get());
				model_Main.tables().getSorted_table().setVisible(sorted_show_property.get());
			} else {
				sorted_show_property.set(true);
				sprintf("sorted_show? " + sorted_show_property);
				setShowTableButton(button, sorted_show_property.get());
				model_Main.tables().getSorted_table().setVisible(sorted_show_property.get());
				updateTableVisible();
			}
		} else if (tableType.equals(TableType.SORTIT.getType())) {
			Messages.sprintf("SORTIT button pressed");
			if (sortit_show_property.get()) {
				// Hiding
				sortit_show_property.set(false);
				updateTableVisible();
				setHideTableButton(button, sortit_show_property.get());
				model_Main.tables().getSortIt_table().setVisible(sortit_show_property.get());
			} else {
				// Showing
				sortit_show_property.set(true);
				setShowTableButton(button, sortit_show_property.get());
				model_Main.tables().getSortIt_table().setVisible(sortit_show_property.get());
				updateTableVisible();
			}
		} else {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
	}

	public void updateTableVisible() {
		visible = 0;

		if (asitis_show_property.get()) {
			visible++;
		}
		if (sorted_show_property.get()) {
			visible++;
		}
		if (sortit_show_property.get()) {
			visible++;
		}
		sprintf("Visible is: " + visible);
		tables_rootPaneNodeLayoutBounds = UI_Tools.getNodeLayoutBounds(model_Main.tables().getTables_rootPane());

//		Messages.sprintf("tables_rootPaneNodeLayoutBounds WIDTH: " + tables_rootPaneNodeLayoutBounds.getWidth());
		if (visible >= 1) {
			if (asitis_show_property.get() == true) {
				setTableViewWidth(model_Main.tables().getAsItIs_table(), true);
				Messages.sprintf("asitis_show: " + model_Main.tables().getAsItIs_table().getPrefWidth() + " prefWidth: "
						+ Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible));
			}
			if (sorted_show_property.get() == true) {
				setTableViewWidth(model_Main.tables().getSorted_table(), true);
				Messages.sprintf("sorted_show: " + model_Main.tables().getSorted_table().getPrefWidth() + " prefwidth: "
						+ Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible));
			}
			if (sortit_show_property.get() == true) {
				setTableViewWidth(model_Main.tables().getSortIt_table(), true);
				Messages.sprintf("sortit_show: " + model_Main.tables().getSortIt_table().getPrefWidth() + " prefWidth: "
						+ Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible));
			}
		}
		Messages.sprintf("tables_rootPaneNodeLayoutBounds WIDTH: " + tables_rootPaneNodeLayoutBounds.getWidth());
		sprintf("Visible table total is: " + visible + " maxWidth " + maxWidth.get());

	}

	private void setTableViewWidth(TableView<FolderInfo> table, boolean show) {
		int hidden = 3 - visible;
		if (!show) {
			Platform.runLater(() -> {
//			table.setPrefWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
//					- (button_Bounds.getWidth() * (hidden)));
				table.setPrefWidth(0);
				table.setMinWidth(0);
				table.setMaxWidth(0);
//				table.setMaxWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
//						- (button_Bounds.getWidth() * (hidden)));
			});
		} else {
			Platform.runLater(() -> {
//			table.setPrefWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
//					- (button_Bounds.getWidth() * (hidden)));
				table.setPrefWidth(Region.USE_PREF_SIZE);
				table.setMinWidth(Region.USE_PREF_SIZE);
//				table.setMaxWidth(Region.USE_COMPUTED_SIZE);
				table.setMaxWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
						- (button_Bounds.getWidth() * (hidden)));
			});
		}

	}
}
