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

	private SimpleBooleanProperty asitis_show = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty sortit_show = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty sorted_show = new SimpleBooleanProperty(true);

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

		// TextField - Tables
		if (node instanceof VBox) {
			VBox table_VBox = (VBox) node;
			for (Node table_Node : table_VBox.getChildren()) {
				Messages.sprintf("table_Node VBOX Found:" + table_Node);
				if (table_Node instanceof HBox) {
					// HBox
					HBox topButtons_HBox = (HBox) table_Node;
					setRegionSize(topButtons_HBox, button_Bounds.getWidth(), button_Bounds.getHeight());
					for (Node buttons_Node : topButtons_HBox.getChildren()) {
						Messages.sprintf("buttons: " + buttons_Node);
						// Show/hide Button
						if (buttons_Node instanceof FlowPane) {
							FlowPane controlButtons_FlowPane = (FlowPane) buttons_Node;
							controlButtons_FlowPane.setVisible(!controlButtons_FlowPane.isVisible());
						}
						if (buttons_Node instanceof Button) {
							if (buttons_Node.getId().equals(button.getId())) {
								if (showButton) {
									setRegionSize(table_VBox, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
//									setRegionSize(table_Node, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
									Messages.sprintf("showButton setting regionsize: " + button_Bounds.getWidth()
											+ " height: " + button_Bounds.getHeight());
								} else {
									setRegionSize(table_VBox, button_Bounds.getWidth(), button_Bounds.getHeight());
//									setRegionSize(table_Node, button_Bounds.getWidth(), button_Bounds.getHeight());
									Messages.sprintf("!showButton setting regionsize: " + button_Bounds.getWidth()
											+ " height: " + button_Bounds.getHeight());
								}
							} else {
								buttons_Node.setVisible(!buttons_Node.isVisible());
							}
						}

						if (buttons_Node instanceof TextField) {
							TextField tf = (TextField) buttons_Node;
							tf.setVisible(!tf.isVisible());
						}
					}
				}
				if (table_Node instanceof FlowPane) {
					FlowPane fp = (FlowPane) table_Node;
					fp.setVisible(!fp.isVisible());
				}
				if (table_Node instanceof TableView) {
					TableView<?> table = (TableView<?>) table_Node;
					table.setVisible(!table.isVisible());
				}
			}
		}
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
			if (asitis_show.get()) {
				asitis_show.set(false);
				updateTableVisible();
				sprintf("asitis_show? " + asitis_show.get());
				setHideTableButton(button, asitis_show.get());
				model_Main.tables().getAsItIs_table().setVisible(asitis_show.get());

			} else {
				sprintf("asitis_show? " + asitis_show.get());
				asitis_show.set(true);
				setShowTableButton(button, asitis_show.get());
				model_Main.tables().getAsItIs_table().setVisible(asitis_show.get());
				updateTableVisible();
			}
		} else if (tableType.equals(TableType.SORTED.getType())) {
			Messages.sprintf("SORTED button pressed");
			if (sorted_show.get()) {
				sorted_show.set(false);
				updateTableVisible();
				sprintf("sorted_show? " + sorted_show.get());
				setHideTableButton(button, sorted_show.get());
				model_Main.tables().getSorted_table().setVisible(sorted_show.get());
			} else {
				sorted_show.set(true);
				sprintf("sorted_show? " + sorted_show);
				setShowTableButton(button, sorted_show.get());
				model_Main.tables().getSorted_table().setVisible(sorted_show.get());
				updateTableVisible();
			}
		} else if (tableType.equals(TableType.SORTIT.getType())) {
			Messages.sprintf("SORTIT button pressed");
			if (sortit_show.get()) {
				// Hiding
				sortit_show.set(false);
				updateTableVisible();
				setHideTableButton(button, sortit_show.get());
				model_Main.tables().getSortIt_table().setVisible(sortit_show.get());
			} else {
				// Showing
				sortit_show.set(true);
				setShowTableButton(button, sortit_show.get());
				model_Main.tables().getSortIt_table().setVisible(sortit_show.get());
				updateTableVisible();
			}
		} else {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
	}

	private void updateTableVisible() {
		visible = 0;

		if (asitis_show.get()) {
			visible++;
		}
		if (sorted_show.get()) {
			visible++;
		}
		if (sortit_show.get()) {
			visible++;
		}
		sprintf("Visible is: " + visible);
		tables_rootPaneNodeLayoutBounds = UI_Tools.getNodeLayoutBounds(model_Main.tables().getTables_rootPane());

//		Messages.sprintf("tables_rootPaneNodeLayoutBounds WIDTH: " + tables_rootPaneNodeLayoutBounds.getWidth());
		if (visible >= 1) {
			if (asitis_show.get() == true) {
				setTableViewWidth(model_Main.tables().getSorted_table());
				Messages.sprintf("asitis_show: " + model_Main.tables().getAsItIs_table().getPrefWidth() + " prefWidth: "
						+ Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible));
			}
			if (sorted_show.get() == true) {
				setTableViewWidth(model_Main.tables().getSorted_table());
				Messages.sprintf("sorted_show: " + model_Main.tables().getSorted_table().getPrefWidth() + " prefwidth: "
						+ Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible));
			}
			if (sortit_show.get() == true) {
				setTableViewWidth(model_Main.tables().getSortIt_table());
				Messages.sprintf("sortit_show: " + model_Main.tables().getSortIt_table().getPrefWidth() + " prefWidth: "
						+ Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible));
			}
		}
		Messages.sprintf("tables_rootPaneNodeLayoutBounds WIDTH: " + tables_rootPaneNodeLayoutBounds.getWidth());
		sprintf("Visible table total is: " + visible + " maxWidth " + maxWidth.get());

	}

	private void setTableViewWidth(TableView<FolderInfo> table) {
		int hidden = 3 - visible;
		Platform.runLater(() -> {
			table.setPrefWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
					- (button_Bounds.getWidth() * (hidden)));
			table.setMinWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
					- (button_Bounds.getWidth() * (hidden)));
			table.setMaxWidth(Math.floor(tables_rootPaneNodeLayoutBounds.getWidth() / visible)
					- (button_Bounds.getWidth() * (hidden)));
		});
	}

}
