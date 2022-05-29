/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.messages.Messages.sprintf;

import com.girbola.Main;
import com.girbola.configuration.GUIPrefs;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import common.utils.ui.UI_Tools;
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

	private Model_main model;

	private Image show_im;
	private Image hide_im;

	public HideButtons(Model_main model) {
		this.model = model;
		visible = 3;
		show_im = GUI_Methods.loadImage("showTable.png", GUIPrefs.BUTTON_WIDTH);
		hide_im = GUI_Methods.loadImage("hideTable.png", GUIPrefs.BUTTON_WIDTH);

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

	private void setShowTableButton(Button button, boolean value) {
		ImageView iv = (ImageView) button.getGraphic();
		iv.setImage(show_im);
		button.setGraphic(iv);
		setButtonsState(button, value);
	}

	private void setHideTableButton(Button button, boolean value) {
		ImageView iv = (ImageView) button.getGraphic();
		iv.setImage(hide_im);
		button.setGraphic(iv);
		setButtonsState(button, value);
	}

	private void updateTableWidths() {
		model.tables();
	}
	
	private void setButtonsState(Button button, boolean showButton) {
		Bounds bound = UI_Tools.getNodeLayoutBounds(button);

		Node node = button.getParent().getParent(); // VBox - Buttons -
		// TextField - Tables
		if (node instanceof VBox) {
			VBox table_VBox = (VBox) node;
			for (Node table_Node : table_VBox.getChildren()) {
				Messages.sprintf("table_Node VBOX Found:" + table_Node);
				if (table_Node instanceof HBox) {
					// HBox
					HBox topButtons_HBox = (HBox) table_Node;
					setRegionSize(topButtons_HBox, bound.getWidth(), bound.getHeight());
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
									Messages.sprintf("showButton setting regionsize: " + bound.getWidth() + " height: " + bound.getHeight());
								} else {
									setRegionSize(table_VBox, bound.getWidth(), bound.getHeight());
									Messages.sprintf("!showButton setting regionsize: " + bound.getWidth() + " height: " + bound.getHeight());
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
		if (tableType.equals(TableType.ASITIS.getType())) {
			Messages.sprintf("Asitis button pressed");
			if (asitis_show.get()) {
				updateTableVisible();
				if (visible == 1) {
					return;
				}
				asitis_show.set(false);
				sprintf("asitis_show? " + asitis_show.get());
				setHideTableButton(button, asitis_show.get());
				model.tables().getAsItIs_table().setVisible(asitis_show.get());

			} else {
				sprintf("asitis_show? " + asitis_show.get());
				asitis_show.set(true);
				setShowTableButton(button, asitis_show.get());
				model.tables().getAsItIs_table().setVisible(asitis_show.get());
				updateTableVisible();

			}
		} else if (tableType.equals(TableType.SORTED.getType())) {
			Messages.sprintf("SORTED button pressed");
			if (sorted_show.get()) {
				updateTableVisible();
				if (visible == 1) {
					return;
				}
				sorted_show.set(false);
				sprintf("sorted_show? " + sorted_show.get());
				setHideTableButton(button, sorted_show.get());
				model.tables().getSorted_table().setVisible(sorted_show.get());
			} else {
				sorted_show.set(true);
				sprintf("sorted_show? " + sorted_show);
				setShowTableButton(button, sorted_show.get());
				model.tables().getSorted_table().setVisible(sorted_show.get());
				updateTableVisible();
			}
		} else if (tableType.equals(TableType.SORTIT.getType())) {
			Messages.sprintf("SORTIT button pressed");
			if (sortit_show.get()) {
				// Hiding
				updateTableVisible();
				if (visible == 1) {
					return;
				}
				sortit_show.set(false);
				setHideTableButton(button, sortit_show.get());
				model.tables().getSortIt_table().setVisible(sortit_show.get());
			} else {
				// Showing
				sortit_show.set(true);
				setShowTableButton(button, sortit_show.get());
				model.tables().getSortIt_table().setVisible(sortit_show.get());
				updateTableVisible();
			}
		} else {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}

	}

	public int getTest() {
		return this.test;
	}

	private void updateTableVisible() {
		visible = 0;
		sprintf("Visible is: " + visible);
		if (asitis_show.get() == true) {
			visible++;
		}
		if (sorted_show.get() == true) {
			visible++;
		}
		if (sortit_show.get() == true) {
			visible++;
		}
		maxWidth .set((Main.getMain_stage().getMaxWidth()));
		maxWidth.set( Math.floor(Main.getMain_stage().getMaxWidth()) / visible);
		sprintf("Visible table total is: " + visible + " maxWidth " + maxWidth.get());

	}
}
